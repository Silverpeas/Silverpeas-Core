/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.io;

import org.silverpeas.io.process.AbstractIOProcess;
import org.silverpeas.io.session.IOSession;
import org.silverpeas.io.session.SimpleIOSession;

import com.silverpeas.annotation.Service;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DefaultIOProcessManagement implements IOProcessManagement {

  /*
   * (non-Javadoc)
   * @see org.silverpeas.io.IOProcessManagement#execute(org.silverpeas.io.process.AbstractIOProcess)
   */
  @Override
  public <R> R execute(final AbstractIOProcess<R> process) throws Exception {

    // Initialization
    final InternalContext context = new InternalContext();
    final FileHandler fileHandler = new FileHandler(context.session);

    // Processing
    beforeProcessing(context, fileHandler, process);
    filesProcessing(context, fileHandler, process);
    checksProcessing(context, fileHandler, process);
    final R result = onSuccessfulProcessing(context, fileHandler, process);

    // Commits
    commitFiles(context, fileHandler);

    // Return the result of the successful method
    return result;
  }

  /**
   * Before processing
   * @param context
   * @param fileHandler
   * @param process
   * @throws Exception
   */
  private <R> void beforeProcessing(final InternalContext context, final FileHandler fileHandler,
      final AbstractIOProcess<R> process) throws Exception {
    if (!context.isProcessInError) {
      try {
        process.processBefore(context.session);

        // LOG
        SilverTrace.info("IOProcessManagement", "IOProcess.processBefore()",
            "ioProcess.PROCESS_BEFORE_DONE",
            new StringBuilder("ioSessionId = ").append(context.session.getId()).toString());

      } catch (final Exception exception) {
        handleError(context, fileHandler, process, IOErrorType.DURING_BEFORE_PROCESSING, exception);
      }
    }
  }

  /**
   * Files processing
   * @param context
   * @param fileHandler
   * @param process
   * @throws Exception
   */
  private <R> void filesProcessing(final InternalContext context, final FileHandler fileHandler,
      final AbstractIOProcess<R> process) throws Exception {
    if (!context.isProcessInError) {
      try {
        process.processFiles(context.session, fileHandler);

        // LOG
        SilverTrace.info("IOProcessManagement", "IOProcess.processFiles()",
            "ioProcess.PROCESS_FILES_DONE",
            new StringBuilder("ioSessionId = ").append(context.session.getId()).toString());

      } catch (final Exception exception) {
        handleError(context, fileHandler, process, IOErrorType.DURING_FILES_PROCESSING, exception);
      }
    }
  }

  /**
   * Checks processing
   * @param context
   * @param fileHandler
   * @param process
   */
  private <R> void checksProcessing(final InternalContext context, final FileHandler fileHandler,
      final AbstractIOProcess<R> process) throws Exception {
    if (!context.isProcessInError) {
      synchronized (IOMonitoring.SYNCHRONIZE) {
        try {
          IOFactory.getChecker().checks(context.session, fileHandler);

          // LOG
          SilverTrace.info("IOProcessManagement", "IOChecker.check()", "ioChecker.CHECKS_DONE",
              new StringBuilder("ioSessionId = ").append(context.session.getId()).toString());

        } catch (final Exception exception) {
          handleError(context, fileHandler, process, IOErrorType.DURING_CHECKS_PROCESSING,
              exception);
        }
      }
    }
  }

  /**
   * On successful processing
   * @param context
   * @param fileHandler
   * @param process
   */
  private <R> R onSuccessfulProcessing(final InternalContext context,
      final FileHandler fileHandler, final AbstractIOProcess<R> process) throws Exception {
    R result = null;
    if (!context.isProcessInError) {
      try {
        result = process.onSuccessful(context.session);

        // LOG
        SilverTrace.info("IOProcessManagement", "IOProcess.onSuccessful()",
            "ioProcess.ON_SUCCESSFUL_DONE",
            new StringBuilder("ioSessionId = ").append(context.session.getId()).toString());

      } catch (final Exception exception) {
        handleError(context, fileHandler, process, IOErrorType.DURING_ON_SUCESSFULL_PROCESSING,
            exception);
      }
    }
    return result;
  }

  /**
   * IO error handling
   * @param context
   * @param fileHandler
   * @param process
   * @param errorType
   * @param exception
   * @throws Exception
   */
  private <R> void handleError(final InternalContext context, final FileHandler fileHandler,
      final AbstractIOProcess<R> process, final IOErrorType errorType, final Exception exception)
      throws Exception {
    context.isProcessInError = true;
    try {

      // Error
      SilverTrace.error(
          "IOProcessManagement",
          "IOProcess.onFailure()",
          "ioProcess.EX_IO_PROCESS_FAILURE",
          new StringBuilder("ioSessionId = ").append(context.session.getId())
              .append(", ioErrorType = ").append(errorType.name())
              .append(", exceptionClassName = ").append(exception.getClass().getName()).toString(),
          exception);

      // Treatment
      process.onFailure(context.session, errorType, exception);

    } finally {
      rollbackFiles(context, fileHandler);
    }
  }

  /**
   * @param context
   * @param fileHandler
   */
  private void commitFiles(final InternalContext context, final FileHandler fileHandler)
      throws Exception {
    if (!context.isProcessInError) {
      synchronized (IOMonitoring.SYNCHRONIZE) {
        try {
          fileHandler.checkinSessionWorkingPath();

          // LOG
          SilverTrace.info("IOProcessManagement", "IOProcess.commitFiles()",
              "ioProcess.COMMIT_FILES_DONE",
              new StringBuilder("ioSessionId = ").append(context.session.getId()).toString());

        } finally {
          clearSession(context, fileHandler);
        }
      }
    }
  }

  /**
   * @param context
   * @param fileHandler
   */
  private void rollbackFiles(final InternalContext context, final FileHandler fileHandler) {
    synchronized (IOMonitoring.SYNCHRONIZE) {
      try {

        // LOG
        SilverTrace.info("IOProcessManagement", "IOProcess.rollbackFiles()",
            "ioProcess.ROLLBACK_FILES_DONE",
            new StringBuilder("ioSessionId = ").append(context.session.getId()).toString());

      } finally {
        clearSession(context, fileHandler);
      }
    }
  }

  /**
   * Clearing session
   * @param context
   * @param fileHandler
   */
  private void clearSession(final InternalContext context, final FileHandler fileHandler) {
    fileHandler.deleteSessionWorkingPath();

    // LOG
    SilverTrace.info("IOProcessManagement", "IOProcess.clearSession()",
        "ioProcess.SESSION_CLEARED",
        new StringBuilder("ioSessionId = ").append(context.session.getId()).toString());
  }

  /**
   * @author Yohann Chastagnier
   */
  private final class FileHandler extends org.silverpeas.io.file.FileHandler {
    protected FileHandler(final IOSession session) {
      super(session);
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.io.file.AbstractFileHandler#deleteSessionWorkingPath()
     */
    @Override
    public void deleteSessionWorkingPath() {
      super.deleteSessionWorkingPath();
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.io.file.AbstractFileHandler#checkinSessionWorkingPath()
     */
    @Override
    public void checkinSessionWorkingPath() throws Exception {
      super.checkinSessionWorkingPath();
    }
  }

  /**
   * Internal context
   * @author Yohann Chastagnier
   */
  private class InternalContext {
    final public IOSession session;
    public boolean isProcessInError = false;

    public InternalContext() {
      session = SimpleIOSession.create();
    }
  }
}

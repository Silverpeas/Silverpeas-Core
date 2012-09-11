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
package org.silverpeas.process.management;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.silverpeas.process.ProcessFactory;
import org.silverpeas.process.SilverpeasProcess;
import org.silverpeas.process.session.Session;
import org.silverpeas.process.util.ProcessList;

import com.silverpeas.annotation.Service;
import com.silverpeas.util.CollectionUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Default implementation of the <code>ProcessManagement</code> interface.
 * @author Yohann Chastagnier
 */
@Service
public class DefaultProcessManagement implements ProcessManagement {

  private final Map<Long, LinkedList<InternalContext<?>>> parentFileTransactions =
      new ConcurrentHashMap<Long, LinkedList<InternalContext<?>>>();

  /*
   * (non-Javadoc)
   * @see
   * org.silverpeas.io.ProcessManagement#execute(com.stratelia.webactiv.beans.admin.UserDetail,
   * java.lang.String, org.silverpeas.io.process.SilverpeasProcess)
   */
  @Override
  public <C extends ProcessExecutionContext> void execute(final SilverpeasProcess<C> process,
      final C processExecutionContext) throws Exception {
    execute(new ProcessList<C>(process), processExecutionContext);
  }

  /*
   * All methods of this process management have to call this one.
   * @see
   * org.silverpeas.process.management.ProcessManagement#execute(org.silverpeas.process.util.ProcessList
   * , org.silverpeas.process.management.ProcessExecutionContext)
   */
  @Override
  public <C extends ProcessExecutionContext> void execute(final ProcessList<C> processes,
      final C processExecutionProcess) throws Exception {
    if (processes.isNotEmpty()) {

      // Context
      InternalContext<C> context = null;
      final Long threadId = Thread.currentThread().getId();
      LinkedList<InternalContext<?>> internalParentContexts = parentFileTransactions.get(threadId);
      try {

        // Required or New file transaction ?
        if (internalParentContexts != null &&
            !processExecutionProcess.isRequiresNewFileTransaction()) {
          // Attaching session of the last registred transaction to the current transaction
          context =
              new InternalContext<C>(processExecutionProcess, internalParentContexts.peekLast()
                  .getSession());
          // Attaching FileHandler of last registred transaction to the current transaction
          processExecutionProcess.setFileHandler(internalParentContexts.peekLast()
              .getProcessExecutionContext().getFileHandler());
        } else {
          // Prepare the context saving : new transaction case
          if (internalParentContexts == null) {
            internalParentContexts = new LinkedList<InternalContext<?>>();
            parentFileTransactions.put(threadId, internalParentContexts);
          }
          // Initializing the new context
          context = new InternalContext<C>(processExecutionProcess);
          processExecutionProcess.setFileHandler(new FileHandler(context.getSession()));
          // Saving the context
          internalParentContexts.add(context);
        }

        // Session parameters
        for (final Map.Entry<String, Object> parameter : processes.getSessionParameters()
            .entrySet()) {
          context.getSession().setAttribute(parameter.getKey(), parameter.getValue());
        }

        // Processing files
        for (final SilverpeasProcess<C> process : processes.getList()) {
          if (context.isProcessInError()) {
            break;
          }
          context.update(process);
          // Perform
          processing(process, context);
        }

        // Checks
        if (context.isOpeningFileTransaction()) {
          checks(context);
        }

        // Processing on successful
        for (final SilverpeasProcess<C> process : processes.getList()) {
          if (context.isProcessInError()) {
            break;
          }
          onSuccessful(process, context);
        }

        // Commits
        commit(context);

      } finally {
        if (context != null && context.isOpeningFileTransaction()) {
          internalParentContexts.pollLast();
          if (CollectionUtil.isEmpty(internalParentContexts)) {
            parentFileTransactions.remove(threadId);
          }
        }
      }
    }
  }

  /**
   * Files processing
   * @param process
   * @param context
   * @throws Exception
   */
  private <C extends ProcessExecutionContext> void processing(final SilverpeasProcess<C> process,
      final InternalContext<C> context) throws Exception {
    if (!context.isProcessInError()) {
      try {
        process.process(context.getProcessExecutionContext(), context.getSession());

        // LOG
        SilverTrace.debug("Process", "processManagement.processFiles()",
            "processManagement.MAIN_PROCESSING_DONE",
            new StringBuilder("SessionId = ").append(context.getSession().getId()).toString());

      } catch (final Exception exception) {
        handleError(context, ProcessErrorType.DURING_MAIN_PROCESSING, exception);
      }
    }
  }

  /**
   * Checks processing
   * @param context
   */
  private <C extends ProcessExecutionContext> void checks(final InternalContext<C> context)
      throws Exception {
    if (!context.isProcessInError()) {
      synchronized (ProcessMonitoring.SYNCHRONIZE) {
        try {
          ProcessFactory.getChecker().checks(context.getProcessExecutionContext(),
              context.getSession(), context.getCheckTypesToProcess());

          // LOG
          SilverTrace.debug("Process", "IOChecker.check()", "ioChecker.CHECKS_DONE",
              new StringBuilder("SessionId = ").append(context.getSession().getId()).toString());

        } catch (final Exception exception) {
          handleError(context, ProcessErrorType.DURING_CHECKS_PROCESSING, exception);
        }
      }
    }
  }

  /**
   * On successful processing
   * @param process
   * @param context
   */
  private <C extends ProcessExecutionContext> void onSuccessful(final SilverpeasProcess<C> process,
      final InternalContext<C> context) throws Exception {
    if (!context.isProcessInError()) {
      try {
        process.onSuccessful(context.getProcessExecutionContext(), context.getSession());

        // LOG
        SilverTrace.debug("Process", "processManagement.onSuccessful()",
            "processManagement.ON_SUCCESSFUL_DONE",
            new StringBuilder("SessionId = ").append(context.getSession().getId()).toString());

      } catch (final Exception exception) {
        handleError(context, ProcessErrorType.DURING_ON_SUCESSFULL_PROCESSING, exception);
      }
    }
  }

  /**
   * IO error handling
   * @param context
   * @param errorType
   * @param exception
   * @throws Exception
   */
  private <C extends ProcessExecutionContext> void handleError(final InternalContext<C> context,
      final ProcessErrorType errorType, final Exception exception) throws Exception {
    context.setProcessInError();
    try {

      // Error
      SilverTrace.error(
          "Process",
          "processManagement.onFailure()",
          "processManagement.EX_IO_PROCESS_FAILURE",
          new StringBuilder("SessionId = ").append(context.getSession().getId())
              .append(", ioErrorType = ").append(errorType.name())
              .append(", exceptionClassName = ").append(exception.getClass().getName()).toString(),
          exception);

      // Treatment
      ProcessErrorType firstErrorType = null;
      Exception continueException = null;
      for (final SilverpeasProcess<C> processStarted : context.getProcessesStarted()) {
        try {
          processStarted.onFailure(context.getProcessExecutionContext(), context.getSession(),
              (firstErrorType == null || ProcessErrorType.DURING_CHECKS_PROCESSING
                  .equals(errorType)) ? errorType : ProcessErrorType.OTHER_PROCESS_FAILED,
              exception);
        } catch (final Exception e) {
          if (continueException == null) {
            continueException = e;
          }
        } finally {
          if (firstErrorType == null) {
            firstErrorType = errorType;
          }
        }
      }

      if (continueException != null) {
        throw continueException;
      }

    } finally {
      rollback(context);
    }
  }

  /**
   * @param context
   * @param fileHandler
   */
  private <C extends ProcessExecutionContext> void commit(final InternalContext<C> context)
      throws Exception {
    if (context.isOpeningFileTransaction() && !context.isProcessInError()) {
      synchronized (ProcessMonitoring.SYNCHRONIZE) {
        try {
          ((FileHandler) context.getProcessExecutionContext().getFileHandler())
              .checkinSessionWorkingPath();

          // LOG
          SilverTrace.debug("Process", "processManagement.commit()",
              "processManagement.COMMIT_FILES_DONE",
              new StringBuilder("SessionId = ").append(context.getSession().getId()).toString());

        } finally {
          clearSession(context);
        }
      }
    }
  }

  /**
   * @param context
   * @param fileHandler
   */
  private <C extends ProcessExecutionContext> void rollback(final InternalContext<C> context) {
    if (context.isOpeningFileTransaction()) {
      synchronized (ProcessMonitoring.SYNCHRONIZE) {
        try {

          // LOG
          SilverTrace.debug("Process", "processManagement.rollback()",
              "processManagement.ROLLBACK_FILES_DONE",
              new StringBuilder("SessionId = ").append(context.getSession().getId()).toString());

        } finally {
          clearSession(context);
        }
      }
    }
  }

  /**
   * Clearing session
   * @param context
   */
  private <C extends ProcessExecutionContext> void clearSession(final InternalContext<C> context) {
    if (context.isOpeningFileTransaction()) {
      ((FileHandler) context.getProcessExecutionContext().getFileHandler())
          .deleteSessionWorkingPath();

      // LOG
      SilverTrace.debug("Process", "processManagement.clearSession()",
          "processManagement.SESSION_CLEARED",
          new StringBuilder("SessionId = ").append(context.getSession().getId()).toString());
    }
  }

  /**
   * @author Yohann Chastagnier
   */
  private final class FileHandler extends org.silverpeas.process.io.file.FileHandler {
    protected FileHandler(final Session session) {
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
}

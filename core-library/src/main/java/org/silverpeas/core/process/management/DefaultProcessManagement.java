/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.process.management;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.process.SilverpeasProcess;
import org.silverpeas.core.process.check.ProcessCheck;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.core.process.util.ProcessList;
import org.silverpeas.core.util.CollectionUtil;

import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Map;

/**
 * Default implementation of the <code>ProcessManagement</code> interface.
 * @author Yohann Chastagnier
 */
@Singleton
public class DefaultProcessManagement implements ProcessManagement {

  private final ThreadLocal<LinkedList<InternalContext<?>>> parentFileTransactions =
      new ThreadLocal<>();

  /*
   * (non-Javadoc)
   * @see
   * org.silverpeas.io.ProcessManagement#execute(org.silverpeas.core.admin.user.model.UserDetail,
   * java.lang.String, org.silverpeas.io.process.SilverpeasProcess)
   */
  @SuppressWarnings("unchecked")
  @Override
  public <C extends ProcessExecutionContext> void execute(final SilverpeasProcess<C> process,
      final C processExecutionContext) throws Exception {
    execute(new ProcessList<C>(process), processExecutionContext);
  }

  /*
   * All methods of this process management have to call this one.
   * @see
   * ProcessManagement#execute(ProcessList
   * , ProcessExecutionContext)
   */
  @Override
  public <C extends ProcessExecutionContext> void execute(final ProcessList<C> processes,
      final C processExecutionContext) throws Exception {
    if (processes.isNotEmpty()) {

      // Context
      InternalContext<C> context = null;
      LinkedList<InternalContext<?>> internalParentContexts = parentFileTransactions.get();
      try {

        // Required or New file transaction ?
        if (internalParentContexts != null && !processExecutionContext.requiresNewFileTransaction()) {
          // Attaching internal context to parents
          context =
              new InternalContext<>(internalParentContexts.peekLast().getLast(),
                  processExecutionContext);
        } else {
          // Prepare the context saving : new transaction case
          if (internalParentContexts == null) {
            internalParentContexts = new LinkedList<>();
            parentFileTransactions.set(internalParentContexts);
          }
          // Initializing the new context
          context = new InternalContext<>(processExecutionContext);
          processExecutionContext.setFileHandler(new FileHandler(context.getSession()));
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
          if (context.isFileTransactionInError()) {
            break;
          }
          context.update(process);
          // Perform
          processing(process, context);
        }

        // Next treatment on transaction opener
        if (context.isOpeningFileTransaction() && !context.isFileTransactionInError()) {

          // Checks
          checks(context);

          // Are checks OK ?
          if (!context.isFileTransactionInError()) {
            try {

              // Processing 'on successful'
              InternalContext<? extends ProcessExecutionContext> curContext = context.getLast();
              while (curContext != null) {
                for (final SilverpeasProcess<?> process : curContext.getOnSuccessfulProcesses()) {
                  onSuccessful(process, curContext);
                }
                curContext = curContext.getPrevious();
              }
            } finally {

              // Commits
              commit(context);
            }
          }
        }
      } finally {
        if (context != null && context.isOpeningFileTransaction()) {
          internalParentContexts.pollLast();
          if (CollectionUtil.isEmpty(internalParentContexts)) {
            parentFileTransactions.remove();
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
    try {
      process.process(context.getProcessExecutionContext(), context.getSession());
    } catch (final Exception exception) {
      handleError(context, ProcessErrorType.DURING_MAIN_PROCESSING, exception, process);
    }
  }

  /**
   * Checks processing
   * @param context
   */
  private void checks(final InternalContext<?> context) throws Exception {
    synchronized (ProcessMonitoring.SYNCHRONIZE) {
      try {
        for (final ProcessCheck check : ProcessCheckRegistration.getChecks()) {
          if (context.getCheckTypesToProcess().contains(check.getType())) {
            check.check(context.getProcessExecutionContext());
          }
        }
      } catch (final Exception exception) {
        handleError(context, ProcessErrorType.DURING_CHECKS_PROCESSING, exception, null);
      }
    }
  }

  /**
   * On successful processing
   * @param process
   * @param context
   */
  private void onSuccessful(final SilverpeasProcess<?> process, final InternalContext<?> context)
      throws Exception {
    try {
      process.onSuccessful();
    } catch (final Throwable throwable) {

      // LOG
      SilverTrace.error("Process", "processManagement.onSuccessful()",
          "processManagement.ON_SUCCESSFUL_ERROR",
          new StringBuilder("SessionId = ").append(context.getSession().getId()).toString(),
          throwable);
    }
  }

  /**
   * ERROR HANDLER
   * @param context
   * @param errorType
   * @param exception
   * @throws Exception
   */
  private void handleError(final InternalContext<?> context, ProcessErrorType errorType,
      Exception exception, SilverpeasProcess<?> fromProcess) throws Exception {
    if (!context.isFileTransactionInError()) {
      context.setFileTransactionInError(errorType, exception, fromProcess);
    }
    if (context.isOpeningFileTransaction()) {
      try {

        errorType = context.getErrorType();
        exception = context.getException();
        fromProcess = context.getProcessInError();

        // Error
        SilverTrace.error(
            "Process",
            "processManagement.onFailure()",
            "processManagement.EX_PROCESS_FAILURE",
            new StringBuilder("SessionId = ").append(context.getSession().getId())
                .append(", ioErrorType = ").append(errorType.name())
                .append(", exceptionClassName = ").append(exception.getClass().getName())
                .toString(), exception);

        // Treatment
        Exception continueException = null;
        InternalContext<? extends ProcessExecutionContext> curContext = context.getLast();
        while (curContext != null) {
          for (final SilverpeasProcess<?> processStarted : curContext.getOnFailureProcesses()) {
            try {
              processStarted
                  .onFailure(
                      (ProcessErrorType.DURING_CHECKS_PROCESSING.equals(errorType) || processStarted == fromProcess)
                          ? errorType : ProcessErrorType.OTHER_PROCESS_FAILED, exception);
            } catch (final Exception e) {
              if (continueException == null) {
                continueException = e;
              }
            }
          }
          curContext = curContext.getPrevious();
        }

        if (continueException != null) {
          throw continueException;
        }

      } finally {
        rollback(context);
      }
    } else {
      throw exception;
    }
  }

  /**
   * COMMIT
   * @param context
   * @throws Exception
   */
  private void commit(final InternalContext<?> context) throws Exception {
    synchronized (ProcessMonitoring.SYNCHRONIZE) {
      try {
        ((FileHandler) context.getProcessExecutionContext().getFileHandler())
            .checkinSessionWorkingPath();
      } finally {
        clearSession(context);
      }
    }
  }

  /**
   * ROLLBACK
   * @param context
   */
  private void rollback(final InternalContext<?> context) {
    if (context.isOpeningFileTransaction()) {
      synchronized (ProcessMonitoring.SYNCHRONIZE) {
        try {
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
  private void clearSession(final InternalContext<?> context) {
    if (context.isOpeningFileTransaction()) {
      ((FileHandler) context.getProcessExecutionContext().getFileHandler())
          .deleteSessionWorkingPath();
    }
  }

  /**
   * @author Yohann Chastagnier
   */
  private final class FileHandler extends org.silverpeas.core.process.io.file.FileHandler {
    protected FileHandler(final ProcessSession session) {
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

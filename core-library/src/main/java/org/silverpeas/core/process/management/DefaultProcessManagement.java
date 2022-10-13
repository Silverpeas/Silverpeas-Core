/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.process.management;

import org.silverpeas.core.process.SilverpeasProcess;
import org.silverpeas.core.process.check.ProcessCheck;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.core.process.util.ProcessList;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

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
   *
   * org.silverpeas.io.ProcessManagement#execute(org.silverpeas.core.admin.user.model.UserDetail,
   * java.lang.String, org.silverpeas.io.process.SilverpeasProcess)
   */
  @Override
  public <C extends ProcessExecutionContext> void execute(final SilverpeasProcess<C> process,
      final C processExecutionContext) throws Exception {
    execute(new ProcessList<>(process), processExecutionContext);
  }

  /*
   * All methods of this process management have to call this one.
   *
   * ProcessManagement#execute(ProcessList
   * , ProcessExecutionContext)
   */
  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public <C extends ProcessExecutionContext> void execute(final ProcessList<C> processes,
      final C processExecutionContext) throws Exception {
    if (!processes.isNotEmpty()) {
      return;
    }

    InternalContext<C> context = null;
    LinkedList<InternalContext<?>> existingInternalParentContexts = parentFileTransactions.get();
    LinkedList<InternalContext<?>> internalParentContexts = existingInternalParentContexts == null ?
        new LinkedList<>():existingInternalParentContexts;
    try {
      context = initInternalContext(processExecutionContext, internalParentContexts);

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
          processSuccessfulExecution(context);
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

  private <C extends ProcessExecutionContext> void processSuccessfulExecution(final InternalContext<C> context)
      throws Exception {
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

  @Nonnull
  private <C extends ProcessExecutionContext> InternalContext<C> initInternalContext(
      final C processExecutionContext, LinkedList<InternalContext<?>> internalParentContexts) {
    // Context
    InternalContext<C> context;

    // Required or New file transaction ?
    if (!internalParentContexts.isEmpty() && !processExecutionContext.requiresNewFileTransaction()) {
      // Attaching internal context to parents
      context = new InternalContext<>(Objects.requireNonNull(internalParentContexts.peekLast())
          .getLast(), processExecutionContext);
    } else {
      // Prepare the context saving : new transaction case
      if (internalParentContexts.isEmpty()) {
        parentFileTransactions.set(internalParentContexts);
      }
      // Initializing the new context
      context = new InternalContext<>(processExecutionContext);
      processExecutionContext.setFileHandler(new FileHandler(context.getSession()));
      // Saving the context
      internalParentContexts.add(context);
    }
    return context;
  }

  /**
   * Files processing
   * @param process the process working on files.
   * @param context the execution context of the process
   * @throws Exception if an error occurs while processing the files.
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
   * @param context the context of execution of the process.
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
   * @param process the process to execute.
   * @param context the context of execution of the process.
   */
  private void onSuccessful(final SilverpeasProcess<?> process, final InternalContext<?> context) {
    try {
      process.onSuccessful();
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("The process has thrown an exception while processing the successful status " +
              "(sessionId = " + context.getSession().getId() + ")", e);
    }
  }

  private void handleError(final InternalContext<?> context, ProcessErrorType errorType,
      Exception exception, SilverpeasProcess<?> fromProcess) throws Exception {
    if (!context.isFileTransactionInError()) {
      context.setFileTransactionInError(errorType, exception, fromProcess);
    }
    if (!context.isOpeningFileTransaction()) {
      throw exception;
    }
    try {

      errorType = context.getErrorType();
      exception = context.getException();
      fromProcess = context.getProcessInError();

      // Error
      SilverLogger.getLogger(this)
          .error("The process has failed. Stack info: sessionId = " + context.getSession().getId() +
              ", ioErrorType = " + errorType.name() + ", exceptionClassName = " +
              exception.getClass().getName(), exception);

      // Treatment
      Exception continueException = null;
      InternalContext<? extends ProcessExecutionContext> curContext = context.getLast();
      while (curContext != null) {
        for (final SilverpeasProcess<?> processStarted : curContext.getOnFailureProcesses()) {
          continueException =
              processFailure(errorType, exception, fromProcess, continueException, processStarted);
        }
        curContext = curContext.getPrevious();
      }

      if (continueException != null) {
        throw continueException;
      }

    } finally {
      rollback(context);
    }
  }

  private Exception processFailure(final ProcessErrorType errorType, final Exception exception,
      final SilverpeasProcess<?> fromProcess, Exception continueException,
      final SilverpeasProcess<?> processStarted) {
    try {
      processStarted.onFailure((ProcessErrorType.DURING_CHECKS_PROCESSING.equals(errorType) ||
              processStarted == fromProcess) ? errorType :
              ProcessErrorType.OTHER_PROCESS_FAILED, exception);
    } catch (final Exception e) {
      if (continueException == null) {
        continueException = e;
      }
    }
    return continueException;
  }

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

 private void rollback(final InternalContext<?> context) {
    if (context.isOpeningFileTransaction()) {
      synchronized (ProcessMonitoring.SYNCHRONIZE) {
        clearSession(context);
      }
    }
  }

  private void clearSession(final InternalContext<?> context) {
    if (context.isOpeningFileTransaction()) {
      ((FileHandler) context.getProcessExecutionContext().getFileHandler())
          .deleteSessionWorkingPath();
    }
  }

  /**
   * @author Yohann Chastagnier
   */
  private static final class FileHandler extends org.silverpeas.core.process.io.file.FileHandler {
    private FileHandler(final ProcessSession session) {
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

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.silverpeas.core.process.SilverpeasProcess;
import org.silverpeas.core.process.check.ProcessCheckType;
import org.silverpeas.core.process.session.DefaultProcessSession;
import org.silverpeas.core.process.session.ProcessSession;

/**
 * Internal context of ProcessManagement.
 * @author Yohann Chastagnier
 * @see ProcessManagement
 */
class InternalContext<C extends ProcessExecutionContext> {

  private final InternalContext<? extends ProcessExecutionContext> previous;
  private InternalContext<? extends ProcessExecutionContext> next;

  private final C processExecutionContext;
  private ProcessSession session;
  private final List<SilverpeasProcess<C>> startedProcesses = new ArrayList<>();
  private final Set<ProcessCheckType> checkTypesToProcess = new HashSet<>();

  private boolean isFileTransactionInError = false;
  private SilverpeasProcess<?> processInError = null;
  private ProcessErrorType errorType = null;
  private Exception exception = null;

  /**
   * Default unique constructor
   * @param processExecutionContext
   */
  protected InternalContext(final C processExecutionContext) {
    this.processExecutionContext = processExecutionContext;
    session = DefaultProcessSession.create();
    previous = null;
  }

  /**
   * Default unique constructor
   * @param previous
   * @param processExecutionContext
   */
  protected InternalContext(final InternalContext<? extends ProcessExecutionContext> previous,
      final C processExecutionContext) {
    this.processExecutionContext = processExecutionContext;
    this.previous = previous;
    this.previous.next = this;

    // Get reference on existing shared data
    this.session = previous.session;
    processExecutionContext.setFileHandler(previous.getProcessExecutionContext().getFileHandler());
  }

  /**
   * @return the processExecutionContext
   */
  protected C getProcessExecutionContext() {
    return processExecutionContext;
  }

  /**
   * @return the session
   */
  protected ProcessSession getSession() {
    return session;
  }

  /**
   * @param session the session to set
   */
  public void setSession(final ProcessSession session) {
    this.session = session;
  }

  /**
   * @param errorType
   * @param exception
   * @param processInError
   */
  protected void setFileTransactionInError(final ProcessErrorType errorType,
      final Exception exception, final SilverpeasProcess<?> processInError) {
    InternalContext<?> context = this;
    while (context != null) {
      context.isFileTransactionInError = true;
      context.processInError = processInError;
      context.errorType = errorType;
      context.exception = exception;
      context = context.getPrevious();
    }
  }

  /**
   * @return the isFileTransactionInError
   */
  protected boolean isFileTransactionInError() {
    return isFileTransactionInError;
  }

  /**
   * @return the processInError
   */
  protected SilverpeasProcess<?> getProcessInError() {
    return processInError;
  }

  /**
   * @return the errorType
   */
  protected ProcessErrorType getErrorType() {
    return errorType;
  }

  /**
   * @return the exception
   */
  protected Exception getException() {
    return exception;
  }

  /**
   * Updating context from the given process
   * @param process
   */
  protected void update(final SilverpeasProcess<C> process) {
    startedProcesses.add(process);
    getFirst().checkTypesToProcess.addAll(process.getProcessType().getCheckTypesToProcess());
  }

  /**
   * @return the checkTypesToProcess
   */
  protected Set<ProcessCheckType> getCheckTypesToProcess() {
    return checkTypesToProcess;
  }

  /**
   * @return the startedProcesses
   */
  protected List<SilverpeasProcess<C>> getOnSuccessfulProcesses() {
    return startedProcesses;
  }

  /**
   * @return the startedProcesses
   */
  protected List<SilverpeasProcess<C>> getOnFailureProcesses() {
    Collections.reverse(startedProcesses);
    return startedProcesses;
  }

  /**
   * @return the isOpeningFileTransaction
   */
  protected boolean isOpeningFileTransaction() {
    return (previous == null);
  }

  /**
   * @return the previous
   */
  protected InternalContext<? extends ProcessExecutionContext> getPrevious() {
    return previous;
  }

  /**
   * @return the next
   */
  protected InternalContext<? extends ProcessExecutionContext> getNext() {
    return next;
  }

  /**
   * @return the next
   */
  protected InternalContext<? extends ProcessExecutionContext> getFirst() {
    InternalContext<?> parent = previous;
    while (parent != null) {
      if (parent.getPrevious() != null) {
        parent = parent.getPrevious();
      } else {
        return parent;
      }
    }
    return this;
  }

  /**
   * @return the next
   */
  protected InternalContext<? extends ProcessExecutionContext> getLast() {
    InternalContext<?> child = next;
    while (child != null) {
      if (child.getNext() != null) {
        child = child.getNext();
      } else {
        return child;
      }
    }
    return this;
  }
}

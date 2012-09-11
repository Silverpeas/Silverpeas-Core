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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.silverpeas.process.SilverpeasProcess;
import org.silverpeas.process.check.CheckType;
import org.silverpeas.process.session.Session;
import org.silverpeas.process.session.SimpleSession;

/**
 * Internal context of ProcessManagement.
 * @author Yohann Chastagnier
 * @see ProcessManagement
 */
class InternalContext<C extends ProcessExecutionContext> {

  private final boolean isOpeningFileTransaction;
  private final C processExecutionContext;
  private Session session;
  private boolean isProcessInError = false;
  private final List<SilverpeasProcess<C>> processesStarted = new ArrayList<SilverpeasProcess<C>>();
  private final Set<CheckType> checkTypesToProcess = new HashSet<CheckType>();

  /**
   * Default unique constructor
   * @param processExecutionContext
   */
  protected InternalContext(final C processExecutionContext) {
    this.processExecutionContext = processExecutionContext;
    session = SimpleSession.create();
    this.isOpeningFileTransaction = true;
  }

  /**
   * Default unique constructor
   * @param processExecutionContext
   */
  protected InternalContext(final C processExecutionContext, final Session session) {
    this.processExecutionContext = processExecutionContext;
    this.session = session;
    this.isOpeningFileTransaction = false;
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
  protected Session getSession() {
    return session;
  }

  /**
   * @param session the session to set
   */
  public void setSession(final Session session) {
    this.session = session;
  }

  /**
   * @return the isProcessInError
   */
  protected void setProcessInError() {
    isProcessInError = true;
  }

  /**
   * @return the isProcessInError
   */
  protected boolean isProcessInError() {
    return isProcessInError;
  }

  /**
   * Updating context from the given process
   * @param process
   */
  protected void update(final SilverpeasProcess<C> process) {
    processesStarted.add(0, process);
    checkTypesToProcess.addAll(process.getProcessType().getCheckTypesToProcess());
  }

  /**
   * @return the checkTypesToProcess
   */
  protected Set<CheckType> getCheckTypesToProcess() {
    return checkTypesToProcess;
  }

  /**
   * @return the processesStarted
   */
  protected List<SilverpeasProcess<C>> getProcessesStarted() {
    return processesStarted;
  }

  /**
   * @return the isOpeningFileTransaction
   */
  protected boolean isOpeningFileTransaction() {
    return isOpeningFileTransaction;
  }
}

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
package org.silverpeas.process;

import org.silverpeas.process.management.ProcessErrorType;
import org.silverpeas.process.management.ProcessExecutionContext;
import org.silverpeas.process.session.Session;

/**
 * Interface which has to be implemented by each process (or task in other words) that has to be
 * taken in charge by the Silverpeas Process API.
 * @author Yohann Chastagnier
 */
public interface SilverpeasProcess<C extends ProcessExecutionContext> {

  /**
   * Gets the session
   * @return
   */
  ProcessType getProcessType();

  /**
   * Containing main treatment of the process.
   * @return
   */
  void process(C context, Session session) throws Exception;

  /**
   * Containing treatments which have to be done after a successful execution of process method and
   * after successful validations.
   */
  public void onSuccessful(C context, Session session) throws Exception;

  /**
   * This method is called when exception is generated during a execution of Silverpeas Processes
   * chain and if the process method of the task have been executed.
   * @param context
   * @param session
   * @param errorType
   * @param exception
   * @throws Exception
   */
  public void onFailure(C context, Session session, ProcessErrorType errorType, Exception exception)
      throws Exception;
}

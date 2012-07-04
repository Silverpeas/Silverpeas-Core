/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.workflow.api.error;

import java.util.*;

import com.silverpeas.workflow.api.instance.*;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.*;
import com.silverpeas.workflow.api.*;

public interface WorkflowError {
  /**
   * @return ProcessInstance
   */
  public ProcessInstance getProcessInstance() throws WorkflowException;

  /**
   * @return history step
   */
  public HistoryStep getHistoryStep() throws WorkflowException;

  /**
   * @return error message
   */
  public String getErrorMessage();

  /**
   * @return stack trace
   */
  public String getStackTrace();

  /**
   * @return user
   */
  public User getUser() throws WorkflowException;

  /**
   * @return action
   */
  public Action getAction() throws WorkflowException;

  /**
   * @return action date
   */
  public Date getActionDate();

  /**
   * @return user role
   */
  public String getUserRole();

  /**
   * @return resolved state
   */
  public State getResolvedState() throws WorkflowException;
}
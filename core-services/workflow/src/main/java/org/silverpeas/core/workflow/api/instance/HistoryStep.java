/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.workflow.api.instance;

import java.util.*;

import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.User;

public interface HistoryStep extends Comparable<HistoryStep> {
  /**
   * @return ProcessInstance
   */
  public ProcessInstance getProcessInstance();

  /**
   * @return the actor
   */
  public User getUser() throws WorkflowException;

  /**
   * Get the step id
   * @return the step id
   */
  public String getId();

  /**
   * Get the role under which the user did the action
   * @return the role's name
   */
  public String getUserRoleName();

  /**
   * @return the action name
   */
  public String getAction();

  /**
   * @return the action date
   */
  public Date getActionDate();

  /**
   * @return the resolved state name
   */
  public String getResolvedState();

  /**
   * @return the resulting state name
   */
  public String getResultingState();

  /**
   * @return int
   */
  public int getActionStatus();

  /**
   * Get the data filled at this step
   */
  public DataRecord getActionRecord() throws WorkflowException;

  /**
   * Set the data filled at this step
   */
  public void setActionRecord(DataRecord data) throws WorkflowException;

  /**
   * Delete the data filled at this step
   */
  public void deleteActionRecord() throws WorkflowException;
}

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

import java.util.Date;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.User;

public interface UpdatableProcessInstance extends ProcessInstance {
  /**
   * Set the workflow instance id
   * @param instanceId instance id
   */
  public void setInstanceId(String instanceId);

  /**
   * Set the workflow model id
   * @param instanceId model id
   */
  public void setModelId(String modelId);

  /**
   * @param step
   */
  public void addHistoryStep(HistoryStep step) throws WorkflowException;

  /**
   * @param step
   */
  public void updateHistoryStep(HistoryStep step) throws WorkflowException;

  /**
   * Cancel all the atomic operations since the step where first action had occured
   * @param state the name of state where ac action has been discussed
   * @param actionDate date of state re-resolving
   */
  public void reDoState(String state, Date actionDate) throws WorkflowException;

  /**
   * @param itemName
   * @param value
   */
  public void setField(String name, Field value) throws WorkflowException;

  /**
   * Save a new version of given form (including values)
   * @param step the new step
   * @param formData Form's values as a DataRecord object
   */
  public void saveActionRecord(HistoryStep step, DataRecord formData)
      throws WorkflowException;

  /**
   * @param state
   */
  public void addActiveState(State state) throws WorkflowException;

  /**
   * @param state
   */
  public void removeActiveState(State state) throws WorkflowException;

  /**
   * @param state
   */
  public void addTimeout(State state) throws WorkflowException;

  /**
   * @param state
   */
  public void removeTimeout(State state) throws WorkflowException;

  /**
   * @param user
   */
  public void addWorkingUser(User user, State state, String role)
      throws WorkflowException;

  public void addWorkingUser(Actor actor, State state) throws WorkflowException;

  /**
   * @param user
   */
  public void removeWorkingUser(User user, State state, String role)
      throws WorkflowException;

  /**
   * Remove all working users for given state
   * @param state state for which the user is interested
   */
  public void removeWorkingUsers(State state)
      throws WorkflowException;

  /**
   * Add an user in the interested user list
   * @param user user to add
   * @param state state for which the user is interested
   * @param role role name under which the user is interested
   */
  public void addInterestedUser(User user, State state, String role)
      throws WorkflowException;

  public void addInterestedUser(Actor actor, State state) throws WorkflowException;

  /**
   * Remove an user from the interested user list
   * @param user user to remove
   * @param state state for which the user is interested
   * @param role role name under which the user is interested
   */
  public void removeInterestedUser(User user, State state, String role)
      throws WorkflowException;

  /**
   * Remove all interested users for given state
   * @param state state for which the user is interested
   */
  void removeInterestedUsers(State state) throws WorkflowException;

  /**
   * Lock this instance by admin for all states
   */
  public void lock() throws WorkflowException;

  /**
   * Lock this instance by admin for all states
   */
  public void unLock() throws WorkflowException;

  /**
   * Set the error status of this instance
   * @param errorStatus true if this instance is in error
   */
  public void setErrorStatus(boolean errorStatus);

  /**
     */
  public void computeValid();

  /**
   * Cancel a question without response
   * @param question the question to cancel
   */
  public void cancelQuestion(Question question) throws WorkflowException;

  /**
   * Set the timeout status of this instance
   * @param timeoutStatus true if this instance is in an active state for a long long time
   */
  public void setTimeoutStatus(boolean timeoutStatus);
}

/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.instance;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;

import java.util.Date;

/**
 * A process instance that can be updated.
 */
public interface UpdatableProcessInstance extends ProcessInstance {
  /**
   * Set the workflow instance id
   * @param instanceId instance id
   */
  void setInstanceId(String instanceId);

  /**
   * Set the workflow model id
   * @param modelId model id
   */
  void setModelId(String modelId);

  /**
   * Add a step in the history of actions performed in this process instance.
   * @param step a step in the workflow process history.
   */
  void addHistoryStep(HistoryStep step) throws WorkflowException;

  /**
   * Update the related history step by the given one.
   * @param step a step in the workflow process history.
   */
  void updateHistoryStep(HistoryStep step);

  /**
   * Cancel all the atomic operations since the step where first action had occurred
   * @param state the name of state where ac action has been discussed
   * @param actionDate date of state re-resolving
   */
  void reDoState(String state, Date actionDate) throws WorkflowException;

  /**
   * Sets the specified field in the process instance.
   * @param name the name of the field
   * @param field the field to set
   */
  void setField(String name, Field field) throws WorkflowException;

  /**
   * Save a new version of given form (including values)
   * @param step the new step
   * @param formData Form's values as a DataRecord object
   */
  void saveActionRecord(HistoryStep step, DataRecord formData)
      throws WorkflowException;

  /**
   * Adds the specified state as an active one in this process instance. An active state is the
   * current state of the process instance; that is to say a state that is currently waiting for
   * actions by working users. A process instance can have several active states in parallel.
   * @param state the active state.
   */
  void addActiveState(State state) throws WorkflowException;

  /**
   * Removes the specified state as an active one. An active state is the current state of the
   * process instance; that is to say a state that is currently waiting for actions by working
   * users. A process instance can have several active states in parallel.
   * @param state the active state.
   */
  void removeActiveState(State state) throws WorkflowException;

  /**
   * Adds a timeout to the specified active state. A timeout is a way to avoid a long time state.
   * The time out is defined by a {@link org.silverpeas.core.workflow.api.model.TimeOutAction}
   * provided by the specified state.
   * @param state the state for which a time out has to be added.
   */
  void addTimeout(State state) throws WorkflowException;

  /**
   * Removes the timeout from the specified active state. A timeout is a way to avoid a long time
   * state.
   * @param state the state for which a time out has to be removed.
   */
  @SuppressWarnings("unused")
  void removeTimeout(State state) throws WorkflowException;

  /**
   * Adds the specified working user to the given state with the specified role.
   * @param user the working user to add
   * @param state the state on which the user will work
   * @param role the role the user will play in the above state
   * @throws WorkflowException if the adding fails.
   */
  void addWorkingUser(User user, State state, String role) throws WorkflowException;

  /**
   * Adds the specified actor to the given state.
   * @param actor the actor to add
   * @param state the state for which the actor has to be defined
   * @throws WorkflowException if the adding fails.
   */
  void addWorkingUser(Actor actor, State state) throws WorkflowException;

  /**
   * Removes the specified user from the working users of the specified state and with the given
   * role.
   * @param user the user to remove.
   * @param state the state from which the user has to be removed
   * @param role the role the user plays in the above state
   * @throws WorkflowException if the removing fails.
   */
  void removeWorkingUser(User user, State state, String role) throws WorkflowException;

  /**
   * Remove all working users for given state
   * @param state state for which the user is interested
   */
  void removeWorkingUsers(State state) throws WorkflowException;

  /**
   * Add an user in the interested user list
   * @param user user to add
   * @param state state for which the user is interested
   * @param role role name under which the user is interested
   */
  @SuppressWarnings("unused")
  void addInterestedUser(User user, State state, String role) throws WorkflowException;

  void addInterestedUser(Actor actor, State state) throws WorkflowException;

  /**
   * Remove an user from the interested user list
   * @param user user to remove
   * @param state state for which the user is interested
   * @param role role name under which the user is interested
   */
  @SuppressWarnings("unused")
  void removeInterestedUser(User user, State state, String role) throws WorkflowException;

  /**
   * Remove all interested users for given state
   * @param state state for which the user is interested
   */
  void removeInterestedUsers(State state) throws WorkflowException;

  /**
   * Lock this instance by admin for all states
   */
  void lock() throws WorkflowException;

  /**
   * Lock this instance by admin for all states
   */
  void unLock() throws WorkflowException;

  /**
   * Set the error status of this instance
   * @param errorStatus true if this instance is in error
   */
  void setErrorStatus(boolean errorStatus);


  @SuppressWarnings("unused")
  void computeValid();

  /**
   * Cancel a question without response
   * @param question the question to cancel
   */
  void cancelQuestion(Question question) throws WorkflowException;

  /**
   * Set the timeout status of this instance
   * @param timeoutStatus true if this instance is in an active state for a long long time
   */
  void setTimeoutStatus(boolean timeoutStatus);
}

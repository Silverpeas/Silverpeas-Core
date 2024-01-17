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
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.QualifiedUsers;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.instance.ActionAndState;
import org.silverpeas.core.workflow.engine.instance.LockingUser;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * A process instance spawned from a given process model. The process model defines the workflow.
 * Each process instance is made up of states and actions through which the process instance changes
 * of states. The current(s) state(s) at which is a process instance are the active states. A
 * process instance can have several active parallel states.
 */
public interface ProcessInstance extends Serializable {

  /**
   * Gets the model of the process from which it was spawned.
   * @return the {@link ProcessModel} of this process instance.
   */
  ProcessModel getProcessModel() throws WorkflowException;

  /**
   * Get the workflow instance id
   * @return instance id
   */
  String getInstanceId();

  /**
   * Get the workflow model id
   * @return model id
   */
  String getModelId();

  /**
   * Gets the different steps in the history of this process instance. A history step defines the
   * change of a state by an action performed by a given working user.
   * @return an array of {@link HistoryStep} objects.
   */
  HistoryStep[] getHistorySteps();

  /**
   * Gets the step with the specified identifier in the history of this process instance.
   * @param stepId the unique identifier of a history step.
   * @return the asked history step.
   * @throws WorkflowException if no such history step can be found.
   */
  HistoryStep getHistoryStep(String stepId) throws WorkflowException;

  /**
   * Gets the data that were provided to the specified action when it was was performed. The most
   * recent data is returned in the case the action has been done several times.
   * @param actionName action name the name of the performed action.
   * @return the data record.
   */
  @SuppressWarnings("unused")
  DataRecord getActionRecord(String actionName) throws WorkflowException;

  /**
   * Gets a new data record associated with the given action.
   * @param actionName action name
   * @param language the ISO-631 code of the language in which the data will be.
   * @return the new empty data record.
   */
  DataRecord getNewActionRecord(String actionName, String language)
      throws WorkflowException;

  /**
   * Gets all the participants in this process instance.
   * @return the list of participants in this process instance.
   * @throws WorkflowException if an error occurs.
   */
  List<Participant> getParticipants() throws WorkflowException;

  /**
   * Get the last user who resolved the given state
   * @param resolvedState the resolved state
   * @return the user as a {@link Participant} object
   * @throws WorkflowException if an error occurs.
   */
  Participant getParticipant(String resolvedState) throws WorkflowException;

  /**
   * Gets the folder of this process instance.
   * @return the folder as a {@link DataRecord} object.
   * @throws WorkflowException if an error occurs.
   */
  DataRecord getFolder() throws WorkflowException;

  /**
   * Updates the folder of this process instance with the specified one.
   * @param data the data record with which the folder has to be updated.
   * @throws WorkflowException if the update fails.
   */
  void updateFolder(DataRecord data) throws WorkflowException;

  /**
   * @param role a role a user can play in this process instance.
   * @param lang the ISO-631 code of a language in which data have to be.
   * @return a record with all the data.
   * @throws WorkflowException if an error occurs.
   */
  DataRecord getAllDataRecord(String role, String lang) throws WorkflowException;

  /**
   * Gets a data record which will be used to render this process instance as a row in a list of
   * process instances. The information set in the record are filtered by what is allowed to be
   * rendered for the given role.
   * @param role a role a user can play in this process instance.
   * @param lang the ISO-631 code of the language in which data have to be.
   * @throws WorkflowException if an error occurs.
   */
  DataRecord getRowDataRecord(String role, String lang) throws WorkflowException;

  /**
   * Gets the data record of the specified form for the given role and written in the specified
   * language. The information set in the record are filtered by what is allowed to be
   * accessed for the given role.
   * @param formName the name of the form to get.
   * @param role a role a user can play in this process instance.
   * @param lang the ISO-631 code of the language in which data have to be.
   * @return DataRecord or null if no such form can be found.
   * @throws WorkflowException if an error occurs.
   */
  DataRecord getFormRecord(String formName, String role, String lang)
      throws WorkflowException;

  /**
   * Gets the specified field.
   * @param fieldName the name that identifies a field in this process instance.
   * @return the corresponding field.
   * @throws WorkflowException if the field cannot be found or if an error occurs.
   */
  Field getField(String fieldName) throws WorkflowException;

  /**
   * Gets all the active states of this process instance. An active state is the current state at
   * which the process is and waiting for actions.
   * @return an array with the name of all the active states.
   */
  String[] getActiveStates();

  /**
   * Gets all the users working on this process instance.
   * @return an array of {@link Actor} objects.
   * @throws WorkflowException if an error occurs.
   */
  Actor[] getWorkingUsers() throws WorkflowException;

  /**
   * Gets all the users working on the specified state of this instance state.
   * @param state the name of a state in this process instance.
   * @return an array of {@link Actor} objects.
   */
  Actor[] getWorkingUsers(String state) throws WorkflowException;

  /**
   * Gets all the users working on the specified state of this instance state with the given role.
   * @param state the name of a state in this process instance.
   * @return an array of {@link Actor} objects.
   */
  Actor[] getWorkingUsers(String state, String role) throws WorkflowException;

  /**
   * Gets all the states that are assigned to the specified user with given role.
   * @param user a user in the workflow.
   * @param roleName a role the user has to play in this process instance.
   * @return an array with the name of all the states assigned to the given user with the given
   * role name.
   */
  String[] getAssignedStates(User user, String roleName);

  /**
   * Gets all the states that are assigned to the specified user.
   * @param user a user in the workflow.
   * @return an array with the name of all the states assigned to the given user
   */
  String[] getAllAssignedStates(final User user);

  /**
   * Gets the user that has locked the specified state.
   * @param state the name of a state in this process instance.
   * @return the user as a {@link LockingUser} instance.
   */
  LockingUser getLockingUser(String state);

  /**
   * Is this process instance valid?
   * @return true is this instance is valid. False otherwise.
   */
  boolean isValid();

  /**
   * Is this process instance has been locked by the supervisor?
   * @return true is this instance is locked by admin, false otherwise.
   */
  boolean isLockedByAdmin();

  /**
   * Is this process instance in error by an action?
   * @return true if this instance is in error, false otherwise.
   */
  boolean getErrorStatus();

  /**
   * Gets the timeout status of this instance
   * @return true if this instance is in an active state for a long long time. False otherwise.
   */
  boolean getTimeoutStatus();

  /**
   * Locks this instance for the given instance and state
   * @param state state that have to be locked
   * @param user the locking user
   */
  void lock(State state, User user) throws WorkflowException;

  /**
   * Un-locks this instance for the given instance and state
   * @param state state that have to be un-locked
   * @param user the unlocking user
   */
  void unLock(State state, User user) throws WorkflowException;

  /**
   * Gets concrete users affected to given role at runtime
   * @param role the name of the role
   * @return users affected to given role
   * @throws WorkflowException if an error occurs.
   */
  List<User> getUsersInRole(String role) throws WorkflowException;

  /**
   * Gets concrete users affected to given group at runtime
   * @param groupId the id of the group
   * @return users in given group
   * @throws WorkflowException if an error occurs
   */
  List<User> getUsersInGroup(String groupId) throws WorkflowException;

  /**
   * Computes tuples role/user (stored in an Actor object) from a QualifiedUsers object
   * @param qualifiedUsers Users defined by their role or by a relation with a participant
   * @param state state for which these user were/may be actors
   * @return tuples role/user as an array of Actor objects
   * @throws WorkflowException if an error occurs
   */
  Actor[] getActors(QualifiedUsers qualifiedUsers, State state)
      throws WorkflowException;

  /**
   * Test is a active state is in back status
   * @param stateName name of active state
   * @return true if resolution of active state involves a cancel of actions
   */
  boolean isStateInBackStatus(String stateName);

  /**
   * Get step saved by given user id.
   * @return the history step or null if there is no such step.
   * @throws WorkflowException if an error occurs.
   */
  HistoryStep getSavedStep(String userId) throws WorkflowException;

  /**
   * Recent the most recent step where the named action has been performed.
   */
  HistoryStep getMostRecentStep(String actionName);

  /**
   * Get all the steps where given user (with given role) can go back from the given state
   * @param user user that can do the back actions
   * @param roleName role name of this user
   * @param stateName name of state where user want to go back from
   * @return an array of HistoryStep objects
   */
  HistoryStep[] getBackSteps(User user, String roleName, String stateName)
      throws WorkflowException;

  /**
   * Add a question
   * @param content question text
   * @param stepId id of destination step for the question
   * @param fromState the state where the question was asked
   * @param fromUser the user who asked the question
   * @return The state to which the question is
   */
  State addQuestion(String content, String stepId, State fromState,
      User fromUser) throws WorkflowException;

  /**
   * Answer a question
   * @param content response text
   * @param questionId id of question corresponding to this response
   * @return The state where the question was asked
   */
  State answerQuestion(String content, String questionId)
      throws WorkflowException;

  /**
   * Get all the questions asked in the given state
   * @param stateName given state name
   * @return all the questions (not yet answered) asked in the given state
   */
  Question[] getPendingQuestions(String stateName)
      throws WorkflowException;

  /**
   * Get all the questions asked from the given state
   * @param stateName given state name
   * @return all the questions (not yet answered) asked from the given state
   */
  Question[] getSentQuestions(String stateName);

  /**
   * Get all the questions asked from the given state and that have been answered
   * @param stateName given state name
   * @return all the answered questions asked from the given state
   */
  Question[] getRelevantQuestions(String stateName);

  /**
   * Get all the questions asked in this processInstance
   * @return all the questions
   */
  Question[] getQuestions();

  /**
   * Returns this instance title.
   */
  String getTitle(String role, String lang);

  /**
   * Gets the timeout action to be launched after the given datetime.
   * @return the asked {@link ActionAndState} or null if no such action is found.
   * @throws WorkflowException if an error occurs
   */
  ActionAndState getTimeOutAction(Date dateRef) throws WorkflowException;
}
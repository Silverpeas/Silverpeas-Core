/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

import java.util.Date;
import java.util.List;

public interface ProcessInstance {

  /**
   * @return ProcessModel
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
   * @return HistoryStep[]
   */
  HistoryStep[] getHistorySteps();

  /**
   * @return HistoryStep
   */
  HistoryStep getHistoryStep(String stepId) throws WorkflowException;

  /**
   * Returns the data which was given when the action was performed. Returns the most recent data
   * when the action has been done several times.
   * @param actionName action name
   */
  DataRecord getActionRecord(String actionName) throws WorkflowException;

  /**
   * Get a new data record associated to the given action
   * @param actionName action name
   */
  DataRecord getNewActionRecord(String actionName, String language)
      throws WorkflowException;

  /**
   * @return Vector
   */
  List<Participant> getParticipants() throws WorkflowException;

  /**
   * Get the last user who resolved the given state
   * @param resolvedState the resolved state
   * @return this user as a Participant object
   */
  Participant getParticipant(String resolvedState) throws WorkflowException;

  /**
   * @return DataRecord
   */
  DataRecord getFolder() throws WorkflowException;

  void updateFolder(DataRecord data) throws WorkflowException;

  /**
   * @return DataRecord
   */
  DataRecord getAllDataRecord(String role, String lang) throws WorkflowException;

  /**
   * Returns a DataRecord which will be used to represent this process instance as a row in a list.
   */
  DataRecord getRowDataRecord(String role, String lang) throws WorkflowException;

  /**
   * @return DataRecord
   */
  DataRecord getFormRecord(String formName, String role, String lang)
      throws WorkflowException;

  /**
   * @param fieldName the fieldName that identifies the Field instance to get
   * @return Field
   */
  Field getField(String fieldName) throws WorkflowException;

  /**
   * @return String[]
   */
  String[] getActiveStates();

  /**
   * Returns all the working users on this instance.
   * @return Actor[]
   */
  Actor[] getWorkingUsers() throws WorkflowException;

  /**
   * Returns all the working users on this instance state.
   * @param state
   * @return User[]
   */
  Actor[] getWorkingUsers(String state) throws WorkflowException;

  /**
   * Returns all the working users on this instance state.
   * @param state
   * @return User[]
   */
  Actor[] getWorkingUsers(String state, String role) throws WorkflowException;

  /**
   * Returns all the state name assigned to the user with given role
   * @param user
   * @param roleName
   * @return String[]
   */
  String[] getAssignedStates(User user, String roleName) throws WorkflowException;

  /**
   * @param state
   * @return User
   */
  LockingUser getLockingUser(String state) throws WorkflowException;

  /**
   * Get the validity state of this instance
   * @return true is this instance is valid
   */
  boolean isValid();

  /**
   * Get the lock Admin status of this instance
   * @return true is this instance is locked by admin
   */
  boolean isLockedByAdmin();

  /**
   * Get the error status of this instance
   * @return true if this instance is in error
   */
  boolean getErrorStatus();

  /**
   * Get the timeout status of this instance
   * @return true if this instance is in an active state for a long long time
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
   * @throws WorkflowException
   */
  List<User> getUsersInRole(String role) throws WorkflowException;

  /**
   * Gets concrete users affected to given group at runtime
   * @param groupId the id of the group
   * @return users in given group
   * @throws WorkflowException
   */
  List<User> getUsersInGroup(String groupId) throws WorkflowException;

  /**
   * Computes tuples role/user (stored in an Actor object) from a QualifiedUsers object
   * @param qualifiedUsers Users defined by their role or by a relation with a participant
   * @param state state for which these user were/may be actors
   * @return tuples role/user as an array of Actor objects
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
   * @throws WorkflowException
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
   * Get all the questions asked from the given state and that have been aswered
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
   * Returns the timeout action to be launched after given date
   * @throws WorkflowException
   */
  ActionAndState getTimeOutAction(Date dateRef) throws WorkflowException;
}
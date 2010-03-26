/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.workflow.engine.instance;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.Actor;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.engine.AbstractReferrableObject;
import com.silverpeas.workflow.engine.WorkflowHub;

/**
 * @table SB_Workflow_WorkingUser
 * @depends com.silverpeas.workflow.engine.instance.ProcessInstanceImpl
 * @key-generator MAX
 */
public class WorkingUser extends AbstractReferrableObject {
  /**
   * Used for persistence
   * @primary-key
   * @field-name id
   * @field-type string
   * @sql-type integer
   */
  private String id = null;

  /**
   * @field-name processInstance
   * @field-type com.silverpeas.workflow.engine.instance.ProcessInstanceImpl
   * @sql-name instanceId
   */
  private ProcessInstanceImpl processInstance = null;

  /**
   * @field-name userId
   */
  private String userId = null;

  /**
   * @field-name state
   */
  private String state = null;

  /**
   * @field-name role
   */
  private String role = null;

  /**
   * Default Constructor
   */
  public WorkingUser() {
  }

  /**
   * For persistence in database Get this object id
   * @return this object id
   */
  public String getId() {
    return id;
  }

  /**
   * For persistence in database Set this object id
   * @param this object id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get state name for which user is affected
   * @return state name
   */
  public String getState() {
    return state;
  }

  /**
   * Set state name for which user is affected
   * @param state state name
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * Get state role for which user is affected
   * @return state role
   */
  public String getRole() {
    return role;
  }

  /**
   * Set state role for which user is affected
   * @param state state role
   */
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Get the user id
   * @return user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set the user id
   * @param userId user id
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Get the instance for which user is affected
   * @return instance
   */
  public ProcessInstanceImpl getProcessInstance() {
    return processInstance;
  }

  /**
   * Set the instance for which user is affected
   * @param processInstance instance
   */
  public void setProcessInstance(ProcessInstanceImpl processInstance) {
    this.processInstance = processInstance;
  }

  /**
   * Converts WorkingUser to User
   * @return an object implementing User interface and containing user details
   */
  public User toUser() throws WorkflowException {
    return WorkflowHub.getUserManager().getUser(this.getUserId());
  }

  /**
   * Converts WorkingUser to Actor
   * @return an object implementing Actor interface
   */
  public Actor toActor() throws WorkflowException {
    State state = processInstance.getProcessModel().getState(this.state);
    User user = WorkflowHub.getUserManager().getUser(this.getUserId());

    return new ActorImpl(user, role, state);
  }

  /**
   * Get User information from an array of workingUsers
   * @param workingUsers an array of WorkingUser objects
   * @return an array of objects implementing User interface and containing user details
   */
  static public User[] toUser(WorkingUser[] workingUsers)
      throws WorkflowException {
    String[] userIds = new String[workingUsers.length];

    for (int i = 0; i < workingUsers.length; i++) {
      userIds[i] = workingUsers[i].getUserId();
    }

    return WorkflowHub.getUserManager().getUsers(userIds);
  }

  /**
   * This method has to be implemented by the referrable object it has to compute the unique key
   * @return The unique key.
   */
  public String getKey() {
    return (this.getUserId() + "--" + this.getState() + "--" + this.getRole());
  }
}
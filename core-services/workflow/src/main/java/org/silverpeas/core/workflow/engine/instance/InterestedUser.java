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

package org.silverpeas.core.workflow.engine.instance;

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;
import org.silverpeas.core.workflow.engine.WorkflowHub;

/**
 * @table SB_Workflow_InterestedUser
 * @depends ProcessInstanceImpl
 * @key-generator MAX
 */
public class InterestedUser extends AbstractReferrableObject {
  /**
   * Used for persistence
   * @primary-key
   * @field-name id
   * @field-type string
   * @sql-type integer
   */
  private String id = null;

  /**
   * @field-name userId
   */
  private String userId = null;

  /**
   * @field-name usersRole
   */
  private String usersRole = null;

  /**
   * @field-name groupId
   */
  private String groupId = null;

  /**
   * @field-name processInstance
   * @field-type ProcessInstanceImpl
   * @sql-name instanceId
   */
  private ProcessInstanceImpl processInstance = null;

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
  public InterestedUser() {
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
   * Get role name under which user can access to this instance
   * @return role name
   */
  public String getRole() {
    return role;
  }

  /**
   * Set role name for which user is affected
   * @param role role name
   */
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Get state name for which user can access to this instance
   * @return state name
   */
  public String getState() {
    return state;
  }

  /**
   * Set state name for which user can access to this instance
   * @param state state name
   */
  public void setState(String state) {
    this.state = state;
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
   * Get the user role
   * @return user role name
   */
  public String getUsersRole() {
    return usersRole;
  }

  /**
   * Set the user role
   * @param usersRole role
   */
  public void setUsersRole(String usersRole) {
    this.usersRole = usersRole;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Get the instance to which user is interested
   * @return instance
   */
  public ProcessInstanceImpl getProcessInstance() {
    return processInstance;
  }

  /**
   * Set the instance to which user is interested
   * @param processInstance instance
   */
  public void setProcessInstance(ProcessInstanceImpl processInstance) {
    this.processInstance = processInstance;
  }

  /**
   * Converts InterestedUser to User
   * @return an object implementing User interface and containing user details
   */
  public User toUser() throws WorkflowException {
    return WorkflowHub.getUserManager().getUser(this.getUserId());
  }

  /**
   * Get User information from an array of workingUsers
   * @param workingUsers an array of WorkingUser objects
   * @return an array of objects implementing User interface and containing user details
   */
  static public User[] toUser(InterestedUser[] interestedUsers)
      throws WorkflowException {
    String[] userIds = new String[interestedUsers.length];

    for (int i = 0; i < interestedUsers.length; i++) {
      userIds[i] = interestedUsers[i].getUserId();
    }

    return WorkflowHub.getUserManager().getUsers(userIds);
  }

  /**
   * This method has to be implemented by the referrable object it has to compute the unique key
   * @return The unique key.
   */
  public String getKey() {
    return (this.getUserId() + "--" + this.getState() + "--" + this.getRole() + "--" + this
        .getUsersRole());
  }
}
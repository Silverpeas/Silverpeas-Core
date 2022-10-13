/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.instance;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.WorkflowHub;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "sb_workflow_workinguser")
public class WorkingUser extends BasicJpaEntity<WorkingUser, UniqueIntegerIdentifier> {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "instanceid", nullable = false)
  private ProcessInstanceImpl processInstance = null;

  @Column
  private String userId = null;

  @Column
  private String usersRole = null;

  @Column
  private String state = null;

  @Column
  private String role = null;

  @Column
  private String groupId = null;

  /**
   * Get state name for which user is affected
   * @return state name
   */
  public String getState() {
    return (state == null) ? "" : state;
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
   * Get state role for which user is affected
   * @return state role
   */
  public List<String> getRoles() {
    return Arrays.asList(role.split(":"));
  }

  /**
   * Set state role for which user is affected
   * @param role state role
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
   * Converts WorkingUser to Actors
   * @return an object implementing Actor interface
   */
  public Collection<Actor> toActors() throws WorkflowException {
    State actualState = processInstance.getProcessModel().getState(this.state);
    Collection<Actor> actors = new ArrayList<>();

    // first add user by id
    if (this.getUserId() != null) {
      User user = WorkflowHub.getUserManager().getUser(this.getUserId());
      actors.add(new ActorImpl(user, role, actualState));
    }

    // then add users by group or role
    if (StringUtil.isDefined(getGroupId())) {
      User[] users =
          WorkflowHub.getUserManager().getUsersInGroup(getGroupId());
      for (User anUser : users) {
        actors.add(new ActorImpl(anUser, role, actualState));
      }
    } else {
      // then add users by role
      if (this.usersRole != null) {
        User[] users =
            WorkflowHub.getUserManager().getUsersInRole(usersRole, processInstance.getModelId());
        for (User anUser : users) {
          actors.add(new ActorImpl(anUser, role, actualState));
        }
      }
    }

    return actors;
  }

  /**
   * Get User information from an array of workingUsers
   * @param workingUsers an array of WorkingUser objects
   * @return an array of objects implementing User interface and containing user details
   */
  public static User[] toUser(WorkingUser[] workingUsers) throws WorkflowException {
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
  private String getKey() {
    return getUserId() + "--" + getState() + "--" + getRole() + "--" + getUsersRole();
  }

  @Override
  public boolean equals(Object theOther) {
    if (theOther instanceof String) {
      return getKey().equals(theOther);
    } else if (theOther instanceof WorkingUser) {
      return getKey().equals(((WorkingUser) theOther).getKey());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }

}
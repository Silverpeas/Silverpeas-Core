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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.instance;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "sb_workflow_interesteduser")
public class InterestedUser extends BasicJpaEntity<InterestedUser, UniqueIntegerIdentifier> {

  @Column
  private String userId = null;

  @Column
  private String usersRole = null;

  @Column
  private String groupId = null;

  @ManyToOne
  @JoinColumn(name = "instanceid", nullable = false)
  private ProcessInstanceImpl processInstance = null;

  @Column
  private String state = null;

  @Column
  private String role = null;

  /**
   * Default Constructor
   */
  protected InterestedUser() {
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

  public String getKey() {
    return getUserId() + "--" + getState() + "--" + getRole() + "--" + getUsersRole();
  }

  @Override
  public boolean equals(Object theOther) {
    if (theOther instanceof String) {
      return getKey().equals(theOther);
    } else if (theOther instanceof InterestedUser){
      return getKey().equals(((InterestedUser) theOther).getKey());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }
}
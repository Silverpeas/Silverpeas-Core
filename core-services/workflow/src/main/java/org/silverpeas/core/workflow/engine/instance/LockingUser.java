/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
import java.util.Date;

@Entity
@Table(name = "sb_workflow_lockinguser")
public class LockingUser extends BasicJpaEntity<LockingUser, UniqueIntegerIdentifier> {

  @Column
  private String userId = null;

  @ManyToOne
  @JoinColumn(name = "instanceid", nullable = false)
  private ProcessInstanceImpl processInstance = null;

  @Column
  private String state = null;

  @Column
  private Date lockDate = null;

  /**
   * Default Constructor
   */
  public LockingUser() {
    lockDate = new Date();
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
   * Get the date when user locked the instance
   * @return lock date
   */
  public Date getLockDate() {
    return lockDate;
  }

  /**
   * This method has to be implemented by the referrable object it has to compute the unique key
   * @return The unique key.
   */
  private String getKey() {
    if (this.getState() == null) {
      return "";
    }
    return this.getState();
  }

  @Override
  public boolean equals(Object theOther) {
    if (theOther instanceof String) {
      return getKey().equals(theOther);
    } else if (theOther instanceof LockingUser) {
      return getKey().equals(((LockingUser) theOther).getKey());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }
}
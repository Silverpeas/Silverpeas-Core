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

import java.util.Date;

import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * @table SB_Workflow_ActiveState
 * @depends ProcessInstanceImpl
 * @key-generator MAX
 */
public class ActiveState extends AbstractReferrableObject {
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
   * @field-type ProcessInstanceImpl
   * @sql-name instanceId
   */
  private ProcessInstanceImpl processInstance = null;

  /**
   * @field-name state
   */
  private String state = null;

  /**
   * @field-name backStatus
   */
  private boolean backStatus = false;

  /**
   * Flag that indicates if this active state is there for a long long time (As several timeout can
   * be defined in chain, timeoutstatus numeric value N represent the Nth timeout
   * @field-name timeoutStatus
   */
  private int timeoutStatus = 0;

  /**
   * Date at which current state will be in timeout
   * @field-name timeoutDate
   */
  private Date timeoutDate = null;

  /**
   * Default Constructor
   */
  public ActiveState() {
  }

  /**
   * Constructor
   * @param state state name
   */
  public ActiveState(String state) {
    this.state = state;
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
   * Get state name
   * @return state name
   */
  public String getState() {
    return state;
  }

  /**
   * Set state name
   * @param state state name
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * Get the status regarding a possible undo process
   * @return true if state is active to be discussed
   */
  public boolean getBackStatus() {
    return backStatus;
  }

  public int getBackStatusCastor() {
    if (getBackStatus())
      return 1;
    else
      return 0;
  }

  /**
   * Set the status regarding a possible undo process
   * @param backStatus true if state is active to be discussed
   */
  public void setBackStatus(boolean backStatus) {
    this.backStatus = backStatus;
  }

  public void setBackStatusCastor(int backStatus) {
    this.backStatus = (backStatus == 1);
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
   * Get the timeout status of this active state
   * @return true if this an active state is there for a long long time
   */
  public int getTimeoutStatus() {
    return timeoutStatus;
  }

  /**
   * Set the date at which current state will be in timeout
   * @param timeoutDate the timeout Date to set
   */
  public void setTimeoutDate(Date timeoutDate) {
    this.timeoutDate = timeoutDate;
  }

  /**
   * Get the date at which current state will be in timeout
   * @return the timeout Date
   */
  public Date getTimeoutDate() {
    return timeoutDate;
  }

  /**
   * Set the timeout status of this active state
   * @param timeoutStatus true if this active state is there for a long long time
   */
  public void setTimeoutStatus(int timeoutStatus) {
    this.timeoutStatus = timeoutStatus;
  }

  /**
   * This method has to be implemented by the referrable object it has to compute the unique key
   * @return The unique key.
   */
  public String getKey() {
    return this.getState();
  }

}
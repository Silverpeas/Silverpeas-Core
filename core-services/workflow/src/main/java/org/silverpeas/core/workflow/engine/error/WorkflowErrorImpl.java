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

package org.silverpeas.core.workflow.engine.error;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.silverpeas.core.workflow.api.Workflow;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.error.WorkflowError;
import org.silverpeas.core.workflow.api.user.User;

/**
 * @table SB_Workflow_Error
 * @key-generator MAX
 */
public class WorkflowErrorImpl implements WorkflowError {
  /**
   * Blank constructor for Castor
   */
  public WorkflowErrorImpl() {
  }

  /**
   * A WorkflowErrorImpl is build from a process instance, a generic event, a history step and a
   * Exception
   */
  public WorkflowErrorImpl(ProcessInstance instance, GenericEvent event,
      HistoryStep step, Exception exception) {
    this.processInstance = instance;
    this.instanceId = instance.getInstanceId();
    this.errorMessage = exception.getMessage();
    this.actionName = event.getActionName();
    this.actionDate = event.getActionDate();
    this.userRole = event.getUserRoleName();

    this.step = step;
    if (step != null) {
      this.stepId = step.getId();
    }
    this.user = event.getUser();
    if (user != null) {
      this.userId = event.getUser().getUserId();
    }
    this.state = event.getResolvedState();
    if (state != null) {
      this.stateName = event.getResolvedState().getName();
    }
    // Convert stack trace to String
    ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(baoStream);
    exception.printStackTrace(printStream);
    this.stackTrace = baoStream.toString().trim();
  }

  /**
   * @return ProcessInstance
   */
  public ProcessInstance getProcessInstance() throws WorkflowException {
    if (processInstance == null) {
      if (instanceId != null) {
        processInstance = Workflow.getProcessInstanceManager()
            .getProcessInstance(instanceId);
      }
    }

    return processInstance;
  }

  /**
   * Get error Id
   */
  public String getId() {
    return id;
  }

  /**
   * Set error Id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get instance Id
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Set instance Id
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * @return history step
   */
  public HistoryStep getHistoryStep() throws WorkflowException {
    if (step == null) {
      if (stepId != null) {
        if (getProcessInstance() != null)
          step = getProcessInstance().getHistoryStep(stepId);
      }
    }

    return step;
  }

  /**
   * Get history step Id
   */
  public String getStepId() {
    return stepId;
  }

  /**
   * Set history step Id
   */
  public void setStepId(String stepId) {
    this.stepId = stepId;
  }

  /**
   * @return error message
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * @return error message
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * @return stack trace
   */
  public String getStackTrace() {
    return stackTrace;
  }

  /**
   * @return stack trace
   */
  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }

  /**
   * @return user
   */
  public User getUser() throws WorkflowException {
    if (user == null) {
      if (userId != null) {
        user = Workflow.getUserManager().getUser(userId);
      }
    }

    return user;
  }

  /**
   * Get user Id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set user Id
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @return action
   */
  public Action getAction() throws WorkflowException {
    if (action == null && actionName != null) {
      if (getProcessInstance() != null) {
        action = getProcessInstance().getProcessModel().getAction(actionName);
      }
    }

    return action;
  }

  /**
   * Get action name
   */
  public String getActionName() {
    return actionName;
  }

  /**
   * Set action name
   */
  public void setActionName(String actionName) {
    this.actionName = actionName;
  }

  /**
   * Get action date
   */
  public Date getActionDate() {
    return actionDate;
  }

  /**
   * Set action date
   */
  public void setActionDate(Date actionDate) {
    this.actionDate = actionDate;
  }

  /**
   * Get user role
   */
  public String getUserRole() {
    return userRole;
  }

  /**
   * Set user role
   */
  public void setUserRole(String userRole) {
    this.userRole = userRole;
  }

  /**
   * @return resolved state
   */
  public State getResolvedState() throws WorkflowException {
    if (state == null && stateName != null) {
      if (getProcessInstance() != null) {
        state = getProcessInstance().getProcessModel().getState(stateName);
      }
    }

    return state;
  }

  /**
   * Get state name
   */
  public String getStateName() {
    return stateName;
  }

  /**
   * Set state name
   */
  public void setStateName(String stateName) {
    this.stateName = stateName;
  }

  /**
   * @field-name id
   * @field-type string
   * @sql-type integer
   * @primary-key
   */
  private String id = null;

  /**
   * @field-name instanceId
   * @field-type string
   * @sql-type integer
   */
  private String instanceId = null;
  private transient ProcessInstance processInstance = null;

  /**
   * @field-name stepId
   * @field-type string
   * @sql-type integer
   */
  private String stepId = null;
  private transient HistoryStep step = null;

  /**
   * @field-name errorMessage
   */
  private String errorMessage = null;

  /**
   * @field-name stackTrace
   */
  private String stackTrace = null;

  /**
   * @field-name userId
   */
  private String userId = null;
  private transient User user = null;

  /**
   * @field-name actionName
   */
  private String actionName = null;
  private transient Action action = null;

  /**
   * @field-name actionDate
   * @sql-type timestamp
   */
  private Date actionDate = null;

  /**
   * @field-name userRole
   */
  private String userRole = null;

  /**
   * @field-name stateName
   */
  private String stateName = null;
  private transient State state = null;
}
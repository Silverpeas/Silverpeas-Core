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
import java.util.GregorianCalendar;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.Form;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.WorkflowHub;

/**
 * @table SB_Workflow_HistoryStep
 * @depends ProcessInstanceImpl
 * @key-generator MAX
 */
public class HistoryStepImpl implements UpdatableHistoryStep, Comparable<HistoryStep> {
  /**
   * Used for persistence
   * @primary-key
   * @field-name id
   * @field-type string
   * @sql-type integer
   */
  private String id = null;

  /**
   * Process instance whose an action has been logged by this history step
   * @field-name processInstance
   * @field-type ProcessInstanceImpl
   * @sql-name instanceId
   */
  private ProcessInstanceImpl processInstance = null;

  /**
   * Id of user whose action has been logged in this history step.
   * @field-name userId
   */
  private String userId = null;

  /**
   * Role under which user did the action logged in this history step.
   * @field-name userRoleName
   */
  private String userRoleName = null;

  /**
   * Name of action that has been logged by this history step
   * @field-name action
   */
  private String action = null;

  /**
   * Date of action that has been logged by this history step
   * @field-name actionDate
   * @sql-type timestamp
   */
  private Date actionDate = null;

  /**
   * Name of the state that has been resolved by action that has been logged by this history step
   * @field-name resolvedState
   */
  private String resolvedState = null;

  /**
   * Name of the state that must be resulted from action that has been logged by this history step
   * @field-name resultingState
   * @sql-name toState
   */
  private String resultingState = null;

  /**
   * Resulting status of action
   * @field-name actionStatus
   */
  private int actionStatus = 0;

  /**
   * Default constructor
   */
  public HistoryStepImpl() {
    setTodayAsActionDate();
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
   * Get the process instance
   * @return process instance
   */
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }

  /**
   * Set the process instance
   * @param instance process instance
   */
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = (ProcessInstanceImpl) processInstance;
  }

  /**
   * Get the actor of the action logged in this History step
   * @return the actor
   */
  public User getUser() throws WorkflowException {
    return WorkflowHub.getUserManager().getUser(this.getUserId());
  }

  /**
   * Get the actor id of the action logged in this History step
   * @return the actor id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set the actor id of the action logged in this History step
   * @param userId the actor id
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Get the role under which the user did the action
   * @return the role's name
   */
  public String getUserRoleName() {
    return userRoleName;
  }

  /**
   * Set the role under which the user did the action
   * @param userRoleName the role's name
   */
  public void setUserRoleName(String userRoleName) {
    this.userRoleName = userRoleName;
  }

  /**
   * Get the action name logged in this History step
   * @return the action name
   */
  public String getAction() {
    return action;
  }

  /**
   * Set the action name logged in this History step
   * @param action the action name
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * Get the date when the action has been done
   * @return the action date
   */
  public Date getActionDate() {
    return actionDate;
  }

  /**
   * Set the date when the action has been done
   * @param actionDate the action date
   */
  public void setActionDate(Date actionDate) {
    this.actionDate = actionDate;
  }

  /**
   * Set the today as date when the action has been done
   */
  public void setTodayAsActionDate() {
    GregorianCalendar calendar = new GregorianCalendar();
    this.actionDate = calendar.getTime();
  }

  /**
   * Get the name of state that has been resolved
   * @return the resolved state name
   */
  public String getResolvedState() {
    return resolvedState;
  }

  /**
   * Set the name of state that has been resolved
   * @param state the resolved state name
   */
  public void setResolvedState(String state) {
    this.resolvedState = state;
  }

  /**
   * Get the name of state that has been resolved
   * @return the resolved state name
   */
  public String getResultingState() {
    return resultingState;
  }

  /**
   * Set the name of state that must result from logged action
   * @param state state name
   */
  public void setResultingState(String state) {
    this.resultingState = state;
  }

  /**
   * Get the resulting status of action logged in this history step
   * @return action status
   * <ul>
   * <li>-1 : Process failed
   * <li>0 : To Be Processed
   * <li>1 : Processed
   * <li>2 : Affectations Done
   * </ul>
   */
  public int getActionStatus() {
    return actionStatus;
  }

  /**
   * Set the resulting status of action logged in this history step
   * <ul>
   * <li>-1 : Process failed
   * <li>0 : To Be Processed
   * <li>1 : Processed
   * <li>2 : Affectations Done
   * <li>3 : Saved (to be continued)
   * </ul>
   * @param actionStatus action status
   */
  public void setActionStatus(int actionStatus) {
    this.actionStatus = actionStatus;
  }

  /**
   * Get the data associated to this step. Returns null if there is no form associated to the action
   */
  public DataRecord getActionRecord() throws WorkflowException {
    ProcessModel model = processInstance.getProcessModel();

    Action actionObj = model.getAction(action);
    if (actionObj == null) {
      return null;
    }
    Form form = actionObj.getForm();
    if (form == null) {
      return null;
    }
    String formId = id;
    try {
      RecordSet formSet = model.getFormRecordSet(form.getName());
      return formSet.getRecord(formId);
    } catch (FormException e) {
      throw new WorkflowException("HistoryStepImpl",
          "workflowEngine.EXP_UNKNOWN_FORM", "form=" + form.getName() + "("
          + formId + ")", e);
    }
  }

  /**
   * Set the data associated to this step.
   */
  public void setActionRecord(DataRecord data) throws WorkflowException {
    ProcessModel model = processInstance.getProcessModel();

    Action actionObj = model.getAction(action);
    if (actionObj == null) {
      return;
    }
    Form form = actionObj.getForm();
    if (form == null) {
      return;
    }
    String formId = id;
    try {
      RecordSet formSet = model.getFormRecordSet(form.getName());
      // In case of draft step, have to delete previous record if it exists
      if (this.actionStatus == 3) {
        DataRecord previousRecord = formSet.getRecord(formId);
        if (previousRecord != null) {
          formSet.delete(previousRecord);
        }
      }
      data.setId(formId);
      formSet.save(data);
    } catch (FormException e) {
      throw new WorkflowException("ProcessInstanceImpl",
          "workflowEngine.EXP_UNKNOWN_FORM", "form=" + form.getName() + "("
          + formId + ")", e);
    }
  }

  /**
   * Delete the data associated to this step.
   */
  public void deleteActionRecord() throws WorkflowException {
    ProcessModel model = processInstance.getProcessModel();

    Action actionObj = model.getAction(action);
    if (actionObj == null) {
      return;
    }
    Form form = actionObj.getForm();
    if (form == null) {
      return;
    }
    String formId = id;
    try {
      RecordSet formSet = model.getFormRecordSet(form.getName());
      formSet.delete(getActionRecord());
    } catch (FormException e) {
      throw new WorkflowException("ProcessInstanceImpl",
          "workflowEngine.EXP_UNKNOWN_FORM", "form=" + form.getName() + "("
          + formId + ")", e);
    }
  }

  public int compareTo(HistoryStep arg0) {
    if (arg0 == null) {
      return 0;
    }
    HistoryStep anotherStep = arg0;
    int stepId = Integer.parseInt(this.id);
    int anotherStepId = Integer.parseInt(anotherStep.getId());
    return stepId - anotherStepId;
  }
}

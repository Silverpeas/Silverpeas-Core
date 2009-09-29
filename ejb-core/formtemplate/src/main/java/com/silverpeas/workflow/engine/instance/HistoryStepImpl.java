package com.silverpeas.workflow.engine.instance;

import java.util.Date;
import java.util.GregorianCalendar;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.UpdatableHistoryStep;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.Form;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.engine.WorkflowHub;

/**
 * @table SB_Workflow_HistoryStep
 * @depends com.silverpeas.workflow.engine.instance.ProcessInstanceImpl
 * @key-generator MAX
 */
public class HistoryStepImpl implements UpdatableHistoryStep, Comparable {
  /**
   * Used for persistence
   * 
   * @primary-key
   * @field-name id
   * @field-type string
   * @sql-type integer
   */
  private String id = null;

  /**
   * Process instance whose an action has been logged by this history step
   * 
   * @field-name processInstance
   * @field-type com.silverpeas.workflow.engine.instance.ProcessInstanceImpl
   * @sql-name instanceId
   */
  private ProcessInstanceImpl processInstance = null;

  /**
   * Id of user whose action has been logged in this history step.
   * 
   * @field-name userId
   */
  private String userId = null;

  /**
   * Role under which user did the action logged in this history step.
   * 
   * @field-name userRoleName
   */
  private String userRoleName = null;

  /**
   * Name of action that has been logged by this history step
   * 
   * @field-name action
   */
  private String action = null;

  /**
   * Date of action that has been logged by this history step
   * 
   * @field-name actionDate
   * @sql-type timestamp
   */
  private Date actionDate = null;

  /**
   * Name of the state that has been resolved by action that has been logged by
   * this history step
   * 
   * @field-name resolvedState
   */
  private String resolvedState = null;

  /**
   * Name of the state that must be resulted from action that has been logged by
   * this history step
   * 
   * @field-name resultingState
   * @sql-name toState
   */
  private String resultingState = null;

  /**
   * Resulting status of action
   * 
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
   * 
   * @return this object id
   */
  public String getId() {
    return id;
  }

  /**
   * For persistence in database Set this object id
   * 
   * @param this object id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the process instance
   * 
   * @return process instance
   */
  public ProcessInstance getProcessInstance() {
    return (ProcessInstance) processInstance;
  }

  /**
   * Set the process instance
   * 
   * @param instance
   *          process instance
   */
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = (ProcessInstanceImpl) processInstance;
  }

  /**
   * Get the actor of the action logged in this History step
   * 
   * @return the actor
   */
  public User getUser() throws WorkflowException {
    return WorkflowHub.getUserManager().getUser(this.getUserId());
  }

  /**
   * Get the actor id of the action logged in this History step
   * 
   * @return the actor id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set the actor id of the action logged in this History step
   * 
   * @param userId
   *          the actor id
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Get the role under which the user did the action
   * 
   * @return the role's name
   */
  public String getUserRoleName() {
    return userRoleName;
  }

  /**
   * Set the role under which the user did the action
   * 
   * @param userRoleName
   *          the role's name
   */
  public void setUserRoleName(String userRoleName) {
    this.userRoleName = userRoleName;
  }

  /**
   * Get the action name logged in this History step
   * 
   * @return the action name
   */
  public String getAction() {
    return action;
  }

  /**
   * Set the action name logged in this History step
   * 
   * @param action
   *          the action name
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * Get the date when the action has been done
   * 
   * @return the action date
   */
  public Date getActionDate() {
    return actionDate;
  }

  /**
   * Set the date when the action has been done
   * 
   * @param actionDate
   *          the action date
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
   * 
   * @return the resolved state name
   */
  public String getResolvedState() {
    return resolvedState;
  }

  /**
   * Set the name of state that has been resolved
   * 
   * @param state
   *          the resolved state name
   */
  public void setResolvedState(String state) {
    this.resolvedState = state;
  }

  /**
   * Get the name of state that has been resolved
   * 
   * @return the resolved state name
   */
  public String getResultingState() {
    return resultingState;
  }

  /**
   * Set the name of state that must result from logged action
   * 
   * @param state
   *          state name
   */
  public void setResultingState(String state) {
    this.resultingState = state;
  }

  /**
   * Get the resulting status of action logged in this history step
   * 
   * @return action status
   *         <ul>
   *         <li>-1 : Process failed
   *         <li>0 : To Be Processed
   *         <li>1 : Processed
   *         <li>2 : Affectations Done
   *         </ul>
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
   * </ul>
   * 
   * @param actionStatus
   *          action status
   */
  public void setActionStatus(int actionStatus) {
    this.actionStatus = actionStatus;
  }

  /**
   * Get the data associated to this step. Returns null if there is no form
   * associated to the action
   */
  public DataRecord getActionRecord() throws WorkflowException {
    ProcessModel model = processInstance.getProcessModel();

    Action actionObj = model.getAction(action);
    if (actionObj == null)
      return null;

    Form form = actionObj.getForm();
    if (form == null)
      return null;

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
    if (actionObj == null)
      return;

    Form form = actionObj.getForm();
    if (form == null)
      return;

    String formId = id;
    try {
      RecordSet formSet = model.getFormRecordSet(form.getName());
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
    if (actionObj == null)
      return;

    Form form = actionObj.getForm();
    if (form == null)
      return;

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

  public int compareTo(Object arg0) {
    if (arg0 == null)
      return 0;
    HistoryStep anotherStep = (HistoryStep) arg0;
    int stepId = Integer.parseInt(this.id);
    int anotherStepId = Integer.parseInt(anotherStep.getId());
    return stepId - anotherStepId;
  }
}

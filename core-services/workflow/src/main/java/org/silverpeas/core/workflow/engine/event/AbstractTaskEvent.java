package org.silverpeas.core.workflow.engine.event;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;

import java.util.Date;

public class AbstractTaskEvent implements GenericEvent {

  private User user;
  private ProcessInstance processInstance;
  private ProcessModel processModel;
  private Date actionDate;
  private String actionName;
  private String userRoleName;
  private State resolvedState;
  private DataRecord data;
  private User substitute;

  public AbstractTaskEvent(Task resolvedTask, String actionName, DataRecord data) {
    this.user = resolvedTask.getUser();
    this.processModel = resolvedTask.getProcessModel();
    this.processInstance = resolvedTask.getProcessInstance();
    this.resolvedState = resolvedTask.getState();
    this.actionName = actionName;
    this.actionDate = new Date();
    this.userRoleName = resolvedTask.getUserRoleName();
    this.data = data;
  }

  /**
   * Returns the actor.
   */
  public User getUser() {
    return user;
  }

  /**
   * Returns the process instance. Returns null when the task is an instance creation.
   */
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }

  /**
   * Set the process instance (when created).
   */
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }

  /**
   * Returns the process model (peas). Must be not null when the task is an instance creation.
   */
  public ProcessModel getProcessModel() {
    return processModel;
  }

  /**
   * Returns the state/activity resolved by the user.
   */
  public State getResolvedState() {
    return resolvedState;
  }

  /**
   * Returns the name of the action chosen to resolve the activity.
   */
  public String getActionName() {
    return actionName;
  }

  /**
   * Returns the action date.
   */
  public Date getActionDate() {
    return actionDate;
  }

  /**
   * Returns the data filled when the action was processed.
   */
  public DataRecord getDataRecord() {
    return data;
  }

  /**
   * Returns the role name of the actor
   */
  public String getUserRoleName() {
    return userRoleName;
  }

  @Override
  public User getSubstitute() {
    return substitute;
  }

  @Override
  public void setSubstitute(User substitute) {
    this.substitute = substitute;
  }

  @Override
  public User getUserOrSubstitute() {
    return (getSubstitute() == null) ? getUser() : getSubstitute();
  }

}
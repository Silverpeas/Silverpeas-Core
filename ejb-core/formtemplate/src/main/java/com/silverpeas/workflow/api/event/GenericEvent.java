package com.silverpeas.workflow.api.event;

import java.util.Date;

import com.silverpeas.form.DataRecord;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.User;

/**
 * A GenericEvent object is the description of an action on an activity
 * 
 * Those descriptions are sent to the workflow engine by the workflow tools when
 * the user has done an action in a process instance.
 */
public interface GenericEvent {
  /**
   * Returns the actor.
   */
  public User getUser();

  /**
   * Returns the role name of the actor
   */
  public String getUserRoleName();

  /**
   * Returns the process instance.
   */
  public ProcessInstance getProcessInstance();

  /**
   * Returns the state/activity resolved by the user.
   */
  public State getResolvedState();

  /**
   * Returns the action date.
   */
  public Date getActionDate();

  /**
   * Returns the name of the action choosen to resolve the activity.
   */
  public String getActionName();

  /**
   * Returns the data associated to this event.
   */
  public DataRecord getDataRecord();
}

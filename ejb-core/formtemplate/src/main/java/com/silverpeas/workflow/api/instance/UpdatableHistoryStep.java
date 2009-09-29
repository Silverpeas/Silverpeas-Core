package com.silverpeas.workflow.api.instance;

import java.util.Date;

public interface UpdatableHistoryStep extends HistoryStep {
  /**
   * Set the process instance
   * 
   * @param instance
   *          process instance
   */
  public void setProcessInstance(ProcessInstance processInstance);

  /**
   * Set the actor id of the action logged in this History step
   * 
   * @param userId
   *          the actor id
   */
  public void setUserId(String userId);

  /**
   * Set the action name logged in this History step
   * 
   * @param action
   *          the action name
   */
  public void setAction(String action);

  /**
   * Set the date when the action has been done
   * 
   * @param actionDate
   *          the action date
   */
  public void setActionDate(Date actionDate);

  /**
   * Set the name of state that has been resolved
   * 
   * @param state
   *          the resolved state name
   */
  public void setResolvedState(String state);

  /**
   * Set the name of state that must result from logged action
   * 
   * @param state
   *          state name
   */
  public void setResultingState(String state);

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
  public void setActionStatus(int actionStatus);

  /**
   * Set the role under which the user did the action
   * 
   * @param userRoleName
   *          the role's name
   */
  public void setUserRoleName(String userRoleName);
}
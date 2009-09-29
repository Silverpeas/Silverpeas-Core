package com.silverpeas.workflow.api;

import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.User;

/**
 * The workflow engine services relate to process instance management.
 */
public interface UpdatableProcessInstanceManager extends ProcessInstanceManager {
  /**
   * Creates a new process instance
   * 
   * @param modelId
   *          model id
   * @return the new ProcessInstance object
   */
  public ProcessInstance createProcessInstance(String modelId)
      throws WorkflowException;

  /**
   * Removes a new process instance
   * 
   * @param instanceId
   *          instance id
   */
  public void removeProcessInstance(String instanceId) throws WorkflowException;

  /**
   * Locks the given instance for the given instance and state
   * 
   * @param instance
   *          instance that have to be locked
   * @param state
   *          state that have to be locked
   * @param user
   *          the locking user
   */
  public void lock(ProcessInstance instance, State state, User user)
      throws WorkflowException;

  /**
   * Locks the given instance for the given instance and state
   * 
   * @param instance
   *          instance that have to be locked
   * @param state
   *          state that have to be locked
   * @param user
   *          the locking user
   */
  public void unlock(ProcessInstance instance, State state, User user)
      throws WorkflowException;
}
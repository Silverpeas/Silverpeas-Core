package com.silverpeas.workflow.api;

import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.Actor;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.api.model.State;


/**
 * The workflow engine services relate to process instance management.
 */
public interface ProcessInstanceManager 
{
	/**
	 * Get the list of process instances for a given peas Id, user and role.
	 * @param	peasId	id of processManager instance
	 * @param	user	user for who the process instance list is
	 * @param	role	role name of the user for who the process instance list is (useful when user has different roles)
	 * @return	an array of ProcessInstance objects
	 */
	public ProcessInstance[] getProcessInstances(String peasId, User user, String role) throws WorkflowException;

	/**
	 * Get the list of process instances for a given peas Id, that have the given state activated
	 * @param	peasId	id of processManager instance
	 * @param	state	activated state
	 * @return	an array of ProcessInstance objects
	 */
	public ProcessInstance[] getProcessInstancesInState(String peasId, State state) throws WorkflowException;

	/**
	 * Get the process instances for a given instance id
	 * @param	instanceId	id of searched instance
	 * @return	the searched process instance
	 */
	public ProcessInstance getProcessInstance(String instanceId) throws WorkflowException;

	/**
	 * Build a new HistoryStep
	 * Return an object implementing HistoryStep interface
	 */
	public HistoryStep createHistoryStep();

	/**
	 * Builds an actor from a user and a role.
	 */
	public Actor createActor(User user, String roleName, State state);

}

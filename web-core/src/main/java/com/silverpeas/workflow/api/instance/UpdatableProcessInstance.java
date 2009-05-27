package com.silverpeas.workflow.api.instance;

import java.util.Date;

import com.silverpeas.workflow.api.*;
import com.silverpeas.workflow.api.user.*;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.form.*;

public interface UpdatableProcessInstance extends ProcessInstance 
{
	/**
	 * Set the workflow instance id
	 * @param	instanceId	instance id
	 */
	public void setInstanceId(String instanceId);
   
	/**
	 * Set the workflow model id
	 * @param	instanceId	model id
	 */
	public void setModelId(String modelId);

	/**
    * @param step
    */
	public void addHistoryStep(HistoryStep step) throws WorkflowException;
   
	/**
    * @param step
    */
	public void updateHistoryStep(HistoryStep step) throws WorkflowException;

	/**
	 * Cancel all the atomic operations since the step where first action had occured
	 * @param	state	the name of state where ac action has been discussed
	 * @param	actionDate	date of state re-resolving
	 */
	public void reDoState(String state, Date actionDate) throws WorkflowException;

	/**
	 * @param itemName
	 * @param value
	 */
	public void setField(String name, Field value) throws WorkflowException;

	/**
	 * Save a new version of given form (including values)
	 * @param	step	      the new step
	 * @param	formData	   Form's values as a DataRecord object
	 */
	public void saveActionRecord(HistoryStep step, DataRecord formData)
	   throws WorkflowException;

	/**
	 * @param state
	 */
	public void addActiveState(State state) throws WorkflowException;

	/**
	 * @param state
	 */
	public void removeActiveState(State state) throws WorkflowException;

	/**
	 * @param state
	 */
	public void addTimeout(State state) throws WorkflowException;

	/**
	 * @param state
	 */
	public void removeTimeout(State state) throws WorkflowException;
	
	/**
	 * @param user
	 */
	public void addWorkingUser(User user, State state, String role) throws WorkflowException;

	/**
	 * @param user
	 */
	public void removeWorkingUser(User user, State state, String role) throws WorkflowException;
   
	/**
	 * Add an user in the interested user list
	 * @param	user	user to add
	 * @param	state	state for which the user is interested
	 * @param	role	role name under which the user is interested
	 */
	public void addInterestedUser(User user, State state, String role) throws WorkflowException;

	/**
	 * Remove an user from the interested user list
	 * @param	user	user to remove
	 * @param	state	state for which the user is interested
	 * @param	role	role name under which the user is interested
	 */
	public void removeInterestedUser(User user, State state, String role) throws WorkflowException;

	/**
	 * Lock this instance by admin for all states
	 */
	public void lock() throws WorkflowException;

	/**
	 * Lock this instance by admin for all states
	 */
	public void unLock() throws WorkflowException;

   /**
    * Set the error status of this instance
    * @param errorStatus true if this instance is in error
    */
	public void setErrorStatus(boolean errorStatus);
	
	/**
     */
    public void computeValid();

	/**
	 * Cancel a question without response
	 * @param	question		the question to cancel
	 */
	public void cancelQuestion(Question question) throws WorkflowException;
}

package com.silverpeas.workflow.api.error;

import java.util.*;

import com.silverpeas.workflow.api.instance.*;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.*;
import com.silverpeas.workflow.api.*;

public interface WorkflowError 
{
	/**
	 * @return ProcessInstance
	 */
	public ProcessInstance getProcessInstance() throws WorkflowException;
   
	/**
	 * @return history step
	 */
	public HistoryStep getHistoryStep() throws WorkflowException;

	/**
	 * @return error message
	 */
	public String getErrorMessage();

	/**
	 * @return stack trace
	 */
	public String getStackTrace();

	/**
	 * @return user
	 */
	public User getUser() throws WorkflowException;

	/**
	 * @return action
	 */
	public Action getAction() throws WorkflowException;

	/**
	 * @return action date
	 */
	public Date getActionDate();

	/**
	 * @return user role 
	 */
	public String getUserRole();

	/**
	 * @return resolved state
	 */
	public State getResolvedState() throws WorkflowException;
}
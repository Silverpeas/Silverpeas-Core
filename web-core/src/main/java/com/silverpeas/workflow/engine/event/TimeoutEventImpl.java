package com.silverpeas.workflow.engine.event;

import java.util.Date;
import com.silverpeas.workflow.api.event.TimeoutEvent;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.form.DataRecord;

/**
 * A TimeoutEvent object is the description of an instance, 
 * that is in the same active state since too long
 *
 * Those descriptions are sent to the timeout manager
 */
public class TimeoutEventImpl implements TimeoutEvent
{
	/**
	 * A TimeoutEventImpl object is built from a processInstance, a state and an action
	 */
	public TimeoutEventImpl(ProcessInstance processInstance, State resolvedState, Action action)
	{
		this.processInstance = processInstance;
		this.resolvedState = resolvedState;
		this.action = action;
		this.actionDate = new Date();
	}

    /**
	 * Returns the actor.
	 */
    public User getUser()
	{
	   return null;
	}

	/**
	 * Returns the process instance.
	 *
	 * Returns null when the task is an instance creation.
	 */
    public ProcessInstance getProcessInstance()
	{
	   return processInstance;
	}

	/**
	 * Returns the state/activity resolved by the user.
	 */
	public State getResolvedState()
	{
	   return resolvedState;
	}

	/**
	 * Returns the name of the action chosen to resolve the activity.
	 */
	public String getActionName()
	{
	   return action.getName();
	}

	/**
	 * Returns the action date.
	 */
	public Date getActionDate()
	{
	   return actionDate;
	}

    /**
	 * Returns the data filled when the action was processed.
	 */
	public DataRecord getDataRecord()
	{
	   return null;
	}

	/**
	 * Returns the role name of the actor
	 */
	public String getUserRoleName()
	{
		return "supervisor";
	}

	/*
	 * Internal states.
	 */
    private ProcessInstance processInstance = null;
    private Action action = null;
    private State resolvedState = null;
	private Date actionDate = null;
}

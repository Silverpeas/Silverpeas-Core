package com.silverpeas.workflow.engine.task;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.Question;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.User;

/**
 * A task object is an activity description
 * built by the workflow engine
 * and sent via the taskManager to an external system 
 * which will notify the end user and manage the task realisation. 
 *
 * Task objects will be created by the workflow engine when a new task
 * is assigned to a user.
 * Task objects will be created too for the ProcessManager GUI which
 * will be used by the user to do the assigned activity.
 */
public class TaskImpl extends AbstractTaskImpl
{
    /**
     * Builds a TaskImpl.
     */
    public TaskImpl(User user,
                    String roleName,
                    ProcessInstance processInstance,
                    State state)
       throws WorkflowException
	{
		super(user, roleName, processInstance.getProcessModel());
		this.processInstance = processInstance;
		this.state = state;
		this.backSteps = null;
	}

	/**
     * Builds a TaskImpl.
     */
    public TaskImpl(User user,
                    String roleName,
                    ProcessInstance processInstance,
                    State state,
					HistoryStep[] backSteps,
					Question[] sentQuestions,
					Question[] relevantQuestions,
					Question[] pendingQuestions)
       throws WorkflowException
    {
		super(user, roleName, processInstance.getProcessModel());
		this.processInstance = processInstance;
		this.state = state;
		this.backSteps = backSteps;
		this.sentQuestions = sentQuestions;
		this.relevantQuestions = relevantQuestions;
		this.pendingQuestions = pendingQuestions;
    }
    
    /**
     * Returns the process instance.
     */
    public ProcessInstance getProcessInstance()
    {
		return processInstance;
    }

    /**
     * Returns the state to be resolved by the user.
     */
    public State getState()
    {
		return state;
    }

    /**
     * Returns the history steps that user can discussed (ask a question to the actor of that step).
     */
    public HistoryStep[] getBackSteps()
    {
		return backSteps;
    }

    /**
     * Returns the questions that must be answered
     */
    public Question[] getPendingQuestions()
    {
		return pendingQuestions;
    }

    /**
     * Returns the (non onsolete) questions that have been answered
     */
    public Question[] getRelevantQuestions()
    {
		return relevantQuestions;
    }

	/**
     * Returns the question that have been asked and are waiting for a response
     */
    public Question[] getSentQuestions()
    {
		return sentQuestions;
    }

	/**
     * Returns the action names list from which the user must choose
     * to resolve the activity.
     */
    public String[] getActionNames()
    {
       Action[] actions = state.getAllowedActions();
       String[] actionNames = new String[actions.length];

       for (int i=0 ; i< actions.length ; i++)
       {
          actionNames[i] = actions[i].getName();
       }

       return actionNames;
    }

    /*
     * Internal fields
     */
    private ProcessInstance processInstance = null;
    private State state = null;
	private HistoryStep[] backSteps = null;
	private Question[] pendingQuestions = null;
	private Question[] relevantQuestions = null;
	private Question[] sentQuestions = null;
}
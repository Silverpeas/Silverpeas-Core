package com.silverpeas.workflow.api.instance;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.User;

import java.util.Date;

/**
 * A Question object represents a question asked for the instance
 */
public interface Question
{
	/**
	 * Get the question id
	 */
	public String getId();

	/**
	 * Get the process instance where the question was asked
	 */
	public ProcessInstance getProcessInstance();

	/**
	 * Get the state where the question was asked
	 */
	public State getFromState();

	/**
	 * Get the destination state for the question
	 */
	public State getTargetState();

	/**
	 * Get the question content
	 */
	public String getQuestionText();

	/**
	 * Get the response content
	 */
	public String getResponseText();

	/**
	 * Answer this question
	 */
	public void answer(String responseText);

	/**
	 * Get the user who asked the question
	 */
	public User getFromUser() throws WorkflowException;

	/**
	 * Get the user who received the question
	 */
	public User getToUser() throws WorkflowException;

	/**
	 * Get the date when question was asked
	 */
	public Date getQuestionDate();

	/**
	 * Get the date when question was asked
	 */
	public Date getResponseDate();

	/**
	 * Is a response was sent to this question
	 */
	public boolean hasResponse();

	/**
	 * Has this question been answered and taken in account, if yes, so it's not relevant anymore (return false)
	 */
	public boolean isRelevant();

}
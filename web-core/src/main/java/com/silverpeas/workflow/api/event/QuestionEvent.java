package com.silverpeas.workflow.api.event;

import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.ProcessModel;

/**
 * A QuestionEvent object is the description of a question and 'back' action 
 *
 * Those descriptions are sent to the workflow engine
 * by the workflow tools when
 * the user has done a 'back' action in the instance
 */
public interface QuestionEvent extends GenericEvent
{
	/**
	 * Returns the process model (peas).
	 */
    public ProcessModel getProcessModel();

	/**
	 * Set the process instance.
	 */
    public void setProcessInstance(ProcessInstance instance);

	/**
	 * Returns the discussed step id
	 */
	public String getStepId();

}
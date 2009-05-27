package com.silverpeas.workflow.api.event;

import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.ProcessModel;

/**
 * A ResponseEvent object is the description of a response sent
 *
 * Those descriptions are sent to the workflow engine
 * by the workflow tools when
 * the user answer a question without re-playing the workflow
 */
public interface ResponseEvent extends GenericEvent
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
	 * Returns the id of question corresponding to this answer
	 */
	public String getQuestionId();
}
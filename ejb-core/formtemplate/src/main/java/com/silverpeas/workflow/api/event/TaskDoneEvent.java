package com.silverpeas.workflow.api.event;

import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.ProcessModel;

/**
 * A TaskDoneEvent object is the description of a done activity.
 *
 * Those descriptions are sent to the workflow engine
 * by the workflow tools when
 * the user has done a task in a process instance.
 */
public interface TaskDoneEvent extends GenericEvent
{
	/**
	 * Returns the process model (peas).
	 */
    public ProcessModel getProcessModel();

	/**
	 * Set the process instance.
	 */
    public void setProcessInstance(ProcessInstance instance);
}
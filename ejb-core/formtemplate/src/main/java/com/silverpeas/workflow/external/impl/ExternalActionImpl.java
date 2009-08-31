package com.silverpeas.workflow.external.impl;

import com.silverpeas.workflow.api.event.GenericEvent;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.Parameter;
import com.silverpeas.workflow.api.model.Trigger;
import com.silverpeas.workflow.external.ExternalAction;

public abstract class ExternalActionImpl implements ExternalAction {

	private ProcessInstance process;
	private GenericEvent	event;
	private Trigger			trigger;
	
	public void setProcessInstance(ProcessInstance process)
	{
		this.process = process;
	}
	
	public void setEvent(GenericEvent event)
	{
		this.event = event;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}
	
	public Parameter getTriggerParameter(String paramName)
	{
		return trigger.getParameter(paramName);
	}

	public ProcessInstance getProcessInstance()
	{
		return process;
	}
	
	public GenericEvent getEvent()
	{
		return event;
	}
	
	public abstract void execute();

}

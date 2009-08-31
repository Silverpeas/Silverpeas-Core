package com.silverpeas.workflow.external.impl;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class AddSilverTrace extends ExternalActionImpl {

	public AddSilverTrace() {}
	
	public void execute() 
	{
		String message = "instanceId = "+getProcessInstance().getInstanceId();
		if (getEvent() != null)
		{
			message += " event '"+getEvent().getActionName()+"'";
			if (getEvent().getUser() != null)
			{
				message +=" by "+getEvent().getUser().getFullName();
			}
		}
		SilverTrace.info("workflowEngine", "AddSilverTrace.execute", "root.MSG_GEN_PARAM_VALUE", message);
	}
}

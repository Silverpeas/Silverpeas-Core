package com.stratelia.webactiv.beans.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class SynchroGroupScheduler implements SchedulerEventHandler {
	
	public static final String ADMINSYNCHROGROUP_JOB_NAME = "AdminSynchroGroupJob";
	
	private List synchronizedGroupIds = null;
	private Admin admin = null;
	
	public void initialize(String cron, Admin admin, List synchronizedGroupIds)
	{
		try
		{
			this.admin = admin;
			this.synchronizedGroupIds = synchronizedGroupIds;
			
			Vector jobList = SimpleScheduler.getJobList(this);
			if (jobList != null && jobList.size() > 0)
				SimpleScheduler.removeJob(this, ADMINSYNCHROGROUP_JOB_NAME);
			SimpleScheduler.getJob(this, ADMINSYNCHROGROUP_JOB_NAME, cron, this, "doSynchroGroup");
		}
		catch (Exception e)
		{
			SilverTrace.error("admin", "SynchroGroupScheduler.initialize()", "importExport.EX_CANT_INIT_SCHEDULED_IMPORT", e);
		}
	}

	public void handleSchedulerEvent(SchedulerEvent aEvent) {
		switch (aEvent.getType())
        {
			case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
				SilverTrace.error("admin", "SynchroGroupScheduler.handleSchedulerEvent", "The job '" + aEvent.getJob().getJobName() + "' was not successfull");
	            break;

			case SchedulerEvent.EXECUTION_SUCCESSFULL:
	            SilverTrace.debug("admin", "SynchroGroupScheduler.handleSchedulerEvent", "The job '" + aEvent.getJob().getJobName() + "' was successfull");
	            break;

			default:
	            SilverTrace.error("admin", "SynchroGroupScheduler.handleSchedulerEvent", "Illegal event type");
	            break;
        }
	}
	
	public void doSynchroGroup(Date date)
	{
		SilverTrace.info("admin", "SynchroGroupScheduler.doSynchroGroup()", "root.MSG_GEN_ENTER_METHOD");
		
		SynchroGroupReport.startSynchro();
		
		String groupId = null;
		for (int i=0; synchronizedGroupIds != null && i<synchronizedGroupIds.size(); i++)
		{
			groupId = (String) synchronizedGroupIds.get(i);
			try {
				admin.synchronizeGroupByRule(groupId, true);
			} catch (AdminException e) {
				SilverTrace.error("admin", "SynchroGroupScheduler.doSynchroGroup", "admin.MSG_ERR_SYNCHRONIZE_GROUP", e);
			}
		}
		
		SynchroGroupReport.stopSynchro();
		
		SilverTrace.info("admin", "SynchroGroupScheduler.doScheduledImport()", "root.MSG_GEN_EXIT_METHOD");
	}
	
	public void addGroup(String groupId)
    {
		if (synchronizedGroupIds == null)
			synchronizedGroupIds = new ArrayList();
        synchronizedGroupIds.add(groupId);
    }

    public void removeGroup(String groupId)
    {
    	if (synchronizedGroupIds != null)
    		synchronizedGroupIds.remove(groupId);
    }
}
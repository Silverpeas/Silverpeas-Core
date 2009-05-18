package com.stratelia.webactiv.organization;

import java.util.Date;
import java.util.Vector;

import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;

public class ScheduledDBReset implements SchedulerEventHandler
{

    // Local constants
    private static final String   DBRESET_JOB_NAME = "ScheduledDBReset";
    private AdminController         m_ac = null;

	/**
	 * Initialize timeout manager
	 * 
	 */
	public void initialize(String cronString)
	{
		try
		{
			Vector jobList = SimpleScheduler.getJobList(this);

			if (jobList.size() != 0)
			{
				// Remove previous scheduled job
				SimpleScheduler.removeJob(this, DBRESET_JOB_NAME);
			}
            if ((cronString != null) && (cronString.length() > 0))
            {
                // Create new scheduled job
                SimpleScheduler.getJob(this, DBRESET_JOB_NAME, cronString, this, "doDBReset");
            }
		}
		catch (Exception e)
		{
			SilverTrace.error("admin", "ScheduledDBReset.initialize", "admin.EX_ERR_INITIALIZE", e);
		}
		
	}

    /**
     * Scheduler Event handler
     *
     */
    public void handleSchedulerEvent(SchedulerEvent aEvent)
    {
        switch (aEvent.getType())
        {
			case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
				SilverTrace.error("admin", "ScheduledDBReset.handleSchedulerEvent", "The job '" + aEvent.getJob().getJobName() + "' was not successfull");
	            break;
	
			case SchedulerEvent.EXECUTION_SUCCESSFULL:
	            SilverTrace.debug("admin", "ScheduledDBReset.handleSchedulerEvent", "The job '" + aEvent.getJob().getJobName() + "' was successfull");
	            break;

			default:
	            SilverTrace.error("admin", "ScheduledDBReset.handleSchedulerEvent", "Illegal event type");
	            break;
        }
    }

    /**
     * This method is called periodically by the scheduler,
     * it test for each peas of type processManager
     * if associated model contains states with timeout events
     * If so, all the instances of these peas that have the "timeout" states actives
	 * are read to check if timeout interval has been reached.
     * In that case, the administrator can be notified, the active state and the instance are marked as timeout 
     *
     * @param currentDate   the date when the method is called by the scheduler
     *
     * @see SimpleScheduler for parameters,
     */
	public void doDBReset(Date date)
	{
        if (m_ac == null)
        {
            m_ac = new AdminController(null);
        }
		try
		{
            m_ac.resetAllDBConnections(true);
		}
		catch (Exception e)
		{
            SilverTrace.error("admin", "ScheduledDBReset.doDBReset", "admin.EX_ERR_TIMEOUT_MANAGEMENT", e);
		}
	}
}

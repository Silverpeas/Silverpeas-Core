/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.organization;

import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.scheduler.simple.SimpleScheduler;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;

public class ScheduledDBReset implements SchedulerEventListener {

  // Local constants
  private static final String DBRESET_JOB_NAME = "ScheduledDBReset";
  private AdminController adminController = null;

  /**
   * Initialize timeout manager
   * @param cronString
   */
  public void initialize(String cronString) {
    try {
      SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
      Scheduler scheduler = schedulerFactory.getScheduler();
      // Remove previous scheduled job
      scheduler.unscheduleJob(DBRESET_JOB_NAME);
      if (StringUtil.isDefined(cronString)) {
        JobTrigger trigger = JobTrigger.triggerAt(cronString);
        scheduler.scheduleJob(DBRESET_JOB_NAME, trigger, this);
      }
    } catch (Exception e) {
      SilverTrace.error("admin", "ScheduledDBReset.initialize", "admin.EX_ERR_INITIALIZE", e);
    }

  }

  /**
   * This method is called periodically by the scheduler, it test for each peas of type
   * processManager if associated model contains states with timeout events If so, all the instances
   * of these peas that have the "timeout" states actives are read to check if timeout interval has
   * been reached. In that case, the administrator can be notified, the active state and the
   * instance are marked as timeout.
   * @see SimpleScheduler for parameters,
   */
  public void doDBReset() {
    if (adminController == null) {
      adminController = new AdminController(null);
    }
    try {
      adminController.resetAllDBConnections(true);
    } catch (Exception e) {
      SilverTrace.error("admin", "ScheduledDBReset.doDBReset",
          "admin.EX_ERR_TIMEOUT_MANAGEMENT", e);
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    SilverTrace.debug("admin", "ScheduledDBReset.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' is executed");
    doDBReset();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("admin", "ScheduledDBReset.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("admin", "ScheduledDBReset.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}

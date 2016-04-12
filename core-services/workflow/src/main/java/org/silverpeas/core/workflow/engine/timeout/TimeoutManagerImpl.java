/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.workflow.engine.timeout;

import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.workflow.api.TimeoutManager;
import org.silverpeas.core.workflow.api.Workflow;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.TimeoutEvent;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.engine.WorkflowEngineTask;
import org.silverpeas.core.workflow.engine.event.TimeoutEventImpl;
import org.silverpeas.core.workflow.engine.instance.ActionAndState;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.util.Date;

/**
 * The workflow engine services relate to error management.
 */
@Singleton
public class TimeoutManagerImpl implements TimeoutManager, SchedulerEventListener {

  // Local constants
  private static final String TIMEOUT_MANAGER_JOB_NAME = "WorkflowTimeoutManager";

  /**
   * Initialize timeout manager
   */
  @Override
  public void initialize() {
    try {
      SettingBundle settings = ResourceLocator.getSettingBundle(
          "org.silverpeas.workflow.engine.schedulerSettings");
      // List<SchedulerJob> jobList = SimpleScheduler.getJobList(this);
      Scheduler scheduler = SchedulerProvider.getScheduler();
      if (scheduler.isJobScheduled(TIMEOUT_MANAGER_JOB_NAME)) {
        // Remove previous scheduled job
        scheduler.unscheduleJob(TIMEOUT_MANAGER_JOB_NAME);
      }

      // Create new scheduled job
      String cronString = settings.getString("timeoutSchedule");
      JobTrigger trigger = JobTrigger.triggerAt(cronString);
      scheduler.scheduleJob(TIMEOUT_MANAGER_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }

  }

  /**
   * This method is called periodically by the scheduler, it test for each peas of type
   * processManager if associated model contains states with timeout events If so, all the instances
   * of these peas that have the "timeout" states actives are read to check if timeout interval has
   * been reached. In that case, the administrator can be notified, the active state and the
   * instance are marked as timeout
   */
  public void doTimeoutManagement() {
    Date beginDate = new Date();

    try {
      // parse all "process manager" peas
      ProcessInstance[] instances =
          Workflow.getProcessInstanceManager().getTimeOutProcessInstances();
      Date now = new Date();

      for (final ProcessInstance instance : instances) {
        try {
          ActionAndState timeoutActionAndState = instance.getTimeOutAction(now);
          TimeoutEvent event = new TimeoutEventImpl(instance, timeoutActionAndState.getState(),
              timeoutActionAndState.getAction());
          WorkflowEngineTask.addTimeoutRequest(event);
        } catch (WorkflowException e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    } finally {
      Date endDate = new Date();
      long delay = (endDate.getTime() - beginDate.getTime()) / 1000;
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    doTimeoutManagement();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverLogger.getLogger(this).error("The job {0} was not successful",
        anEvent.getJobExecutionContext().getJobName());
  }
}
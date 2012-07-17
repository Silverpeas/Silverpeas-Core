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

package com.silverpeas.workflow.engine.timeout;

import java.util.Date;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

import com.stratelia.webactiv.util.*;
import com.silverpeas.scheduler.*;

import com.silverpeas.workflow.api.*;
import com.silverpeas.workflow.api.event.TimeoutEvent;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.engine.event.TimeoutEventImpl;
import com.silverpeas.workflow.engine.instance.ActionAndState;
import com.silverpeas.workflow.engine.WorkflowEngineThread;
import com.silverpeas.scheduler.trigger.JobTrigger;

/**
 * The workflow engine services relate to error management.
 */
public class TimeoutManagerImpl implements TimeoutManager, SchedulerEventListener {

  // Local constants
  private static final String TIMEOUT_MANAGER_JOB_NAME = "WorkflowTimeoutManager";

  /**
   * Initialize timeout manager
   */
  @Override
  public void initialize() {
    try {
      ResourceLocator settings = new ResourceLocator(
          "com.silverpeas.workflow.engine.schedulerSettings", "");
      // List<SchedulerJob> jobList = SimpleScheduler.getJobList(this);
      SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
      Scheduler scheduler = schedulerFactory.getScheduler();
      if (scheduler.isJobScheduled(TIMEOUT_MANAGER_JOB_NAME)) {
        // Remove previous scheduled job
        scheduler.unscheduleJob(TIMEOUT_MANAGER_JOB_NAME);
      }

      // Create new scheduled job
      String cronString = settings.getString("timeoutSchedule");
      JobTrigger trigger = JobTrigger.triggerAt(cronString);
      scheduler.scheduleJob(TIMEOUT_MANAGER_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverTrace.error("workflowEngine", "TimeoutManagerImpl.initialize",
          "workflowEngine.EX_ERR_INITIALIZE", e);
    }

  }

  /**
   * This method is called periodically by the scheduler, it test for each peas of type
   * processManager if associated model contains states with timeout events If so, all the instances
   * of these peas that have the "timeout" states actives are read to check if timeout interval has
   * been reached. In that case, the administrator can be notified, the active state and the
   * instance are marked as timeout
   * @param currentDate the date when the method is called by the scheduler
   * @see SimpleScheduler for parameters,
   */
  public void doTimeoutManagement() {
    Date beginDate = new Date();

    try {
      // parse all "process manager" peas
      ProcessInstance[] instances =
          Workflow.getProcessInstanceManager().getTimeOutProcessInstances();
      Date now = new Date();

      for (int k = 0; k < instances.length; k++) {
        try {
          ActionAndState timeoutActionAndState = instances[k].getTimeOutAction(now);
          TimeoutEvent event = new TimeoutEventImpl(instances[k],
              timeoutActionAndState.getState(), timeoutActionAndState.getAction());
          WorkflowEngineThread.addTimeoutRequest(event);
          SilverTrace.info("workflowEngine",
              "TimeoutManagerImpl.doTimeoutManagement",
              "workflowEngine.WARN_TIMEOUT_DETECTED", "instance Id : '"
              + instances[k].getInstanceId() + "' state : '"
              + timeoutActionAndState.getState().getName());
        } catch (WorkflowException e) {
          SilverTrace.error("workflowEngine",
              "TimeoutManagerImpl.doTimeoutManagement",
              "workflowEngine.EX_ERR_TIMEOUT_MANAGEMENT", e);
          continue;
        }
      }
    } catch (Exception e) {
      SilverTrace.error("workflowEngine",
          "TimeoutManagerImpl.doTimeoutManagement",
          "workflowEngine.EX_ERR_TIMEOUT_MANAGEMENT", e);
    } finally {
      Date endDate = new Date();
      long delay = (endDate.getTime() - beginDate.getTime()) / 1000;
      SilverTrace.debug("workflowEngine",
          "TimeoutManagerImpl.doTimeoutManagement", "Duree de traitement : "
          + delay + " seconds.");
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    SilverTrace.debug("workflowEngine",
        "TimeoutManagerImpl.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' is executing");
    doTimeoutManagement();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("workflowEngine",
        "TimeoutManagerImpl.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("workflowEngine",
        "TimeoutManagerImpl.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}
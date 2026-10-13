/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.timeout;

import jakarta.inject.Inject;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.workflow.api.ProcessInstanceManager;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.engine.WorkflowTaskEngine;
import org.silverpeas.core.workflow.engine.event.TimeoutEventImpl;
import org.silverpeas.core.workflow.engine.instance.ActionAndState;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.Date;
import java.util.Optional;

/**
 * The workflow engine services relate to error management.
 */
@Service
public class TimeoutManagerImpl implements Initialization, SchedulerEventListener {

  @Inject
  private ProcessInstanceManager manager;

  @Inject
  private WorkflowTaskEngine taskEngine;

  // Local constants
  private static final String TIMEOUT_MANAGER_JOB_NAME = "WorkflowTimeoutManager";

  /**
   * Initialize timeout manager
   */
  @Override
  public void init() {
    try {
      SettingBundle settings = ResourceLocator.getSettingBundle(
          "org.silverpeas.workflow.engine.schedulerSettings");
      Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
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
   * This method is called periodically by the scheduler. It gets all the instances of the
   * workflows having an active state whose timeout date has been reached. For each of them, the
   * timeout action to launch is resolved and a request to process it is pushed to the workflow
   * engine. The processing of the timeout of an instance is independent of the processing of the
   * others: an error with an instance is logged and the next instance is then processed.
   */
  private void doTimeoutManagement() {
    try {
      var instances = manager.getTimeOutProcessInstances();
      Date now = new Date();
      for (final ProcessInstance instance : instances) {
        processTimeout(now, instance);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  private void processTimeout(final Date now, final ProcessInstance instance) {
    try {
      var workflowApp = Administration.get().getComponentInstLight(instance.getModelId());
      if (workflowApp == null || workflowApp.isRemoved()) {
        return;
      }
      Optional<ActionAndState> timeoutAction = instance.getTimeOutAction(now);
      if (timeoutAction.isPresent()) {
        ActionAndState actionAndState = timeoutAction.get();
        taskEngine.addTimeoutRequest(new TimeoutEventImpl(instance, actionAndState.getState(),
            actionAndState.getAction()));
      } else {
        SilverLogger.getLogger(this)
            .warn("No timeout action to process for the process instance {0} (workflow {1})",
                instance.getInstanceId(), instance.getModelId());
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("The timeout of the process instance {0} (workflow {1}) cannot be processed: {2}",
              new Object[]{instance.getInstanceId(), instance.getModelId(), e.getMessage()}, e);
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    doTimeoutManagement();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    // Nothing to do here
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverLogger.getLogger(this).error("The job {0} was not successful",
        anEvent.getJobExecutionContext().getJobName());
  }

}
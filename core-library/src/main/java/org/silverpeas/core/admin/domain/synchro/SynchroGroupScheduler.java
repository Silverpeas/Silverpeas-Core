/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.admin.domain.synchro;

import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.kernel.logging.SilverLogger;

public class SynchroGroupScheduler implements SchedulerEventListener {

  public static final String ADMIN_SYNCHRO_GROUP_JOB_NAME = "AdminSynchroGroupJob";

  /**
   * Initializing the JOB to schedule it according the CRON data with given synchronized groups.
   * @param cron CRON data.
   */
  public void initialize(String cron) {
    try {
      Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
      scheduler.unscheduleJob(ADMIN_SYNCHRO_GROUP_JOB_NAME);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(ADMIN_SYNCHRO_GROUP_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    SynchroGroupManager.get().synchronize();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    // nothing to do
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    final String jobName = anEvent.getJobExecutionContext().getJobName();
    SilverLogger.getLogger(this).error("The domain synchronization job {0} failed!", jobName);
  }
}

/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.scheduler;

import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.annotation.Nullable;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import java.text.ParseException;

/**
 * Initializer taking in charge the scheduling of a job at Silverpeas startup and of its
 * unscheduling at Silverpeas shutdown. If no job trigger is defined, no scheduling is performed.
 * The volatile scheduler is used by this initializer in the job scheduling.
 *
 * @author mmoquillon
 */
public abstract class SchedulingInitializer implements Initialization {

  @Inject
  private Scheduler scheduler;

  @Override
  public final void init() throws SchedulerException {
    preSchedule();
    if (isSchedulingEnabled()) {
      Job job = getJob();
      scheduler.unscheduleJob(job.getName());
      if (getTrigger() != null) {
        scheduler.scheduleJob(job, getTrigger());
      }
    }
  }

  @Override
  public final void release() throws SchedulerException {
    if (isSchedulingEnabled()) {
      Job job = getJob();
      scheduler.unscheduleJob(job.getName());
    }
    postUnschedule();
  }

  /**
   * Performs pre-scheduling treatments. By default does nothing. It is invoked even if the
   * scheduling isn't done (because not enabled or no trigger can be found).
   */
  protected void preSchedule() {
    // does nothing. For children classes
  }

  /**
   * Performs post-unscheduling treatments. By default does nothing. It is invoked even if the job
   * hasn't be scheduled.
   */
  protected void postUnschedule() {
    // does nothing. For children classes
  }

  /**
   * Gets the trigger to use for scheduling the job. By default, the trigger is built from a CRON
   * statement. If this statement is empty, then the trigger isn't created and null is then
   * returned. Overrides this method to specify a hard-coded trigger or a trigger built from other
   * than a CRON statement.
   *
   * @return a {@link JobTrigger} instance or null if no trigger can be found or defined.
   * @throws SchedulerException if the creation of the job trigger fails.
   */
  @Nullable
  protected JobTrigger getTrigger() throws SchedulerException {
    try {
      String cron = getCron();
      if (StringUtil.isDefined(cron)) {
        return JobTrigger.triggerAt(getCron());
      }
      return null;
    } catch (ParseException e) {
      throw new SchedulerException(e.getMessage(), e);
    }
  }

  /**
   * Gets the CRON statement to use to schedule the execution of the job.
   *
   * @return a CRON statement.
   * @see org.silverpeas.core.scheduler.trigger.CronJobTrigger
   */
  @NonNull
  protected abstract String getCron();

  /**
   * Gets the job to schedule and unschedule in this initializer.
   *
   * @return the job to schedule.
   */
  @NonNull
  protected abstract Job getJob();

  /**
   * Is the conditions required to schedule the job are all matched? If yes, the job will be then
   * scheduled. Otherwise nothing will be done.
   *
   * @return true if the job can be scheduled, false otherwise.
   */
  protected abstract boolean isSchedulingEnabled();

}
  
/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.scheduler.quartz;

import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * Executor of a {@link org.silverpeas.core.scheduler.Job} instance. Such executor implements the
 * {@link org.quartz.Job} interface in order to be scheduled in a Quartz scheduler instead of the
 * actual job to execute which is a job specific to the Silverpeas Scheduler Engine; it is the link
 * between the Scheduler API with the Scheduler implementation built atop of the Quartz API. The
 * executor fetches the details about the actual job to execute and about the Scheduler event
 * listener to invoke directly from the Quartz job execution. Those information needs then to be
 * passed a long with the scheduling of this executor.
 * context
 * @author mmoquillon
 */
public abstract class JobExecutor implements org.quartz.Job {

  protected abstract Job getJob(final JobDetail jobDetail) throws JobExecutionException;

  protected abstract SchedulerEventListener getSchedulerEventListener(final JobDetail jobDetail)
      throws JobExecutionException;

  @Override
  public final void execute(org.quartz.JobExecutionContext jec) throws JobExecutionException {
    JobDetail jobDetail = jec.getJobDetail();
    jobDetail.getJobDataMap().put(QuartzScheduler.JOB_SCHEDULED, false);
    Job job = getJob(jobDetail);
    SchedulerEventListener eventListener = getSchedulerEventListener(jobDetail);
    JobExecutionContext context = JobExecutionContext.createWith(job.getName(), jec.getFireTime());
    execute(context, job, eventListener);
  }

  protected final void execute(final JobExecutionContext context, final Job job,
      final SchedulerEventListener eventListener) {
    CacheServiceProvider.clearAllThreadCaches();
    if (eventListener == null) {
      try {
        job.execute(context);
      } catch (Exception ex) {
        SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      }
    } else {
      try {
        eventListener.triggerFired(SchedulerEvent.triggerFired(context));
        job.execute(context);
        eventListener.jobSucceeded(SchedulerEvent.jobSucceeded(context));
      } catch (Exception ex) {
        try {
          eventListener.jobFailed(SchedulerEvent.jobFailed(context, ex));
        } catch (Exception e) {
          SilverLogger.getLogger(this)
              .error("Error while executing job {0}: {1}",
                  new String[]{job.getName(), e.getMessage()}, e);
        }
      }
    }
  }
}
  
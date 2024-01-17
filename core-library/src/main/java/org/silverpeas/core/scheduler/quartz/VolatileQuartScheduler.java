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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.scheduler.quartz;

import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.SchedulerEventListener;

import javax.enterprise.inject.Default;
import javax.inject.Singleton;

/**
 * A volatile scheduler implementation using Quartz as scheduling backend. It wraps a Quartz
 * scheduler and delegates to it all of the calls after transforming the parameters into their
 * Quartz counterparts. The Quartz scheduler is configured to use a RAM Job store to store all the
 * scheduled jobs. Those jobs will be lost after a scheduler shutdown and they will require to be
 * scheduled again at the scheduler starting.
 */
@Default
@Service
@Singleton
public class VolatileQuartScheduler extends QuartzScheduler {

  /**
   * Constructs a new volatile scheduler.
   */
  protected VolatileQuartScheduler() {
  }

  @Override
  public void init() {
    setUpQuartzScheduler(null);
  }

  @Override
  public void release() throws Exception {
    shutdown();
  }

  @Override
  protected Job encodeJob(final Job job) {
    return job;
  }

  @Override
  protected SchedulerEventListener encodeEventListener(final SchedulerEventListener listener) {
    return listener;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Class<VolatileJobExecutor> getJobExecutor() {
    return VolatileJobExecutor.class;
  }

  @Override
  protected <T> void execute(final SchedulingTask<T> schedulingTask) throws SchedulerException {
    schedulingTask.execute();
  }

  public static class VolatileJobExecutor extends JobExecutor {
    protected Job getJob(final JobDetail jobDetail) throws JobExecutionException {
      try {
        return (Job) jobDetail.getJobDataMap().get(ACTUAL_JOB);
      } catch (ClassCastException e) {
        throw new JobExecutionException(e);
      }
    }

    protected SchedulerEventListener getSchedulerEventListener(final JobDetail jobDetail)
        throws JobExecutionException {
      try {
        return (SchedulerEventListener) jobDetail.getJobDataMap().get(JOB_LISTENER);
      } catch (ClassCastException e) {
        throw new JobExecutionException(e);
      }
    }
  }
}
  
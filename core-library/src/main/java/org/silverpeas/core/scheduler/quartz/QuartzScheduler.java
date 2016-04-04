/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.ScheduledJob;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.enterprise.inject.Default;

import static org.silverpeas.core.util.ArgumentAssertion.assertDefined;
import static org.silverpeas.core.util.ArgumentAssertion.assertNotNull;

/**
 * A scheduler implementation using Quartz as scheduling backend. It wraps a Quartz scheduler and
 * delegates to it all of the call after transforming the parameters into their Quartz
 * counterparts.
 */
@Default
public class QuartzScheduler implements Scheduler {

  /**
   * The key in the job data map that refers the scheduled job in the scheduler. The scheduled job
   * is set in the data map in order to be retrieved at job execution. The actual job execution
   * firing will be delegated by this scheduled job.
   * @see JobDataMap
   */
  private static final String SCHEDULED_JOB = "job";
  /**
   * The Quartz scheduler (the backend).
   */
  private org.quartz.Scheduler backend;

  /**
   * Constructs a new scheduler and bootstraps the Quartz scheduler backend.
   * @throws SchedulerException if the unerlying Quartz scheduler setting up fails.
   */
  protected QuartzScheduler() throws SchedulerException {
    StdSchedulerFactory quartzSchedulerFactory = new StdSchedulerFactory();
    try {
      backend = quartzSchedulerFactory.getScheduler();
      backend.start();
    } catch (org.quartz.SchedulerException ex) {
      SilverLogger.getLogger(this).error("Quartz Scheduler failed to start!", ex);
      throw new SchedulerException(ex.getMessage(), ex);
    }
  }

  @Override
  public ScheduledJob scheduleJob(String jobName, JobTrigger trigger,
      SchedulerEventListener listener) throws SchedulerException {
    checkArguments(jobName, trigger, listener);
    QuartzSchedulerJob job =
        new QuartzSchedulerJob(jobName, trigger).withSchedulerEventListener(listener);
    JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class).withIdentity(jobName).build();
    jobDetail.getJobDataMap().put(SCHEDULED_JOB, job);
    try {
      schedule(job, jobDetail);
      return job;
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error("The scheduling of the job ''{0}'' failed!",
          new String[] {jobName}, ex);
      throw new SchedulerException(ex.getMessage(), ex);
    }
  }

  @Override
  public ScheduledJob scheduleJob(Job theJob, JobTrigger trigger, SchedulerEventListener listener)
      throws SchedulerException {
    checkArguments(theJob, trigger);
    QuartzSchedulerJob job =
        new QuartzSchedulerJob(theJob, trigger).withSchedulerEventListener(listener);
    JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class).withIdentity(theJob.getName()).build();
    jobDetail.getJobDataMap().put(SCHEDULED_JOB, job);
    try {
      schedule(job, jobDetail);
      return job;
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error("The scheduling of the job ''{0}'' failed!",
          new String[]{theJob.getName()}, ex);
      throw new SchedulerException(ex.getMessage(), ex);
    }
  }

  @Override
  public ScheduledJob scheduleJob(Job theJob, JobTrigger trigger) throws SchedulerException {
    return scheduleJob(theJob, trigger, null);
  }

  @Override
  public void unscheduleJob(String jobName) throws SchedulerException {
    checkJobName(jobName);
    try {
      this.backend.deleteJob(new JobKey(jobName, org.quartz.Scheduler.DEFAULT_GROUP));
    } catch (org.quartz.SchedulerException ex) {
      SilverLogger.getLogger(this).error("The unscheduling of the job ''{0}'' failed!",
          new String[]{jobName}, ex);
      throw new SchedulerException(ex.getMessage(), ex);
    }
  }

  @Override
  public boolean isJobScheduled(String jobName) {
    checkJobName(jobName);
    try {
      JobDetail jobDetail =
          this.backend.getJobDetail(new JobKey(jobName, org.quartz.Scheduler.DEFAULT_GROUP));
      return jobDetail != null;
    } catch (org.quartz.SchedulerException ex) {
      return false;
    }
  }

  @Override
  public void shutdown() throws SchedulerException {
    try {
      this.backend.shutdown();
    } catch (org.quartz.SchedulerException ex) {
      SilverLogger.getLogger(this).error("The scheduler shutdown failed!", ex);
      throw new SchedulerException(ex.getMessage(), ex);
    }
  }

  /**
   * Schedules the specified job with the specified scheduling detail within the Quartz scheduler.
   * @param job the job to schedule.
   * @param jobDetail the detail about the scheduling of the job. It contains among others
   * execution
   * parameters.
   * @throws SchedulerException if an error occurs while scheduling the specified job.
   */
  private void schedule(final QuartzSchedulerJob job, final JobDetail jobDetail)
      throws org.quartz.SchedulerException {
    Trigger quartzTrigger = QuartzTriggerBuilder.buildFrom(job);
    this.backend.scheduleJob(jobDetail, quartzTrigger);
    job.setNextExecutionTime(quartzTrigger.getNextFireTime());
  }

  /**
   * Checks the specified arguments are well defined.
   * @param jobName the job name should be not null and not empty.
   * @param trigger the job trigger should be not null.
   * @param listener the scheduler event listener should be not null.
   */
  private static void checkArguments(final String jobName, final JobTrigger trigger,
      final SchedulerEventListener listener) {
    checkJobName(jobName);
    checkJobTrigger(trigger);
    assertNotNull(listener, "The scheduler event listener shouldn't be null");
  }

  /**
   * Checks the specified arguments are well defined.
   * @param job the job name should be not null and not empty.
   * @param trigger the trigger should be not null.
   */
  private static void checkArguments(final Job job, final JobTrigger trigger) {
    assertNotNull(job, "The job should not be null");
    checkJobTrigger(trigger);
  }

  /**
   * Checks the specified job name is well defined.
   * @param jobName the job name should not be null and empty.
   */
  private static void checkJobName(final String jobName) {
    assertDefined(jobName, "The job name should be defined");
  }

  /**
   * Checks the specified job trigger is well defined.
   * @param trigger the trigger should not be null.
   */
  private static void checkJobTrigger(final JobTrigger trigger) {
    assertNotNull(trigger, "The job trigger shouldn't be null");
  }

  /**
   * A job as registered within the Quartz scheduler. This job performs the actual job execution
   * flow, taking into account the exceptions handling, and the scheduler event firing.
   */
  public static class QuartzJob implements org.quartz.Job {

    @Override
    public void execute(org.quartz.JobExecutionContext jec) throws JobExecutionException {
      JobDataMap data = jec.getJobDetail().getJobDataMap();
      QuartzSchedulerJob job = (QuartzSchedulerJob) data.get(SCHEDULED_JOB);
      SchedulerEventListener eventListener = job.getSchedulerEventListener();
      job.setNextExecutionTime(jec.getNextFireTime());
      JobExecutionContext context =
          JobExecutionContext.createWith(job.getName(), jec.getFireTime());
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
            SilverLogger.getLogger(this).error("Error while executing job {0}: {1}",
                new String[] {job.getName(), e.getMessage()}, e);
          }
        }
      }
    }
  }
}

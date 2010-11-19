/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.scheduler.quartz;

import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.Job;
import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.ScheduledJob;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerException;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Service;
import static com.silverpeas.scheduler.SchedulerFactory.*;
import static com.silverpeas.util.AssertArgument.*;

/**
 * A scheduler implementation using Quartz as scheduling backend.
 * It wraps a Quartz scheduler and delegates to it all of the call after transforming the parameters
 * into their Quartz counterparts.
 */
@Service("scheduler")
public class QuartzScheduler
    implements Scheduler {

  /**
   * The key in the job data map that refers the scheduled job in the scheduler.
   * The scheduled job is set in the data map in order to be retrieved at job execution. The actual
   * job execution firing will be delegated by this scheduled job.
   * @see JobDataMap
   */
  private static final String SCHEDULED_JOB = "job";
  /**
   * The Quartz scheduler (the backend).
   */
  private org.quartz.Scheduler quartzScheduler;
  /**
   * A Quartz trigger builder.
   */
  private QuartzTriggerBuilder triggerBuilder = new QuartzTriggerBuilder();

  /**
   * Constructs a new scheduler and bootstraps the Quartz scheduler backend.
   * @throws SchedulerException if the unerlying Quartz scheduler setting up fails.
   */
  protected QuartzScheduler() throws SchedulerException {
    StdSchedulerFactory quartzSchedulerFactory = new StdSchedulerFactory();
    try {
      quartzScheduler = quartzSchedulerFactory.getScheduler();
      quartzScheduler.start();
      SilverTrace.info(MODULE_NAME, getClass().getSimpleName() + ".<init>()", "root.EX_NO_MESSAGE",
          getClass().getSimpleName() + " is started");
    } catch (org.quartz.SchedulerException ex) {
      SilverTrace.fatal(MODULE_NAME, getClass().getSimpleName() + ".<init>()",
          "root.EX_NO_MESSAGE", getClass().getSimpleName() + " failed to start", ex);
      throw new SchedulerException(ex.getMessage(), ex);
    }
  }

  @Override
  public ScheduledJob scheduleJob(String jobName,
      JobTrigger trigger,
      SchedulerEventListener listener) throws SchedulerException {
    checkArguments(jobName, trigger, listener);
    QuartzSchedulerJob job =
        new QuartzSchedulerJob(jobName, trigger).withSchedulerEventListener(listener);
    JobDetail jobDetail = new JobDetail(jobName, QuartzJob.class);
    jobDetail.getJobDataMap().put(SCHEDULED_JOB, job);
    try {
      schedule(job, jobDetail);
      return job;
    } catch (Exception ex) {
      SilverTrace.error(MODULE_NAME, getClass().getSimpleName() + ".scheduleJob()",
          "root.EX_NO_MESSAGE", "The scheduling of the job '" + jobName + "' failed!", ex);
      throw new SchedulerException(ex.getMessage(), ex);
    }
  }

  @Override
  public ScheduledJob scheduleJob(Job theJob,
      JobTrigger trigger,
      SchedulerEventListener listener) throws SchedulerException {
    checkArguments(theJob, trigger);
    QuartzSchedulerJob job =
        new QuartzSchedulerJob(theJob, trigger).withSchedulerEventListener(listener);
    JobDetail jobDetail = new JobDetail(theJob.getName(), QuartzJob.class);
    jobDetail.getJobDataMap().put(SCHEDULED_JOB, job);
    try {
      schedule(job, jobDetail);
      return job;
    } catch (Exception ex) {
      SilverTrace.error(MODULE_NAME, getClass().getSimpleName() + ".scheduleJob()",
          "root.EX_NO_MESSAGE", "The scheduling of the job '" + theJob.getName() + "' failed!", ex);
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
      this.quartzScheduler.deleteJob(jobName, org.quartz.Scheduler.DEFAULT_GROUP);
    } catch (org.quartz.SchedulerException ex) {
      SilverTrace.error(MODULE_NAME, getClass().getSimpleName() + ".unscheduleJob()",
          "root.EX_NO_MESSAGE", "The unscheduling of the job '" + jobName + "' failed!", ex);
      throw new SchedulerException(ex.getMessage(), ex);
    }
  }

  @Override
  public boolean isJobScheduled(String jobName) {
    checkJobName(jobName);
    try {
      JobDetail jobDetail =
          this.quartzScheduler.getJobDetail(jobName, org.quartz.Scheduler.DEFAULT_GROUP);
      return jobDetail != null;
    } catch (org.quartz.SchedulerException ex) {
      return false;
    }
  }

  @Override
  public void shutdown() throws SchedulerException {
    try {
      SilverTrace.info(MODULE_NAME, getClass().getSimpleName() + ".shutdown()",
          "root.EX_NO_MESSAGE", getClass().getSimpleName() + " is shutdown");
      this.quartzScheduler.shutdown();
    } catch (org.quartz.SchedulerException ex) {
      SilverTrace.fatal(MODULE_NAME, getClass().getSimpleName() + ".shutdown()",
          "root.EX_NO_MESSAGE", "The scheduler shutdown failed!", ex);
      throw new SchedulerException(ex.getMessage(), ex);
    }
  }

  /**
   * Schedules the specified job with the specified scheduling detail within the Quartz scheduler.
   * @param job the job to schedule.
   * @param jobDetail the detail about the scheduling of the job. It contains among others
   * execution parameters.
   * @throws Exception if an error occurs while scheduling the specified job.
   */
  private void schedule(final QuartzSchedulerJob job, final JobDetail jobDetail) throws Exception {
    Trigger quartzTrigger = triggerBuilder.buildFrom(job);
    this.quartzScheduler.scheduleJob(jobDetail, quartzTrigger);
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
   * A job as registered within the Quartz scheduler.
   * This job performs the actual job execution flow, taking into account the exceptions handling,
   * and the scheduler event firing.
   */
  public static class QuartzJob implements org.quartz.Job {

    @Override
    public void execute(org.quartz.JobExecutionContext jec) throws JobExecutionException {
      JobDataMap data = jec.getJobDetail().getJobDataMap();
      QuartzSchedulerJob job = (QuartzSchedulerJob) data.get(SCHEDULED_JOB);
      SchedulerEventListener eventListener = job.getSchedulerEventListener();
      job.setNextExecutionTime(jec.getNextFireTime());
      JobExecutionContext context = JobExecutionContext.createWith(job.getName(),
          jec.getFireTime());
      if (eventListener == null) {
        try {
          job.execute(context);
        } catch (Exception ex) {
          SilverTrace.error(MODULE_NAME, QuartzScheduler.class.getName(), ex.getMessage(), ex);
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
            SilverTrace.error(MODULE_NAME, QuartzScheduler.class.getName(), e.getMessage(), ex);
          }
        }
      }
    }
  }}

/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.scheduler.quartz;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.EmptyJob;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.ScheduledJob;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.Process;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;

import static org.silverpeas.core.util.ArgumentAssertion.assertDefined;
import static org.silverpeas.core.util.ArgumentAssertion.assertNotNull;

/**
 * An abstract scheduler implementation using Quartz as scheduling backend. It wraps a Quartz
 * scheduler and delegates to it all of the calls after transforming the parameters into their
 * Quartz counterparts. The way the jobs are stored depends on the concrete type of this abstract
 * class. It defines the job scheduling mechanism by using the Quartz API.
 */
public abstract class QuartzScheduler implements Scheduler, Initialization {

  /**
   * The key in the job data map that refers to the job implementation in the scheduler. This job
   * is set in the data map in order to be retrieved at job execution. The actual job execution
   * firing will be delegated to this job once constructed.
   * @see JobDataMap
   */
  static final String ACTUAL_JOB = "job";
  /**
   * The key in the job data map that refers the event listener registered in the scheduler for a
   * given job triggering. The listener is set in the data map in order to be retrieved at
   * job execution.
   * @see JobDataMap
   */
  static final String JOB_LISTENER = "listener";

  /**
   * The key in the job data map that refers the scheduling status of the job. This status is
   * explicitly set when the job is just scheduled and it is unset once its execution is triggered.
   */
  static final String JOB_SCHEDULED = "scheduled";

  /**
   * The Quartz scheduler (the backend).
   */
  private org.quartz.Scheduler quartz;

  protected QuartzScheduler() {
  }

  @Override
  public int getPriority() {
    return 0;
  }

  /**
   * Creates a Quartz scheduler from the specified Quartz properties file and starts it.
   * @param quartzProperties the path of the Quartz properties file in the Silverpeas
   * configuration repository.
   * @throws QuartzSchedulerException if no Quartz scheduler cannot be instantiated or it cannot be
   * started.
   */
  final void setUpQuartzScheduler(final String quartzProperties) {
    try {
      StdSchedulerFactory quartzSchedulerFactory = new StdSchedulerFactory();
      if (StringUtil.isDefined(quartzProperties)) {
        quartzSchedulerFactory.initialize(
            ResourceLocator.getSettingsAsProperties(quartzProperties));
      }
      quartz = quartzSchedulerFactory.getScheduler();
      if (!quartz.isStarted()) {
        quartz.start();
      }
    } catch (org.quartz.SchedulerException ex) {
      SilverLogger.getLogger(this).error("Quartz Scheduler failed to start!", ex);
      throw new QuartzSchedulerException(ex.getMessage(), ex);
    }
  }

  /**
   * Encodes the specified job into an object ready to be handled by the job executor related to
   * this scheduler.
   * Each type of scheduler has its own way to encode a job in order to be retrieved by the job
   * executor related by the scheduler.
   * @param job the job to encode for the job executor related to this scheduler.
   * @return the encoded job.
   */
  protected abstract Object encodeJob(final Job job);

  /**
   * Encodes the specified scheduler event listener into an object ready to be handled by the job
   * executor related to this scheduler.
   * Each type of scheduler has its own way to encode an event listener in order to be retrieved
   * by the job executor related by the scheduler.
   * @param listener the event listener to encode for the job executor related to this scheduler.
   * @return the encoded scheduler event listener.
   */
  protected abstract Object encodeEventListener(final SchedulerEventListener listener);

  /**
   * Gets the job executor related to this scheduler. Each type of scheduler has its own job
   * executor that knows how to handle jobs and event listeners.
   * @param <T> the concrete type of the job executor
   * @return the class of the job executor.
   */
  protected abstract <T extends JobExecutor> Class<T> getJobExecutor();

  /**
   * Builds a Quartz {@link org.quartz.JobDetail} instance for the specified job by
   * setting both the {@link Job} to execute and  the {@link SchedulerEventListener} to invoke.
   * @param job the job to schedule.
   * @param listener the scheduler event listener to set with the job.
   * @return a {@link JobDetail} object.
   */
  private JobDetail buildJobDetail(final Job job, final SchedulerEventListener listener) {
    JobDetail jobDetail = JobBuilder.newJob(getJobExecutor()).withIdentity(job.getName()).build();
    jobDetail.getJobDataMap().put(ACTUAL_JOB, encodeJob(job));
    if (listener != null) {
      jobDetail.getJobDataMap().put(JOB_LISTENER, encodeEventListener(listener));
    }
    return jobDetail;
  }

  /**
   * Executes the specified task. The execution of the task is delegated to this method that
   * can wraps the task execution within a peculiar execution context like a transaction for
   * example for persistent jobs.
   * @param schedulingTask the scheduling task to execute.
   */
  protected abstract void execute(final SchedulingTask schedulingTask)
      throws org.quartz.SchedulerException;

  private void fireNow(final Date expectedFireTime, final Job job,
      final SchedulerEventListener listener) throws org.quartz.SchedulerException {
    Date fireTime = expectedFireTime;
    if (fireTime == null) {
      fireTime = new Date();
    }
    Class<? extends JobExecutor> jobExecutorClass = getJobExecutor();
    JobExecutor executor;
    try {
      executor = jobExecutorClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new org.quartz.SchedulerException(e.getMessage(), e);
    }
    org.silverpeas.core.scheduler.JobExecutionContext context =
        org.silverpeas.core.scheduler.JobExecutionContext.createWith(job.getName(), fireTime);
    executor.execute(context, job, listener);
  }

  @Override
  public ScheduledJob scheduleJob(String jobName, JobTrigger trigger,
      SchedulerEventListener listener) throws SchedulerException {
    checkArguments(jobName, trigger, listener);
    return scheduleJob(new EmptyJob(jobName), trigger, listener);
  }

  @Override
  public ScheduledJob scheduleJob(Job theJob, JobTrigger jobTrigger,
      SchedulerEventListener listener) throws SchedulerException {
    checkArguments(theJob, jobTrigger);
    Trigger quartzTrigger = QuartzTriggerBuilder.forJob(theJob.getName()).buildFrom(jobTrigger);
    JobDetail jobDetail = buildJobDetail(theJob, listener);
    try {
      if (isInPast(quartzTrigger)) {
        fireNow(quartzTrigger.getFinalFireTime(), theJob, listener);
      } else {
        jobDetail.getJobDataMap().put(JOB_SCHEDULED, true);
        execute(() -> this.quartz.scheduleJob(jobDetail, quartzTrigger));
      }
      return new QuartzScheduledJob(quartzTrigger);
    } catch (org.quartz.SchedulerException ex) {
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
      execute(() -> this.quartz.deleteJob(JobKey.jobKey(jobName)));
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
      JobDetail jobDetail = this.quartz.getJobDetail(JobKey.jobKey(jobName));
      boolean isScheduled = false;
      if (jobDetail != null) {
        isScheduled = jobDetail.getJobDataMap().getBoolean(JOB_SCHEDULED);
      }
      return isScheduled;
    } catch (org.quartz.SchedulerException e) {
      SilverLogger.getLogger(this).warn(e);
      return false;
    }
  }

  public Optional<ScheduledJob> getScheduledJob(final String jobName) {
    checkJobName(jobName);
    try {
      Trigger trigger = this.quartz.getTrigger(TriggerKey.triggerKey(jobName));
      return Optional.of(new QuartzScheduledJob(trigger));
    } catch (org.quartz.SchedulerException e) {
      SilverLogger.getLogger(this).warn(e);
      return Optional.empty();
    }
  }

  @Override
  public void shutdown() throws SchedulerException {
    try {
      this.quartz.shutdown();
    } catch (org.quartz.SchedulerException ex) {
      SilverLogger.getLogger(this).error("The scheduler shutdown failed!", ex);
      throw new SchedulerException(ex.getMessage(), ex);
    }
  }

  private boolean isInPast(final Trigger trigger) {
    return !trigger.mayFireAgain() && trigger.getFinalFireTime() != null &&
        trigger.getFinalFireTime().before(Date.from(OffsetDateTime.now().toInstant()));
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

  protected interface SchedulingTask<T> extends Process<T> {

    @Override
    T execute() throws org.quartz.SchedulerException;
  }
}

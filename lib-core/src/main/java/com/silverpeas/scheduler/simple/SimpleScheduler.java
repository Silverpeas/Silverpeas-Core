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

package com.silverpeas.scheduler.simple;

import com.silverpeas.scheduler.trigger.CronJobTrigger;
import com.silverpeas.scheduler.trigger.FixedPeriodJobTrigger;
import com.silverpeas.scheduler.Job;
import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.ScheduledJob;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerException;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.silverpeas.scheduler.SchedulerFactory.*;

/**
 * A simple scheduler implementation. It provides a easy way to schedule jobs at given moments in
 * time. The job execution policy is provided by a job trigger, represented as a
 * <code>JobTrigger</code> object; <code>JobTrigger</code> objects control when the job has to be
 * executed in a repeatedly way. The execution of the job itself can be actually performed in two
 * ways:
 * <ul>
 * <li>by a scheduling event listener through the reception of the event mapped with the job
 * execution triggering,</li>
 * <li>by a <code>Job</code> object that wraps the execution code.
 * </ul>
 */
@Deprecated
public class SimpleScheduler implements Scheduler {

  private final Map<String, SchedulerJob> jobs =
      new HashMap<String, SchedulerJob>();
  private ExecutorService jobSchedulingExecutor = Executors.newCachedThreadPool();

  /**
   * Is the job identified by the specified name is scheduled by this scheduler?
   * @param jobName the job name.
   * @return true if the job identified by the specified name is scheduled by this scheduler, false
   * otherwise.
   */
  @Override
  public boolean isJobScheduled(final String jobName) {
    return jobs.containsKey(jobName);
  }

  /**
   * This method removes a job
   * @param aJob A job object
   */
  private synchronized void removeJob(SchedulerJob aJob) {
    aJob.stop();
    jobs.remove(aJob.getJobName());
  }

  /**
   * Unschedules the job with the specified name. If no job is scheduled under the specified name,
   * nothing is done.
   * @param jobName the name of the job to unschedule.
   */
  @Override
  public void unscheduleJob(final String jobName) {
    if (jobs.containsKey(jobName)) {
      SchedulerJob job = jobs.get(jobName);
      removeJob(job);
    }
  }

  /**
   * This method stops all scheduling jobs
   */
  private synchronized void stopAllJobs() {
    for (SchedulerJob scheduledJob : jobs.values()) {
      scheduledJob.stop();
    }
    jobs.clear();
  }

  /**
   * This method kills all active jobs. The unique instance of the SimpleScheduler will be
   * destroyed.
   */
  @Override
  public synchronized void shutdown() {
    SilverTrace.debug(MODULE_NAME, "SimpleScheduler",
        "-------------------- SimpleScheduler shutdown --------------------",
        new Exception("ForStack"));
    stopAllJobs();
    jobSchedulingExecutor.shutdown();
  }

  /**
   * The constructor is private because it will be created internal.
   */
  protected SimpleScheduler() {
    SilverTrace.debug(MODULE_NAME, "SimpleScheduler",
        "-------------------- SimpleScheduler started --------------------");
  }

  /**
   * This method adds a job to the internal list of jobs an starts the job
   * @param aJobOwner A job owner
   * @param aNewJob A new job
   */
  private synchronized ScheduledJob addJob(SchedulerJob aNewJob) throws SchedulerException {
    if (jobs.containsKey(aNewJob.getJobName())) {
      throw new SchedulerException("An job is already scheduled under the name '" + aNewJob.
          getJobName() + "'");
    }
    jobs.put(aNewJob.getJobName(), aNewJob);
    jobSchedulingExecutor.submit(aNewJob);
    return aNewJob;
  }

  @Override
  public ScheduledJob scheduleJob(final String jobName,
      final JobTrigger trigger,
      final SchedulerEventListener listener) throws SchedulerException {
    checkRequiredArgs(jobName, trigger, listener);
    if (trigger instanceof FixedPeriodJobTrigger) {
      FixedPeriodJobTrigger jobTrigger = (FixedPeriodJobTrigger) trigger;
      SchedulerEventJob newJob = new SchedulerEventJobMinute(this, listener,
          jobName, jobTrigger);
      return addJob(newJob);
    } else if (trigger instanceof CronJobTrigger) {
      CronJobTrigger cronJobTrigger = (CronJobTrigger) trigger;
      SchedulerEventJob newJob = new SchedulerEventJob(this, listener,
          jobName);
      newJob.setSchedulingParameter(cronJobTrigger);
      return addJob(newJob);
    }
    throw new IllegalArgumentException("Trigger " + trigger.getClass().getName()
        + " not supported yet");
  }

  @Override
  public ScheduledJob scheduleJob(final Job theJob,
      final JobTrigger trigger,
      final SchedulerEventListener listener) throws SchedulerException {
    if (theJob == null) {
      throw new IllegalArgumentException("The job is required!");
    }
    String jobName = theJob.getName();
    checkRequiredArgs(jobName, trigger, listener);
    if (trigger instanceof FixedPeriodJobTrigger) {
      FixedPeriodJobTrigger jobTrigger = (FixedPeriodJobTrigger) trigger;
      SchedulerMethodJob newJob = new SchedulerMethodJobMinute(this, listener,
          jobName, jobTrigger);
      newJob.setExecutionParameter(new JobExecutor(theJob), "execute");
      return addJob(newJob);
    } else if (trigger instanceof CronJobTrigger) {
      CronJobTrigger cronJobTrigger = (CronJobTrigger) trigger;
      SchedulerMethodJob newJob = new SchedulerMethodJob(this, listener, jobName);
      newJob.setSchedulingParameter(cronJobTrigger);
      newJob.setExecutionParameter(new JobExecutor(theJob), "execute");
      return addJob(newJob);
    }
    throw new IllegalArgumentException("Trigger " + trigger.getClass().getName()
        + " not supported yet");
  }

  /**
   * Checks the required specified arguments are correctly set, otherwise throws an
   * IllegalArgumentException exception.
   * @param jobName a job name,
   * @param trigger a trigger of a job,
   * @param handler an scheduling event handler.
   */
  private void checkRequiredArgs(final String jobName,
      final JobTrigger trigger,
      final SchedulerEventListener handler) {
    if (jobName == null || jobName.isEmpty()) {
      throw new IllegalArgumentException("The job name is required!");
    }
    if (trigger == null) {
      throw new IllegalArgumentException("The job trigger is required!");
    }
    if (handler == null) {
      throw new IllegalArgumentException("The scheduling event handler is requried!");
    }
  }

  @Override
  public ScheduledJob scheduleJob(Job theJob, JobTrigger trigger) throws SchedulerException {
    return scheduleJob(theJob, trigger, new SchedulerEventListener() {

      @Override
      public void triggerFired(SchedulerEvent anEvent) throws Exception {

      }

      @Override
      public void jobSucceeded(SchedulerEvent anEvent) {

      }

      @Override
      public void jobFailed(SchedulerEvent anEvent) {

      }
    });
  }

  /**
   * An executor of job. This class is defined in order to keep the way this scheduler
   * implementation works, whatever the interface changes are.
   */
  private static class JobExecutor {

    private Job job;

    /**
     * Constructs a now executor for the specified job.
     * @param jobToExecute the job to execute at trigger firing.
     */
    public JobExecutor(final Job jobToExecute) {
      this.job = jobToExecute;
    }

    /**
     * Executes the job. This method will be called by the internal mechanism of this scheduler
     * implemenation.
     * @param date the date at which the execution is triggered.
     * @throws Exception an execption if an error occurs during the job execution.
     */
    public void execute(final Date date) throws Exception {
      JobExecutionContext ctx = JobExecutionContext.createWith(this.job.getName(), date);
      this.job.execute(ctx);
    }
  }
}

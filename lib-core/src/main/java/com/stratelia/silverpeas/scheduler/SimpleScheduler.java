/**
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.scheduler;

import com.stratelia.silverpeas.scheduler.trigger.CronJobTrigger;
import com.stratelia.silverpeas.scheduler.trigger.FixedPeriodJobTrigger;
import com.stratelia.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple scheduler implementation.
 * It provides a easy way to schedule jobs at given moments in time.
 * The job execution policy is provided by a job trigger, represented as a <code>JobTrigger</code>
 * object; <code>JobTrigger</code> objects control when the job has to be executed in a repeatedly
 * way.
 * The execution of the job itself can be actually performed in two ways:
 * <ul>
 * <li>by a scheduling event listener through the reception of the event mapped with the
 * job execution triggering,</li>
 * <li>by a <code>Job</code> object that wraps the execution code.
 * </ul>
 */
public class SimpleScheduler {

  private static SimpleScheduler theSimpleScheduler;
  private final Map<String, SchedulerJob> jobs =
      new HashMap<String, SchedulerJob>();

  private static void initScheduler() {
    synchronized (SimpleScheduler.class) {
      if (theSimpleScheduler == null) {
        theSimpleScheduler = new SimpleScheduler();
      }
    }
  }

  /**
   * Is the job identified by the specified name is scheduled by this scheduler?
   * @param jobName the job name.
   * @return true if the job identified by the specified name is scheduled by this scheduler, false
   * otherwise.
   */
  public static boolean isJobScheduled(final String jobName) {
    return theSimpleScheduler.jobs.containsKey(jobName);
  }

  /**
   * Unschedules the job with the specified name.
   * If no job is scheduled under the specified name, nothing is done.
   * @param jobName the name of the job to unschedule.
   */
  public static void unscheduleJob(final String jobName) {
    if (theSimpleScheduler != null && theSimpleScheduler.jobs.containsKey(jobName)) {
      SchedulerJob job = theSimpleScheduler.jobs.get(jobName);
      theSimpleScheduler.removeJob(job);
    }
  }

  /**
   * This method kills all active jobs. The unique instance of the SimpleScheduler will be
   * destroyed.
   */
  public synchronized static void shutdown() {
    SilverTrace.debug("scheduler", "SimpleScheduler",
        "-------------------- SimpleScheduler shutdown --------------------",
        new Exception("ForStack"));
    if (theSimpleScheduler != null) {
      theSimpleScheduler.stopAllJobs();
      theSimpleScheduler = null;
    }
  }

  /**
   * The constructor is private because it will be created internal.
   */
  private SimpleScheduler() {
    this(null);
  }

  /**
   * The constructor is private because it will be created internal
   * @param aBasePath The path to the directory where the logfiles will be created
   */
  private SimpleScheduler(File aBasePath) {
    //htJobs = new HashMap<SchedulerEventHandler, List<SchedulerJob>>();
    SilverTrace.debug("scheduler", "SimpleScheduler",
        "-------------------- SimpleScheduler started --------------------");
  }

  /**
   * This method adds a job to the internal list of jobs an starts the job
   * @param aJobOwner A job owner
   * @param aNewJob A new job
   */
  private synchronized SchedulerJob addJob(SchedulerJob aNewJob) throws SchedulerException {
    if (jobs.containsKey(aNewJob.getJobName())) {
      throw new SchedulerException("An job is already scheduled under the name '" + 
          aNewJob.getJobName() + "'");
    }
    jobs.put(aNewJob.getJobName(), aNewJob);
    aNewJob.start();
    return aNewJob;
  }

  /**
   * This method removes a job
   * @param aJob A job object
   */
  private synchronized void removeJob(SchedulerJob aJob) {
    aJob.stopThread();
    theSimpleScheduler.jobs.remove(aJob.getJobName());
  }

  /**
   * This method stops all scheduling jobs
   */
  private synchronized void stopAllJobs() {
    for (SchedulerJob scheduledJob : jobs.values()) {
     scheduledJob.stopThread();
    }
    jobs.clear();
  }

  /**
   * Schedules a job under the specified name, that will be fired with the specified
   * trigger, and by setting the specified handler to recieve the events mapped with the job
   * execution state.
   * A scheduled job will be registered in the scheduler under the specified name and its execution
   * will be fired by the specified trigger. The computation of the job will be delegated to the
   * event handler at job exectution triggering.
   * If a job was already scheduled under the specified name, then a SchedulerException is thrown.
   * @param jobName the name under which the job should be registered in this scheduler.
   * @param trigger the trigger that will command the job execution in the timeline.
   * @param handler a scheduling event handler that will recieve the different events mapped with
   * the job execution state and that should compute the job.
   * @return a representation of the registered job.
   * @throws SchedulerException if either a job is already scheduled under the specified name or if
   * the job scheduling fails.
   */
  public static SchedulerJob scheduleJob(final String jobName,
      final JobTrigger trigger,
      final SchedulerEventHandler handler) throws SchedulerException {
    checkRequiredArgs(jobName, trigger, handler);
    initScheduler();
    if (trigger instanceof FixedPeriodJobTrigger) {
      FixedPeriodJobTrigger jobTrigger = (FixedPeriodJobTrigger) trigger;
      SchedulerEventJob newJob = new SchedulerEventJobMinute(theSimpleScheduler, handler,
          jobName, jobTrigger.getTimeInterval());
      return theSimpleScheduler.addJob(newJob);
    } else if (trigger instanceof CronJobTrigger) {
      CronJobTrigger cronJobTrigger = (CronJobTrigger) trigger;
      SchedulerEventJob newJob = new SchedulerEventJob(theSimpleScheduler, handler,
          jobName);
      newJob.setSchedulingParameter(cronJobTrigger.getCronExpression());
      return theSimpleScheduler.addJob(newJob);
    }
    throw new IllegalArgumentException("Trigger " + trigger.getClass().getName() +
        " not supported yet");
  }

  /**
   * Schedules the specified job. It will be fired with the specified trigger and the specified
   * event handler will recieve the events mapped with the job execution state.
   * If a job was already scheduled under the same name of the specified job, then a
   * SchedulerException is thrown.
   * @param theJob the job to schedule.
   * @param trigger the trigger that will fire the job execution.
   * @param handler a scheduling event handler that will recieve the different events mapped with
   * the job execution state.
   * @return a representation of the registered job.
   * @throws SchedulerException if either a job is already scheduled under the same name that
   * the specified job or if the job scheduling fails.
   */
  public static SchedulerJob scheduleJob(final Job theJob,
      final JobTrigger trigger,
      final SchedulerEventHandler handler) throws SchedulerException {
    if (theJob == null) {
      throw new IllegalArgumentException("The job is required!");
    }
    String jobName = theJob.getName();
    checkRequiredArgs(jobName, trigger, handler);
    initScheduler();
    if (trigger instanceof FixedPeriodJobTrigger) {
      FixedPeriodJobTrigger jobTrigger = (FixedPeriodJobTrigger) trigger;
      SchedulerMethodJob newJob = new SchedulerMethodJobMinute(theSimpleScheduler, handler,
          jobName, jobTrigger.getTimeInterval());
      newJob.setExecutionParameter(new JobExecutor(theJob), "execute");
      return theSimpleScheduler.addJob(newJob);
    } else if (trigger instanceof CronJobTrigger) {
      CronJobTrigger cronJobTrigger = (CronJobTrigger) trigger;
      SchedulerMethodJob newJob = new SchedulerMethodJob(theSimpleScheduler, handler, jobName);
      newJob.setSchedulingParameter(cronJobTrigger.getCronExpression());
      newJob.setExecutionParameter(new JobExecutor(theJob), "execute");
      return theSimpleScheduler.addJob(newJob);
    }
    throw new IllegalArgumentException("Trigger " + trigger.getClass().getName() +
        " not supported yet");
  }

  /**
   * Checks the required specified arguments are correctly set, otherwise throws an
   * IllegalArgumentException exception.
   * @param jobName a job name,
   * @param trigger a trigger of a job,
   * @param handler an scheduling event handler.
   */
  private static void checkRequiredArgs(final String jobName,
      final JobTrigger trigger,
      final SchedulerEventHandler handler) {
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

  /**
   * An executor of job.
   * This class is defined in order to keep the way this scheduler implementation works, whatever
   * the interface changes are.
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
     * Executes the job.
     * This method will be called by the internal mechanism of this scheduler implemenation.
     * @param date the date at which the execution is triggered.
     * @throws Exception an execption if an error occurs during the job execution.
     */
    public void execute(final Date date) throws Exception {
      JobExecutionContext ctx = new JobExecutionContext();
      ctx.setFireTime(date);
      this.job.execute(ctx);
    }
  }
}

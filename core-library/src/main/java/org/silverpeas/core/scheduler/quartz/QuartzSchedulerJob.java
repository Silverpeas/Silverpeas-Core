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
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.trigger.JobTrigger;

import java.io.Serializable;
import java.util.Date;

/**
 * The QuartzSchedulerJob is, as its name implies, a job that will be scheduled within the Quartz
 * scheduler. For each job to schedule by the Scheduler API, a corresponding QuartzSchedulerJob
 * instance is created and registered into the Quartz scheduler. This instance will wrap the actual
 * job, so that when the Quartz scheduler will fire it, it will delegate the job execution to the
 * wrapped job.
 */
public class QuartzSchedulerJob implements ScheduledJob, Serializable {

  private static final long serialVersionUID = 5310306615365508746L;

  private Job job;
  private JobTrigger trigger;
  private SchedulerEventListener listener;
  private long nextFireTime;

  /**
   * Constructs a new job for the Quartz scheduler.
   * @param name the name of the scheduled job.
   * @param trigger the trigger that will fire it.
   */
  protected QuartzSchedulerJob(final String name, final JobTrigger trigger) {
    this.job = new Job(name) {

      @Override
      public void execute(JobExecutionContext context) {
      }
    };
    this.trigger = trigger;
  }

  /**
   * Constructs a new job for the Quartz scheduler.
   * @param jobToExecute the job to execute at trigger firing.
   * @param trigger the trigger that will fire it.
   */
  protected QuartzSchedulerJob(final Job jobToExecute, final JobTrigger trigger) {
    this.job = jobToExecute;
    this.trigger = trigger;
  }

  /**
   * Specifies a scheduler event listener that will recieve all of the events concerning this job.
   * @param listener a scheduler event listener.
   * @return itself.
   */
  protected QuartzSchedulerJob withSchedulerEventListener(final SchedulerEventListener listener) {
    this.listener = listener;
    return this;
  }

  @Override
  public String getName() {
    return this.job.getName();
  }

  @Override
  public JobTrigger getTrigger() {
    return this.trigger;
  }

  @Override
  public SchedulerEventListener getSchedulerEventListener() {
    return this.listener;
  }

  @Override
  public void execute(JobExecutionContext context) throws Exception {
    this.job.execute(context);
  }

  @Override
  public Date getNextExecutionTime() {
    return new Date(this.nextFireTime);
  }

  @Override
  public long getNexExecutionTimeInMillis() {
    return this.nextFireTime;
  }

  /**
   * Sets the next time at which this job will be executed.
   * @param executionTime the next job execution time.
   */
  protected void setNextExecutionTime(final Date executionTime) {
    this.nextFireTime = executionTime.getTime();
  }

}

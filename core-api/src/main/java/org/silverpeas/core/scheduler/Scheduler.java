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

package org.silverpeas.core.scheduler;

import org.silverpeas.core.scheduler.trigger.JobTrigger;

/**
 * It is the main interface of a Silverpeas scheduler. It provides the features needed by Silverpeas
 * components to schedule some jobs at given moments in time or at regular time. A Scheduler object
 * should maintain a registry of Job and Trigger instances. Once registered, the scheduler is
 * responsible for executing the jobs when their associated trigger fires (when their scheduled time
 * arrives). A job can be executed in a two ways: by an event listener at trigger firing event
 * reception or by a Job object carrying the operation to run. A listener of scheduler's events can
 * be specified at job registering to handle, for example, the status of the job execution or to
 * perform some non-business operations out of the job. Scheduler instances are produced by a
 * SchedulerFactory that encapsulates the actual backend used to implement the scheduling system.
 * Whatever the instances returned by the factory, they share a single entry point to the scheduling
 * backend so that a job scheduled with an instance can be found with another one.
 */
public interface Scheduler {

  /**
   * Schedules a job under the specified name, that will be fired with the specified trigger, and by
   * setting the specified listener to recieve the events mapped with the job execution state. A
   * scheduled job will be registered in the scheduler under the specified name and its execution
   * will be fired by the specified trigger. The computation of the job will be delegated to the
   * event listener when the associated trigger fires (it will recieve the event mapped with this
   * trigger firing). If a job is already scheduled under the specified name, then a
   * SchedulerException exception is thrown.
   * @param jobName the name under which the job should be registered in this scheduler.
   * @param trigger the trigger that will command the job execution in the timeline.
   * @param listener a listener of scheduler's events. It will recieve the different events fired by
   * the scheduler and mapped with the job execution state. It is expected it will accomplish the
   * job itself at trigger firing. It is required.
   * @return the job scheduled in the scheduler.
   * @throws SchedulerException if either a job is already scheduled under the specified name or if
   * the job scheduling fails.
   */
  ScheduledJob scheduleJob(final String jobName,
      final JobTrigger trigger,
      final SchedulerEventListener listener) throws SchedulerException;

  /**
   * Schedules the specified job. It will be fired with the specified trigger and the specified
   * event listener will recieve the events mapped with the state of the job execution. The
   * specified job will be registered as a scheduled job in the scheduler under its name and its
   * execution will be fired by the specified trigger. In the case an event listener is specified,
   * it will recieve the events mapped with the state of the job execution. If a job is already
   * scheduled under the same name of the specified job, then a SchedulerException exception is
   * thrown.
   * @param theJob the job to schedule.
   * @param trigger the trigger that will fire the job execution.
   * @param listener a listener of scheduler's events mapped with the state of the job execution.
   * Null means no listener to register with the job.
   * @return the job scheduled in the scheduler.
   * @throws SchedulerException if either a job is already scheduled under the same name that the
   * specified job or if the job scheduling fails.
   */
  ScheduledJob scheduleJob(final Job theJob,
      final JobTrigger trigger,
      final SchedulerEventListener listener) throws SchedulerException;

  /**
   * Schedules the specified job. It will be fired with the specified trigger. The specified job
   * will be registered as a scheduled job in the scheduler under its name and its execution will be
   * fired by the specified trigger. If a job is already scheduled under the same name of the
   * specified job, then a SchedulerException exception is thrown.
   * @param theJob the job to schedule.
   * @param trigger the trigger that will fire the job execution.
   * @return the job scheduled in the scheduler.
   * @throws SchedulerException if either a job is already scheduled under the same name that the
   * specified job or if the job scheduling fails.
   */
  ScheduledJob scheduleJob(final Job theJob, final JobTrigger trigger) throws SchedulerException;

  /**
   * Unschedules the job with the specified name. If no job is scheduled under the specified name,
   * nothing is done.
   * @param jobName the name of the job to unschedule.
   * @throws SchedulerException if the specified job cannot be unscheduled.
   */
  void unscheduleJob(final String jobName) throws SchedulerException;

  /**
   * Is the job identified by the specified name is scheduled by this scheduler?
   * @param jobName the job name.
   * @return true if the job identified by the specified name is scheduled by this scheduler, false
   * otherwise.
   */
  boolean isJobScheduled(final String jobName);

  /**
   * Shutdowns this scheduler. The firing of triggers are halted and the jobs removed. All of the
   * resources are cleaned up. Once shutdown done, the scheduler cannot be restarted and used for
   * scheduling new jobs.
   * @throws SchedulerException if the scheduler shutdown failed.
   */
  void shutdown() throws SchedulerException;
}

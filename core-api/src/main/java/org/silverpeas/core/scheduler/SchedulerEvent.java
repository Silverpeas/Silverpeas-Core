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

/**
 * A scheduler event represents an event that is generated within the scheduling system and that is
 * dispatched by schedulers to registered event listeners. A scheduler event is generated each time
 * a state change occurs in the scheduling system machinery and the event carries information about
 * this state change. A state change occurs in the scheduling system in the following situation:
 * <ul>
 * <li>A trigger fires the execution of a job: the scheduling system then runs the job within a
 * dedicated thread;</li>
 * <li>A job terminates abnormally: the job execution throws an exception that is catched by the
 * scheduling system; the failover handler is running.</li>
 * <li>A job terminates correctly: the job termination handler is running.</li>
 * </ul>
 */
public class SchedulerEvent {

  /**
   * The different possible types of an event send by the scheduler.
   */
  public enum Type {
        /**
     * A trigger has fired.
     */
    TRIGGER_FIRED,
        /**
     * The execution of a job has succeeded.
     */
    JOB_SUCCEEDED,
        /**
     * The execution of a job has failed.
     */
    JOB_FAILED;
  }

  private JobExecutionContext ctx;
  private Throwable exception = null;
  private Type type;

  /**
   * Creates a new scheduler event about a trigger firing.
   * @param the context of the job to be executed.
   * @return the scheduler event fired by the firing of a trigger.
   */
  public static SchedulerEvent triggerFired(final JobExecutionContext context) {
    return new SchedulerEvent(Type.TRIGGER_FIRED, context);
  }

  /**
   * Creates a new scheduler event about a success of a job execution.
   * @param the context of the completed execution of a job.
   * @return the scheduler event fired by the job completion.
   */
  public static SchedulerEvent jobSucceeded(final JobExecutionContext context) {
    return new SchedulerEvent(Type.JOB_SUCCEEDED, context);
  }

  /**
   * Creates a new scheduler event about a failure of a job execution.
   * @param context the context of the failed execution of a job.
   * @param throwable the Throwable object carrying the job failure.
   * @return the scheduler event fired by a job failure.
   */
  public static SchedulerEvent jobFailed(final JobExecutionContext context,
      final Throwable throwable) {
    SchedulerEvent event = new SchedulerEvent(Type.JOB_FAILED, context);
    event.setThrowable(throwable);
    return event;
  }

  /**
   * This method returns the event type
   * @return The type of the event
   */
  public Type getType() {
    return type;
  }

  /**
   * Is an exception thrown during a job execution?
   * @return true if an exception was thrown during the execution of a job, false otherwise.
   */
  public boolean isExceptionThrown() {
    return this.exception != null;
  }

  /**
   * Gets the throwable object that was thrown during a job execution and that carries the job
   * failure.
   * @return the throwable raised during a job execution or null if the job completed successfully.
   */
  public Throwable getJobThrowable() {
    return this.exception;
  }

  /**
   * Gets the context of a job execution.
   * @return a JobExecutionContext instance as it is or it will passed to a job execution.
   */
  public JobExecutionContext getJobExecutionContext() {
    return this.ctx;
  }

  /**
   * Constructs a scheduler event.
   * @param aType The type of the event
   * @param aJob The job, which is the source for the event
   */
  protected SchedulerEvent(final Type aType, final JobExecutionContext aContext) {
    this.type = aType;
    this.ctx = aContext;
  }

  protected void setThrowable(final Throwable aThrowable) {
    this.exception = aThrowable;
  }
}

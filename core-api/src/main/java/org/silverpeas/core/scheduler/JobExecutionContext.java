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

import java.util.Date;

/**
 * The context of a job execution. A such object embeds information about the execution context of a
 * job such as some execution parameters or the trigger from which the execution was fired.
 */
public class JobExecutionContext {

  private Date date;
  private String jobName;

  /**
   * Creates a new context for the execution of the specified job and fired at the specified time.
   * @param jobName the name of the job that is being executed.
   * @param fireTime the time at which the job has been starting.
   * @return the execution context of the job.
   */
  public static JobExecutionContext createWith(final String jobName, final Date fireTime) {
    JobExecutionContext context = new JobExecutionContext();
    return context.jobNamed(jobName).jobFiredAt(fireTime);
  }

  /**
   * Gets the actual time at which the trigger fired.
   * @return the triggering time.
   */
  public Date getFireTime() {
    return date;
  }

  /**
   * Sets the actual time at which the trigger fired.
   * @param date the triggering time.
   * @return itself.
   */
  public JobExecutionContext jobFiredAt(final Date date) {
    this.date = date;
    return this;
  }

  /**
   * Gets the name of the job that takes part in the job execution .
   * @return the name of the executed job.
   */
  public String getJobName() {
    return this.jobName;
  }

  /**
   * Sets the name of the job that takes part in the job execution.
   * @param theJobName the name of the executed job.
   * @return itself.
   */
  public JobExecutionContext jobNamed(final String theJobName) {
    this.jobName = theJobName;
    return this;
  }

  /**
   * Constructs a new context of a job execution.
   */
  private JobExecutionContext() {

  }
}

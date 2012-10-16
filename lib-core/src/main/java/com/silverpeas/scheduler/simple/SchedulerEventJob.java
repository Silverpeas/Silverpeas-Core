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

package com.silverpeas.scheduler.simple;

import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerException;
import java.util.Date;

/**
 * This class extends the class 'SchedulerJob' for the functionality of the scheduled execution of
 * shell scripts.
 */
@Deprecated
public class SchedulerEventJob extends SchedulerJob {
  /**
   * The constructor has proteceted access, because the generation of jobs should be done in a
   * central way by the class 'SimpleScheduler'
   * @param aController The controller, that controls all job executions
   * @param aOwner The owner of the job
   * @param aJobName The name of the job
   */
  protected SchedulerEventJob(SimpleScheduler theJobController,
      SchedulerEventListener theJobOwner, String theJobName)
      throws SchedulerException {
    super(theJobController, theJobOwner, theJobName);
  }

  /**
   * This method implements the abstract method of the base class. It creates a new SchedulerEvent
   * and sends it to the job owner.
   * @param theExecutionDate The date of the execution
   */
  @Override
  protected void execute(Date theExecutionDate) throws SchedulerException {
    try {
      JobExecutionContext ctx = JobExecutionContext.createWith(getName(), theExecutionDate);
      getOwner().triggerFired(SchedulerEvent.triggerFired(ctx));
    } catch (Exception aException) {
      throw new SchedulerException(
          "SchedulerShellJob.execute: Execution failed (Reason: "
          + aException.getMessage() + ")");
    }
  }
}

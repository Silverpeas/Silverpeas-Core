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

package com.stratelia.silverpeas.scheduler.trigger;

import com.stratelia.silverpeas.scheduler.TimeUnit;
import java.util.Date;

/**
 * A trigger of a job execution.
 * A job trigger specifies the time, regular or not, at which a job must be executed.
 */
public abstract class JobTrigger {
  
  private Date startDate;
  
  /**
   * Gets the date at which the trigger's scheduling should start.
   * It may or not be the first actual fire time of the trigger according the type of this trigger.
   * @return the job trigger's scheduling start date.
   */
  public Date getStartDate() {
    return startDate;
  }
 
  /**
   * Creates a job trigger that will fire a job execution every the specified time.
   * @return a job trigger whose the triggering is scheduled periodically at a given time.
   */
  public static JobTrigger triggerEvery(int time, final TimeUnit unit) {
    JobTriggerFactory triggerFactory = new JobTriggerFactory();
    return triggerFactory.createWithAsPeriodicity(time, unit);
  }
  
  /**
   * Creates a job trigger that will fire a job execution at given moments in time, defined by the
   * specified cron expression.
   * For more information on the syntax of the cron expression, please see the CronJobTrigger
   * documentation.
   * @see CronJobTrigger
   * @param cron the Unix cron-like expression.
   * @return a job trigger whose the triggering is scheduled at given moments in time.
   */
  public static JobTrigger triggerAt(final String cron) {
    JobTriggerFactory triggerFactory = new JobTriggerFactory();
    return triggerFactory.createWithAsCronExpression(cron);
  }
  
  /**
   * Sets the date at which the trigger's scheduling should start.
   * May or may not be the first actual fire time of the trigger. However the first actual first
   * time will not be before this date. 
   * @param startDate the job trigger's scheduling start date.
   */
  public JobTrigger startAt(final Date startDate) {
    if (startDate == null || startDate.before(new Date())) {
      throw new IllegalArgumentException("Either the start date of the job is null or it is"
          + " anterior at now!");
    }
    this.startDate = startDate;
    return this;
  }

}

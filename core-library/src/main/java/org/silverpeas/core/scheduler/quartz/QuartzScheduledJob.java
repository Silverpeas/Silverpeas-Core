/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.quartz.Trigger;
import org.silverpeas.core.scheduler.ScheduledJob;

import java.util.Date;

/**
 * This class represents a job that is scheduled in a Quartz scheduler. For each job to
 * schedule by the Scheduler Engine, a corresponding {@link QuartzScheduledJob}
 * instance is created with the trigger that is actually used by the Quartz scheduler to schedule
 * the job. In Quartz, a trigger is always related to the scheduled job and a scheduled job can
 * have one or more triggers in Quartz.
 * <p>
 * A {@link QuartzScheduledJob} instance isn't serializable meaning it is lost at platform shutdown.
 * Please, use the
 * </p>
 */
public class QuartzScheduledJob implements ScheduledJob {

  private final Trigger trigger;

  /**
   * Constructs a new job that is scheduled in a Quartz scheduler.
   * @param trigger the Quartz trigger related to the scheduled job in the Quartz scheduler.
   */
  protected QuartzScheduledJob(final Trigger trigger) {
    this.trigger = trigger;
  }

  @Override
  public String getName() {
    return this.trigger.getJobKey().getName();
  }

  @Override
  public Date getNextExecutionTime() {
    return this.trigger.getNextFireTime();
  }

}

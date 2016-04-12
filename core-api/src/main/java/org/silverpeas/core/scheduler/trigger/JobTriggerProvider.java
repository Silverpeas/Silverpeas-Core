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

package org.silverpeas.core.scheduler.trigger;

import java.text.ParseException;

/**
 * A factory of job triggers. This factory creates the specific job triggers depending upons the
 * scheduling parameters.
 */
public class JobTriggerProvider {

  /**
   * Constructs a new job trigger factory.
   */
  private JobTriggerProvider() {
  }

  /**
   * Creates a new job trigger that will fire a job execution at the specified regular time.
   * @param time the time between each job triggering.
   * @param unit the unit in which the time is expressed.
   * @return a job trigger whose type depends upon the specified periodicity.
   */
  public static FixedPeriodJobTrigger getJobTriggerWithPeriodicity(int time, final TimeUnit unit) {
    FixedPeriodJobTrigger trigger = new FixedPeriodJobTrigger(time, unit);
    return trigger;
  }

  /**
   * Creates a new job trigger that will fire a job exectution at given moments in the time, as
   * defined by the specified cron expression.
   * @param cron the Unix cron-like expression.
   * @see CronJobTrigger
   * @return a CronJobTrigger instance.
   * @throws ParseException if the specified cron expression is not valid.
   */
  public static CronJobTrigger getJobTriggerWithCronExpression(final String cron) throws ParseException {
    CronJobTrigger trigger = new CronJobTrigger(cron);
    return trigger;
  }

}

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
package com.silverpeas.scheduler.trigger;

import java.text.ParseException;
import org.quartz.CronExpression;

/**
 * This job trigger fires a job execution at given moments in time, defined with a Unix cron-like
 * definition.
 *
 * <p>The format of the expected cron expression is as following:
 * <code>MINUTES HOURS DAY_OF_MONTH MONTH DAY_OF_WEEK</code> with</p>
 * <table cellspacing="8">
 *  <tr>
 *    <th align="left">Field Name</th>
 *    <th align="left">Allowed Values</th>
 *    <th align="left">Allowed Special Characters</th>
 *  </tr>
 *  <tr>
 *    <td align="left"><code>MINUTES</code></td>
 *    <td align="left"><code>0-59</code></td>
 *    <td align="left"><code>, - * /</code></td>
 *  </tr>
 *  <tr>
 *    <td align="left"><code>HOURS</code></td>
 *    <td align="left"><code>0-23</code></td>
 *    <td align="left"><code>, - * /</code></td>
 *  </tr>
 *  <tr>
 *    <td align="left"><code>DAY_OF_MONTH</code></td>
 *    <td align="left"><code>1-31</code></td>
 *    <td align="left"><code>, - * ? /</code></td>
 *  </tr>
 *  <tr>
 *    <td align="left"><code>MONTH</code></td>
 *    <td align="left"><code>1-12 or JAN-DEC</code></td>
 *    <td align="left"><code>, - * /</code></td>
 *  </tr>
 *  <tr>
 *    <td align="left"><code>DAY_OF_WEEK</code></td>
 *    <td align="left"><code>1-7 or SUN-SAT</code></td>
 *    <td align="left"><code>, - * ? /</code></td>
 *  </tr>
 * </table>
 * <p>The '*' character is used to specify all values. For example, "*" in the minute field means
 * "every minute".</p>
 * <p>The '?' character is allowed for the DAY_OF_MONTH or DAY_OF_WEEK fields. It is used to specify
 * 'no specific value'. This is useful when you need to specify something in one of the two
 * fields as currently specifying both the two fields is not supported.</p>
 * <p>The '-' character is used to specify ranges. For example "10-12" in the hour field means
 * "the hours 10, 11 and 12".</p>
 * <p>The ',' character is used to specify additional values. For example "MON,WED,FRI" in the
 * DAY_OF_WEEK field means "the days Monday, Wednesday, and Friday".</p>
 * <p>The '/' character is used to specify increments. For example "0/15" in the seconds field means
 * "the seconds 0, 15, 30, and 45". And "5/15" in the seconds field means "the seconds 5, 20, 35,
 * and 50". Specifying '*' before the '/' is equivalent to specifying 0 is the value to start with.
 * Essentially, for each field in the expression, there is a set of numbers that can be turned on or
 * off. For seconds and minutes, the numbers range from 0 to 59. For hours 0 to 23, for days of the
 * month 0 to 31, and for months 1 to 12. The "/" character simply helps you turn on every "nth"
 * value in the given set. Thus "7/6" in the month field only turns on month "7", it does NOT mean
 * every 6th month, please note that subtlety.</p>
 * <p>Be careful when setting fire times between mid-night and 1:00 AM - "daylight savings" can
 * cause a skip or a repeat depending on whether the time moves back or jumps forward.</p>
 */
public final class CronJobTrigger extends JobTrigger {

  private static final String CRON_SYNTAX_TO_FIX =
      "[0-9,/\\-\\*]+[ ]+[0-9,/\\-\\*]+[ ]+\\*[ ]+[0-9,/\\-\\*]+[ ]+\\*";
  private String cron;

  /**
   * Gets the cron expression that drives the triggering policy of this trigger.
   * @return the cron expression as String.
   */
  public String getCronExpression() {
    return cron;
  }

  /**
   * Creates a new job trigger that will fire a job execution at given moments in time as defined
   * by the specified Unix cron-like expression.
   * @param cronExpression the Unix-like cron expression.
   * @throws ParseException if the cron expression is not valid.
   */
  protected CronJobTrigger(final String cronExpression) throws ParseException {
    // if the day-of-week and the day-of-month are set with '*', replace one by '?'.
    // It is a workaround of the 'every' support for  both day-of-month and day-of-week parameters
    // as usualy that means whatever for the user (they are not set explicitly).
    String trimedCronExpression = cronExpression.trim();
    if (trimedCronExpression.matches(CRON_SYNTAX_TO_FIX)) {
      this.cron = trimedCronExpression.substring(0, trimedCronExpression.length() - 1) + "?";
    } else {
      this.cron = trimedCronExpression;
    }
    CronExpression exp = new CronExpression("* " + this.cron);
  }

  @Override
  public void accept(JobTriggerVisitor visitor) {
    visitor.visit(this);
  }
}

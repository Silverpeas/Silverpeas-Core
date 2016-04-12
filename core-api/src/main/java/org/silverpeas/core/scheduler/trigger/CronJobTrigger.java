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

import org.silverpeas.core.util.ServiceProvider;

import java.text.ParseException;

/**
 * This job trigger fires a job execution at given moments in time, defined with a Unix cron-like
 * definition.
 * <p>
 * The format of the expected cron expression is as following:
 * <code>MINUTES HOURS DAY_OF_MONTH MONTH DAY_OF_WEEK</code> with
 * </p>
 * <table cellspacing="8">
 * <tr>
 * <th align="left">Field Name</th>
 * <th align="left">Allowed Values</th>
 * <th align="left">Allowed Special Characters</th>
 * </tr>
 * <tr>
 * <td align="left"><code>MINUTES</code></td>
 * <td align="left"><code>0-59</code></td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>HOURS</code></td>
 * <td align="left"><code>0-23</code></td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>DAY_OF_MONTH</code></td>
 * <td align="left"><code>1-31</code></td>
 * <td align="left"><code>, - * ? /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>MONTH</code></td>
 * <td align="left"><code>1-12 or JAN-DEC</code></td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>DAY_OF_WEEK</code></td>
 * <td align="left"><code>1-7 or SUN-SAT</code></td>
 * <td align="left"><code>, - * ? /</code></td>
 * </tr>
 * </table>
 * <p>
 * The '*' character is used to specify all values. For example, "*" in the minute field means
 * "every minute".
 * </p>
 * <p>
 * The '?' character is allowed for the DAY_OF_MONTH or DAY_OF_WEEK fields. It is used to specify
 * 'no specific value'. This is useful when you need to specify something in one of the two fields
 * as currently specifying both the two fields is not supported.
 * </p>
 * <p>
 * The '-' character is used to specify ranges. For example "10-12" in the hour field means
 * "the hours 10, 11 and 12".
 * </p>
 * <p>
 * The ',' character is used to specify additional values. For example "MON,WED,FRI" in the
 * DAY_OF_WEEK field means "the days Monday, Wednesday, and Friday".
 * </p>
 * <p>
 * The '/' character is used to specify increments. For example "0/15" in the seconds field means
 * "the seconds 0, 15, 30, and 45". And "5/15" in the seconds field means "the seconds 5, 20, 35,
 * and 50". Specifying '*' before the '/' is equivalent to specifying 0 is the value to start with.
 * Essentially, for each field in the expression, there is a set of numbers that can be turned on or
 * off. For seconds and minutes, the numbers range from 0 to 59. For hours 0 to 23, for days of the
 * month 0 to 31, and for months 1 to 12. The "/" character simply helps you turn on every "nth"
 * value in the given set. Thus "7/6" in the month field only turns on month "7", it does NOT mean
 * every 6th month, please note that subtlety.
 * </p>
 * <p>
 * Be careful when setting fire times between mid-night and 1:00 AM - "daylight savings" can cause a
 * skip or a repeat depending on whether the time moves back or jumps forward.
 * </p>
 */
public final class CronJobTrigger extends JobTrigger {

  private static final String CRON_SYNTAX_TO_FIX =
      "[0-9,/\\-\\*]+[ ]+[0-9,/\\-\\*]+[ ]+\\*[ ]+[0-9,/\\-\\*]+[ ]+\\*";
  private CronExpression cron;

  /**
   * Gets the cron expression that drives the triggering policy of this trigger.
   * @return the cron expression as String.
   */
  public CronExpression getCronExpression() {
    return cron;
  }

  /**
   * Creates a new job trigger that will fire a job execution at given moments in time as defined by
   * the specified Unix cron-like expression.
   * @param cronExpression the Unix-like cron expression.
   * @throws ParseException if the cron expression is not valid.
   */
  protected CronJobTrigger(final String cronExpression) throws ParseException {
    // if the day-of-week and the day-of-month are set with '*', replace one by '?'.
    // It is a workaround of the 'every' support for both day-of-month and day-of-week parameters
    // as usualy that means whatever for the user (they are not set explicitly).
    String[] cronExpressionParts = cronExpression.trim().split("[ ]+");
    if (cronExpressionParts.length != 5) {
      throw new ParseException("Malformed cron expression '" + cronExpression +
          "'. The cron expression must contain 5 space-separated statements: " +
          "MINUTES HOURS DAY_OF_MONTH MONTH DAY_OF_WEEK", cronExpression.length());
    }
    if (cronExpressionParts[2].equals("*") && cronExpressionParts[4].matches("[0-9,/\\-,\\*]+")) {
      cronExpressionParts[2] = "?";
    } else if (cronExpressionParts[4].equals("*") && cronExpressionParts[2].matches("[0-9,/\\-]+")) {
      cronExpressionParts[4] = "?";
    }

    CronExpressionFactory factory = ServiceProvider.getService(CronExpressionFactory.class);
    this.cron = factory.create(
        cronExpressionParts[0] + " " + cronExpressionParts[1] + " " + cronExpressionParts[2] + " " +
            cronExpressionParts[3] + " " + cronExpressionParts[4]);
  }

  @Override
  public void accept(JobTriggerVisitor visitor) {
    visitor.visit(this);
  }
}

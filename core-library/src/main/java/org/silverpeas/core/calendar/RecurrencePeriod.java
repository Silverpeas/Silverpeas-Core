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

package org.silverpeas.core.calendar;

/**
 * A period of a recurrence. It defines the unit of time the recurrence is scheduled and the
 * interval in this unit of time.
 */
public class RecurrencePeriod {

  private int interval;
  private TimeUnit timeUnit;

  public static RecurrencePeriod every(int interval, TimeUnit unit) {
    return new RecurrencePeriod(interval, unit);
  }

  /**
   * Gets the interval of this period.
   * @return the period interval.
   */
  public int getInterval() {
    return interval;
  }

  /**
   * Gets the unit of time of the interval.
   * @return the unit of time on which the period is defined.
   */
  public TimeUnit getUnit() {
    return timeUnit;
  }

  private RecurrencePeriod(int every, TimeUnit unit) {
    this.interval = every;
    this.timeUnit = unit;
  }

}

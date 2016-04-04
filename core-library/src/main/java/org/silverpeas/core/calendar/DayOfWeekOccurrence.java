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
 * The day of week occurrence represents an nth occurrence of the day in a week, a month or in a
 * year. Such objects are mainly used to represent a rule in an event recurrence.
 */
public class DayOfWeekOccurrence {

  /**
   * A constant that defines a specific value for all the occurrences of the represented day in a
   * week, a month or a year.
   */
  public static final int ALL_OCCURRENCES = 0;

  private int nth;
  private DayOfWeek dayOfWeek;

  /**
   * Creates an instance of DayOfWeekOccurrence representing the nth occurrence of the specified day
   * of week in a month or a year.
   * @param nth the nth occurrence of the specified day of week. It accepts negative numbers. A
   * positive value means the nth occurrence of the day of week encountered in the month or in the
   * year, whereas a negative value means the nth occurrence back from the end of the month or of
   * the year.
   * @param dayOfWeek the day of week.
   * @return a DayOfWeekOccurrence instance.
   */
  public static DayOfWeekOccurrence nthOccurrence(int nth, final DayOfWeek dayOfWeek) {
    return new DayOfWeekOccurrence(nth, dayOfWeek);
  }

  /**
   * Creates an instance of DayOfWeekOccurrence representing all the occurrences of the specified
   * day of week in a week, a month or a year.
   * @param dayOfWeek the day of week.
   * @return a DayOfWeekOccurrence instance.
   */
  public static DayOfWeekOccurrence allOccurrences(final DayOfWeek dayOfWeek) {
    return new DayOfWeekOccurrence(ALL_OCCURRENCES, dayOfWeek);
  }

  /**
   * Gets the day of week of this occurrence.
   * @return the day of week.
   */
  public DayOfWeek dayOfWeek() {
    return dayOfWeek;
  }

  /**
   * Gets the nth this occurrence is.
   * @return the nth occurrence or ALL_OCCURRENCES if all of the underlying day of week are
   * considered.
   */
  public int nth() {
    return nth;
  }

  private DayOfWeekOccurrence(int nth, final DayOfWeek dayOfWeek) {
    if (nth < 0) {
      throw new IllegalArgumentException("The nth occurrence must be a positive value!");
    }
    if (dayOfWeek == null) {
      throw new IllegalArgumentException("The day of week must be indicated!");
    }
    this.nth = nth;
    this.dayOfWeek = dayOfWeek;
  }
}

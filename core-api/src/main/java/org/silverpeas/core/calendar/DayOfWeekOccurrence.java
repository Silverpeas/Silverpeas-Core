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
package org.silverpeas.core.calendar;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;

/**
 * The occurrence of a day of week represents the nth occurrence of a day of week in a month or
 * in a year within a recurrence rule of a {@link Plannable} object. For example, the third tuesday
 * in the month.
 */
@Embeddable
public class DayOfWeekOccurrence {

  /**
   * A constant that defines a specific value for all the occurrences of the represented day of
   * week in a week, a month or a year.
   */
  public static final int ALL_OCCURRENCES = 0;

  /**
   * A constant that defines a specific value for the last day according to the recurrence rule.
   * For example, for a monthly recurrence the last monday of each month.
   */
  public static final int LAST_DAY = -1;

  @Column(name = "recur_nth", nullable = false)
  @NotNull
  private int nth;
  @Column(name = "recur_dayOfWeek", nullable = false)
  @Enumerated(EnumType.ORDINAL)
  @NotNull
  private DayOfWeek dayOfWeek;

  protected DayOfWeekOccurrence() {
  }

  private DayOfWeekOccurrence(int nth, final DayOfWeek dayOfWeek) {
    if (nth < LAST_DAY) {
      throw new IllegalArgumentException(
          "The nth occurrence must be an integer greater than or equal to LAST_DAY constant value");
    }
    if (dayOfWeek == null) {
      throw new IllegalArgumentException("The day of week must be indicated!");
    }
    this.nth = nth;
    this.dayOfWeek = dayOfWeek;
  }

  /**
   * Creates an instance of DayOfWeekOccurrence representing the nth occurrence of the specified day
   * of week in a month or in a year.
   * @param nth a positive number indicating the nth occurrence of the specified day of week,
   * id est the nth occurrence of the day of week encountered in the month or in the year.
   * @param dayOfWeek the day of week.
   * @return a DayOfWeekOccurrence instance.
   */
  public static DayOfWeekOccurrence nth(int nth, final DayOfWeek dayOfWeek) {
    return new DayOfWeekOccurrence(nth, dayOfWeek);
  }

  /**
   * Creates an instance of DayOfWeekOccurrence representing all the occurrences of the specified
   * day of week in a week, in a month or in a year.
   * @param dayOfWeek the day of week.
   * @return a DayOfWeekOccurrence instance.
   */
  public static DayOfWeekOccurrence all(final DayOfWeek dayOfWeek) {
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DayOfWeekOccurrence)) {
      return false;
    }

    final DayOfWeekOccurrence that = (DayOfWeekOccurrence) o;

    if (nth != that.nth) {
      return false;
    }
    return dayOfWeek == that.dayOfWeek;

  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(nth).append(dayOfWeek).toHashCode();
  }

}

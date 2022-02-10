/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.core.date.TimeUnit;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A period of a recurrence. It defines the recurrence of a {@link Plannable} object in a calendar
 * as a regular interval in a given unit of time. For example, in <i>every 2 weeks</i>, week is the
 * unit * of time whereas 2 is the interval in this unit of time. The unit of time of the recurrence
 * period cannot be less that the day.
 */
@Embeddable
public class RecurrencePeriod implements Serializable {

  @Column(name = "recur_periodInterval", nullable = false)
  @NotNull
  private int interval;
  @Column(name = "recur_periodUnit", nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull
  private TimeUnit timeUnit;

  /**
   * Constructs an empty recurrence period for the persistence engine.
   */
  protected RecurrencePeriod() {
    // empty for JPA.
  }

  private RecurrencePeriod(int every, TimeUnit unit) {
    this.interval = every;
    this.timeUnit = unit;
  }

  /**
   * Creates a recurrence period from the specified frequency statement that is expressed by a
   * regular interval in a given unit of time. For example, <i>every 3 months</i>.
   * @param interval the regular interval of the period in the specified unit of time.
   * @param unit an unit of time. It doesn't must be lesser than the day, otherwise an
   * {@link IllegalArgumentException} is thrown.
   * @return a recurrence period matching specified the frequency statement.
   */
  public static RecurrencePeriod every(int interval, TimeUnit unit) {
    if (unit.ordinal() < TimeUnit.DAY.ordinal()) {
      throw new IllegalArgumentException(
          "The recurrence of an object planned in a calendar cannot be less than the day");
    }
    if (interval <= 0) {
      throw new IllegalArgumentException(
          "The recurrence of an object planned in a calendar cannot be less than every 1 of a " +
              "given unit of time");
    }
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

  /**
   * Is the frequency is hourly?
   * @return true if the recurrence period is on a per-hours basis. False otherwise.
   */
  public boolean isHourly() {
    return timeUnit == TimeUnit.HOUR;
  }

  /**
   * Is the frequency is daily?
   * @return true if the recurrence period is on a per-days basis. False otherwise.
   */
  public boolean isDaily() {
    return timeUnit == TimeUnit.DAY;
  }

  /**
   * Is the frequency is weekly?
   * @return true if the recurrence period is on a per-weeks basis. False otherwise.
   */
  public boolean isWeekly() {
    return timeUnit == TimeUnit.WEEK;
  }

  /**
   * Is the frequency is monthly?
   * @return true if the recurrence period is on a per-months basis. False otherwise.
   */
  public boolean isMonthly() {
    return timeUnit == TimeUnit.MONTH;
  }

  /**
   * Is the frequency is yearly?
   * @return true if the recurrence period is on a per-years basis. False otherwise.
   */
  public boolean isYearly() {
    return timeUnit == TimeUnit.YEAR;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RecurrencePeriod)) {
      return false;
    }

    final RecurrencePeriod that = (RecurrencePeriod) o;

    if (interval != that.interval) {
      return false;
    }
    return timeUnit == that.timeUnit;

  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(interval).append(timeUnit).toHashCode();
  }

}

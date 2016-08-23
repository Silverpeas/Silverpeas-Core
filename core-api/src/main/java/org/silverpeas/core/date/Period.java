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
package org.silverpeas.core.date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * A period is a laps of time starting at a given date or date time and ending at a given
 * date or date time. When the period takes care of the time, it is always set in
 * UTC/Greenwich in order to avoid any bugs by comparing two periods in different time zones
 * or offset zones.
 * @author mmoquillon
 */
@Embeddable
public class Period {

  @Column(name = "startDate", nullable = false)
  private OffsetDateTime startDateTime;
  @Column(name = "endDate", nullable = false)
  private OffsetDateTime endDateTime;
  @Column(name = "inDays", nullable = false)
  private boolean inDays = false;

  /**
   * Creates a new period of time between the two specified dates. The period is spanning all the
   * day(s) between the specified start day and end day; the period is expressed in days.
   * @param startDay the start day of the period. It defines the inclusive date at which the
   * period starts.
   * @param endDay the end day of the period. It defines the inclusive date at which the period
   * ends. The end date must be the same or after the start date. An end date equal to the start
   * date means the period is spanning all the day.
   * @return the period of days between the two specified dates.
   */
  public static Period between(LocalDate startDay, LocalDate endDay) {
    checkPeriod(startDay, endDay);
    Period period = new Period();
    period.startDateTime = startDay.atStartOfDay().atOffset(ZoneOffset.UTC);
    period.endDateTime = endDay.plusDays(1)
        .atStartOfDay()
        .minusMinutes(1).atOffset(ZoneOffset.UTC);
    period.inDays = true;
    return period;
  }

  /**
   * Creates a new period of time between the two specified date times. The period starts at the
   * specified date and time and it ends at the specified other date and time.
   * @param startDateTime the start date and time of the period. It defines the inclusive date
   * time at which the period starts.
   * @param endDateTime the end date and time of the period. It defines the inclusive date time
   * at which the period ends. The end date time must be after the start date time.
   * @return the period of time between the two specified date times.
   */
  public static Period between(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
    checkPeriod(startDateTime, endDateTime);
    Period period = new Period();
    period.startDateTime = startDateTime.withOffsetSameInstant(ZoneOffset.UTC);
    period.endDateTime = endDateTime.withOffsetSameInstant(ZoneOffset.UTC);
    period.inDays = false;
    return period;
  }

  /**
   * Gets the date and the time in UTC/Greenwich at which this period starts on the timeline.
   * In the case of a period in days, the time is set at midnight. Nevertheless, in a such period,
   * the time is meaningless and it is then recommended to get the local date from the returned
   * date time.
   * @return a date and time in UTC/Greenwich.
   */
  public OffsetDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Gets the date and the time in UTC/Greenwich at which this period ends on the timeline.
   * In the case of a period in days, the time is set at 23 hours and 59 minutes. Nevertheless, in
   * a such period, the time is meaningless and it is then recommended to get the local date from
   * the returned date time.
   * @return a date and time in UTC/Greenwich.
   */
  public OffsetDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Is this period in days?
   * @return true if the laps of time defining this period is expressed in days. False otherwise.
   */
  public boolean isInDays() {
    return inDays;
  }

  private static void checkPeriod(final OffsetDateTime startDateTime,
      final OffsetDateTime endDateTime) {
    if (startDateTime.isAfter(endDateTime) || startDateTime.isEqual(endDateTime)) {
      throw new IllegalArgumentException("The end date time must be after the start date time");
    }
  }

  private static void checkPeriod(final LocalDate startDate, final LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("The end date must be after or equal to the start date");
    }
  }
}

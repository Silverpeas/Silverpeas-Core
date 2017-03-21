/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.date;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;

/**
 * A period is a laps of time starting at a given date or datetime and ending at a given
 * date or datetime. When the period takes care of the time, it is always set in
 * UTC/Greenwich in order to avoid any bugs by comparing two periods in different time zones
 * or offset zones.
 * @author mmoquillon
 */
@Embeddable
public class Period implements Cloneable {

  @Column(name = "startDate", nullable = false)
  private OffsetDateTime startDateTime;
  @Column(name = "endDate", nullable = false)
  private OffsetDateTime endDateTime;
  @Column(name = "inDays", nullable = false)
  private boolean inDays = false;

  /**
   * Creates a new period of time between the two specified date or datetime.
   * If date parameters are instances of {@link LocalDate}, take a look at method
   * {@link #between(LocalDate, LocalDate)}.
   * If date parameters are instances of {@link OffsetDateTime}, take a look at method
   * {@link #between(OffsetDateTime, OffsetDateTime)}.<br/>
   * @param start the start of the period. It defines the inclusive date or datetime at which the
   * period starts.
   * @param end the end day of the period. It defines the exclusive date or the exclusive datetime
   * at which the period ends. The end date must be the same or after the start date. An end date
   * equal to the start date means the period is spanning all the day; it is equivalent to an end
   * date being one day after the start date.
   * @return the period of days between the two specified dates.
   * @throw IllegalArgumentException if date parameters are not both {@link LocalDate} or
   * {@link OffsetDateTime} instances.
   */
  public static Period between(java.time.temporal.Temporal start, java.time.temporal.Temporal end) {
    if (start instanceof LocalDate && end instanceof LocalDate) {
      return between(LocalDate.from(start), LocalDate.from(end));
    } else if (start instanceof OffsetDateTime && end instanceof OffsetDateTime) {
      return between(OffsetDateTime.from(start), OffsetDateTime.from(end));
    } else {
      throw new IllegalArgumentException(
          "Temporal parameters must be both of type LocalDate or OffsetDateTime");
    }
  }

  /**
   * Creates a new period of time between the two specified dates. The period is spreading over all
   * the day(s) between the specified inclusive start day and the exclusive end day; the period is
   * expressed in days. For example, a period between 2016-12-15 and 2016-12-17 means the period
   * is spreading over two days (2016-12-15 and 2016-12-16).
   * @param startDay the start day of the period. It defines the inclusive date at which the
   * period starts.
   * @param endDay the end day of the period. It defines the exclusive date at which the period
   * ends. The end date must be the same or after the start date. An end date equal to the start
   * date means the period is spanning all the day of the start date; it is equivalent to an end
   * date being one day after the start date.
   * @return the period of days between the two specified dates.
   */
  public static Period between(LocalDate startDay, LocalDate endDay) {
    checkPeriod(startDay, endDay);
    Period period = new Period();
    period.startDateTime = startDay.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
    period.endDateTime = endDay.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
    if (startDay.isEqual(endDay)) {
      period.endDateTime = period.endDateTime.plusDays(1);
    }
    period.inDays = true;
    return period;
  }

  /**
   * Creates a new period of time between the two specified datetime. The period starts at the
   * specified inclusive datetime and it ends at the specified other exclusive datetime. For
   * example, a period between 2016-12-17T13:30:00Z and 2016-12-17T14:30:00Z means the period is
   * spanning one hour the December 12.
   * @param startDateTime the start datetime of the period. It defines the inclusive date
   * time at which the period starts.
   * @param endDateTime the end datetime of the period. It defines the exclusive datetime
   * at which the period ends. The end datetime must be after the start datetime.
   * @return the period of time between the two specified datetimes.
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
   * Converts the specified temporal instance to an {@link OffsetDateTime} instance. The temporal
   * instance must be either of type {@link OffsetDateTime} or {@link LocalDate}. Any other
   * types aren't supported by the {@link Period} and as such an {@link IllegalArgumentException}
   * is thrown.
   *
   * In some cases it is useful to get an {@link OffsetDateTime} instead of a temporal which
   * could be a {@link LocalDate} or a {@link OffsetDateTime} depending on the {@link #isInDays()}
   * information. This method computes from the given temporal the corresponding OffsetDateTime
   * which is then manipulated by the {@link Period} object for its internal treatment.
   *
   * If the temporal is already an {@link OffsetDateTime} instance, then nothing is converted and
   * the temporal is directly returned. If the temporal is a {@link LocalDate} instance (case of
   * a period on all the days), then the local date is converted into an {@link OffsetDateTime}
   * instance by taking the start of the day in UTC/Greenwich.
   * @param temporal the temporal to convert.
   * @return an {@link OffsetDateTime} instance.
   * @throws IllegalArgumentException if the specified temporal is of a type not supported by
   * the {@link Period}.
   */
  public static OffsetDateTime asOffsetDateTime(final Temporal temporal) {
    return TemporalConverter.applyByType(temporal,
        date -> date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime(),
        dateTime -> dateTime);
  }

  /**
   * Gets the inclusive temporal start date of this period of time.
   *
   * If the period is in days, then the returned temporal is a {@link LocalDate} which represents
   * the first day of the period.<br/>
   * Otherwise, the date and the time in UTC/Greenwich at which this period starts on the
   * timeline is returned.
   * @return a temporal instance ({@link LocalDate} if all day period or {@link OffsetDateTime})
   * otherwise.
   */
  public Temporal getStartDate() {
    return isInDays() ? startDateTime.toLocalDate() : startDateTime;
  }

  /**
   * Gets the exclusive temporal end date of this period of time.
   *
   * If the period is in days, then the returned temporal is a {@link LocalDate} which represents
   * the last day of the period.<br/>
   * Otherwise, the date and the time in UTC/Greenwich at which this period ends on the
   * timeline is returned.
   * @return a temporal instance ({@link LocalDate} if all day period or {@link OffsetDateTime})
   * otherwise.
   */
  public Temporal getEndDate() {
    return isInDays() ? endDateTime.toLocalDate() : endDateTime;
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
      throw new IllegalArgumentException("The end datetime must be after the start datetime");
    }
  }

  private static void checkPeriod(final LocalDate startDate, final LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("The end date must be after or equal to the start date");
    }
  }

  @Override
  public Period clone() {
    Period period = null;
    try {
      period = (Period) super.clone();
    } catch (CloneNotSupportedException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return period;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Period)) {
      return false;
    }

    final Period period = (Period) o;
    return inDays == period.inDays && startDateTime.equals(period.startDateTime) &&
        endDateTime.equals(period.endDateTime);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(startDateTime)
        .append(endDateTime)
        .append(inDays)
        .toHashCode();
  }
}

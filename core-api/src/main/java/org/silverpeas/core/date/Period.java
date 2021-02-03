/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
import org.silverpeas.core.annotation.constraint.DateRange;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.Objects;

import static org.silverpeas.core.date.TemporalConverter.asLocalDate;
import static org.silverpeas.core.date.TemporalConverter.asOffsetDateTime;

/**
 * A period is a laps of time starting at a given date or datetime and ending at a given
 * date or datetime. When the period takes care of the time, it is always set in
 * UTC/Greenwich in order to avoid any bugs by comparing two periods in different time zones
 * or offset zones.
 * @author mmoquillon
 */
@Embeddable
@DateRange(start = "startDate", end = "endDate")
public class Period implements Serializable {
  private static final long serialVersionUID = -4679172808271849961L;

  @Column(name = "startDate", nullable = false)
  private OffsetDateTime startDateTime;
  @Column(name = "endDate", nullable = false)
  private OffsetDateTime endDateTime;
  @Column(name = "inDays", nullable = false)
  private boolean inDays = false;

  /**
   * Creates a new period of time between the two specified non null date or datetime.
   * If date parameters are instances of {@link LocalDate}, take a look at method
   * {@link #between(LocalDate, LocalDate)}.
   * If date parameters are instances of {@link OffsetDateTime}, take a look at method
   * {@link #between(OffsetDateTime, OffsetDateTime)}.
   * @param start the start of the period. It defines the inclusive date or datetime at which the
   * period starts.
   * @param end the end day of the period. It defines the exclusive date or the exclusive datetime
   * at which the period ends. The end date must be the same or after the start date. An end date
   * equal to the start date means the period is spanning all the day; it is equivalent to an end
   * date being one day after the start date.
   * @return the period of days between the two specified dates.
   * @throws IllegalArgumentException if date parameters are not both {@link LocalDate} or
   * {@link OffsetDateTime} instances.
   */
  public static Period between(java.time.temporal.Temporal start, java.time.temporal.Temporal end) {
    if (start instanceof LocalDate && end instanceof LocalDate) {
      return between(LocalDate.from(start), LocalDate.from(end));
    } else if (start instanceof OffsetDateTime && end instanceof OffsetDateTime) {
      return between(OffsetDateTime.from(start), OffsetDateTime.from(end));
    } else {
      throw new IllegalArgumentException(
          "Temporal parameters must be either of type LocalDate or OffsetDateTime");
    }
  }

  /**
   * Creates a new period of time between the two non null specified dates. The period is spreading
   * over all the day(s) between the specified inclusive start day and the exclusive end day; the
   * period is expressed in days. For example, a period between 2016-12-15 and 2016-12-17 means the
   * period is spreading over two days (2016-12-15 and 2016-12-16).
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
    period.startDateTime = startDay == LocalDate.MIN ? OffsetDateTime.MIN :
        startDay.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
    period.endDateTime = endDay == LocalDate.MAX ? OffsetDateTime.MAX :
        endDay.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
    if (startDay.isEqual(endDay)) {
      period.endDateTime = period.endDateTime.plusDays(1);
    }
    period.inDays = true;
    return period;
  }

  /**
   * Creates a new period of time between the two non null specified datetime. The period starts at
   * the specified inclusive datetime and it ends at the specified other exclusive datetime. For
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
    period.startDateTime = startDateTime == OffsetDateTime.MIN ? OffsetDateTime.MIN :
        startDateTime.withOffsetSameInstant(ZoneOffset.UTC);
    period.endDateTime = endDateTime == OffsetDateTime.MAX ? OffsetDateTime.MAX :
        endDateTime.withOffsetSameInstant(ZoneOffset.UTC);
    period.inDays = false;
    return period;
  }

  /**
   * Creates a new period of time between the two specified date or datetime.
   * If date parameters are instances of {@link LocalDate}, take a look at the method
   * {@link Period#betweenNullable(LocalDate, LocalDate)}.
   * If date parameters are instances of {@link OffsetDateTime}, take a look at the method
   * {@link Period#betweenNullable(OffsetDateTime, OffsetDateTime)}.
   * If both date parameters are null, then a period between {@link LocalDate#MIN} and
   * {@link LocalDate#MAX} is returned unless those parameters are explicitly typed; for example:
   * {@code Period.betweenNullable((OffsetDateTime) null, null)}
   * @param start the start of the period. It defines the inclusive date or datetime at which the
   * period starts. If it is null then the minimum temporal (date or datetime) is taken.
   * @param end the end day of the period. It defines the exclusive date or the exclusive datetime
   * at which the period ends. The end date must be the same or after the start date. An end date
   * equal to the start date means the period is spanning all the day; it is equivalent to an end
   * date being one day after the start date. If It is null then the maximum temporal (date or
   * datetime) is taken.
   * @return the period of days between the two specified dates.
   * @throws IllegalArgumentException if date parameters are not both {@link LocalDate} or
   * {@link OffsetDateTime} instances.
   * @see LocalDate#MIN for the minimum supported date.
   * @see OffsetDateTime#MIN for the maximum supported date.
   * @see LocalDate#MAX for the maximum supported datetime.
   * @see OffsetDateTime#MAX for the maximum supported datetime.
   */
  public static Period betweenNullable(java.time.temporal.Temporal start,
      java.time.temporal.Temporal end) {
    if (start == null && end == null) {
      return betweenNullable(LocalDate.MIN, LocalDate.MAX);
    }
    if (start != null && end != null) {
      // we ensure start and end are of the same type
      return between(start, end);
    }
    if (start instanceof LocalDate || end instanceof LocalDate) {
      return betweenNullable(minOrDate(start), maxOrDate(end));
    } else if (start instanceof OffsetDateTime || end instanceof OffsetDateTime) {
      return betweenNullable(minOrDateTime(start), maxOrDateTime(end));
    }
    throw new IllegalArgumentException(
          "Temporal parameters must be either of type LocalDate or OffsetDateTime");
  }

  /**
   * Creates a new period of time between the two specified dates. The period is spreading over all
   * the day(s) between the specified inclusive start day and the exclusive end day; the period is
   * expressed in days. For example, a period between 2016-12-15 and 2016-12-17 means the period
   * is spreading over two days (2016-12-15 and 2016-12-16).
   * @param startDay the start day of the period. It defines the inclusive date at which the
   * period starts. If null, then the minimum supported {@link LocalDate#MIN} date is taken.
   * @param endDay the end day of the period. It defines the exclusive date at which the period
   * ends. The end date must be the same or after the start date. An end date equal to the start
   * date means the period is spanning all the day of the start date; it is equivalent to an end
   * date being one day after the start date. If null, then the maximum supported
   * {@link LocalDate#MAX} is taken.
   * @return the period of days between the two specified dates.
   * @see LocalDate#MIN for the minimum supported date.
   * @see LocalDate#MAX for the maximum supported date.
   */
  public static Period betweenNullable(LocalDate startDay, LocalDate endDay) {
    LocalDate start = minOrDate(startDay);
    LocalDate end = maxOrDate(endDay);
    return between(start, end);
  }

  /**
   * Creates a new period of time between the two specified datetime. The period starts at the
   * specified inclusive datetime and it ends at the specified other exclusive datetime. For
   * example, a period between 2016-12-17T13:30:00Z and 2016-12-17T14:30:00Z means the period is
   * spanning one hour the December 12.
   * @param startDateTime the start datetime of the period. It defines the inclusive date
   * time at which the period starts. If null then the minimum supported
   * {@link OffsetDateTime#MIN} is taken.
   * @param endDateTime the end datetime of the period. It defines the exclusive datetime
   * at which the period ends. The end datetime must be after the start datetime. If null, then the
   * maximum supported {@link OffsetDateTime#MAX} is taken.
   * @return the period of time between the two specified date times.
   * @see OffsetDateTime#MIN for the minimum supported date.
   * @see OffsetDateTime#MAX for the maximum supported date.
   */
  public static Period betweenNullable(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
    OffsetDateTime start = minOrDateTime(startDateTime);
    OffsetDateTime end = maxOrDateTime(endDateTime);
    return between(start, end);
  }

  /**
   * Gets the inclusive temporal start date of this period of time.
   *
   * If the period is in days, then the returned temporal is a {@link LocalDate} which represents
   * the first day of the period.<br>
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
   * the last day of the period.<br>
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

  /**
   * Is this period starts at the the minimum supported date/datetime in Java?
   * @return true if this period starts at the minimum date/datetime supported by Java.
   * False otherwise.
   * @see LocalDate#MIN for the minimum supported date.
   * @see OffsetDateTime#MIN for the maximum supported date.
   */
  public boolean startsAtMinDate() {
    return startDateTime.withOffsetSameInstant(OffsetDateTime.MIN.getOffset())
        .equals(OffsetDateTime.MIN);
  }

  /**
   * Is this period ends at the the maximum supported date/datetime in Java?
   * @return true if this period ends at the minimum date/datetime supported by Java.
   * False otherwise.
   * @see LocalDate#MAX for the maximum supported datetime.
   * @see OffsetDateTime#MAX for the maximum supported datetime.
   */
  public boolean endsAtMaxDate() {
    return endDateTime.withOffsetSameInstant(OffsetDateTime.MAX.getOffset())
        .equals(OffsetDateTime.MAX);
  }

  /**
   * Is this period including the specified temporal?
   * @param dateTime either a date or a date time. Any other temporal type isn't supported.
   * @return true if the specified date is included in this period, false otherwise.
   */
  public boolean includes(final Temporal dateTime) {
    OffsetDateTime dt = asOffsetDateTime(dateTime);
    return dt.compareTo(startDateTime) >= 0 && dt.compareTo(endDateTime) < 0;
  }

  /**
   * Is this period ending before the specified temporal?
   * @param dateTime either a date or a date time. Any other temporal type isn't supported.
   * @return true if this period's end date is at or before the specified temporal (the period's
   * end date is exclusive).
   */
  public boolean endsBefore(final Temporal dateTime) {
    OffsetDateTime dt = asOffsetDateTime(dateTime);
    return dt.compareTo(endDateTime) >= 0;
  }

  /**
   * Is this period ending after the specified temporal?
   * @param dateTime either a date or a date time. Any other temporal type isn't supported.
   * @return true if this period's end date is at or before the specified temporal (the period's
   * end date is exclusive).
   */
  public boolean endsAfter(final Temporal dateTime) {
    OffsetDateTime dt = asOffsetDateTime(dateTime);
    return dt.compareTo(endDateTime) < 0;
  }

  /**
   * Is this period starting after the specified temporal?
   * @param dateTime either a date or a date time. Any other temporal type isn't supported.
   * @return true if this period's start date is after the specified temporal (the period's
   * start date is inclusive).
   */
  public boolean startsAfter(final Temporal dateTime) {
    OffsetDateTime dt = asOffsetDateTime(dateTime);
    return dt.compareTo(startDateTime) < 0;
  }

  private static void checkPeriod(final OffsetDateTime startDateTime,
      final OffsetDateTime endDateTime) {
    Objects.requireNonNull(startDateTime);
    Objects.requireNonNull(endDateTime);
    if (startDateTime.isAfter(endDateTime) || startDateTime.isEqual(endDateTime)) {
      throw new IllegalArgumentException("The end datetime must be after the start datetime");
    }
  }

  private static void checkPeriod(final LocalDate startDate, final LocalDate endDate) {
    Objects.requireNonNull(startDate);
    Objects.requireNonNull(endDate);
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("The end date must be after or equal to the start date");
    }
  }

  private static LocalDate minOrDate(final Temporal date) {
    return date == null ? LocalDate.MIN : asLocalDate(date);
  }

  private static LocalDate maxOrDate(final Temporal date) {
    return date == null ? LocalDate.MAX : asLocalDate(date);
  }

  private static OffsetDateTime minOrDateTime(final Temporal dateTime) {
    return dateTime == null ? OffsetDateTime.MIN : asOffsetDateTime(dateTime);
  }

  private static OffsetDateTime maxOrDateTime(final Temporal dateTime) {
    return dateTime == null ? OffsetDateTime.MAX : asOffsetDateTime(dateTime);
  }

  public Period copy() {
    Period period = new Period();
    period.startDateTime = startDateTime;
    period.endDateTime = endDateTime;
    period.inDays = inDays;
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

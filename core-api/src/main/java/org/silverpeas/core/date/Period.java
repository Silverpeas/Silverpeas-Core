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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Objects;

import static org.silverpeas.core.date.TemporalConverter.asInstant;

/**
 * <p>
 * A period is a laps of time starting at a given date or datetime and ending at another given date
 * or datetime. When the period takes care of the time, it is always set in UTC/Greenwich in order
 * to avoid any bugs by comparing two periods in different time zones or offset zones. A period
 * is indefinite when it spans over a very large of time that cannot be reached; in this case the
 * period is counted in days between {@link LocalDate#MIN} and {@link LocalDate#MAX}. An indefinite
 * period can be created either by:
 * </p>
 *<ul>
 *   <li>invoking the {@link Period#indefinite()} method,</li>
 *   <li>or by invoking one of the <code>between(...,...)</code> method with as arguments the
 *   <code>MIN</code> et <code>MAX</code> values of one of the concrete date or datetime of the Java
 *   Time API,</li>
 *   <li>or by invoking one of the <code>betweenNullable(...,...)</code> method with as arguments
 *   either null or the <code>MIN</code> et <code>MAX</code> values of one of the concrete date or
 *   datetime of the Java Time API.</li>
 *</ul>
 *
 * @author mmoquillon
 */
@Embeddable
@DateRange(start = "startDate", end = "endDate")
public class Period implements Serializable {
  private static final long serialVersionUID = -4679172808271849961L;

  @Column(name = "startDate", nullable = false)
  private Instant startDateTime;
  @Column(name = "endDate", nullable = false)
  private Instant endDateTime;
  @Column(name = "inDays", nullable = false)
  private boolean inDays = false;

  /**
   * Creates an indefinite period of time. An undefined period is a period over an indefinite range
   * of time meaning that whatever any event, it occurs during this period. It is like an infinite
   * period but starting at {@link LocalDate#MIN} and ending at {@link LocalDate#MAX}.
   * @return an undefined period.
   */
  public static Period indefinite() {
    return betweenInDays(Instant.MIN, Instant.MAX);
  }

  /**
   * Creates a new period of time between the two specified non null date or datetime. It accepts
   * as both {@link Temporal} parameters either {@link LocalDate}, {@link LocalDateTime},
   * {@link OffsetDateTime}, {@link ZonedDateTime} or {@link Instant} instances.
   *
   * @param start the start of the period. It defines the inclusive date or datetime at which the
   *              period starts.
   * @param end   the end day of the period. It defines the exclusive date or the exclusive datetime
   *              at which the period ends. The end date must be the same or after the start date.
   *              An end date equal to the start date means the period is spanning all the day; it
   *              is equivalent to an end date being one day after the start date.
   * @return the period of days between the two specified dates.
   * @throws IllegalArgumentException if the concrete type of the temporal parameters isn't
   * supported or if they aren't of the same concrete type.
   */
  public static Period between(Temporal start, Temporal end) {
    Objects.requireNonNull(start);
    Objects.requireNonNull(end);
    if (!start.getClass().equals(end.getClass())) {
      throw new IllegalArgumentException("Temporal parameters must be of same type." +
          " Actually period start is " + start.getClass().getSimpleName() +
          " and the period end is " + end.getClass().getSimpleName());
    }
    if (start instanceof LocalDate && end instanceof LocalDate) {
      return between(LocalDate.from(start), LocalDate.from(end));
    }
    return between(asInstant(start), asInstant(end));
  }

  /**
   * Creates a new period of time between the two non null specified dates. The period is spreading
   * over all the day(s) between the specified inclusive start day and the exclusive end day; the
   * period is expressed in days. For example, a period between 2016-12-15 and 2016-12-17 means the
   * period is spreading over two days (2016-12-15 and 2016-12-16).
   *
   * @param startDay the start day of the period. It defines the inclusive date at which the period
   *                 starts.
   * @param endDay   the end day of the period. It defines the exclusive date at which the period
   *                 ends. The end date must be the same or after the start date. An end date equal
   *                 to the start date means the period is spanning all the day of the start date;
   *                 it is equivalent to an end date being one day after the start date.
   * @return the period of days between the two specified dates.
   */
  public static Period between(LocalDate startDay, LocalDate endDay) {
    Objects.requireNonNull(startDay);
    Objects.requireNonNull(endDay);
    LocalDate upperBound = startDay.equals(endDay) ? endDay.plusDays(1) : endDay;
    return betweenInDays(asInstant(startDay), asInstant(upperBound));
  }

  /**
   * Creates a new period of time between the two non null specified datetime. The period starts at
   * the specified inclusive datetime and it ends at the specified other exclusive datetime. For
   * example, a period between 2016-12-17T13:30:00Z and 2016-12-17T14:30:00Z means the period is
   * spanning one hour the December 12.
   *
   * @param startDateTime the start datetime of the period. It defines the inclusive date time at
   *                      which the period starts.
   * @param endDateTime   the end datetime of the period. It defines the exclusive datetime at which
   *                      the period ends. The end datetime must be after the start datetime.
   * @return the period of time between the two specified datetimes.
   */
  public static Period between(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
    return between(asInstant(startDateTime), asInstant(endDateTime));
  }

  /**
   * Creates a new period of time between the two non null specified datetime. The period starts at
   * the specified inclusive datetime and it ends at the specified other exclusive datetime. For
   * example, a period between 2016-12-17T13:30:00Z and 2016-12-17T14:30:00Z means the period is
   * spanning one hour the December 12.
   *
   * @param startDateTime the start datetime of the period. It defines the inclusive date time at
   *                      which the period starts.
   * @param endDateTime the end datetime of the period. It defines the exclusive datetime at which
   *                    the period ends. The end datetime must be after the start datetime.
   * @return the period of time between the two specified datetimes.
   */
  public static Period between(ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
    return between(asInstant(startDateTime), asInstant(endDateTime));
  }

  /**
   * Creates a new period of time between the two non null specified instant. The period starts at
   * the specified inclusive instant and it ends at the specified other exclusive instant. For
   * example, a period between 2016-12-17T13:30:00Z and 2016-12-17T14:30:00Z means the period is
   * spanning one hour the December 12.
   *
   * @param startInstant the start instant of the period. It defines the inclusive epoch date time
   *                     at which the period starts.
   * @param endInstant the end instant of the period. It defines the exclusive epoch date time at
   *                   which the period ends. The end instant must be after the start instant.
   * @return the period of time between the two specified instants.
   */
  public static Period between(Instant startInstant, Instant endInstant) {
    checkPeriod(startInstant, endInstant);
    Period period = new Period();
    period.startDateTime = startInstant;
    period.endDateTime = endInstant;
    period.inDays = period.startsAtMinDate() && period.endsAtMaxDate();
    return period;
  }

  /**
   * Creates a new period of time between the two days represented by the two non-null specified
   * instant. The period is spreading over all the day(s) between the specified inclusive start day
   * and the exclusive end day; the period is expressed in days. For example, a period between
   * 2016-12-15 and 2016-12-17 means the period is spreading over two days (2016-12-15 and
   * 2016-12-16). If you want the period spreads over a whole day, then the endInstant must
   * after one day the startInstant (as the endInstant is exclusive): a period starting at
   * 2016-12-15 and ending at 2016-12-16 means the period is spreading over the 2016-12-15.
   * <p>
   * This method is a convenient one to represent a period in days with {@link java.util.Date} or
   * {@link Instant} object (by using {@link Date#toInstant()}). Nevertheless we strongly recommend
   * to prefer the {@link Period#between(LocalDate, LocalDate)} instead to avoid unexpected
   * surprise with the date handling.
   * </p>
   *
   * @param startInstant the start instant of the period. It defines the inclusive epoch day
   *                     at which the period starts.
   * @param endInstant the end instant of the period. It defines the exclusive epoch day at
   *                   which the period ends. The end instant must be after the start instant.
   * @return the period of time between the two specified instants.
   */
  public static Period betweenInDays(Instant startInstant, Instant endInstant) {
    Period period = between(startInstant, endInstant);
    period.inDays = true;
    return period;
  }

  /**
   * Creates a new period of time between the two specified date or datetime. If date parameters are
   * instances of {@link LocalDate}, take a look at the method {@link
   * Period#betweenNullable(LocalDate, LocalDate)}. If date parameters are instances of {@link
   * OffsetDateTime}, take a look at the method {@link Period#betweenNullable(OffsetDateTime,
   * OffsetDateTime)}. If both date parameters are null, then a period between {@link LocalDate#MIN}
   * and {@link LocalDate#MAX} is returned unless those parameters are explicitly typed; for
   * example: {@code Period.betweenNullable((OffsetDateTime) null, null)}
   *
   * @param start the start of the period. It defines the inclusive date or datetime at which the
   *              period starts. If it is null then the minimum temporal (date or datetime) is
   *              taken.
   * @param end   the end day of the period. It defines the exclusive date or the exclusive datetime
   *              at which the period ends. The end date must be the same or after the start date.
   *              An end date equal to the start date means the period is spanning all the day; it
   *              is equivalent to an end date being one day after the start date. If It is null
   *              then the maximum temporal (date or datetime) is taken.
   * @return the period of days between the two specified dates.
   * @throws IllegalArgumentException if date parameters are not both {@link LocalDate} or {@link
   *                                  OffsetDateTime} instances.
   * @see LocalDate#MIN for the minimum supported date.
   * @see OffsetDateTime#MIN for the maximum supported date.
   * @see LocalDate#MAX for the maximum supported datetime.
   * @see OffsetDateTime#MAX for the maximum supported datetime.
   */
  public static Period betweenNullable(Temporal start, Temporal end) {
    if (start != null && end != null) {
      // we ensure start and end are of the same type
      return between(start, end);
    } else if (start instanceof LocalDate || end instanceof LocalDate ||
        (start == null && end == null)) {
      return betweenInDays(minOrInstant(start), maxOrInstant(end));
    }
    return between(minOrInstant(start), maxOrInstant(end));
  }

  /**
   * Creates a new period of time between the two specified dates. The period is spreading over all
   * the day(s) between the specified inclusive start day and the exclusive end day; the period is
   * expressed in days. For example, a period between 2016-12-15 and 2016-12-17 means the period is
   * spreading over two days (2016-12-15 and 2016-12-16).
   *
   * @param startDay the start day of the period. It defines the inclusive date at which the period
   *                 starts. If null, then the minimum supported {@link LocalDate#MIN} date is
   *                 taken.
   * @param endDay   the end day of the period. It defines the exclusive date at which the period
   *                 ends. The end date must be the same or after the start date. An end date equal
   *                 to the start date means the period is spanning all the day of the start date;
   *                 it is equivalent to an end date being one day after the start date. If null,
   *                 then the maximum supported {@link LocalDate#MAX} is taken.
   * @return the period of days between the two specified dates.
   * @see LocalDate#MIN for the minimum supported date.
   * @see LocalDate#MAX for the maximum supported date.
   */
  public static Period betweenNullable(LocalDate startDay, LocalDate endDay) {
   LocalDate upperBound = startDay != null && startDay.equals(endDay) ? endDay.plusDays(1) : endDay;
   return betweenInDays(minOrInstant(startDay), maxOrInstant(upperBound));
  }

  /**
   * Creates a new period of time between the two specified datetime. The period starts at the
   * specified inclusive datetime and it ends at the specified other exclusive datetime. For
   * example, a period between 2016-12-17T13:30:00Z and 2016-12-17T14:30:00Z means the period is
   * spanning one hour the December 12.
   *
   * @param startDateTime the start datetime of the period. It defines the inclusive date time at
   *                      which the period starts. If null then the minimum supported {@link
   *                      OffsetDateTime#MIN} is taken.
   * @param endDateTime   the end datetime of the period. It defines the exclusive datetime at which
   *                      the period ends. The end datetime must be after the start datetime. If
   *                      null, then the maximum supported {@link OffsetDateTime#MAX} is taken.
   * @return the period of time between the two specified date times.
   * @see OffsetDateTime#MIN for the minimum supported date.
   * @see OffsetDateTime#MAX for the maximum supported date.
   */
  public static Period betweenNullable(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
    return between(minOrInstant(startDateTime), maxOrInstant(endDateTime));
  }

  /**
   * Creates a new period of time between the two specified datetime. The period starts at the
   * specified inclusive datetime and it ends at the specified other exclusive datetime. For
   * example, a period between 2016-12-17T13:30:00Z and 2016-12-17T14:30:00Z means the period is
   * spanning one hour the December 12.
   *
   * @param startDateTime the start datetime of the period. It defines the inclusive date time at
   *                      which the period starts. If null then the minimum supported
   *                      {@link OffsetDateTime#MIN} is taken.
   * @param endDateTime the end datetime of the period. It defines the exclusive datetime at which
   *                    the period ends. The end datetime must be after the start datetime. If null,
   *                    then the maximum supported {@link OffsetDateTime#MAX} is taken.
   * @return the period of time between the two specified date times.
   * @see OffsetDateTime#MIN for the minimum supported date.
   * @see OffsetDateTime#MAX for the maximum supported date.
   */
  public static Period betweenNullable(ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
    return between(minOrInstant(startDateTime), maxOrInstant(endDateTime));
  }

  /**
   * Creates a new period of time between the two specified instants. The period starts at the
   * specified inclusive instant and it ends at the specified other exclusive instant. For example,
   * a period between 2016-12-17T13:30:00Z and 2016-12-17T14:30:00Z means the period is spanning one
   * hour the December 12.
   *
   * @param startInstant the start instant of the period. It defines the inclusive epoch date time
   *                     at which the period starts. If null then the minimum supported
   *                     {@link OffsetDateTime#MIN} is taken.
   * @param endInstant the end instant of the period. It defines the exclusive epoch date time at
   *                   which the period ends. The end instant must be after the start instant. If
   *                   null, then the maximum supported {@link Instant#MAX} is taken.
   * @return the period of time between the two specified instants.
   * @see Instant#MIN for the minimum supported date.
   * @see Instant#MAX for the maximum supported date.
   */
  public static Period betweenNullable(Instant startInstant, Instant endInstant) {
    Instant start = minOrInstant(startInstant);
    Instant end = maxOrInstant(endInstant);
    return between(start, end);
  }

  /**
   * Gets the inclusive temporal start date of this period of time.
   * <p>
   * If the period is in days, then the returned temporal is a {@link LocalDate} which represents
   * the first day of the period.<br> Otherwise, the date and the time in UTC/Greenwich at which
   * this period starts on the timeline is returned.
   *
   * @return a temporal instance ({@link LocalDate} if all day period or {@link OffsetDateTime})
   * otherwise.
   */
  public Temporal getStartDate() {
    Temporal startDate;
    if (startsAtMinDate()) {
      startDate = isInDays() ? LocalDate.MIN : OffsetDateTime.MIN;
    } else {
      startDate = isInDays() ? LocalDate.ofInstant(startDateTime, ZoneOffset.UTC) :
          OffsetDateTime.ofInstant(startDateTime, ZoneOffset.UTC);
    }
    return startDate;
  }

  /**
   * Gets the exclusive temporal end date of this period of time.
   * <p>
   * If the period is in days, then the returned temporal is a {@link LocalDate} which represents
   * the last day of the period.<br> Otherwise, the date and the time in UTC/Greenwich at which this
   * period ends on the timeline is returned.
   *
   * @return a temporal instance ({@link LocalDate} if all day period or {@link OffsetDateTime})
   * otherwise.
   */
  public Temporal getEndDate() {
    Temporal endDate;
    if (endsAtMaxDate()) {
      endDate = isInDays() ? LocalDate.MAX : OffsetDateTime.MAX;
    } else {
      endDate = isInDays() ? LocalDate.ofInstant(endDateTime, ZoneOffset.UTC) :
          OffsetDateTime.ofInstant(endDateTime, ZoneOffset.UTC);
    }
    return endDate;
  }

  /**
   * Is this period in days?
   *
   * @return true if the laps of time defining this period is expressed in days. False otherwise.
   */
  public boolean isInDays() {
    return inDays;
  }

  /**
   * Is this period an indefinite one? That is to say a period ranging over an indefinite range
   * of time meaning that whatever any event, it occurs during this period. In Silverpeas, an
   * indefinite period starts at {@link LocalDate#MIN} and ends at {@link LocalDate#MAX}.
   * @return true if this period is an indefinite one. False otherwise.
   */
  public boolean isIndefinite() {
    return startsAtMinDate() && endsAtMaxDate();
  }

  /**
   * Is this period starts at the the minimum supported date/datetime in Java?
   * @return true if this period starts at the minimum date/datetime supported by Java. False
   * otherwise.
   * @see Instant#MIN for the minimum supported date.
   */
  public boolean startsAtMinDate() {
    return startDateTime.equals(Instant.MIN);
  }

  /**
   * Is this period ends at the the maximum supported date/datetime in Java?
   * @return true if this period ends at the minimum date/datetime supported by Java. False
   * otherwise.
   * @see Instant#MAX for the maximum supported datetime.
   */
  public boolean endsAtMaxDate() {
    return endDateTime.equals(Instant.MAX);
  }

  /**
   * Is this period including the specified temporal?
   *
   * @param dateTime either a date or a date time. Any other temporal type isn't supported.
   * @return true if the specified date is included in this period, false otherwise.
   */
  public boolean includes(final Temporal dateTime) {
    Instant dt = asInstant(dateTime);
    return dt.compareTo(startDateTime) >= 0 && dt.compareTo(endDateTime) < 0;
  }

  /**
   * Is this period ending before the specified temporal?
   *
   * @param dateTime either a date or a date time. Any other temporal type isn't supported.
   * @return true if this period's end date is at or before the specified temporal (the period's end
   * date is exclusive).
   */
  public boolean endsBefore(final Temporal dateTime) {
    Instant dt = asInstant(dateTime);
    return dt.compareTo(endDateTime) >= 0;
  }

  /**
   * Is this period ending after the specified temporal?
   *
   * @param dateTime either a date or a date time. Any other temporal type isn't supported.
   * @return true if this period's end date is at or before the specified temporal (the period's end
   * date is exclusive).
   */
  public boolean endsAfter(final Temporal dateTime) {
    Instant dt = asInstant(dateTime);
    return dt.compareTo(endDateTime) < 0;
  }

  /**
   * Is this period starting after the specified temporal?
   *
   * @param dateTime either a date or a date time. Any other temporal type isn't supported.
   * @return true if this period's start date is after the specified temporal (the period's start
   * date is inclusive).
   */
  public boolean startsAfter(final Temporal dateTime) {
    Instant dt = asInstant(dateTime);
    return dt.compareTo(startDateTime) < 0;
  }

  private static void checkPeriod(final Instant startDateTime, final Instant endDateTime) {
    Objects.requireNonNull(startDateTime);
    Objects.requireNonNull(endDateTime);
    if (startDateTime.isAfter(endDateTime) || startDateTime.equals(endDateTime)) {
      throw new IllegalArgumentException("The end datetime must be after the start datetime");
    }
  }

  private static Instant minOrInstant(final Temporal temporal) {
    return temporal == null ? Instant.MIN : asInstant(temporal);
  }

  private static Instant maxOrInstant(final Temporal temporal) {
    return temporal == null ? Instant.MAX : asInstant(temporal);
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

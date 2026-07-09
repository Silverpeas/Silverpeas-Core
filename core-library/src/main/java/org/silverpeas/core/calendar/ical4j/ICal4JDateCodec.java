/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.ical4j;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.date.TemporalConverter;

import jakarta.inject.Singleton;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A decoder/encoder of iCal4J temporal values with Silverpeas dates.
 * <p>
 * Since iCal4J 4.x, the library represents dates and datetimes directly with the
 * {@link java.time.temporal.Temporal} types of the Java Time API:
 * <ul>
 *   <li>a date (all day) is a {@link LocalDate};</li>
 *   <li>a datetime in UTC is an {@link Instant} (serialized with the {@code Z} suffix);</li>
 *   <li>a datetime with an explicit time zone is a {@link ZonedDateTime} (serialized with a
 *   {@code TZID} parameter).</li>
 * </ul>
 * This codec is therefore in charge of mapping the Silverpeas temporals to the expected iCal4J
 * temporal representation and vice-versa.
 */
@Technical
@Bean
@Singleton
public class ICal4JDateCodec {

  /**
   * Comparator ordering the iCal4J temporals (dates and datetimes of possibly different types) by
   * their instant on the UTC timeline.
   * @return a comparator of {@link Temporal} instances.
   */
  public static Comparator<Temporal> temporalComparator() {
    return Comparator.comparing(ICal4JDateCodec::toInstant);
  }

  private static Instant toInstant(final Temporal temporal) {
    if (temporal instanceof Instant) {
      return (Instant) temporal;
    } else if (temporal instanceof LocalDate) {
      return ((LocalDate) temporal).atStartOfDay(ZoneOffset.UTC).toInstant();
    } else if (temporal instanceof OffsetDateTime) {
      return ((OffsetDateTime) temporal).toInstant();
    } else if (temporal instanceof ZonedDateTime) {
      return ((ZonedDateTime) temporal).toInstant();
    } else if (temporal instanceof LocalDateTime) {
      // a floating datetime (without any time zone) is interpreted, as iCal4J does, in the
      // default time zone of the system
      return ((LocalDateTime) temporal).atZone(ZoneId.systemDefault()).toInstant();
    }
    return Instant.from(temporal);
  }

  /**
   * Indicates if the date of an component must be encoded in UTC.
   * @param eventRecurrent true if event is recurrent, false otherwise.
   * @param component the component data from which to verify the conditions.
   * @return true if dates must be encoded into UTC, false otherwise.
   */
  public boolean isEventDateToBeEncodedIntoUtc(final boolean eventRecurrent,
      final CalendarComponent component) {
    return component.getPeriod().isInDays() || !eventRecurrent;
  }

  /**
   * Encodes a temporal data into an iCal4J temporal.
   * @param eventRecurrent true if event is recurrent, false otherwise.
   * @param component the component data to use to encode the given temporal.
   * @param aTemporal the temporal data to encode which have to be extracted from the given
   * component.
   * @return an iCal4J temporal.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public Temporal encode(final boolean eventRecurrent, final CalendarComponent component,
      final Temporal aTemporal) {
    final Temporal temporal =
        isEventDateToBeEncodedIntoUtc(eventRecurrent, component) ?
            aTemporal :
            OffsetDateTime.from(aTemporal).atZoneSameInstant(component.getCalendar().getZoneId());
    return encode(temporal);
  }

  /**
   * Encodes a temporal data into an iCal4J temporal.
   * @param aTemporal the temporal data to encode.
   * @return an iCal4J temporal.
   * @throws IllegalArgumentException if the encoding fails.
   */
  public Temporal encode(final Temporal aTemporal) {
    return TemporalConverter.applyByType(aTemporal, localDateConversion(),
        offsetDateTimeConversion(), zonedDateTimeConversion());
  }

  /**
   * Encodes a temporal data set into an iCal4J date list.
   * @param temporals the temporal data set to encode.
   * @return an iCal4J date list.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public DateList<Temporal> encode(final Collection<? extends Temporal> temporals) {
    final List<Temporal> dates = temporals.stream()
        .map(this::encode)
        .sorted(temporalComparator())
        .collect(Collectors.toList());
    return new DateList<>(dates);
  }

  /**
   * Encodes a datetime into an iCal4J temporal set in UTC.
   * <p>
   * The datetime is represented as an {@link OffsetDateTime} in UTC and not as an {@link Instant}:
   * both are serialized by iCal4J with the {@code Z} (Zulu) suffix, but only the former supports
   * the temporal arithmetic (weeks, months, years) required by the recurrence computation.
   * @param dateTime the datetime to encode.
   * @return an iCal4J UTC datetime as an {@link OffsetDateTime}.
   */
  public OffsetDateTime encode(final OffsetDateTime dateTime) {
    return dateTime.withOffsetSameInstant(ZoneOffset.UTC);
  }

  /**
   * Encodes a datetime into an iCal4J temporal that takes into account the time zone of the
   * specified datetime.
   * @param dateTime the datetime with timezone to encode.
   * @return an iCal4J zoned datetime.
   */
  public ZonedDateTime encode(final ZonedDateTime dateTime) {
    return dateTime;
  }

  /**
   * Encodes a date into an iCal4J temporal.
   * @param date a date.
   * @return an iCal4J date as a {@link LocalDate}.
   */
  public LocalDate encode(final LocalDate date) {
    return date;
  }

  /**
   * Gets the conversion function of an {@link OffsetDateTime} instance to an iCal4J temporal.
   * @return a conversion of a {@link OffsetDateTime} to an iCal4J UTC datetime.
   */
  public TemporalConverter.Conversion<OffsetDateTime, Temporal> offsetDateTimeConversion() {
    return TemporalConverter.Conversion.of(OffsetDateTime.class, this::encode);
  }

  /**
   * Gets the conversion function of a {@link ZonedDateTime} instance to an iCal4J temporal.
   * @return a conversion of a {@link ZonedDateTime} to an iCal4J zoned datetime.
   */
  public TemporalConverter.Conversion<ZonedDateTime, Temporal> zonedDateTimeConversion() {
    return TemporalConverter.Conversion.of(ZonedDateTime.class, this::encode);
  }

  /**
   * Gets the conversion function of a {@link LocalDate} instance to an iCal4J temporal.
   * @return a conversion of a {@link LocalDate} to an iCal4J date.
   */
  public TemporalConverter.Conversion<LocalDate, Temporal> localDateConversion() {
    return TemporalConverter.Conversion.of(LocalDate.class, this::encode);
  }

  public TimeZone getTimeZone(final ZoneId zoneId) {
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    return registry.getTimeZone(zoneId.getId());
  }

  /**
   * Decodes an iCal4J temporal (date or datetime) into a Silverpeas temporal.
   * @param temporal the iCal4J date or datetime to decode.
   * @return a temporal instance. If the temporal contains time data, then it is returned as an
   * {@link OffsetDateTime} set in UTC; otherwise it is returned as a {@link LocalDate}.
   * @throws SilverpeasRuntimeException if the decoding fails.
   */
  public final Temporal decode(final Temporal temporal) {
    if (temporal instanceof LocalDate) {
      return temporal;
    }
    return OffsetDateTime.ofInstant(toInstant(temporal), ZoneOffset.UTC);
  }
}

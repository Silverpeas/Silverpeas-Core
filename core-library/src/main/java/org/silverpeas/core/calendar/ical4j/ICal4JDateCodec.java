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

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.date.TemporalConverter;

import javax.inject.Singleton;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A decoder/encoder of iCal4J dates with Silverpeas dates.
 */
@Technical
@Bean
@Singleton
public class ICal4JDateCodec {

  private static final String ICAL_LOCAL_PATTERN = "yyyyMMdd'T'HHmmss";
  private static final String ICAL_UTC_PATTERN = ICAL_LOCAL_PATTERN + "'Z'";
  private static final String ICAL_DATE_PATTERN = "yyyyMMdd";

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
   * Encodes a temporal data into an iCal4J date.
   * @param eventRecurrent true if event is recurrent, false otherwise.
   * @param component the component data to use to encode the given temporal.
   * @param aTemporal the temporal data to encode which have to be extracted from the given
   * component.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public Date encode(final boolean eventRecurrent, final CalendarComponent component,
      final java.time.temporal.Temporal aTemporal) {
    final java.time.temporal.Temporal temporal =
        isEventDateToBeEncodedIntoUtc(eventRecurrent, component) ?
            aTemporal :
            OffsetDateTime.from(aTemporal).atZoneSameInstant(component.getCalendar().getZoneId());
    return encode(temporal);
  }

  /**
   * Encodes a temporal data into an iCal4J date.
   * @param aTemporal the temporal data to encode.
   * @return an iCal4J date.
   * @throws IllegalArgumentException if the encoding fails.
   */
  public Date encode(final Temporal aTemporal) {
    return TemporalConverter.applyByType(aTemporal, localDateConversion(),
        offsetDateTimeConversion(), zonedDateTimeConversion());
  }

  /**
   * Encodes a temporal data set into an iCal4J date.
   * @param temporals the temporal data set to encode.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public DateList encode(final Collection<? extends Temporal> temporals) {
    return temporals.stream().map(this::encode).sorted().collect(Collectors.toCollection(() -> {
      final DateList list = new DateList();
      list.setUtc(true);
      return list;
    }));
  }

  /**
   * Encodes a datetime into an iCal4J date set in UTC.
   * @param dateTime the datetime to encode.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public DateTime encode(final OffsetDateTime dateTime) {
    try {
      return new DateTime(DateTimeFormatter.ofPattern(ICAL_UTC_PATTERN)
          .format(dateTime.withOffsetSameInstant(ZoneOffset.UTC)));
    } catch (ParseException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Encodes a datetime into an iCal4J date that takes into account the time zone of the specified
   * datetime.
   * @param dateTime the datetime with timezone to encode.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public DateTime encode(final ZonedDateTime dateTime) {
    try {
      return new DateTime(DateTimeFormatter.ofPattern(ICAL_LOCAL_PATTERN).format(dateTime),
          getTimeZone(dateTime));
    } catch (ParseException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Encodes a date into an iCal4J date.
   * @param date a date.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public Date encode(final LocalDate date) {
    try {
      return new Date(DateTimeFormatter.ofPattern(ICAL_DATE_PATTERN).format(date));
    } catch (ParseException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Gets the conversion function of an {@link OffsetDateTime} instance to an iCal4J date object.
   * The function will throw a {@link SilverpeasRuntimeException} if the conversion fails.
   * @return a conversion of a {@link OffsetDateTime} to an iCal4J {@link DateTime} value.
   */
  public TemporalConverter.Conversion<OffsetDateTime, Date> offsetDateTimeConversion() {
    return TemporalConverter.Conversion.of(OffsetDateTime.class, this::encode);
  }

  /**
   * Gets the conversion function of a {@link ZonedDateTime} instance to an iCal4J date object. The
   * function will throw a {@link SilverpeasRuntimeException} if the conversion fails.
   * @return a conversion of a {@link ZonedDateTime} to an iCal4J {@link DateTime} value.
   */
  public TemporalConverter.Conversion<ZonedDateTime, Date> zonedDateTimeConversion() {
    return TemporalConverter.Conversion.of(ZonedDateTime.class, this::encode);
  }

  /**
   * Gets the conversion function of a {@link LocalDate} instance to an iCal4J date object. The
   * function will throw a {@link SilverpeasRuntimeException} if the conversion fails.
   * @return a conversion of a {@link LocalDate} to an iCal4J {@link Date} value.
   */
  public TemporalConverter.Conversion<LocalDate, Date> localDateConversion() {
    return TemporalConverter.Conversion.of(LocalDate.class, this::encode);
  }

  private TimeZone getTimeZone(final ZonedDateTime date) {
    return getTimeZone(date.getZone());
  }

  public TimeZone getTimeZone(final ZoneId zoneId) {
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    return registry.getTimeZone(zoneId.getId());
  }

  /**
   * Decodes an iCal4J Date or DateTime into a Temporal.
   * @param aDate the Date or DateTime to decode.
   * @return a temporal instance. If the temporal contains time data, then UTC timezone is set.
   * @throws SilverpeasRuntimeException if the decoding fails.
   */
  public final java.time.temporal.Temporal decode(final Date aDate) {
    if (aDate instanceof DateTime) {
      return OffsetDateTime.ofInstant(aDate.toInstant(), ZoneOffset.UTC);
    } else {
      return LocalDate.ofInstant(aDate.toInstant(), ZoneOffset.UTC);
    }
  }
}

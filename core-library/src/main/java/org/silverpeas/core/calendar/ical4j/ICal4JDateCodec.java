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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.ical4j;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.date.TimeZoneUtil;

import javax.inject.Singleton;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A decoder/encoder of iCal4J dates with Silverpeas dates.
 */
@Singleton
public class ICal4JDateCodec {

  static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
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
   * @param aTemporal the temporal data to encode which have to be extracted from the given component.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public Date encode(final boolean eventRecurrent, final CalendarComponent component,
      final java.time.temporal.Temporal aTemporal) {
    final java.time.temporal.Temporal temporal =
        isEventDateToBeEncodedIntoUtc(eventRecurrent, component) ? aTemporal :
            OffsetDateTime.from(aTemporal).atZoneSameInstant(component.getCalendar().getZoneId());
    return encode(temporal);
  }

  /**
   * Encodes a temporal data into an iCal4J date.
   * @param aTemporal the temporal data to encode.
   * @return an iCal4J date.
   * @throws IllegalArgumentException if the encoding fails.
   */
  public Date encode(final java.time.temporal.Temporal aTemporal) {
    return TemporalConverter.applyByType(aTemporal, this::encode, this::encode, this::encode);
  }

  /**
   * Encodes a temporal data set into an iCal4J date.
   * @param temporals the temporal data set to encode.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public DateList encode(final Collection<? extends java.time.temporal.Temporal> temporals) {
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
      return new DateTime(
          DateTimeFormatter.ofPattern(ICAL_LOCAL_PATTERN).format(dateTime), getTimeZone(dateTime));
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
      return new Date(
          DateTimeFormatter.ofPattern(ICAL_DATE_PATTERN).format(date));
    } catch (ParseException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
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
   * @param defaultZoneId the default zone id.
   * @return a temporal instance.
   * @throws SilverpeasRuntimeException if the decoding fails.
   */
  public final java.time.temporal.Temporal decode(final Date aDate, final ZoneId defaultZoneId) {
    if (aDate instanceof DateTime) {
      return decode((DateTime) aDate, defaultZoneId);
    } else {
      return decode(aDate);
    }
  }

  /**
   * Decodes a iCal4J DateTime into an OffsetDateTime set in UTC.
   * @param dateTime the datetime to decode.
   * @param defaultZoneId the default zone id.
   * @return an OffsetDateTime instance.
   * @throws SilverpeasRuntimeException if the decoding fails.
   */
  public OffsetDateTime decode(final DateTime dateTime, final ZoneId defaultZoneId) {
    boolean isUtc = dateTime.isUtc();
    TemporalAccessor temporalAccessor =
        DateTimeFormatter.ofPattern(isUtc ? ICAL_UTC_PATTERN : ICAL_LOCAL_PATTERN)
            .parse(dateTime.toString());
    ZoneId zoneId;
    if (dateTime.getTimeZone() != null) {
      zoneId = TimeZoneUtil.toZoneId(dateTime.getTimeZone().getID());
    } else {
      zoneId = isUtc ? ZoneOffset.UTC : defaultZoneId;
    }
    return LocalDateTime.from(temporalAccessor).atZone(zoneId).toOffsetDateTime()
        .withOffsetSameInstant(ZoneOffset.UTC);
  }

  /**
   * Decodes an iCal4J date into a LocalDate.
   * @param date a date to decode.
   * @return a LocalDate instance.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public LocalDate decode(final Date date) {
    TemporalAccessor temporalAccessor =
        DateTimeFormatter.ofPattern(ICAL_DATE_PATTERN).parse(date.toString());
    return LocalDate.from(temporalAccessor);
  }
}

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
import org.silverpeas.core.date.Temporal;

import javax.inject.Singleton;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A decoder/encoder of iCal4J dates with Silverpeas dates.
 */
@Singleton
public class ICal4JDateCodec {

  private static final String ICAL_UTC_PATTERN = "yyyyMMdd'T'HHmmss'Z'";
  private static final String ICAL_LOCAL_PATTERN = "yyyyMMdd'T'HHmmss";
  private static final String ICAL_DATE_PATTERN = "yyyyMMdd";

  /**
   * Encodes a Silverpeas date into an iCal4J date.
   * @param aDate the date to encode.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public Date encode(final Temporal<?> aDate) throws SilverpeasRuntimeException {
    return encode(aDate, false);
  }

  /**
   * Encodes a temporal data into an iCal4J date.
   * @param aTemporal the temporal data to encode.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public Date encode(final java.time.temporal.Temporal aTemporal)
      throws SilverpeasRuntimeException {
    if (aTemporal instanceof LocalDate) {
      return encode((LocalDate) aTemporal);
    } else if (aTemporal instanceof OffsetDateTime) {
      return encode((OffsetDateTime) aTemporal);
    } else if (aTemporal instanceof ZonedDateTime) {
      return encode((ZonedDateTime) aTemporal);
    }
    throw new IllegalArgumentException("the type of given temporal is not yet handled");
  }

  /**
   * Encodes a temporal data set into an iCal4J date.
   * @param temporals the temporal data set to encode.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public DateList encode(final Collection<? extends java.time.temporal.Temporal> temporals)
      throws SilverpeasRuntimeException {
    return temporals.stream().map(this::encode).sorted().collect(Collectors.toCollection(() -> {
      final DateList list = new DateList();
      list.setUtc(true);
      return list;
    }));
  }

  /**
   * Encodes a date time into an iCal4J date set in UTC.
   * @param dateTime the date time to encode.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public DateTime encode(final OffsetDateTime dateTime) throws SilverpeasRuntimeException {
    try {
      return new DateTime(DateTimeFormatter.ofPattern(ICAL_UTC_PATTERN)
          .format(dateTime.withOffsetSameInstant(ZoneOffset.UTC)));
    } catch (ParseException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Encodes a date time into an iCal4J date that takes into account the time zone of the specified
   * date time.
   * @param dateTime the date time with timezone to encode.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public DateTime encode(final ZonedDateTime dateTime) throws SilverpeasRuntimeException {
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
  public Date encode(final LocalDate date) throws SilverpeasRuntimeException {
    try {
      return new Date(
          DateTimeFormatter.ofPattern(ICAL_DATE_PATTERN).format(date));
    } catch (ParseException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Encodes the specified Silverpeas date into an iCal4J date set in UTC.
   * @param aDate the date to encode.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public Date encodeInUTC(final Temporal<?> aDate) throws SilverpeasRuntimeException {
    return encode(aDate, true);
  }

  /**
   * Encodes the specified Silverpeas date into an iCal4J date set or not in UTC according to the
   * specified UTC flag. If the UTC flag is positioned at false, then the encoded date is set in the
   * same timezone than the specified date to encode.
   * @param aDate the date to encode.
   * @param inUTC the UTC flag indicating whether the iCal4J date must be set in UTC. If false, the
   * encoded date will be in the same timezone than the specified date.
   * @return an iCal4J date.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public Date encode(final Temporal<?> aDate, boolean inUTC) throws SilverpeasRuntimeException {
    Date iCal4JDate = null;
    try {
      if (aDate.isTimeSupported()) {
        if (inUTC) {
          iCal4JDate = new DateTime(aDate.toICalInUTC());
        } else {
          iCal4JDate = new DateTime(aDate.toICal());
          ((DateTime) iCal4JDate).setTimeZone(getTimeZone(aDate));
        }
      } else if (!aDate.isTimeSupported()) {
        iCal4JDate = new Date(aDate.toICal());
      }
    } catch (ParseException ex) {

      throw new SilverpeasRuntimeException(ex.getMessage(), ex);
    }
    return iCal4JDate;
  }

  private TimeZone getTimeZone(final Temporal<?> date) {
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    return registry.getTimeZone(date.getTimeZone().getID());
  }

  private TimeZone getTimeZone(final ZonedDateTime date) {
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    return registry.getTimeZone(date.getZone().getId());
  }
}

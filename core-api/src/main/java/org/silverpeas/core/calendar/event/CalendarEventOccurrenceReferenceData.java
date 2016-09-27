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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.calendar.event;

import org.silverpeas.core.date.Period;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;

import static org.silverpeas.core.calendar.event.CalendarEventOccurrence.getLastStartDateTimeFrom;

/**
 * This class represents the reference data of an occurrence of an event.
 * It provides methods which permit to identify an occurrence, to get its period and also to know
 * the time zone of the user or the system.
 * @author Yohann Chastagnier
 */
public class CalendarEventOccurrenceReferenceData {

  private String occurrenceId;
  private Period period;
  private ZoneOffset zoneOffset =
      ZoneOffset.ofTotalSeconds(TimeZone.getDefault().getRawOffset() / 1000);

  /**
   * Hidden constructor.
   * @param occurrenceId the identifier of a {@link CalendarEventOccurrence}.
   */
  private CalendarEventOccurrenceReferenceData(final String occurrenceId) {
    this.occurrenceId = occurrenceId;
  }

  /**
   * Initialize an instance from the identifier of an event occurrence.
   * @param occurrenceId identifier of an event occurrence.
   * @return the initialized instance.
   */
  public static CalendarEventOccurrenceReferenceData fromOccurrenceId(final String occurrenceId) {
    return new CalendarEventOccurrenceReferenceData(occurrenceId);
  }

  /**
   * Sets the period of the time window of the occurrence.
   * @param period a period.
   * @param zoneOffset the zone offset of the location of the user or system behind the action.
   * @return itself.
   */
  public CalendarEventOccurrenceReferenceData withPeriod(Period period, ZoneOffset zoneOffset) {
    this.period = period;
    this.zoneOffset = zoneOffset;
    return this;
  }

  /**
   * Gets the period.
   * @return a period.
   */
  public Period getPeriod() {
    return period;
  }

  /**
   * Gets the start date time the occurrence has before changing eventually its period.
   * @return an offset date time.
   */
  public OffsetDateTime getOriginalStartDateTime() {
    return getLastStartDateTimeFrom(occurrenceId);
  }

  /**
   * Gets the start day time of the occurrence according to {@link #getOriginalStartDateTime()}.
   * @return an offset date time.
   */
  public OffsetDateTime getOriginalStartDayTime() {
    return getOriginalStartDateTime().withOffsetSameInstant(zoneOffset).withHour(0).withMinute(0)
        .withOffsetSameInstant(ZoneOffset.UTC);
  }

  /**
   * Indicates if the given occurrence concerns the one specified by the data of the instance.
   * @param occurrence the occurrence to verify.
   * @return true if the given occurrence concerns this data, false otherwise.
   */
  public boolean concerns(final CalendarEventOccurrence occurrence) {
    return occurrenceId.equals(occurrence.getId());
  }
}

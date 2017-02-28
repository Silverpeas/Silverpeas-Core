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

package org.silverpeas.core.calendar;

import org.silverpeas.core.date.Period;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

/**
 * This class represents the reference to an occurrence of a calendar event.
 * It provides methods which permit to identify an occurrence, to get its period and also to know
 * the time zone of the user or the system.
 * @author Yohann Chastagnier
 * @deprecated
 * TODO CALENDAR remove this class once the transfer to the new code is achieved
 */
public class CalendarEventOccurrenceReference {

  private String occurrenceId;
  private Period period;
  private CalendarEventOccurrence occurrence;

  /**
   * Hidden constructor.
   * @param occurrenceId the identifier of a {@link CalendarEventOccurrence}.
   */
  private CalendarEventOccurrenceReference(final String occurrenceId) {
    this.occurrenceId = occurrenceId;
  }

  /**
   * Hidden constructor.
   * @param occurrence an instance of {@link CalendarEventOccurrence}.
   */
  private CalendarEventOccurrenceReference(final CalendarEventOccurrence occurrence) {
    this.occurrence = occurrence;
    this.occurrenceId = occurrence.getId();
    this.period = occurrence.getPeriod();
  }

  /**
   * Initialize an instance from the identifier of an event occurrence.
   * @param occurrenceId identifier of an event occurrence.
   * @return the initialized instance.
   */
  public static CalendarEventOccurrenceReference fromOccurrenceId(final String occurrenceId) {
    return new CalendarEventOccurrenceReference(occurrenceId);
  }

  /**
   * Initialize an instance from the identifier of an event occurrence.
   * @param occurrence an occurrence of a calendar event.
   * @return the initialized instance.
   */
  public static CalendarEventOccurrenceReference fromOccurrence(
      final CalendarEventOccurrence occurrence) {
    return new CalendarEventOccurrenceReference(occurrence);
  }

  /**
   * Sets the period of the time window of the occurrence.
   * @param period a period.
   * @return itself.
   */
  public CalendarEventOccurrenceReference withPeriod(Period period) {
    this.period = period;
    return this;
  }

  /**
   * Gets the actual period of this occurrence. This period can be an updated one.
   * @return a period.
   */
  public Period getPeriod() {
    return period;
  }

  /**
   * Gets the start date (and time if not on all day) the occurrence has before changing
   * eventually its period.
   * @return a temporal of type LocalDate or OffsetDateTime.
   */
  public Temporal getOriginalStartDate() {
    if (this.occurrence != null) {
      return this.occurrence.getOriginalStartDate();
    }
    String temporal = this.occurrenceId.split("@")[1];
    if (temporal.contains("T")) {
      return OffsetDateTime.parse(temporal);
    } else {
      return LocalDate.parse(temporal);
    }
  }

  /**
   * Indicates if this reference is about the specified occurrence.
   * @param occurrence the occurrence to verify.
   * @return true if the given occurrence is referred by this reference, false otherwise.
   */
  public boolean refers(final CalendarEventOccurrence occurrence) {
    return occurrenceId.equals(occurrence.getId());
  }
}

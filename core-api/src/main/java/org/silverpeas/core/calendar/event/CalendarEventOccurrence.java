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

package org.silverpeas.core.calendar.event;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * The occurrence of an event in a Silverpeas calendar. An event occurrence starts and ends at a
 * given date and it represents an event that has occurred or that is going to occur at a given
 * moment in time.
 */
public class CalendarEventOccurrence {

  private int id;
  private OffsetDateTime startDateTime;
  private OffsetDateTime endDateTime;
  private CalendarEvent event;

  /**
   * Constructs a new occurrence from the specified calendar event, starting and ending at the
   * specified dates.
   * @param event the event from which the occurrence is instantiated.
   * @param startDateTime the start date and time of the occurrence.
   * @param endDateTime the end date and time of the occurrence.
   */
  public CalendarEventOccurrence(final CalendarEvent event, final OffsetDateTime startDateTime,
      final OffsetDateTime endDateTime) {
    this.id = new HashCodeBuilder().append(event.getId()).append(startDateTime).toHashCode();
    this.event = event;
    this.startDateTime = startDateTime.withOffsetSameInstant(ZoneOffset.UTC);
    this.endDateTime = endDateTime.withOffsetSameInstant(ZoneOffset.UTC);
  }

  /**
   * Gets the event to which this occurrence belongs.
   * @return the event from which this occurrence is instanciated.
   */
  public CalendarEvent getCalendarEvent() {
    return this.event;
  }

  /**
   * Gets the date and time at which this occurrence should starts
   * @return the start date of the event occurrence.
   */
  public OffsetDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Gets the date at which this event should ends
   * @return the end date of the event occurrence.
   */
  public OffsetDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Gets the unique identifier of this occurrence.
   * @return
   */
  public String getId() {
    return String.valueOf(id);
  }

  /**
   * Deletes this event occurrence.
   * <ul>
   * <li>If the occurrence is the single one of the event, then the event is deleted.</li>
   * <li>If the occurrence is one of among any of an event, then the date time at which this
   * occurrence starts is added as an exception in the recurrence rule of the event.</li>
   * <li>If the occurrence is the last one of the event, then the event is deleted.</li>
   * </ul>
   */
  public void delete() {
    getCalendarEvent().getCalendar().getPlannedEvents().getOccurrences().remove(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CalendarEventOccurrence)) {
      return false;
    }

    final CalendarEventOccurrence that = (CalendarEventOccurrence) o;

    return id == that.id;

  }

  @Override
  public int hashCode() {
    return id;
  }
}

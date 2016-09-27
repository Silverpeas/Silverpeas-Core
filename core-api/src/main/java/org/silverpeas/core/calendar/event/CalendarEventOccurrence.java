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
package org.silverpeas.core.calendar.event;

import org.silverpeas.core.calendar.CalendarTimeWindow;
import org.silverpeas.core.date.Period;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;


/**
 * The occurrence of an event in a Silverpeas calendar. An event occurrence starts and ends at a
 * given date and it represents an event that has occurred or that is going to occur at a given
 * moment in time.
 */
public class CalendarEventOccurrence {

  private String id;
  private Period period;
  private CalendarEvent event;

  /**
   * Hidden constructor.
   */
  private CalendarEventOccurrence() {
  }

  /**
   * Constructs a new occurrence from the specified calendar event, starting and ending at the
   * specified dates.
   * @param event the event from which the occurrence is instantiated.
   * @param startDateTime the start date and time of the occurrence.
   * @param endDateTime the end date and time of the occurrence.
   */
  CalendarEventOccurrence(final CalendarEvent event, final OffsetDateTime startDateTime,
      final OffsetDateTime endDateTime) {
    this.id = event.getId() + "@" + startDateTime;
    this.event = event;
    this.period = Period.between(startDateTime, endDateTime);
  }

  private static CalendarEventOccurrenceGenerator generator() {
    return CalendarEventOccurrenceGenerator.get();
  }

  /**
   * Gets the event occurrences that occur in the specified window of time of a given calendar.
   * @param timeWindow a window of time of a calendar.
   * @return a list of event occurrences or an empty list if there is no occurrences of events
   * in the specified window of time.
   */
  public static List<CalendarEventOccurrence> getOccurrencesIn(
      final CalendarTimeWindow timeWindow) {
    return generator().generateOccurrencesIn(timeWindow);
  }

  /**
   * Gets the date and time from an occurrence identifier.
   * @param occurrenceId an occurrence identifier.
   * @return the start date of the event occurrence before any recent change.
   */
  static OffsetDateTime getLastStartDateTimeFrom(String occurrenceId) {
    return OffsetDateTime.parse(occurrenceId.split("@")[1]);
  }

  /**
   * Gets the event from which this occurrence was spawned.
   *
   * From the returned event, the title, the description or any other event properties can be
   * modified. Nevertheless, the change can be effective only by invoking the {@code update} method
   * of either the {@link CalendarEvent} to apply the modifications to all occurrences or this
   * occurrence to apply the modifications only to this occurrence. Only the period at which the
   * event occur in the calendar cannot be used to update this occurrence. For doing, please use
   * either the {@code setPeriod} or the {@code setDay} method of {@link CalendarEventOccurrence}.
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
    return period.getStartDateTime();
  }

  /**
   * Gets the date at which this event should ends
   * @return the end date of the event occurrence.
   */
  public OffsetDateTime getEndDateTime() {
    return period.getEndDateTime();
  }

  /**
   * Gets the unique identifier of this occurrence.
   * @return the unique identifier of this occurrence.
   */
  public String getId() {
    return String.valueOf(id);
  }

  /**
   * Gets the date and time at which this occurrence originally starts before any changes.
   * @return the start date of the event occurrence before any recent change.
   */
  public OffsetDateTime getLastStartDateTime() {
    return getLastStartDateTimeFrom(getId());
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
    return id.equals(that.id);

  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  /**
   * Changes the planning of this occurrence in the calendar. The change will be effectively
   * performed once the {@code update} method invoked.
   * @param newPeriod a new period of time on which this occurrence will occur or has actually
   * occurred.
   */
  public void setPeriod(final Period newPeriod) {
    this.period = newPeriod;
  }

  /**
   * Gets the period of this occurrence in the calendar.
   * @return a period.
   */
  public Period getPeriod() {
    return period;
  }

  /**
   * Changes the planning of this occurrence in the calendar. The change will be effectively
   * performed once the {@code update} method invoked.
   * @param newDay the new day at which this occurrence will occur or has actually occurred.
   */
  public void setDay(final LocalDate newDay) {
    Period newPeriod = Period.between(newDay, newDay);
    setPeriod(newPeriod);
  }
}

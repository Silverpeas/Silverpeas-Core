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

import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.repository.OperationContext;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

/**
 * A collection of {@link CalendarEvent} instances planned in a given calendar. Each calendar has
 * its own collection of planned events in which they are indexed by their identifier; a
 * planned event is ensured to be unique both by its identifier and by the calendar it belongs to.
 * A {@link PlannedCalendarEvents} is always related to a given calendar.
 *
 * Adding an event into this collection means planning it in the underlying calendar. The planning
 * causes the events to be persisted in the data source. Removing an event from this collection
 * means unplanning it from the underlying calendar, causing its deletion in the date source.
 *
 * Each event get from this collection is, by default, detached from any persistence context and
 * hence any change requires a call to the {@code update} method to be effectively reported to the
 * actual event planned in the calendar.
 * @author mmoquillon
 */
public class PlannedCalendarEvents {

  private Calendar calendar;
  private CalendarEventRepository repository;
  private PlannedEventOccurrences occurrences;

  /**
   * Gets the calendar to which this collection is related.
   * @return the underlying calendar.
   */
  public Calendar getCalendar() {
    return this.calendar;
  }

  /**
   * Gets either the calendar event with the specified identifier or nothing if no
   * such event exists with the given identifier.
   * @param eventId the unique identifier of the event to get.
   * @return optionally an event with the specified identifier.
   */
  public Optional<CalendarEvent> get(String eventId) {
    return Optional.ofNullable(repository.getById(eventId));
  }

  /**
   * Adds the specified event. Adding this event in this collection is like to plan it in the
   * underlying calendar.
   * @param event the event to add, hence to persist, into the calendar.
   * @return the event once successfully persisted and indexed into the store.
   */
  public CalendarEvent add(final CalendarEvent event) {
    return Transaction.getTransaction().perform(() -> {
      event.setCalendar(getCalendar());
      return repository.save(OperationContext.fromUser(event.getCreator()), event);
    });
  }

  /**
   * Removes the specified event. Removing it is like to unplan it in the underlying calendar;
   * it is deleted in the data source. By doing it the event won't belong
   * anymore to the underlying calendar.
   * @param event the unique identifier of the event to delete.
   */
  public void remove(final CalendarEvent event) {
    Transaction.getTransaction().perform(() -> {
      repository.delete(event);
      return null;
    });
  }

  /**
   * Updates the event in the calendar with the specified one.
   * @param event the event from which to update its equivalent event in the calendar.
   */
  public void update(final CalendarEvent event) {
    Transaction.getTransaction().perform(() -> {
      repository.save(OperationContext.fromUser(event.getLastUpdater()), event);
      return null;
    });
  }

  /**
   * Is this collection is empty?
   * @return true if there is no events planned in the underlying calendar. Otherwise returns false.
   */
  public boolean isEmpty() {
    return repository.size() == 0;
  }

  /**
   * Clears this collection of all of the planned events.
   */
  public void clear() {
    Transaction.getTransaction().perform(() -> {
      repository.deleteAll();
      return null;
    });
  }

  /**
   * Gets the collection of occurrences of the planned events in this collection.
   * @return a collection of planned event occurrences.
   */
  public PlannedEventOccurrences getOccurrences() {
    return this.occurrences;
  }

  protected PlannedCalendarEvents(final Calendar calendar) {
    this.calendar = calendar;
    this.repository = CalendarEventRepository.getFor(calendar);
    this.occurrences =  new PlannedEventOccurrences(repository);
  }
}

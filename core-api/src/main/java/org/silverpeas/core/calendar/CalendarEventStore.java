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
package org.silverpeas.core.calendar;

import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.repository.OperationContext;

import java.util.Optional;

/**
 * A store of persisted {@link CalendarEvent} instances for a given calendar. Each calendar has
 * its own store of events in which each event is indexed by their identifier; a persisted
 * event is ensured to be unique both by its identifier and by the calendar it belongs to. A store
 * uses the persistence repository for events to store and retrieve the events which it is in
 * charge; the persistence repository is shared by all the stores.
 *
 * Each event get from this store is a shadow copy of a persisted event and thus any changes to
 * the event requires a call to the {@code update} method to be effectively reported to the
 * persisted event.
 * @author mmoquillon
 */
public class CalendarEventStore {

  private Calendar calendar;
  private CalendarEventRepository repository = CalendarEventRepository.get();

  /**
   * Gets either the calendar event with the specified identifier from this store or nothing if no
   * such event exists with the given identifier.
   * @param eventId the unique identifier of the event to get.
   * @return optionally an event with the specified identifier.
   */
  public Optional<CalendarEvent> get(String eventId) {
    return repository.getById(calendar, eventId);
  }

  /**
   * Adds the specified event into this store of events. Adding this event will persist it
   * into the calendar of this store. By doing it, the event will belong to the underlying calendar.
   * @param event the event to add, hence to persist, into the calendar.
   * @return the event once successfully persisted and indexed into the store.
   */
  public CalendarEvent add(final CalendarEvent event) {
    return Transaction.getTransaction().perform(() -> {
      event.setCalendar(calendar);
      return repository.save(OperationContext.fromUser(event.getCreator()), event);
    });
  }

  /**
   * Removes the specified event from the store of events. Removing it from the stores deletes it
   * in the data source; it is no more persisted. By doing it the event won't belong
   * anymore to the underlying calendar.
   * @param eventId the unique identifier of the event to delete.
   */
  public void remove(final String eventId) {
    Transaction.getTransaction().perform(() -> {
      repository.deleteById(eventId);
      return null;
    });
  }

  protected CalendarEventStore(final Calendar calendar) {
    this.calendar = calendar;
  }

}

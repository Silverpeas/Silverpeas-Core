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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.repository;

import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.datasource.repository.EntityRepository;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;

/**
 * A repository to persist occurrences of recurrent calendar events when they have changed from the
 * event or from their planning in the timeline of a calendar.
 * @author mmoquillon
 */
public interface CalendarEventOccurrenceRepository
    extends EntityRepository<CalendarEventOccurrence> {

  /**
   * Gets an instance of the implementation of a {@link CalendarEventOccurrenceRepository}.
   * @return a persistence repository of event occurrences.
   */
  static CalendarEventOccurrenceRepository get() {
    return ServiceProvider.getService(CalendarEventOccurrenceRepository.class);
  }

  /**
   * Gets all the persisted occurrences of the specified event.
   * @return a list of the persisted occurrences of the given event. If no occurrences, then an
   * empty list is returned.
   */
  List<CalendarEventOccurrence> getAllByEvent(final CalendarEvent event);

  /**
   * Gets all the persisted occurrences of the specified events occurring in the specified period
   * of time.
   * @param period the period of time into which the instances of the event should occur.
   * @return a list of the persisted occurrences of the given event and occurring in the given
   * period of time. If no occurrences of the events and in the given period of time were persisted,
   * then an empty list is returned.
   */
  List<CalendarEventOccurrence> getAll(final Collection<CalendarEvent> events, final Period period);

  /**
   * Gets all the persisted occurrences of a given event since and including the specified one.
   * @param occurrence the occurrence since which all the others occurrences, including iself, have
   * to be get.
   * @return a list of persisted occurrences since and including the specified one. All of them are
   * from the same event.
   */
  List<CalendarEventOccurrence> getAllSince(final CalendarEventOccurrence occurrence);
}
  
/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.calendar.repository;

import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventFilter;
import org.silverpeas.core.persistence.datasource.repository.EntityRepository;
import org.silverpeas.core.util.ServiceProvider;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * A persistence repository of calendar events. A calendar event is always persisted for a given
 * calendar; a calendar event is always related to an existing calendar.
 * @author Yohann Chastagnier
 */
public interface CalendarEventRepository extends EntityRepository<CalendarEvent> {

  static CalendarEventRepository get() {
    return ServiceProvider.getSingleton(CalendarEventRepository.class);
  }

  /**
   * Gets an event by its external identifier which is unique into context of a calendar.
   * @param calendar the calendar to search into.
   * @param externalId the external identifier as string.
   * @return the calendar event if any, null otherwise.
   */
  CalendarEvent getByExternalId(final Calendar calendar, final String externalId);

  /**
   * Gets all the events that satisfies the specified filter<br>
   * Please be careful to always close the streams in order to avoid memory leaks!!!
   * <pre>
   * {@code
   *   try(Stream<CalendarEvent> event : streamAll(myFilter)) {
   *     // Performing the treatment
   *   }
   * }
   * </pre>
   * @param filter a filter to apply on the calendar events to return. The filter can be empty and
   * then no filtering will be applied on the requested calendar events.
   * @return the events as a stream.
   */
  Stream<CalendarEvent> streamAll(final CalendarEventFilter filter);

  /**
   * Deletes all the events that belongs to the specified calendar.
   * @param calendar the calendar for which all the events must be deleted.
   */
  void deleteAll(final Calendar calendar);

  /**
   * Gets size in events in the repository for the specified calendar.
   * @param calendar the calendar for which all the events must be counted.
   * @return the count of events in the given calendar.
   */
  long size(final Calendar calendar);

  /**
   * Gets all the events matching the specified filter and that occur between the two specified date
   * and times.
   * @param filter a filter to apply on the calendar events to return. The filter can be empty and
   * then no filtering will be applied on the requested calendar events.
   * @param startDateTime the inclusive datetime in UTC/Greenwich at which begins the period
   * in which the events are get.
   * @param endDateTime the inclusive datetime in UTC/Greenwich at which ends the period in
   * which the events are get.
   * @return a list of events filtering by the given filter and that occur between the two date
   * times or an empty list if there is no events matching the specified arguments.
   */
  List<CalendarEvent> getAllBetween(CalendarEventFilter filter, OffsetDateTime startDateTime,
      OffsetDateTime endDateTime);

}

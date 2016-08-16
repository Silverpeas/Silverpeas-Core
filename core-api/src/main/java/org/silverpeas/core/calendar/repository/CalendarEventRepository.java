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

package org.silverpeas.core.calendar.repository;

import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.repository.SilverpeasEntityRepository;
import org.silverpeas.core.util.ServiceProvider;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * A persistence repository of calendar events. A calendar event is always persisted for a given
 * calendar; a calendar event is always related to an existing calendar.
 * @author Yohann Chastagnier
 */
public interface CalendarEventRepository
    extends SilverpeasEntityRepository<CalendarEvent, UuidIdentifier> {

  /**
   * Gets the repository of events that belongs to the specified calendar.
   * @param calendar a persisted calendar. If the given calendar isn't persisted then an
   * {@link IllegalArgumentException} is thrown.
   * @return an instance of the {@link CalendarEventRepository} implementation.
   */
  static CalendarEventRepository getFor(Calendar calendar) {
    CalendarEventRepository repository = ServiceProvider.getService(CalendarEventRepository.class);
    repository.setCalendar(calendar);
    return repository;
  }

  /**
   * Sets the calendar instance to which all the events handled by this repository have to be
   * related. This will set the calendar as a filter for all the repository's queries on the events.
   * If the calendar isn't persisted, then an {@link IllegalArgumentException} is thrown.
   * @param calendar a calendar instance.
   */
  void setCalendar(final Calendar calendar);

  /**
   * Deletes all the events that belongs to the underlying calendar.
   */
  void deleteAll();

  /**
   * Gets size in events in the repository for the underlying calendar.
   * @return the count of events in the underlying calendar.
   */
  long size();

  /**
   * Gets all the events belonging to the underlying calendar that occur between the two specified
   * date and times.
   * @param startDateTime the inclusive date and time in UTC/Greenwich at which begins the period in
   * which the events are get.
   * @param endDateTime the inclusive date and time in UTC/Greenwich at which ends the period in
   * which the events are get.
   * @return a list of events that occur between the two date times or an empty list if there
   * is no events in the specified calendar between the two date times.
   */
  List<CalendarEvent> getAllBetween(OffsetDateTime startDateTime, OffsetDateTime endDateTime);

}

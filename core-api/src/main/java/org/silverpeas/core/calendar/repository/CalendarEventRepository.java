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

  static CalendarEventRepository get() {
    return ServiceProvider.getService(CalendarEventRepository.class);
  }

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
   * Gets all the events belonging to the specified calendar that occur between the two specified
   * date and times.
   * @param calendar the calendar with with events must be linked to.
   * @param startDateTime the inclusive date and time in UTC/Greenwich at which begins the period in
   * which the events are get.
   * @param endDateTime the inclusive date and time in UTC/Greenwich at which ends the period in
   * which the events are get.
   * @return a list of events that occur between the two date times or an empty list if there
   * is no events in the specified calendar between the two date times.
   */
  List<CalendarEvent> getAllBetween(final Calendar calendar, OffsetDateTime startDateTime,
      OffsetDateTime endDateTime);

}

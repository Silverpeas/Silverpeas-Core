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

import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.repository.SilverpeasEntityRepository;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
   * Gets from the specified calendar the event having the specified identifier or nothing if no
   * such event exists into the calendar.
   * This method replace the getById(String id) one.
   * @param calendar a calendar.
   * @param id the unique identifier of the event to get
   * @return optionally the calendar event matching the specified identifier.
   */
  Optional<CalendarEvent> getById(Calendar calendar, String id);

  /**
   * Gets from the specified calendar all the event whose identifier matches the specified ones or
   * nothing if no such events exist into the calendar.
   * This method replace the getById(String ...ids) one.
   * @param calendar a calendar.
   * @param ids one or several unique identifiers of calendar events.
   * @return a list of events or an empty list if there is no events with the specified identifiers.
   */
  List<CalendarEvent> getById(Calendar calendar, String ...ids);

  /**
   * Gets from the specified calendar all the event whose identifier matches the specified ones or
   * nothing if no such events exist into the calendar.
   * This method replace the getById(Collection<String> id) one.
   * @param calendar a calendar.
   * @param ids one or several unique identifiers of calendar events.
   * @return a list of events or an empty list if there is no events with the specified identifiers.
   */
  List<CalendarEvent> getById(Calendar calendar, Collection<String> ids);

  /**
   * Throws a {@link SilverpeasRuntimeException}
   * Uses instead {@code getById(Calendar calendar, String id);}
   */
  @Override
  default CalendarEvent getById(String id) {
    throw new NotSupportedException("getById(String id) not supported");
  }

  /**
   * Throws a {@link SilverpeasRuntimeException}
   * Uses instead {@code getById(Calendar calendar, String... ids);}
   */
  @Override
  default List<CalendarEvent> getById(String... ids) {
    throw new NotSupportedException("getById(String... ids) not supported");
  }

  /**
   * Throws a {@link SilverpeasRuntimeException}
   * Uses instead {@code getById(Calendar calendar, Collection<String> ids);}
   */
  @Override
  default List<CalendarEvent> getById(Collection<String> ids) {
    throw new NotSupportedException("getById(Collection<String> ids) not supported");
  }

  /**
   * Finds calendar events according to the given criteria.
   * @param criteria the calendar event criteria.
   * @return the calendar event list corresponding to the given criteria.
   */
  //List<CalendarEvent> findByCriteria(final CalendarEventCriteria criteria);
}

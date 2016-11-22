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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEventFilter;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;

import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yohann Chastagnier
 */
@Singleton
public class DefaultCalendarEventRepository extends SilverpeasJpaEntityRepository<CalendarEvent>
    implements CalendarEventRepository {

  @Override
  public long size(final Calendar calendar) {
    NamedParameters params = newNamedParameters()
        .add("calendar", calendar);
    return getFromNamedQuery("calendarEventCount", params, Long.class);
  }

  @Override
  public List<CalendarEvent> getAllBetween(final CalendarEventFilter filter,
      final OffsetDateTime startDateTime, final OffsetDateTime endDateTime) {
    String namedQuery = "calendarEvents";
    NamedParameters parameters = newNamedParameters();
    if (!filter.getCalendars().isEmpty()) {
      parameters.add("calendars", filter.getCalendars());
      namedQuery += "ByCalendar";
    }
    if (!filter.getParticipants().isEmpty()) {
      parameters.add("participantIds",
          filter.getParticipants().stream().map(User::getId).collect(Collectors.toList()));
      namedQuery += "ByParticipants";
    }

    namedQuery += "ByPeriod";
    parameters.add("startDateTime", startDateTime).add("endDateTime", endDateTime);
    return findByNamedQuery(namedQuery, parameters);
  }

  @Override
  public void deleteAll(final Calendar calendar) {
    NamedParameters params = newNamedParameters().add("calendar", calendar);
    deleteFromNamedQuery("calendarEventsDeleteAll", params);
  }
}

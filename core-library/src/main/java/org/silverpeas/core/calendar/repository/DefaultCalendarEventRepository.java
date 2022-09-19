/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventFilter;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Yohann Chastagnier
 */
@Repository
public class DefaultCalendarEventRepository extends BasicJpaEntityRepository<CalendarEvent>
    implements CalendarEventRepository {

  private static final String CALENDARS_PARAMETER = "calendars";
  private static final String CALENDAR_PARAM = "calendar";
  private static final String CALENDAR_PARAMETER = CALENDAR_PARAM;
  private static final String MOVE_CALENDAR_JPQL_PATTERN =
      "update CalendarComponent c " +
      "set c.calendar = :target, " +
          "c.lastUpdaterId = :lastUpdaterId, " +
          "c.lastUpdateDate = :lastUpdateDate " +
      "where c in (%s)";

  @Override
  public CalendarEvent getByExternalId(final Calendar calendar, final String externalId) {
    NamedParameters params =
        newNamedParameters().add(CALENDAR_PARAMETER, calendar).add("externalId", externalId);
    return getFromNamedQuery("calendarEventByCalendarAndExternalId", params);
  }

  @Override
  public Stream<CalendarEvent> streamAll(final CalendarEventFilter filter) {
    final String byParticipants = "ByParticipants";
    String namedQuery = "calendarEvents";
    NamedParameters parameters = newNamedParameters();
    if (!filter.getCalendars().isEmpty()) {
      parameters.add(CALENDARS_PARAMETER, filter.getCalendars());
      namedQuery += "ByCalendar";
    }
    if (!filter.getParticipants().isEmpty()) {
      parameters.add("participantIds",
          filter.getParticipants().stream().map(User::getId).collect(Collectors.toList()));
      namedQuery += byParticipants;
    }
    Optional<Instant> optionalSynchroDate = filter.getSynchronizationDateLimit();
    if (optionalSynchroDate.isPresent()) {
      parameters.add("synchronizationDateLimit", optionalSynchroDate.get());
      if (namedQuery.contains(byParticipants)) {
        throw new UnsupportedOperationException(
            "The filter on both participants and synchronization date isn't yet supported!");
      }
      namedQuery += "BeforeSynchronizationDate";
    }
    return streamByNamedQuery(namedQuery, parameters, Object[].class)
        .map(o -> (CalendarEvent) o[0]);
  }

  @Override
  public long size(final Calendar calendar) {
    NamedParameters params = newNamedParameters().add(CALENDAR_PARAM, calendar);
    return getFromNamedQuery("calendarEventCount", params, Long.class);
  }

  @Override
  public List<CalendarEvent> getAllBetween(final CalendarEventFilter filter,
      final Instant startDateTime, final Instant endDateTime) {
    String namedQuery = "calendarEvents";
    NamedParameters parameters = newNamedParameters();
    if (!filter.getCalendars().isEmpty()) {
      parameters.add(CALENDARS_PARAMETER, filter.getCalendars());
      namedQuery += "ByCalendar";
    }
    if (!filter.getParticipants().isEmpty()) {
      parameters.add("participantIds",
          filter.getParticipants().stream().map(User::getId).collect(Collectors.toList()));
      namedQuery += "ByParticipants";
    }

    namedQuery += "ByPeriod";
    parameters.add("startDateTime", startDateTime).add("endDateTime", endDateTime);
    return streamByNamedQuery(namedQuery, parameters, Object[].class)
        .map(o -> (CalendarEvent) o[0]).collect(Collectors.toList());
  }

  @Override
  public CalendarEvent moveToCalendar(final CalendarEvent event, final Calendar target) {
    final OperationContext fromCache = OperationContext.getFromCache();
    final NamedParameters params = newNamedParameters()
        .add("target", target)
        .add("event", event)
        .add("lastUpdaterId", fromCache.getUser().getId())
        .add("lastUpdateDate", new Timestamp(new Date().getTime()));
    final String updateEventQuery = String.format(MOVE_CALENDAR_JPQL_PATTERN,
        "select e.component from CalendarEvent e where e = :event");
    updateFromJpqlQuery(updateEventQuery, params);
    final String updateEventOccurrenceQuery = String.format(MOVE_CALENDAR_JPQL_PATTERN,
        "select o.component from CalendarEventOccurrence o where o.event = :event");
    updateFromJpqlQuery(updateEventOccurrenceQuery, params);
    getEntityManager().clear();
    return getById(event.getId());
  }

  @Override
  public void deleteAll(final Calendar calendar) {
    NamedParameters params = newNamedParameters().add(CALENDAR_PARAMETER, calendar);
    String idQuery =
        "select e.id.id from CalendarEvent e where e.component.calendar = :calendar";
    String eventBatchQuery = "select e from CalendarEvent e where e.id.id in :eventIds";
    List<String> ids = listFromJpqlString(idQuery, params, String.class);
    for(Collection<String> batchIds : split(ids)) {
      params = newNamedParameters().add("eventIds", batchIds);
      listFromJpqlString(eventBatchQuery, params).forEach(CalendarEvent::delete);
    }
  }
}

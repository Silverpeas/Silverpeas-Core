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
package org.silverpeas.core.calendar.repository;

import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.calendar.notification.AttendeeLifeCycleEventNotifier
    .notifyAttendees;

/**
 * @author mmoquillon
 */
@Singleton
public class DefaultCalendarEventOccurrenceRepository
    extends BasicJpaEntityRepository<CalendarEventOccurrence>
    implements CalendarEventOccurrenceRepository {

  private static final String EVENT_PARAM = "event";

  @Override
  public List<CalendarEventOccurrence> getAllByEvent(final CalendarEvent event) {
    NamedParameters parameters = newNamedParameters().add(EVENT_PARAM, event);
    return findByNamedQuery("occurrenceByEvent", parameters);
  }

  @Override
  public List<CalendarEventOccurrence> getAll(final Collection<CalendarEvent> events,
      final Period period) {
    if (events.isEmpty()) {
      return Collections.emptyList();
    }
    NamedParameters parameters = newNamedParameters().add("events", events)
        .add("startDateTime", Period.asOffsetDateTime(period.getStartDate()))
        .add("endDateTime", Period.asOffsetDateTime(period.getEndDate()));
    return findByNamedQuery("occurrenceByEventsAndByPeriod", parameters);
  }

  @Override
  public long deleteSince(final CalendarEventOccurrence occurrence, final boolean notify) {
    NamedParameters parameters =
        newNamedParameters().add(EVENT_PARAM, occurrence.getCalendarEvent())
            .add("date", Period.asOffsetDateTime(occurrence.getStartDate()));
    List<CalendarEventOccurrence> occurrences =
        findByNamedQuery("occurrenceByEventSince", parameters);
    occurrences.forEach(o -> {
      getEntityManager().remove(o);
      if (notify) {
        notifyAttendees(o, o.getAttendees(), null);
      }
    });
    return occurrences.size();
  }

  @Override
  public long deleteAllByEvent(final CalendarEvent event, final boolean notify) {
    List<CalendarEventOccurrence> occurrences = getAllByEvent(event);
    occurrences.forEach(o -> {
      getEntityManager().remove(o);
      if (notify) {
        notifyAttendees(o, o.getAttendees(), null);
      }
    });
    return occurrences.size();
  }
}
  
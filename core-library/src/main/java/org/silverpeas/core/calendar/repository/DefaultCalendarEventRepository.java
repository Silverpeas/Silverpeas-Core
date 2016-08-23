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
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityManager;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
public class DefaultCalendarEventRepository
    extends SilverpeasJpaEntityManager<CalendarEvent, UuidIdentifier>
    implements CalendarEventRepository {

  private Calendar calendar;

  @Override
  public CalendarEvent getById(final String id) {
    NamedParameters params = newNamedParameters()
        .add("id", convertToEntityIdentifier(id))
        .add("calendar", calendar);
    return findOneByNamedQuery("calendarEventById", params);
  }

  @Override
  public List<CalendarEvent> getById(final String... ids) {
    return getById(Arrays.asList(ids));
  }

  @Override
  public List<CalendarEvent> getById(final Collection<String> ids) {
    NamedParameters params = newNamedParameters()
        .add("ids", convertToEntityIdentifiers(ids))
        .add("calendar", calendar);
    return findByNamedQuery("calendarEventsByIds", params);
  }

  @Override
  public long size() {
    NamedParameters params = newNamedParameters()
        .add("calendar", calendar);
    return fromNamedQuery("calendarEventCount", params, Long.class);
  }

  @Override
  public List<CalendarEvent> getAllBetween(final OffsetDateTime startDateTime,
      final OffsetDateTime endDateTime) {
    /*Query query = getEntityManager().createNativeQuery(
        "select distinct * from sb_cal_event as e, sb_cal_recurrence as rec, " +
            "SB_Cal_Recurrence_DayOfWeek as rec_dow, SB_Cal_Recurrence_Exception as rec_exc where " +
            "(e.recurrenceId is null or (e.recurrenceId = rec.id and rec_exc.recurrenceId = rec.id and rec_dow.recurrenceId = rec.id)) and " +
            "e.calendarId = :calendar and ((e.startDate <= :startDateTime and e.endDate >= " +
            ":startDateTime) or (e.startDate >= :startDateTime and e.startDate <= :endDateTime) " +
            "or (e.endDate < :startDateTime and e.recurrenceId is not null and (rec.recur_endDate >= :startDateTime or rec" +
            ".recur_endDate is null))) order by e.startDate",
        CalendarEvent.class);
    query.setParameter("calendar", calendar.getId());
    query.setParameter("startDateTime", Date.from(startDateTime.toInstant()),
        TemporalType.TIMESTAMP);
    query.setParameter("endDateTime", Date.from(endDateTime.toInstant()), TemporalType.TIMESTAMP);
    return query.getResultList();*/
    NamedParameters params = newNamedParameters()
        .add("startDateTime", startDateTime)
        .add("endDateTime", endDateTime)
        .add("calendar", calendar);
    return findByNamedQuery("calendarEventsByPeriod", params);
  }

  @Override
  public void setCalendar(final Calendar calendar) {
    this.calendar = calendar;
  }

  @Override
  public void deleteAll() {
    NamedParameters params = newNamedParameters().add("calendar", calendar);
    deleteFromNamedQuery("calendarEventsDeleteAll", params);
  }

}

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

import edu.emory.mathcs.backport.java.util.Arrays;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityManager;

import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Yohann Chastagnier
 */
public class DefaultCalendarEventRepository
    extends SilverpeasJpaEntityManager<CalendarEvent, UuidIdentifier>
    implements CalendarEventRepository {
  @Override
  public Optional<CalendarEvent> getById(final Calendar calendar, final String id) {
    NamedParameters params = newNamedParameters()
        .add("id", convertToEntityIdentifier(id))
        .add("calendar", calendar);
    return Optional.ofNullable(findOneByNamedQuery("byId", params));
  }

  @Override
  public List<CalendarEvent> getById(final Calendar calendar, final String... ids) {
    return getById(calendar, Arrays.asList(ids));
  }

  @Override
  public List<CalendarEvent> getById(final Calendar calendar, final Collection<String> ids) {
    NamedParameters params = newNamedParameters()
        .add("ids", convertToEntityIdentifiers(ids))
        .add("calendar", calendar);
    return findByNamedQuery("byIds", params);
  }

  @Override
  public long size(final Calendar calendar) {
    NamedParameters params = newNamedParameters()
        .add("calendar", calendar);
    return fromNamedQuery("count", params, Long.class);
  }

  @Override
  public List<CalendarEvent> getAllBetween(final Calendar calendar,
      final OffsetDateTime startDateTime, final OffsetDateTime endDateTime) {
    NamedParameters params = newNamedParameters()
        .add("startDateTime", Date.from(startDateTime.toInstant()), TemporalType.TIMESTAMP)
        .add("endDateTime", Date.from(endDateTime.toInstant()), TemporalType.TIMESTAMP)
        .add("calendar", calendar);
    return findByNamedQuery("byPeriod", params);
  }

  /*@Override
  public List<CalendarEvent> findByCriteria(final CalendarEventCriteria criteria) {
    NamedParameters params = newNamedParameters();
    CalendarEventJPQLQueryBuilder queryBuilder = new CalendarEventJPQLQueryBuilder(params);
    criteria.processWith(queryBuilder);

    // Playing the query and returning the requested result
    return findByCriteria(queryBuilder.result());
  }*/
}

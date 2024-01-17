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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singleton;

/**
 * @author Yohann Chastagnier
 */
@Singleton
@Repository
public class DefaultCalendarRepository extends SilverpeasJpaEntityRepository<Calendar>
    implements CalendarRepository {

  @Override
  public List<Calendar> getByComponentInstanceId(final String componentInstanceId) {
    return getByComponentInstanceIds(singleton(componentInstanceId));
  }

  @Override
  public List<Calendar> getByComponentInstanceIds(final Collection<String> componentInstanceIds) {
    NamedParameters parameters = newNamedParameters();
    return findByNamedQuery("calendarsByComponentInstanceIds",
        parameters.add("componentInstanceIds", componentInstanceIds));
  }

  @Override
  public List<Calendar> getAllSynchronized() {
    return findByNamedQuery("synchronizedCalendars", newNamedParameters());
  }

  @Override
  public void delete(final List<Calendar> calendars) {
    deleteAllEventsIn(calendars);
    super.delete(calendars);
  }

  @Override
  public long deleteById(final Collection<String> calendarIds) {
    deleteAllEventsIn(getById(calendarIds));
    return super.deleteById(calendarIds);
  }

  @Override
  public long deleteByComponentInstanceId(final String componentInstanceId) {
    deleteAllEventsIn(getByComponentInstanceId(componentInstanceId));
    return super.deleteByComponentInstanceId(componentInstanceId);
  }

  private void deleteAllEventsIn(final List<Calendar> calendars) {
    CalendarEventRepository eventRepository = CalendarEventRepository.get();
    for (Calendar calendar: calendars) {
      eventRepository.deleteAll(calendar);
    }
  }
}

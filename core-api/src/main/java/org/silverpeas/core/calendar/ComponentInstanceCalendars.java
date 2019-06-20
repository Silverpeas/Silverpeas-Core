/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.calendar;

import org.silverpeas.core.calendar.repository.CalendarRepository;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Add some functionality set around a list of calendars linked to a {@link
 * org.silverpeas.core.admin.component.model.SilverpeasComponentInstance}.
 * @author silveryocha
 */
public class ComponentInstanceCalendars extends ArrayList<Calendar> {

  private static final Comparator<Calendar> CALENDAR_COMPARATOR_BY_CREATION_DATE_ASC =
      Comparator.comparing(SilverpeasJpaEntity::getCreationDate);

  /**
   * Hidden constructor.
   * @param calendars calendars of a component instance.
   */
  private ComponentInstanceCalendars(final List<Calendar> calendars) {
    super(calendars);
  }

  /**
   * Gets the calendars represented by the specified component instance.
   * <p>The calendar are sorted initially from older to newer</p>
   * @param instanceId the unique identifier identifying an instance of a Silverpeas
   * component.
   * @return a list containing the calendar instances which matched if any, empty list otherwise.
   * @see Calendar#getByComponentInstanceId(String)
   */
  static ComponentInstanceCalendars getByComponentInstanceId(String instanceId) {
    CalendarRepository calendarRepository = CalendarRepository.get();
    final ComponentInstanceCalendars list =
        new ComponentInstanceCalendars(calendarRepository.getByComponentInstanceId(instanceId));
    list.sort(CALENDAR_COMPARATOR_BY_CREATION_DATE_ASC);
    return list;
  }

  /**
   * Gets the main calendar linked to a component instance.
   * @return the optional main calendar.
   */
  public Optional<Calendar> getMainCalendar() {
    return stream().filter(Calendar::isMain).findFirst();
  }
}

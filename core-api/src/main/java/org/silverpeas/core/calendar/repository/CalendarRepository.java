/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.persistence.datasource.repository.EntityRepository;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;

/**
 * A persistence repository of calendars. It provides business methods to manage the calendars in
 * the Silverpeas data source.
 * @author Yohann Chastagnier
 */
public interface CalendarRepository extends EntityRepository<Calendar> {

  /**
   * Gets an instance of the implementation of a {@link CalendarRepository}.
   * @return a persistence repository of calendars.
   */
  static CalendarRepository get() {
    return ServiceProvider.getSingleton(CalendarRepository.class);
  }

  /**
   * Gets the calendars represented by the specified component instance.
   * @param componentInstanceId the unique identifier identifying an instance of a Silverpeas
   * component. For instance, the component can be a collaborative application or a personal one.
   * @return a list containing the calendar instances which matched if any, empty list otherwise.
   */
  List<Calendar> getByComponentInstanceId(String componentInstanceId);

  /**
   * Gets the calendars represented by the specified component instances.
   * @param componentInstanceIds the unique identifiers identifying instances of a Silverpeas
   * component. For instance, a component can be a collaborative application or a personal one.
   * @return a list containing the calendar instances which matched if any, empty list otherwise.
   */
  List<Calendar> getByComponentInstanceIds(Collection<String> componentInstanceIds);

  /**
   * Gets all the calendars in Silverpeas synchronized with an external one.
   * @return a list of synchronized calendars.
   */
  List<Calendar> getAllSynchronized();
}

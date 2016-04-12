/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.datereminder.persistence.service;

import java.util.Date;
import java.util.Collection;

import org.silverpeas.core.datereminder.exception.DateReminderException;
import org.silverpeas.core.datereminder.persistence.DateReminderDetail;
import org.silverpeas.core.datereminder.persistence.PersistentResourceDateReminder;
import org.silverpeas.core.persistence.EntityReference;

/**
 * A service on the persistent date reminder for Silverpeas entities. It wraps the mechanism to compute and
 * to retrieve a date reminder for a given resource handled in Silverpeas.
 *
 * @author Cécile Bonin
 */
public interface PersistentDateReminderService {


  /**
   * Gets the DateReminder of the resource referred by the specified {@link EntityReference}.
   * If no date reminder exists for the resource, then
   * {@link PersistentResourceDateReminder#NoneDateReminder} is returned.
   *
   * @param resource a reference to the resource for which a date reminder will be initialized.
   * @return either the date reminder associated with the specified resource
   * or NoneDateReminder if no such date reminder exists.
   */
  PersistentResourceDateReminder get(final EntityReference resource);

  /**
   * Create the DateReminder of the resource referred by the specified {@link EntityReference}.
   *
   * @param resource a reference to the resource for which a dateReminder will be set.
   * @param dateReminderDetail
   * @return the new date reminder created, associated with the specified resource
   * @throws DateReminderException if an unexpected error occurs while initializing a dateReminder.
   */
  PersistentResourceDateReminder create(EntityReference resource, DateReminderDetail dateReminderDetail) throws DateReminderException;

  /**
   * Set the DateReminder of the resource referred by the specified {@link EntityReference}.
   *
   * @param resource a reference to the resource for which a dateReminder will be set.
   * @param dateReminderDetail
   * @return the date reminder updated, associated with the specified resource
   * @throws DateReminderException if an unexpected error occurs while initializing a dateReminder.
   */
  PersistentResourceDateReminder set(EntityReference resource, DateReminderDetail dateReminderDetail) throws DateReminderException;

  /**
   * Removes quietly the DateReminder of the resource referred by the specified entity reference.
   *
   * @param resource the resource for which the dateReminder has to be removed.
   */
  void remove(final EntityReference resource);

  /**
   * Gets the DateReminder of the resource that mature.
   *
   * @param deadLine the date which must trigger the date reminder.
   * @return the list of date reminder that mature.
   */
  Collection<PersistentResourceDateReminder> listAllDateReminderMaturing(Date deadLine);

}
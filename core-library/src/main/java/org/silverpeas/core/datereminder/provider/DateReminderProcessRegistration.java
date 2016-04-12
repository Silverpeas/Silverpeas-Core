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
package org.silverpeas.core.datereminder.provider;

import org.silverpeas.core.util.MapUtil;
import org.silverpeas.core.persistence.EntityReference;
import org.silverpeas.core.datereminder.persistence.PersistentResourceDateReminder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registering <code>DateReminderProcess</code> instances. Registred
 * <code>DateReminderProcess</code> are used by
 * <code>DateReminderScheduler</code>.
 * @author Cécile Bonin
 * @see DateReminderProcess
 */
public class DateReminderProcessRegistration {

  /**
   * DateReminderProcess container
   */
  private static final Map<String, List<DateReminderProcess>> processes =
      new HashMap<String, List<DateReminderProcess>>();

  /**
   * Register a DateReminderProcess
   * @param resourceType
   * @param process
   */
  public static synchronized void register(final Class resourceType,
      final DateReminderProcess process) {
    MapUtil.putAddList(processes, EntityReference.getType(resourceType), process);
  }

  /**
   * Unregister a DateReminderProcess
   * @param resourceType
   * @param process
   */
  public static synchronized void unregister(final Class resourceType,
      final DateReminderProcess process) {
    MapUtil.removeValueList(processes, EntityReference.getType(resourceType), process);
  }

  /**
   * Get all processes for a given persistent date reminder
   * @param dateReminder
   * @return the list of DateReminderProcess
   */
  public static List<DateReminderProcess> getProcesses(
      PersistentResourceDateReminder dateReminder) {
    List<DateReminderProcess> processesOfType = processes.get(dateReminder.getResourceType());
    return processesOfType != null ? processesOfType : new ArrayList<>(0);
  }
}
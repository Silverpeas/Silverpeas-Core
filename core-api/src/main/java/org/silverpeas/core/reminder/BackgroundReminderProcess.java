/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.core.reminder;

import javax.inject.Named;

/**
 * <p>
 * Each scheduled reminder is linked to a process which must be performed when the reminder is
 * triggered.<br/>
 * The linked process must be an implementation of this interface.
 * </p>
 * <p>
 * Each implementation of this interface MUST observe the following convention of {@link Named}
 * annotation naming :
 * <br/><code>[PROCESS NAME][PROCESS NAME SUFFIX]</code><br/>
 * <code>CalendarEventUserNotificationReminderProcess</code>for example, where
 * <code>CalendarEventUserNotification</code> is the process name and
 * <code>ReminderProcess</code> the process name suffix.
 * </p>
 *
 * @author silveryocha
 */
public interface BackgroundReminderProcess {

  class Constants {

    /**
     * The predefined suffix that must compound the name of each implementation of this interface.
     * An implementation of this interface by a Silverpeas feature called {@code
     * CalendarEventUserNotification} for example MUST be named
     * <code>CalendarEventUserNotification[PROCESS_NAME_SUFFIX]</code> where PROCESS_NAME_SUFFIX
     * is the predefined suffix as defined below.
     */
    public static final String PROCESS_NAME_SUFFIX = "ReminderProcess";

    private Constants() {
    }
  }

  /**
   * Gets the {@link ReminderProcessName} instance representing the name of the
   * {@link BackgroundReminderProcess}.
   * @return the {@link ReminderProcessName} instance.
   */
  ReminderProcessName getName();

  /**
   * Performs the treatment of the process.
   * @param reminder a reminder.
   */
  void performWith(final Reminder reminder);
}

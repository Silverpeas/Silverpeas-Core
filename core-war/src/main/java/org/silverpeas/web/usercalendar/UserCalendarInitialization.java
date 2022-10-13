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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.usercalendar;

import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;

/**
 * Handles the initialization of a calendar linked to a user.
 * @author Yohann Chastagnier
 */
class UserCalendarInitialization {

  public static void initialize(final String userCalendarInstanceId) {
    new UserCalendarInitialization().init(userCalendarInstanceId);
  }

  private void init(final String userCalendarInstanceId) {
    PersonalComponentInstance.from(userCalendarInstanceId).ifPresent(i -> {
      final User user = i.getUser();

      Calendar userCalendar = Calendar.newMainCalendar(i);
      userCalendar.setZoneId(user.getUserPreferences().getZoneId());
      userCalendar.save();
    });
  }
}

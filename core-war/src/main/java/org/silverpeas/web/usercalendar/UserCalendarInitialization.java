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

package org.silverpeas.web.usercalendar;

import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * Handles the initialization of a calendar linked to a user.
 * @author Yohann Chastagnier
 */
class UserCalendarInitialization {

  public static void initialize(final String userCalendarInstanceId) {
    new UserCalendarInitialization().init(userCalendarInstanceId);
  }

  private void init(final String userCalendarInstanceId) {
    PersonalComponentInstance.from(userCalendarInstanceId).ifPresent(personalComponentInstance -> {
      final User user = personalComponentInstance.getUser();

      /*
      Creating the calendar
       */

      Calendar userCalendar = new Calendar(userCalendarInstanceId);
      userCalendar.setTitle(user.getDisplayedName());
      userCalendar.save();

      /*
      Migrating all the data
       */

      migrate(user, userCalendar);
    });
  }

  /**
   * Migrating the calendar data associated to a user from the old structure to the new one.<br/>
   * This method of migration has been chosen because of the high complexity of making migration
   * scripts whereas all functional services are already implemented on production instance.<br/>
   * TODO userCalendar After some production releases, this method should be deleted.
   */
  private void migrate(final User user, final Calendar userCalendar) {
    SilverLogger.getLogger(this)
        .info("migrating the personal calendar of user ''{0}'' ({1})", user.getId(),
            userCalendar.getId());

    // TODO userCalendar writing here the migration

    SilverLogger.getLogger(this)
        .info("migration of the personal calendar of user ''{0}'' ({1}) is terminated",
            user.getId(), userCalendar.getId());
  }
}

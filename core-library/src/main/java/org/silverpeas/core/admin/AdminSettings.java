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
package org.silverpeas.core.admin;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Handled the settings around the attachments.
 * @author silveryocha
 */
public class AdminSettings {

  private static SettingBundle settings = ResourceLocator
      .getSettingBundle("org.silverpeas.admin.admin");

  private AdminSettings() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Indicates if the automatic deletion of removed users is enabled.
   * @return true if enabled, false otherwise.
   */
  public static boolean isAutomaticDeletionOfRemovedUsersEnabled() {
    return isDefined(getDeletionOfRemovedUsersCron()) && getDeletionOfRemovedUsersDayDelay() > 0;
  }

  /**
   * Gets the cron of the JOB execution in charge of deleting the removed users.
   * @return cron as string, empty to deactivate the JOB.
   */
  public static String getDeletionOfRemovedUsersCron() {
    return settings.getString("DeleteRemovedUsersCron", "");
  }

  /**
   * Gets the delay in days after which a removed user can be deleted.
   * @return day delay as int, 0 to deactivate the automatic deletion of removed users.
   */
  public static int getDeletionOfRemovedUsersDayDelay() {
    return settings.getInteger("DeleteRemovedUsersDelay", 30);
  }
}

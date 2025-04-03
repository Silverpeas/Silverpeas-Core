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
package org.silverpeas.core.admin;

import org.silverpeas.kernel.bundle.SettingBundle;

import static org.silverpeas.kernel.bundle.ResourceLocator.getSettingBundle;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * Handled the settings around the attachments.
 * @author silveryocha
 */
public class AdminSettings {

  private static final SettingBundle settings = getSettingBundle("org.silverpeas.admin.admin");

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

  /**
   * Indicates if the automatic deletion of removed groups is enabled.
   * @return true if enabled, false otherwise.
   */
  public static boolean isAutomaticDeletionOfRemovedGroupsEnabled() {
    return isDefined(getDeletionOfRemovedGroupsCron()) && getDeletionOfRemovedGroupsDayDelay() > 0;
  }

  /**
   * Gets the cron of the JOB execution in charge of deleting the removed groups.
   * @return cron as string, empty to deactivate the JOB.
   */
  public static String getDeletionOfRemovedGroupsCron() {
    return settings.getString("DeleteRemovedGroupsCron", "");
  }

  /**
   * Gets the delay in days after which a removed group can be deleted.
   * @return day delay as int, 0 to deactivate the automatic deletion of removed groups.
   */
  public static int getDeletionOfRemovedGroupsDayDelay() {
    return settings.getInteger("DeleteRemovedGroupsDelay", 30);
  }

  /**
   * Gets the cron of the JOB execution in charge of deleting the removed spaces.
   * @return cron as string, empty to deactivate the JOB.
   */
  public static String getDeletionOfRemovedSpacesCron() {
    return settings.getString("DeleteRemovedSpacesCron", "");
  }

  /**
   * Gets the delay in days after which a removed space can be deleted.
   * @return day delay as int, 0 to deactivate the automatic deletion of removed spaces.
   */
  public static int getDeletionOfRemovedSpacesDayDelay() {
    return settings.getInteger("DeleteRemovedSpacesDelay", 30);
  }

  /**
   * Indicates if the automatic deletion of removed spaces is enabled.
   * @return true if enabled, false otherwise.
   */
  public static boolean isAutomaticDeletionOfRemovedSpacesEnabled() {
    return isDefined(getDeletionOfRemovedSpacesCron()) && getDeletionOfRemovedGroupsDayDelay() > 0;
  }

  /**
   * Gets the cron of the JOB execution in charge of deleting the removed spaces.
   * @return cron as string, empty to deactivate the JOB.
   */
  public static String getDeletionOfRemovedApplicationsCron() {
    return settings.getString("DeleteRemovedApplicationsCron", "");
  }

  /**
   * Gets the delay in days after which a removed space can be deleted.
   * @return day delay as int, 0 to deactivate the automatic deletion of removed spaces.
   */
  public static int getDeletionOfRemovedApplicationsDayDelay() {
    return settings.getInteger("DeleteRemovedApplicationsDelay", 30);
  }

  /**
   * Indicates if the automatic deletion of removed spaces is enabled.
   * @return true if enabled, false otherwise.
   */
  public static boolean isAutomaticDeletionOfRemovedApplicationsEnabled() {
    return isDefined(getDeletionOfRemovedApplicationsCron())
        && getDeletionOfRemovedApplicationsDayDelay() > 0;
  }
}

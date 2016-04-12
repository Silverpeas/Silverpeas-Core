/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.io.temp;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.time.TimeUnit;

/**
 * All settings around the temporary data management services.
 * @author Yohann Chastagnier
 */
public class TemporaryDataManagementSetting {

  protected static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.util.data.temporaryDataManagementSettings");

  /**
   * Gets the CRON setting for the scheduler of the cleaning job.
   * @return a string that represents a cron setting.
   */
  public static String getJobCron() {
    return settings.getString("temporaryData.cleaner.job.cron", "");
  }

  /**
   * Gets the time after that a file, according to its last modified date, must be deleted.
   * @return a time in milliseconds, 0 or negative value if files must be deleted each time the
   * cleaning job is awaken up by the scheduler.
   */
  public static long getTimeAfterThatFilesMustBeDeleted() {
    return getTimeInMs(settings.getLong("temporaryData.cleaner.job.file.age.hours", 0));
  }

  /**
   * Gets the time after that a file, according to its last modified date, must be deleted at
   * server start.
   * @return a time in milliseconds, 0 if files must be deleted immediately, -1 if no deletion
   * at server start.
   */
  public static long getTimeAfterThatFilesMustBeDeletedAtServerStart() {
    return getTimeInMs(settings.getLong("temporaryData.cleaner.job.start.file.age.hours", -1));
  }

  private static long getTimeInMs(long nbHours) {
    if (nbHours > 0) {
      return UnitUtil.getTimeData(nbHours, TimeUnit.HOUR).getTimeAsLong();
    }
    return nbHours;
  }
}

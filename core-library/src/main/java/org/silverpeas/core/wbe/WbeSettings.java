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

package org.silverpeas.core.wbe;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

/**
 * @author silveryocha
 */
public class WbeSettings {

  public static final String SETTINGS_PATH = "org.silverpeas.wbe.wbeSettings";

  private WbeSettings() {
  }

  /**
   * Indicates if Web Browser Edition is enabled.
   * @return true if enabled, false otherwise.
   */
  public static boolean isEnabled() {
    return getSettings().getBoolean("wbe.enabled", false);
  }

  /**
   * Gets the prefix of WEb Browser Edition user ids to exchange.
   * @return a string which could be empty but never null.
   */
  public static String getWbeUserIdPrefix() {
    return getSettings().getString("wbe.user.id.prefix", "");
  }

  private static SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle(SETTINGS_PATH);
  }
}

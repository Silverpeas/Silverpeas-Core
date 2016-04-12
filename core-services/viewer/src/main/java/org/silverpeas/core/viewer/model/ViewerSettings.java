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
package org.silverpeas.core.viewer.model;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.io.temp.TemporaryDataManagementSetting;
import org.silverpeas.core.viewer.service.JsonPdfToolManager;

/**
 * All settings around the viewer services.
 * @author Yohann Chastagnier
 */
public class ViewerSettings {

  protected static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.viewer.viewer");

  /**
   * Gets the maximum width the preview can be.
   * @return a non null integer.
   */
  public static int getPreviewMaxWidth() {
    return settings.getInteger("preview.width.max", 500);
  }

  /**
   * Gets the maximum height the preview can be.
   * @return a non null integer.
   */
  public static int getPreviewMaxHeight() {
    return settings.getInteger("preview.height.max", 500);
  }

  /**
   * Gets a licence key provided by the author of the current viewer.
   * @return a string, empty if no licence key.
   */
  public static String getLicenceKey() {
    return settings.getString("flexpaper.licenseKey", "");
  }

  /**
   * Indicates if the cache is enabled.
   * @return true if enabled, false otherwise.
   */
  public static boolean isCacheEnabled() {
    return settings.getBoolean("viewer.cache.enabled", true);
  }

  /**
   * Indicates if the split strategy is enabled.<br/>
   * Even if it is disabled, in some cases, the strategy is still used (when the default strategy
   * does not work for example).
   * @return true if enabled, false otherwise.
   */
  public static boolean isSplitStrategyEnabled() {
    return settings.getBoolean("viewer.conversion.strategy.split.enabled", true) &&
        JsonPdfToolManager.isActivated();
  }

  /**
   * Indicates if the silent conversion is enabled.<br/>
   * The cache must be enabled.
   * @return true if enabled and if {@link #isCacheEnabled()} returns true, false otherwise.
   */
  public static boolean isSilentConversionEnabled() {
    return isCacheEnabled() && settings.getBoolean("viewer.cache.conversion.silent.enabled", true);
  }

  /**
   * Indicates if the cached files written with time to live method. Each time a viewer is
   * accessing cached data, the time to live is reset.<br/>
   * If time to live is not enabled, then the file written into the cache are deleted only
   * according to the Silverpeas cache rules.
   * @return true if time to live, false otherwise.
   */
  public static boolean isTimeToLiveEnabled() {
    return isCacheEnabled() &&
        TemporaryDataManagementSetting.getTimeAfterThatFilesMustBeDeleted() >= 0 &&
        settings.getBoolean("viewer.cache.timeToLive.enabled", true);
  }
}

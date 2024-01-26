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

package org.silverpeas.core.date;

import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;

import java.time.ZoneId;
import java.util.MissingResourceException;

/**
 * @author silveryocha
 */
public class TimeZoneUtil {

  private static final SettingBundle TIMEZONE_MAPPING =
      ResourceLocator.getSettingBundle("org.silverpeas.util.timezone");
  private static final String ZONE_ID_MAP_KEY_PREFIX = "zoneid.map.";

  /**
   * Hidden constructor.
   */
  private TimeZoneUtil() {
  }

  /**
   * Gets from a mapping the corresponding zone identifier.
   * @param timeZoneId time zone identifier.
   * @return the zone identifier.
   * @throws MissingResourceException if mapping does not yet exist.
   */
  private static String getFromMapping(final String timeZoneId) {
    return TIMEZONE_MAPPING.getString(ZONE_ID_MAP_KEY_PREFIX + timeZoneId.replace(" ", "_"));
  }

  /**
   * Gets the zone id instance from a time zone id which could be different than the registered
   * identifiers of {@link ZoneId#getAvailableZoneIds()} instances.<br>
   * If not registered, the zone identifier is search into a property file:
   * org.silverpeas.util.timezone.properties<br>
   * The mapping comes from
   * <a href="http://unicode.org/repos/cldr/trunk/common/supplemental/windowsZones.xml">this
   * source</a>.
   * @param timeZoneId a time zone identifier as string.
   * @return the corresponding {@link ZoneId} instance.
   */
  public static ZoneId toZoneId(final String timeZoneId) {
    try {
      return ZoneId.of(timeZoneId);
    } catch (Exception e) {
      SilverLogger.getLogger(TimeZoneUtil.class).warn(e);
    }
    return ZoneId.of(getFromMapping(timeZoneId));
  }
}

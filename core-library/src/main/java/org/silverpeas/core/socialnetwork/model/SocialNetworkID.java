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
 * FLOSS exception.  You should have received a copy of the text describing
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

package org.silverpeas.core.socialnetwork.model;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

public enum SocialNetworkID {
  FACEBOOK, LINKEDIN, UNKNOWN;

  private static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.social.settings.socialNetworkSettings");

  public boolean isEnabled() {
    switch (this) {
      case FACEBOOK:
        return settings.getBoolean("facebook.enable", false);

      case LINKEDIN:
        return settings.getBoolean("linkedIn.enable", false);

      default:
        return false;
    }
  }

  public static boolean oneIsEnable() {
    for (SocialNetworkID socialNetworkId : values()) {
      if (socialNetworkId.isEnabled()) {
        return true;
      }
    }

    return false;
  }
  public static SocialNetworkID from(String socialNetworkId) {
    if (StringUtil.isDefined(socialNetworkId)) {
      if (socialNetworkId.equalsIgnoreCase(FACEBOOK.name())) {
        return FACEBOOK;
      } else if (socialNetworkId .equalsIgnoreCase(LINKEDIN.name())) {
        return LINKEDIN;
      }
    }
    return UNKNOWN;
  }
}

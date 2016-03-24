/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.core.web.util.security;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

/**
 * User: Yohann Chastagnier
 * Date: 05/03/14
 */
public class SecuritySettings {

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.util.security");

  /**
   * Is web security mechanisms enabled?
   * - tokens
   * - SQL injection
   * - XSS injection
   * - ...
   * @return
   */
  private static boolean isWebProtectionEnabled() {
    return settings.getBoolean("security.web.protection", false);
  }

  /**
   * Is the SQL injection security mechanism enabled?
   * @return true if the security mechanism is enabled for Silverpeas, false otherwise.
   */
  public static boolean isWebSqlInjectionSecurityEnabled() {
    return isWebProtectionEnabled() &&
        settings.getBoolean("security.web.protection.injection.sql", false);
  }

  /**
   * Is the XSS injection security mechanism enabled?
   * @return true if the security mechanism is enabled for Silverpeas, false otherwise.
   */
  public static boolean isWebXssInjectionSecurityEnabled() {
    return isWebProtectionEnabled() &&
        settings.getBoolean("security.web.protection.injection.xss", false);
  }

  /**
   * Is the security mechanism based on the synchronizer token pattern enabled?
   * @return true if the security mechanism is enabled for Silverpeas, false otherwise.
   */
  public static boolean isWebSecurityByTokensEnabled() {
    return isWebProtectionEnabled() && settings.getBoolean("security.web.protection.token", false);
  }

  /**
   * Is the renew of the synchronizer tokens used to protect a user session enabled?
   * @return true if the renew of session tokens is enabled in Silverpeas, false otherwise.
   */
  public static boolean isSessionTokenRenewEnabled() {
    return isWebSecurityByTokensEnabled() &&
        settings.getBoolean("security.web.protection.sessiontoken.renew", false);
  }
}

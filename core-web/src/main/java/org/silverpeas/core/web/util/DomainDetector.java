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
package org.silverpeas.core.web.util;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import javax.servlet.http.HttpServletRequest;

public class DomainDetector {

  private static SettingBundle domainSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.authentication.settings.domainSettings");

  /**
   * Return domain id according to server URL and settings file
   * @param request
   * @return domainId if according rule is present in settings file, null otherwise
   */
  public static String getDomainId(HttpServletRequest request) {
    String requestURL = request.getRequestURL().toString();
    String serverName = requestURL.substring(requestURL.indexOf("//") + 2, requestURL.length());
    serverName = serverName.substring(0, serverName.indexOf("/"));
    if (serverName.indexOf(":") != -1) {
      serverName = serverName.substring(0, serverName.indexOf(":"));
    }
    if (serverName.indexOf(".") != -1) {
      serverName = serverName.substring(0, serverName.indexOf("."));
    }
    return domainSettings.getString(serverName.toLowerCase(), null);
  }
}

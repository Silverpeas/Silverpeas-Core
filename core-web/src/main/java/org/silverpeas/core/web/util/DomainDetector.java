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

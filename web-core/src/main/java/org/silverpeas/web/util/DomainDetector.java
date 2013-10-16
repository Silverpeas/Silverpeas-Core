package org.silverpeas.web.util;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.webactiv.util.ResourceLocator;

public class DomainDetector {

  private static ResourceLocator domainSettings = new ResourceLocator(
      "org.silverpeas.authentication.settings.domainSettings", "");

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

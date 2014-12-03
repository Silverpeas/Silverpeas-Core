/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.silverpeas.notificationManager;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class AbstractNotification {

  private ResourceLocator notifResources = new ResourceLocator(
      "org.silverpeas.notificationManager.settings.notificationManagerSettings", "");

  public String getApplicationURL() {
    return URLManager.getApplicationURL();
  }

  public String computeURL(final Integer userId, final String urlBase) {
    return computeURL(Integer.toString(userId), urlBase);
  }

  public String computeURL(final String userId, final String urlBase) {
    return (urlBase.startsWith("http") ? urlBase : getUserAutoRedirectURL(userId, urlBase));
  }

  public String getUserAutoRedirectURL(final String userId, final String target) {
    String encodedTarget = URLManager.encodeURL(target);
    try {
      final UserDetail ud = UserDetail.getById(userId);
      final Domain dom = ud.getDomain();
      String url;
      if (URLManager.isPermalink(target)) {
        url = dom.getSilverpeasServerURL() + getApplicationURL() + target;
      } else {
        url = getUserAutoRedirectURL(dom) + encodedTarget;
      }
      return url;
    } catch (final Exception e) {
      SilverTrace.error("peasCore", "URLManager.getUserAutoRedirectURL(userId, target)",
          "admin.EX_ERR_GET_USER_DETAILS", "user id: '" + userId + "', target: '" + target + "'",
          e);
      return "ErrorGettingDomainServer" + encodedTarget;
    }
  }

  public String getUserAutoRedirectURL(final Domain dom) {
      return dom.getSilverpeasServerURL() + getApplicationURL()
          + "/autoRedirect.jsp?domainId=" + dom.getId() + "&goto=";
  }

  /**
   * Gets the resources locator used for settings some notification parameters.
   * @return the locator of the resource with the notification parameters.
   */
  protected ResourceLocator getNotificationResources() {
    return notifResources;
  }

  /**
   * Sets the resource locator to be used by this NotificationManager instance in order to sets the
   * notification parameters.
   * @param resourceLocator the locator of the resource with the notification parameters.
   */
  public void setNotificationResources(final ResourceLocator resourceLocator) {
    notifResources = resourceLocator;
  }

  public String getUserAutoRedirectSilverpeasServerURL(final String userId) {
    final UserDetail ud = UserDetail.getById(userId);
    final Domain dom = ud.getDomain();
    String url = dom.getSilverpeasServerURL() + getApplicationURL();
    return url;
  }
}

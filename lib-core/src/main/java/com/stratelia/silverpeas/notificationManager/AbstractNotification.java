/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

import java.net.URLEncoder;

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
      "com.stratelia.silverpeas.notificationManager.settings.notificationManagerSettings", "");

  public String getApplicationURL() {
    return URLManager.getApplicationURL();
  }

  public String computeURL(final Integer userId, final String urlBase) {
    return computeURL(Integer.toString(userId), urlBase);
  }

  public String computeURL(final String userId, final String urlBase) {
    return (urlBase.startsWith("http") ? urlBase : getUserAutoRedirectURL(userId, urlBase));
  }

  public String getUserAutoRedirectURL(final String userId,
      final String target) {
    try {
      return getUserAutoRedirectURL(userId) + URLEncoder.encode(target, "UTF-8");
    } catch (final Exception e) {
      SilverTrace.error("peasCore",
          "URLManager.getUserAutoRedirectURL(userId)", "root.EX_NO_MESSAGE",
          "Cannot encode '" + target + "'", e);
      return null;
    }
  }

  public String getUserAutoRedirectURL(final String userId) {
    try {
      final UserDetail ud = AdminReference.getAdminService().getUserDetail(userId);
      final Domain dom = AdminReference.getAdminService().getDomain(ud.getDomainId());
      return dom.getSilverpeasServerURL() + getApplicationURL()
          + "/autoRedirect.jsp?domainId=" + dom.getId() + "&goto=";
    } catch (final Exception ae) {
      SilverTrace.error("peasCore",
          "URLManager.getUserAutoRedirectURL(userId)",
          "admin.EX_ERR_GET_USER_DETAILS", "user id: '" + userId + "'", ae);
      return "ErrorGettingDomainServer";
    }
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
}

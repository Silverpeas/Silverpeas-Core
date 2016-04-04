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
package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class AbstractNotification {

  public String getApplicationURL() {
    return URLUtil.getApplicationURL();
  }

  public String computeURL(final Integer userId, final String urlBase) {
    return computeURL(Integer.toString(userId), urlBase);
  }

  public String computeURL(final String userId, final String urlBase) {
    return (urlBase.startsWith("http") ? urlBase : getUserAutoRedirectURL(userId, urlBase));
  }

  public String getUserAutoRedirectURL(final String userId, final String target) {
    String encodedTarget = URLUtil.encodeURL(target);
    try {
      final UserDetail ud = UserDetail.getById(userId);
      final Domain dom = ud.getDomain();
      String url;
      if (URLUtil.isPermalink(target)) {
        url = dom.getSilverpeasServerURL() + getApplicationURL() + target;
      } else {
        url = getUserAutoRedirectURL(dom) + encodedTarget;
      }
      return url;
    } catch (final Exception e) {
      SilverLogger.getLogger(this)
          .error("Error while getting user auto redirect url {0} for user {1}",
              new String[]{target, userId}, e);
      return "ErrorGettingDomainServer" + encodedTarget;
    }
  }

  public String getUserAutoRedirectURL(final Domain dom) {
      return dom.getSilverpeasServerURL() + getApplicationURL()
          + "/autoRedirect.jsp?domainId=" + dom.getId() + "&goto=";
  }

  public String getUserAutoRedirectSilverpeasServerURL(final String userId) {
    final UserDetail ud = UserDetail.getById(userId);
    final Domain dom = ud.getDomain();
    String url = dom.getSilverpeasServerURL() + getApplicationURL();
    return url;
  }
}

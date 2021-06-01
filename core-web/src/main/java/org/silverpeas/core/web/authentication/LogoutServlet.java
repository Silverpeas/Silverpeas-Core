/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.authentication;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.silverpeas.core.security.session.SessionManagementProvider.getSessionManagement;

public class LogoutServlet extends HttpServlet {

  private static final long serialVersionUID = 996291597161289526L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      // Get the session
      HttpSession session = request.getSession(false);
      if (session != null) {
        getSessionManagement().closeSession(session.getId());
        invalidateUserSession(session);
      }
      SettingBundle resource = ResourceLocator.getSettingBundle(
          "org.silverpeas.authentication.settings.authenticationSettings");
      String postLogoutPage = resource.getString("logout.page", "/Login?logout=true");
      if (postLogoutPage.startsWith("http")) {
        response.sendRedirect(postLogoutPage);
        return;
      }
      StringBuilder buffer = new StringBuilder(512);
      buffer.append(request.getScheme()).append("://").append(request.getServerName()).append(':');
      buffer.append(request.getServerPort()).append(request.getContextPath());
      User currentUser = User.getCurrentRequester();
      if (currentUser != null) {
        String paramDelimiter = (postLogoutPage.contains("?") ? "&" : "?");
        postLogoutPage +=
            paramDelimiter + LoginServlet.PARAM_DOMAINID + "=" + currentUser.getDomainId();
      }
      buffer.append(postLogoutPage);
      response.sendRedirect(response.encodeRedirectURL(buffer.toString()));
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void invalidateUserSession(final HttpSession session) {
    try {
      // In some cases, a wildfly session is yet valid whereas it does not exist anymore a
      // Silverpeas session. But the wildfly session must be invalidated because the logout can
      // be performed by a cloud mechanism which need clean wildfly session.
      session.invalidate();
    } catch (IllegalStateException e) {
      SilverLogger.getLogger(this).silent(e);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    doPost(request, response);
  }
}

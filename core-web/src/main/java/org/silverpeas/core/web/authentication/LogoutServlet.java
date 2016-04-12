/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.authentication;

import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {

  private static final long serialVersionUID = 996291597161289526L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the session
    HttpSession session = request.getSession(false);
    if (session != null) {
      SessionManagementProvider.getSessionManagement().closeSession(session.getId());
    }
    StringBuilder buffer = new StringBuilder(512);
    buffer.append(request.getScheme()).append("://").append(request.getServerName()).append(':');
    buffer.append(request.getServerPort()).append(request.getContextPath());
    SettingBundle resource = ResourceLocator.getSettingBundle(
        "org.silverpeas.authentication.settings.authenticationSettings");
    String postLogoutPage = resource.getString("logout.page", "/Login.jsp?ErrorCode=4&logout=true");
    if (postLogoutPage.startsWith("http")) {
      response.sendRedirect(postLogoutPage);
      return;
    }
    buffer.append(postLogoutPage);
    response.sendRedirect(response.encodeRedirectURL(buffer.toString()));
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {
    doPost(request, response);
  }
}

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
package org.silverpeas.core.web.mvc.route;

import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class MainRequestRouter extends HttpServlet {

  private static final long serialVersionUID = 5131039058584808582L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    // Get the session
    HttpSession session = request.getSession(false);
    // Get the context
    String uri = request.getRequestURI();
    String servletPath = request.getServletPath();
    String pathInfo = request.getPathInfo();
    String requestUrl = request.getRequestURL().toString();

    String absolutePrefix = requestUrl.substring(0, requestUrl.length() - request.getRequestURI().
        length());
    if (pathInfo != null) {
      uri = uri.substring(0, uri.lastIndexOf(pathInfo));
    }
    String webContext = uri.substring(0, uri.lastIndexOf(servletPath));
    if (webContext.charAt(webContext.length() - 1) == '/') {
      webContext = webContext.substring(0, webContext.length() - 1);
    }
    final String fullAppContext = absolutePrefix + webContext;

    // Get the favorite frameset to the current user
    if (session != null) {
      GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
          GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      final String lookFrame = gef.getLookFrame();
      if (lookFrame.startsWith("/")) {
        response.sendRedirect(response.encodeRedirectURL(fullAppContext + lookFrame));
      } else {
        response.sendRedirect(response.encodeRedirectURL(fullAppContext+ "/admin/jsp/" + lookFrame));
      }
    } else {
      SilverLogger.getLogger(this).warn("It seems that it is not possible to create an HTTP session. Please verify SSL certificates...");
      response.sendRedirect(response.encodeRedirectURL(fullAppContext + "/admin/jsp/silverpeas-main.jsp"));
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {
    doPost(request, response);
  }
}

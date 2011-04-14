/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.stratelia.webactiv.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class MainRequestRouter extends HttpServlet {

  private static final long serialVersionUID = 5131039058584808582L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the session
    HttpSession m_Session = request.getSession(false);
    // Get the context
    String sURI = request.getRequestURI();
    String sServletPath = request.getServletPath();
    String sPathInfo = request.getPathInfo();
    String sRequestURL = request.getRequestURL().toString();

    String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length()
        - request.getRequestURI().length());
    if (sPathInfo != null) {
      sURI = sURI.substring(0, sURI.lastIndexOf(sPathInfo));
    }
    String m_sContext = sURI.substring(0, sURI.lastIndexOf(sServletPath));
    if (m_sContext.charAt(m_sContext.length() - 1) == '/') {
      m_sContext = m_sContext.substring(0, m_sContext.length() - 1);
    }

    // Get the favorite frameset to the current user
    GraphicElementFactory gef = (GraphicElementFactory) m_Session.getAttribute(
        "SessionGraphicElementFactory");

    if (gef.getLookFrame().startsWith("/")) {
      response.sendRedirect(
          response.encodeRedirectURL(m_sAbsolute + m_sContext + gef.getLookFrame()));
    } else {
      response.sendRedirect(response.encodeRedirectURL(m_sAbsolute + m_sContext + "/admin/jsp/" 
          + gef.getLookFrame()));
    }
    return;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {
    doPost(request, response);
  }
}
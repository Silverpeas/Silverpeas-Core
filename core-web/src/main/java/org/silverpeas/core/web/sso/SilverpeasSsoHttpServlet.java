/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.web.sso;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet is the parent one of Silverpeas SSO modules.
 * @author Yohann Chastagnier
 */
public abstract class SilverpeasSsoHttpServlet extends HttpServlet {

  private static final long serialVersionUID = -2013173095753706593L;

  /**
   * Computes the {@link SilverpeasSsoPrincipal} if possible.
   * @return a valid {@link SilverpeasSsoPrincipal} on successful SSO authentication, null
   * otherwise.
   */
  protected abstract SilverpeasSsoPrincipal computeSsoPrincipal(final HttpServletRequest request,
      final HttpServletResponse response);

  @Override
  public void doPost(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    final SilverpeasSsoPrincipal ssoPrincipal = computeSsoPrincipal(request, response);
    final RequestDispatcher requestDispatcher = getServletConfig().getServletContext()
        .getRequestDispatcher("/sso");
    if (ssoPrincipal != null) {
      // SSO authentication detected
      requestDispatcher.forward(new SilverpeasSsoHttpRequest(request, ssoPrincipal), response);
    } else {
      requestDispatcher.forward(request, response);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }
}

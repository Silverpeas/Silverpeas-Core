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
package org.silverpeas.core.web.http;

import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Centralizing the code which permits to redirect safely to any content URL from a Servlet.
 * @author Yohann Chastagnier
 */
public class ServletSafeContentRedirect extends SafeContentRedirect {

  /**
   * Hidden constructor.
   * @param request the current http request.
   * @param response the current http response.
   */
  ServletSafeContentRedirect(final HttpServletRequest request, final HttpServletResponse response) {
    super(request, response);
  }

  /**
   * Redirects to the specified URL.
   * @param url the URL to redirect to.
   */
  public void redirectTo(final String url) {
    final String destination = getDestination(url);
    try {
      request.getRequestDispatcher(destination).forward(request, response);
    } catch (ServletException | IOException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }
}

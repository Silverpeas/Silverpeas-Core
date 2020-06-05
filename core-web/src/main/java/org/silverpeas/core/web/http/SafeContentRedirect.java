/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.web.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.silverpeas.core.util.URLUtil.*;

/**
 * Centralizing the code which permits to redirect safely to any content URL.
 * @author Yohann Chastagnier
 */
public abstract class SafeContentRedirect {

  final HttpServletResponse response;
  final HttpServletRequest request;

  /**
   * Hidden constructor.
   * @param request the current http request.
   * @param response the current http response.
   */
  SafeContentRedirect(final HttpServletRequest request, final HttpServletResponse response) {
    this.request = request;
    this.response = response;
  }

  /**
   * Initializing the file response context.
   * @param request the current request.
   * @param response the current response.
   * @return the initialized file response.
   */
  public static ServletSafeContentRedirect fromServlet(final HttpServletRequest request,
      final HttpServletResponse response) {
    return new ServletSafeContentRedirect(request, response);
  }

  /**
   * Sets the URL of redirection and perform on it some computing.
   * @param url the URL to redirect to.
   */
  public String getDestination(final String url) {
    if (request.getAttribute("IsInternalLink") == null &&
        (url.startsWith(getFullApplicationURL(request)) || url.startsWith(getApplicationURL()))) {
      request.setAttribute("IsInternalLink", true);
      request.setAttribute("IsPermalink", isPermalink(url));
    }
    request.setAttribute("URL", url);
    return "/util/jsp/safeContentRedirection.jsp";
  }
}

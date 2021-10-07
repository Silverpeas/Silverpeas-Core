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
package org.silverpeas.web.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.silverpeas.core.util.URLUtil.getSilverpeasFingerprint;

/**
 * A filter that handle resource caches.
 * @author silveryocha
 */
public class ResourceCacheFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (request instanceof HttpServletRequest) {
      final HttpServletRequest httpRequest = (HttpServletRequest) request;
      final HttpServletResponse httpResponse = (HttpServletResponse) response;
      if (performFingerprintVersion(httpRequest, httpResponse)) {
        return;
      }
    }
    chain.doFilter(request, response);
  }

  /**
   * Performing fingerprint version detection. If detected, forwarding to the real resource.
   * @param httpRequest the HTTP request.
   * @param httpResponse the response.
   * @return true if fingerprint version has bee processed, false otherwise.
   */
  private boolean performFingerprintVersion(final HttpServletRequest httpRequest,
      final HttpServletResponse httpResponse) throws ServletException, IOException {
    final String requestURI = httpRequest.getRequestURI();
    final int fingerprintIndex = requestURI.indexOf(getSilverpeasFingerprint());
    if (fingerprintIndex > 0) {
      final String contextPath = httpRequest.getContextPath();
      final String leftPart = requestURI.substring(contextPath.length(), fingerprintIndex);
      final String rightPart = requestURI.substring(fingerprintIndex + getSilverpeasFingerprint().length());
      final String newUri = leftPart + rightPart;
      httpResponse.setHeader("Cache-Control", "max-age=31536000, s-maxage=31536000, immutable");
      final RequestDispatcher rd = httpRequest.getRequestDispatcher(newUri);
      rd.forward(httpRequest, httpResponse);
      return true;
    }
    return false;
  }

  @Override
  public void destroy() {
    // Nothing to do here
  }

  @Override
  public void init(FilterConfig filterConfig) {
    // Nothing to do here
  }
}

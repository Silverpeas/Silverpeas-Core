/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.web.filter;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.HttpMethod;
import org.silverpeas.core.util.security.SecuritySettings;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * This filter provide the right behaviour to handle Cross-Origin Resource Sharing (CORS).
 * Please taking a look at <code>http://www.w3.org/TR/2012/WD-cors-20120403/</code>.
 * A huge advantage, for example, is that there is no need to change the coding of the ajax http
 * request even in the case where requests are sent from a different domain of that of the requested
 * server ...
 * @author Yohann Chastagnier
 */
public class WebCORSFilter implements Filter {

  private static final String ALL_DOMAINS_ALLOWED = "*";

  /*
   * (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
   * javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
      final FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Allow only framing (frame, iframe and objects) coming from the same origin
    httpResponse.addHeader("X-Frame-Options", "SAMEORIGIN");

    // The allowed domains are always indicated
    httpResponse.addHeader("Access-Control-Allow-Origin", getAllowedDomain(httpRequest));

    // In case of detection of an OPTIONS HTTP method, additional headers are filled.
    if (HttpMethod.OPTIONS.name().equals(httpRequest.getMethod())) {
      httpResponse.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
      httpResponse.addHeader("Access-Control-Allow-Headers",
          "Content-Type, " + UserPrivilegeValidation.HTTP_SESSIONKEY + ", " +
              UserPrivilegeValidation.HTTP_AUTHORIZATION);
    }

    // The request treatment continue.
    chain.doFilter(request, response);
  }

  /**
   * Check the URL of the domain behind the specified HTTP request is allowed to be accessed from
   * Silverpeas. It is to establish a CORS protection.
   * For doing, it gets from the security settings the URL of the domains authorized to be accessed
   * from Silverpeas and checks the origin of the specified request is among the allowed domains.
   * If the setting on the allowed domains is valued with "*" then no CORS protection is enabled.
   * If the setting isn't valued, then only the web resource of Silverpeas are allowed to be
   * accessed from Silverpeas itself.
   * @param httpRequest the incoming HTTP request.
   * @return the string identifying the domain allowed to access the resource targeted by the
   * specified resource.
   */
  private String getAllowedDomain(final HttpServletRequest httpRequest) {
    final List<String> allowedDomains = SecuritySettings.getAllowedDomains();
    final String allowedDomain;
    if (allowedDomains.contains(ALL_DOMAINS_ALLOWED)) {
      // No restrictions
      allowedDomain = ALL_DOMAINS_ALLOWED;
    } else {
      final String serverOrigin = httpRequest.getHeader("Origin");
      if (StringUtil.isDefined(serverOrigin) && allowedDomains.contains(serverOrigin)) {
        allowedDomain = serverOrigin;
      } else {
        allowedDomain = URLUtil.getServerURL(httpRequest);
      }
    }
    return allowedDomain;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    // Nothing to do.
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
    // Nothing to do.
  }
}

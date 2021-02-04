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

package org.silverpeas.core.webapi.wbe;

import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.session.UserSessionEvent;
import org.silverpeas.core.wbe.DefaultWbeUser;
import org.silverpeas.core.wbe.WbeHostManager;
import org.silverpeas.core.wbe.WbeUser;

import javax.enterprise.event.Observes;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

import static javax.ws.rs.core.Response.Status.fromStatusCode;
import static org.silverpeas.core.wbe.WbeLogger.logger;

/**
 * This filter is today design to log HTTP exchanges between Silverpeas (Web Browser Edition
 * Host) and the WBE client. The logging is enabled at the DEBUG logging level which can be set
 * from Silverpeas's administration.
 * <p>
 *   Errors are always logged whatever the logging level set.
 * </p>
 * @author silveryocha
 */
public class WbeFilter implements Filter {

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
      final FilterChain chain) throws IOException, ServletException {
    if (response instanceof HttpServletResponse) {
      final HttpServletResponse httpResponse = (HttpServletResponse) response;
      final boolean debug = logger().isLoggable(Level.DEBUG);
      if (debug) {
        final HttpRequest httpRequest = HttpRequest.decorate(request);
        final Enumeration<String> headerNames = httpRequest.getHeaderNames();
        final StringBuilder sb = new StringBuilder();
        while (headerNames.hasMoreElements()) {
          final String headerName = headerNames.nextElement();
          sb.append(headerName).append("=").append(httpRequest.getHeader(headerName)).append("\n");
        }
        final Enumeration<String> parameterNames = httpRequest.getParameterNames();
        while (parameterNames.hasMoreElements()) {
          final String parameterName = parameterNames.nextElement();
          sb.append(parameterName).append("=").append(httpRequest.getParameter(parameterName)).append("\n");
        }
        logger().debug("handling {0} on {1} from WBE host {2}:{3}", httpRequest.getMethod(),
            httpRequest.getRequestURI(), httpRequest.getRemoteHost(),
            String.valueOf(httpRequest.getRemotePort()));
        logger().debug("with headers and parameters:\n{0}", sb);
      }
      chain.doFilter(request, response);
      if (httpResponse.getStatus() >= 400) {
        logger().error("error {0} - {1}", httpResponse.getStatus(), fromStatusCode(httpResponse.getStatus()));
      } else if (debug) {
        logger().debug("status {0} - {1}", httpResponse.getStatus(), fromStatusCode(httpResponse.getStatus()));
      }
      return;
    }
    chain.doFilter(request, response);
  }

  @Override
  public void init(final FilterConfig filterConfig) {
    // Nothing to do.
  }

  @Override
  public void destroy() {
    // Nothing to do.
  }

  /**
   * On session ending, cleaning the cache of users.
   * @param userSessionEvent the user session event.
   */
  public void onEvent(@Observes final UserSessionEvent userSessionEvent) {
    if (userSessionEvent.isClosing()) {
      final WbeUser user = new DefaultWbeUser(userSessionEvent.getSessionInfo());
      WbeHostManager.get().revokeUser(user);
    }
  }
}

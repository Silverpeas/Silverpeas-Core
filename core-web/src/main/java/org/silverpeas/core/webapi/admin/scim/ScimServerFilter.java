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

package org.silverpeas.core.webapi.admin.scim;

import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

import static java.text.MessageFormat.format;
import static javax.ws.rs.core.Response.Status.fromStatusCode;
import static org.silverpeas.core.webapi.admin.scim.ScimLogger.logger;
import static org.silverpeas.core.webapi.admin.scim.ScimResourceURIs.SCIM_2_BASE_URI;

/**
 * @author silveryocha
 */
public class ScimServerFilter implements Filter {

  public static final String PUSH_SILVERPEAS_AUTHORIZED_ADMIN_IDS_PROP_KEY =
      "push.silverpeas.authorized.admin.ids";

  private static final Pattern SCIM_PATH_PATTERN = Pattern
      .compile(SCIM_2_BASE_URI.replace("{domainId}", "[0-9]+"));

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
      final FilterChain chain) throws IOException, ServletException {
    if (response instanceof HttpServletResponse) {
      final HttpServletResponse httpResponse = (HttpServletResponse) response;
      final HttpRequest httpRequest = HttpRequest.decorate(request);
      if (SCIM_PATH_PATTERN.matcher(httpRequest.getRequestURI()).find()) {
        logger().debug(
            () -> format("handling {0} on {1} from SCIM client {2}", httpRequest.getMethod(),
                httpRequest.getRequestURI(), httpRequest.getRemoteHost()));
        chain.doFilter(request, response);
        if (httpResponse.getStatus() >= 400) {
          logger().error("error {0} - {1}", httpResponse.getStatus(),
              fromStatusCode(httpResponse.getStatus()));
        } else {
          logger().debug(() -> format("status {0} - {1}", httpResponse.getStatus(),
              fromStatusCode(httpResponse.getStatus())));
        }
        return;
      }
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
}

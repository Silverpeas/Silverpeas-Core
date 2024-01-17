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
package org.silverpeas.web.filter;

import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * A filter to handle all the exception thrown from the Silverpeas application.
 * @author Yohann Chastagnier
 */
public class SilverpeasExceptionLoggingFilter implements Filter {

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
      final FilterChain chain) throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
    } catch (Exception t) {
      final SilverLogger logger = SilverLogger.getLogger("silverpeas.exception.unexpected");
      if (request instanceof HttpServletRequest) {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        logger.error("From request [{0}] getting error [{1}]",
            new Object[]{httpRequest.getRequestURL(),
                defaultStringIfNotDefined(t.getLocalizedMessage(), "unknown")}, t);
      } else {
        logger.error(t.getLocalizedMessage(), t);
      }
      throw t;
    }
  }

  @Override
  public void init(final FilterConfig filterConfig) {
    // Nothing to do here
  }

  @Override
  public void destroy() {
    // Nothing to do here
  }
}

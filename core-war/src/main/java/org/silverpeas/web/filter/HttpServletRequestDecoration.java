/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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

import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A filter to decorate the incoming request into a {@link HttpRequest}
 * instance.
 * @author mmoquillon
 */
public class HttpServletRequestDecoration implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpRequest httpRequest = HttpRequest.decorate(request);
    if (!httpRequest.isContentInMultipart()) {
      Long jQueryTs = httpRequest.getParameterAsLong("_");
      if (jQueryTs == null) {
        jQueryTs = httpRequest.getParameterAsLong("IEFix");
      }
      if (jQueryTs != null && jQueryTs.compareTo(0l) != 0) {
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        servletResponse.setHeader("Cache-Control", "no-store"); //HTTP 1.1
        servletResponse.setHeader("Pragma", "no-cache"); //HTTP 1.0
        servletResponse.setDateHeader("Expires", -1); //prevents caching at the proxy server
      }
    }
    chain.doFilter(httpRequest, response);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void init(FilterConfig filterConfig) {
  }

}

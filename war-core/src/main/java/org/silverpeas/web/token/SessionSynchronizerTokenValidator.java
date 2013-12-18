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
package org.silverpeas.web.token;

import com.stratelia.webactiv.util.ResourceLocator;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.silverpeas.token.exception.TokenValidationException;

/**
 * A validator of a session token for each incoming request.
 *
 * @author mmoquillon
 */
public class SessionSynchronizerTokenValidator implements Filter {

  // The filter configuration object we are associated with.  If
  // this value is null, this filter instance is not currently
  // configured.
  private FilterConfig config = null;
  private static final Logger logger = Logger.getLogger(SessionSynchronizerTokenValidator.class.
      getSimpleName());
  private static final String DEFAULT_RULE
      = "^/(?!(util/)|(images/)|(Main/)|(Rclipboard)|(clipboard)|(admin/))\\w+/.*(?<!(.gif)|(.png)|(.jpg)|(.js)|(.css))$";
  private static final String RULE_PREFIX = "security.web.protected.rule";
  private final ResourceLocator settings = new ResourceLocator("org.silverpeas.util.security", "");

  public SessionSynchronizerTokenValidator() {
  }

  /**
   * Validates the incoming request is performed within a valid user session.
   *
   * @param request The servlet request we are processing
   * @param response The servlet response we are creating
   * @param chain The filter chain we are processing
   *
   * @exception IOException if an input/output error occurs
   * @exception ServletException if a servlet error occurs
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    String requestPath = getRequestPath(request);
    if (isProtected(requestPath)) {
      logger.log(Level.INFO, "Validate the request for path {0}", requestPath);
      SynchronizerTokenService service = SynchronizerTokenServiceFactory.
          getSynchronizerTokenService();
      try {
        service.validate((HttpServletRequest) request);
      } catch (TokenValidationException ex) {
        logger.log(Level.SEVERE, "The request for path {0} isn''t valid: {1}",
            new String[]{requestPath, ex.getMessage()});
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
      }
    }
    chain.doFilter(request, response);
  }

  /**
   * Destroy method for this filter.
   */
  @Override
  public void destroy() {
  }

  /**
   * Init method for this filter
   *
   * @param filterConfig the configuration of this filter.
   */
  @Override
  public void init(FilterConfig filterConfig) {
    this.config = filterConfig;
  }

  protected String getRequestPath(ServletRequest request) {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String path = httpRequest.getRequestURI();
    if (path.startsWith(httpRequest.getContextPath())) {
      path = path.substring(httpRequest.getContextPath().length());
    }
    return path;
  }

  protected boolean isProtected(String path) {
    boolean isProtected = true;
    String regexp = null;
    Enumeration<String> properties = settings.getKeys();
    for (; properties.hasMoreElements() && isProtected;) {
      String property = properties.nextElement();
      if (property.startsWith(RULE_PREFIX)) {
        regexp = settings.getString(property);
        isProtected &= path.matches(regexp);
      }
    }
    if (regexp == null) {
      isProtected = path.matches(DEFAULT_RULE);
    }
    return isProtected;
  }

}

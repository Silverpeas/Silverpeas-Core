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

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.silverpeas.core.web.token.SynchronizerTokenService;
import org.silverpeas.core.security.token.exception.TokenValidationException;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.util.security.SecuritySettings;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * A validator of a session token for each incoming request. For each protected web resources, the
 * requests are expected to carry a synchronizer token that must match the token mapped with the
 * user session. The request validation is in fact delegated to a
 * {@link SynchronizerTokenService} instance; this object just process the
 * status of the validation.
 *
 * @author mmoquillon
 */
public class SessionSynchronizerTokenValidator implements Filter {

  private static final SilverLogger logger = SilverLogger.getLogger("security");

  @Inject
  private SynchronizerTokenService tokenService;
  @Inject
  private SessionManagement sessionManagement;

  public SessionSynchronizerTokenValidator() {
  }

  /**
   * Validates the incoming request is performed within a valid user session.
   *
   * If the request isn't sent within an opened user session, then the user is redirected to the
   * authentication page.
   *
   * If the request is sent within an opened user session but it doesn't carry a valid session
   * synchronizer token, then it is rejected and a forbidden status is sent back.
   *
   * @param request The servlet request to validate.
   * @param response The servlet response to sent back.
   * @param chain The filter chain we are processing
   *
   * @exception IOException if an input/output error occurs
   * @exception ServletException if a servlet error occurs
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    if (SecuritySettings.isWebSecurityByTokensEnabled() && isProtectedResource(httpRequest)) {
      try {
        checkAuthenticatedRequest(httpRequest);
        tokenService.validate(httpRequest);
        chain.doFilter(request, response);
      } catch (TokenValidationException ex) {
        logger.error("The request for path {0} isn''t valid: {1}",
            new String[]{pathOf(request), ex.getMessage()});
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
      } catch (UnauthenticatedRequestException ex) {
        logger.error("The request for path {0} isn''t sent within an opened session",
            pathOf(request));
        redirectToAuthenticationPage(request, response);
      }
    } else {
      chain.doFilter(request, response);
    }
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

  }

  private void checkAuthenticatedRequest(HttpServletRequest request) throws
      UnauthenticatedRequestException {
    if (!isCredentialManagement(request) && !isWebDAVResource(request)) {
      boolean isAuthenticated = false;
      HttpSession session = request.getSession(false);
      if (session != null) {
        SessionInfo sessionInfo = sessionManagement.getSessionInfo(session.getId());
        isAuthenticated = sessionInfo.isDefined();
      }
      if (!isAuthenticated) {
        throw new UnauthenticatedRequestException();
      }
    }
  }

  private void redirectToAuthenticationPage(ServletRequest request, ServletResponse response)
      throws ServletException, IOException {
    String destination = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    if (destination.startsWith("http") || destination.startsWith("ftp")) {
      httpResponse.sendRedirect(httpResponse.encodeRedirectURL(destination));
    } else {
      RequestDispatcher requestDispatcher = httpRequest.getRequestDispatcher(destination);
      requestDispatcher.forward(httpRequest, httpResponse);
    }
  }

  private boolean isCredentialManagement(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.endsWith("/Qaptcha") || uri.contains("/CredentialsServlet/") ||
        uri.contains("/services/password/") || uri.contains("/services/authentication") ||
        uri.contains("/AuthenticationServlet") ||
        (isWebServiceRequested(request) &&
            StringUtil.isDefined(request.getHeader(UserPrivilegeValidation.HTTP_AUTHORIZATION)));
  }

  private boolean isProtectedResource(HttpServletRequest request) {
    return tokenService.isAProtectedResource(request) && !isFileDragAndDrop(request)
        && !(isWebServiceRequested(request) && StringUtil.isDefined(request.getHeader(
                UserPrivilegeValidation.HTTP_SESSIONKEY)));
  }

  private boolean isWebDAVResource(HttpServletRequest request) {
    return request.getRequestURI().contains("/repository/silverpeas/webdav/") ||
        request.getRequestURI().contains("/repository2000/silverpeas/webdav");
  }

  private boolean isFileDragAndDrop(HttpServletRequest request) {
    return request.getRequestURI().contains("DragAndDrop/");
  }

  private boolean isWebServiceRequested(HttpServletRequest request) {
    return request.getRequestURI().contains("/services/");
  }

  private String pathOf(ServletRequest request) {
    return ((HttpServletRequest) request).getRequestURI();
  }

  private static class UnauthenticatedRequestException extends Exception {

    private static final long serialVersionUID = 9173126171348369053L;

  }
}

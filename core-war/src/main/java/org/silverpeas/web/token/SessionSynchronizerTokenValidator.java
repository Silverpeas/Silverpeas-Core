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
package org.silverpeas.web.token;

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.token.exception.TokenValidationException;
import org.silverpeas.core.util.security.SecuritySettings;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.token.SynchronizerTokenService;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.silverpeas.core.util.URLUtil.getApplicationURL;

/**
 * A validator of a session token for each incoming request. For each protected web resources, the
 * requests are expected to carry a synchronizer token that must match the token mapped with the
 * user session. The request validation is in fact delegated to a {@link SynchronizerTokenService}
 * instance; this object just process the status of the validation.
 *
 * @author mmoquillon
 */
public class SessionSynchronizerTokenValidator implements Filter {

  private static final SilverLogger logger = SilverLogger.getLogger("silverpeas.core.security");

  @Inject
  private SynchronizerTokenService tokenService;
  @Inject
  private SessionManagement sessionManagement;

  /**
   * Validates the incoming request is performed within a valid user session.
   * <p>
   * If the request isn't sent within an opened user session, then the user is redirected to the
   * authentication page.
   * </p>
   * <p>
   * If the request is sent within an opened user session but it doesn't carry a valid session
   * synchronizer token, then it is rejected and a forbidden status is sent back.
   * </p>
   *
   * @param request The servlet request to validate.
   * @param response The servlet response to sent back.
   * @param chain The filter chain we are processing
   * @throws IOException if an input/output error occurs
   * @throws ServletException if a servlet error occurs
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    if (SecuritySettings.isWebSecurityByTokensEnabled() && isProtectedResource(httpRequest)) {
      try {
        checkAuthenticatedRequest(httpRequest);
        tokenService.validate(httpRequest, false);
        chain.doFilter(request, response);
      } catch (TokenValidationException ex) {
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
      } catch (UnauthenticatedRequestException ex) {
        logger.error("The request for path {0} isn''t sent within an opened session",
            ((HttpServletRequest) request).getRequestURI());
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
    // nothing to destroy
  }

  /**
   * Init method for this filter
   *
   * @param filterConfig the configuration of this filter.
   */
  @Override
  public void init(FilterConfig filterConfig) {
    // nothing to init
  }

  private void checkAuthenticatedRequest(HttpServletRequest request) throws
      UnauthenticatedRequestException {
    if (!isWebDAVResource(request)) {
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
    return tokenService.isAProtectedResource(request, false)
        && !(isFileDragAndDrop(request) ||
            isCredentialManagement(request) ||
            isSsoAuthentication(request) ||
            isWebServiceRequested(request) ||
            isWebBrowserEditionResource(request) ||
            isCMISResource(request) ||
            isDragAndDropWebEditionResource(request) ||
            isWorkflowDesignerResource(request));
  }

  private boolean isWebDAVResource(HttpServletRequest request) {
    return request.getRequestURI().contains("/repository/silverpeas/webdav/") ||
        request.getRequestURI().contains("/repository2000/silverpeas/webdav");
  }

  private boolean isFileDragAndDrop(HttpServletRequest request) {
    return request.getRequestURI().contains("DragAndDrop/");
  }

  private boolean isWebServiceRequested(HttpServletRequest request) {
    return request.getRequestURI().contains(getApplicationURL() + "/services/");
  }

  private boolean isCMISResource(HttpServletRequest request) {
    return request.getRequestURI().contains(getApplicationURL() + "/cmis/");
  }

  private boolean isSsoAuthentication(HttpServletRequest request) {
    return request.getRequestURI().contains(getApplicationURL() + "/sso");
  }

  private boolean isWebBrowserEditionResource(HttpServletRequest request) {
    return request.getRequestURI().contains(getApplicationURL() + "/services/wbe/");
  }

  private boolean isDragAndDropWebEditionResource(HttpServletRequest request) {
    return request.getRequestURI().contains(getApplicationURL() + "/Rddwe/");
  }

  private boolean isWorkflowDesignerResource(HttpServletRequest request) {
    return request.getRequestURI().contains(getApplicationURL() + "/RworkflowDesigner/");
  }

  private static class UnauthenticatedRequestException extends Exception {
    private static final long serialVersionUID = 9173126171348369053L;
  }
}

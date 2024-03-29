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
package org.silverpeas.core.web.mvc.webcomponent;

import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.authentication.SilverpeasSessionOpener;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.token.SynchronizerTokenService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.silverpeas.core.web.mvc.controller.MainSessionController
    .MAIN_SESSION_CONTROLLER_ATT;

/**
 * Servlet that verifies especially the user is authenticated.
 * @author Yohann Chastagnier
 * Date: 20/09/13
 */
public class SilverpeasAuthenticatedHttpServlet extends SilverpeasHttpServlet {

  private static final long serialVersionUID = 3879578969267125005L;

  @Inject
  private SilverpeasSessionOpener silverpeasSessionOpener;
  @Inject
  private SynchronizerTokenService tokenService;

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    try {
      // Gets the main session controller
      MainSessionController mainSessionCtrl = null;
      if (session != null) {
        mainSessionCtrl = (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
      }

      // Verify the user is authenticated
      if (mainSessionCtrl == null) {
        throwUserSessionExpiration();
      } else {

        // Verify that the user can sign in
        verifyUserAuthentication(mainSessionCtrl);

        // Perform the request
        super.service(request, response);
      }
    } catch (UserSessionExpirationException e) {
      SilverLogger.getLogger(this).debug(e.getMessage(), e);

      /*
       The session doesn't contain an authenticated user :
       - delay is passed
       - session expired manually
       */
      // Logging
      if (session != null) {
        SilverLogger.getLogger(this).warn("NewSessionId={0}", session.getId());
        // Clean up the session
        silverpeasSessionOpener.closeSession(session);
      }
      // Redirecting the user
      redirectOrForwardService(request, response,
          ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout"));
    }
  }

  /**
   * Verifies the user authentication rules.
   * @param mainSessionCtrl the main session controller.
   */
  private void verifyUserAuthentication(final MainSessionController mainSessionCtrl) {
    try {
      AuthenticationUserVerifierFactory
          .getUserCanLoginVerifier(mainSessionCtrl.getCurrentUserDetail()).verify();
    } catch (AuthenticationException e) {
      SilverLogger.getLogger(this).debug(e.getMessage(), e);
      throwUserSessionExpiration();
    }
  }

  /**
   * Renews the session security token.
   * @param request the incoming HTTP request
   */
  protected void renewSessionSecurityToken(final HttpServletRequest request) {
    SessionInfo sessionInfo = SessionManagementProvider.getSessionManagement()
        .getSessionInfo(getMainSessionController(request).getSessionId());
    getSynchronizerTokenService().setUpSessionTokens(sessionInfo);
  }

  /**
   * Expires the current user session (even if no user session exists) and stop all treatments.
   * Sends an RuntimeException.
   */
  protected void throwUserSessionExpiration() {
    throw new UserSessionExpirationException();
  }

  /**
   * Used to handle the expiration of the user session.
   */
  private static class UserSessionExpirationException extends RuntimeException {

    private static final long serialVersionUID = -7476590253287182372L;
    // Empty
  }

  /**
   * Retrieves the Main session controller.
   * @param request the incoming HTTP request
   * @return the {@link MainSessionController} instance related by the request.
   */
  protected MainSessionController getMainSessionController(final HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      return (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
    }
    return null;
  }

  protected SynchronizerTokenService getSynchronizerTokenService() {
    return tokenService;
  }

  /**
   * Retrieves the SessionInfo linked to the current request.
   * @param request the incoming HTTP request
   * @return the {@link SessionInfo} instance related by the request
   */
  protected SessionInfo getSessionInfo(final HttpServletRequest request) {
    return SessionManagementProvider.getSessionManagement()
        .getSessionInfo(getMainSessionController(request).getSessionId());
  }
}

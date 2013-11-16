/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.peasCore.servlets;

import com.silverpeas.authentication.SilverpeasSessionOpener;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import org.silverpeas.authentication.exception.AuthenticationException;
import org.silverpeas.authentication.verifier.AuthenticationUserVerifierFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.stratelia.silverpeas.peasCore.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;

/**
 * Servlet that verifies especially the user is authenticated.
 * User: Yohann Chastagnier
 * Date: 20/09/13
 */
public class SilverpeasAuthenticatedHttpServlet extends SilverpeasHttpServlet {
  private static final SilverpeasSessionOpener silverpeasSessionOpener =
      new SilverpeasSessionOpener();

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    try {

      // Gets the main session controller
      HttpSession session = request.getSession(false);
      MainSessionController mainSessionCtrl = null;
      if (session != null) {
        mainSessionCtrl = (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
      }


      // Verify the user is authenticated
      if (mainSessionCtrl == null) {
        throwUserSessionExpiration();
      } else {

        // Verify that the user can login
        try {
          AuthenticationUserVerifierFactory
              .getUserCanLoginVerifier(mainSessionCtrl.getCurrentUserDetail()).verify();
        } catch (AuthenticationException e) {
          throwUserSessionExpiration();
        }

        // Perform the request
        super.service(request, response);
      }
    } catch (UserSessionExpirationException uste) {

      /*
      The session doesn't contains an authenticated user :
      - delay is passed
      - session expired manually
       */

      // Logging
      SilverTrace.warn("peasCore", "SilverpeasAuthenticatedHttpServlet.service",
          "root.MSG_GEN_SESSION_TIMEOUT", "NewSessionId=" + request.getSession(true).getId());
      // Thoroughly clean the session
      silverpeasSessionOpener.closeSession(request);
      // Redirecting the user
      redirectOrForwardService(request, response,
          GeneralPropertiesManager.getString("sessionTimeout"));
    }
  }

  /**
   * Expires the current user session (even if no user session exists) and stop all treatments.
   * Sends an RemoteException.
   */
  protected void throwUserSessionExpiration() {
    throw new UserSessionExpirationException();
  }

  /**
   * Used to handle the expiration of the user session.
   */
  private class UserSessionExpirationException extends RuntimeException {
    private static final long serialVersionUID = -7476590253287182372L;
    // Empty
  }

  /**
   * Retrieves the Main session controller.
   * @param request
   * @return
   */
  protected MainSessionController getMainSessionController(final HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      return (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
    }
    return null;
  }
}

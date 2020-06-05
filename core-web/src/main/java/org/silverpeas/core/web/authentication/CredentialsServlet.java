/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.web.authentication;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.authentication.verifier.UserCanLoginVerifier;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.authentication.credentials.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller tier for credential management (called by MandatoryQuestionChecker)
 *
 * @author Ludovic Bertin
 */
public class CredentialsServlet extends HttpServlet {

  private static final long serialVersionUID = -7586840606648226466L;
  private static final Map<String, FunctionHandler> handlers = new HashMap<>(20);

  static {
    initHandlers();
  }

  /**
   * Load mapping between functions and associated handlers
   */
  private static void initHandlers() {
    // Password change management
    handlers.put("ForcePasswordChange", new ForcePasswordChangeHandler());
    handlers.put("EffectiveChangePassword", new EffectiveChangePasswordHandler());
    handlers.put("EffectiveChangePasswordBeforeExpiration",
        new EffectiveChangePasswordBeforeExpirationHandler());
    handlers.put("ChangeQuestion", new ChangeQuestionHandler());
    handlers.put("ValidateQuestion", new ValidationQuestionHandler());
    handlers.put("LoginQuestion", new LoginQuestionHandler());
    handlers.put("ValidateAnswer", new ValidationAnswerHandler());
    handlers.put("ChangePassword", new ChangePasswordHandler());
    handlers.put("ChangePasswordFromLogin", new ChangePasswordFromLoginHandler());
    handlers.put("EffectiveChangePasswordFromLogin", new EffectiveChangePasswordFromLoginHandler());
    handlers.put("ChangeExpiredPassword", new ChangeExpiredPasswordHandler());
    // Password reset management
    handlers.put("ForgotPassword", new ForgotPasswordHandler());
    handlers.put("ResetPassword", new ResetPasswordHandler());
    handlers.put("ResetLoginPassword", new ResetLoginPasswordHandler());
    handlers.put("SendMessage", new SendMessageHandler());
    // User Registration
    handlers.put("NewRegistration", new NewRegistrationHandler());
    handlers.put("Register", new RegisterHandler());
    // Terms of service
    handlers.put("TermsOfServiceRequest", new TermsOfServiceRequestHandler());
    handlers.put("TermsOfServiceResponse", new TermsOfServiceResponseHandler());
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String function = getFunction(request);
    FunctionHandler handler = handlers.get(function);
    if (handler != null) {
      UserDetail user = null;
      String login = request.getParameter("Login");
      String domainId = request.getParameter("DomainId");
      String destinationPage = "";
      AuthenticationUserVerifierFactory.getUserCanTryAgainToLoginVerifier(user)
          .clearSession(request);
      if (StringUtil.isDefined(login) && StringUtil.isDefined(domainId)) {
        // Verify that the user can login
        UserCanLoginVerifier userStateVerifier = AuthenticationUserVerifierFactory
            .getUserCanLoginVerifier(AuthenticationCredential.newWithAsLogin(login).withAsDomainId(
                    domainId));
        try {
          user = userStateVerifier.getUser();
          userStateVerifier.verify();
        } catch (AuthenticationException e) {
          SilverLogger.getLogger(this).debug(e.getMessage(), e);
          destinationPage = userStateVerifier.getErrorDestination();
        }
      }

      if (!StringUtil.isDefined(destinationPage)) {
        destinationPage = handler.doAction(request);
      }

      if (destinationPage.startsWith("http")) {
        AuthenticationUserVerifierFactory.getUserCanTryAgainToLoginVerifier(user).clearCache();
        response.sendRedirect(response.encodeRedirectURL(destinationPage));
        return;
      }
      RequestDispatcher dispatcher = request.getRequestDispatcher(destinationPage);
      dispatcher.forward(request, response);
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "command not found : " + function);
    }
  }

  /**
   * Retrieves function from path info.
   *
   * @param request HTTP request
   * @return the function as a String
   */
  private String getFunction(HttpServletRequest request) {
    String function = "Error";
    String pathInfo = request.getPathInfo();
    if (pathInfo != null) {
      // remove first '/'
      pathInfo = pathInfo.substring(1);
      function = pathInfo.substring(pathInfo.indexOf('/') + 1);
    }
    return function;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }
}

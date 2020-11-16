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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.Authentication;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.exception
    .AuthenticationNoMoreUserConnectionAttemptException;
import org.silverpeas.core.security.authentication.exception
    .AuthenticationUserMustAcceptTermsOfService;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.authentication.verifier.UserCanLoginVerifier;
import org.silverpeas.core.security.authentication.verifier.UserCanTryAgainToLoginVerifier;
import org.silverpeas.core.security.authentication.verifier.UserMustAcceptTermsOfServiceVerifier;
import org.silverpeas.core.security.authentication.verifier.UserMustChangePasswordVerifier;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasHttpServlet;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static java.text.MessageFormat.format;

/**
 * This servlet listens for incoming authentication requests for Silverpeas.
 *
 * This servlet delegates the authentication process and the HTTP session opening in Silverpeas to
 * the corresponding services. If the authentication and the session opening succeed, the user
 * behind the authentication ask is redirected to its user home page. Otherwise, he's redirected to
 * an authentication failure page (that can the login page enriched with an error message).
 */
public class AuthenticationServlet extends SilverpeasHttpServlet {

  private static final long serialVersionUID = -8695946617361150513L;
  private static final String SSO_UNEXISTANT_USER_ACCOUNT = "Error_SsoOnUnexistantUserAccount";
  private static final String TECHNICAL_ISSUE = "2";
  private static final String INCORRECT_LOGIN_PWD = "1";
  private static final String INCORRECT_LOGIN_PWD_DOMAIN = "6";
  private static final String LOGIN_ERROR_PAGE = "/Login?ErrorCode=";
  private static final int COOKIE_TIMELIFE = 31536000;

  @Inject
  private AuthenticationService authService;
  @Inject
  private SilverpeasSessionOpener silverpeasSessionOpener;
  @Inject
  private CredentialEncryption credentialEncryption;
  @Inject
  private MandatoryQuestionChecker mandatoryQuestionChecker;

  private final SilverLogger logger = SilverLogger.getLogger(this);

  /**
   * Ask for an authentication for the user behind the incoming HTTP request from a form.
   *
   * @param servletRequest the HTTP request.
   * @param response the HTTP response.
   * @throws IOException when an error occurs while processing the request or sending the response.
   * @throws ServletException if the request for the POST couldn't be handled.
   */
  @Override
  public void doPost(HttpServletRequest servletRequest, HttpServletResponse response)
      throws IOException, ServletException {
    HttpRequest request = HttpRequest.decorate(servletRequest);

    final UserSessionStatus userSessionStatus = existOpenedUserSession(servletRequest);
    final AuthenticationParameters authenticationParameters = new AuthenticationParameters(request);
    if (userSessionStatus.isValid()) {
      final User connectedUser = userSessionStatus.getInfo().getUserDetail();
      // Prevent trashing the user session on SSO authentication when user behind the current
      // session is the one authenticated for SSO.
      if (authenticationParameters.isSsoMode() &&
          connectedUser.getLogin().equals(authenticationParameters.getLogin()) &&
          connectedUser.getDomainId().equals(authenticationParameters.getDomainId())) {
        SilverLogger.getLogger("silverpeas.sso").debug(() -> format(
            "SSO Authentication of PRINCIPAL {0} on domain {1}, keeping existing user session alive {2}",
            connectedUser.getLogin(), connectedUser.getDomainId(), userSessionStatus.getInfo().getSessionId()));
        forward(request, response, "/Login");
        return;
      }
      final HttpSession session = servletRequest.getSession(false);
      silverpeasSessionOpener.closeSession(session);
    }

    // Get an existing session or creates a new one.
    HttpSession session = request.getSession();

    if (!StringUtil.isDefined(request.getCharacterEncoding())) {
      request.setCharacterEncoding(Charsets.UTF_8.name());
    }
    if (!userSessionStatus.isValid() && request.isWithinAnonymousUserSession()) {
      // invalidate previous Wildfly session (directly because anonymous session are not managed
      // by Silverpeas SessionManager)
      session.invalidate();
      // create a new Wildfly session to process correctly the authentication process
      request.getSession(true);
    }

    String authenticationKey = authenticate(request, authenticationParameters);

    // Verify if the user can try again to login.
    UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier
        = AuthenticationUserVerifierFactory.getUserCanTryAgainToLoginVerifier(
            authenticationParameters.getCredential());
    userCanTryAgainToLoginVerifier.clearSession(request);

    if (authService.isInError(authenticationKey)) {
      processError(authenticationKey, request, response, authenticationParameters,
          userCanTryAgainToLoginVerifier);
    } else {
      openNewSession(authenticationKey, request, response, authenticationParameters,
          userCanTryAgainToLoginVerifier);
    }
  }

  private void openNewSession(String authenticationKey,
      HttpRequest request,
      HttpServletResponse response,
      AuthenticationParameters authenticationParameters,
      UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier)
      throws ServletException, IOException {
    // Clearing user connection attempt cache.
    userCanTryAgainToLoginVerifier.clearCache();

    if (authenticationParameters.getDomainId() != null) {
      storeDomain(response, authenticationParameters);
    }

    storeLogin(response, authenticationParameters);

    if (request.getAttribute("skipTermsOfServiceAcceptance") == null) {
      UserMustAcceptTermsOfServiceVerifier verifier = AuthenticationUserVerifierFactory.
          getUserMustAcceptTermsOfServiceVerifier(authenticationParameters.getCredential());
      try {
        verifier.verify();
      } catch (AuthenticationUserMustAcceptTermsOfService authenticationUserMustAcceptTermsOfService) {
        logger.warn(authenticationUserMustAcceptTermsOfService.getMessage(),
            authenticationUserMustAcceptTermsOfService);
        forward(request, response, verifier.getDestination(request));
        return;
      }
    }

    String destination = mandatoryQuestionChecker.check(request, authenticationKey);
    if (StringUtil.isDefined(destination)) {
      forward(request, response, destination);
      return;
    }

    String absoluteUrl = silverpeasSessionOpener.openSession(request, authenticationKey);
    // fetch the new opened session
    HttpSession session = request.getSession(false);
    session.
        setAttribute("Silverpeas_pwdForHyperlink", authenticationParameters.getPassword());

    response.sendRedirect(response.encodeRedirectURL(absoluteUrl));
  }

  private void processError(final String errorCode,
      final HttpRequest request,
      final HttpServletResponse response,
      final AuthenticationParameters authenticationParameters,
      final UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier)
      throws ServletException, IOException {
    String url = "";
    HttpSession session = request.getSession();
    if (authenticationParameters.isCasMode()) {
      url = "/admin/jsp/casAuthenticationError.jsp";
    } else {
      if (AuthenticationService.ERROR_INCORRECT_LOGIN_PWD.equals(errorCode) ||
          AuthenticationService.ERROR_INCORRECT_LOGIN_PWD_DOMAIN.equals(errorCode)) {
        url = processBadLogin(errorCode, request, response, authenticationParameters,
            userCanTryAgainToLoginVerifier);
      } else if (UserCanLoginVerifier.ERROR_USER_ACCOUNT_BLOCKED.equals(errorCode) ||
          UserCanLoginVerifier.ERROR_USER_ACCOUNT_DEACTIVATED.equals(errorCode)) {
        url = processBadAccountState(errorCode, response, authenticationParameters,
            userCanTryAgainToLoginVerifier);
      } else if (AuthenticationService.ERROR_PWD_EXPIRED.equals(errorCode) ||
          AuthenticationService.ERROR_PWD_MUST_BE_CHANGED.equals(errorCode)) {
        url = processPasswordExpiration(authenticationParameters, session);
      } else if (UserMustChangePasswordVerifier.ERROR_PWD_MUST_BE_CHANGED_ON_FIRST_LOGIN
          .equals(errorCode)) {
        // User has been successfully authenticated, but he has to change his password on his
        // first login and login / domain id can be stored
        storeLogin(response, authenticationParameters);
        storeDomain(response, authenticationParameters);
        url = AuthenticationUserVerifierFactory.getUserMustChangePasswordVerifier(
            authenticationParameters.getCredential()).getDestinationOnFirstLogin(request);
        forward(request, response, url);
        return;
      } else if (authenticationParameters.isSsoMode()) {
        // User has been successfully authenticated on AD, but he has no user account on Silverpeas
        // -> login / domain id can be stored
        storeDomain(response, authenticationParameters);
        storeLogin(response, authenticationParameters);
        url = LOGIN_ERROR_PAGE + SSO_UNEXISTANT_USER_ACCOUNT;
      } else {
        url = LOGIN_ERROR_PAGE + TECHNICAL_ISSUE;
      }
      if (url != null) {
        String paramDelimiter = (url.contains("?") ? "&" : "?");
        url += paramDelimiter + LoginServlet.PARAM_DOMAINID + "=" +
            authenticationParameters.getDomainId();
      }
    }
    response.sendRedirect(
        response.encodeRedirectURL(URLUtil.getFullApplicationURL(request) + url));
  }

  private String processPasswordExpiration(final AuthenticationParameters authenticationParameters,
      final HttpSession session) {
    final String url;
    String allowPasswordChange = (String) session.getAttribute(
        Authentication.PASSWORD_CHANGE_ALLOWED);
    if (StringUtil.getBooleanValue(allowPasswordChange)) {
      SettingBundle settings = ResourceLocator.getSettingBundle(
          "org.silverpeas.authentication.settings.passwordExpiration");
      url = settings.getString("passwordExpiredURL") + "?login=" + authenticationParameters.
          getLogin() + "&domainId=" + authenticationParameters.getDomainId();
    } else {
      url = LOGIN_ERROR_PAGE + AuthenticationService.ERROR_PWD_EXPIRED;
    }
    return url;
  }

  private String processBadAccountState(final String errorCode, final HttpServletResponse response,
      final AuthenticationParameters authenticationParameters,
      final UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier) {
    String url = null;
    if (userCanTryAgainToLoginVerifier.isActivated() || StringUtil.isDefined(
        userCanTryAgainToLoginVerifier.getUser().getId())) {
      // If user can try again to login verifier is activated or if the user has been found
      // from credential, the login and the domain are stored
      storeLogin(response, authenticationParameters);
      storeDomain(response, authenticationParameters);
      url = AuthenticationUserVerifierFactory
          .getUserCanLoginVerifier(userCanTryAgainToLoginVerifier.getUser())
          .getErrorDestination();
    } else {
      if (AuthenticationService.ERROR_INCORRECT_LOGIN_PWD.equals(errorCode)) {
        url = LOGIN_ERROR_PAGE + INCORRECT_LOGIN_PWD;
      } else if (AuthenticationService.ERROR_INCORRECT_LOGIN_PWD_DOMAIN.equals(errorCode)) {
        url = LOGIN_ERROR_PAGE + INCORRECT_LOGIN_PWD_DOMAIN;
      }
    }
    return url;
  }

  private String processBadLogin(final String errorCode, final HttpRequest request,
      final HttpServletResponse response, final AuthenticationParameters authenticationParameters,
      final UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier) {
    String url = null;
    try {
      if (userCanTryAgainToLoginVerifier.isActivated()) {
        storeLogin(response, authenticationParameters);
        storeDomain(response, authenticationParameters);
      }
      if (AuthenticationService.ERROR_INCORRECT_LOGIN_PWD.equals(errorCode)) {
        url = userCanTryAgainToLoginVerifier.verify()
            .performRequestUrl(request, LOGIN_ERROR_PAGE + INCORRECT_LOGIN_PWD);
      } else if (AuthenticationService.ERROR_INCORRECT_LOGIN_PWD_DOMAIN.equals(errorCode)) {
        url = userCanTryAgainToLoginVerifier.verify()
            .performRequestUrl(request, LOGIN_ERROR_PAGE + INCORRECT_LOGIN_PWD_DOMAIN);
      }
    } catch (AuthenticationNoMoreUserConnectionAttemptException e) {
      logger.error(e.getMessage(), e);
      url = userCanTryAgainToLoginVerifier.getErrorDestination();
    }
    return url;
  }

  private void forward(HttpServletRequest request, HttpServletResponse response,
      String destination) throws ServletException, IOException {
    RequestDispatcher dispatcher = request.getRequestDispatcher(destination);
    dispatcher.forward(request, response);
  }

  private void storeLogin(HttpServletResponse response, AuthenticationParameters params) {
    if (!isAnonymousAuthentication(params)) {
      String sLogin = params.getLogin();
      boolean secured = params.isSecuredAccess();
      if (params.isNewEncryptionMode()) {
        writeCookie(response, "var1", credentialEncryption.encode(sLogin), -1, secured);
        writeCookie(response, "var1", credentialEncryption.encode(sLogin), COOKIE_TIMELIFE, secured);
      } else {
        writeCookie(response, "svpLogin", sLogin, -1, secured);
        writeCookie(response, "svpLogin", sLogin, COOKIE_TIMELIFE, secured);
      }
    }
  }

  private void storeDomain(HttpServletResponse response, AuthenticationParameters params) {
    if (!isAnonymousAuthentication(params)) {
      String sDomainId = params.getDomainId();
      boolean secured = params.isSecuredAccess();
      writeCookie(response, "defaultDomain", sDomainId, -1, secured);
      writeCookie(response, "defaultDomain", sDomainId, COOKIE_TIMELIFE, secured);
    }
  }

  private boolean isAnonymousAuthentication(AuthenticationParameters params) {
    User anonymous = UserDetail.getAnonymousUser();
    return anonymous != null && anonymous.getLogin().equals(params.getLogin());
  }

  private String authenticate(HttpServletRequest request,
      AuthenticationParameters authenticationParameters) {
    String key = request.getParameter("TestKey");
    if (!StringUtil.isDefined(key)) {
      AuthenticationCredential credential = AuthenticationCredential.newWithAsLogin(
          authenticationParameters.getLogin());
      if (authenticationParameters.isUserByInternalAuthTokenMode() || authenticationParameters.
          isSsoMode() || authenticationParameters.isCasMode()) {
        key = authService.authenticate(
            credential.withAsDomainId(authenticationParameters.getDomainId()));
      } else if (authenticationParameters.isSocialNetworkMode()) {
        key = authService.authenticate(credential.withAsDomainId(authenticationParameters.
            getDomainId()));
      } else {
        key = authService.authenticate(credential
            .withAsPassword(authenticationParameters.getPassword())
            .withAsDomainId(authenticationParameters.getDomainId()));
      }
      authenticationParameters.setCredential(credential);
      HttpSession session = request.getSession(false);
      for (Map.Entry<String, Serializable> capability : credential.getCapabilities().entrySet()) {
        session.setAttribute(capability.getKey(), capability.getValue());
      }
      return key;
    }
    return null;
  }

  /**
   * Ask for an authentication for the user behind the incoming HTTP request.
   *
   * @param request the HTTP request.
   * @param response the HTTP response.
   * @throws ServletException if the servlet fails to answer the request.
   * @throws IOException if an IO error occurs with the client.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {
    doPost(request, response);
  }

  private void writeCookie(HttpServletResponse response, String name, String value, int duration,
      boolean secure) {
    String cookieValue;
    try {
      cookieValue = URLEncoder.encode(value, Charsets.UTF_8.name());
    } catch (UnsupportedEncodingException ex) {
      logger.error(ex.getMessage(), ex);
      cookieValue = value;
    }
    Cookie cookie = new Cookie(name, cookieValue);
    cookie.setSecure(secure);
    cookie.setMaxAge(duration);
    cookie.setPath("/");
    response.addCookie(cookie);
  }
}

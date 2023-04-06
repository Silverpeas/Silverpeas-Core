/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.web.authentication;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationProtocol;
import org.silverpeas.core.security.authentication.AuthenticationResponse;
import org.silverpeas.core.security.authentication.AuthenticationResponse.Status;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.exception.AuthenticationNoMoreUserConnectionAttemptException;
import org.silverpeas.core.security.authentication.exception.AuthenticationUserMustAcceptTermsOfService;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.authentication.verifier.UserCanTryAgainToLoginVerifier;
import org.silverpeas.core.security.authentication.verifier.UserMustAcceptTermsOfServiceVerifier;
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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static java.text.MessageFormat.format;

/**
 * This servlet listens for incoming authentication requests for Silverpeas.
 * <p>
 * This servlet delegates the authentication process and the HTTP session opening in Silverpeas to
 * the corresponding services. If the authentication and the session opening succeed, the user
 * behind the authentication ask is redirected to its user home page. Otherwise, he's redirected to
 * an authentication failure page (that can the login page enriched with an error message).
 * </p>
 */
public class AuthenticationServlet extends SilverpeasHttpServlet {

  private static final long serialVersionUID = -8695946617361150513L;
  private static final String SSO_NON_EXISTING_USER_ACCOUNT = "Error_SsoOnUnexistantUserAccount";
  private static final String LOGIN_ERROR_PAGE = "/Login?ErrorCode=";
  private static final int COOKIE_TIME_LIFE = 31536000;

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
   * @param servletRequest the HTTP request.
   * @param response the HTTP response.
   */
  @Override
  public void doPost(HttpServletRequest servletRequest, HttpServletResponse response) {
    try {
      HttpRequest request = HttpRequest.decorate(servletRequest);

      final UserSessionStatus userSessionStatus = existOpenedUserSession(servletRequest);
      final AuthenticationParameters authenticationParameters =
          new AuthenticationParameters(request);
      if (userSessionStatus.isValid()) {
        final User connectedUser = userSessionStatus.getInfo().getUserDetail();
        // Prevent trashing the user session on SSO authentication when user behind the current
        // session is the one authenticated for SSO.
        if (authenticationParameters.isSsoMode() &&
            connectedUser.getLogin().equals(authenticationParameters.getLogin()) &&
            connectedUser.getDomainId().equals(authenticationParameters.getDomainId())) {
          SilverLogger.getLogger("silverpeas.sso")
              .debug(() -> format(
                  "SSO Authentication of PRINCIPAL {0} on domain {1}, keeping existing user " +
                      "session alive {2}",
                  connectedUser.getLogin(), connectedUser.getDomainId(),
                  userSessionStatus.getInfo().getSessionId()));
          forward(request, response, "/Login");
          return;
        }
        final HttpSession session = servletRequest.getSession(false);
        silverpeasSessionOpener.closeSession(session);
      }

      // Get an existing HTTP session or creates a new one.
      request.getSession();

      if (!StringUtil.isDefined(request.getCharacterEncoding())) {
        request.setCharacterEncoding(Charsets.UTF_8.name());
      }
      if (!userSessionStatus.isValid() && request.isWithinAnonymousUserSession()) {
        // previously the user accessed anonymously Silverpeas and hence has an anonymous
        // Silverpeas session. It requires then to renew the HTTP session to open upon it a new
        // Silverpeas session.
        renewHttpSession(request);
      }

      AuthenticationResponse result = authenticate(request, authenticationParameters);

      // Verify if the user can try again to login.
      UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier =
          AuthenticationUserVerifierFactory.getUserCanTryAgainToLoginVerifier(
              authenticationParameters.getCredential());
      userCanTryAgainToLoginVerifier.clearSession(request);

      if (result == null || result.getStatus().isInError()) {
        processError(result, request, response, authenticationParameters,
            userCanTryAgainToLoginVerifier);
      } else {
        openNewSession(result.getToken(), request, response, authenticationParameters,
            userCanTryAgainToLoginVerifier);
      }
    } catch (ServletException | IOException e) {
      SilverLogger.getLogger(this).error(e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void openNewSession(final String token,
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

    String destination = mandatoryQuestionChecker.check(request, token);
    if (StringUtil.isDefined(destination)) {
      forward(request, response, destination);
      return;
    }

    String absoluteUrl = silverpeasSessionOpener.openSession(request, token);
    // fetch the new opened session
    HttpSession session = request.getSession(false);
    session.
        setAttribute("Silverpeas_pwdForHyperlink", authenticationParameters.getPassword());

    response.sendRedirect(response.encodeRedirectURL(absoluteUrl));
  }

  private void processError(final AuthenticationResponse error,
      final HttpRequest request,
      final HttpServletResponse response,
      final AuthenticationParameters authenticationParameters,
      final UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier)
      throws ServletException, IOException {
    String url;
    if (authenticationParameters.isCasMode()) {
      url = "/admin/jsp/casAuthenticationError.jsp";
    } else if (error != null) {
      url = processUrlForError(error, request, response, authenticationParameters,
          userCanTryAgainToLoginVerifier);
      if (url != null) {
        String paramDelimiter = (url.contains("?") ? "&" : "?");
        url += paramDelimiter + LoginServlet.PARAM_DOMAINID + "=" +
            authenticationParameters.getDomainId();
      } else {
        return;
      }
    } else {
      url = LOGIN_ERROR_PAGE + Status.UNKNOWN_FAILURE;
    }
    response.sendRedirect(
        response.encodeRedirectURL(URLUtil.getFullApplicationURL(request) + url));
  }

  private String processUrlForError(final AuthenticationResponse error, final HttpRequest request,
      final HttpServletResponse response,
      final AuthenticationParameters authenticationParameters,
      final UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier)
      throws ServletException, IOException {
    final String url;
    Status status = error.getStatus();
    if (status == Status.BAD_LOGIN_PASSWORD ||
        status == Status.BAD_LOGIN_PASSWORD_DOMAIN) {
      url = processBadLogin(status, request, response, authenticationParameters,
          userCanTryAgainToLoginVerifier);
    } else if (status == Status.USER_ACCOUNT_BLOCKED ||
        status == Status.USER_ACCOUNT_DEACTIVATED) {
      url = processBadAccountState(status, response, authenticationParameters,
          userCanTryAgainToLoginVerifier);
    } else if (status == Status.PASSWORD_EXPIRED ||
        status == Status.PASSWORD_TO_CHANGE) {
      HttpSession session = request.getSession();
      url = processPasswordExpiration(authenticationParameters, session);
    } else if (status == Status.PASSWORD_TO_CHANGE_ON_FIRST_LOGIN) {
      // User has been successfully authenticated, but he has to change his password on his
      // first login and login / domain id can be stored
      storeLogin(response, authenticationParameters);
      storeDomain(response, authenticationParameters);
      url = AuthenticationUserVerifierFactory.getUserMustChangePasswordVerifier(
          authenticationParameters.getCredential()).getDestinationOnFirstLogin(request);
      forward(request, response, url);
      return null;
    } else if (authenticationParameters.isSsoMode()) {
      // User has been successfully authenticated on AD, but he has no user account on Silverpeas
      // -> login / domain id can be stored
      storeDomain(response, authenticationParameters);
      storeLogin(response, authenticationParameters);
      url = LOGIN_ERROR_PAGE + SSO_NON_EXISTING_USER_ACCOUNT;
    } else {
      url = LOGIN_ERROR_PAGE + status.getCode();
    }
    return url;
  }

  private String processPasswordExpiration(final AuthenticationParameters authenticationParameters,
      final HttpSession session) {
    final String url;
    String allowPasswordChange = (String) session.getAttribute(
        AuthenticationProtocol.PASSWORD_CHANGE_ALLOWED);
    if (StringUtil.getBooleanValue(allowPasswordChange)) {
      SettingBundle settings = ResourceLocator.getSettingBundle(
          "org.silverpeas.authentication.settings.passwordExpiration");
      url = settings.getString("passwordExpiredURL") + "?login=" + authenticationParameters.
          getLogin() + "&domainId=" + authenticationParameters.getDomainId();
    } else {
      url = LOGIN_ERROR_PAGE + Status.PASSWORD_EXPIRED;
    }
    return url;
  }

  private String processBadAccountState(final Status error,
      final HttpServletResponse response, final AuthenticationParameters authenticationParameters,
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
    } else if (error == Status.BAD_LOGIN_PASSWORD || error == Status.BAD_LOGIN_PASSWORD_DOMAIN) {
      url = LOGIN_ERROR_PAGE + error;
    }
    return url;
  }

  private String processBadLogin(final Status error, final HttpRequest request,
      final HttpServletResponse response, final AuthenticationParameters authenticationParameters,
      final UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier) {
    String url = null;
    try {
      if (userCanTryAgainToLoginVerifier.isActivated()) {
        storeLogin(response, authenticationParameters);
        storeDomain(response, authenticationParameters);
      }
      if (error == Status.BAD_LOGIN_PASSWORD || error == Status.BAD_LOGIN_PASSWORD_DOMAIN) {
        url = userCanTryAgainToLoginVerifier.verify()
            .performRequestUrl(request, LOGIN_ERROR_PAGE + error);
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
    if (isNotAnonymousAuthentication(params)) {
      String sLogin = params.getLogin();
      boolean secured = params.isSecuredAccess();
      if (params.isNewEncryptionMode()) {
        writeCookie(response, "var1", credentialEncryption.encode(sLogin), -1, secured);
        writeCookie(response, "var1", credentialEncryption.encode(sLogin), COOKIE_TIME_LIFE,
            secured);
      } else {
        writeCookie(response, "svpLogin", sLogin, -1, secured);
        writeCookie(response, "svpLogin", sLogin, COOKIE_TIME_LIFE, secured);
      }
    }
  }

  private void storeDomain(HttpServletResponse response, AuthenticationParameters params) {
    if (isNotAnonymousAuthentication(params)) {
      String sDomainId = params.getDomainId();
      boolean secured = params.isSecuredAccess();
      writeCookie(response, "defaultDomain", sDomainId, -1, secured);
      writeCookie(response, "defaultDomain", sDomainId, COOKIE_TIME_LIFE, secured);
    }
  }

  private boolean isNotAnonymousAuthentication(AuthenticationParameters params) {
    User anonymous = UserDetail.getAnonymousUser();
    return anonymous == null || !anonymous.getLogin().equals(params.getLogin());
  }

  private AuthenticationResponse authenticate(HttpServletRequest request,
      AuthenticationParameters authenticationParameters) {
    final String key = request.getParameter("TestKey");
    if (StringUtil.isNotDefined(key)) {
      AuthenticationCredential credential = AuthenticationCredential.newWithAsLogin(
          authenticationParameters.getLogin());
      final AuthenticationResponse result;
      if (authenticationParameters.isUserByInternalAuthTokenMode() || authenticationParameters.
          isSsoMode() || authenticationParameters.isCasMode()) {
        result = authService.authenticate(
            credential.withAsDomainId(authenticationParameters.getDomainId()));
      } else if (authenticationParameters.isSocialNetworkMode()) {
        result = authService.authenticate(credential.withAsDomainId(authenticationParameters.
            getDomainId()));
      } else {
        result = authService.authenticate(credential
            .withAsPassword(authenticationParameters.getPassword())
            .withAsDomainId(authenticationParameters.getDomainId()));
      }
      authenticationParameters.setCredential(credential);
      HttpSession session = request.getSession(false);
      for (Map.Entry<String, Serializable> capability : credential.getCapabilities().entrySet()) {
        session.setAttribute(capability.getKey(), capability.getValue());
      }
      return result;
    }
    return null;
  }

  /**
   * Ask for an authentication for the user behind the incoming HTTP request.
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
    String cookieValue = URLEncoder.encode(value, Charsets.UTF_8);
    Cookie cookie = new Cookie(name, cookieValue);
    cookie.setSecure(secure);
    cookie.setMaxAge(duration);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  private void renewHttpSession(final HttpRequest request) {
    HttpSession session = request.getSession(false);
    Map<String, Object> attrs = new HashMap<>();
    session.getAttributeNames().asIterator().forEachRemaining(a -> {
      if (a.startsWith("Redirect") || a.equals("gotoNew")) {
        attrs.put(a, session.getAttribute(a));
      }
    });
    session.invalidate();

    HttpSession newSession = request.getSession(true);
    attrs.forEach(newSession::setAttribute);
  }
}

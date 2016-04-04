/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.authentication;

import org.silverpeas.core.util.URLUtil;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.core.security.authentication.Authentication;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.exception.AuthenticationNoMoreUserConnectionAttemptException;
import org.silverpeas.core.security.authentication.exception.AuthenticationUserMustAcceptTermsOfService;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.authentication.verifier.UserCanLoginVerifier;
import org.silverpeas.core.security.authentication.verifier.UserCanTryAgainToLoginVerifier;
import org.silverpeas.core.security.authentication.verifier.UserMustAcceptTermsOfServiceVerifier;
import org.silverpeas.core.security.authentication.verifier.UserMustChangePasswordVerifier;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * This servlet listens for incoming authentication requests for Silverpeas.
 *
 * This servlet delegates the authentication process and the HTTP session opening in Silverpeas to
 * the corresponding services. If the authentication and the session opening succeed, the user
 * behind the authentication ask is redirected to its user home page. Otherwise, he's redirected to
 * an authentication failure page (that can the login page enriched with an error message).
 */
public class AuthenticationServlet extends HttpServlet {

  @Inject
  private AuthenticationService authService;
  @Inject
  private SilverpeasSessionOpener silverpeasSessionOpener;
  @Inject
  private CredentialEncryption credentialEncryption;
  @Inject
  private MandatoryQuestionChecker mandatoryQuestionChecker;
  private static final long serialVersionUID = -8695946617361150513L;
  private static final String SSO_UNEXISTANT_USER_ACCOUNT = "Error_SsoOnUnexistantUserAccount";
  private static final String TECHNICAL_ISSUE = "2";
  private static final String INCORRECT_LOGIN_PWD = "1";
  private static final String INCORRECT_LOGIN_PWD_DOMAIN = "6";

  /**
   * Ask for an authentication for the user behind the incoming HTTP request from a form.
   *
   * @param servletRequest the HTTP request.
   * @param servletResponse the HTTP response.
   * @throws IOException when an error occurs while processing the request or sending the response.
   * @throws javax.servlet.ServletException
   */
  @Override
  public void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws IOException, ServletException {
    HttpRequest request = HttpRequest.decorate(servletRequest);
    // get an existing session or creates a new one.
    HttpSession session = request.getSession();

    if (!StringUtil.isDefined(request.getCharacterEncoding())) {
      request.setCharacterEncoding(CharEncoding.UTF_8);
    }
    if (request.isWithinAnonymousUserSession()) {
      session.invalidate();
    }

    // Get the authentication settings
    SettingBundle authenticationSettings = ResourceLocator.getSettingBundle(
        "org.silverpeas.authentication.settings.authenticationSettings");
    boolean securedAccess = request.isSecure();
    boolean isNewEncryptMode = StringUtil.isDefined(request.getParameter("Var2"));
    AuthenticationParameters authenticationParameters = new AuthenticationParameters(request);
    String domainId = getDomain(request, authenticationParameters, authenticationSettings);
    AuthenticationCredential credential = AuthenticationCredential.newWithAsLogin(
        authenticationParameters.getLogin())
        .withAsPassword(authenticationParameters.getPassword())
        .withAsDomainId(domainId);

    String authenticationKey = authenticate(request, authenticationParameters, domainId);
    String url = "";

    // Verify if the user can try again to login.
    UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier
        = AuthenticationUserVerifierFactory.getUserCanTryAgainToLoginVerifier(credential);
    userCanTryAgainToLoginVerifier.clearSession(request);

    if (!authService.isInError(authenticationKey)) {

      // Clearing user connection attempt cache.
      userCanTryAgainToLoginVerifier.clearCache();

      if (domainId != null) {
        storeDomain(servletResponse, domainId, securedAccess);
      }
      storeLogin(servletResponse, isNewEncryptMode, authenticationParameters.getLogin(),
          securedAccess);

      // if required by user, store password in cookie
      storePassword(servletResponse, authenticationParameters.getStoredPassword(), isNewEncryptMode,
          authenticationParameters.getClearPassword(), securedAccess);

      if (request.getAttribute("skipTermsOfServiceAcceptance") == null) {
        UserMustAcceptTermsOfServiceVerifier verifier = AuthenticationUserVerifierFactory.
            getUserMustAcceptTermsOfServiceVerifier(credential);
        try {
          verifier.verify();
        } catch (AuthenticationUserMustAcceptTermsOfService authenticationUserMustAcceptTermsOfService) {
          forward(request, servletResponse, verifier.getDestination(request));
          return;
        }
      }

      if (mandatoryQuestionChecker.check(request, authenticationKey)) {
        forward(request, servletResponse, mandatoryQuestionChecker.getDestination());
        return;
      }

      String absoluteUrl = silverpeasSessionOpener.openSession(request, authenticationKey);
      // fetch the new opened session
      session = request.getSession(false);
      session.
          setAttribute("Silverpeas_pwdForHyperlink", authenticationParameters.getClearPassword());
      writeSessionCookie(servletResponse, session, securedAccess);
      servletResponse.sendRedirect(servletResponse.encodeRedirectURL(absoluteUrl));
      return;
    }
    // Authentication failed : remove password from cookies to avoid infinite loop
    removeStoredPassword(servletResponse, securedAccess);
    if (authenticationParameters.isCasMode()) {
      url = "/admin/jsp/casAuthenticationError.jsp";
    } else {
      if (AuthenticationService.ERROR_INCORRECT_LOGIN_PWD.equals(authenticationKey) ||
          AuthenticationService.ERROR_INCORRECT_LOGIN_PWD_DOMAIN.equals(authenticationKey)) {
        try {
          if (userCanTryAgainToLoginVerifier.isActivated()) {
            storeLogin(servletResponse, isNewEncryptMode, authenticationParameters.getLogin(),
                securedAccess);
            storeDomain(servletResponse, domainId, securedAccess);
          }
          if (AuthenticationService.ERROR_INCORRECT_LOGIN_PWD.equals(authenticationKey)) {
            url = userCanTryAgainToLoginVerifier.verify()
              .performRequestUrl(request, "/Login.jsp?ErrorCode=" + INCORRECT_LOGIN_PWD);
          } else if (AuthenticationService.ERROR_INCORRECT_LOGIN_PWD_DOMAIN.equals(authenticationKey)) {
            url = userCanTryAgainToLoginVerifier.verify()
                .performRequestUrl(request, "/Login.jsp?ErrorCode=" + INCORRECT_LOGIN_PWD_DOMAIN);
          }
        } catch (AuthenticationNoMoreUserConnectionAttemptException e) {
          url = userCanTryAgainToLoginVerifier.getErrorDestination();
        }
      } else if (UserCanLoginVerifier.ERROR_USER_ACCOUNT_BLOCKED.equals(authenticationKey) ||
          UserCanLoginVerifier.ERROR_USER_ACCOUNT_DEACTIVATED.equals(authenticationKey)) {
        if (userCanTryAgainToLoginVerifier.isActivated() || StringUtil.isDefined(
            userCanTryAgainToLoginVerifier.getUser().getId())) {
          // If user can try again to login verifier is activated or if the user has been found
          // from credential, the login and the domain are stored
          storeLogin(servletResponse, isNewEncryptMode, authenticationParameters.getLogin(),
              securedAccess);
          storeDomain(servletResponse, domainId, securedAccess);
          url = AuthenticationUserVerifierFactory
              .getUserCanLoginVerifier(userCanTryAgainToLoginVerifier.getUser())
              .getErrorDestination();
        } else {
          if (AuthenticationService.ERROR_INCORRECT_LOGIN_PWD.equals(authenticationKey)) {
            url = "/Login.jsp?ErrorCode=" + INCORRECT_LOGIN_PWD;
          } else if (AuthenticationService.ERROR_INCORRECT_LOGIN_PWD_DOMAIN.equals(authenticationKey)) {
            url = "/Login.jsp?ErrorCode=" + INCORRECT_LOGIN_PWD_DOMAIN;
          }
        }
      } else if (AuthenticationService.ERROR_PWD_EXPIRED.equals(authenticationKey)) {
        String allowPasswordChange = (String) session.getAttribute(
            Authentication.PASSWORD_CHANGE_ALLOWED);
        if (StringUtil.getBooleanValue(allowPasswordChange)) {
          SettingBundle settings = ResourceLocator.getSettingBundle(
              "org.silverpeas.authentication.settings.passwordExpiration");
          url = settings.getString("passwordExpiredURL") + "?login=" + authenticationParameters.
              getLogin() + "&domainId=" + domainId;
        } else {
          url = "/Login.jsp?ErrorCode=" + AuthenticationService.ERROR_PWD_EXPIRED;
        }
      } else if (AuthenticationService.ERROR_PWD_MUST_BE_CHANGED.equals(authenticationKey)) {
        String allowPasswordChange = (String) session.getAttribute(
            Authentication.PASSWORD_CHANGE_ALLOWED);
        if (StringUtil.getBooleanValue(allowPasswordChange)) {
          SettingBundle settings = ResourceLocator.getSettingBundle(
              "org.silverpeas.authentication.settings.passwordExpiration");
          url = settings.getString("passwordExpiredURL") + "?login=" + authenticationParameters.
              getLogin() + "&domainId=" + domainId;
        } else {
          url = "/Login.jsp?ErrorCode=" + AuthenticationService.ERROR_PWD_EXPIRED;
        }
      } else if (UserMustChangePasswordVerifier.ERROR_PWD_MUST_BE_CHANGED_ON_FIRST_LOGIN
          .equals(authenticationKey)) {
        // User has been successfully authenticated, but he has to change his password on his
        // first login and login / domain id can be stored
        storeLogin(servletResponse, isNewEncryptMode, authenticationParameters.getLogin(),
            securedAccess);
        storeDomain(servletResponse, domainId, securedAccess);
        url = AuthenticationUserVerifierFactory.getUserMustChangePasswordVerifier(credential)
            .getDestinationOnFirstLogin(request);
        forward(request, servletResponse, url);
        return;
      } else if (authenticationParameters.isSsoMode()) {
        // User has been successfully authenticated on AD, but he has no user account on Silverpeas
        // -> login / domain id can be stored
        storeDomain(servletResponse, domainId, securedAccess);
        storeLogin(servletResponse, isNewEncryptMode, authenticationParameters.getLogin(),
            securedAccess);
        url = "/Login.jsp?ErrorCode=" + SSO_UNEXISTANT_USER_ACCOUNT;
      } else {
        url = "/Login.jsp?ErrorCode=" + TECHNICAL_ISSUE;
      }
    }
    servletResponse.sendRedirect(
        servletResponse.encodeRedirectURL(URLUtil.getFullApplicationURL(request) + url));
  }

  /**
   * Centralization.
   *
   * @param request
   * @param response
   * @param destination
   */
  private void forward(HttpServletRequest request, HttpServletResponse response,
      String destination) throws ServletException, IOException {
    RequestDispatcher dispatcher = request.getRequestDispatcher(destination);
    dispatcher.forward(request, response);
  }

  private void storePassword(HttpServletResponse response, String shoudStorePasword,
      boolean newEncryptMode, String clearPassword, boolean secured) {
    if (StringUtil.getBooleanValue(shoudStorePasword)) {
      if (newEncryptMode) {
        writeCookie(response, "var2", credentialEncryption.encode(clearPassword), -1, secured);
        writeCookie(response, "var2", credentialEncryption.encode(clearPassword), 31536000,
            secured);
      } else {
        writeCookie(response, "svpPassword", credentialEncryption.encode(clearPassword), -1,
            secured);
        writeCookie(response, "svpPassword", credentialEncryption.encode(clearPassword), 31536000,
            secured);
      }
    }
  }

  private void removeStoredPassword(HttpServletResponse response, boolean secured) {
    writeCookie(response, "var2", "", 0, secured);
    writeCookie(response, "svpPassword", "", 0, secured);
  }

  private void storeLogin(HttpServletResponse response, boolean newEncryptMode, String sLogin,
      boolean secured) {
    if (newEncryptMode) {
      writeCookie(response, "var1", credentialEncryption.encode(sLogin),
          -1, secured);
      writeCookie(response, "var1", credentialEncryption.encode(sLogin),
          31536000, secured);
    } else {
      writeCookie(response, "svpLogin", sLogin, -1, secured);
      writeCookie(response, "svpLogin", sLogin, 31536000, secured);
    }
  }

  private void storeDomain(HttpServletResponse response, String sDomainId, boolean secured) {
    writeCookie(response, "defaultDomain", sDomainId, -1, secured);
    writeCookie(response, "defaultDomain", sDomainId, 31536000, secured);
  }

  private String getDomain(HttpServletRequest request, AuthenticationParameters authParameters,
      SettingBundle authSettings) {
    if (authParameters.isUserByInternalAuthTokenMode()) {
      return authParameters.getDomainId();
    } else if (authParameters.isSsoMode()) {
      return authSettings.getString("sso.authentication.domainId", "0");
    } else if (authParameters.isCasMode()) {
      return authSettings.getString("cas.authentication.domainId", "0");
    }
    OrganizationController controller = OrganizationControllerProvider.getOrganisationController();
    return controller.getDomain(request.getParameter("DomainId")).getId();
  }

  private String authenticate(HttpServletRequest request,
      AuthenticationParameters authenticationParameters, String sDomainId) {
    String key = request.getParameter("TestKey");
    if (!StringUtil.isDefined(key)) {
      AuthenticationCredential credential = AuthenticationCredential.newWithAsLogin(
          authenticationParameters.getLogin());
      if (authenticationParameters.isUserByInternalAuthTokenMode() || authenticationParameters.
          isSsoMode() || authenticationParameters.isCasMode()) {
        key = authService.authenticate(credential.withAsDomainId(sDomainId));
      } else if (authenticationParameters.isSocialNetworkMode()) {
        key = authService.authenticate(credential.withAsDomainId(authenticationParameters.
            getDomainId()));
      } else {
        key = authService.authenticate(credential
            .withAsPassword(authenticationParameters.getClearPassword())
            .withAsDomainId(sDomainId));
      }
      HttpSession session = request.getSession(false);
      for (Map.Entry<String, Object> capability : credential.getCapabilities().entrySet()) {
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

  /**
   * Write session cookie.
   *
   * @return
   */
  private void writeSessionCookie(HttpServletResponse response, HttpSession session, boolean secured) {
    Cookie cookie = new Cookie("JSESSIONID", session.getId());
    cookie.setMaxAge(-1);
    cookie.setPath(session.getServletContext().getContextPath());
    cookie.setHttpOnly(true);
    if (secured) {
      cookie.setSecure(secured);
    }
    response.addCookie(cookie);
  }

  /**
   * Write connections cookie.
   *
   * @param name
   * @param value
   * @param duration
   * @return
   */
  private void writeCookie(HttpServletResponse response, String name, String value, int duration,
      boolean secure) {
    String cookieValue;
    try {
      cookieValue = URLEncoder.encode(value, CharEncoding.UTF_8);
    } catch (UnsupportedEncodingException ex) {
      cookieValue = value;
    }
    Cookie cookie = new Cookie(name, cookieValue);
    cookie.setMaxAge(duration);
    cookie.setPath("/");
    if (secure) {
      cookie.setSecure(true);
    }
    response.addCookie(cookie);
  }
}

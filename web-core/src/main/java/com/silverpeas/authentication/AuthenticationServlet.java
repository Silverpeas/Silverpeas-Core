/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.authentication;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.authentication.Authentication;
import org.silverpeas.authentication.AuthenticationCredential;
import org.silverpeas.authentication.AuthenticationService;
import org.silverpeas.authentication.exception.AuthenticationNoMoreUserConnectionAttemptException;
import org.silverpeas.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.authentication.verifier.UserCanLoginVerifier;
import org.silverpeas.authentication.verifier.UserCanTryAgainToLoginVerifier;
import org.silverpeas.authentication.verifier.UserMustChangePasswordVerifier;

/**
 * This servlet listens for incoming authentication requests for Silverpeas.
 *
 * This servlet delegates the authentication process and the HTTP session opening in Silverpeas to
 * the corresponding services. If the authentication and the session opening succeed, the user
 * behind the authentication ask is redirected to its user home page. Otherwise, he's redirected to
 * an authentication failure page (that can the login page enriched with an error message).
 */
public class AuthenticationServlet extends HttpServlet {

  private static AuthenticationService authService = new AuthenticationService();
  private static final long serialVersionUID = -8695946617361150513L;
  private static final SilverpeasSessionOpener silverpeasSessionOpener =
      new SilverpeasSessionOpener();
  private static final String TECHNICAL_ISSUE = "2";
  private static final String INCORRECT_LOGIN_PWD = "1";

  /**
   * Ask for an authentication for the user behind the incoming HTTP request from a form.
   *
   * @param request the HTTP request.
   * @param response the HTTP response.
   * @throws IOException when an error occurs while processing the request or sending the response.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    if (!StringUtil.isDefined(request.getCharacterEncoding())) {
      request.setCharacterEncoding(CharEncoding.UTF_8);
    }
    if (silverpeasSessionOpener.isAnonymousUser(request)) {
      silverpeasSessionOpener.closeSession(request);
    }

    // Get the authentication settings
    ResourceLocator authenticationSettings = new ResourceLocator(
        "org.silverpeas.authentication.settings.authenticationSettings", "");
    HttpSession session = request.getSession();
    boolean securedAccess = silverpeasSessionOpener.isNavigationSecure(request);
    boolean isNewEncryptMode = StringUtil.isDefined(request.getParameter("Var2"));
    AuthenticationParameters authenticationParameters = new AuthenticationParameters(request);
    session.setAttribute("Silverpeas_pwdForHyperlink", authenticationParameters.getClearPassword());
    String domainId = getDomain(request, authenticationSettings,
        authenticationParameters.isCasMode());
    AuthenticationCredential credential =
        AuthenticationCredential.newWithAsLogin(authenticationParameters.getLogin())
        .withAsPassword(authenticationParameters.getPassword())
        .withAsDomainId(domainId);

    String authenticationKey = authenticate(request, authenticationParameters, domainId);
    String url;

    // Verify if the user can try again to login.
    UserCanTryAgainToLoginVerifier userCanTryAgainToLoginVerifier =
        AuthenticationUserVerifierFactory.getUserCanTryAgainToLoginVerifier(credential);
    userCanTryAgainToLoginVerifier.clearSession(request);

    if (authenticationKey != null && !authenticationKey.startsWith("Error")) {

      // Clearing user connection attempt cache.
      userCanTryAgainToLoginVerifier.clearCache();

      if (domainId != null) {
        storeDomain(response, domainId, securedAccess);
      }
      storeLogin(response, isNewEncryptMode, authenticationParameters.getLogin(), securedAccess);

      // if required by user, store password in cookie
      storePassword(response, authenticationParameters.getStoredPassword(), isNewEncryptMode,
          authenticationParameters.getClearPassword(), securedAccess);

      MandatoryQuestionChecker checker = new MandatoryQuestionChecker();
      if (checker.check(request, authenticationKey)) {
        RequestDispatcher dispatcher = request.getRequestDispatcher(checker.getDestination());
        dispatcher.forward(request, response);
        return;
      }
      String absoluteUrl = silverpeasSessionOpener.openSession(request, authenticationKey);
      writeSessionCookie(response, session, securedAccess);
      response.sendRedirect(response.encodeRedirectURL(absoluteUrl));
      return;
    }
    // Authentication failed : remove password from cookies to avoid infinite loop
    removeStoredPassword(response, securedAccess);
    if (authenticationParameters.isCasMode()) {
      url = "/admin/jsp/casAuthenticationError.jsp";
    } else {
      if ("Error_1".equals(authenticationKey)) {
        try {
          if (userCanTryAgainToLoginVerifier.isActivated()) {
            storeLogin(response, isNewEncryptMode, authenticationParameters.getLogin(),
                securedAccess);
            storeDomain(response, domainId, securedAccess);
          }
          url = userCanTryAgainToLoginVerifier.verify()
              .performRequestUrl(request, "/Login.jsp?ErrorCode=" + INCORRECT_LOGIN_PWD);
        } catch (AuthenticationNoMoreUserConnectionAttemptException e) {
          url = userCanTryAgainToLoginVerifier.getErrorDestination();
        }
      } else if (AuthenticationService.ERROR_PWD_EXPIRED.equals(authenticationKey)) {
        String allowPasswordChange = (String) session.getAttribute(
            Authentication.PASSWORD_CHANGE_ALLOWED);
        if (StringUtil.getBooleanValue(allowPasswordChange)) {
          ResourceLocator settings = new ResourceLocator(
              "com.silverpeas.authentication.settings.passwordExpiration", "");
          url = settings.getString("passwordExpiredURL") + "?login=" + authenticationParameters.
              getLogin() + "&domainId=" + domainId;
        } else {
          url = "/Login.jsp?ErrorCode=" + AuthenticationService.ERROR_PWD_EXPIRED;
        }
      } else if (AuthenticationService.ERROR_PWD_MUST_BE_CHANGED.equals(authenticationKey)) {
        String allowPasswordChange = (String) session.getAttribute(
            Authentication.PASSWORD_CHANGE_ALLOWED);
        if (StringUtil.getBooleanValue(allowPasswordChange)) {
          ResourceLocator settings = new ResourceLocator(
              "com.silverpeas.authentication.settings.passwordExpiration", "");
          url = settings.getString("passwordExpiredURL") + "?login=" + authenticationParameters.
              getLogin() + "&domainId=" + domainId;
        } else {
          url = "/Login.jsp?ErrorCode=" + AuthenticationService.ERROR_PWD_EXPIRED;
        }
      } else if (UserMustChangePasswordVerifier.ERROR_PWD_MUST_BE_CHANGED_ON_FIRST_LOGIN
          .equals(authenticationKey)) {
        // User has been successfully authenticated, but he has to change his password on his
        // first login and login / domain id can be stored
        storeLogin(response, isNewEncryptMode, authenticationParameters.getLogin(), securedAccess);
        storeDomain(response, domainId, securedAccess);
        url = AuthenticationUserVerifierFactory.getUserMustChangePasswordVerifier(credential)
            .getDestinationOnFirstLogin(request);
        RequestDispatcher dispatcher = request.getRequestDispatcher(url);
        dispatcher.forward(request, response);
        return;
      } else if (UserCanLoginVerifier.ERROR_USER_ACCOUNT_BLOCKED.equals(authenticationKey)) {
        if (userCanTryAgainToLoginVerifier.isActivated() || StringUtil.isDefined(
            userCanTryAgainToLoginVerifier.getUser().getId())) {
          // If user can try again to login verifier is activated or if the user has been found
          // from credential, the login and the domain are stored
          storeLogin(response, isNewEncryptMode, authenticationParameters.getLogin(), securedAccess);
          storeDomain(response, domainId, securedAccess);
          url = AuthenticationUserVerifierFactory.getUserCanLoginVerifier((UserDetail) null)
              .getErrorDestination();
        } else {
          url = "/Login.jsp?ErrorCode=" + INCORRECT_LOGIN_PWD;
        }
      } else {
        url = "/Login.jsp?ErrorCode=" + TECHNICAL_ISSUE;
      }
    }
    response.sendRedirect(response.encodeRedirectURL(URLManager.getFullApplicationURL(request)
        + url));
  }

  private void storePassword(HttpServletResponse response, String shoudStorePasword,
      boolean newEncryptMode, String clearPassword, boolean secured) {
    if (StringUtil.getBooleanValue(shoudStorePasword)) {
      SilverTrace.debug("authentication", "AuthenticationServlet.doPost()",
          "root.MSG_GEN_ENTER_METHOD", "Ok");
      if (newEncryptMode) {
        writeCookie(response, "var2", CredentialEncryptionFactory.getInstance().getEncryption().
            encode(
            clearPassword), -1, secured);
        writeCookie(response, "var2", CredentialEncryptionFactory.getInstance().getEncryption().
            encode(
            clearPassword), 31536000, secured);
      } else {
        writeCookie(response, "svpPassword", CredentialEncryptionFactory.getInstance().
            getEncryption()
            .encode(
            clearPassword), -1, secured);
        writeCookie(response, "svpPassword", CredentialEncryptionFactory.getInstance().
            getEncryption()
            .encode(
            clearPassword), 31536000, secured);
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
      writeCookie(response, "var1", CredentialEncryptionFactory.getInstance().getEncryption().
          encode(sLogin),
          -1, secured);
      writeCookie(response, "var1", CredentialEncryptionFactory.getInstance().getEncryption().
          encode(sLogin),
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

  private String getDomain(HttpServletRequest request, ResourceLocator authSettings, boolean casMode) {
    String sDomainId = request.getParameter("DomainId");
    if (casMode) {
      sDomainId = authSettings.getString("cas.authentication.domainId", "0");
    }
    return sDomainId;
  }

  private String authenticate(HttpServletRequest request,
      AuthenticationParameters authenticationParameters, String sDomainId) {
    String key = request.getParameter("TestKey");
    if (!StringUtil.isDefined(key)) {
      AuthenticationCredential credential =
          AuthenticationCredential.newWithAsLogin(authenticationParameters.getLogin());
      if (authenticationParameters.isCasMode()) {
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
    cookie.setHttpOnly(true);
    if (secure) {
      cookie.setSecure(true);
    }
    response.addCookie(cookie);
  }
}

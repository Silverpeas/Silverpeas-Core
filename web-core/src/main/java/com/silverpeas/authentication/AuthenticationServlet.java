/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.authentication;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.authentication.Authentication;
import com.stratelia.silverpeas.authentication.EncryptionFactory;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The class AuthenticationServlet is called to authenticate user in Silverpeas.
 */
public class AuthenticationServlet extends HttpServlet {

  private static LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();
  private static final long serialVersionUID = -8695946617361150513L;
  private static final AuthenticationService AUTHENTICATION_SERVICE = new AuthenticationService();
  private static final String TECHNICAL_ISSUE = "2";
  private static final String INCORRECT_LOGIN_PWD = "1";

  /**
   * Method invoked when called from a form or directly by URL
   * @param request the HTTP request.
   * @param response the HTTP response.
   * @throws IOException when an error occurs while recieving the request or sending the response.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    // Get the session
    HttpSession session = request.getSession();
    if (!StringUtil.isDefined(request.getCharacterEncoding())) {
      request.setCharacterEncoding("UTF-8");
    }
    if (AUTHENTICATION_SERVICE.isAnonymousUser(request)) {
      AUTHENTICATION_SERVICE.unauthenticate(request);
    }

    // Get the authentication settings
    ResourceLocator authenticationSettings = new ResourceLocator(
        "com.silverpeas.authentication.settings.authenticationSettings", "");
    boolean isNewEncryptMode = StringUtil.isDefined(request.getParameter("Var2"));
    IdentificationParameters identificationParameters = new IdentificationParameters(session,
        request);
    session.setAttribute("Silverpeas_pwdForHyperlink", identificationParameters.getClearPassword());
    String sDomainId = getDomain(request, authenticationSettings,
        identificationParameters.isCasMode());

    String authenticationKey = identify(request, identificationParameters, sDomainId);
    String url;
    if (authenticationKey != null && !authenticationKey.startsWith("Error")) {
      if (sDomainId != null) {
        storeDomain(response, sDomainId);
      }
      storeLogin(response, isNewEncryptMode, identificationParameters.getLogin());

      // if required by user, store password in cookie
      storePassword(response, identificationParameters.getStoredPassword(), isNewEncryptMode,
          identificationParameters.getClearPassword());

      MandatoryQuestionChecker checker = new MandatoryQuestionChecker();
      if (checker.check(request, authenticationKey)) {
        RequestDispatcher dispatcher = request.getRequestDispatcher(checker.getDestination());
        dispatcher.forward(request, response);
        return;
      }
      String absoluteUrl = AUTHENTICATION_SERVICE.authenticate(request, authenticationKey);

      final Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
      sessionCookie.setMaxAge(-1);
      sessionCookie.setSecure(false);
      sessionCookie.setPath(request.getContextPath());
      response.addCookie(sessionCookie);
      response.sendRedirect(response.encodeRedirectURL(absoluteUrl));
      return;
    }
    // Authentication failed : remove password from cookies to avoid infinite loop
    removeStoredPassword(response);
    if (identificationParameters.isCasMode()) {
      url = "/admin/jsp/casAuthenticationError.jsp";
    } else {
      if("Error_1".equals(authenticationKey)) {
        url = "/Login.jsp?ErrorCode=" + INCORRECT_LOGIN_PWD;
      }
      else if(LoginPasswordAuthentication.ERROR_PWD_EXPIRED.equals(authenticationKey)){
          String allowPasswordChange = (String) session.getAttribute(Authentication.PASSWORD_CHANGE_ALLOWED);
          if(StringUtil.getBooleanValue(allowPasswordChange)){
            ResourceLocator settings = new ResourceLocator("com.silverpeas.authentication.settings.passwordExpiration", "");
            url = settings.getString("passwordExpiredURL")+"?login="+identificationParameters.getLogin()+"&domainId="+sDomainId;
          } else {
            url = "/Login.jsp?ErrorCode=" + LoginPasswordAuthentication.ERROR_PWD_EXPIRED;
          }
      }
      else {
        url = "/Login.jsp?ErrorCode=" + TECHNICAL_ISSUE;
      }
    }
    response.sendRedirect(response.encodeRedirectURL(URLManager.getFullApplicationURL(request) +
        url));
  }

  private void storePassword(HttpServletResponse response, String sStorePassword,
      boolean newEncryptMode, String sDecodedPassword) {
    if (StringUtil.getBooleanValue(sStorePassword)) {
      SilverTrace.debug("authentication", "AuthenticationServlet.doPost()",
          "root.MSG_GEN_ENTER_METHOD", "Ok");
      if (newEncryptMode) {
        writeCookie(response, "var2", EncryptionFactory.getInstance().getEncryption().encode(
            sDecodedPassword), -1);
        writeCookie(response, "var2", EncryptionFactory.getInstance().getEncryption().encode(
            sDecodedPassword), 31536000);
      } else {
        writeCookie(response, "svpPassword", EncryptionFactory.getInstance().getEncryption()
            .encode(
            sDecodedPassword), -1);
        writeCookie(response, "svpPassword", EncryptionFactory.getInstance().getEncryption()
            .encode(
            sDecodedPassword), 31536000);
      }
    }
  }

  private void removeStoredPassword(HttpServletResponse response) {
    writeCookie(response, "var2", "", 0);
    writeCookie(response, "svpPassword", "", 0);
  }

  private void storeLogin(HttpServletResponse response, boolean newEncryptMode, String sLogin) {
    if (newEncryptMode) {
      writeCookie(response, "var1", EncryptionFactory.getInstance().getEncryption().encode(sLogin),
          -1);
      writeCookie(response, "var1", EncryptionFactory.getInstance().getEncryption().encode(sLogin),
          31536000);
    } else {
      writeCookie(response, "svpLogin", sLogin, -1);
      writeCookie(response, "svpLogin", sLogin, 31536000);
    }
  }

  private void storeDomain(HttpServletResponse response, String sDomainId) {
    writeCookie(response, "defaultDomain", sDomainId, -1);
    writeCookie(response, "defaultDomain", sDomainId, 31536000);
  }

  private String getDomain(HttpServletRequest request, ResourceLocator authenticationSettings,
      boolean casMode) {
    String sDomainId = request.getParameter("DomainId");
    if (casMode) {
      sDomainId = authenticationSettings.getString("cas.authentication.domainId", "0");
    }
    return sDomainId;
  }

  private String identify(HttpServletRequest request,
      IdentificationParameters identificationParameters, String sDomainId) {
    String testKey = request.getParameter("TestKey");
    if (!StringUtil.isDefined(testKey)) {
      if (identificationParameters.isCasMode()) {
        return lpAuth.authenticate(identificationParameters.getLogin(), sDomainId, request);
      } else if (identificationParameters.isSocialNetworkMode()) {
        return lpAuth.authenticate(identificationParameters.getLogin(), identificationParameters
            .getDomainId(), request);
      }
      return lpAuth.authenticate(identificationParameters.getLogin(), identificationParameters.
          getClearPassword(),
          sDomainId, request);
    }
    return null;
  }

  /**
   * Method invoked when called from a form or directly by URL.
   * @param request the HTTP request.
   * @param response the HTTP response.
   * @throws ServletException if the servlet fails to answer the request.
   * @throws IOException if an IO error occurs with the client.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  /**
   * Write connections cookie.
   * @param name
   * @param value
   * @param duration
   * @return
   */
  private void writeCookie(HttpServletResponse response, String name, String value, int duration) {
    Cookie cookie;
    try {
      cookie = new Cookie(name, URLEncoder.encode(value, "UTF-8"));
    } catch (UnsupportedEncodingException ex) {
      cookie = new Cookie(name, value);
    }
    cookie.setMaxAge(duration); // Duration in s
    cookie.setPath("/");
    response.addCookie(cookie);
  }
}

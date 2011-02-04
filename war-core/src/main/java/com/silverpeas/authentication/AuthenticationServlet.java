/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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
import com.stratelia.silverpeas.authentication.EncryptionFactory;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;

/**
 * The class AuthenticationServlet is called to authenticate user in Silverpeas
 */
public class AuthenticationServlet extends HttpServlet {

  private static LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();
  private static final long serialVersionUID = -8695946617361150513L;
  private final int keyMaxLength = 12;

  /**
   * Method invoked when called from a form or directly by URL
   * @param request the HTTP request.
   * @param response the HTTP response.
   * @throws IOException when an error occurs while recieving the request or sending the response.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Get the session
    HttpSession session = request.getSession(true);
    if (!StringUtil.isDefined(request.getCharacterEncoding())) {
      request.setCharacterEncoding("UTF-8");
    }

    // Get the authentication settings
    ResourceLocator authenticationSettings = new ResourceLocator(
        "com.silverpeas.authentication.settings.authenticationSettings", "");
    boolean cookieEnabled = SilverpeasSettings.readBoolean(authenticationSettings, "cookieEnabled",
        false);

    // Get the context
    String sURI = request.getRequestURI();
    String sServletPath = request.getServletPath();
    String sPathInfo = request.getPathInfo();
    String sRequestURL = request.getRequestURL().toString();
    String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - sURI.length());

    if (sPathInfo != null) {
      sURI = sURI.substring(0, sURI.lastIndexOf(sPathInfo));
    }
    String m_sContext = sURI.substring(0, sURI.lastIndexOf(sServletPath));
    if (StringUtil.isDefined(m_sContext) && m_sContext.endsWith("/")) {
      m_sContext = m_sContext.substring(0, m_sContext.length() - 1);
    }
    // CAS authentication
    String casUser = null;
    if (session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) != null) {
      casUser = ((Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)).
          getPrincipal().getName();
    }
    boolean casMode = (casUser != null);

    String stringKey = convert2Alpha(session.getId());
    // New field names in login form
    boolean isNewEncryptMode = StringUtil.isDefined(request.getParameter("Var2"));

    String sLogin = null;
    String sPassword = null;
    String sStorePassword = null;
    String sCryptedPassword = null;
    if (casMode) {
      sLogin = casUser;
      sPassword = "";
    } else if (isNewEncryptMode) {
      sLogin = request.getParameter("Var2");
      sPassword = request.getParameter("Var1");
      sStorePassword = request.getParameter("tq");
      sCryptedPassword = request.getParameter("dq");
    } else {
      // Get the parameters from the login page
      sLogin = request.getParameter("Login");
      sPassword = request.getParameter("Password");
      sStorePassword = request.getParameter("storePassword");
      sCryptedPassword = request.getParameter("cryptedPassword");
    }

    SilverTrace.debug("authentication", "AuthenticationServlet.doPost()",
        "root.MSG_GEN_PARAM_VALUE", "sCryptedPassword = " + sCryptedPassword);

    // Cookies
    String cookieLoginName;
    String cookiePwdName;
    String sDecodedPassword;
    if (isNewEncryptMode) {
      // Specific Sogreah
      cookieLoginName = "var1";
      cookiePwdName = "var2";
      String decodedLogin = decode(sLogin, stringKey, false);
      sDecodedPassword = ((!StringUtil.isDefined(sCryptedPassword)) ? decode(sPassword, stringKey,
          false) : decode(sPassword, stringKey, true));
      if (cookieEnabled) {
        if (StringUtil.isDefined(sCryptedPassword)) {
          decodedLogin = decode(sLogin, stringKey, true);
        }
      }
      sLogin = decodedLogin;
    } else {
      cookieLoginName = "svpLogin";
      cookiePwdName = "svpPassword";
      sDecodedPassword = ((!StringUtil.isDefined(sCryptedPassword)) ? sPassword : decode(
          sPassword));
    }
    session.setAttribute("Silverpeas_pwdForHyperlink", sDecodedPassword);
    String sDomainId = request.getParameter("DomainId");
    if (casMode) {
      sDomainId = authenticationSettings.getString("cas.authentication.domainId", "0");
    }

    String testKey = request.getParameter("TestKey");
    String authentificationKey = null;
    if (!StringUtil.isDefined(testKey)) {
      if (casMode) {
        authentificationKey = lpAuth.authenticate(sLogin, sDomainId, request);
      } else {
        authentificationKey = lpAuth.authenticate(sLogin, sDecodedPassword,
            sDomainId, request); // Throws Specific Exception
      }
    }
    String url;
    if (authentificationKey != null && !authentificationKey.startsWith("Error")) {
      writeCookie(response, "defaultDomain", sDomainId, -1);
      writeCookie(response, "defaultDomain", sDomainId, 31536000);
      if (isNewEncryptMode) {
        // encrypter le login que si on est dans le cas de Sogreah
        writeCookie(response, cookieLoginName, encode(sLogin), -1);
        writeCookie(response, cookieLoginName, encode(sLogin), 31536000);
      } else {
        writeCookie(response, cookieLoginName, sLogin, -1);
        writeCookie(response, cookieLoginName, sLogin, 31536000);
      }

      // if required by user, store password in cookie
      if (sStorePassword != null && "Yes".equals(sStorePassword)) {
        SilverTrace.debug("authentication", "AuthenticationServlet.doPost()",
            "root.MSG_GEN_ENTER_METHOD", "Ok");
        writeCookie(response, cookiePwdName, encode(sDecodedPassword), -1);
        writeCookie(response, cookiePwdName, encode(sDecodedPassword), 31536000);
      }

      url = "/LoginServlet?Key=" + authentificationKey;
    } else {
      // Authentication failed : remove password from cookies to avoid infinite
      // loop
      writeCookie(response, cookiePwdName, "", 0);
      if (casMode) {
        url = "/admin/jsp/casAuthenticationError.jsp";
      } else {
        // mettre a jour le code erreur : "2" = probleme technique
        // "1" = login ou mot de passe incorrect
        String errorCode = ("Error_1".equals(authentificationKey) ? "1" : "2");

        // No user defined for this login-password combination
        url = "/Login.jsp?ErrorCode=" + errorCode;
      }
    }
    response.sendRedirect(response.encodeRedirectURL(m_sAbsolute + m_sContext
        + url));
  }

  /**
   * Method invoked when called from a form or directly by URL
   * @param request the HTTP request.
   * @param response the HTTP response.
   * @throws ServletException if the servlet fails to answer the request.
   * @throws IOException  if an IO error occurs with the client.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  private String encode(String str) {
    return new EncryptionFactory().getEncryption().encode(str);
  }

  private String decode(String str) {
    return new EncryptionFactory().getEncryption().decode(str);
  }

  private String decode(String str, String key, boolean extraCrypt) {
    return new EncryptionFactory().getEncryption().decode(str, key, extraCrypt);
  }

  /**
   * Convert a string to a string with only letters in upper case
   * @param toConvert
   * @return the String in UpperCase
   */
  private String convert2Alpha(String toConvert) {
    String alphaString = "";
    for (int i = 0; i < toConvert.length()
        && alphaString.length() < keyMaxLength; i++) {
      int asciiCode = toConvert.toUpperCase().charAt(i);
      if (asciiCode >= 65 && asciiCode <= 90) {
        alphaString += toConvert.substring(i, i + 1);
      }
    }
    // We fill the key to keyMaxLength char. if not enough letters in sessionId.
    if (alphaString.length() < keyMaxLength) {
      alphaString += "ZFGHZSZHHJNT".substring(0, keyMaxLength
          - alphaString.length());
    }
    return alphaString;
  }

  /**
   * Write connections cookie
   * @param name
   * @param value
   * @param duration
   * @return
   */
  private void writeCookie(HttpServletResponse response, String name,
      String value, int duration) {
    Cookie cookie = new Cookie(name, value);
    cookie.setMaxAge(duration); // Duration in s
    cookie.setPath("/");
    response.addCookie(cookie);
  }
}

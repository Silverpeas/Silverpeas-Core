/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * The class AuthenticationServlet is called to authenticate user in Silverpeas
 */
public class AuthenticationServlet extends HttpServlet {

  private static LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();
  private static final long serialVersionUID = -8695946617361150513L;

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
    boolean isNewEncryptMode = StringUtil.isDefined(request.getParameter("Var2"));
    Identification identification = new Identification(session, request);
    session.setAttribute("Silverpeas_pwdForHyperlink", identification.getClearPassword());
    String sDomainId = getDomain(request, authenticationSettings, identification.isCasMode());

    String authentificationKey = authenticate(request, identification, sDomainId);
    String url;
    if (authentificationKey != null && !authentificationKey.startsWith("Error")) {
      storeDomain(response, sDomainId);
      storeLogin(response, isNewEncryptMode, identification.getLogin());

      // if required by user, store password in cookie
      storePassword(response, identification.getStoredPassword(), isNewEncryptMode, identification.
              getClearPassword());

      url = "/LoginServlet?Key=" + authentificationKey;
    } else {
      // Authentication failed : remove password from cookies to avoid infinite loop
      removeStoredPassword(response);
      if (identification.isCasMode()) {
        url = "/admin/jsp/casAuthenticationError.jsp";
      } else {
        // mettre a jour le code erreur : "2" = probleme technique
        // "1" = login ou mot de passe incorrect
        String errorCode = ("Error_1".equals(authentificationKey) ? "1" : "2");
        url = "/Login.jsp?ErrorCode=" + errorCode;
      }
    }
    response.sendRedirect(response.encodeRedirectURL(URLManager.getFullApplicationURL(request)
            + url));
  }

  private void storePassword(HttpServletResponse response, String sStorePassword,
          boolean newEncryptMode, String sDecodedPassword) {
    if (StringUtil.getBooleanValue(sStorePassword)) {
      SilverTrace.debug("authentication", "AuthenticationServlet.doPost()",
              "root.MSG_GEN_ENTER_METHOD", "Ok");
      if (newEncryptMode) {
        writeCookie(response, "var2", encode(sDecodedPassword), -1);
        writeCookie(response, "var2", encode(sDecodedPassword), 31536000);
      } else {
        writeCookie(response, "svpPassword", encode(sDecodedPassword), -1);
        writeCookie(response, "svpPassword", encode(sDecodedPassword), 31536000);
      }
    }
  }

  private void removeStoredPassword(HttpServletResponse response) {
    writeCookie(response, "var2", "", 0);
    writeCookie(response, "svpPassword", "", 0);
  }

  private void storeLogin(HttpServletResponse response, boolean newEncryptMode, String sLogin) {
    if (newEncryptMode) {
      writeCookie(response, "var1", encode(sLogin), -1);
      writeCookie(response, "var1", encode(sLogin), 31536000);
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

  private String authenticate(HttpServletRequest request, Identification identification,
          String sDomainId) {
    String testKey = request.getParameter("TestKey");
    if (!StringUtil.isDefined(testKey)) {
      if (identification.isCasMode()) {
        return lpAuth.authenticate(identification.getLogin(), sDomainId, request);
      }
      return lpAuth.authenticate(identification.getLogin(), identification.getClearPassword(),
              sDomainId, request);
    }
    return null;
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
    return EncryptionFactory.getInstance().getEncryption().encode(str);
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

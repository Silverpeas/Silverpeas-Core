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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.authentication.AuthenticationException;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * The class AuthenticationServlet is called to authenticate user in Silverpeas
 */
public class StressTestAuthenticationServlet extends HttpServlet {

  private static LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();
  private static final long serialVersionUID = -8695946617361150513L;  

  /**
   * Method invoked when called from a form or directly by URL
   * @param request the HTTP request.
   * @param response the HTTP response.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Get the session
    if (!StringUtil.isDefined(request.getCharacterEncoding())) {
      request.setCharacterEncoding("UTF-8");
    }

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

    // Get the parameters from the login page
    String sLogin = request.getParameter("Login");

    // Cookies
    String sDomainId = request.getParameter("DomainId");

    String testKey = request.getParameter("TestKey");
    String authentificationKey = null;

    try {
      lpAuth.storeAuthenticationKey(sLogin, sDomainId, testKey);
    } catch (AuthenticationException e) {
      SilverTrace.error("authentication", "AuthenticationServlet.doPost()",
            "root.CANT_STORE_AUTHENTICATION_TESTKEY", "sLogin = " + sLogin + ", sDomainId = " +
                sDomainId
                + ", testKey = " + testKey, e);
    }
    authentificationKey = testKey;
    String url;
    if (authentificationKey != null && !authentificationKey.startsWith("Error")) {
      writeCookie(response, "defaultDomain", sDomainId, -1);
      writeCookie(response, "defaultDomain", sDomainId, 31536000);

      url = "/LoginServlet?Key=" + authentificationKey;
    } else {

      // mettre a jour le code erreur : "2" = probleme technique
      // "1" = login ou mot de passe incorrect
      String errorCode = ("Error_1".equals(authentificationKey) ? "1" : "2");

      // No user defined for this login-password combination
      url = "/Login.jsp?ErrorCode=" + errorCode;

    }
    response.sendRedirect(response.encodeRedirectURL(m_sAbsolute + m_sContext
        + url));
  }

  /**
   * Method invoked when called from a form or directly by URL
   * @param request the HTTP request.
   * @param response the HTTP response.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
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

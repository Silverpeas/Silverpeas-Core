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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.silverpeas.servlets;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jcifs.Config;
import jcifs.UniAddress;
import jcifs.http.NtlmSsp;
import jcifs.netbios.NbtAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;
import jcifs.util.Base64;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * The class AuthenticationServlet is called to authenticate user in Silverpeas
 */
public class AuthenticationNTLMServlet extends HttpServlet {
  private String defaultDomain;

  private String domainController;

  private boolean loadBalance;

  private boolean enableBasic;

  private boolean insecureBasic;

  private String realm;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    /*
     * Set jcifs properties we know we want; soTimeout and cachePolicy to 10min.
     */
    Config.setProperty("jcifs.smb.client.soTimeout", "300000");
    Config.setProperty("jcifs.netbios.cachePolicy", "600");

    ResourceLocator authenticationSettings = new ResourceLocator(
        "com.silverpeas.authentication.settings.authenticationSettings", "");

    Enumeration e = authenticationSettings.getKeys();
    String name;
    while (e.hasMoreElements()) {
      name = (String) e.nextElement();
      if (name.startsWith("jcifs.")) {
        Config.setProperty(name, authenticationSettings.getString(name));
      }
    }
    defaultDomain = Config.getProperty("jcifs.smb.client.domain");
    domainController = Config.getProperty("jcifs.http.domainController");
    if (domainController == null) {
      domainController = defaultDomain;
      loadBalance = Config.getBoolean("jcifs.http.loadBalance", true);
    }
    enableBasic = Boolean.valueOf(Config.getProperty("jcifs.http.enableBasic"))
        .booleanValue();
    insecureBasic = Boolean.valueOf(
        Config.getProperty("jcifs.http.insecureBasic")).booleanValue();
    realm = Config.getProperty("jcifs.http.basicRealm");
    if (realm == null)
      realm = "jCIFS";
  }

  protected void service(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    UniAddress dc;
    boolean offerBasic = enableBasic && (insecureBasic || request.isSecure());
    String msg = request.getHeader("Authorization");
    if (msg != null
        && (msg.startsWith("NTLM ") || (offerBasic && msg.startsWith("Basic ")))) {
      if (loadBalance) {
        dc = new UniAddress(NbtAddress.getByName(domainController, 0x1C, null));
      } else {
        dc = UniAddress.getByName(domainController, true);
      }
      NtlmPasswordAuthentication ntlm;
      if (msg.startsWith("NTLM ")) {
        byte[] challenge = SmbSession.getChallenge(dc);
        ntlm = NtlmSsp.authenticate(request, response, challenge);
        if (ntlm == null)
          return;
      } else {
        String auth = new String(Base64.decode(msg.substring(6)), "US-ASCII");
        int index = auth.indexOf(':');
        String user = (index != -1) ? auth.substring(0, index) : auth;
        String password = (index != -1) ? auth.substring(index + 1) : "";
        index = user.indexOf('\\');
        if (index == -1)
          index = user.indexOf('/');
        String domain = (index != -1) ? user.substring(0, index)
            : defaultDomain;
        user = (index != -1) ? user.substring(index + 1) : user;
        ntlm = new NtlmPasswordAuthentication(domain, user, password);
      }
      try {
        SmbSession.logon(dc, ntlm);
      } catch (SmbAuthException sae) {
        response.setHeader("WWW-Authenticate", "NTLM");
        if (offerBasic) {
          response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm
              + "\"");
        }
        response.setHeader("Connection", "close");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.flushBuffer();
        return;
      }
      HttpSession ssn = request.getSession();
      ssn.setAttribute("NtlmHttpAuth", ntlm);
      ssn.setAttribute("ntlmdomain", ntlm.getDomain());
      ssn.setAttribute("ntlmuser", ntlm.getUsername());
    } else {
      HttpSession ssn = request.getSession(false);
      if (ssn == null || ssn.getAttribute("NtlmHttpAuth") == null) {

        ResourceLocator authenticationSettings = getSettings();
        boolean doNTLM = true;
        if (authenticationSettings.getBoolean("ntlm.useOnlyWithIE", false)) {
          String userAgent = request.getHeader("user-agent");
          if (StringUtil.isDefined(userAgent)
              && userAgent.indexOf("MSIE") == -1)
            doNTLM = false;
        }
        if (doNTLM) {
          response.setHeader("WWW-Authenticate", "NTLM");
          if (offerBasic) {
            response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm
                + "\"");
          }
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.flushBuffer();
          return;
        }
      }
    }
    super.service(request, response);
  }

  private static LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();

  /**
   * Method invoked when called from a form or directly by URL
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Get the session
    HttpSession session = request.getSession(true);

    String redirectURL = null;
    String startURL = getStartURL(request);

    NtlmPasswordAuthentication ntlm = (NtlmPasswordAuthentication) session
        .getAttribute("NtlmHttpAuth");
    if (ntlm == null)
      redirectURL = startURL + "Login.jsp";

    if (!StringUtil.isDefined(redirectURL)) {
      // Get the authentication settings
      ResourceLocator authenticationSettings = getSettings();

      String login = ntlm.getUsername();
      String domain = ntlm.getDomain();
      if (domain != null)
        domain = domain.toLowerCase();

      String defaultDomainId = SilverpeasSettings.readString(
          authenticationSettings, "ntlm.defaultDomainId", "0");
      String domainId = SilverpeasSettings.readString(authenticationSettings,
          "ntlm.domainId." + domain, defaultDomainId);

      SilverTrace.info("authentication", "AuthenticationNTLMServlet.doPost",
          "root.MSG_GEN_PARAM_VALUE", "login = " + login + ", domain = "
              + domain + ", domainId = " + domainId + ", defaultDomainId = "
              + defaultDomainId);

      // Used by hyperlink and to be coherent with AuthenticationServlet
      session.setAttribute("Silverpeas_pwdForHyperlink",
          "UnavailablePasswordWithNTLM");

      // Authenticate current user on his domain
      String authentificationKey = lpAuth
          .authenticate(login, domainId, request);

      if ((authentificationKey == null || authentificationKey
          .startsWith("Error"))
          && !defaultDomainId.equals(domainId)) {
        // Authentication failed on domainId
        // Try to authenticate on defaultDomain (which is different from
        // domainId)
        authentificationKey = lpAuth.authenticate(login, domainId, request);
      }

      if (authentificationKey != null
          && !authentificationKey.startsWith("Error")) {
        redirectURL = startURL + "LoginServlet?Key=" + authentificationKey;
      } else {
        // mettre à jour le code erreur : "2" = problème technique
        // "1" = login ou mot de passe incorrect
        String errorCode = "2";
        if ("Error_1".equals(authentificationKey))
          errorCode = "1";

        // No user define for this login-password combination
        redirectURL = startURL + "Login.jsp?ErrorCode=" + errorCode;
      }
    }

    response.sendRedirect(response.encodeRedirectURL(redirectURL));
  }

  /**
   * Method invoked when called from a form or directly by URL
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  private ResourceLocator getSettings() {
    return new ResourceLocator(
        "com.silverpeas.authentication.settings.authenticationSettings", "");
  }

  private String getStartURL(HttpServletRequest request) {
    // Get the context
    String sURI = request.getRequestURI();
    String sServletPath = request.getServletPath();
    String sPathInfo = request.getPathInfo();
    String sRequestURL = request.getRequestURL().toString();
    String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length()
        - sURI.length());

    if (sPathInfo != null)
      sURI = sURI.substring(0, sURI.lastIndexOf(sPathInfo));

    String m_sContext = sURI.substring(0, sURI.lastIndexOf(sServletPath));
    if (m_sContext.length() > 0
        && m_sContext.charAt(m_sContext.length() - 1) == '/') {
      m_sContext = m_sContext.substring(0, m_sContext.length() - 1);
    }

    return m_sAbsolute + m_sContext + "/";
  }
}
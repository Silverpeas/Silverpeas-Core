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
package com.stratelia.webactiv.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.authentication.Authentication;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class LoginServlet extends HttpServlet {
  private String m_sContext = "";
  private String m_sAbsolute = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    com.stratelia.silverpeas.peasCore.MainSessionController controller = null;

    // Get the session
    HttpSession session = request.getSession(true);

    // Get the context
    String sURI = request.getRequestURI();
    String sServletPath = request.getServletPath();
    String sPathInfo = request.getPathInfo();
    String sRequestURL = request.getRequestURL().toString();

    m_sAbsolute = sRequestURL.substring(0, sRequestURL.length()
        - request.getRequestURI().length());

    SilverTrace.info("peasCore", "LoginServlet.doPost()",
        "root.MSG_GEN_PARAM_VALUE", "sURI=" + sURI);
    SilverTrace.info("peasCore", "LoginServlet.doPost()",
        "root.MSG_GEN_PARAM_VALUE", "sServletPath=" + sServletPath);
    SilverTrace.info("peasCore", "LoginServlet.doPost()",
        "root.MSG_GEN_PARAM_VALUE", "sPathInfo=" + sPathInfo);
    SilverTrace.info("peasCore", "LoginServlet.doPost()",
        "root.MSG_GEN_PARAM_VALUE", "sRequestURL=" + sRequestURL);
    SilverTrace.info("peasCore", "LoginServlet.doPost()",
        "root.MSG_GEN_PARAM_VALUE", "sAbsolute=" + m_sAbsolute);

    if (sPathInfo != null)
      sURI = sURI.substring(0, sURI.lastIndexOf(sPathInfo));

    m_sContext = sURI.substring(0, sURI.lastIndexOf(sServletPath));
    if (m_sContext.charAt(m_sContext.length() - 1) == '/') {
      m_sContext = m_sContext.substring(0, m_sContext.length() - 1);
    }

    // Get the parameters from the login page
    String sKey = (String) request.getParameter("Key");
    if (sKey == null) {
      sKey = (String) session.getAttribute("svplogin_Key");
    }
    String sPassword = (String) session
        .getAttribute("Silverpeas_pwdForHyperlink");

    try {
      // Get the user profile from the admin
      SilverTrace.info("peasCore", "LoginServlet.doPost()",
          "root.MSG_GEN_PARAM_VALUE", "session id=" + session.getId());
      controller = new com.stratelia.silverpeas.peasCore.MainSessionController(
          sKey, session.getId()); // Throws Specific Exception

      // Get and store password change capabilities
      String allowPasswordChange = (String) session
          .getAttribute(Authentication.PASSWORD_CHANGE_ALLOWED);
      controller
          .setAllowPasswordChange(((allowPasswordChange != null) && (allowPasswordChange
          .equals("yes"))) ? true : false);

      // Notify user about password expiration if needed
      Boolean alertUserAboutPwdExpiration = (Boolean) session
          .getAttribute(Authentication.PASSWORD_IS_ABOUT_TO_EXPIRE);
      if ((alertUserAboutPwdExpiration != null)
          && (alertUserAboutPwdExpiration.booleanValue()))
        alertUserAboutPwdExpiration(controller.getUserId(), controller
            .getOrganizationController().getAdministratorUserIds(
            controller.getUserId())[0], controller.getFavoriteLanguage());

    } catch (Exception e) {
      SilverTrace.error("peasCore", "LoginServlet.doPost()",
          "peasCore.EX_LOGIN_SERVLET_CANT_CREATE_MAIN_SESSION_CTRL",
          "session id=" + session.getId(), e);
    }

    if ((controller != null)
        && (!controller.getCurrentUserDetail().isAccessRemoved())) {
      // Init session management and session object !!! This method reset the
      // Session Object
      SessionManager.getInstance().addSession(session, request, controller);

      // Put the main session controller in the session
      session.setAttribute("SilverSessionController", controller);
      // Add pwd for Hyperlink
      session.setAttribute("Silverpeas_pwdForHyperlink", sPassword);

      // Init server name and server port
      String serverName = request.getServerName();
      String serverPort = "";
      int srv_port = request.getServerPort();
      if (srv_port != 80)
        serverPort = String.valueOf(srv_port);
      controller.initServerProps(serverName, serverPort);

      // Put a graphicElementFactory in the session
      GraphicElementFactory gef = new GraphicElementFactory(controller
          .getFavoriteLook());
      session.setAttribute("SessionGraphicElementFactory", gef);

      String favoriteFrame = gef.getLookFrame();
      SilverTrace.debug("peasCore", "doPost", "root.MSG_GEN_PARAM_VALUE",
          "controller.getUserAccessLevel()=" + controller.getUserAccessLevel());
      SilverTrace.debug("peasCore", "doPost", "root.MSG_GEN_PARAM_VALUE",
          "controller.isAppInMaintenance()=" + controller.isAppInMaintenance());

      String sDirectAccessSpace = request.getParameter("DirectAccessSpace");
      String sDirectAccessCompo = request.getParameter("DirectAccessCompo");

      if (controller.isAppInMaintenance()
          && !controller.getCurrentUserDetail().isAccessAdmin()) {
        response.sendRedirect(response.encodeRedirectURL(m_sAbsolute
            + m_sContext + "/admin/jsp/appInMaintenance.jsp"));
      } else if (StringUtil.isDefined(sDirectAccessSpace)
          && StringUtil.isDefined(sDirectAccessCompo)) {
        response.sendRedirect(response.encodeRedirectURL(m_sAbsolute
            + m_sContext
            + URLManager.getURL(sDirectAccessSpace, sDirectAccessCompo)
            + "Main"));
      } else {
        response.sendRedirect(response.encodeRedirectURL(m_sAbsolute
            + m_sContext + "/Main/" + favoriteFrame));
      }
    } else {
      // No user define for this login-password combination
      SilverTrace.error("peasCore", "LoginServlet.doPost()",
          "peasCore.EX_USER_KEY_NOT_FOUND", "key=" + sKey);
      response.sendRedirect(response.encodeRedirectURL(m_sAbsolute + m_sContext
          + "/Login.jsp"));
    }
  }

  private void alertUserAboutPwdExpiration(String userId, String fromUserId,
      String language) {
    try {
      ResourceLocator messages = new ResourceLocator(
          "com.stratelia.silverpeas.peasCore.multilang.peasCoreBundle",
          language);
      NotificationSender sender = new NotificationSender(null);
      NotificationMetaData notifMetaData = new NotificationMetaData(
          NotificationParameters.NORMAL, messages
          .getString("passwordExpirationAlert"), messages
          .getString("passwordExpirationMessage"));
      notifMetaData.setSender(fromUserId);
      notifMetaData.addUserRecipient(userId);
      sender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP,
          notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.warn("peasCore", "LoginServlet.alertUserAboutPwdExpiration",
          "peasCore.EX_CANT_SEND_PASSWORD_EXPIRATION_ALERT", "userId = "
          + userId, e);
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }
}

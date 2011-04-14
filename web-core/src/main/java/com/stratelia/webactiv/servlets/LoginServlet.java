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
package com.stratelia.webactiv.servlets;

import com.stratelia.silverpeas.authentication.Authentication;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import static com.silverpeas.util.StringUtil.*;

public class LoginServlet extends HttpServlet {

  private static final long serialVersionUID = 8524810441906567361L;
  private static final int HTTP_DEFAULT_PORT = 80;

  public LoginServlet() {
    super();
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    MainSessionController controller = null;

    // Get a new session
    HttpSession session = request.getSession(true);
    if (!session.isNew()) {
      closeSession(session);
      session = request.getSession(true);
    }
    StringBuilder absoluteUrl = new StringBuilder(256);
    if (request.isSecure() && !GeneralPropertiesManager.getBoolean("server.ssl", false)) {
      absoluteUrl.append("http");
    } else {
      absoluteUrl.append(request.getScheme());
    }
    absoluteUrl.append("://").append(request.getServerName()).append(':');
    int serverPort =
        GeneralPropertiesManager.getInteger("server.http.port", request.getServerPort());
    absoluteUrl.append(serverPort);
    absoluteUrl.append(URLManager.getApplicationURL());
    // Get the parameters from the login page
    String sKey = request.getParameter("Key");
    if (sKey == null) {
      sKey = (String) session.getAttribute("svplogin_Key");
    }
    String sPassword = (String) session.getAttribute("Silverpeas_pwdForHyperlink");

    try {
      // Get the user profile from the admin
      SilverTrace.info("peasCore", "LoginServlet.service()",
          "root.MSG_GEN_PARAM_VALUE", "session id=" + session.getId());
      controller = new MainSessionController(sKey, session.getId()); // Throws Specific Exception

      // Get and store password change capabilities
      String allowPasswordChange = (String) session.getAttribute(
          Authentication.PASSWORD_CHANGE_ALLOWED);
      controller.setAllowPasswordChange(getBooleanValue(allowPasswordChange));

      // Notify user about password expiration if needed
      Boolean alertUserAboutPwdExpiration = (Boolean) session.getAttribute(
          Authentication.PASSWORD_IS_ABOUT_TO_EXPIRE);
      if ((alertUserAboutPwdExpiration != null)
          && (alertUserAboutPwdExpiration.booleanValue())) {
        alertUserAboutPwdExpiration(controller.getUserId(), controller.getOrganizationController().
            getAdministratorUserIds(controller.getUserId())[0], controller.getFavoriteLanguage());
      }

    } catch (Exception e) {
      SilverTrace.error("peasCore", "LoginServlet.service()",
          "peasCore.EX_LOGIN_SERVLET_CANT_CREATE_MAIN_SESSION_CTRL",
          "session id=" + session.getId(), e);
    }

    if ((controller != null) && (!controller.getCurrentUserDetail().isAccessRemoved())) {
      // Init session management and session object !!! This method reset the
      // Session Object
      if (!UserDetail.isAnonymousUser(controller.getUserId())) {
        SessionManager.getInstance().addSession(session, request, controller);
      }

      // Put the main session controller in the session
      session.setAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT, controller);
      // Add pwd for Hyperlink
      session.setAttribute("Silverpeas_pwdForHyperlink", sPassword);

      // Init server name and server port
      String serverName = request.getServerName();
      String port = "";
      if (serverPort != HTTP_DEFAULT_PORT) {
        port = String.valueOf(serverPort);
      }
      controller.initServerProps(serverName, port);

      // Retrieve personal workspace
      String personalWs = controller.getPersonalization().getPersonalWorkSpaceId();
      SilverTrace.debug("peasCore", "LoginServlet.service", "user personal workspace=" + personalWs);

      // Put a graphicElementFactory in the session
      GraphicElementFactory gef = new GraphicElementFactory(controller.getFavoriteLook());
      if (isDefined(personalWs)) {
        gef.setSpaceId(personalWs);
      }
      gef.setMainSessionController(controller);
      session.setAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT, gef);

      String favoriteFrame = gef.getLookFrame();
      SilverTrace.debug("peasCore", "LoginServlet.service", "root.MSG_GEN_PARAM_VALUE",
          "controller.getUserAccessLevel()=" + controller.getUserAccessLevel());
      SilverTrace.debug("peasCore", "LoginServlet.service", "root.MSG_GEN_PARAM_VALUE",
          "controller.isAppInMaintenance()=" + controller.isAppInMaintenance());

      String sDirectAccessSpace = request.getParameter("DirectAccessSpace");
      String sDirectAccessCompo = request.getParameter("DirectAccessCompo");

      if (controller.isAppInMaintenance() && !controller.getCurrentUserDetail().isAccessAdmin()) {
        absoluteUrl.append("/admin/jsp/appInMaintenance.jsp");
      } else if (isDefined(sDirectAccessSpace)
          && isDefined(sDirectAccessCompo)) {
        absoluteUrl.append(URLManager.getURL(sDirectAccessSpace, sDirectAccessCompo)).append("Main");
      } else {
        absoluteUrl.append("/Main/").append(favoriteFrame);
      }
    } else {
      // No user define for this login-password combination
      SilverTrace.error("peasCore", "LoginServlet.service()", "peasCore.EX_USER_KEY_NOT_FOUND",
          "key=" + sKey);
      absoluteUrl.append("/Login.jsp");
    }
    absoluteUrl.append(";jsessionid=").append(session.getId());
    final Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
    sessionCookie.setMaxAge(-1);
    sessionCookie.setSecure(false);
    sessionCookie.setPath(request.getContextPath());
    response.addCookie(sessionCookie);
    response.sendRedirect(response.encodeRedirectURL(absoluteUrl.toString()));
  }

  private void alertUserAboutPwdExpiration(String userId, String fromUserId,
      String language) {
    try {
      ResourceLocator messages = new ResourceLocator(
          "com.stratelia.silverpeas.peasCore.multilang.peasCoreBundle", language);
      NotificationSender sender = new NotificationSender(null);
      NotificationMetaData notifMetaData = new NotificationMetaData(
          NotificationParameters.NORMAL, messages.getString("passwordExpirationAlert"), messages.
          getString("passwordExpirationMessage"));
      notifMetaData.setSender(fromUserId);
      notifMetaData.addUserRecipient(userId);
      sender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP, notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.warn("peasCore", "LoginServlet.alertUserAboutPwdExpiration",
          "peasCore.EX_CANT_SEND_PASSWORD_EXPIRATION_ALERT", "userId = " + userId, e);
    }
  }

  /**
   * Closes any previous existing session for the current user.
   * A previous session can exist with the anonymous accesses.
   * @param session the previous HTTP session to close.
   */
  private void closeSession(final HttpSession session) {
    MainSessionController controller =
        (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if (controller != null && controller.getCurrentUserDetail().isAnonymous()) {
      session.invalidate();
    } else {
      SessionManager.getInstance().closeSession(session.getId());
    }
  }
}

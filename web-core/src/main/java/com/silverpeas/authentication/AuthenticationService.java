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
import com.stratelia.silverpeas.authentication.Authentication;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * Service used for user authentication : creating all the required ressources for Silverpeas 
 * in the Session.
 * @author ehugonnet
 */
public class AuthenticationService {

  private static final int HTTP_DEFAULT_PORT = 80;

  public AuthenticationService() {
  }
  
  public boolean isAnonymousUser(HttpServletRequest request) {
     HttpSession session = request.getSession();
     MainSessionController controller = (MainSessionController) session.getAttribute(
            MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
     if(controller != null) {
       return controller.getCurrentUserDetail().isAnonymous();
     }
     return false;
  }

  public String authenticate(HttpServletRequest request, String sKey) {
    HttpSession session = request.getSession();
    try {
      // Get the user profile from the admin
      SilverTrace.info("peasCore", "AuthenticationService.authenticate()",
              "root.MSG_GEN_PARAM_VALUE", "session id=" + session.getId());
      MainSessionController controller = new MainSessionController(sKey, session.getId());
      // Get and store password change capabilities
      String allowPasswordChange = (String) session.getAttribute(
              Authentication.PASSWORD_CHANGE_ALLOWED);
      controller.setAllowPasswordChange(StringUtil.getBooleanValue(allowPasswordChange));
      // Notify user about password expiration if needed
      Boolean alertUserAboutPwdExpiration = (Boolean) session.getAttribute(
              Authentication.PASSWORD_IS_ABOUT_TO_EXPIRE);
      String redirectURL = null;
      if (alertUserAboutPwdExpiration != null && alertUserAboutPwdExpiration) {
        redirectURL = alertUserAboutPwdExpiration(controller.getUserId(), controller.
                getOrganizationController().
                getAdministratorUserIds(controller.getUserId())[0], controller.getFavoriteLanguage(),
                StringUtil.getBooleanValue(allowPasswordChange));
      }
      if (!controller.getCurrentUserDetail().isAccessRemoved()) {
        // Init session management and session object !!! This method reset theSession Object
        if (!UserDetail.isAnonymousUser(controller.getUserId())) {
          SessionManager.getInstance().addSession(session, request, controller);
        }
        // Put the main session controller in the session
        session.setAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT, controller);
        return getHomePageUrl(request, redirectURL);
      }

    } catch (Exception e) {
      SilverTrace.error("peasCore", "AuthenticationService.authenticate()",
              "peasCore.EX_LOGIN_SERVLET_CANT_CREATE_MAIN_SESSION_CTRL",
              "session id=" + session.getId(), e);
    }
    return getAuthenticationErrorPageUrl(request, sKey);
  }

  /**
   * No user is defined for this login-password combination.
   * @param request
   * @param sKey
   * @return
   */
  public String getAuthenticationErrorPageUrl(HttpServletRequest request, String sKey) {
    HttpSession session = request.getSession();
    SilverTrace.error("peasCore", "AuthenticationService.authenticate()",
            "peasCore.EX_USER_KEY_NOT_FOUND", "key=" + sKey);
    StringBuilder absoluteUrl = new StringBuilder(getAbsoluteUrl(request));
    if(absoluteUrl.charAt(absoluteUrl.length() -1) != '/') {
      absoluteUrl.append('/');
    }
    absoluteUrl.append("Login.jsp");
    return absoluteUrl.toString();
  }

  public String getHomePageUrl(HttpServletRequest request, String redirectURL) {
    StringBuilder absoluteUrl = new StringBuilder(getAbsoluteUrl(request));
    HttpSession session = request.getSession();
    MainSessionController controller = (MainSessionController) session.getAttribute(
            MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    // Init server name and server port
    String serverName = request.getServerName();
    int serverPort = getServerPort(request);
    String port = "";
    if (serverPort != HTTP_DEFAULT_PORT) {
      port = String.valueOf(serverPort);
    }
    controller.initServerProps(serverName, port);

    // Retrieve personal workspace
    String personalWs = controller.getPersonalization().getPersonalWorkSpaceId();
    SilverTrace.debug("peasCore", "AuthenticationService.authenticate",
            "user personal workspace=" + personalWs);

    // Put a graphicElementFactory in the session
    GraphicElementFactory gef = new GraphicElementFactory(controller.getFavoriteLook());
    if (StringUtil.isDefined(personalWs)) {
      gef.setSpaceId(personalWs);
    }
    gef.setMainSessionController(controller);
    session.setAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT, gef);

    String favoriteFrame = gef.getLookFrame();
    SilverTrace.debug("peasCore", "AuthenticationService.authenticate", "root.MSG_GEN_PARAM_VALUE",
            "controller.getUserAccessLevel()=" + controller.getUserAccessLevel());
    SilverTrace.debug("peasCore", "AuthenticationService.authenticate", "root.MSG_GEN_PARAM_VALUE",
            "controller.isAppInMaintenance()=" + controller.isAppInMaintenance());
    String sDirectAccessSpace = request.getParameter("DirectAccessSpace");
    String sDirectAccessCompo = request.getParameter("DirectAccessCompo");
    if (controller.isAppInMaintenance() && !controller.getCurrentUserDetail().isAccessAdmin()) {
      absoluteUrl.append("/admin/jsp/appInMaintenance.jsp");
    } else if (StringUtil.isDefined(redirectURL)) {
      absoluteUrl.append(redirectURL);
    } else if (StringUtil.isDefined(sDirectAccessSpace) && StringUtil.isDefined(sDirectAccessCompo)) {
      absoluteUrl.append(URLManager.getURL(sDirectAccessSpace, sDirectAccessCompo)).append("Main");
    } else {
      absoluteUrl.append("/Main/").append(favoriteFrame);
    }    
    return absoluteUrl.toString();
  }

  /**
   * Computes the beginning of an absolute URL for the home page.
   * @param request
   * @return
   */
  public String getAbsoluteUrl(HttpServletRequest request) {
    StringBuilder absoluteUrl = new StringBuilder(256);
    if (request.isSecure() && !GeneralPropertiesManager.getBoolean("server.ssl", false)) {
      absoluteUrl.append("http");
    } else {
      absoluteUrl.append(request.getScheme());
    }
    absoluteUrl.append("://").append(request.getServerName()).append(':');
    absoluteUrl.append(getServerPort(request));
    absoluteUrl.append(URLManager.getApplicationURL());
    return absoluteUrl.toString();
  }

  private int getServerPort(HttpServletRequest request) {
    return GeneralPropertiesManager.getInteger("server.http.port", request.getServerPort());
  }

  private String alertUserAboutPwdExpiration(String userId, String fromUserId,
          String language, boolean allowPasswordChange) {
    try {
      ResourceLocator settings =
              new ResourceLocator("com.silverpeas.authentication.settings.passwordExpiration", "");
      String notificationType = settings.getString("notificationType", "POPUP");
      String passwordChangeURL =
              settings.getString("passwordChangeURL", "defaultPasswordAboutToExpire.jsp");

      if ("POPUP".equalsIgnoreCase(notificationType) || !allowPasswordChange) {
        sendPopupNotificationAboutPwdExpiration(userId, fromUserId, language);
        return null;
      }
      return passwordChangeURL;
    } catch (NotificationManagerException e) {
      SilverTrace.warn("peasCore", "AuthenticationService.alertUserAboutPwdExpiration",
              "peasCore.EX_CANT_SEND_PASSWORD_EXPIRATION_ALERT", "userId = " + userId, e);
      return null;
    }
  }

  private void sendPopupNotificationAboutPwdExpiration(String userId, String fromUserId,
          String language) throws NotificationManagerException {
    ResourceLocator messages = new ResourceLocator(
            "com.stratelia.silverpeas.peasCore.multilang.peasCoreBundle", language);
    NotificationSender sender = new NotificationSender(null);
    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL, 
            messages.getString("passwordExpirationAlert"), messages.getString("passwordExpirationMessage"));
    notifMetaData.setSender(fromUserId);
    notifMetaData.addUserRecipient(new UserRecipient(userId));
    sender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP, notifMetaData);
  }
  
  
  public void unauthenticate(HttpServletRequest request) {
    HttpSession session = request.getSession();
    session.removeAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    session.removeAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    Enumeration<String> names = (Enumeration<String>)session.getAttributeNames();
    while(names.hasMoreElements()) {
      String attributeName = names.nextElement();
      if(!attributeName.startsWith("Redirect") && !"gotoNew".equals(attributeName)) {
        session.removeAttribute(attributeName);
      }
    }
    SessionManager.getInstance().removeSession(session);
  }
}

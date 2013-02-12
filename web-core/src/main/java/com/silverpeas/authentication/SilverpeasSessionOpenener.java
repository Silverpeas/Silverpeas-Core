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

import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementFactory;
import com.silverpeas.util.StringUtil;
import org.silverpeas.authentication.Authentication;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.MainSessionController;
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
 * Service used to open an HTTP session in the Silverpeas platform.
 * <p/>
 * It asks for a session opening to the session manager and then  it creates all the required session
 * resources for Silverpeas and stores them into the user session.
 *
 * @author ehugonnet
 */
public class SilverpeasSessionOpenener {

  private static final int HTTP_DEFAULT_PORT = 80;

  public SilverpeasSessionOpenener() {
  }

  /**
   * Is the user behind the specified incoming request an anonymous one?
   *
   * @param request the incoming HTTP request.
   * @return true if the user sending the request is an anonymous one, false otherwise.
   */
  public boolean isAnonymousUser(HttpServletRequest request) {
    HttpSession session = request.getSession();
    MainSessionController controller = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if (controller != null) {
      return controller.getCurrentUserDetail().isAnonymous();
    }
    return false;
  }

  /**
   * Opens a session in Silverpeas for the authenticated user behinds the specified HTTP request.
   * <p/>
   * In order a session to be opened in Silverpeas, the user has to be authenticated. The
   * authentication of the user is represented by an authentication key that is unique for each user
   * so that a user can be also identified by its key.
   * <p/>
   * With its authentication key and some attributes from the request,a session in Silverpeas can
   * be opened and set for the user.
   *
   * @param request the HTTP request asking a session opening.
   * @param authKey the authentication key computed from a user authentication process and that is
   * unique to the user.
   * @return the URL of the user home page in Silverpeas or the URL of an error page if a problem
   *         occurred during the session opening (for example, the user wasn't authenticated).
   */
  public String openSession(HttpServletRequest request, String authKey) {
    HttpSession session = request.getSession();
    try {
      // Get the user profile from the admin
      SilverTrace.info("peasCore", "SilverpeasSessionOpenener.openSession()",
          "root.MSG_GEN_PARAM_VALUE", "session id=" + session.getId());
      MainSessionController controller = new MainSessionController(authKey, session.getId());
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
            getOrganisationController().
            getAdministratorUserIds(controller.getUserId())[0], controller.getFavoriteLanguage(),
            StringUtil.getBooleanValue(allowPasswordChange));
      }
      if (!controller.getCurrentUserDetail().isDeletedState()) {
        // Init session management and session object !!! This method reset theSession Object
        if (!UserDetail.isAnonymousUser(controller.getUserId())) {
          SessionManagementFactory factory = SessionManagementFactory.getFactory();
          SessionManagement sessionManagement = factory.getSessionManagement();
          sessionManagement.openSession(controller.getCurrentUserDetail(), request);
        }
        // Put the main session controller in the session
        session.setAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT, controller);
        return getHomePageUrl(request, redirectURL);
      }

    } catch (Exception e) {
      SilverTrace.error("peasCore", "SilverpeasSessionOpenener.openSession()",
          "peasCore.EX_LOGIN_SERVLET_CANT_CREATE_MAIN_SESSION_CTRL",
          "session id=" + session.getId(), e);
    }
    return getErrorPageUrl(request, authKey);
  }

  /**
   * Closes the session in Silverpeas for the user behind the specified HTTP request. All the
   * resources allocated for the maintain the user session in Silverpeas are then freed.
   *
   * @param request the HTTP request.
   */
  public void closeSession(HttpServletRequest request) {
    HttpSession session = request.getSession();
    session.removeAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    session.removeAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    Enumeration<String> names = (Enumeration<String>) session.getAttributeNames();
    while (names.hasMoreElements()) {
      String attributeName = names.nextElement();
      if (!attributeName.startsWith("Redirect") && !"gotoNew".equals(attributeName)) {
        session.removeAttribute(attributeName);
      }
    }
    SessionManagementFactory factory = SessionManagementFactory.getFactory();
    SessionManagement sessionManagement = factory.getSessionManagement();
    sessionManagement.closeSession(session.getId());
  }

  /**
   * The user wasn't yet authenticated then computes the error page.
   *
   * @param request the HTTP request asking a session opening.
   * @param authKey the authentication key computed from a user authentication process and that is
   * unique to the user.
   * @return the URL of an error page.
   */
  protected String getErrorPageUrl(HttpServletRequest request, String authKey) {
    SilverTrace.error("peasCore", "SilverpeasSessionOpenener.openSession()",
        "peasCore.EX_USER_KEY_NOT_FOUND", "key=" + authKey);
    StringBuilder absoluteUrl = new StringBuilder(getAbsoluteUrl(request));
    if (absoluteUrl.charAt(absoluteUrl.length() - 1) != '/') {
      absoluteUrl.append('/');
    }
    absoluteUrl.append("Login.jsp");
    return absoluteUrl.toString();
  }

  /**
   * The user was authenticated and its session in Silverpeas was opened successfully, then
   * computes its home page.
   *
   * @param request the HTTP request asking a session opening.
   * @param redirectURL a redirection URL.
   * @return the URL of the user home page in Silverpeas.
   */
  protected String getHomePageUrl(HttpServletRequest request, String redirectURL) {
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
    SilverTrace.debug("peasCore", "SilverpeasSessionOpenener.openSession",
        "user personal workspace=" + personalWs);

    // Put a graphicElementFactory in the session
    GraphicElementFactory gef = new GraphicElementFactory(controller.getFavoriteLook());
    if (StringUtil.isDefined(personalWs)) {
      gef.setSpaceId(personalWs);
    }
    gef.setHttpRequest(request);
    session.setAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT, gef);

    String favoriteFrame = gef.getLookFrame();
    SilverTrace.debug("peasCore", "SilverpeasSessionOpenener.openSession", "root.MSG_GEN_PARAM_VALUE",
        "controller.getUserAccessLevel()=" + controller.getUserAccessLevel());
    SilverTrace.debug("peasCore", "SilverpeasSessionOpenener.openSession", "root.MSG_GEN_PARAM_VALUE",
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
   *
   * @param request the HTTP request asking for a session opening.
   * @return an absolute URL from which the user home page will be computed.
   */
  protected String getAbsoluteUrl(HttpServletRequest request) {
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
      SilverTrace.warn("peasCore", "SilverpeasSessionOpenener.alertUserAboutPwdExpiration",
          "peasCore.EX_CANT_SEND_PASSWORD_EXPIRATION_ALERT", "userId = " + userId, e);
      return null;
    }
  }

  private void sendPopupNotificationAboutPwdExpiration(String userId, String fromUserId,
                                                       String language) throws NotificationManagerException {
    ResourceLocator messages = new ResourceLocator(
        "com.stratelia.silverpeas.peasCore.multilang.peasCoreBundle", language);
    NotificationSender sender = new NotificationSender(null);
    NotificationMetaData notifMetaData =
        new NotificationMetaData(NotificationParameters.NORMAL,
            messages.getString("passwordExpirationAlert"), messages
            .getString("passwordExpirationMessage"));
    notifMetaData.setSender(fromUserId);
    notifMetaData.addUserRecipient(new UserRecipient(userId));
    sender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP, notifMetaData);
  }
}

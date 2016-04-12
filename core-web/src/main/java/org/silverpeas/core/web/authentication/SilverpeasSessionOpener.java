/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.web.authentication;

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.Authentication;
import org.silverpeas.core.security.authentication.UserAuthenticationListener;
import org.silverpeas.core.security.authentication.UserAuthenticationListenerRegistration;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.token.SynchronizerTokenService;

import javax.inject.Singleton;
import javax.servlet.http.HttpSession;

/**
 * Service used to open an HTTP session in the Silverpeas platform.
 * <p/>
 * It asks for a session opening to the session manager and then it creates all the required session
 * resources for Silverpeas and stores them into the user session.
 *
 * @author ehugonnet
 */
@Singleton
public class SilverpeasSessionOpener {

  private static final int HTTP_DEFAULT_PORT = 80;

  protected SilverpeasSessionOpener() {
  }

  /**
   * Opens a session in Silverpeas for the authenticated user behinds the specified HTTP request.
   * <p/>
   * In order a session to be opened in Silverpeas, the user has to be authenticated. The
   * authentication of the user is represented by an authentication key that is unique for each user
   * so that a user can be also identified by its key.
   * <p/>
   * With its authentication key and some attributes from the request,a session in Silverpeas can be
   * opened and set for the user.
   * <p/>
   * In the case a session was already opened for the user with the same web browser, the current
   * session is then taken into account and the user access information is updated. In this case, no
   * new session is opened and the previous one isn't invalidated. If this behavior isn't what you
   * expect, then you have to close explicitly the current session of the user before calling this
   * method. In the case the user is already connected but with another web browser, a new session
   * is opened without invalidating the other one.
   *
   * @param request the HTTP request asking a session opening.
   * @param authKey the authentication key computed from a user authentication process and that is
   * unique to the user.
   * @return the URL of the user home page in Silverpeas or the URL of an error page if a problem
   * occurred during the session opening (for example, the user wasn't authenticated).
   */
  public String openSession(HttpRequest request, String authKey) {
    HttpSession session = request.getSession(false);
    // a session should exists: it could be either an authentication session opened for the
    // authentication process or an already opened user specific session.
    try {
      SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
      // is the current session is valid? If it is valid, then the information about the session
      // is updated with, for example, the timestamp of the last access (this one), and then it
      // is returned.
      SessionInfo sessionInfo = sessionManagement.validateSession(session.getId());
      String allowPasswordChange = (String) session.getAttribute(
          Authentication.PASSWORD_CHANGE_ALLOWED);
      MainSessionController controller;
      if (!sessionInfo.isDefined() || sessionInfo == SessionInfo.AnonymousSession) {
        // the session is a new one, then open it in Silverpeas
        controller = new MainSessionController(authKey, session.getId());
        session.setAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT, controller);
        // Get and store password change capabilities
        controller.setAllowPasswordChange(StringUtil.getBooleanValue(allowPasswordChange));
        if (!controller.getCurrentUserDetail().isDeletedState() &&
            !controller.getCurrentUserDetail().isDeactivatedState()) {
          if (!UserDetail.isAnonymousUser(controller.getUserId())) {
            sessionInfo = sessionManagement.openSession(controller.getCurrentUserDetail(), request);
            registerSuccessfulConnexion(controller);
            SynchronizerTokenService.getInstance().setUpSessionTokens(sessionInfo);
          } else {
            sessionManagement.openAnonymousSession(request);
          }
        }
      } else {
        // the session already exists, reuse it
        controller = (MainSessionController) session.getAttribute(
            MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      }

      // Notify user about password expiration if needed
      Boolean alertUserAboutPwdExpiration = (Boolean) session.getAttribute(
          Authentication.PASSWORD_IS_ABOUT_TO_EXPIRE);
      String redirectURL = null;
      if (alertUserAboutPwdExpiration != null && alertUserAboutPwdExpiration) {
        redirectURL = alertUserAboutPwdExpiration(controller.getUserId(),
            OrganizationControllerProvider.getOrganisationController()
                .getAdministratorUserIds(controller.getUserId())[0],
            controller.getFavoriteLanguage(),
            StringUtil.getBooleanValue(allowPasswordChange));
      }
      // Put the main session controller in the session
      return getHomePageUrl(request, redirectURL);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Session opening failure!", e);
    }

    return getErrorPageUrl(request, authKey);
  }

  /**
   * Some treatments in case of successful login
   *
   * @param controller
   */
  private void registerSuccessfulConnexion(MainSessionController controller) {

    // Last login date + nb successful login (reloading user data explicitly)
    UserDetail user = UserDetail.getById(controller.getUserId());
    user.setLastLoginDate(DateUtil.getNow());
    user.setNbSuccessfulLoginAttempts(user.getNbSuccessfulLoginAttempts() + 1);
    AdminController adminController = ServiceProvider.getService(AdminController.class);
    adminController.updateUser(user);
  }

  /**
   * Closes the specified session.
   *
   * All the resources allocated for the maintain the user session in Silverpeas are then freed.
   *
   * @param session the HTTP session to close.
   */
  public void closeSession(HttpSession session) {
    if (session != null) {
      SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
      sessionManagement.closeSession(session.getId());
    }
  }

  /**
   * The user wasn't yet authenticated then computes the error page.
   *
   * @param request the HTTP request asking a session opening.
   * @param authKey the authentication key computed from a user authentication process and that is
   * unique to the user.
   * @return the URL of an error page.
   */
  protected String getErrorPageUrl(HttpRequest request, String authKey) {
    SilverLogger.getLogger(this).error("Not user found with the authentication key {0}", authKey);
    StringBuilder absoluteUrl = new StringBuilder(getAbsoluteUrl(request));
    if (absoluteUrl.charAt(absoluteUrl.length() - 1) != '/') {
      absoluteUrl.append('/');
    }
    absoluteUrl.append("Login.jsp");
    return absoluteUrl.toString();
  }

  /**
   * The user was authenticated and its session in Silverpeas was opened successfully, then computes
   * its home page.
   *
   * @param request the HTTP request asking a session opening.
   * @param redirectURL a redirection URL.
   * @return the URL of the user home page in Silverpeas.
   */
  protected String getHomePageUrl(HttpRequest request, String redirectURL) {
    String absoluteBaseURL = getAbsoluteUrl(request);
    StringBuilder absoluteUrl = new StringBuilder(absoluteBaseURL);

    HttpSession session = request.getSession(false);
    MainSessionController controller = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    // Init server name and server port
    String serverName = request.getServerName();
    int serverPort = request.getServerPort();
    String port = "";
    if (serverPort != HTTP_DEFAULT_PORT) {
      port = String.valueOf(serverPort);
    }
    controller.initServerProps(serverName, port);

    // Retrieve personal workspace
    String personalWs = controller.getPersonalization().getPersonalWorkSpaceId();

    // Put a graphicElementFactory in the session
    GraphicElementFactory gef = new GraphicElementFactory(controller.getFavoriteLook());
    if (StringUtil.isDefined(personalWs)) {
      gef.setSpaceIdForCurrentRequest(personalWs);
    }
    gef.setHttpRequest(request);
    session.setAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT, gef);

    String favoriteFrame = gef.getLookFrame();
    String sDirectAccessSpace = request.getParameter("DirectAccessSpace");
    String sDirectAccessCompo = request.getParameter("DirectAccessCompo");
    if (controller.isAppInMaintenance() && !controller.getCurrentUserDetail().isAccessAdmin()) {
      absoluteUrl.append("/admin/jsp/appInMaintenance.jsp");
    } else if (StringUtil.isDefined(redirectURL)) {
      absoluteUrl.append(redirectURL);
    } else if (StringUtil.isDefined(sDirectAccessSpace) && StringUtil.isDefined(sDirectAccessCompo)) {
      absoluteUrl.append(URLUtil.getURL(sDirectAccessSpace, sDirectAccessCompo)).append("Main");
    } else {
      absoluteUrl.append("/Main/").append(favoriteFrame);
    }

    // checks authentication hooks
    String alternativeURL = null;
    for (UserAuthenticationListener listener : UserAuthenticationListenerRegistration
        .getListeners()) {
      alternativeURL = listener.firstHomepageAccessAfterAuthentication(request,
          controller.getCurrentUserDetail(), absoluteUrl.toString());
    }
    if (StringUtil.isDefined(alternativeURL)) {
      return absoluteBaseURL + alternativeURL;
    }
    return absoluteUrl.toString();
  }

  /**
   * Computes the beginning of an absolute URL for the home page.
   *
   * @param request the HTTP request asking for a session opening.
   * @return an absolute URL from which the user home page will be computed.
   */
  protected String getAbsoluteUrl(HttpRequest request) {
    StringBuilder absoluteUrl = new StringBuilder(256);
    if (request.isSecure()) {
      absoluteUrl.append("https://");
    } else {
      absoluteUrl.append("http://");
    }
    absoluteUrl.append(request.getServerName()).append(':');
    absoluteUrl.append(request.getServerPort());
    absoluteUrl.append(URLUtil.getApplicationURL());
    return absoluteUrl.toString();
  }

  private String alertUserAboutPwdExpiration(String userId, String fromUserId,
      String language, boolean allowPasswordChange) {
    try {
      SettingBundle settings = ResourceLocator.getSettingBundle(
          "org.silverpeas.authentication.settings.passwordExpiration");
      String notificationType = settings.getString("notificationType", "POPUP");
      String passwordChangeURL = settings.getString("passwordChangeURL",
          "defaultPasswordAboutToExpire.jsp");

      if ("POPUP".equalsIgnoreCase(notificationType) || !allowPasswordChange) {
        sendPopupNotificationAboutPwdExpiration(userId, fromUserId, language);
        return null;
      }
      return passwordChangeURL;
    } catch (NotificationManagerException e) {
      SilverLogger.getLogger(this)
          .error("Cannot send the password expiration alert for user {0}", new String[]{userId}, e);
      return null;
    }
  }

  private void sendPopupNotificationAboutPwdExpiration(String userId, String fromUserId,
      String language) throws NotificationManagerException {
    LocalizationBundle messages = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.peasCore.multilang.peasCoreBundle", language);
    NotificationSender sender = new NotificationSender(null);
    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        messages.getString("passwordExpirationAlert"), messages
        .getString("passwordExpirationMessage"));
    notifMetaData.setSender(fromUserId);
    notifMetaData.addUserRecipient(new UserRecipient(userId));
    sender.notifyUser(NotificationParameters.ADDRESS_BASIC_POPUP, notifMetaData);
  }

  public static SilverpeasSessionOpener getInstance() {
    return ServiceProvider.getService(SilverpeasSessionOpener.class);
  }
}

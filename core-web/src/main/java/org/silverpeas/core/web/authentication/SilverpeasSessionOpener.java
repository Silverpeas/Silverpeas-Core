/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.authentication;

import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;
import org.silverpeas.core.security.authentication.AuthenticationProtocol;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.UserAuthenticationListener;
import org.silverpeas.core.security.authentication.UserAuthenticationListenerRegistration;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.rs.HTTPAuthentication;
import org.silverpeas.core.web.token.SynchronizerTokenService;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.http.HttpSession;

import static org.silverpeas.core.admin.service.OrganizationControllerProvider.getOrganisationController;

/**
 * Service used to open an HTTP session in the Silverpeas platform.
 * <p>
 * It asks for a session opening to the session manager, and then it creates all the required session
 * resources for Silverpeas and stores them into the user session.
 * @author ehugonnet
 */
@Technical
@Bean
public class SilverpeasSessionOpener {

  SilverpeasSessionOpener() {
  }

  /**
   * Opens a session in Silverpeas for the authenticated user behinds the specified HTTP request.
   * <p>
   * In order a session to be opened in Silverpeas, the user has to be authenticated. The
   * authentication of the user is represented by an authentication key that is unique for each user
   * so that a user can be also identified by its key.
   * <p>
   * With its authentication key and some attributes from the request,a session in Silverpeas can be
   * opened and set for the user.
   * <p>
   * In the case a session was already opened for the user with the same web browser, the current
   * session is then taken into account and the user access information is updated. In this case, no
   * new session is opened and the previous one isn't invalidated. If this behavior isn't what you
   * expect, then you have to close explicitly the current session of the user before calling this
   * method. In the case the user is already connected but with another web browser, a new session
   * is opened without invalidating the other one.
   * @param request the HTTP request asking a session opening.
   * @param authKey the authentication key computed from a user authentication process and that is
   * unique to the user.
   * @return the URL of the user home page in Silverpeas or the URL of an error page if a problem
   * occurred during the session opening (for example, the user wasn't authenticated).
   */
  public String openSession(HttpRequest request, String authKey) {
    HttpSession session = request.getSession(false);
    // a session should exist: it could be either an authentication session opened for the
    // authentication process or an already opened user specific session.
    try {
      SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
      // is the current session is valid? If it is valid, then the information about the session
      // is updated with, for example, the timestamp of the last access (this one), and then it
      // is returned.
      SessionInfo sessionInfo = sessionManagement.validateSession(session.getId());
      MainSessionController controller;
      if (!sessionInfo.isDefined() || sessionInfo.isAnonymous()) {
        // the session is a new one, then open it in Silverpeas
        controller = new MainSessionController(authKey, session);
        // Get and store password change capabilities
        controller.setAllowPasswordChange(isPasswordChangedAllowed(session));
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
      String redirectURL = notifyAboutPasswordExpiration(session, controller);
      // Put the main session controller in the session
      return getHomePageUrl(request, redirectURL, true);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Session opening failure!", e);
    }

    SilverLogger.getLogger(this).error("No user found with the authentication key {0}", authKey);
    return getErrorPageUrl(request);
  }

  /**
   * Prepares current session in Silverpeas from a {@link SessionInfo} obtained by another service
   * in charge of authentication of Users (like {@link HTTPAuthentication} from WEB services).
   * <p>
   * In the case a session was already opened for the user with the same web browser, the current
   * session is then taken into account and the user access information is updated. In this case, no
   * new session is opened and the previous one isn't invalidated. If this behavior isn't what you
   * expect, then you have to close explicitly the current session of the user before calling this
   * method. In the case the user is already connected but with another web browser, a new session
   * is opened without invalidating the other one.
   * @param request the HTTP request asking to prepare a session from a {@link SessionInfo}.
   * @param sessionInfo an existing session info.
   * @return the URL of the user home page in Silverpeas or the URL of an error page if a problem
   * occurred during the session opening (for example, the user wasn't really authenticated).
   */
  String prepareFromExistingSessionInfo(HttpRequest request, SessionInfo sessionInfo) {
    HttpSession session = request.getSession(false);
    // a session should exist: it could be either an authentication session opened for the
    // authentication process or an already opened user specific session.
    try {

      if (!sessionInfo.isDefined()) {
        throw new SilverpeasRuntimeException("session " + session.getId() + " is not defined");
      }

      MainSessionController controller = (MainSessionController) session
          .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

      if (controller != null) {
        // the session already exists indeed, reuse it
        controller = (MainSessionController) session
            .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      } else {
        // the session is a new one, then open it in Silverpeas
        controller = new MainSessionController(sessionInfo, session);
        if (!sessionInfo.isAnonymous()) {
          // Get and store password change capabilities
          controller.setAllowPasswordChange(isPasswordChangedAllowed(session));
          // Registering the successful connexion at this time
          registerSuccessfulConnexion(controller);
        }
      }

      // Notify user about password expiration if needed
      String redirectURL = notifyAboutPasswordExpiration(session, controller);
      // Put the main session controller in the session
      return getHomePageUrl(request, redirectURL, true);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Session opening failure!", e);
    }

    SilverLogger.getLogger(this)
        .error("No user found with the session into {0}", sessionInfo.getSessionId());
    return getErrorPageUrl(request);
  }

  /**
   * Notifies, if necessary, the user behind the session that his password is expiring.
   * @param session a valid HTTP session
   * @param controller a valid main controller.
   * @return the URL of the page that permits to the user to change its password, null if no
   * password change is needed.
   */
  private String notifyAboutPasswordExpiration(final HttpSession session,
      final MainSessionController controller) {
    Boolean alertUserAboutPwdExpiration =
        (Boolean) session.getAttribute(AuthenticationProtocol.PASSWORD_IS_ABOUT_TO_EXPIRE);
    String redirectURL = null;
    if (alertUserAboutPwdExpiration != null && alertUserAboutPwdExpiration) {
      redirectURL = alertUserAboutPwdExpiration(controller.getUserId(),
          getOrganisationController().getAdministratorUserIds(controller.getUserId())[0],
          controller.getFavoriteLanguage(), isPasswordChangedAllowed(session));
    }
    return redirectURL;
  }

  /**
   * Indicates if the change of password is allowed.<br> The information is taken from the HTTP
   * session which has been updated by the {@link AuthenticationService}
   * @param session the HTTP session.
   * @return true if password change is allowed, false otherwise.
   */
  private boolean isPasswordChangedAllowed(final HttpSession session) {
    return StringUtil
        .getBooleanValue((String) session.getAttribute(AuthenticationProtocol.PASSWORD_CHANGE_ALLOWED));
  }

  /**
   * Some treatments in case of successful login
   * @param controller a valid main controller
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
   * Closes the specified session. All the resources allocated to maintain the user session in
   * Silverpeas are then freed.
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
   * @param request the HTTP request asking a session opening.
   * @return the URL of an error page.
   */
  String getErrorPageUrl(HttpRequest request) {
    StringBuilder absoluteUrl = new StringBuilder(getAbsoluteUrl(request));
    if (absoluteUrl.charAt(absoluteUrl.length() - 1) != '/') {
      absoluteUrl.append('/');
    }
    absoluteUrl.append("Login");
    return absoluteUrl.toString();
  }

  /**
   * The user was authenticated and its session in Silverpeas was opened successfully, then computes
   * its home page.
   * @param request the HTTP request asking a session opening.
   * @param redirectURL a redirection URL.
   * @param isFirstSessionAccess true if it is the first home page access of the session.
   * @return the URL of the user home page in Silverpeas.
   */
  String getHomePageUrl(HttpRequest request, String redirectURL,
      final boolean isFirstSessionAccess) {
    String absoluteBaseURL = getAbsoluteUrl(request);
    StringBuilder absoluteUrl = new StringBuilder(absoluteBaseURL);

    HttpSession session = request.getSession(false);
    MainSessionController controller = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

    // Retrieve personal workspace
    String personalWs = controller.getPersonalization().getPersonalWorkSpaceId();

    // Put a graphicElementFactory in the session
    final GraphicElementFactory gef = new GraphicElementFactory(controller);
    if (StringUtil.isDefined(personalWs)) {
      gef.setSpaceIdForCurrentRequest(personalWs);
    }
    gef.setHttpRequest(request);
    session.setAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT, gef);

    String favoriteFrame = gef.getLookFrame();
    String sDirectAccessSpace = request.getParameter("DirectAccessSpace");
    String sDirectAccessCompo = request.getParameter("DirectAccessCompo");
    if (MainSessionController.isAppInMaintenance() &&
        !controller.getCurrentUserDetail().isAccessAdmin()) {
      absoluteUrl.append("/admin/jsp/appInMaintenance.jsp");
    } else if (StringUtil.isDefined(redirectURL)) {
      absoluteUrl.append(redirectURL);
    } else if (StringUtil.isDefined(sDirectAccessSpace) &&
        StringUtil.isDefined(sDirectAccessCompo)) {
      absoluteUrl.append(URLUtil.getURL(sDirectAccessSpace, sDirectAccessCompo)).append("Main");
    } else {
      absoluteUrl.append("/Main/").append(favoriteFrame);
    }

    return performUserAuthenticationListener(request, controller, absoluteUrl.toString(),
        isFirstSessionAccess);
  }

  /**
   * Computes the beginning of an absolute URL for the home page.
   * @param request the HTTP request asking for a session opening.
   * @return an absolute URL from which the user home page will be computed.
   */
  String getAbsoluteUrl(HttpRequest request) {
    StringBuilder absoluteUrl = new StringBuilder(256);
    if (request.isSecure()) {
      absoluteUrl.append("https://");
    } else {
      //noinspection HttpUrlsUsage
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
    } catch (NotificationException e) {
      SilverLogger.getLogger(this)
          .error("Cannot send the password expiration alert for user {0}", new String[]{userId}, e);
      return null;
    }
  }

  private void sendPopupNotificationAboutPwdExpiration(String userId, String fromUserId,
      String language) throws NotificationException {
    LocalizationBundle messages = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.peasCore.multilang.peasCoreBundle", language);
    NotificationSender sender = new NotificationSender(null);
    NotificationMetaData notifMetaData =
        new NotificationMetaData(NotificationParameters.PRIORITY_NORMAL,
            messages.getString("passwordExpirationAlert"), messages
            .getString("passwordExpirationMessage"));
    notifMetaData.setSender(fromUserId);
    notifMetaData.addUserRecipient(new UserRecipient(userId));
    sender.notifyUser(BuiltInNotifAddress.BASIC_POPUP.getId(), notifMetaData);
  }

  /**
   * Performs all the registered {@link UserAuthenticationListener} instances which some could
   * compute and return an alternative URL.
   * @param request the current request.
   * @param controller the main controller instance.
   * @param homePageUrl the home page url.
   * @param isFirstSessionAccess a boolean indicating it is first access to Silverpeas in the
   * current session.
   * @return the given homePageUrl or an alternative one.
   */
  private String performUserAuthenticationListener(final HttpRequest request,
      final MainSessionController controller, final String homePageUrl,
      final boolean isFirstSessionAccess) {
    String alternativeURL = null;
    for (UserAuthenticationListener listener : UserAuthenticationListenerRegistration
        .getListeners()) {
      final String url;
      if (isFirstSessionAccess) {
        url = listener
            .firstHomepageAccessAfterAuthentication(request, controller.getCurrentUserDetail(),
                homePageUrl);
      } else {
        url = listener.homepageAccessFromLoginWhenUserSessionAlreadyOpened(request,
            controller.getCurrentUserDetail(), homePageUrl);
      }
      if (StringUtil.isDefined(url)) {
        alternativeURL = url;
      }
    }
    if (StringUtil.isDefined(alternativeURL)) {
      return getAbsoluteUrl(request) + alternativeURL;
    }
    return homePageUrl;
  }

  public static SilverpeasSessionOpener getInstance() {
    return ServiceProvider.getService(SilverpeasSessionOpener.class);
  }
}

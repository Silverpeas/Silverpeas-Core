/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasHttpServlet;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Optional;

import static javax.ws.rs.core.UriBuilder.fromPath;
import static org.silverpeas.core.util.StringUtil.*;

/**
 * This servlet handle the login page access.<br>
 * If it exists an opened user session, the user is redirected to the welcome Silverpeas page.
 * @author Yohann Chastagnier
 */
public class LoginServlet extends SilverpeasHttpServlet {
  private static final long serialVersionUID = 4492227920914085441L;
  private static final String LOGOUT_PARAM = "logout";

  @Inject
  private SilverpeasSessionOpener silverpeasSessionOpener;

  private static final SettingBundle general =
      ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
  public static final String PARAM_DOMAINID = "DomainId";

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {

    // Verify the user is authenticated
    final UserSessionStatus userSessionStatus = existOpenedUserSession(request);
    if (userSessionStatus.isValid()) {
      performOpenedUserSession(request, response, userSessionStatus);
    } else {
      performLoginDispatch(request, response);
    }
  }

  /**
   * Performs the redirection when an opened user session exists.
   * @param request the current request.
   * @param response the current response.
   * @param userSessionStatus the status of user session.
   * @throws IOException on redirect error.
   */
  private void performOpenedUserSession(final HttpServletRequest request,
      final HttpServletResponse response, final UserSessionStatus userSessionStatus)
      throws ServletException, IOException {
    if (mustCloseSession(request)) {
      final HttpSession session = request.getSession(false);
      silverpeasSessionOpener.closeSession(session);
      performLoginDispatch(request, response);
    } else {
      final HttpRequest httpRequest = HttpRequest.decorate(request);
      final String destinationUrl;
      if (userSessionStatus.isFromDesktop()) {
        destinationUrl =
            silverpeasSessionOpener.getHomePageUrl(httpRequest, null, false);
      } else {
        destinationUrl = silverpeasSessionOpener
            .prepareFromExistingSessionInfo(httpRequest, userSessionStatus.getInfo());
      }
      redirectOrForwardService(httpRequest, response, destinationUrl);
    }
  }

  /**
   * Indicates if the session must be closed.
   * @return true if the session must be closed, false otherwise.
   */
  private boolean mustCloseSession(final HttpServletRequest request) {
    return (getSsoLoginPage(request).isPresent() && general.getBoolean("login.sso.path.newSession", false))
        || "Error_SsoNotAllowed".equals(getErrorCode(request));
  }

  /**
   * @param request the current request.
   * @return an optional SSO login page.
   */
  private Optional<String> getSsoLoginPage(final HttpServletRequest request) {
    final String ssoPath = general.getString("login.sso.path", "");
    return isDefined(ssoPath) ?
        Optional.of(fromPath(request.getContextPath()).path(ssoPath).build().toString()) :
        Optional.empty();
  }

  private boolean isAnonymousAccessActivated() {
    return UserDetail.isAnonymousUserExist();
  }

  /**
   * Performs the rules of Login dispatch.<br>
   * This method must be called only if it does not exists an authenticated user into the session.
   * @param servletRequest the current request.
   * @param response the current response.
   * @throws IOException in case of redirect error.
   */
  private void performLoginDispatch(final HttpServletRequest servletRequest,
      final HttpServletResponse response) throws IOException, ServletException {

    HttpRequest request = HttpRequest.decorate(servletRequest);

    String loginPage;
    String errorCode = getErrorCode(request);
    final Optional<String> ssoLoginPage = getSsoLoginPage(request);
    if (ssoLoginPage.isPresent()
        && (isNotDefined(errorCode) || "3".equals(errorCode))
        && !request.getParameterAsBoolean(LOGOUT_PARAM)) {
      loginPage = ssoLoginPage.get();
      response.sendRedirect(response.encodeRedirectURL(loginPage));
    } else if (isNotDefined(errorCode) && isAnonymousAccessActivated() &&
        !request.isWithinAnonymousUserSession() && !request.isWithinUserSession()) {

      // first access to the platform
      UserDetail anonymousUser = UserDetail.getAnonymousUser();
      UriBuilder uriBuilder = UriBuilder.fromPath("/AuthenticationServlet");
      addParameter(uriBuilder, "Login", anonymousUser.getLogin());
      addParameter(uriBuilder, "Password", anonymousUser.getLogin());
      addParameter(uriBuilder, PARAM_DOMAINID, "0");

      RequestDispatcher dispatcher = request.getRequestDispatcher(uriBuilder.toTemplate());
      dispatcher.forward(request, response);
    } else {
      loginPage = general.getString("loginPage");

      if (!isDefined(loginPage)) {
        loginPage = request.getContextPath() + "/defaultLogin.jsp";
      } else if (!loginPage.startsWith(request.getContextPath())) {
        loginPage = request.getContextPath() + "/" + loginPage;
      }
      UriBuilder uriBuilder = UriBuilder.fromPath(loginPage);
      addParameter(uriBuilder, PARAM_DOMAINID, getDomainId(request));
      addParameter(uriBuilder, "ErrorCode", errorCode);
      addParameter(uriBuilder, LOGOUT_PARAM, request.getParameter(LOGOUT_PARAM));

      response.sendRedirect(response.encodeRedirectURL(uriBuilder.toTemplate()));
    }
  }

  private void addParameter(UriBuilder uriBuilder, String param, String value) {
    if (StringUtil.isDefined(value)) {
      uriBuilder.queryParam(param, value);
    }
  }

  /**
   * Gets the error code from the request.
   * @param request the current request.
   * @return the error code if any.
   */
  private String getErrorCode(final HttpServletRequest request) {
    return request.getParameter("ErrorCode");
  }

  /**
   * Gets the domain identifier if any.
   * @param request the current request.
   * @return the identifier of the domain if any, null otherwise.
   */
  private String getDomainId(final HttpServletRequest request) {
    String domainId = general.getString("loginPage.domainId.default");
    if (isInteger(request.getParameter(PARAM_DOMAINID))) {
      domainId = request.getParameter(PARAM_DOMAINID);
    }
    return domainId;
  }
}

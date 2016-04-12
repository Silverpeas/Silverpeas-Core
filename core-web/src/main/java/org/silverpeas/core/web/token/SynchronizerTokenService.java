/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.token;

import org.silverpeas.core.date.DateTime;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.TokenGenerator;
import org.silverpeas.core.security.token.TokenGeneratorProvider;
import org.silverpeas.core.security.token.exception.TokenValidationException;
import org.silverpeas.core.security.token.synchronizer.SynchronizerToken;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.util.security.SecuritySettings;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

/**
 * A service to manage the synchronizer tokens used in Silverpeas to protect the user sessions or
 * the web resources published by Silverpeas.
 *
 * Each resource in Silverpeas and accessible through the Web can be protected by one or more
 * security tokens. These tokens are named synchronizer token as they are transmitted within each
 * request and must match the ones expected by Silverpeas to access the asked resource. This service
 * provides the functions to generate, to validate and to set such tokens for the Web resource in
 * Silverpeas to protect (not all resources require to be protected in Silverpeas).
 *
 * @author mmoquillon
 */
@Singleton
public class SynchronizerTokenService {

  public static final String SESSION_TOKEN_KEY = "X-STKN";
  public static final String NAVIGATION_TOKEN_KEY = "X-NTKN";
  private static final String UNPROTECTED_URI_RULE =
      "(?i)(?!.*(/qaptcha|rpdcsearch/|rclipboard/|rchat/chat[0-9]+|blockingNews|services/password/)).*";
  private static final String DEFAULT_GET_RULE
      = "(?i)^/\\w+[\\w/]*/jsp/.*(delete|update|creat|block|unblock).*$";
  private static final SilverLogger logger = SilverLogger.getLogger("security");
  private static final List<String> DEFAULT_PROTECTED_METHODS = Arrays.asList("POST", "PUT",
      "DELETE");


  public static SynchronizerTokenService getInstance() {
    return ServiceProvider.getService(SynchronizerTokenService.class);
  }

  protected SynchronizerTokenService() {

  }

  /**
   * Sets up a session token for the specified Silverpeas session. It creates a synchronizer token
   * to protect the specified opened user session. If a token is already protecting the session, the
   * token is then renewed.
   *
   * A session token is a token used to validate that any requests to a protected web resource are
   * correctly sent within an opened and valid user session. The setting occurs only if the security
   * mechanism by token is enabled.
   *
   * @param session the user session to protect with a synchronizer token.
   */
  public void setUpSessionTokens(SessionInfo session) {
    if (SecuritySettings.isWebSecurityByTokensEnabled()) {
      UserDetail user = session.getUserDetail();
      Token token = session.getAttribute(SESSION_TOKEN_KEY);
      TokenGenerator generator = TokenGeneratorProvider.getTokenGenerator(SynchronizerToken.class);
      if (token != null) {
        logger.info("Renew the session token for the user {0} ({1})",
            user.getId(), user.getDisplayedName());
        token = generator.renew(token);
      } else {
        logger.info("Create the session token for the user {0} ({1})",
            user.getId(), user.getDisplayedName());
        token = generator.generate();
      }
      session.setAttribute(SESSION_TOKEN_KEY, token);
    }
  }

  /**
   * Sets up a navigation token for the user behind the specified request. It creates a synchronizer
   * token to protect the web navigation of the user from this start (the current resource targeted
   * by the request). Within a protected navigation, each request must be stamped with the
   * navigation token in order to be accepted (otherwise the request is rejected). Each time a
   * request is validated with a navigation token, the token is then renewed.
   *
   * @param request an HTTP request from which the navigation to protect is identified.
   */
  public void setUpNavigationTokens(HttpServletRequest request) {
    if (SecuritySettings.isWebSecurityByTokensEnabled()) {
      logger.info("Create a navigation token for path {0}", getRequestPath(request));
      HttpSession session = request.getSession();
      TokenGenerator generator = TokenGeneratorProvider.getTokenGenerator(SynchronizerToken.class);
      Token token = generator.generate();
      session.setAttribute(NAVIGATION_TOKEN_KEY, token);
    }
  }

  /**
   * Validates the request to a Silverpeas web resource can be trusted. The request is validated
   * only if both the security mechanism by token is enabled and the request targets a protected web
   * resource.
   *
   * The access to a protected web resource is considered as trusted if and only if it is stamped
   * with the expected security tokens for the requested resource. Otherwise, the request isn't
   * considered as trusted and should be rejected. A request is stamped at least with the session
   * token, that is to say with the token that is set with the user session.
   *
   * @param request the HTTP request to check.
   * @throws TokenValidationException if the specified request cannot be trusted.
   */
  public void validate(HttpServletRequest request) throws TokenValidationException {
    if (SecuritySettings.isWebSecurityByTokensEnabled() && isAProtectedResource(request)) {
      logger.info("Validate the request for path {0}", getRequestPath(request));
      Token expectedToken = getSessionToken(request);
      // is there a user session opened?
      if (expectedToken.isDefined()) {
        String actualToken = getTokenInRequest(SESSION_TOKEN_KEY, request);
        validate(actualToken, expectedToken);
      }

      // is the navigation protected by a token?
      // the token is popped from the current session.
      expectedToken = getTokenInSession(NAVIGATION_TOKEN_KEY, request, true);
      if (expectedToken.isDefined()) {
        logger.info("Validate the request origin for path {0}", getRequestPath(request));
        String actualToken = getTokenInRequest(NAVIGATION_TOKEN_KEY, request);
        validate(actualToken, expectedToken);
      }
    }
  }

  /**
   * Is the resource targeted by the specified request must be protected by a synchronizer token?
   *
   * A resource is protected if either the request is a POST, PUT or a DELETE HTTP method or if the
   * requested URI is declared as to be protected.
   *
   * @param request the request to a possibly protected resource.
   * @return true if the requested resource is a protected one and then the request should be
   * validate.
   */
  public boolean isAProtectedResource(HttpServletRequest request) {
    boolean isProtected = false;
    if (request.getRequestURI().matches(UNPROTECTED_URI_RULE)) {
      isProtected = DEFAULT_PROTECTED_METHODS.contains(request.getMethod());
      if (!isProtected && request.getMethod().equals("GET")) {
        String path = getRequestPath(request);
        isProtected = path.matches(DEFAULT_GET_RULE);
      }
    }
    return isProtected;
  }

  /**
   * Gets the synchronizer token used to protect the session of the user behind the specified
   * request.
   *
   * @param request an HTTP request.
   * @return the synchronizer token. If no token was set for the session mapped with the specified
   * request or if no session was opened, then the returned token isn't defined (NoneToken).
   */
  public Token getSessionToken(HttpServletRequest request) {
    return getTokenInSession(SESSION_TOKEN_KEY, request, false);
  }

  /**
   * Gets the synchronizer token used to protect the specified user session.
   *
   * @param session an opened session of a user in Silverpeas
   * @return the token protecting the specified session.
   */
  public Token getSessionToken(SessionInfo session) {
    Token token = session.getAttribute(SESSION_TOKEN_KEY);
    return (token == null ? SynchronizerToken.NoneToken : token);
  }

  /**
   * Gets the current one-time synchronizer token used to protect the web navigation within which
   * the specified request is sent.
   *
   * @param request an HTTP request.
   * @return the synchronizer token. If no token carried by the specified request to validate its
   * origin, then a NoneToken is returned.
   */
  public Token getNavigationToken(HttpServletRequest request) {
    return getTokenInSession(NAVIGATION_TOKEN_KEY, request, false);
  }

  private String getRequestPath(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path.startsWith(request.getContextPath())) {
      path = path.substring(request.getContextPath().length());
    }
    return path;
  }

  private void validate(String actualToken, Token expectedToken) throws TokenValidationException {
    if (!(StringUtil.isDefined(actualToken) && expectedToken.isDefined()
        && expectedToken.getValue().equals(actualToken))) {
      throwTokenInvalidException();
    }
  }

  private void throwTokenInvalidException() throws TokenValidationException {
    DateTime now = DateTime.now();
    throw new TokenValidationException("Attempt of a CSRF attack detected at " + now.toISO8601());
  }

  private Token getTokenInSession(String tokenId, HttpServletRequest request, boolean pop) {
    Token token = null;
    HttpSession session = request.getSession(false);
    if (session != null) {
      token = (Token) session.getAttribute(tokenId);
      if (token != null && pop) {
        session.removeAttribute(tokenId);
      }
    }
    if (token == null) {
      String sessionId = request.getHeader(UserPrivilegeValidation.HTTP_SESSIONKEY);
      if (StringUtil.isDefined(sessionId)) {
        SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
        SessionInfo sessionInfo = sessionManagement.getSessionInfo(sessionId);
        if (sessionInfo.isDefined()) {
          token = sessionInfo.getAttribute(tokenId);
          if (token != null && pop) {
            sessionInfo.unsetAttribute(tokenId);
          }
        }
      }
    }

    return (token == null ? SynchronizerToken.NoneToken : token);
  }

  private String getTokenInRequest(String tokenId, HttpServletRequest request) {
    String token = request.getHeader(tokenId);
    if (StringUtil.isNotDefined(token)) {
      token = request.getParameter(tokenId);
      if (StringUtil.isNotDefined(token)) {
        token = (String) request.getAttribute(tokenId);
      }
    }
    return token;
  }

}

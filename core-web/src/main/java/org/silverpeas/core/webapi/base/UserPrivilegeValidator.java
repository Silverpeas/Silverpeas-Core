/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.base;

import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionValidationContext;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.codec.binary.Base64;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.SimpleDocumentAccessControl;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.UserSessionReference;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.UserReference;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.util.Charsets;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.isInteger;

/**
 * It is a decorator of a REST-based web service that provides access to the validation of the
 * authentication and of the authorization for a caller to request the decorated web service.
 *
 * Indeed, the validation mechanism is encapsulated within the RESTWebService as it requires access
 * to the incoming HTTP request as well to the current user session if any. In order to delegate
 * externally the validation triggering,
 */
@Singleton
public class UserPrivilegeValidator implements UserPrivilegeValidation {

  @Inject
  private SessionManagement sessionManagement;

  @Inject
  private ComponentAccessControl componentAccessController;

  @Inject
  private SimpleDocumentAccessControl documentAccessController;

  @Inject
  private OrganizationController organisationController;

  /**
   * This constant is used to specify into the request attributes if the registering of "the last
   * access time" of the user must be skipped. Indeed, some REST services uses the user session
   * validation process in order to get the user behind the request if any. But those services
   * must not impact on "the last access time" of the user.
   */
  private static final String SKIP_LAST_USER_ACCESS_TIME_REGISTERING =
      "SKIP_LAST_USER_ACCESS_TIME_REGISTERING";

  /**
   * Validates the authentication of the user at the origin of a web request.
   *
   * The validation checks first the user is already authenticated and then it has a valid opened
   * session in Silverpeas. Otherwise it attempts to open a new session for the user by using its
   * credentials passed through the request (as an HTTP header). Once the authentication succeed,
   * the identification of the user is done and detail about it can then be got. A runtime exception
   * is thrown with an HTTP status code UNAUTHORIZED (401) at validation failure. The validation
   * fails when one of the below situation is occurring: <ul> <li>The user session key is
   * invalid;</li> <li>The user isn't authenticated and no credentials are passed with the
   * request;</li> <li>The user authentication failed.</li> </ul>
   *
   * @param request the HTTP request from which the authentication of the caller can be done.
   * @return the opened session of the user at the origin of the specified request.
   * @throws WebApplicationException exception if the validation failed.
   */
  @Override
  public SessionInfo validateUserAuthentication(final HttpServletRequest request) throws
      WebApplicationException {
    SessionInfo userSession;
    String sessionKey = getUserSessionKey(request);
    if (isDefined(sessionKey)) {
      SessionValidationContext sessionValidationContext =
          SessionValidationContext.withSessionKey(sessionKey);
      if (mustSkipLastUserAccessTimeRegistering(request)) {
        sessionValidationContext.skipLastUserAccessTimeRegistering();
      }
      userSession = validateUserSession(sessionValidationContext);
    } else {
      userSession = authenticateUser(request);
    }

    // Verify that the user can login
    verifyUserCanLogin(userSession);

    // Returning the user session
    return userSession;
  }

  /**
   * Sets into the request attributes the {@link
   * UserPrivilegeValidator#SKIP_LAST_USER_ACCESS_TIME_REGISTERING} attribute to true.
   * @param request the current request performed.
   * @return itself.
   */
  @Override
  public UserPrivilegeValidation skipLastUserAccessTimeRegistering(final HttpServletRequest
      request) {
    request.setAttribute(SKIP_LAST_USER_ACCESS_TIME_REGISTERING, true);
    return this;
  }

  /**
   * Indicates if the last user access time registering must be skipped from the value of {@link
   * UserPrivilegeValidator#SKIP_LAST_USER_ACCESS_TIME_REGISTERING} attribute contained into the
   * request attributes.
   * @param request the current request performed.
   * @return true to skip, false otherwise.
   */
  private boolean mustSkipLastUserAccessTimeRegistering(final HttpServletRequest request) {
    return request.getAttribute(SKIP_LAST_USER_ACCESS_TIME_REGISTERING) != null;
  }

  /**
   * Verify that the user can login
   *
   * @param userSession
   */
  private void verifyUserCanLogin(SessionInfo userSession) {
    if (userSession != null && userSession.getUserDetail() != null) {
      try {
        AuthenticationUserVerifierFactory.getUserCanLoginVerifier(userSession.getUserDetail())
            .verify();
      } catch (AuthenticationException e) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    }
  }

  /**
   * Validates the authorization of the specified user to access the component instance with the
   * specified unique identifier.
   *
   * @param user the user for whom the authorization has to be validated.
   * @param instanceId the unique identifier of the accessed component instance.
   * @throws WebApplicationException exception if the validation failed.
   */
  @Override
  public void validateUserAuthorizationOnComponentInstance(final UserDetail user, String instanceId)
      throws WebApplicationException {
    if (user == null || !componentAccessController.isUserAuthorized(user.getId(), instanceId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Validates the authorization of the specified user to access the specified attachment.
   *
   * @param request the HTTP request from which the authentication of the caller can be done.
   * @param user the user for whom the authorization has to be validated.
   * @param doc the document accessed.
   * @throws WebApplicationException exception if the validation failed.
   */
  @Override
  public void validateUserAuthorizationOnAttachment(final HttpServletRequest request,
      final UserDetail user, SimpleDocument doc) throws WebApplicationException {
    AccessControlContext context = AccessControlContext.init();
    if (HttpMethod.PUT.equals(request.getMethod())) {
      context.onOperationsOf(AccessControlOperation.creation);
    } else if (HttpMethod.POST.equals(request.getMethod())) {
      context.onOperationsOf(AccessControlOperation.modification);
    } else if (HttpMethod.DELETE.equals(request.getMethod())) {
      context.onOperationsOf(AccessControlOperation.deletion);
    }
    if (!documentAccessController.isUserAuthorized(user.getId(), doc, context)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets the key of the session of the user calling this web service. The session key is first
   * retrieved from the HTTP header X-Silverpeas-Session. If no such parameter is set, the session
   * is then retrieved from the specified HTTP request. In the case the incoming request isn't sent
   * within an opened HTTP session, then an empty string is returned as no HTTP session was defined
   * for the current request.
   *
   * @return the user session key or an empty string if no HTTP session is active for the current
   * request.
   */
  private String getUserSessionKey(final HttpServletRequest request) {
    String sessionKey = request.getHeader(HTTP_SESSIONKEY);

    // if no session key is passed among the HTTP headers, check the request is within a session
    if (!isDefined(sessionKey)) {
      HttpSession httpSession = request.getSession(false);
      if (httpSession != null) {
        sessionKey = httpSession.getId();
      }
    }
    return sessionKey;
  }

  /**
   * Authenticates the user by using the credentials in the header Authorization of the specified
   * HTTP request.
   *
   * According to the HTTP specification, authentication credentials must be carried by the HTTP
   * header Authorization. Its value must follow the RFC 2617 basic digest and it must be Base 64
   * encoded.
   *
   * In Silverpeas, the authentication process with web services asks for the unique identifier of
   * the user as login instead of its true login text that can be not unique (it is unique only
   * within a given Silverpeas domain).
   *
   * Once the user well authenticated, return details about him. If the authentication fails, then a
   * WebApplicationException exception is thrown with an HTTP status code UNAUTHORIZED (401). The
   * implementation of this method is for taking into account the Silverpeas security doesn't
   * satisfy the JAAS way. Once JAAS supported in Silverpeas, the web services should use the
   * SecurityContext instead of the credentials token passed in the header of HTTP requests.
   *
   * @return the detail about the authenticated user requested this web service.
   */
  private SessionInfo authenticateUser(final HttpServletRequest request) {
    SessionInfo session = SessionInfo.NoneSession;
    String userCredentials = request.getHeader(HTTP_AUTHORIZATION);
    if (isDefined(userCredentials)) {
      String[] auth = userCredentials.split(" ");
      if (auth.length == 2 && auth[0].equalsIgnoreCase("Basic")) {
        String decoded = new String(Base64.decodeBase64(auth[1]), Charsets.UTF_8);
        // the first ':' character is the separator according to the RFC 2617 in basic digest
        int loginPasswordSeparatorIndex = decoded.indexOf(':');
        if (loginPasswordSeparatorIndex > 0) {
          String[] login = decoded.substring(0, loginPasswordSeparatorIndex).split("@domain");
          String password = decoded.substring(loginPasswordSeparatorIndex + 1);
          if (login.length == 2 && isDefined(login[0]) && isInteger(login[1])) {
            AuthenticationCredential credential =
                AuthenticationCredential.newWithAsLogin(login[0])
                    .withAsPassword(password)
                    .withAsDomainId(login[1]);
            AuthenticationService authenticator = AuthenticationServiceProvider.getService();
            String key = authenticator.authenticate(credential);
            if (!authenticator.isInError(key)) {
              HttpSession httpSession = request.getSession(true);
              try {
                String userId = Administration.get().identify(key, httpSession.getId(), false);
                session = sessionManagement.openSession(UserDetail.getById(userId), request);
              } catch (AdminException e) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
              }
            }
          }
        }
      }
    }
    if (!session.isDefined()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return session;
  }

  /**
   * Validates the current user session with the specified session key. The session key is either an
   * identifier of an opened HTTP session or an identifier token associated to an existing user.
   *
   * This method checks first that the specified key identifies uniquely an opened session in
   * Silverpeas. In this case it validates this session matches the one within which the current
   * HTTP request was sent. If the validation succeeds, the HTTP session is returned. If the key
   * doesn't identify any opened session, the method validates it matchs the identifier token of a
   * user in Silverpeas and in this case a session is then created only for the current request and
   * is returned. If the validation fails, a WebApplicationException exception is thrown.
   *
   * As the anonymous user has no opened session, when a request is recieved by this web service and
   * that request does neither belong to an opened session nor carries autentication information, it
   * is accepted only if the anonymous access is activated; in this case, an anonymous session is
   * created for the circumstance.
   *
   * @param context the context of the validation that contains at least the session key
   * @return the session identified by the specified session key. It can be either an opened HTTP
   * session or a session spawned only for the current request.
   */
  private SessionInfo validateUserSession(SessionValidationContext context) {
    String sessionKey = context.getSessionKey();
    SessionInfo sessionInfo = sessionManagement.validateSession(context);
    if (!sessionInfo.isDefined()) {
      // the key isn't a session identifier; is it then a user auth token?
      UserDetail user = null;
      final PersistentResourceToken userToken = PersistentResourceToken.getToken(sessionKey);
      UserSessionReference userSessionRef = userToken.getResource(UserSessionReference.class);
      if (userSessionRef != null) {
        user = userSessionRef.getEntity().getUser();
        if (!user.isValidState()) {
          user = null;
        }
      } else {
        // the key isn't a user auth token; is it then a user token?
        UserReference userRef = userToken.getResource(UserReference.class);
        if (userRef != null) {
          user = userRef.getEntity();
        }
      }
      if (user != null) {
        // the session key is a token and this token is bound to an existing user
        sessionInfo = new SessionInfo(sessionKey, user);
      } else {
        // the session key isn't a token or it isn't bound to an existing user
        if (!UserDetail.isAnonymousUserExist()) {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        sessionInfo = new SessionInfo(null, UserDetail.getAnonymousUser());
      }
    }
    return sessionInfo;
  }
}

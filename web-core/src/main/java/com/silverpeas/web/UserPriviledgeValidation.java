/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package com.silverpeas.web;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;

import org.silverpeas.attachment.model.SimpleDocument;

import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * It is a decorator of a REST-based web service that provides access to the validation of the
 * authentification and of the authorization for a caller to request the decorated web service.
 *
 * Indeed, the validation mechanisme is encapsulated within the RESTWebService as it requires access
 * to the incoming HTTP request as well to the current user session if any. In order to delegate
 * externally the validation triggering,
 */
@Named
public class UserPriviledgeValidation {

  @Inject
  @Named("sessionManager")
  private SessionManagement sessionManagement;
  @Inject
  @Named("componentAccessController")
  private AccessController<String> componentAccessController;
  @Inject
  @Named("simpleDocumentAccessController")
  private AccessController<SimpleDocument> documentAccessController;
  /**
   * The HTTP header paremeter in an incoming request that carries the user session key. This
   * parameter isn't mandatory as the session key can be found from an active HTTP session. If
   * neither HTTP session nor session key is available for the incoming request, user credentials
   * must be passed in the standard HTTP header parameter Authorization.
   */
  public static final String HTTP_SESSIONKEY = "X-Silverpeas-Session";
  /**
   * The standard HTTP header parameter in an incoming request that carries user credentials
   * information in order to open an authorized connexion with the web service that backs the
   * refered resource. This parameter must be used when requests aren't sent through an opened HTTP
   * session. It should be the prefered way for a REST client to access resources in Silverpeas as
   * it offers better scalability.
   */
  public static final String HTTP_AUTHORIZATION = "Authorization";

  /**
   * Validates the authentication of the user at the origin of a web request.
   *
   * The validation checks first the user is already authenticated and in that case its
   * authenticated session is always valid. Otherwise it attempt to authenticate the user by using
   * its credentials passed through the request (as an HTTP header). Once the authentication
   * succeed, the identification of the user is done and detail about it can then be got. A runtime
   * exception is thrown with an HTTP status code UNAUTHORIZED (401) at validation failure. The
   * validation fails when one of the belowed situation is occuring: <ul> <li>The user session key
   * is invalid;</li> <li>The user isn't authenticated and no credentials are passed with the
   * request;</li> <li>The user authentication failed.</li> </ul>
   *
   * @param request the HTTP request from which the authentication of the caller can be done.
   * @return details on the user at the origin of the specified request.
   * @throws WebApplicationException exception if the validation failed.
   */
  public UserDetail validateUserAuthentication(final HttpServletRequest request) throws
      WebApplicationException {
    UserDetail authenticatedUser;
    String sessionId = getUserSessionKey(request);
    if (isDefined(sessionId)) {
      authenticatedUser = validateUserSession(sessionId);
    } else {
      authenticatedUser = authenticateUser(request);
    }
    return authenticatedUser;
  }

  /**
   * Validates the authorization of the specified user to access the component instance with the
   * specified unique identifier.
   *
   * @param user the user for whom the authorization has to be validated.
   * @param instanceId the unique identifier of the accessed component instance.
   * @throws WebApplicationException exception if the validation failed.
   */
  public void validateUserAuthorizationOnComponentInstance(final UserDetail user, String instanceId)
      throws WebApplicationException {
    if (!componentAccessController.isUserAuthorized(user.getId(), instanceId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Validates the authorization of the specified user to access the specified attachment.
   *
   * @param user the user for whom the authorization has to be validated.
   * @param doc the document accessed.
   * @throws WebApplicationException exception if the validation failed.
   */
  public void validateUserAuthorizationOnAttachment(final UserDetail user, SimpleDocument doc)
      throws WebApplicationException {
    if (!documentAccessController.isUserAuthorized(user.getId(), doc)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets the key of the session of the user calling this web service. The session key is first
   * retrieved from the HTTP header parameter X-Silverpeas-Session. If no such parameter is set, it
   * is then retrieved from the current HTTP session if any. If the incoming request isn't sent
   * within an active HTTP session, then an empty string is returned as no HTTP session was defined
   * for the current request.
   *
   * @return the user session key or an empty string if no HTTP session is active for the current
   * request.
   */
  private String getUserSessionKey(final HttpServletRequest request) {
    String sessionId = request.getHeader(HTTP_SESSIONKEY);
    if (!isDefined(sessionId)) {
      HttpSession httpSession = request.getSession();
      if (httpSession != null) {
        sessionId = httpSession.getId();
      }
    }
    return sessionId;
  }

  /**
   * Authenticates the user by using the specified credentials. Once the user well authenticated,
   * return details about him. If the authentication fails, then a WebApplicationException exception
   * is thrown with an HTTP status code UNAUTHORIZED (401). The implementation of this method is for
   * taking into account the Silverpeas security doesn't satisfy the JAAS way. Once JAAS supported
   * in Silverpeas, the web services should use the SecurityContext instead of the credentials token
   * passed in the header of HTTP requests.
   *
   * @return the detail about the authenticated user requested this web service.
   */
  private UserDetail authenticateUser(final HttpServletRequest request) {
    String userCredentials = request.getHeader(HTTP_AUTHORIZATION);
    if (isDefined(userCredentials)) {
      String decoded = new String(Base64.decodeBase64(userCredentials));
      // the first ':' character is the separator according to the RFC 2617 in basic digest
      int loginPasswordSeparatorIndex = decoded.indexOf(":");
      String userId = decoded.substring(0, loginPasswordSeparatorIndex);
      String password = decoded.substring(loginPasswordSeparatorIndex + 1);
      AdminController adminController = new AdminController(null);
      UserFull user = adminController.getUserFull(userId);
      if (user == null || !user.getPassword().equals(password)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      return user;
    } else {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
  }

  /**
   * Validates the current user session with the specified session key. If the incoming request is
   * within an opened HTTP session (not so stateless, should be avoided in RESTful REST web services
   * for both scalability and REST policy reasons), then take checks this session is valide before
   * processing the request. If the session is valid, then detail about the user is returned,
   * otherwise a WebApplicationException exception is thrown. As the anonymous user has no opened
   * session, when a request is recieved by this web service and that request does neither belong to
   * an opened session nor carries autentication information, it is accepted only if the anonymous
   * access is authorized; in that case, the request is attached to the anonymous user account.
   *
   * @param sessionKey the user session key.
   * @return the detail about the user requesting this web service.
   */
  private UserDetail validateUserSession(String sessionKey) {
    SessionInfo sessionInfo = sessionManagement.getSessionInfo(sessionKey);
    if (sessionInfo == null) {
      if (!UserDetail.isAnonymousUserExist()) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      return UserDetail.getAnonymousUser();
    }
    return sessionInfo.getUserDetail();
  }
}

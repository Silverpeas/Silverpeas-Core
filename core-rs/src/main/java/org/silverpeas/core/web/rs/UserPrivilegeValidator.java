/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licence
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package org.silverpeas.core.web.rs;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.security.authorization.SimpleDocumentAccessControl;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionValidationContext;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * It is a decorator of a REST-based web service that provides access to the validation of the
 * authentication and of the authorization for a caller to request the decorated web service.
 * <p>
 * Indeed, the validation mechanism is encapsulated within the RESTWebService as it requires access
 * to the incoming HTTP request as well to the current user session if any. In order to delegate
 * externally the validation triggering,
 * </p>
 */
@Service
public class UserPrivilegeValidator implements UserPrivilegeValidation {

  @Inject
  private SessionManagement sessionManager;

  @Inject
  private ComponentAccessControl componentAccessController;

  @Inject
  private SimpleDocumentAccessControl documentAccessController;

  @Inject
  private PublicationAccessControl publicationAccessController;

  @Inject
  private OrganizationController organizationController;

  @Inject
  private HTTPAuthentication authentication;

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
   * <p>
   * The validation checks first the user is already authenticated, then it has a valid opened
   * session in Silverpeas. Otherwise, it attempts to open a new session for the user by using its
   * credentials passed through the request (as an HTTP header). Once the authentication succeed,
   * the identification of the user is done and detail about it can then be got. His session key is
   * then passed in the header of the HTTP response. A runtime exception is thrown with an HTTP
   * status code UNAUTHORIZED (401) at validation failure. The validation fails when one of the
   * below situation is occurring:
   * </p>
   * <ul>
   *   <li>The user session key is invalid;</li>
   *   <li>The user isn't authenticated and no credentials are passed within the request;</li>
   *   <li>The user authentication failed.</li>
   * </ul>
   *
   * @param request the HTTP request from which the authentication of the caller can be done.
   * @return the opened session of the user at the origin of the specified request.
   * @throws WebApplicationException exception if the validation failed.
   */
  @Override
  public SessionInfo validateUserAuthentication(final HttpServletRequest request,
      final HttpServletResponse response) {
    SessionInfo userSession = SessionInfo.NoneSession;
    String sessionKey = getUserSessionKey(request);
    if (StringUtil.isDefined(sessionKey)) {
      SessionValidationContext sessionValidationContext =
          SessionValidationContext.withSessionKey(sessionKey);
      if (mustSkipLastUserAccessTimeRegistering(request)) {
        sessionValidationContext.skipLastUserAccessTimeRegistering();
      }
      userSession = validateUserSession(sessionValidationContext);
      response.setHeader(HTTP_SESSIONKEY, userSession.getSessionId());
    }
    if(!userSession.isDefined()) {
      userSession = authentication.authenticate(
          new HTTPAuthentication.AuthenticationContext(request, response));
    }

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
   * Validates the authorization of the specified user to access the component instance with the
   * specified unique identifier. If no such component instance exists then a
   * {@link WebApplicationException} is thrown with the Not Found HTTP status code (404). If the
   * user isn't authorized to access the component instance, a {@link WebApplicationException} is
   * thrown with the Forbidden HTTP status code (403).
   *
   * @param user the user for whom the authorization has to be validated.
   * @param instanceId the unique identifier of the accessed component instance.
   * @throws WebApplicationException exception either if the component instance isn't found or
   * if the validation failed.
   */
  @Override
  public void validateUserAuthorizationOnComponentInstance(final User user, String instanceId) {
    SilverpeasComponentInstance instance = organizationController.getComponentInstance(instanceId)
        .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    if (user == null ||
        !componentAccessController.isUserAuthorized(user.getId(), instance.getId())) {
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
      final User user, SimpleDocument doc) {
    AccessControlContext context = AccessControlContext.init();
    if (HttpMethod.PUT.equals(request.getMethod())) {
      context.onOperationsOf(AccessControlOperation.CREATION);
    } else if (HttpMethod.POST.equals(request.getMethod())) {
      context.onOperationsOf(AccessControlOperation.MODIFICATION);
    } else if (HttpMethod.DELETE.equals(request.getMethod())) {
      context.onOperationsOf(AccessControlOperation.DELETION);
    }
    if (!documentAccessController.isUserAuthorized(user.getId(), doc, context)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Validates the authorization of the specified user to access the specified publication.
   *
   * @param request the HTTP request from which the authentication of the caller can be done.
   * @param user the user for whom the authorization has to be validated.
   * @param publication the publication accessed.
   * @throws WebApplicationException exception if the validation failed.
   */
  @Override
  public void validateUserAuthorizationOnPublication(final HttpServletRequest request,
      final User user, PublicationDetail publication) {
    AccessControlContext context = AccessControlContext.init();
    if (!publicationAccessController.isUserAuthorized(user.getId(), publication, context)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets the key of the session of the user calling this web service. The session key is first
   * retrieved from the HTTP header X-Silverpeas-Session then from the HTTP request parameter.
   * If no such parameter is set, the session is then retrieved from the specified HTTP request.
   * In the case the incoming request isn't sent within an opened HTTP session, then an empty
   * string is returned as no HTTP session was defined for the current request.
   *
   * @return the user session key or an empty string if no HTTP session is active for the current
   * request.
   */
  private String getUserSessionKey(final HttpServletRequest request) {
    String sessionKey = request.getHeader(HTTP_SESSIONKEY);
    if (StringUtil.isNotDefined(sessionKey)) {
      sessionKey = request.getParameter(HTTP_SESSIONKEY);
    }

    // if no session key is passed among the HTTP headers, check the request is within a session
    if (!StringUtil.isDefined(sessionKey)) {
      HttpSession httpSession = request.getSession(false);
      if (httpSession != null) {
        sessionKey = httpSession.getId();
      }
    }
    return sessionKey;
  }

  /**
   * Validates the current user session with the specified session key. The session key is either an
   * identifier of an opened HTTP session or a token associated with a virtual existing user session.
   * <p>
   * This method checks first that the specified key identifies uniquely an opened session in
   * Silverpeas. In this case it validates this session matches the one within which the current
   * HTTP request was sent. If the validation succeeds, the HTTP session is returned. If the key
   * doesn't identify any opened session, the method validates it matches the token of an existing
   * user session and in this case a session is then created only for the current request and it
   * is returned. If the validation fails, a WebApplicationException exception is thrown.
   * </p>
   * <p>
   * As the anonymous user has no opened session, when a request is received by this web service and
   * that request does neither belong to an opened session nor carries authentication information,
   * it is accepted only if the anonymous access is activated; in this case, an anonymous session is
   * created for the circumstance.
   * </p>
   * @param context the context of the validation that contains at least the session key
   * @return the session identified by the specified session key. It can be either an opened HTTP
   * session or a session spawned only for the current request.
   */
  private SessionInfo validateUserSession(SessionValidationContext context) {
    return sessionManager.validateSession(context);
  }
}

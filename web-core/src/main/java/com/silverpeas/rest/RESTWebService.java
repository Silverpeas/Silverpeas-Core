/*
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
package com.silverpeas.rest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;

/**
 * The class of the Silverpeas REST web services. It provides all of the common features required by
 * the web services in Silverpeas like the user priviledge checking.
 */
public abstract class RESTWebService {

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
  @Inject
  @Named("sessionManager")
  private SessionManagement sessionManager;
  @Inject
  @Named("componentAccessController")
  private AccessController<String> accessController;
  @Inject
  private OrganizationController organizationController;
  @Context
  private UriInfo uriInfo;
  @Context
  private HttpServletRequest httpRequest;
  @DefaultValue("")
  @HeaderParam(HTTP_AUTHORIZATION)
  private String credentials;
  @DefaultValue("")
  @HeaderParam(HTTP_SESSIONKEY)
  private String sessionKey;
  private UserDetail userDetail = null;

  /**
   * Sets a specific access controller other than the default one in Silverpeas. At Silverpeas
   * bootstrapping, an instance of access controller is created and injected as dependency in each
   * new REST web service instance (for doing, the web service must be managed by an IoC container).
   * This method is mainly for testing purpose.
   *
   * @param accessController the access controller to set.
   */
  protected void setAccessController(final AccessController<String> accessController) {
    this.accessController = accessController;
  }

  /**
   * Sets a specific session management service other than the default one in Silverpeas. At
   * Silverpeas bootstrapping, a service for session management is created and injected as
   * dependency in each new REST web service instance (for doing, the web service must be managed by
   * an IoC container). This method is mainly for testing purpose.
   *
   * @param sessionManager the session manager to set.
   */
  protected void setSessionManager(final SessionManagement sessionManager) {
    this.sessionManager = sessionManager;
  }

  /**
   * Gets the identifier of the component instance to which the requested resource belongs to.
   *
   * @return the identifier of the Silverpeas component instance.
   */
  abstract protected String getComponentId();

  /**
   * Gets information about the URI with which this web service was invoked.
   *
   * @return an UriInfo instance.
   */
  protected UriInfo getUriInfo() {
    return uriInfo;
  }

  /**
   * Gets the HTTP servlet context mapped with this web service.
   *
   * @return the HTTP servlet context.
   */
  protected HttpServletRequest getHttpServletContext() {
    return httpRequest;
  }

  /**
   * Gets the detail about the user that has called this web service. If the user isn't already
   * identified by this web service, then an identification is performed before through an
   * authentication operation followed by an authorization validation. If the identification or the
   * authorization fails, then a WebApplicationException is thrown with respectively a HTTP status
   * code UNAUTHORIZED (401) or FORBIDEN (403).
   *
   * @return the detail about the user.
   */
  protected UserDetail getUserDetail() {
    if (userDetail == null) {
      checkUserPriviledges();
    }
    return userDetail;
  }

  /**
   * Gets the preference of the user that requested the resource backed by this web service. If the
   * user isn't already identified by this web service, then an identification is performed before
   * through an authentication operation followed by an authorization validation. If the
   * identification or the authorization fails, then a WebApplicationException is thrown with
   * respectively a HTTP status code UNAUTHORIZED (401) or FORBIDEN (403). If the preferences can be
   * retrieved, then null is returned.
   *
   * @return the user preference or null if its preferences can be retrieved.
   */
  protected UserPreferences getUserPreferences() {
    return SilverpeasServiceProvider.getPersonalizationService().getUserSettings(
        getUserDetail().getId());
  }

  /**
   * Checks the user that requested this web service has the correct priviledges to access the
   * underlying referenced resource that belongs to a Silverpeas component instance. For doing, the
   * resource should exists, otherwise a WebApplicationException is thrown with a status code NOT
   * FOUND (404).
   * 
   * This method should be called at each incoming request processing to ensure a strong security. User
   * information is retreived from the context of the incoming HTTP request. If no user information
   * can be retrieved, then a WebApplicationException is thrown with a status code UNAUTHORIZED
   * (401). When the check fails, a WebApplicationException is thrown with the HTTP status code set
   * according to the failure.
   */
  protected void checkUserPriviledges() {
    checkComponentInstanceExistance(getComponentId());
    checkUserAuthentication();
    checkUserAuthorizationOnComponent(getComponentId());
  }
  
  /**
   * Checks the component instance requested in the URI exists in Silverpeas.
   * If no such component exists, then a WebApplicationException is thrown with a HTTP status code
   * NOT FOUND (404).
   * @param componentId the unique identifier of the component instance.
   */
  private void checkComponentInstanceExistance(String componentId) {
    if (!getOrganizationController().isComponentExist(componentId)) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  /**
   * Checks the user that requested the resource backed by this web service  is authorized to access
   * the specified Silverpeas component instance. If the user isn't authorized to access the
   * specified component, then a WebApplicationException exception is thrown with a HTTP status code
   * FORBIDDEN (403).
   *
   * @param componentId the identifier of the Silverpeas component instance.
   */
  private void checkUserAuthorizationOnComponent(String componentId) {
    if (!getAccessController().isUserAuthorized(getUserDetail().getId(), componentId)) {
      throw new WebApplicationException(Status.FORBIDDEN);
    }
  }

  /**
   * Checks the user that requested the resource backed by this web service is well authenticated.
   * Once the authentication succeed, the identification of the user is done and detail about it can
   * then be got by a getUserDetail() method call.
   * <p/>
   * The authentication checking is performed by following steps: <ul> <li>checks if an active HTTP
   * session exists for the user,</li> <li>if no active HTTP session is available for the user,
   * authenticates him with the credentials passed in the header of the HTTP request,</li> <li>if
   * neither credentials are available nor the credentials are valid nor the session key is valid, a
   * WebApplicationException exception is thrown with as HTTP status code UNAUTHORIZED (401).</li>
   * </ul>
   */
  private void checkUserAuthentication() {
    String sessionId = getUserSessionKey();
    if (sessionId.isEmpty()) {
      this.userDetail = authenticateUser(credentials);
    } else {
      this.userDetail = validateUserSession(sessionId);
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
   *         request.
   */
  private String getUserSessionKey() {
    String sessionId = sessionKey;
    if (sessionId.isEmpty()) {
      HttpSession httpSession = httpRequest.getSession();
      if (httpSession != null) {
        sessionId = httpSession.getId();
      }
    }
    return sessionId;
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
    SessionInfo sessionInfo = getSessionManagement().getSessionInfo(sessionKey);
    if (sessionInfo == null) {
      if (!UserDetail.isAnonymousUserExist()) {
        throw new WebApplicationException(Status.UNAUTHORIZED);
      }
      return UserDetail.getAnonymousUser();
    }
    return sessionInfo.getUserDetail();
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
  private UserDetail authenticateUser(String userCredentials) {
    String decoded = new String(Base64.decodeBase64(userCredentials));
    // the first ':' character is the separator according to the RFC 2617 in basic digest
    int loginPasswordSeparatorIndex = decoded.indexOf(":");
    String userId = decoded.substring(0, loginPasswordSeparatorIndex);
    String password = decoded.substring(loginPasswordSeparatorIndex + 1);
    AdminController adminController = new AdminController(null);
    UserFull user = adminController.getUserFull(userId);
    if (user == null || !user.getPassword().equals(password)) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }
    return adminController.getUserDetail(userId);
  }

  /**
   * Gets the controller of user access on the silverpeas resources.
   *
   * @return the user access controller.
   */
  private AccessController<String> getAccessController() {
    return accessController;
  }
  
  /**
   * Gets the organization controller.
   * @return an OrganizationController instance.
   */
  protected OrganizationController getOrganizationController() {
    return organizationController;
  }

  /**
   * Gets the session manager to use to control the user authentication.
   *
   * @return the user session manager.
   */
  private SessionManagement getSessionManagement() {
    return sessionManager;
  }
}

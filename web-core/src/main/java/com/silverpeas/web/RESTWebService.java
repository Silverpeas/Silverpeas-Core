/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.web;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.session.SessionInfo;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static com.silverpeas.web.UserPriviledgeValidation.HTTP_AUTHORIZATION;
import static com.silverpeas.web.UserPriviledgeValidation.HTTP_SESSIONKEY;

/**
 * The class of the Silverpeas REST web services. It provides all of the common features required by
 * the web services in Silverpeas like the user priviledge checking.
 */
public abstract class RESTWebService {

  @Inject
  private OrganizationController organizationController;
  @Context
  private UriInfo uriInfo;
  @Context
  private HttpServletRequest httpRequest;
  @Context
  private HttpServletResponse httpResponse;
  private UserDetail userDetail = null;
  private Collection<SilverpeasRole> userRoles = null;

  /**
   * Gets the identifier of the component instance to which the requested resource belongs to.
   *
   * @return the identifier of the Silverpeas component instance.
   */
  abstract public String getComponentId();

  /**
   * Validates the authentication of the user requesting this web service. If no session was opened
   * for the user, then open a new one. The validation is actually delegated to the validation
   * service by passing it the required information.
   *
   * This method should be invoked for web service requiring an authenticated user. Otherwise, the
   * annotation Authenticated can be also used instead at class level.
   *
   * @see UserPriviledgeValidation
   * @param validation the validation instance to use.
   * @throws WebApplicationException if the authentication isn't valid (no authentication and
   * authentication failure).
   */
  public void validateUserAuthentication(final UserPriviledgeValidation validation) throws
          WebApplicationException {
    HttpServletRequest request = getHttpServletRequest();
    SessionInfo session = validation.validateUserAuthentication(request);
    if (request.getHeader(HTTP_SESSIONKEY) != null || (request.getHeader(HTTP_AUTHORIZATION) != null
            && session.getLastAccessTimestamp() == session.getOpeningTimestamp())) {
      getHttpServletResponse().setHeader(HTTP_SESSIONKEY, session.getSessionId());
    }
    this.userDetail = session.getUserDetail();
  }

  /**
   * Validates the authorization of the user to request this web service. For doing, the user must
   * have the rights to access the component instance that manages this web resource. The validation
   * is actually delegated to the validation service by passing it the required information.
   *
   * This method should be invoked for web service requiring an authorized access. For doing, the
   * authentication of the user must be first valdiated. Otherwise, the annotation Authorized can be
   * also used instead at class level for both authentication and authorization.
   *
   * @see UserPriviledgeValidation
   * @param validation the validation instance to use.
   * @throws WebApplicationException if the rights of the user are not enough to access this web
   * resource.
   */
  public void validateUserAuthorization(final UserPriviledgeValidation validation) throws
          WebApplicationException {
    validation.validateUserAuthorizationOnComponentInstance(getUserDetail(), getComponentId());
  }

  /**
   * Gets information about the URI with which this web service was invoked.
   *
   * @return an UriInfo instance.
   */
  public UriInfo getUriInfo() {
    return uriInfo;
  }

  /**
   * Gets the HTTP servlet request mapped with the execution context of this web service.
   *
   * @return the HTTP servlet request.
   */
  public HttpServletRequest getHttpServletRequest() {
    return httpRequest;
  }

  /**
   * Gets the HTTP servlet response mapped with the execution context of this web service.
   *
   * @return the HTTP servlet response.
   */
  public HttpServletResponse getHttpServletResponse() {
    return httpResponse;
  }

  /**
   * Gets the detail about the user that has called this web service. If the user isn't already
   * identified by this web service, then null is returned.
   *
   * @return the detail about the user.
   */
  protected UserDetail getUserDetail() {
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
   * Gets roles of the authenticated user.
   * @return
   */
  protected Collection<SilverpeasRole> getUserRoles() {
    if (userRoles == null) {
      userRoles = SilverpeasRole
          .from(organizationController.getUserProfiles(getUserDetail().getId(), getComponentId()));
    }
    return userRoles;
  }

  /**
   * Gets the organization controller.
   *
   * @return an OrganizationController instance.
   */
  protected OrganizationController getOrganizationController() {
    return organizationController;
  }
}

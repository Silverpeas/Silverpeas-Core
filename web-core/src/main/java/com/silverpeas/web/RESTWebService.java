/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
import org.silverpeas.util.StringUtil;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.notification.message.MessageManager;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.settings.SilverpeasSettings;
import org.silverpeas.token.Token;
import org.silverpeas.web.token.SynchronizerTokenService;
import org.silverpeas.web.token.SynchronizerTokenServiceFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static com.silverpeas.web.UserPriviledgeValidation.HTTP_AUTHORIZATION;
import static com.silverpeas.web.UserPriviledgeValidation.HTTP_SESSIONKEY;

/**
 * The class of the Silverpeas REST web services. It provides all of the common features required by
 * the web services in Silverpeas like the user priviledge checking.
 */
public abstract class RESTWebService {
  public static final String REST_WEB_SERVICES_URI_BASE =
      SilverpeasSettings.getRestWebServicesUriBase();

  /**
   * The HTTP header parameter that provides the real size of an array of resources. It is for
   * client side when a pagination mechanism is used in order to calculate the number of pages.
   */
  public static final String RESPONSE_HEADER_ARRAYSIZE = "X-Silverpeas-Size";
  @Inject
  private OrganisationController organizationController;
  @Context
  private UriInfo uriInfo;
  @Context
  private HttpServletRequest httpServletRequest;
  private HttpRequest httpRequest;
  @Context
  private HttpServletResponse httpResponse;
  private UserDetail userDetail = null;
  private Collection<SilverpeasRole> userRoles = null;
  private SilverpeasRole greaterUserRole;

  private ResourceLocator bundle = null;

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
    // Sent back the identifier of the spawned session in the HTTP response
    if (StringUtil.isDefined(session.getSessionId()) && session.getLastAccessTimestamp() == session.
        getOpeningTimestamp()) {
      getHttpServletResponse().setHeader(HTTP_SESSIONKEY, session.getSessionId());
      if (request.getHeader(HTTP_AUTHORIZATION) != null
          && session.getLastAccessTimestamp() == session.getOpeningTimestamp()) {
        SynchronizerTokenService tokenService = SynchronizerTokenServiceFactory.
            getSynchronizerTokenService();
        tokenService.setUpSessionTokens(session);
        Token token = tokenService.getSessionToken(session);
        getHttpServletResponse().addHeader(SynchronizerTokenService.SESSION_TOKEN_KEY, token.
            getValue());
      }
    }
    this.userDetail = session.getUserDetail();
    if (this.userDetail != null) {
      MessageManager.setLanguage(this.userDetail.getUserPreferences().getLanguage());
    }
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
    return httpServletRequest;
  }

  /**
   * Gets the HTTP request mapped with the execution context of this web service.
   * @return the HTTP request.
   */
  public HttpRequest getHttpRequest() {
    if (httpRequest == null) {
      httpRequest = (HttpRequest) getHttpServletRequest().getAttribute(HttpRequest.class.getName());
      if (httpRequest == null) {
        httpRequest = HttpRequest.decorate(getHttpServletRequest());
      }
    }
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
   *
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
  protected OrganisationController getOrganisationController() {
    return organizationController;
  }

  /**
   * Gets the location of the bundle to use.
   *
   * @return
   */
  protected String getBundleLocation() {
    return null;
  }

  /**
   * Gets the bundle to use.
   *
   * @return
   */
  protected ResourceLocator getBundle() {
    if (bundle == null) {
      if (getBundleLocation() == null) {
        bundle = GeneralPropertiesManager.getGeneralMultilang(getUserPreferences().getLanguage());
      } else {
        bundle = new ResourceLocator(getBundleLocation(), getUserPreferences().getLanguage());
      }
    }
    return bundle;
  }

  /**
   * Gets the greater role of the user behind the service call.
   * @return
   */
  public SilverpeasRole getGreaterUserRole() {
    if (greaterUserRole == null) {
      greaterUserRole = SilverpeasRole.getGreaterFrom(getUserRoles());
    }
    return greaterUserRole;
  }

  /**
   * This method permits to start the setting of a {@link com.silverpeas.web.RESTWebService
   * .WebTreatment}.
   * @param webTreatment
   * @param <RETURN_VALUE>
   * @return
   */
  protected <RETURN_VALUE> WebProcess<RETURN_VALUE> process(
      WebTreatment<RETURN_VALUE> webTreatment) {
    return new WebProcess<RETURN_VALUE>(webTreatment);
  }

  /**
   * This class handles the execution of a {@link com.silverpeas.web.RESTWebService.WebTreatment}.
   * It provides the centralization of exception catches and handles the lowest role access that
   * must the user verify.
   * @param <RETURN_VALUE> the type of the value returned by the web treatment.
   * @return the value computed by the specified web treatment.
   */
  protected final class WebProcess<RETURN_VALUE> {
    private final WebTreatment<RETURN_VALUE> webTreatment;
    private SilverpeasRole lowestRoleAccess = null;

    /**
     * Default constructor.
     * @param webTreatment
     */
    protected WebProcess(final WebTreatment<RETURN_VALUE> webTreatment) {
      this.webTreatment = webTreatment;
    }

    /**
     * Sets the lowest role access that the user behind the service call must verify.
     * @param lowestRoleAccess
     * @return
     */
    public WebProcess<RETURN_VALUE> lowestAccessRole(SilverpeasRole lowestRoleAccess) {
      this.lowestRoleAccess = lowestRoleAccess;
      return this;
    }

    /**
     * This method calls the execute method of a {@link com.silverpeas.web.RESTWebService
     * .WebTreatment} instance.
     * One of the aim of this mechanism is to centralize the exception catching and also to avoid
     * redundant coding around web exceptions.
     * If a lowest role access is defined, the user must verify it.
     * If the user isn't authentified, a 401 HTTP code is returned.
     * If a problem occurs when processing the request, a 503 HTTP code is returned.
     * @return the value computed by the specified web treatment.
     */
    public RETURN_VALUE execute() {
      try {
        if (lowestRoleAccess != null && (getGreaterUserRole() == null ||
            !getGreaterUserRole().isGreaterThanOrEquals(lowestRoleAccess))) {
          throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return webTreatment.execute();
      } catch (final WebApplicationException ex) {
        throw ex;
      } catch (final Exception ex) {
        throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
      }
    }
  }

  /**
   * Inner class handled by
   * @param <RETURN_VALUE>
   */
  protected abstract class WebTreatment<RETURN_VALUE> {
    public abstract RETURN_VALUE execute();
  }
}

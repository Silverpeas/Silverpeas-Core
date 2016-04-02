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
package org.silverpeas.core.webapi.base;

import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.token.SynchronizerTokenService;
import org.silverpeas.core.webapi.base.aspect.ComponentInstMustExistIfSpecified;
import org.silverpeas.core.webapi.base.aspect.WebEntityMustBeValid;
import org.silverpeas.core.util.SilverpeasSettings;
import org.silverpeas.core.security.token.Token;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static org.silverpeas.core.webapi.base.UserPrivilegeValidation.HTTP_AUTHORIZATION;
import static org.silverpeas.core.webapi.base.UserPrivilegeValidation.HTTP_SESSIONKEY;

/**
 * The class of the Silverpeas REST web services. It provides all of the common features required by
 * the web services in Silverpeas like the user priviledge checking.
 */
@ComponentInstMustExistIfSpecified
@WebEntityMustBeValid
public abstract class RESTWebService implements WebResource {
  public static final String REST_WEB_SERVICES_URI_BASE =
      SilverpeasSettings.getRestWebServicesUriBase();

  /**
   * The HTTP header parameter that provides the real size of an array of resources. It is for
   * client side when a pagination mechanism is used in order to calculate the number of pages.
   */
  public static final String RESPONSE_HEADER_ARRAYSIZE = "X-Silverpeas-Size";
  @Inject
  private OrganizationController organizationController;
  @Inject
  private SynchronizerTokenService tokenService;
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

  private LocalizationBundle bundle = null;

  @Override
  public void validateUserAuthentication(final UserPrivilegeValidation validation) throws
      WebApplicationException {
    HttpServletRequest request = getHttpServletRequest();
    SessionInfo session = validation.validateUserAuthentication(request);
    // Sent back the identifier of the spawned session in the HTTP response
    if (StringUtil.isDefined(session.getSessionId()) && session.getLastAccessTimestamp() == session.
        getOpeningTimestamp()) {
      getHttpServletResponse().setHeader(HTTP_SESSIONKEY, session.getSessionId());
      getHttpServletResponse().addHeader("Access-Control-Expose-Headers",
          UserPrivilegeValidation.HTTP_SESSIONKEY);
      if (request.getHeader(HTTP_AUTHORIZATION) != null
          && session.getLastAccessTimestamp() == session.getOpeningTimestamp()) {
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

  @Override
  public void validateUserAuthorization(final UserPrivilegeValidation validation) throws
      WebApplicationException {
    validation.validateUserAuthorizationOnComponentInstance(getUserDetail(), getComponentId());
  }

  public abstract String getComponentId();

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
    return PersonalizationServiceProvider.getPersonalizationService().getUserSettings(
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
  protected OrganizationController getOrganisationController() {
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
  protected LocalizationBundle getBundle() {
    if (bundle == null) {
      if (getBundleLocation() == null) {
        bundle = ResourceLocator.getGeneralLocalizationBundle(getUserPreferences().getLanguage());
      } else {
        bundle = ResourceLocator.getLocalizationBundle(getBundleLocation(),
            getUserPreferences().getLanguage());
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
   * This method permits to start the setting of a {@link RESTWebService
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
   * This class handles the execution of a {@link RESTWebService.WebTreatment}.
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
     * This method calls the execute method of a {@link RESTWebService
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

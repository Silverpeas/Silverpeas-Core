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
package org.silverpeas.core.web.rs;

import org.silverpeas.core.NotFoundException;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.SilverpeasWebResource;
import org.silverpeas.core.web.WebResourceUri;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.rs.aspect.ComponentInstMustExistIfSpecified;
import org.silverpeas.core.web.rs.aspect.WebEntityMustBeValid;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * The class of the Silverpeas REST web services. It provides all the common features required by
 * the web services in Silverpeas like the user privilege checking.
 */
@ComponentInstMustExistIfSpecified
@WebEntityMustBeValid
public abstract class RESTWebService implements ProtectedWebResource {

  /**
   * The HTTP header parameter that provides the real size of an array of resources. It is for
   * client side when a pagination mechanism is used in order to calculate the number of pages.
   */
  public static final String RESPONSE_HEADER_ARRAYSIZE = "X-Silverpeas-Size";
  @Inject
  @Default
  private RESTRequestContext restRequestContext;
  @Inject
  private OrganizationController organizationController;
  @Context
  private UriInfo uriInfo;
  @Context
  private HttpServletRequest httpServletRequest;
  private HttpRequest httpRequest;
  @Context
  private HttpServletResponse httpResponse;
  private Collection<SilverpeasRole> userRoles = null;
  private SilverpeasRole highestUserRole;

  private LocalizationBundle bundle = null;

  private WebResourceUri webResourceUri;

  @PostConstruct
  protected void initContext() {
    restRequestContext.init(httpServletRequest, httpResponse);
  }

  @Override
  public RESTRequestContext getSilverpeasContext() {
    return restRequestContext;
  }

  @Override
  public WebResourceUri getUri() {
    if (webResourceUri == null) {
      webResourceUri = initWebResourceUri();
    }
    return webResourceUri;
  }

  /**
   * Default initialization of {@link WebResourceUri} instance for all WEB services extending
   * {@link RESTWebService} class.
   * <p>
   * This method can be overrated in case the default initialization is not satisfying a right
   * behavior.
   * </p>
   * <p>
   * In any case, the {@link WebResourceUri} is computed one time (and only one) per request and
   * the
   * result of computation is provided by {@link #getUri()} method.
   * </p>
   * @return a {@link WebResourceUri} instance.
   */
  protected WebResourceUri initWebResourceUri() {
    String path = getResourceBasePath();
    String componentId = getComponentId();
    if (!path.endsWith("/")) {
      path += "/";
    }
    if (StringUtil.isDefined(componentId)) {
      path += componentId;
    }
    return createWebResourceUri(path);
  }

  /**
   * Creates a {@link WebResourceUri} instance from the specified relative path of a web resource.
   * It is dedicated to be used by {@link RESTWebService#initWebResourceUri()} but it can be used
   * to create a custom {@link WebResourceUri} object to craft custom URIs.
   * @param webResourcePath the relative base path of a web resource.
   * @return a {@link WebResourceUri} instance.
   */
  protected WebResourceUri createWebResourceUri(final String webResourcePath) {
    return new WebResourceUri(webResourcePath, getHttpServletRequest(), uriInfo);
  }

  /**
   * Gets the base path of the web resource relative to the root path of all the web resources
   * in Silverpeas as given by {@link SilverpeasWebResource#getBasePath()}.
   * @return the relative path that identifies this REST web service among all other REST web
   * services.
   */
  protected abstract String getResourceBasePath();

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
  @Override
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
   * Is the user behind the request is well-defined in Silverpeas? In the case of an anonymous
   * or public request, the user isn't identified in Silverpeas and therefore this method returns
   * false.
   * @return true if the user behind the request is well identified, false otherwise.
   */
  protected boolean isUserDefined() {
    return getSilverpeasContext().getUser() != null;
  }

  /**
   * Gets the detail about the user that has called this web service. If the user isn't already
   * identified by this web service, then null is returned.
   *
   * @return the detail about the user.
   */
  protected User getUser() {
    return getSilverpeasContext().getUser();
  }

  /**
   * Gets the preference of the user that requested the resource backed by this web service. If the
   * user isn't already identified by this web service, then an identification is performed before
   * through an authentication operation followed by an authorization validation. If the
   * identification or the authorization fails, then a WebApplicationException is thrown with
   * respectively an HTTP status code UNAUTHORIZED (401) or FORBIDDEN (403). If the preferences can be
   * retrieved, then null is returned.
   *
   * @return the user preference or null if its preferences can be retrieved.
   */
  protected UserPreferences getUserPreferences() {
    return getUser().getUserPreferences();
  }

  /**
   * Gets roles of the authenticated user.
   *
   * @return a collection of roles played by the current authenticated and then identified user.
   */
  protected Collection<SilverpeasRole> getUserRoles() {
    if (userRoles == null) {
      userRoles =
          organizationController.getUserSilverpeasRolesOn(getUser(), getComponentId());
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
   * @return the classpath location of the localization bundle used by this Web service.
   */
  protected String getBundleLocation() {
    return null;
  }

  /**
   * Gets the bundle to use.
   *
   * @return the localization bundle to translate texts for the user.
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
   * Gets the highest role of the user behind the service call.
   * @return the highest role the current authenticated and then identified user can play for this
   * Web service.
   */
  public SilverpeasRole getHighestUserRole() {
    if (highestUserRole == null) {
      highestUserRole = SilverpeasRole.getHighestFrom(getUserRoles());
    }
    return highestUserRole;
  }

  /**
   * This method permits to start the setting of a {@link RESTWebService.WebTreatment}.
   * @param webTreatment a treatment to process in the behalf of a Web service.
   * @param <R> the concrete type of the type the treatment will return.
   * @return a process wrapping the treatment it will take in charge.
   */
  protected <R> WebProcess<R> process(
      WebTreatment<R> webTreatment) {
    WebProcess<R> process = new WebProcess<>(webTreatment);
    final String httpMethod = getHttpRequest().getMethod().toUpperCase();
    if (!"GET".equals(httpMethod)) {
      // case of side effect, processing can be done only under the correct lower role
      process.lowestAccessRole(SilverpeasRole.WRITER);
    }
    return process;
  }

  /**
   * This class handles the execution of a {@link RESTWebService.WebTreatment}.
   * It provides the centralization of exception catches and handles the lowest role access that
   * must the user verify.
   * @param <R> the type of the value returned by the web treatment.
   */
  protected final class WebProcess<R> {
    private final WebTreatment<R> webTreatment;
    private SilverpeasRole lowestRoleAccess = null;

    /**
     * Default constructor.
     * @param webTreatment a treatment to process.
     */
    WebProcess(final WebTreatment<R> webTreatment) {
      this.webTreatment = webTreatment;
    }

    /**
     * Sets the lowest role access that the user behind the service call must verify.
     * @param lowestRoleAccess the lowest role allowed to process a given web treatment.
     * @return itself.
     */
    public WebProcess<R> lowestAccessRole(SilverpeasRole lowestRoleAccess) {
      this.lowestRoleAccess = lowestRoleAccess;
      return this;
    }

    /**
     * This method calls the execute method of a {@link RESTWebService
     * .WebTreatment} instance.
     * One of the aim of this mechanism is to centralize the exception catching and also to avoid
     * redundant coding around web exceptions.
     * If the lowest role access is defined, the user must verify it.
     * If the user isn't authenticated, a 401 HTTP code is returned.
     * If a problem occurs when processing the request, a 503 HTTP code is returned.
     * @return the value computed by the specified web treatment.
     */
    public R execute() {
      try {
        if (lowestRoleAccess != null && (getHighestUserRole() == null ||
            !getHighestUserRole().isGreaterThanOrEquals(lowestRoleAccess))) {
          throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return webTreatment.execute();
      } catch (final NotFoundException ex) {
        throw new WebApplicationException(ex, Response.Status.NOT_FOUND);
      } catch (final ForbiddenRuntimeException ex) {
        throw new WebApplicationException(ex, Response.Status.FORBIDDEN);
      } catch (final WebApplicationException ex) {
        throw ex;
      } catch (final Exception ex) {
        throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
      }
    }
  }

  /**
   * Inner class handled by
   * @param <R>
   */
  @FunctionalInterface
  protected interface WebTreatment<R> {
    R execute();
  }

  /**
   * Convenient method to build a URI from the request's absolute path and the specified
   * identifiers. Each identifier will be added to the absolute path as a path node in the
   * returned URI.
   * @param id one or more identifiers identifying uniquely the current requested web resource.
   * @return a URI identifying uniquely in the Web the current requested resource.
   */
  protected URI identifiedBy(final String... id) {
    return identifiedBy(getUri().getAbsolutePathBuilder(), id);
  }

  /**
   * Convenient method to build a URI from the base URI represented by the specified
   * {@link UriBuilder} and from the specified identifiers. Each identifier will be added to the
   * base URI as a path node in the returned URI.
   * @param base a {@link UriBuilder} instance representing the base URI from which the resulted
   * URI will be computed.
   * @param id one or more identifiers identifying uniquely the current requested web resource.
   * @return a URI identifying uniquely in the Web the current requested resource.
   */
  protected URI identifiedBy(final UriBuilder base, final String... id) {
    Stream.of(id).forEach(base::path);
    return base.build();
  }

  /**
   * Computes the {@link PaginationPage} according to the given asked page data.
   * <p>
   * Page data is a String composed of two values separated by a semicolon :
   * <ul>
   *   <li>Left value represents the page number. First page is '1'.</li>
   *   <li>Right value represents the number of data per page.</li>
   * </ul>
   * </p>
   * @param page the page information.
   * @return the initialized {@link PaginationPage}.
   */
  protected PaginationPage fromPage(String page) {
    PaginationPage paginationPage = null;
    if (page != null && !page.isEmpty()) {
      String[] pageAttributes = page.split(";");
      try {
        int nth = Integer.parseInt(pageAttributes[0]);
        int count = Integer.parseInt(pageAttributes[1]);
        if (count > 0) {
          paginationPage = new PaginationPage(nth, count);
        }
      } catch (NumberFormatException ex) {
        SilverLogger.getLogger(this).warn(ex);
        paginationPage = PaginationPage.DEFAULT;
      }
    }
    return paginationPage;
  }
}

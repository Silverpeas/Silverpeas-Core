/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.web.test;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Rule;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.web.test.environment.SilverpeasTestEnvironment;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * The base class for testing REST web services in Silverpeas. This base class wraps all the
 * mechanisms required to prepare the environment for testing web services with RESTEasy and CDI in
 * the context of Silverpeas.
 */
public abstract class RESTWebServiceTest {

  /**
   * The HTTP header parameter in an incoming request that carries the user API token value as it is
   * defined in the Silverpeas REST web service API.
   */
  private static final String AUTH_HTTP_HEADER = "Authorization";

  /**
   * The HTTP header parameter in an incoming request that carries the user session key value as it
   * is defined in the Silverpeas REST web service API. the session key is obtained once the user is
   * authenticated amongst the REST-based Web API.
   */
  private static final String SESSION_KEY_HTTP_HEADER = "X-Silverpeas-Session";

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(
          "/org/silverpeas/web/test/environment/create-table-domain-user-group.sql",
          "/org/silverpeas/web/test/environment/create-table-space-component.sql",
          "/org/silverpeas/web/test/environment/create-table-profile.sql",
          "/org/silverpeas/web/test/environment/create-table-token.sql",
          "/org/silverpeas/web/test/environment/create-table-notification.sql", getTableCreationScript())
      .loadInitialDataSetFrom(getDataSetScript());

  @Before
  public void reloadAdminCaches() {
    Administration.get().reloadCache();
  }

  /**
   * Gets the SQL script file in the classpath that contains statements to create table(s) specific
   * to this test.
   * @return the path in the classpath of a SQL script file (or empty if no such SQL script).
   */
  protected String getTableCreationScript() {
    return StringUtil.EMPTY;
  }

  /**
   * Gets the SQL script file in the classpath that contains statements to prepare the database used
   * by this test with a data set.
   * @return the path in the classpath of a SQL script file (or empty if no such SQL script)
   */
  protected String getDataSetScript() {
    return StringUtil.EMPTY;
  }

  /**
   * Gets the component instances to take into account in tests. Theses component instances will be
   * considered as existing. Others than those will be rejected with an HTTP error 404 (NOT FOUND).
   * @return an array with the identifier of the component instances to take into account in tests.
   * The array cannot be null, but it can be empty.
   */
  public abstract String[] getExistingComponentInstances();

  /**
   * Gets tools to take into account in tests. These tools will be considered as existing. Others
   * than those will be rejected with an HTTP error 404 (NOT FOUND).
   * @return an array with the identifier of tools to take into account in tests. The array cannot
   * be null, but it can be empty.
   */
  @SuppressWarnings("unused")
  public String[] getExistingTools() {
    return new String[]{};
  }

  /**
   * Gets an initialized web target resource instance for tests.
   * @return the {@link WebTarget} instance.
   */
  public WebTarget resource() {
    return ClientBuilder.newClient()
        .target(getBaseURI() + "test-" + this.getClass().getSimpleName() + "/services/");
  }

  protected URI getBaseURI() {
    return URI.create("http://localhost:8080/");
  }

  /**
   * Gets a URI builder initialized with the base URI at which are exposed by default all the REST
   * web resources in Silverpeas. This is to be used for checking URIs in returned entities in the
   * tests.
   * @return an initialized {@link UriBuilder}
   */
  protected UriBuilder getWebResourceBaseURIBuilder() {
    return UriBuilder.fromUri(getBaseURI()).path("silverpeas").path("services");
  }

  public SilverpeasTestEnvironment getSilverpeasEnvironmentTest() {
    return SilverpeasTestEnvironment.get();
  }

  /**
   * <p>
   * Gets (and initializes if necessary) the token key of the given user.<br> The user must exist
   * into database (use {@link SilverpeasTestEnvironment#createDefaultUser()} to add a user, and use
   * {@link #getSilverpeasEnvironmentTest()} to get the silverpeas environment manager instance).
   * </p>
   * <p>
   * For example: <pre>getTokenKeyOf(get().createUser());</pre>
   * </p>
   * @param theUser the user to authenticate.
   * @return the key of the opened session.
   */
  public String getTokenKeyOf(final User theUser) {
    return getSilverpeasEnvironmentTest().getTokenOf(theUser);
  }

  /**
   * Denies the access to the silverpeas resources to all users. It sets no public both the dummy
   * component and all the existing component instances on which the test is working. All the user
   * roles are removed from all the component instances as well as from all of their parent spaces.
   */
  public void denyAuthorizationToUsers() {
    Stream.of(getExistingComponentInstances()).forEach(i -> {
      final OrganizationController organization = OrganizationController.get();
      final ComponentInst inst = organization.getComponentInst(i);
      if (inst != null) {
        if (inst.isPublic()) {
          inst.setPublic(false);
        }
        getSilverpeasEnvironmentTest().updateComponent(inst);
        getSilverpeasEnvironmentTest().removeAllProfiles(inst);

        String spaceId = inst.getSpaceId();
        SpaceInst space;
        do {
          space = organization.getSpaceInstById(spaceId);
          getSilverpeasEnvironmentTest().removeAllProfiles(space);
          spaceId = space.getDomainFatherId();
        } while (!space.isRoot());
      }
    });
    final ComponentInst component = getSilverpeasEnvironmentTest().getDummyPublicComponent();
    component.setPublic(false);
    getSilverpeasEnvironmentTest().updateComponent(component);
  }

  /**
   * Denies the access to the silverpeas spaces to all users.
   */
  @SuppressWarnings("unused")
  public void denySpaceAuthorizationToUsers() {
    throw new NotImplementedException(
        "Migration : the implementation of denySpaceAuthorizationToUsers is not yet performed.." +
            ".");
  }

  /**
   * Authenticates the specified user through the REST-based Web API and returns the opened session
   * identifier. The returned session identifier can then be used in the subsequent requests sent
   * during a test. Once the session is opened for the given user, the session cache service is then
   * get to be set for the local thread of the current running test, so that the data prepared for
   * the test and cached in the session cache of the user will be accessible within the thread of
   * the tested web service.
   * <p></p>
   * <p>
   * Warning: the authentication is performed by the corresponding REST-based web service and as
   * such this one is required to be included in the deployment archive as well as all of its
   * dependencies.
   * </p>
   * @param user the user to authenticate.
   * @return the opened user session identifier.
   */
  public String authenticate(final User user) {
    AuthId authId = AuthId.basicAuth(user.getLogin(), user.getDomainId(), "sasa");
    Invocation.Builder authentication =
        setUpHTTPRequest("authentication", MediaType.APPLICATION_JSON, authId);
    try (Response authResponse =
             authentication.buildPost(Entity.entity("{}", MediaType.APPLICATION_JSON_TYPE))
                 .invoke()) {
      assertThat(authResponse.getStatus(), is(Response.Status.OK.getStatusCode()));
      String sessionKey = authResponse.getHeaderString(UserPrivilegeValidation.HTTP_SESSIONKEY);
      SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
      SessionInfo sessionInfo = sessionManagement.getSessionInfo(sessionKey);
      SessionCacheAccessor cacheAccessor = CacheAccessorProvider.getSessionCacheAccessor();
      cacheAccessor.setCurrentSessionCache(sessionInfo.getCache());

      return sessionKey;
    }
  }

  protected WebTarget applyQueryParameters(String parameterQueryPart, WebTarget resource) {
    MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
    String[] queryParameters = parameterQueryPart.split("&");
    for (String aQueryParameter : queryParameters) {
      if (StringUtil.isDefined(aQueryParameter)) {
        String[] parameterParts = aQueryParameter.split("=");
        parameters.add(parameterParts[0], parameterParts.length > 1 ? parameterParts[1] : "");
      }
    }
    WebTarget newResource = resource;
    for (Map.Entry<String, List<String>> parameter : parameters.entrySet()) {
      newResource = newResource.queryParam(parameter.getKey(), parameter.getValue().toArray());
    }
    return newResource;
  }

  protected AuthId withAsAuthId(AuthId credential) {
    return credential;
  }

  protected Invocation.Builder setUserIdent(AuthId authId, Invocation.Builder http) {
    if (authId.isAuthentication()) {
      http.header(AUTH_HTTP_HEADER, authId.getValue());
    } else if (authId.isInSession()) {
      http.header(SESSION_KEY_HTTP_HEADER, authId.getValue());
    }
    return http;
  }

  protected Invocation.Builder setUpHTTPRequest(final String uri, final String mediaType,
      final AuthId authId) {
    String thePath = uri;
    String queryParams = "";
    WebTarget resource = resource();
    if (thePath.contains("?")) {
      String[] pathParts = thePath.split("\\?");
      thePath = pathParts[0];
      queryParams = pathParts[1];
    }
    Invocation.Builder requestBuilder =
        applyQueryParameters(queryParams, resource.path(thePath)).request(mediaType);
    return setUserIdent(authId, requestBuilder);
  }
}

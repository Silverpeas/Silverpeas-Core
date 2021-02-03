/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Rule;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.web.environment.SilverpeasEnvironmentTest;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The base class for testing REST web services in Silverpeas. This base class wraps all of the
 * mechanismes required to prepare the environment for testing web services with RESTEasy and CDI
 * in the context of Silverpeas.
 */
public abstract class RESTWebServiceTest {

  protected static final String CONTEXT_NAME = "test";

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(
      "/org/silverpeas/web/environment/create-table-domain-user-group.sql",
      "/org/silverpeas/web/environment/create-table-space-component.sql",
      "/org/silverpeas/web/environment/create-table-profile.sql",
      "/org/silverpeas/web/environment/create-table-token.sql", getTableCreationScript())
      .loadInitialDataSetFrom(getDataSetScript());

  @Before
  public void reloadAdminCaches() {
    Administration.get().reloadCache();
  }

  /**
   * Gets the SQL script file in the classpath that contains statements to create table(s)
   * specific to this test.
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
   *
   * @return an array with the identifier of the component instances to take into account in tests.
   * The array cannot be null but it can be empty.
   */
  public abstract String[] getExistingComponentInstances();

  /**
   * Gets tools to take into account in tests. Theses tools will be considered as existing. Others
   * than those will be rejected with an HTTP error 404 (NOT FOUND).
   *
   * @return an array with the identifier of tools to take into account in tests. The array cannot
   * be null but it can be empty.
   */
  public String[] getExistingTools() {
    return new String[]{};
  }

  /**
   * Gets an initialized web target resource instance for tests.
   * @return the {@link WebTarget} instance.
   */
  public WebTarget resource() {
    return ClientBuilder.newClient().target(getBaseURI() + CONTEXT_NAME + "/services/");
  }

  protected URI getBaseURI() {
    return URI.create("http://localhost:8080/");
  }

  public SilverpeasEnvironmentTest getSilverpeasEnvironmentTest() {
    return SilverpeasEnvironmentTest.get();
  }

  /**
   * <p>
   * Gets (and initializes if necessary) the token key of the given user.<br>
   * The user must exist into database (use {@link SilverpeasEnvironmentTest#createDefaultUser()}
   * to add
   * a user, and use {@link #getSilverpeasEnvironmentTest()} to get the silverpeas environment
   * manager instance).
   * </p>
   * <p>
   * For example: <pre>getTokenKeyOf(get().createUser());</pre>
   * </p>
   *
   * @param theUser the user to authenticate.
   * @return the key of the opened session.
   */
  public String getTokenKeyOf(final User theUser) {
    return getSilverpeasEnvironmentTest().getTokenOf(theUser);
  }

  /**
   * Denies the access to the silverpeas resources to all users.</br>
   * It sets non public both the dummy component and all of the existing component instances on
   * which the test is working.
   */
  public void denyAuthorizationToUsers() {
    Stream.of(getExistingComponentInstances()).forEach(i -> {
      final ComponentInst inst = OrganizationController.get().getComponentInst(i);
      if (inst != null && inst.isPublic()) {
        inst.setPublic(false);
        getSilverpeasEnvironmentTest().updateComponent(inst);
      }
    });
    final ComponentInst component = getSilverpeasEnvironmentTest().getDummyPublicComponent();
    component.setPublic(false);
    getSilverpeasEnvironmentTest().updateComponent(component);
  }

  /**
   * Denies the access to the silverpeas spaces to all users.
   */
  public void denySpaceAuthorizationToUsers() {
    throw new NotImplementedException(
        "Migration : the implementation of denySpaceAuthorizationToUsers is not yet performed...");
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
}

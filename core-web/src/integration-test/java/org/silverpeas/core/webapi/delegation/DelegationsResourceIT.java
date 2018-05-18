/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.webapi.delegation;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.web.test.WarBuilder4WebCore;
import org.silverpeas.web.ResourceGettingTest;

import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests on the web service handling the delegations of user roles.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class DelegationsResourceIT extends ResourceGettingTest {

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/webapi/delegation/create-database.sql";
  private static final String DATA_SET_SCRIPT =
      "/org/silverpeas/core/webapi/delegation/create-dataset.sql";
  private String authToken;
  private User user;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(DelegationsResourceIT.class)
        .addRESTWebServiceEnvironment()
        .addStringTemplateFeatures()
        .testFocusedOn(w ->
            w.addPackages(true, "org.silverpeas.core.webapi.delegation")
        ).build();
  }

  @Before
  public void prepareTests() {
    user = getSilverpeasEnvironmentTest().createDefaultUser();
    authToken = getTokenKeyOf(user);
  }

  @Override
  protected String getTableCreationScript() {
    return TABLE_CREATION_SCRIPT;
  }

  @Override
  protected String getDataSetScript() {
    return DATA_SET_SCRIPT;
  }

  /**
   * The user is in a given component instance and we asks for all the delegations that were done
   * to him by another users. The user is authorized to access the given component instance.
   */
  @Test
  public void getAllDelegationsToAnAuthorizedUserForAComponentInstance() {
    /*List<DelegationEntity> t = resource().path("/delegations/delegates/2/workflow32").request()
        .header(API_TOKEN_HTTP_HEADER, encodesAPITokenValue(authToken))
        .get(new GenericType<List<DelegationEntity>>() {});*/
    Response response =
        getAt("delegations/delegates/" + user.getId() + "/workflow32", Response.class);
    assertThat(response.getStatus(), is(200));
  }

  @Override
  public void gettingAResourceByAnUnauthorizedUser() {
    // the user should only be authenticated
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{"kmelia42", "workflow12", "workflow32"};
  }

  @Override
  public String aResourceURI() {
    return "delegations/delegators/1/workflow12";
  }

  @Override
  public String anUnexistingResourceURI() {
    return "/delegations/delegator";
  }

  @Override
  public <T> T aResource() {
    return null;
  }

  @Override
  public String getAPITokenValue() {
    return authToken;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return DelegationEntity.class;
  }
}
  
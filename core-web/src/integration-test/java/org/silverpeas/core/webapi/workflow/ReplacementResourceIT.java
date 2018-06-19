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

package org.silverpeas.core.webapi.workflow;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.web.test.WarBuilder4WebCore;
import org.silverpeas.core.webapi.util.UserEntity;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.web.ResourceGettingTest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests on the web service handling the replacements of users in a workflow.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ReplacementResourceIT extends ResourceGettingTest {

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/webapi/workflow/replacement/create-table.sql";
  private static final String DATA_SET_SCRIPT =
      "/org/silverpeas/core/webapi/workflow/replacement/create-dataset.sql";

  @Inject
  private UserManager userManager;

  private String authToken;
  private User user;
  private Replacement resource;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(ReplacementResourceIT.class)
        .addRESTWebServiceEnvironment()
        .addStringTemplateFeatures()
        .addClasses(UserEntity.class)
        .addMavenDependenciesWithPersistence(
            "org.silverpeas.core.services:silverpeas-core-workflow",
            "org.silverpeas.core.services:silverpeas-core-personalorganizer")
        .testFocusedOn(w -> w.addPackages(true, "org.silverpeas.core.webapi.workflow"))
        .build();
  }

  @Before
  public void prepareTests() throws WorkflowException {
    user = getSilverpeasEnvironmentTest().createDefaultUser();
    authToken = getTokenKeyOf(user);
    List<Replacement> replacements =
        Replacement.getAllOf(userManager.getUser("1"), getExistingComponentInstances()[0]);
    assertThat(replacements.isEmpty(), is(false));
    resource = replacements.get(0);
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
  public void getAllReplacementsByAnAuthorizedUser() {
    final String aWorkflowId = getExistingComponentInstances()[0];
    Response response = getAt(aSubstituteURI(aWorkflowId, "2"), Response.class);
    assertThat(response.getStatus(), is(200));
    List<ReplacementEntity> replacements = (List<ReplacementEntity>) response.getEntity();
    assertThat(replacements.size(), is(1));
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{"workflow24", "workflow42"};
  }

  @Override
  public String aResourceURI() {
    return anIncumbentURI(getExistingComponentInstances()[0], "1");
  }

  @Override
  public String anUnexistingResourceURI() {
    return anIncumbentURI("workflow32", "1");
  }

  @Override
  public Replacement aResource() {
    return resource;
  }

  @Override
  public String getAPITokenValue() {
    return authToken;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return ReplacementEntity.class;
  }

  private String anIncumbentURI(final String workflowId, final String incumbentId) {
    return "workflow/" + workflowId + "/replacements/incumbents/" + incumbentId;
  }

  private String aSubstituteURI(final String workflowId, final String substituteId) {
    return "workflow/" + workflowId + "/replacements/substitutes/" + substituteId;
  }
}
  
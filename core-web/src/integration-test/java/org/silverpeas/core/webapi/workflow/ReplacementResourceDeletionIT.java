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
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.web.ResourceDeletionTest;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests on the web service handling the replacements of users in a workflow.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ReplacementResourceDeletionIT extends ResourceDeletionTest {

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/webapi/workflow/replacement/create-table.sql";
  private static final String DATA_SET_SCRIPT =
      "/org/silverpeas/core/webapi/workflow/replacement/create-dataset.sql";
  private static final String SUPERVISOR_ID = "0";
  private static final String REPLACEMENT_ID = "dfbd67fa-6ee7-477f-ab52-8d973d3c8c60";
  private static final int STATUS_OK = Response.Status.NO_CONTENT.getStatusCode();
  private static final int STATUS_FORBIDDEN = Response.Status.FORBIDDEN.getStatusCode();

  private String authToken;
  private User user;
  private ReplacementEntity resource;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(ReplacementResourceDeletionIT.class)
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
  public void prepareTests() {
    user = User.getById("3");
    authToken = getTokenKeyOf(user);
    Replacement replacement = Replacement.get(REPLACEMENT_ID)
        .orElseThrow(() -> new AssertionError("No Replacement with id " + REPLACEMENT_ID));
    resource = new ReplacementEntity(replacement);
  }

  @Test
  public void theIncumbentDeletesOneOfHisReplacement() {
    Response response = deleteAt(aResourceURI(), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));
  }

  @Test
  public void theSubstituteDeletesOneOfTheReplacement() {
    user = User.getById("2");
    authToken = getTokenKeyOf(user);
    Response response = deleteAt(aResourceURI(), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));
  }

  @Test
  public void theSupervisorDeletesAReplacement() {
    user = User.getById(SUPERVISOR_ID);
    authToken = getTokenKeyOf(user);
    Response response = deleteAt(aResourceURI(), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));
  }

  @Test
  public void aNonConcernedUserDeletesAReplacement() {
    user = User.getById("1");
    authToken = getTokenKeyOf(user);
    Response response = deleteAt(aResourceURI(), Response.class);
    assertThat(response.getStatus(), is(STATUS_FORBIDDEN));
  }

  @Override
  protected String getTableCreationScript() {
    return TABLE_CREATION_SCRIPT;
  }

  @Override
  protected String getDataSetScript() {
    return DATA_SET_SCRIPT;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{"workflow24", "workflow42"};
  }

  @Override
  public String aResourceURI() {
    return replacementUri(getExistingComponentInstances()[1], REPLACEMENT_ID);
  }

  @Override
  public String anUnexistingResourceURI() {
    return replacementUri(getExistingComponentInstances()[0], REPLACEMENT_ID);
  }

  @Override
  public ReplacementEntity aResource() {
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

  private String replacementUri(final String workflowId, final String replacementId) {
    return "workflow/" + workflowId + "/replacements/" + replacementId;
  }
}
  
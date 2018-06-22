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
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.web.ResourceGettingTest;

import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests on the web service handling the replacements of users in a workflow.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ReplacementResourceGettingIT extends ResourceGettingTest {

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/webapi/workflow/replacement/create-table.sql";
  private static final String DATA_SET_SCRIPT =
      "/org/silverpeas/core/webapi/workflow/replacement/create-dataset.sql";
  private static final String SUPERVISOR_ID = "0";
  private static final int STATUS_OK = Response.Status.OK.getStatusCode();

  private String authToken;
  private User user;
  private ReplacementEntity resource;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(ReplacementResourceGettingIT.class)
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
    user = User.getById("1");
    authToken = getTokenKeyOf(user);
    final String replacementId = "92b0fa2f-3287-4f99-a9b7-16424f82d607";
    Replacement replacement = Replacement.get(replacementId)
        .orElseThrow(() -> new AssertionError("No replacement with id " + replacementId));
    resource = new ReplacementEntity(replacement);
  }

  @Override
  protected String getTableCreationScript() {
    return TABLE_CREATION_SCRIPT;
  }

  @Override
  protected String getDataSetScript() {
    return DATA_SET_SCRIPT;
  }

  @Test
  public void getAllReplacementsByAnAuthorizedUser() {
    final String aWorkflowId = getExistingComponentInstances()[0];
    Response response = getAt(uriWithSubstitute(replacementsUri(aWorkflowId), "2"), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));

    ReplacementEntity[] replacements = response.readEntity(ReplacementEntity[].class);
    assertThat(replacements.length, is(1));
    assertThat(replacements[0].getIncumbent().getId(), is("1"));
    assertThat(replacements[0].getSubstitute().getId(), is("2"));
    assertThat(replacements[0].getWorkflowInstanceId(), is(aWorkflowId));
  }

  @Test
  public void getAllReplacementsOfAnAuthorizedUser() {
    final String aWorkflowId = getExistingComponentInstances()[0];
    Response response = getAt(uriWithIncumbent(replacementsUri(aWorkflowId), "1"), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));

    ReplacementEntity[] replacements = response.readEntity(ReplacementEntity[].class);
    assertThat(replacements.length, is(1));
    assertThat(replacements[0].getIncumbent().getId(), is("1"));
    assertThat(replacements[0].getSubstitute().getId(), is("2"));
    assertThat(replacements[0].getWorkflowInstanceId(), is(aWorkflowId));
  }

  @Test
  public void getAllReplacementsWithConcernedUsers() {
    final String aWorkflowId = getExistingComponentInstances()[0];
    Response response =
        getAt(uriWithSubstitute(uriWithIncumbent(replacementsUri(aWorkflowId), "1"), "2"),
            Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));

    ReplacementEntity[] replacements = response.readEntity(ReplacementEntity[].class);
    assertThat(replacements.length, is(1));
    assertThat(replacements[0].getIncumbent().getId(), is("1"));
    assertThat(replacements[0].getSubstitute().getId(), is("2"));
    assertThat(replacements[0].getWorkflowInstanceId(), is(aWorkflowId));
  }

  @Test
  public void aNonConcernedUserAsksForAllReplacementsByASubstitute() {
    user = User.getById("3");
    authToken = getTokenKeyOf(user);

    final String aWorkflowId = getExistingComponentInstances()[0];
    Response response = getAt(uriWithSubstitute(replacementsUri(aWorkflowId), "2"), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));

    ReplacementEntity[] replacements = response.readEntity(ReplacementEntity[].class);
    assertThat(replacements.length, is(0));
  }

  @Test
  public void aNonConcernedUserAsksForAllReplacementsOfAnIncumbent() {
    user = User.getById("3");
    authToken = getTokenKeyOf(user);

    final String aWorkflowId = getExistingComponentInstances()[0];
    Response response = getAt(uriWithIncumbent(replacementsUri(aWorkflowId), "1"), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));

    ReplacementEntity[] replacements = response.readEntity(ReplacementEntity[].class);
    assertThat(replacements.length, is(0));
  }

  @Test
  public void aSupervisorAsksForAllReplacementsByASubstitute() {
    user = User.getById(SUPERVISOR_ID);
    authToken = getTokenKeyOf(user);

    final String aWorkflowId = getExistingComponentInstances()[0];
    Response response = getAt(uriWithSubstitute(replacementsUri(aWorkflowId), "2"), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));

    ReplacementEntity[] replacements = response.readEntity(ReplacementEntity[].class);
    assertThat(replacements.length, is(1));
    assertThat(replacements[0].getIncumbent().getId(), is("1"));
    assertThat(replacements[0].getSubstitute().getId(), is("2"));
    assertThat(replacements[0].getWorkflowInstanceId(), is(aWorkflowId));
  }

  @Test
  public void aSupervisorAsksForAllReplacementsOfAnIncumbent() {
    user = User.getById(SUPERVISOR_ID);
    authToken = getTokenKeyOf(user);

    final String aWorkflowId = getExistingComponentInstances()[0];
    Response response = getAt(uriWithIncumbent(replacementsUri(aWorkflowId), "1"), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));

    ReplacementEntity[] replacements = response.readEntity(ReplacementEntity[].class);
    assertThat(replacements.length, is(1));
    assertThat(replacements[0].getIncumbent().getId(), is("1"));
    assertThat(replacements[0].getSubstitute().getId(), is("2"));
    assertThat(replacements[0].getWorkflowInstanceId(), is(aWorkflowId));
  }

  @Test
  public void aSupervisorAsksForAllReplacementsInAWorkflow() {
    user = User.getById(SUPERVISOR_ID);
    authToken = getTokenKeyOf(user);

    final String aWorkflowId = getExistingComponentInstances()[1];
    Response response = getAt(replacementsUri(aWorkflowId), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));

    ReplacementEntity[] replacements = response.readEntity(ReplacementEntity[].class);
    assertThat(replacements.length, is(4));
    assertThat(Stream.of(replacements).allMatch(r -> r.getWorkflowInstanceId().equals(aWorkflowId)),
        is(true));
  }

  @Test
  public void aNonSupervisorAsksForAllReplacementsInAWorkflow() {
    final String aWorkflowId = getExistingComponentInstances()[1];
    final int Forbidden = Response.Status.FORBIDDEN.getStatusCode();
    Response response = getAt(replacementsUri(aWorkflowId), Response.class);
    assertThat(response.getStatus(), is(Forbidden));
  }

  @Test
  public void aSupervisorAsksForAGivenReplacement() {
    user = User.getById(SUPERVISOR_ID);
    authToken = getTokenKeyOf(user);

    final String aWorkflowId = getExistingComponentInstances()[1];
    final String replacementId = "64c8e712-e48a-4c63-b768-a5385f30a1ae";
    Response response =
        getAt(uriOfReplacement(replacementsUri(aWorkflowId), replacementId), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));

    ReplacementEntity replacement = response.readEntity(ReplacementEntity.class);
    assertThat(replacement.getURI()
        .toString()
        .endsWith(uriOfReplacement(replacementsUri(aWorkflowId), replacementId)), is(true));
    assertThat(replacement.getIncumbent().getId(), is("1"));
    assertThat(replacement.getSubstitute().getId(), is("3"));
    assertThat(replacement.getWorkflowInstanceId(), is(aWorkflowId));
    assertThat(replacement.getStartDate(), is(LocalDate.parse("2018-04-09")));
    assertThat(replacement.getEndDate(), is(LocalDate.parse("2018-04-13")));
  }

  @Test
  public void anIncumbentAsksForOneOfHisReplacement() {
    user = User.getById("1");
    authToken = getTokenKeyOf(user);

    final String aWorkflowId = getExistingComponentInstances()[1];
    final String replacementId = "64c8e712-e48a-4c63-b768-a5385f30a1ae";
    Response response =
        getAt(uriOfReplacement(replacementsUri(aWorkflowId), replacementId), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));

    ReplacementEntity replacement = response.readEntity(ReplacementEntity.class);
    assertThat(replacement.getURI()
        .toString()
        .endsWith(uriOfReplacement(replacementsUri(aWorkflowId), replacementId)), is(true));
    assertThat(replacement.getIncumbent().getId(), is("1"));
    assertThat(replacement.getSubstitute().getId(), is("3"));
    assertThat(replacement.getWorkflowInstanceId(), is(aWorkflowId));
    assertThat(replacement.getStartDate(), is(LocalDate.parse("2018-04-09")));
    assertThat(replacement.getEndDate(), is(LocalDate.parse("2018-04-13")));
  }

  @Test
  public void aSubstituteAsksForOneOfHisReplacement() {
    user = User.getById("3");
    authToken = getTokenKeyOf(user);

    final String aWorkflowId = getExistingComponentInstances()[1];
    final String replacementId = "64c8e712-e48a-4c63-b768-a5385f30a1ae";
    Response response =
        getAt(uriOfReplacement(replacementsUri(aWorkflowId), replacementId), Response.class);
    assertThat(response.getStatus(), is(STATUS_OK));

    ReplacementEntity replacement = response.readEntity(ReplacementEntity.class);
    assertThat(replacement.getURI()
        .toString()
        .endsWith(uriOfReplacement(replacementsUri(aWorkflowId), replacementId)), is(true));
    assertThat(replacement.getIncumbent().getId(), is("1"));
    assertThat(replacement.getSubstitute().getId(), is("3"));
    assertThat(replacement.getWorkflowInstanceId(), is(aWorkflowId));
    assertThat(replacement.getStartDate(), is(LocalDate.parse("2018-04-09")));
    assertThat(replacement.getEndDate(), is(LocalDate.parse("2018-04-13")));
  }

  @Test
  public void aNonSupervisorAsksForAReplacementForWhichHeIsNotConcerned() {
    user = User.getById("2");
    authToken = getTokenKeyOf(user);

    final String aWorkflowId = getExistingComponentInstances()[1];
    final int Forbidden = Response.Status.FORBIDDEN.getStatusCode();
    final String replacementId = "64c8e712-e48a-4c63-b768-a5385f30a1ae";
    Response response =
        getAt(uriOfReplacement(replacementsUri(aWorkflowId), replacementId), Response.class);
    assertThat(response.getStatus(), is(Forbidden));
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{"workflow24", "workflow42"};
  }

  @Override
  public String aResourceURI() {
    return uriWithIncumbent(replacementsUri(getExistingComponentInstances()[0]), "1");
  }

  @Override
  public String anUnexistingResourceURI() {
    return uriWithIncumbent(replacementsUri("workflow32"), "1");
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

  private String uriWithIncumbent(final String replacementsUri, final String incumbentId) {
    final String sep = replacementsUri.contains("?") ? "&" : "?";
    return replacementsUri + sep + "incumbent=" + incumbentId;
  }

  private String uriWithSubstitute(final String replacementsUri, final String substituteId) {
    final String sep = replacementsUri.contains("?") ? "&" : "?";
    return replacementsUri + sep + "substitute=" + substituteId;
  }

  private String replacementsUri(final String workflowId) {
    return "workflow/" + workflowId + "/replacements";
  }

  private String uriOfReplacement(final String replacementsUri, final String replacementId) {
    return replacementsUri + "/" + replacementId;
  }
}
  
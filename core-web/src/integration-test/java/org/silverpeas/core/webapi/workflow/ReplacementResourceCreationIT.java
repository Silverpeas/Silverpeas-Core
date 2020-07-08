/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
import org.silverpeas.core.date.Period;
import org.silverpeas.core.web.test.WarBuilder4WebCore;
import org.silverpeas.core.webapi.util.UserEntity;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.web.ResourceCreationTest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests on the web service handling the replacements of users in a workflow.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ReplacementResourceCreationIT extends ResourceCreationTest {

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/webapi/workflow/replacement/create-table.sql";
  private static final String DATA_SET_SCRIPT =
      "/org/silverpeas/core/webapi/workflow/replacement/create-dataset.sql";
  private static final String SUPERVISOR_ID = "0";
  private static final int STATUS_CREATED = Response.Status.CREATED.getStatusCode();
  private static final int STATUS_FORBIDDEN = Response.Status.FORBIDDEN.getStatusCode();

  @Inject
  private UserManager userManager;

  private String authToken;
  private User user;
  private ReplacementEntity resource;
  private Period period =
      Period.between(LocalDate.now().plusDays(1), LocalDate.now().plusWeeks(3));

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(ReplacementResourceCreationIT.class)
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
    user = User.getById("2");
    authToken = getTokenKeyOf(user);
    resource = new ReplacementEntity(
        Replacement.between(userManager.getUser("2"), userManager.getUser("3"))
            .inWorkflow(getExistingComponentInstances()[0])
            .during(period));
  }

  @Test
  public void aUserCreateANewReplacementOfHim() {
    final Pattern uriPattern = Pattern.compile(
        "^http://localhost:8080/silverpeas/services/" + aResourceURI() + "/[a-z0-9\\-]+$");
    final ReplacementEntity expected = aResource();
    Response response = post(expected, aResourceURI());
    assertThat(response.getStatus(), is(STATUS_CREATED));
    ;
    ReplacementEntity createdEntity = response.readEntity(ReplacementEntity.class);
    assertThat(createdEntity, notNullValue());
    assertThat(createdEntity.getURI(), is(response.getLocation()));
    assertThat(uriPattern.matcher(createdEntity.getURI().toString()).matches(), is(true));
    assertThat(createdEntity.getIncumbent().getId(), is(expected.getIncumbent().getId()));
    assertThat(createdEntity.getSubstitute().getId(), is(expected.getSubstitute().getId()));
    assertThat(createdEntity.getWorkflowInstanceId(), is(expected.getWorkflowInstanceId()));
    assertThat(createdEntity.getStartDate(), is(expected.getStartDate()));
    assertThat(createdEntity.getEndDate(), is(expected.getEndDate()));
  }

  @Test
  public void aSupervisorCreateANewReplacementOfAUser() {
    user = User.getById(SUPERVISOR_ID);
    authToken = getTokenKeyOf(user);
    final Pattern uriPattern = Pattern.compile(
        "^http://localhost:8080/silverpeas/services/" + aResourceURI() + "/[a-z0-9\\-]+$");
    final ReplacementEntity expected = aResource();
    Response response = post(expected, aResourceURI());
    assertThat(response.getStatus(), is(STATUS_CREATED));
    ReplacementEntity createdEntity = response.readEntity(ReplacementEntity.class);
    assertThat(createdEntity, notNullValue());
    assertThat(createdEntity.getURI(), is(response.getLocation()));
    assertThat(uriPattern.matcher(createdEntity.getURI().toString()).matches(), is(true));
    assertThat(createdEntity.getIncumbent().getId(), is(expected.getIncumbent().getId()));
    assertThat(createdEntity.getSubstitute().getId(), is(expected.getSubstitute().getId()));
    assertThat(createdEntity.getWorkflowInstanceId(), is(expected.getWorkflowInstanceId()));
    assertThat(createdEntity.getStartDate(), is(expected.getStartDate()));
    assertThat(createdEntity.getEndDate(), is(expected.getEndDate()));
  }

  @Test
  public void aUserCreateANewReplacementForHim() {
    user = User.getById("3");
    authToken = getTokenKeyOf(user);
    final ReplacementEntity expected = aResource();
    Response response = post(expected, aResourceURI());
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
    return replacementsUri(getExistingComponentInstances()[0]);
  }

  @Override
  public String anUnexistingResourceURI() {
    return replacementsUri("workflow32");
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

  private String replacementsUri(final String workflowId) {
    return "workflow/" + workflowId + "/replacements";
  }
}
  
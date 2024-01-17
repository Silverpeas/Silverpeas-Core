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
import org.silverpeas.web.ResourceUpdateTest;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.net.URI;
import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests on the web service handling the replacements of users in a workflow.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ReplacementResourceUpdateIT extends ResourceUpdateTest {

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/webapi/workflow/replacement/create-table.sql";
  private static final String DATA_SET_SCRIPT =
      "/org/silverpeas/core/webapi/workflow/replacement/create-dataset.sql";
  private static final String SUPERVISOR_ID = "0";
  private static final String REPLACEMENT_ID = "92b0fa2f-3287-4f99-a9b7-16424f82d607";
  private static final String INVALID_REPLACEMENT_ID = "dfbd67fa-6ee7-477f-ab52-8d973d3c8c60";
  private static final int STATUS_BAD_REQUEST = Response.Status.BAD_REQUEST.getStatusCode();
  private static final int STATUS_FORBIDDEN = Response.Status.FORBIDDEN.getStatusCode();

  @Inject
  private UserManager userManager;

  private String authToken;
  private User user;
  private Replacement replacement;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(ReplacementResourceUpdateIT.class)
        .addRESTWebServiceEnvironment()
        .addStringTemplateFeatures()
        .addClasses(UserEntity.class)
        .addMavenDependenciesWithPersistence(
            "org.silverpeas.core.services:silverpeas-core-workflow",
            "org.silverpeas.core.services:silverpeas-core-personalorganizer")
        .testFocusedOn(w -> w.addPackages(true, "org.silverpeas.core.webapi.workflow")
            .addAsResource("org/silverpeas/workflow/multilang"))
        .build();
  }

  @Before
  public void prepareTests() throws WorkflowException {
    user = User.getById("1");
    authToken = getTokenKeyOf(user);
    replacement = Replacement.get(REPLACEMENT_ID)
        .orElseThrow(() -> new AssertionError("Replacement " + REPLACEMENT_ID + " not found!"));
  }

  @Test
  public void aUserChangesTheSubstituteInAReplacementOfHim() throws WorkflowException {
    final ReplacementEntity expected = aResource();
    expected.setSubstitute(userManager.getUser("3"));
    final ReplacementEntity actual = putAt(aResourceURI(), expected);
    assertThat(actual.getURI(), is(expected.getURI()));
    assertThat(actual.getIncumbent().getId(), is(expected.getIncumbent().getId()));
    assertThat(actual.getSubstitute().getId(), is(expected.getSubstitute().getId()));
    assertThat(actual.getWorkflowInstanceId(), is(expected.getWorkflowInstanceId()));
    assertThat(actual.getStartDate(), is(expected.getStartDate()));
    assertThat(actual.getEndDate(), is(expected.getEndDate()));
  }

  @Test
  public void aUserChangesThePeriodInAReplacementOfHim() {
    final ReplacementEntity expected = aResource();
    expected.setStartDate(LocalDate.now().plusDays(1));
    expected.setEndDate(LocalDate.now().plusMonths(1));
    final ReplacementEntity actual = putAt(aResourceURI(), expected);
    assertThat(actual.getURI(), is(expected.getURI()));
    assertThat(actual.getIncumbent().getId(), is(expected.getIncumbent().getId()));
    assertThat(actual.getSubstitute().getId(), is(expected.getSubstitute().getId()));
    assertThat(actual.getWorkflowInstanceId(), is(expected.getWorkflowInstanceId()));
    assertThat(actual.getStartDate(), is(expected.getStartDate()));
    assertThat(actual.getEndDate(), is(expected.getEndDate()));
  }

  @Test
  public void aSupervisorChangesTheSubstituteInAReplacementOfAUser() throws WorkflowException {
    user = User.getById(SUPERVISOR_ID);
    authToken = getTokenKeyOf(user);
    final ReplacementEntity expected = aResource();
    expected.setSubstitute(userManager.getUser("3"));
    final ReplacementEntity actual = putAt(aResourceURI(), expected);
    assertThat(actual.getURI(), is(expected.getURI()));
    assertThat(actual.getIncumbent().getId(), is(expected.getIncumbent().getId()));
    assertThat(actual.getSubstitute().getId(), is(expected.getSubstitute().getId()));
    assertThat(actual.getWorkflowInstanceId(), is(expected.getWorkflowInstanceId()));
    assertThat(actual.getStartDate(), is(expected.getStartDate()));
    assertThat(actual.getEndDate(), is(expected.getEndDate()));
  }

  @Test
  public void aSupervisorChangesThePeriodOfAReplacementOfAUser() {
    user = User.getById(SUPERVISOR_ID);
    authToken = getTokenKeyOf(user);
    final ReplacementEntity expected = aResource();
    expected.setStartDate(LocalDate.now().plusDays(1));
    expected.setEndDate(LocalDate.now().plusMonths(1));
    final ReplacementEntity actual = putAt(aResourceURI(), expected);
    assertThat(actual.getURI(), is(expected.getURI()));
    assertThat(actual.getIncumbent().getId(), is(expected.getIncumbent().getId()));
    assertThat(actual.getSubstitute().getId(), is(expected.getSubstitute().getId()));
    assertThat(actual.getWorkflowInstanceId(), is(expected.getWorkflowInstanceId()));
    assertThat(actual.getStartDate(), is(expected.getStartDate()));
    assertThat(actual.getEndDate(), is(expected.getEndDate()));
  }

  @Test
  public void aUserUpdateAReplacementForHim() throws WorkflowException {
    user = User.getById("3");
    authToken = getTokenKeyOf(user);
    final ReplacementEntity expected = aResource();
    expected.setSubstitute(userManager.getUser(user.getId()));
    try {
      putAt(aResourceURI(), expected);
    } catch (WebApplicationException e) {
      assertThat(e.getResponse().getStatus(), is(STATUS_FORBIDDEN));
    }
  }

  @Test
  public void aSubstituteUpdateAReplacement() {
    final ReplacementEntity expected = aResource();
    user = User.getById(expected.getSubstitute().getId());
    authToken = getTokenKeyOf(user);
    expected.setStartDate(LocalDate.now().plusDays(1));
    expected.setEndDate(LocalDate.now().plusMonths(1));
    try {
      putAt(aResourceURI(), expected);
    } catch (WebApplicationException e) {
      assertThat(e.getResponse().getStatus(), is(STATUS_FORBIDDEN));
    }
  }

  @Test
  public void aSubstituteUpdateAReplacementForHim() {
    final ReplacementEntity expected = aResource();
    user = User.getById(expected.getSubstitute().getId());
    authToken = getTokenKeyOf(user);
    try {
      Field incumbent = ReplacementEntity.class.getDeclaredField("incumbent");
      incumbent.setAccessible(true);
      incumbent.set(expected, new UserEntity(user));
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    try {
      putAt(aResourceURI(), expected);
    } catch (WebApplicationException e) {
      assertThat(e.getResponse().getStatus(), is(STATUS_BAD_REQUEST));
    }
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
    return replacementUri(getExistingComponentInstances()[0], replacement.getId());
  }

  @Override
  public String anUnexistingResourceURI() {
    return replacementUri("workflow32", replacement.getId());
  }

  @Override
  public ReplacementEntity aResource() {
    final String resourceUri = "http://localhost:8080/silverpeas/services/" + aResourceURI();
    return ReplacementEntity.asWebEntity(replacement, URI.create(resourceUri));
  }

  @Override
  public ReplacementEntity anInvalidResource() {
    return new ReplacementEntity(Replacement.get(INVALID_REPLACEMENT_ID)
        .orElseThrow(() -> new AssertionError(
            "Invalid replacement " + INVALID_REPLACEMENT_ID + " not found!")));
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
  
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

package org.silverpeas.core.workflow.engine.user;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.util.SQLRequester;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.WorkflowHub;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests on the delegation business mechanism.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ReplacementIT {

  private static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/workflow/engine/user/create-tables.sql";
  private static final String DATA_SET_SCRIPT =
      "/org/silverpeas/core/workflow/engine/user/create-dataset.sql";
  private static final String WORKFLOW_INSTANCE_ID = "workflow42";

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATA_SET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(ReplacementIT.class)
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .createMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-pdc")
        .createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud")
        .createMavenDependencies("org.silverpeas.core.services:silverpeas-core-personalorganizer")
        .addAsResource("org/silverpeas/lookAndFeel")
        .addAsResource("org/silverpeas/util")
        .testFocusedOn(war -> war.addPackages(true, "org.silverpeas.core.workflow")
            .addAsResource("org/silverpeas/workflow/multilang"))
        .build();
  }

  @Test
  public void getNoReplacementsOfAUserWithoutAnyReplacements() {
    List<Replacement> replacements = Replacement.getAllOf(aUser("0"), WORKFLOW_INSTANCE_ID);
    assertThat(replacements.isEmpty(), is(true));
  }

  @Test
  public void getNoReplacementsByAUserWithoutAnyReplacements() {
    List<Replacement> replacements = Replacement.getAllBy(aUser("0"), WORKFLOW_INSTANCE_ID);
    assertThat(replacements.isEmpty(), is(true));
  }

  @Test
  public void saveNewReplacementBetweenTwoUsersShouldPersistAllItsData() throws SQLException {
    final LocalDate tomorrow = LocalDate.now().plusDays(1);
    OperationContext.fromUser("0");
    Replacement savedReplacement = Replacement.between(aUser("1"), aUser("0"))
                                              .inWorkflow(WORKFLOW_INSTANCE_ID)
                                              .during(
                                                  Period.between(tomorrow, tomorrow.plusWeeks(2)))
                                              .save();
    assertThat(savedReplacement.isPersisted(), is(true));

    Map<String, Object> actualReplacement =
        SQLRequester.findOne("select * from sb_workflow_replacements where id = ?",
            savedReplacement.getId());
    assertThat(actualReplacement.isEmpty(), is(false));
    assertThat(savedReplacement.getIncumbent().getUserId(),
        is(actualReplacement.get("INCUMBENTID")));
    assertThat(savedReplacement.getSubstitute().getUserId(),
        is(actualReplacement.get("SUBSTITUTEID")));
    assertThat(savedReplacement.getWorkflowInstanceId(), is(actualReplacement.get("WORKFLOWID")));
    assertThat(savedReplacement.getPeriod().getStartDate(),
        is(toLocalDate(actualReplacement.get("STARTDATE"))));
    assertThat(savedReplacement.getPeriod().getEndDate(),
        is(toLocalDate(actualReplacement.get("ENDDATE"))));
  }

  @Test
  public void updateAReplacement() throws SQLException {
    final String id = "c550ffb1-6e76-4947-9fe3-b69777d758b6";
    OperationContext.fromUser("0");
    final Replacement replacement =
        Replacement.get(id).orElseThrow(() -> new AssertionError("No replacement of id " + id));
    final Replacement expected = replacement.setSubstitute(aUser("3"))
        .setPeriod(Period.between(LocalDate.now().plusDays(1), LocalDate.now().plusMonths(1)))
        .save();
    Map<String, Object> actual =
        SQLRequester.findOne("select * from sb_workflow_replacements where id = ?",
            replacement.getId());
    assertThat(actual.isEmpty(), is(false));
    assertThat(expected.getIncumbent().getUserId(),
        is(actual.get("INCUMBENTID")));
    assertThat(expected.getSubstitute().getUserId(),
        is(actual.get("SUBSTITUTEID")));
    assertThat(expected.getWorkflowInstanceId(), is(actual.get("WORKFLOWID")));
    assertThat(expected.getPeriod().getStartDate(),
        is(toLocalDate(actual.get("STARTDATE"))));
    assertThat(expected.getPeriod().getEndDate(),
        is(toLocalDate(actual.get("ENDDATE"))));
  }

  @Test
  public void getReplacementsOfAReplacedUserShouldReturnAllOfThem() {
    final User incumbent = aUser("1");
    List<Replacement> replacements = Replacement.getAllOf(incumbent, WORKFLOW_INSTANCE_ID);
    assertThat(replacements.isEmpty(), is(false));
    assertThat(replacements.size(), is(2));
    assertThat(replacements.stream()
                           .allMatch(
                               r -> r.getIncumbent().getUserId().equals(incumbent.getUserId())),
        is(true));
  }

  @Test
  public void getReplacementsByASubstituteShouldReturnAllOfThem() {
    final User substitute = aUser("2");
    List<Replacement> replacements = Replacement.getAllBy(substitute, WORKFLOW_INSTANCE_ID);
    assertThat(replacements.isEmpty(), is(false));
    assertThat(replacements.size(), is(2));
    assertThat(replacements.stream()
                           .allMatch(
                               r -> r.getSubstitute().getUserId().equals(substitute.getUserId())),
        is(true));
  }

  @Test
  public void getAllReplacementsInAWorkflowInstanceShouldReturnAllOfThem() {
    List<Replacement> replacements = Replacement.getAll(WORKFLOW_INSTANCE_ID);
    assertThat(replacements.isEmpty(), is(false));
    assertThat(replacements.size(), is(4));
    assertThat(
        replacements.stream().allMatch(r -> r.getWorkflowInstanceId().equals(WORKFLOW_INSTANCE_ID)),
        is(true));
  }

  @Test
  public void getAllReplacementsWithGivenUsersInAWorkflowInstanceShouldReturnAllOfThem() {
    final User incumbent = aUser("1");
    final User substitute = aUser("2");
    List<Replacement> replacements =
        Replacement.getAllWith(incumbent, substitute, WORKFLOW_INSTANCE_ID);
    assertThat(replacements.isEmpty(), is(false));
    assertThat(replacements.size(), is(1));
    assertThat(replacements.stream()
        .allMatch(r -> r.getWorkflowInstanceId().equals(WORKFLOW_INSTANCE_ID) &&
            r.getSubstitute().getUserId().equals(substitute.getUserId()) &&
            r.getIncumbent().getUserId().equals(incumbent.getUserId())), is(true));
  }

  @Test
  public void getAReplacementByItsId() {
    final String id = "c550ffb1-6e76-4947-9fe3-b69777d758b6";
    Optional<Replacement> replacement = Replacement.get(id);
    assertThat(replacement.isPresent(), is(true));

    replacement.ifPresent(r -> {
      assertThat(r.getId(), is(id));
      assertThat(r.getWorkflowInstanceId(), is(WORKFLOW_INSTANCE_ID));
      assertThat(r.getIncumbent().getUserId(), is("1"));
      assertThat(r.getSubstitute().getUserId(), is("2"));
      assertThat(r.getPeriod().getStartDate(), is(LocalDate.parse("2018-04-09")));
      assertThat(r.getPeriod().getEndDate(), is(LocalDate.parse("2018-04-13")));
    });
  }

  private User aUser(final String userId) {
    try {
      return WorkflowHub.getUserManager().getUser(userId);
    } catch (WorkflowException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  private LocalDate toLocalDate(final Object date) {
    if (date instanceof Date) {
      return ((Date) date).toLocalDate();
    }
    throw new IllegalArgumentException("Not a Date");
  }
}
  
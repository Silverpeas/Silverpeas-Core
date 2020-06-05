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

import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.User;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.silverpeas.core.workflow.engine.user.TestContext.WORKFLOW_ID;
import static org.silverpeas.core.workflow.engine.user.TestContext.aUser;

/**
 * Unit tests on the replacement of users by another ones in a workflow instance.
 * @author mmoquillon
 */
public class ReplacementTest {

  private final User anIncumbent = aUser("3");
  private final User aSubstitute = aUser("5");
  private final Period aPeriod =
      Period.between(LocalDate.now().plusDays(1), LocalDate.now().plusDays(8));

  @Rule
  public TestContext ctx = new TestContext(anIncumbent, aSubstitute);

  @Test
  public void createAReplacementOfAUserByAnotherOne() {
    Replacement replacement = Replacement.between(anIncumbent, aSubstitute)
        .inWorkflow(WORKFLOW_ID)
        .during(aPeriod);
    assertThat(replacement.getIncumbent(), is(anIncumbent));
    assertThat(replacement.getSubstitute(), is(aSubstitute));
    assertThat(replacement.getWorkflowInstanceId(), is(WORKFLOW_ID));
    assertThat(replacement.getPeriod(), is(aPeriod));
  }

  @Test(expected = NullPointerException.class)
  public void createBadlyAReplacementWithANullIncumbent() {
    Replacement.between(null, aSubstitute)
        .inWorkflow(WORKFLOW_ID)
        .during(aPeriod);
  }

  @Test(expected = NullPointerException.class)
  public void createBadlyADelegationWithANullSubstitute() {
    Replacement.between(anIncumbent, null)
        .inWorkflow(WORKFLOW_ID)
        .during(aPeriod);
  }

  @Test(expected = AssertionError.class)
  public void createBadlyAReplacementWithAnNullWorkflowId() {
    Replacement.between(anIncumbent, aSubstitute)
        .inWorkflow(null)
        .during(aPeriod);
  }

  @Test(expected = AssertionError.class)
  public void createBadlyAReplacementByMissingAWorkflowId() {
    Replacement.between(anIncumbent, aSubstitute)
        .during(aPeriod);
  }

  @Test(expected = NullPointerException.class)
  public void createAReplacementInAnUndefinedPeriod() {
    Replacement.between(anIncumbent, aSubstitute)
        .inWorkflow(WORKFLOW_ID)
        .during(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createAReplacementToHimself() {
    Replacement.between(anIncumbent, anIncumbent)
        .inWorkflow(WORKFLOW_ID)
        .during(aPeriod);
  }

  @Test
  public void saveANewReplacement() {
    Replacement replacement = Replacement.between(anIncumbent, aSubstitute)
        .inWorkflow(WORKFLOW_ID)
        .during(aPeriod)
        .save();
    assertThat(replacement.isPersisted(), is(true));
  }

  @Test
  public void getAllEmptyReplacementsOfAUser() {
    List<Replacement> replacements = Replacement.getAllOf(aUser("32"), WORKFLOW_ID);
    assertThat(replacements.isEmpty(), is(true));
  }

  @Test
  public void getAllReplacementsOfAUser() {
    List<Replacement> replacements = Replacement.getAllOf(anIncumbent, WORKFLOW_ID);
    assertThat(replacements.isEmpty(), is(false));
  }

  @Test
  public void getAllEmptyReplacementsByAUser() {
    List<? extends Replacement> replacements = Replacement.getAllBy(aUser("32"), WORKFLOW_ID);
    assertThat(replacements.isEmpty(), is(true));
  }

  @Test
  public void getAllReplacementsByAUser() {
    List<Replacement> replacements = Replacement.getAllBy(aSubstitute, WORKFLOW_ID);
    assertThat(replacements.isEmpty(), is(false));
  }

  @Test
  public void getEmptyReplacementsOfAUserInNonMatchingWorkflowInstance() {
    List<Replacement> replacements = Replacement.getAllOf(anIncumbent, "toto23");
    assertThat(replacements.isEmpty(), is(true));
  }

  @Test
  public void getEmptyReplacementsByAUserInNonMatchingWorflowInstance() {
    List<Replacement> replacements = Replacement.getAllBy(aSubstitute, "toto23");
    assertThat(replacements.isEmpty(), is(true));
  }
}
  
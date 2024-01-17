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
package org.silverpeas.core.workflow.engine.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedBeans;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.User;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.silverpeas.core.workflow.engine.user.TestContext.WORKFLOW_ID;
import static org.silverpeas.core.workflow.engine.user.TestContext.aUser;

/**
 * Unit tests on the replacement of users by another ones in a workflow instance.
 * @author mmoquillon
 */
@EnableSilverTestEnv
@TestManagedBeans({ReplacementConstructor.class})
class ReplacementTest {

  private final User anIncumbent = aUser("3");
  private final User aSubstitute = aUser("5");
  private final Period aPeriod =
      Period.between(LocalDate.now().plusDays(1), LocalDate.now().plusDays(8));

  public TestContext ctx;

  @BeforeEach
  public void init() {
    ctx = new TestContext(anIncumbent, aSubstitute);
    ctx.init();
  }

  @Test
  void createAReplacementOfAUserByAnotherOne() {
    Replacement<?> replacement = Replacement.between(anIncumbent, aSubstitute)
        .inWorkflow(WORKFLOW_ID)
        .during(aPeriod);
    assertThat(replacement.getIncumbent(), is(anIncumbent));
    assertThat(replacement.getSubstitute(), is(aSubstitute));
    assertThat(replacement.getWorkflowInstanceId(), is(WORKFLOW_ID));
    assertThat(replacement.getPeriod(), is(aPeriod));
  }

  @Test
  void createBadlyAReplacementWithANullIncumbent() {
    assertThrows(NullPointerException.class, () ->
        Replacement.between(null, aSubstitute)
            .inWorkflow(WORKFLOW_ID)
            .during(aPeriod));
  }

  @Test
  void createBadlyADelegationWithANullSubstitute() {
    assertThrows(NullPointerException.class, () ->
        Replacement.between(anIncumbent, null)
          .inWorkflow(WORKFLOW_ID)
          .during(aPeriod));
  }

  @Test
  void createBadlyAReplacementWithAnNullWorkflowId() {
    assertThrows(AssertionError.class, () ->
        Replacement.between(anIncumbent, aSubstitute)
          .inWorkflow(null)
          .during(aPeriod));
  }

  @Test
  void createBadlyAReplacementByMissingAWorkflowId() {
    assertThrows(AssertionError.class, () ->
        Replacement.between(anIncumbent, aSubstitute)
          .during(aPeriod));
  }

  @Test
  void createAReplacementInAnUndefinedPeriod() {
    assertThrows(NullPointerException.class, () ->
        Replacement.between(anIncumbent, aSubstitute)
            .inWorkflow(WORKFLOW_ID)
            .during(null));
  }

  @Test
  void createAReplacementToHimself() {
    assertThrows(IllegalArgumentException.class, () ->
        Replacement.between(anIncumbent, anIncumbent)
            .inWorkflow(WORKFLOW_ID)
            .during(aPeriod));
  }

  @Test
  void getAllEmptyReplacementsOfAUser() {
    List<? extends Replacement<?>> replacements = Replacement.getAllOf(aUser("32"), WORKFLOW_ID);
    assertThat(replacements.isEmpty(), is(true));
  }

  @Test
  void getAllReplacementsOfAUser() {
    List<? extends Replacement<?>> replacements = Replacement.getAllOf(anIncumbent, WORKFLOW_ID);
    assertThat(replacements.isEmpty(), is(false));
  }

  @Test
  void getAllEmptyReplacementsByAUser() {
    List<? extends Replacement<?>> replacements = Replacement.getAllBy(aUser("32"), WORKFLOW_ID);
    assertThat(replacements.isEmpty(), is(true));
  }

  @Test
  void getAllReplacementsByAUser() {
    List<? extends Replacement<?>> replacements = Replacement.getAllBy(aSubstitute, WORKFLOW_ID);
    assertThat(replacements.isEmpty(), is(false));
  }

  @Test
  void getEmptyReplacementsOfAUserInNonMatchingWorkflowInstance() {
    List<? extends Replacement<?>> replacements = Replacement.getAllOf(anIncumbent, "toto23");
    assertThat(replacements.isEmpty(), is(true));
  }

  @Test
  void getEmptyReplacementsByAUserInNonMatchingWorkflowInstance() {
    List<? extends Replacement<?>> replacements = Replacement.getAllBy(aSubstitute, "toto23");
    assertThat(replacements.isEmpty(), is(true));
  }
}
  
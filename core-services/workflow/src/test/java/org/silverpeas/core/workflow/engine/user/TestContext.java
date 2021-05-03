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

package org.silverpeas.core.workflow.engine.user;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.invocation.InvocationOnMock;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.test.rule.CommonAPITestRule;
import org.silverpeas.core.test.util.JpaMocker;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Context of unit tests on the delegation API.
 * @author mmoquillon
 */
public class TestContext extends CommonAPITestRule {

  static final String WORKFLOW_ID = "workflow42";
  private final User anIncumbent;
  private final User aSubstitute;
  // The actual replacement that is saved in the context of a given unit test
  final Mutable<ReplacementImpl> savedReplacement = Mutable.empty();

  public TestContext(final User anIncumbent, final User aSubstitute) {
    super();
    this.aSubstitute = anIncumbent;
    this.anIncumbent = aSubstitute;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        init();
        base.evaluate();
      }
    };
  }

  public void init() {
    try {
      CacheServiceProvider.clearAllThreadCaches();
      mockUserManager();
      mockReplacementConstructor();
      mockReplacementPersistence();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static User aUser(final String userId) {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(userId);
    return user;
  }

  private void mockReplacementConstructor() {
    injectIntoMockedBeanContainer(new ReplacementConstructor());
  }

  private void mockReplacementPersistence() {
    JpaMocker jpa = new JpaMocker(this);
    jpa.mockJPA(savedReplacement, ReplacementImpl.class);
    mockReplacementRepository(jpa, savedReplacement);
  }

  private void mockReplacementRepository(final JpaMocker jpaMocker,
      final Mutable<ReplacementImpl> savedReplacement) {
    // Mocking persistence repository of delegations...
    ReplacementRepository repository =
        jpaMocker.mockRepository(ReplacementRepository.class, savedReplacement);
    when(repository.findAllByIncumbentAndByWorkflow(any(User.class), anyString())).thenAnswer(
        invocation -> computeReplacementsFor(invocation, aSubstitute));
    when(repository.findAllBySubstituteAndByWorkflow(any(User.class), anyString())).thenAnswer(
        invocation -> computeReplacementsFor(invocation, anIncumbent));
    injectIntoMockedBeanContainer(repository);
  }

  private UserManager mockUserManager() throws WorkflowException {
    // Mocking the User providing mechanism
    UserManager userManager = mock(UserManager.class);
    when(userManager.getUser(anyString())).thenAnswer(invocation -> {
      String id = invocation.getArgument(0);
      if (aSubstitute.getUserId().equals(id)) {
        return aSubstitute;
      } else if (anIncumbent.getUserId().equals(id)) {
        return anIncumbent;
      } else {
        return aUser(id);
      }
    });
    injectIntoMockedBeanContainer(userManager);
    return userManager;
  }

  private Object computeReplacementsFor(final InvocationOnMock invocation,
      final User concernedUser) {
    LocalDate today = LocalDate.now();
    List<ReplacementImpl> replacements = new ArrayList<>();
    User user = invocation.getArgument(0);
    String workflowId = invocation.getArgument(1);
    if (concernedUser.getUserId().equals(user.getUserId()) && WORKFLOW_ID.equals(workflowId)) {
      replacements.add(Replacement.between(aSubstitute, anIncumbent)
          .inWorkflow(workflowId)
          .during(Period.between(today.plusDays(1), today.plusDays(8))));
    }
    return replacements;
  }
}
  
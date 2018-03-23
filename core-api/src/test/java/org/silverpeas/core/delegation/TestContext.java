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

package org.silverpeas.core.delegation;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.invocation.InvocationOnMock;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;
import org.silverpeas.core.persistence.datasource.model.jpa.PersistenceIdentifierSetter;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.Mutable;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.test.TestUserProvider.aUser;

/**
 * Context of unit tests on the delegation API.
 * @author mmoquillon
 */
public class TestContext extends CommonAPI4Test {

  static final String COMPONENT_ID = "kmelia42";
  private final User aDelegate;
  private final User aDelegator;

  public TestContext(final User predefinedDelegator, final User predefinedDelegate) {
    super();
    this.aDelegator = predefinedDelegator;
    this.aDelegate = predefinedDelegate;
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
    mockUserProvider();
    mockDelegationPersistence();
  }

  private void mockDelegationPersistence() {
    // The delegation that is saved in the context of a given unit test
    final Mutable<Delegation> savedDelegation = Mutable.empty();

    mockJPA(savedDelegation);
    mockDelegationRepository(savedDelegation);
  }

  private void mockDelegationRepository(final Mutable<Delegation> savedDelegation) {
    // Mocking persistence repository of delegations...
    DelegationRepository repository = mock(DelegationRepository.class);
    when(repository.save(any(Delegation.class))).thenAnswer(invocation -> {
      Delegation delegation = invocation.getArgument(0);
      savedDelegation.set(PersistenceIdentifierSetter.setIdTo(delegation, UuidIdentifier.class));
      return savedDelegation.get();
    });
    when(repository.getByDelegator(any(User.class))).thenAnswer(invocation -> {
      List<Delegation> delegations = new ArrayList<>();
      User user = invocation.getArgument(0);
      if (aDelegator.getId().equals(user.getId())) {
        delegations.add(Delegation.ofRolesIn(COMPONENT_ID).between(aDelegator, aDelegate));
        delegations.add(Delegation.ofRolesIn("Workflow12").between(aDelegator, aDelegate));
      }
      return delegations;
    });
    when(repository.getByDelegatorAndComponentId(any(User.class), anyString())).thenAnswer(
        invocation -> computeDelegationsFor(invocation, aDelegator));
    when(repository.getByDelegate(any(User.class))).thenAnswer(invocation -> {
      List<Delegation> delegations = new ArrayList<>();
      User user = invocation.getArgument(0);
      if (aDelegate.getId().equals(user.getId())) {
        delegations.add(Delegation.ofRolesIn(COMPONENT_ID).between(aDelegator, aDelegate));
        delegations.add(Delegation.ofRolesIn("workflow12").between(aDelegator, aDelegate));
      }
      return delegations;
    });
    when(repository.getByDelegateAndComponentId(any(User.class), anyString())).thenAnswer(
        invocation -> computeDelegationsFor(invocation, aDelegate));

    injectIntoMockedBeanContainer(repository);
  }

  private void mockJPA(final Mutable<Delegation> savedDelegation) {
    EntityManagerProvider entityManagerProvider = mock(EntityManagerProvider.class);
    EntityManager entityManager = mock(EntityManager.class);
    when(entityManagerProvider.getEntityManager()).thenReturn(entityManager);
    // below is used when checking a delegation is persisted: a delegation is returned only if
    // the identifier passed as argument matches the delegation that was saved.
    when(entityManager.find(eq(Delegation.class), any(UuidIdentifier.class))).thenAnswer(
        invocation -> {
          UuidIdentifier id = invocation.getArgument(1);
          Delegation delegation = null;
          if (savedDelegation.isPresent()) {
            delegation =
                savedDelegation.get().getId().equals(id.asString()) ? savedDelegation.get() : null;
          }
          return delegation;
        });
    injectIntoMockedBeanContainer(entityManagerProvider);
    injectIntoMockedBeanContainer(new Transaction());
  }

  private UserProvider mockUserProvider() {
    // Mocking the User providing mechanism
    UserProvider userProvider = mock(UserProvider.class);
    when(userProvider.getUser(anyString())).thenAnswer(invocation -> {
      String id = invocation.getArgument(0);
      if (aDelegator.getId().equals(id)) {
        return aDelegator;
      } else if (aDelegate.getId().equals(id)) {
        return aDelegate;
      } else {
        return aUser(id);
      }
    });
    injectIntoMockedBeanContainer(userProvider);
    return userProvider;
  }

  private Object computeDelegationsFor(final InvocationOnMock invocation,
      final User concernedUser) {
    List<Delegation> delegations = new ArrayList<>();
    User user = invocation.getArgument(0);
    String componentId = invocation.getArgument(1);
    if (concernedUser.getId().equals(user.getId()) && COMPONENT_ID.equals(componentId)) {
      delegations.add(Delegation.ofRolesIn(COMPONENT_ID).between(aDelegator, aDelegate));
    }
    return delegations;
  }
}
  
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

import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.admin.user.model.User;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.silverpeas.core.delegation.TestContext.COMPONENT_ID;
import static org.silverpeas.core.test.TestUserProvider.aUser;

/**
 * Unit tests on the delegation of responsibilities from users to another users in Silverpeas.
 * @author mmoquillon
 */
public class DelegationTest {

  private final User aDelegator = aUser("3");
  private final User aDelegate = aUser("5");

  @Rule
  public TestContext ctx = new TestContext(aDelegator, aDelegate);


  @Test
  public void createADelegationOfRolesFromAUserToAnotherOne() {
    Delegation delegation = Delegation.ofRolesIn(COMPONENT_ID).from(aDelegator).to(aDelegate);
    assertThat(delegation.getDelegator(), is(aDelegator));
    assertThat(delegation.getDelegate(), is(aDelegate));
    assertThat(delegation.getComponentInstanceId(), is(COMPONENT_ID));
  }

  @Test
  public void createADelegationOfRolesBetweenTwoUsers() {
    Delegation delegation = Delegation.ofRolesIn(COMPONENT_ID).between(aDelegator, aDelegate);
    assertThat(delegation.getDelegator(), is(aDelegator));
    assertThat(delegation.getDelegate(), is(aDelegate));
    assertThat(delegation.getComponentInstanceId(), is(COMPONENT_ID));
  }

  @Test(expected = NullPointerException.class)
  public void createBadlyADelegationByMissingTheDelegator() {
    Delegation.ofRolesIn(COMPONENT_ID).to(aDelegate);
  }

  @Test(expected = NullPointerException.class)
  public void createBadlyADelegationWithANullDelegate() {
    new Delegation(COMPONENT_ID, aDelegator, null);
  }

  @Test(expected = AssertionError.class)
  public void createBadlyADelegationForNoComponentInstance() {
    new Delegation(null, aUser("3"), aUser("5"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createADelegationToHimself() {
    new Delegation(COMPONENT_ID, aDelegator, aDelegator);
  }

  @Test
  public void saveANewDelegation() {
    Delegation delegation =
        Delegation.ofRolesIn(COMPONENT_ID).between(aDelegator, aDelegate).save();
    assertThat(delegation.isPersisted(), is(true));
  }

  @Test
  public void getAllEmptyDelegationsFromAUser() {
    List<Delegation> delegations = Delegation.getAllFrom(aDelegate);
    assertThat(delegations.isEmpty(), is(true));
  }

  @Test
  public void getAllDelegationsFromAUser() {
    List<Delegation> delegations = Delegation.getAllFrom(aDelegator);
    assertThat(delegations.isEmpty(), is(false));
  }

  @Test
  public void getAllEmptyDelegationsToAUser() {
    List<Delegation> delegations = Delegation.getAllTo(aDelegator);
    assertThat(delegations.isEmpty(), is(true));
  }

  @Test
  public void getAllDelegationsToAUser() {
    List<Delegation> delegations = Delegation.getAllTo(aDelegate);
    assertThat(delegations.isEmpty(), is(false));
  }

  @Test
  public void getAllDelegationsInGivenComponentInstanceFromAUser() {
    List<Delegation> delegations = Delegation.getAllFrom(aDelegator, COMPONENT_ID);
    assertThat(delegations.isEmpty(), is(false));
  }

  @Test
  public void getEmptyDelegationsInGivenComponentInstanceFromAUser() {
    List<Delegation> delegations = Delegation.getAllFrom(aDelegator, "toto23");
    assertThat(delegations.isEmpty(), is(true));
  }

  @Test
  public void getAllDelegationsInGivenComponentInstanceToAUser() {
    List<Delegation> delegations = Delegation.getAllTo(aDelegate, COMPONENT_ID);
    assertThat(delegations.isEmpty(), is(false));
  }

  @Test
  public void getEmptyDelegationsInGivenComponentInstanceToAUser() {
    List<Delegation> delegations = Delegation.getAllTo(aDelegate, "toto23");
    assertThat(delegations.isEmpty(), is(true));
  }
}
  
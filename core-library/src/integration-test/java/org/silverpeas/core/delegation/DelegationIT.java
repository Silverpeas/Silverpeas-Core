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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.util.SQLRequester;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests on the delegation business mechanism.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class DelegationIT {

  private static final String DATABASE_SCRIPT =
      "/org/silverpeas/core/delegation/create-database.sql";
  private static final String DATASET_SCRIPT = "/org/silverpeas/core/delegation/create-dataset.sql";

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(DATABASE_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DelegationIT.class)
        .addAdministrationFeatures()
        .addJpaPersistenceFeatures()
        .addPackages(true, "org.silverpeas.core.delegation")
        .build();
  }

  @Test
  public void getNoDelegationsFromAUserWithoutAnyDelegations() {
    List<Delegation> delegations = Delegation.getAllFrom(User.getById("0"));
    assertThat(delegations.isEmpty(), is(true));
  }

  @Test
  public void getNoDelegationsToAUserWithoutAnyDelegations() {
    List<Delegation> delegations = Delegation.getAllTo(User.getById("0"));
    assertThat(delegations.isEmpty(), is(true));
  }

  @Test
  public void saveNewDelegationsBetweenTwoUsersShouldPersistAllOfItsData() throws SQLException {
    OperationContext.fromUser("0");
    DelegatedResponsibility
        delegatedResponsibility = new DelegatedResponsibility(SilverpeasRole.publisher, "kmelia42");
    Delegation savedDelegation =
        Delegation.of(delegatedResponsibility).between(User.getById("0"), User.getById("1")).save();

    Map<String, Object> actualDelegation =
        SQLRequester.findOne("select * from sb_delegations where id = ?", savedDelegation.getId());
    assertThat(actualDelegation.isEmpty(), is(false));
    assertThat(savedDelegation.getDelegator().getId(), is(actualDelegation.get("DELEGATORID")));
    assertThat(savedDelegation.getDelegate().getId(), is(actualDelegation.get("DELEGATEID")));
    assertThat(savedDelegation.getResponsibility().getComponentInstanceId(),
        is(actualDelegation.get("INSTANCEID")));
    assertThat(savedDelegation.getResponsibility().getRole().toString(),
        is(actualDelegation.get("ROLE")));
  }

  @Test
  public void getDelegationsFromAnExistingDelegatorShouldReturnAllOfThem() {
    final String delegatorId = "1";
    List<Delegation> delegations = Delegation.getAllFrom(User.getById(delegatorId));
    assertThat(delegations.isEmpty(), is(false));
    assertThat(delegations.size(), is(3));
    assertThat(delegations.stream().allMatch(d -> d.getDelegator().getId().equals(delegatorId)),
        is(true));
  }

  @Test
  public void getDelegationsToAnExistingDelegateShouldReturnAllOfThem() {
    final String delegateId = "2";
    List<Delegation> delegations = Delegation.getAllTo(User.getById(delegateId));
    assertThat(delegations.isEmpty(), is(false));
    assertThat(delegations.size(), is(2));
    assertThat(delegations.stream().allMatch(d -> d.getDelegate().getId().equals(delegateId)),
        is(true));
  }

  @Test
  public void getDelegationsInAComponentInstanceFromAnExistingDelegateShouldReturnAllOfThem() {
    final String delegatorId = "1";
    final String componentId = "workflow32";
    List<Delegation> delegations = Delegation.getAllFrom(User.getById(delegatorId), componentId);
    assertThat(delegations.isEmpty(), is(false));
    assertThat(delegations.size(), is(2));
    assertThat(delegations.stream()
        .allMatch(d -> d.getDelegator().getId().equals(delegatorId) &&
            d.getResponsibility().getComponentInstanceId().equals(componentId)), is(true));
  }

  @Test
  public void getDelegationsInAComponentInstanceToAnExistingDelegateShouldReturnAllOfThem() {
    final String delegateId = "2";
    final String componentId = "workflow32";
    List<Delegation> delegations = Delegation.getAllTo(User.getById(delegateId), componentId);
    assertThat(delegations.isEmpty(), is(false));
    assertThat(delegations.size(), is(1));
    assertThat(delegations.stream()
        .allMatch(d -> d.getDelegate().getId().equals(delegateId) &&
            d.getResponsibility().getComponentInstanceId().equals(componentId)), is(true));
  }
}
  
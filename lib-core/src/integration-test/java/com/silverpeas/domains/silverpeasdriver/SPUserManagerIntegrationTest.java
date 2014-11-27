/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.domains.silverpeasdriver;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.test.WarBuilder4LibCore;
import org.silverpeas.test.rule.DbSetupRule;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class SPUserManagerIntegrationTest {

  public static final String TABLE_CREATION_SCRIPT =
      "/com/silverpeas/domains/silverpeasdriver/create_table.sql";
  public static final Operation SPUSER_INSERTION = Operations.insertInto("DomainSP_User")
      .columns("id", "firstName", "lastName", "login", "password", "company")
      .values(0, "Toto", "Chez-les-Papoos", "toto", "toto", "Silverpeas").build();

  @Inject
  private SPUserManager userManager;

  @PersistenceContext
  private EntityManager entityManager;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(SPUSER_INSERTION);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarFor(SPUserManagerIntegrationTest.class)
        .addJpaPersistenceFeatures()
        .testFocusedOn(
            (warBuilder) -> warBuilder.addClasses(SPUserManager.class, SPUserJpaManager.class,
                SPUser.class, SPGroup.class))
        .build();
  }

  @Test
  public void emptyTest() {
  }

  @Test
  public void createANewSPUser() throws Exception {
    SPUser expected = Transaction.performInOne(() -> {
      SPUser newUser = new SPUser();
      newUser.setFirstname("Titi");
      newUser.setLastname("Gros-Minet");
      newUser.setLogin("titi");
      newUser.setPassword("titi");
      return userManager.save(newUser);
    });

    SPUser actual = entityManager.find(SPUser.class, UniqueIntegerIdentifier.from(1));
    assertThat(actual, not(sameInstance(expected)));
    assertThat(actual, notNullValue());
    assertThat(actual.getId(), is(expected.getId()));
    assertThat(actual.getFirstname(), is(expected.getFirstname()));
    assertThat(actual.getLastname(), is(endsWith(expected.getLastname())));
    assertThat(actual.getLogin(), is(expected.getLogin()));
    assertThat(actual.getPassword(), is(expected.getPassword()));
  }

  @Test
  public void getExistingSPUsersByFirstName() throws Exception {
    List<SPUser> users = userManager.findByFirstname("Toto");
    assertThat(users.isEmpty(), is(false));

    SPUser actual = entityManager.find(SPUser.class, UniqueIntegerIdentifier.from(0));
    assertThat(actual.getId(), is("0"));
    assertThat(actual.getFirstname(), is("Toto"));
    assertThat(actual.getLastname(), is("Chez-les-Papoos"));
    assertThat(actual.getLogin(), is("toto"));
    assertThat(actual.getPassword(), is("toto"));
    assertThat(actual.getCompany(), is("Silverpeas"));
  }

  @Test
  public void getExistingSPUsersByLastName() throws Exception {
    List<SPUser> users = userManager.findByLastname("Chez-les-Papoos");
    assertThat(users.isEmpty(), is(false));

    SPUser actual = entityManager.find(SPUser.class, UniqueIntegerIdentifier.from(0));
    assertThat(actual.getId(), is("0"));
    assertThat(actual.getFirstname(), is("Toto"));
    assertThat(actual.getLastname(), is("Chez-les-Papoos"));
    assertThat(actual.getLogin(), is("toto"));
    assertThat(actual.getPassword(), is("toto"));
    assertThat(actual.getCompany(), is("Silverpeas"));
  }

  @Test
  public void getExistingSPUsersByCompany() throws Exception {
    List<SPUser> users = userManager.findByCompany("Silverpeas");
    assertThat(users.isEmpty(), is(false));

    SPUser actual = entityManager.find(SPUser.class, UniqueIntegerIdentifier.from(0));
    assertThat(actual.getId(), is("0"));
    assertThat(actual.getFirstname(), is("Toto"));
    assertThat(actual.getLastname(), is("Chez-les-Papoos"));
    assertThat(actual.getLogin(), is("toto"));
    assertThat(actual.getPassword(), is("toto"));
    assertThat(actual.getCompany(), is("Silverpeas"));
  }
}

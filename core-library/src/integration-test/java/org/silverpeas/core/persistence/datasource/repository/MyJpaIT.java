/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.datasource.repository;


import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOIT;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class MyJpaIT {

  public static final String TABLES_CREATION =
      "/org/silverpeas/core/persistence/datasource/create_table.sql";
  public static final Operation PERSON_SET_UP = Operations.insertInto("persons")
      .columns("id", "firstName", "lastName")
      .values(1L, "Yohann", "Chastagnier")
      .values(2L, "Nicolas", "Eysseric")
      .values(3L, "Miguel", "Moquillon")
      .build();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLES_CREATION)
      .loadInitialDataSetFrom(PERSON_SET_UP);

  @Inject
  private MyPersonRepository repository;

  @Deployment
  public static Archive<?> createTestArchive() {
    return LibCoreWarBuilder.onWarForTestClass(SilverpeasBeanDAOIT.class)
        .addPackages(true, "org.silverpeas.core.persistence.datasource.repository")
        .addAsResource("org/silverpeas/core/persistence/datasource/create_table.sql")
        .build();
  }

  @Test
  public void findAnExistingPerson() {
    var mayBePerson = repository.findById(1L);
    assertThat(mayBePerson.isPresent(), is(true));

    var person = mayBePerson.get();
    assertThat(person.getFirstName(), is("Yohann"));
    assertThat(person.getLastName(), is("Chastagnier"));
  }

  @Test
  public void saveANewPerson() {
    MyPerson person = new MyPerson("Aurore", "Allibe");
    var saved = repository.save(person);
    assertThat(saved.isPersisted(), is(true));
    assertThat(saved.getId(), is(person.getId()));
    assertThat(saved.getFirstName(), is(person.getFirstName()));
    assertThat(saved.getLastName(), is(person.getLastName()));
  }

  @Test
  public void updateExistingPerson() {
    Transaction.performInOne(() -> {
      var mayBePerson = repository.findById(3L);
      assertThat(mayBePerson.isPresent(), is(true));
      var person = mayBePerson.get();
      person.setFirstName("Laurence");
      return repository.save(person);
    });

    var mayBePerson = repository.findById(3L);
    assertThat(mayBePerson.isPresent(), is(true));
    var person = mayBePerson.get();
    assertThat(person.getFirstName(), is("Laurence"));
    assertThat(person.getLastName(), is("Moquillon"));
  }
}
  
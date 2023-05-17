/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.persistence;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.repository.jpa.JpaEntityServiceTest;
import org.silverpeas.core.persistence.datasource.repository.jpa.model.Person;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import java.sql.Connection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.silverpeas.core.test.integration.rule.DbSetupRule.getActualDataSet;
import static org.silverpeas.core.test.integration.rule.DbSetupRule.getSafeConnection;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class TransactionIT {

  private JpaEntityServiceTest jpaEntityServiceTest;

  public static final String TABLES_CREATION =
      "/org/silverpeas/core/persistence/datasource/create_table.sql";
  private static final Operation PERSON_SET_UP = Operations.insertInto("test_persons")
      .columns("id", "firstName", "lastName", "birthday", "createDate", "createdBy",
          "lastUpdateDate", "lastUpdatedBy", "version")
      .values("person_1", "Yohann", "Chastagnier", "1980-01-01", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L)
      .values("person_2", "Nicolas", "Eysseric", "1980-01-01", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L)
      .values("person_3", "Miguel", "Moquillon", "1971-11-30", "2013-11-21 09:57:30.003", "2",
          "2013-11-22 22:00:50.006", "10", 3L)
      .values("person_1000", "firstName", "lastName", "1980-01-01", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L)
      .values("person_1001", "firstName", "lastName", "1980-01-01", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L).build();
  private static final Operation ANIMAL_SET_UP = Operations.insertInto("test_animals")
      .columns("id", "type", "name", "personId", "createDate", "createdBy", "lastUpdateDate",
          "lastUpdatedBy", "version")
      .values(1L, "cat", "Blacky", "person_1", "2013-11-21 09:57:30.003", "1",
          "2013-11-22 22:00:50.006", "2", 2L)
      .values(2L, "dog", "Bagels", "person_2", "2013-11-21 09:57:30.003", "10",
          "2013-11-21 09:57:30.003", "10", 0L)
      .values(3L, "bird", "Titi", "person_2", "2013-11-21 09:57:30.003", "10",
          "2013-11-21 09:57:30.003", "10", 0L)
      .values(1000L, "type", "name", "person_1000", "2013-11-21 09:57:30.003", "10",
          "2013-11-21 09:57:30.003", "10", 0L)
      .values(1001L, "type", "name", "person_1001", "2013-11-21 09:57:30.003", "10",
          "2013-11-21 09:57:30.003", "10", 0L)
      .build();
  private static final Operation EQUIPEMENT_SET_UP = Operations.insertInto("test_equipments")
      .columns("id", "name", "animalId", "startDate", "endDate", "inDays", "createDate",
          "createdBy", "lastUpdateDate", "lastUpdatedBy", "version")
      .values("equipment_1", "necklace", 2L, "0001-01-01 00:00:00.000", "9999-12-31 00:00:00.000",
          1, "2013-11-21 09:57:30.003", "1", "2013-11-22 22:00:50.006", "2", 10L)
      .build();
  private static final Operation UNIQUE_ID_SET_UP = Operations.insertInto("UniqueId")
      .columns("maxId", "tableName")
      .values(9, "test_animals")
      .build();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLES_CREATION)
      .loadInitialDataSetFrom(PERSON_SET_UP, ANIMAL_SET_UP, EQUIPEMENT_SET_UP, UNIQUE_ID_SET_UP);

  @Before
  public void setup() {
    jpaEntityServiceTest = ServiceProvider.getService(JpaEntityServiceTest.class);
    OperationContext.fromUser("0");
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(TransactionIT.class)
        .addJpaPersistenceFeatures()
        .addStubbedOrganizationController()
        .addPublicationTemplateFeatures()
        .testFocusedOn((warBuilder) -> warBuilder
            .addPackages(true, "org.silverpeas.core.persistence.datasource.repository.jpa"))
        .build();
  }

  @Test
  public void transactionSuccess() throws Exception {
    final Person person = jpaEntityServiceTest.getPersonById("person_1");
    assertThat(person, notNullValue());

    try(Connection connection = getSafeConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable table = actualDataSet.getTable("test_persons");
      int index = getTableIndexForId(table, person.getId());
      assertThat(table.getValue(index, "id"), is("person_1"));
      assertThat(table.getValue(index, "id"), is(person.getId()));
      assertThat(table.getValue(index, "firstName"), is("Yohann"));
      assertThat(table.getValue(index, "firstName"), is(person.getFirstName()));
      assertThat(table.getValue(index, "lastUpdatedBy"), is("1"));
      assertThat(table.getValue(index, "lastUpdatedBy"), is(person.getLastUpdaterId()));
    }

      // Modifying person data
      person.setFirstName("UnknownFirstName");

      Transaction transaction = Transaction.getTransaction();
      transaction.perform(() -> {
        person.lastUpdatedBy("26");
        jpaEntityServiceTest.save(person);
        return null;
      });

    try(Connection connection = getSafeConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable table = actualDataSet.getTable("test_persons");
      int index = getTableIndexForId(table, person.getId());
      assertThat(table.getValue(index, "id"), is("person_1"));
      assertThat(table.getValue(index, "firstName"), is("UnknownFirstName"));
      assertThat(table.getValue(index, "lastUpdatedBy"), is("26"));
    }
  }

  @Test
  public void transactionError() throws Exception {
    final Person person = jpaEntityServiceTest.getPersonById("person_1");
    assertThat(person, notNullValue());

    try(Connection connection = getSafeConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable table = actualDataSet.getTable("test_persons");
      int index = getTableIndexForId(table, person.getId());
      assertThat(table.getValue(index, "id"), is("person_1"));
      assertThat(table.getValue(index, "id"), is(person.getId()));
      assertThat(table.getValue(index, "firstName"), is("Yohann"));
      assertThat(table.getValue(index, "firstName"), is(person.getFirstName()));
      assertThat(table.getValue(index, "lastUpdatedBy"), is("1"));
      assertThat(table.getValue(index, "lastUpdatedBy"), is(person.getLastUpdaterId()));
    }

      // Modifying person data
      person.setFirstName("UnknownFirstName");

      boolean exceptionThrown = false;
      try {
        Transaction transaction = Transaction.getTransaction();
        transaction.perform(() -> {
          person.lastUpdatedBy("26");
          jpaEntityServiceTest.save(person);
          jpaEntityServiceTest.flush();
          throw new IllegalArgumentException("ExpectedTransactionError");
        });
      } catch (TransactionRuntimeException e) {
        assertThat(e.getMessage(), is("java.lang.IllegalArgumentException: ExpectedTransactionError"));
        exceptionThrown = true;
      }
      assertThat(exceptionThrown, is(true));

    try(Connection connection = getSafeConnection()) {
      IDataSet actualDataSet = getActualDataSet(connection);
      ITable table = actualDataSet.getTable("test_persons");
      int index = getTableIndexForId(table, person.getId());
      assertThat(table.getValue(index, "id"), is("person_1"));
      assertThat(table.getValue(index, "firstName"), is("Yohann"));
      assertThat(table.getValue(index, "lastUpdatedBy"), is("1"));
    }
  }

  private int getTableIndexForId(ITable table, Object id) throws Exception {
    for (int i = 0; i < table.getRowCount(); i++) {
      if (id.equals(table.getValue(i, "id"))) {
        return i;
      }
    }
    return -1;
  }

}

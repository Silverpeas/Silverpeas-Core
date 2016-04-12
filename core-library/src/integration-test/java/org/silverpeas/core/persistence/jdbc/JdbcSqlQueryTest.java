/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.jdbc;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Insert;
import com.ninja_squad.dbsetup.operation.Operation;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlExecutorProvider;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.persistence.jdbc.sql.ResultSetWrapper;
import org.silverpeas.core.persistence.jdbc.sql.SelectResultRowProcess;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.*;

@RunWith(Arquillian.class)
public class JdbcSqlQueryTest {

  public static final Operation TABLES_CREATION = Operations
      .sql("CREATE TABLE a_table (id int8 PRIMARY KEY NOT NULL , value varchar(50) NOT NULL)");
  public static final Operation TABLE_SET_UP;

  private final static long NB_ROW_AT_BEGINNING = 100L;

  static {
    Insert.Builder insertBulider = Operations.insertInto("a_table").columns("id", "value");
    for (long l = 0; l < NB_ROW_AT_BEGINNING; l++) {
      insertBulider.values(l, "value_" + l);
    }
    TABLE_SET_UP = insertBulider.build();
  }

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create_table_favorit_space.sql")
      .loadInitialDataSetFrom(TABLES_CREATION, TABLE_SET_UP);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(JdbcSqlQueryTest.class)
        .addCommonBasicUtilities()
        .addSilverpeasExceptionBases()
        .build();
  }

  @Test
  public void deletionCommitWithExceptionAfterTransaction() throws Exception {
    List<String> result = executeMultiThreadedOperations(false);
    assertThat(String.join(System.lineSeparator(), result), is(String.join(System.lineSeparator(),
        "DELETION PROCESS - In transaction",
        "DELETION PROCESS - Waiting for verification",
        "VERIFICATION PROCESS - BEFORE DELETION - " + NB_ROW_AT_BEGINNING + " line(s)",
        "DELETION PROCESS - Waiting for verification (lines deleted)",
        "VERIFICATION PROCESS - AFTER DELETION - " + NB_ROW_AT_BEGINNING + " line(s)",
        "DELETION PROCESS - Transaction closed",
        "VERIFICATION PROCESS - AFTER TRANSACTION - 0 line(s)")));
  }


  @Test
  public void deletionRollbackWithExceptionDuringTransaction() throws Exception {
    List<String> result = executeMultiThreadedOperations(true);
    assertThat(String.join(System.lineSeparator(), result), is(String.join(System.lineSeparator(),
        "DELETION PROCESS - In transaction",
        "DELETION PROCESS - Waiting for verification",
        "VERIFICATION PROCESS - BEFORE DELETION - " + NB_ROW_AT_BEGINNING + " line(s)",
        "DELETION PROCESS - Waiting for verification (lines deleted)",
        "VERIFICATION PROCESS - AFTER DELETION - " + NB_ROW_AT_BEGINNING + " line(s)",
        "DELETION PROCESS - Perform rollback", "DELETION PROCESS - Rollback performed",
        "DELETION PROCESS - Transaction closed",
        "VERIFICATION PROCESS - AFTER TRANSACTION - " + NB_ROW_AT_BEGINNING + " line(s)")));
  }

  @SuppressWarnings("UnusedDeclaration")
  private List<String> executeMultiThreadedOperations(final boolean rollbackInTransaction)
      throws Exception {
    final Object monitorOfDeletion = new Object();
    final Object monitorOfVerification = new Object();
    assertThat(getTableLines(), hasSize((int) NB_ROW_AT_BEGINNING));
    final List<String> result = new ArrayList<>();
    Thread deletionProcess = new Thread(() -> {
      try {
        Transaction.performInOne(() -> {
          try (final Connection connection = dbSetupRule.getSafeConnectionFromDifferentThread()) {
            Transaction.performInOne(() -> {
              try (Connection otherConnection = dbSetupRule.getSafeConnectionFromDifferentThread()) {
                // Just opening two connections for pleasure :-)
                Thread.sleep(50);
                synchronized (monitorOfVerification) {
                  info(result, "DELETION PROCESS - In transaction");
                  info(result, "DELETION PROCESS - Waiting for verification");
                  monitorOfVerification.notifyAll();
                }
                synchronized (monitorOfDeletion) {
                  monitorOfDeletion.wait(500);
                }
                Thread.sleep(10);
                synchronized (monitorOfVerification) {
                  assertThat(createDeleteFor("a_table").execute(), is(NB_ROW_AT_BEGINNING));
                  info(result, "DELETION PROCESS - Waiting for verification (lines deleted)");
                  monitorOfVerification.notifyAll();
                }
                synchronized (monitorOfDeletion) {
                  monitorOfDeletion.wait(500);
                }
                Thread.sleep(10);
              }
              return null;
            });
          }
          assertThat(getTableLines(), hasSize(0));
          if (rollbackInTransaction) {
            info(result, "DELETION PROCESS - Perform rollback");
            throw new IllegalArgumentException();
          }
          return null;
        });
      } catch (Exception e) {
        info(result, "DELETION PROCESS - Rollback performed");
      }
      synchronized (monitorOfVerification) {
        info(result, "DELETION PROCESS - Transaction closed");
        monitorOfVerification.notifyAll();
      }
    });
    Thread deletionVerifications = new Thread(() -> {
      try {
        synchronized (monitorOfVerification) {
          monitorOfVerification.wait(500);
        }
        Thread.sleep(10);
        synchronized (monitorOfDeletion) {
          info(result,
              "VERIFICATION PROCESS - BEFORE DELETION - " + getTableLines().size() + " line(s)");
          monitorOfDeletion.notifyAll();
        }
        synchronized (monitorOfVerification) {
          monitorOfVerification.wait(500);
        }
        Thread.sleep(10);
        synchronized (monitorOfDeletion) {
          info(result,
              "VERIFICATION PROCESS - AFTER DELETION - " + getTableLines().size() + " line(s)");
          monitorOfDeletion.notifyAll();
        }
        synchronized (monitorOfVerification) {
          monitorOfVerification.wait(500);
        }
        Thread.sleep(10);
        synchronized (monitorOfDeletion) {
          info(result,
              "VERIFICATION PROCESS - AFTER TRANSACTION - " + getTableLines().size() + " line(s)");
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
    List<Thread> threads = Arrays.asList(deletionVerifications, deletionProcess);
    for (Thread thread : threads) {
      thread.start();
    }
    for (Thread thread : threads) {
      thread.join(2000);
    }
    return result;
  }

  private void info(List<String> logContainer, String message) {
    logContainer.add(message);
    Logger.getAnonymousLogger().info("[" + Thread.currentThread().getId() + "] " + message);
  }

  @Test
  public void selectCountVerifications() throws SQLException {
    assertThat(createCountFor("a_table").execute(), is(NB_ROW_AT_BEGINNING));
    assertThat(createCountFor("a_table").where("id != ?", 8).execute(),
        is(NB_ROW_AT_BEGINNING - 1));
    assertThat(createCountFor("a_table").where("id != ?", 8).and("LENGTH(value) <= ?", 7).execute(),
        is(9l));
  }

  @Test
  public void selectAll() throws SQLException {
    List<Pair<Long, String>> rows =
        createSelect("* from a_table").execute(new TableResultProcess());
    long l = 0;
    assertThat(rows, hasSize((int) NB_ROW_AT_BEGINNING));
    for (Pair<Long, String> row : rows) {
      assertThat(row.getLeft(), is(l));
      l++;
    }

    int resultLimit = (int) (NB_ROW_AT_BEGINNING - 10);
    rows = createSelect("* from a_table").configure(config -> config.withResultLimit(resultLimit))
        .execute(new TableResultProcess());
    assertThat(rows, hasSize(resultLimit));
  }

  @Test(expected = IllegalArgumentException.class)
  public void selectAllButUsingUnique() throws SQLException {
    createSelect("* from a_table").executeUnique(new TableResultProcess());
  }

  @Test
  public void selectOneParameter() throws SQLException {
    final String sqlQuery = "* from a_table where id = ?";
    List<Pair<Long, String>> rows = createSelect(sqlQuery, 30).execute(new TableResultProcess());
    assertThat(rows, hasSize(1));
    assertThat(unique(rows).getLeft(), is(30L));
    rows = createSelect(sqlQuery, 200).execute(new TableResultProcess());
    assertThat(rows, hasSize(0));
    assertThat(unique(rows), nullValue());
  }

  @Test
  public void selectUsingOneAppendParameter() throws SQLException {
    List<Pair<Long, String>> rows =
        createSelect("* from a_table where id = ?", 26).execute(new TableResultProcess());
    assertThat(rows, hasSize(1));
    assertThat(unique(rows).getLeft(), is(26L));
  }

  @Test
  public void selectUsingTwoAppendParametersAndAppendListOfParameters() throws SQLException {
    JdbcSqlQuery sqlQuery = createSelect("* from a_table where (id = ?", 26);
    sqlQuery.or("LENGTH(value) <= ?)", 7);
    sqlQuery.or("id").in(38, 39, 40);
    List<Pair<Long, String>> rows = sqlQuery.execute(new TableResultProcess());
    assertThat(rows, hasSize(14));
  }

  @Test
  public void selectUsingAppendListOfParameters() throws SQLException {
    List<Pair<Long, String>> rows =
        createSelect("* from a_table where id").in(38, 39, 40).execute(new TableResultProcess());
    assertThat(rows, hasSize(3));
  }

  private static class TableResultProcess implements SelectResultRowProcess<Pair<Long, String>> {
    @Override
    public Pair<Long, String> currentRow(final ResultSetWrapper row) throws SQLException {
      return Pair.of(row.getLongObject(1), row.getString(2));
    }
  }

  @Test
  public void createRowUsingAppendSaveParameter() {
    assertThat(getTableLines(), hasSize(100));
    JdbcSqlQuery insertSqlQuery = JdbcSqlQuery.createInsertFor("a_table");
    insertSqlQuery.addInsertParam("id", 200);
    insertSqlQuery.addInsertParam("value", "value_200_inserted");
    Transaction.performInOne(() -> {
      long insertCount = insertSqlQuery.execute();
      assertThat(insertCount, is(1L));
      return null;
    });
    assertThat(getTableLines(), hasSize(101));
    assertThat(getTableLines().get(100), is("200@value_200_inserted"));
  }

  @Test
  public void updateTwoRowsFromThreeUpdatesUsingAppendSaveParameter() {
    assertThat(getTableLines().get(0), is("0@value_0"));
    assertThat(getTableLines().get(26), is("26@value_26"));
    assertThat(getTableLines().get(38), is("38@value_38"));

    JdbcSqlQuery firstInsertSqlQuery = createUpdateFor("a_table");
    firstInsertSqlQuery.addUpdateParam("value", "value_26_updated");
    firstInsertSqlQuery.where("id = ?", 26);

    JdbcSqlQuery secondInsertSqlQuery = createUpdateFor("a_table");
    secondInsertSqlQuery.addUpdateParam("value", "value_38_updated");
    secondInsertSqlQuery.where("id = ?", 38);

    JdbcSqlQuery thirdInsertSqlQuery = createUpdateFor("a_table");
    thirdInsertSqlQuery.addUpdateParam("value", "value_200_updated");
    thirdInsertSqlQuery.where("id = ?", 200);

    Transaction.performInOne(() -> {
      long updateCount = JdbcSqlExecutorProvider.getJdbcSqlExecutor()
          .executeModify(firstInsertSqlQuery, secondInsertSqlQuery, thirdInsertSqlQuery);
      assertThat(updateCount, is(2L));
      return null;
    });

    assertThat(getTableLines().get(0), is("0@value_0"));
    assertThat(getTableLines().get(26), is("26@value_26_updated"));
    assertThat(getTableLines().get(38), is("38@value_38_updated"));
  }

  @Test
  public void deleteRows() {
    assertThat(getTableLines(), hasSize(100));
    Transaction.performInOne(() -> {
      long deleteCount = createDeleteFor("a_table").where("LENGTH(value) <= ?", 7).execute();
      assertThat(deleteCount, is(10L));
      return null;
    });
    assertThat(getTableLines(), hasSize(90));
  }

  @Test
  public void dropTableA() throws SQLException {
    assertThat(createCountFor("INFORMATION_SCHEMA.TABLES").where("lower(TABLE_NAME) = ?", "a_table")
        .execute(), is(1L));
    Transaction.performInOne(() -> {
      createDropFor("a_table").execute();
      return null;
    });
    assertThat(createCountFor("INFORMATION_SCHEMA.TABLES").where("lower(TABLE_NAME) = ?", "a_table")
        .execute(), is(0L));
  }

  @Test
  public void createTableB() throws SQLException {
    assertThat(createCountFor("INFORMATION_SCHEMA.TABLES").where("lower(TABLE_NAME) = ?", "b_table")
        .execute(), is(0L));
    Transaction.performInOne(() -> {
      createTable("b_table").addField("identifier", "integer primary key")
          .addField("description", "varchar(50) NOT NULL").execute();
      createInsertFor("b_table").addInsertParam("identifier", 26)
          .addInsertParam("description", "Drôme").execute();
      return null;
    });
    assertThat(createCountFor("INFORMATION_SCHEMA.TABLES").where("lower(TABLE_NAME) = ?", "b_table")
        .execute(), is(1L));
    assertThat(createCountFor("b_table").execute(), is(1L));
  }

  /**
   * Gets the content of a_table.
   * @return the content of a_table.
   */
  private List<String> getTableLines() {
    try (Connection connection = dbSetupRule.getSafeConnectionFromDifferentThread();
         PreparedStatement statement = connection
             .prepareStatement("SELECT * FROM a_table ORDER BY id");
         ResultSet resultSet = statement.executeQuery()) {
      List<String> result = new ArrayList<>();
      while (resultSet.next()) {
        result.add(resultSet.getLong(1) + "@" + resultSet.getString(2));
      }
      return result;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
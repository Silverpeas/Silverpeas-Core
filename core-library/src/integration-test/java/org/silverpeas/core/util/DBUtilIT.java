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
package org.silverpeas.core.util;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedThreadFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;

/**
 * Integration tests on some DBUtil capabilities.
 *
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class DBUtilIT {

  public static final Operation TABLES_CREATION = Operations.sql(
      "create table if not exists Users (id int primary key, firstName varchar(20) not null, " +
          "lastName varchar(20) not null)",
      "create table if not exists Toto (id int primary key)",
      "create table if not exists UniqueId (maxId int not null, tableName varchar(100) not null)");
  public static final Operation CLEAN_UP = Operations.deleteAllFrom("Users", "UniqueId");
  public static final Operation USER_SET_UP = Operations.insertInto("Users")
      .columns("id", "firstName", "lastName")
      .values(0, "Edouard", "Lafortin")
      .values(1, "Rohan", "Lapointe")
      .build();
  public static final Operation UNIQUE_ID_SET_UP = Operations.insertInto("UniqueId")
      .columns("maxId", "tableName")
      .values(1, "users") // don't forget the table name is set in lower case by DBUtil
      .build();

  private static final int THREAD_COUNT = 15;

  @Resource
  private ManagedThreadFactory managedThreadFactory;

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createDefaultTables()
      .loadInitialDataSetFrom(TABLES_CREATION, CLEAN_UP, USER_SET_UP, UNIQUE_ID_SET_UP);

  @Deployment
  public static Archive<?> createTestArchive() {
    return LibCoreWarBuilder.onWarForTestClass(DBUtilIT.class)
        .build();
  }

  @Test
  public void nextUniqueIdUpdateForAnExistingTableShouldWork() throws SQLException {
    assertThat(actualMaxIdInUniqueIdFor("Users"), is(1));
    int nextId = DBUtil.getNextId("Users", "id");
    assertThat(nextId, is(2));
    assertThat(actualMaxIdInUniqueIdFor("Users"), is(nextId));
  }

  @Test
  public void nextUniqueIdNotUpdatedIfRollbackIsPerformed() throws SQLException {
    final String select = "SELECT firstName FROM Users WHERE id = 1";
    final String update = "UPDATE Users SET firstName = 'updated first name' WHERE id = 1";

    assertThat(actualMaxIdInUniqueIdFor("Users"), is(1));
    boolean rollbackIsEffective = false;
    try {
      Transaction.performInOne(() -> {

        try (Connection connection = dbSetupRule.getSafeConnectionFromDifferentThread();
             PreparedStatement statement = connection.prepareStatement(select);
             ResultSet resultSet = statement.executeQuery()) {
          assertThat(resultSet.next(), is(true));
          assertThat(resultSet.getString(1), is("Rohan"));
        }

        try (Connection connection = dbSetupRule.getSafeConnectionFromDifferentThread();
             PreparedStatement statement = connection.prepareStatement(update)) {
          statement.executeUpdate();
        }

        try (Connection connection = dbSetupRule.getSafeConnectionFromDifferentThread();
             PreparedStatement statement = connection.prepareStatement(select);
             ResultSet resultSet = statement.executeQuery()) {
          assertThat(resultSet.next(), is(true));
          assertThat(resultSet.getString(1), is("updated first name"));
        }

        if (DBUtil.getNextId("Users", "id") != -1) {
          throw new SQLException("Simulating a rollback !");
        }
        return null;
      });
    } catch (Exception e) {
      rollbackIsEffective = true;
    }
    assertThat("Rollback must be effective !", rollbackIsEffective, is(true));
    assertThat("Next identifier must be roll backed", actualMaxIdInUniqueIdFor("Users"), is(1));

    try (Connection connection = dbSetupRule.getSafeConnectionFromDifferentThread();
         PreparedStatement statement = connection.prepareStatement(select);
         ResultSet resultSet = statement.executeQuery()) {
      assertThat(resultSet.next(), is(true));
      assertThat("Rollback must be effective whereas next id is validated...",
          resultSet.getString(1), is("Rohan"));
    }
  }

  @Test
  public void nextUniqueIdUpdateForAnExistingTableShouldWorkInRequiredTransaction()
      throws Exception {
    assertThat(actualMaxIdInUniqueIdFor("Users"), is(1));
    final Thread nextIdThread = managedThreadFactory.newThread(() -> {
      try {
        Transaction.performInOne(() -> {
          int nextId;
          nextId = DBUtil.getNextId("Users", "id");
          assertThat(actualMaxIdInUniqueIdFor("Users"), is(nextId));
          assertThat(nextId, is(2));
          return null;
        });
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    try {
      assertThat(actualMaxIdInUniqueIdFor("Users"), is(1));
      nextIdThread.start();
      nextIdThread.join();
      assertThat(actualMaxIdInUniqueIdFor("Users"), is(2));
    } finally {
      nextIdThread.interrupt();
    }
  }

  @Test
  public void nextUniqueIdUpdateForAnExistingTableShouldWorkAndConcurrency()
      throws SQLException, InterruptedException {
    int nextIdBeforeTesting = actualMaxIdInUniqueIdFor("Users");
    assertThat(nextIdBeforeTesting, is(1));
    int nbThreads = 2 + (int) (Math.random() * 10);
    Logger.getAnonymousLogger()
        .info("Start at " + System.currentTimeMillis() + " with " + nbThreads + " threads");
    final Thread[] threads = new Thread[nbThreads];
    for (int i = 0; i < nbThreads; i++) {
      threads[i] = managedThreadFactory.newThread(() -> {
        int nextId = DBUtil.getNextId("Users", "id");
        Logger.getAnonymousLogger()
            .info("Next id is " + nextId + " at " + System.currentTimeMillis());
        awaitUntil(100, MILLISECONDS);
      });
    }

    try {
      for (Thread thread : threads) {
        thread.start();
      }
      for (Thread thread : threads) {
        thread.join();
      }
      int expectedNextId = nextIdBeforeTesting + nbThreads;
      Logger.getAnonymousLogger()
          .info("Verifying nextId is " + expectedNextId + " at " + System.currentTimeMillis());
      assertThat(actualMaxIdInUniqueIdFor("Users"), is(expectedNextId));
    } finally {
      for (Thread thread : threads) {
        if (thread.isAlive()) {
          thread.interrupt();
        }
      }
    }
  }

  @Test
  public void nextUniqueIdUpdateForANewTableShouldWork() throws SQLException {
    try {
      actualMaxIdInUniqueIdFor("Toto");
    } catch (SQLException se) {
      int nextId = DBUtil.getNextId("Toto", "id");
      assertThat(nextId, is(1));
      assertThat(actualMaxIdInUniqueIdFor("Toto"), is(nextId));
      return;
    }
    fail("Table 'Toto' must not exist...");
  }

  @Test
  public void nextUniqueIdUpdateForANonExistingTableShouldWork() throws SQLException {
    try {
      actualMaxIdInUniqueIdFor("Tartempion");
    } catch (SQLException se) {
      int nextId = DBUtil.getNextId("Tartempion", "id");
      assertThat(nextId, is(1));
      assertThat(actualMaxIdInUniqueIdFor("Tartempion"), is(nextId));
      return;
    }
    fail("Table 'Tartempion' must not exist...");
  }

  private int actualMaxIdInUniqueIdFor(String tableName) throws SQLException {
    final String query = "select maxId from UniqueId where tableName = ?";
    int maxId;
    try (Connection connection = dbSetupRule.getSafeConnectionFromDifferentThread();
         PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, tableName.toLowerCase());
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          maxId = resultSet.getInt(1);
        } else {
          throw new SQLException("No result!");
        }
      }
    }
    return maxId;
  }

  @Test
  public void nextUniqueIdUpdateForAnExistingTablesShouldWorkAndConcurrency() throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      final List<Callable<Pair<String, Integer>>> listsOfGetNextId = getNextIds();
      ExecutorService executorService = Executors.newFixedThreadPool(10);
      List<org.apache.commons.lang3.tuple.Pair<String, Integer>> tableNextIdsInError =
          new ArrayList<>();
      try {
        for (Future<org.apache.commons.lang3.tuple.Pair<String, Integer>> aTreatment :
            executorService
                .invokeAll(listsOfGetNextId)) {
          org.apache.commons.lang3.tuple.Pair<String, Integer> tableIdValue = aTreatment.get();
          if (tableIdValue.getRight() != THREAD_COUNT) {
            if (tableNextIdsInError.isEmpty()) {
              Logger.getAnonymousLogger().severe("Some errors...");
            }
            tableNextIdsInError.add(tableIdValue);
            Logger.getAnonymousLogger()
                .severe("Next id value must be " + THREAD_COUNT + " for table " +
                    tableIdValue.getLeft() +
                    ", but was " + tableIdValue.getRight());
          }
        }
      } finally {
        executorService.shutdown();
      }
      if (!tableNextIdsInError.isEmpty()) {
        fail("The next id of " + tableNextIdsInError.size() + " tables is in error.");
      }
    } finally {
      Logger.getAnonymousLogger()
          .info("Test duration of " + (System.currentTimeMillis() - startTime) + " ms");
    }
  }

  private List<Callable<Pair<String, Integer>>> getNextIds() {
    final List<Object> count = new ArrayList<>();
    final List<Callable<Pair<String, Integer>>> listsOfGetNextId =
        new ArrayList<>(150);
    for (int i = 0; i < 150; i++) {
      listsOfGetNextId.add(() -> {
        String tableName;
        synchronized (count) {
          count.add("");
          tableName = "Users_" + count.size() + "_table";
        }
        return Pair.of(tableName,
            nextUniqueIdUpdateForAnExistingTableShouldWorkAndConcurrency(tableName));
      });
    }
    return listsOfGetNextId;
  }

  private int nextUniqueIdUpdateForAnExistingTableShouldWorkAndConcurrency(final String tableName)
      throws SQLException, InterruptedException {
    Logger.getAnonymousLogger().info(
        "Start at " + System.currentTimeMillis() + " with " + DBUtilIT.THREAD_COUNT + " threads " +
            "for table " +
            tableName);
    final Thread[] threads = new Thread[DBUtilIT.THREAD_COUNT];
    for (int i = 0; i < DBUtilIT.THREAD_COUNT; i++) {
      threads[i] = managedThreadFactory.newThread(() -> {
        try {
          int nextId = DBUtil.getNextId(tableName, "id");
          Logger.getAnonymousLogger().info("Next id for " + tableName + " is " + nextId + " at " +
              System.currentTimeMillis());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    }

    try {
      for (Thread thread : threads) {
        thread.start();
        awaitUntil(5, MILLISECONDS);
      }
      for (Thread thread : threads) {
        thread.join();
      }
      return actualMaxIdInUniqueIdFor(tableName);
    } finally {
      for (Thread thread : threads) {
        if (thread.isAlive()) {
          thread.interrupt();
        }
      }
    }
  }
}

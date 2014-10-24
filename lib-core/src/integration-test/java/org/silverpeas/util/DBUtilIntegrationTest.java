/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.util;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.TransactionProvider;
import org.silverpeas.util.pool.ConnectionPool;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Integration tests on some DBUtil capabilities.
 * @author mmoquillon
 */
@BenchmarkMethodChart
@RunWith(Arquillian.class)
public class DBUtilIntegrationTest {
  @Rule
  public TestRule benchmarkRun = new BenchmarkRule();

  public static final Operation TABLES_CREATION = Operations.sql(
      "create table if not exists User (id int not null primary key, firstName varchar(20) not null, lastName varchar(20) not null)",
      "create table if not exists Toto (id int not null primary key)",
      "create table if not exists UniqueId (maxId int not null, tableName varchar(100) not null)");
  public static final Operation CLEAN_UP = Operations.deleteAllFrom("User", "UniqueId");
  public static final Operation USER_SET_UP = Operations.insertInto("User")
      .columns("id", "firstName", "lastName")
      .values(0, "Edouard", "Lafortin")
      .values(1, "Rohan", "Lapointe")
      .build();
  public static final Operation UNIQUE_ID_SET_UP = Operations.insertInto("UniqueId")
      .columns("maxId", "tableName")
      .values(1, "user") // don't forget the table name is set in lower case by DBUtil
      .build();

  @Resource(lookup = "java:/datasources/silverpeas")
  private DataSource dataSource;
  private DbSetupTracker dbSetupTracker = new DbSetupTracker();

  @Before
  public void prepareDataSource() {
    Operation preparation = Operations.sequenceOf(
        TABLES_CREATION,
        CLEAN_UP,
        USER_SET_UP,
        UNIQUE_ID_SET_UP);
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    File[] libs = Maven.resolver()
        .loadPomFromFile("pom.xml")
        .resolve("com.ninja-squad:DbSetup", "org.apache.commons:commons-lang3",
            "commons-codec:commons-codec", "com.carrotsearch:junit-benchmarks").withTransitivity()
        .asFile();
    return ShrinkWrap.create(WebArchive.class, "test.war").addClass(ServiceProvider.class)
        .addClass(BeanContainer.class).addClass(CDIContainer.class).addClass(DBUtil.class)
        .addClass(ConnectionPool.class).addClass(Transaction.class)
        .addClass(TransactionProvider.class).addClass(StringUtil.class).addAsLibraries(libs).addAsManifestResource("META-INF/services/test-org.silverpeas.util.BeanContainer",
            "services/org.silverpeas.util.BeanContainer")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
        .addAsWebInfResource("test-ds.xml", "test-ds.xml");
  }

  @Test
  public void emptyTest() {
    // just to test the deployment into wildfly works fine.
    dbSetupTracker.skipNextLaunch();
  }

  @Test
  public void nextUniqueIdUpdateForAnExistingTableShouldWork() throws SQLException {
    assertThat(actualMaxIdInUniqueIdFor("User"), is(1));
    int nextId = DBUtil.getNextId("User", "id");
    assertThat(nextId, is(2));
    assertThat(actualMaxIdInUniqueIdFor("User"), is(nextId));
  }

  @Test
  public void nextUniqueIdUpdateForAnExistingTableShouldWorkInNewTransaction()
      throws Exception {
    assertThat(actualMaxIdInUniqueIdFor("User"), is(1));
    final Thread nextIdThread = new Thread(() -> {
      try {
        Transaction.performInOne(() -> {
          int nextId = 0;
          nextId = DBUtil.getNextId("User", "id");
          assertThat(nextId, is(2));
          assertThat(actualMaxIdInUniqueIdFor("User"), is(nextId));
          assertThat(nextId, is(2));
          return null;
        });
        Thread.sleep(500);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    try {
      nextIdThread.start();
      Thread.sleep(250);
      assertThat(actualMaxIdInUniqueIdFor("User"), is(2));
      Thread.sleep(500);
      assertThat(actualMaxIdInUniqueIdFor("User"), is(2));
    } finally {
      nextIdThread.interrupt();
    }
  }

  @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 1000)
  @Test
  public void nextUniqueIdUpdateForAnExistingTableShouldWorkAndConcurrency()
      throws SQLException, InterruptedException {
    int nextIdBeforeTesting = actualMaxIdInUniqueIdFor("User");
    assertThat(nextIdBeforeTesting, is(1));
    int nbThreads = 2 + (int) (Math.random() * 10);
    Logger.getAnonymousLogger()
        .info("Start at " + System.currentTimeMillis() + " with " + nbThreads + " threads");
    final Thread[] threads = new Thread[nbThreads];
    for (int i = 0; i < nbThreads; i++) {
      threads[i] = new Thread(() -> {
        try {
          int nextId = DBUtil.getNextId("User", "id");
          Logger.getAnonymousLogger()
              .info("Next id is " + nextId + " at " + System.currentTimeMillis());
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
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
      assertThat(actualMaxIdInUniqueIdFor("User"), is(expectedNextId));
    } finally {
      for (Thread thread : threads) {
        thread.interrupt();
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
    try(Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, tableName.toLowerCase());
      try(ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          maxId = resultSet.getInt(1);
        } else {
          throw new SQLException("No result!");
        }
      }
    }
    return maxId;
  }

}

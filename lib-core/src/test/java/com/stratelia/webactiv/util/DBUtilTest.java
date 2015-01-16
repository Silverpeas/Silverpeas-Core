/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.webactiv.util;

import com.silverpeas.jndi.SimpleMemoryContextFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.InputStream;
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DBUtilTest {

  private static final String DATASOURCE_NAME = "SilverpeasDB";
  private static DataSource dataSource;
  private static ClassPathXmlApplicationContext context;

  public DBUtilTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    context = new ClassPathXmlApplicationContext(new String[]{
      "spring-h2-datasource.xml"});
    dataSource = context.getBean("dataSource", DataSource.class);
    InitialContext ic = new InitialContext();
    ic.rebind(DATASOURCE_NAME, dataSource);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
    context.close();
    DBUtil.clearTestInstance();
  }

  @Before
  public void init() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    connection.close();
  }

  @After
  public void after() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    connection.close();
  }

  private IDatabaseConnection getConnection() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection;
  }

  protected IDataSet getDataSet() throws Exception {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream(
        "com/stratelia/webactiv/util/dbutil-dataset.xml");
    try {
      FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
      ReplacementDataSet dataSet = new ReplacementDataSet(builder.build(in));
      dataSet.addReplacementObject("[NULL]", null);
      return dataSet;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Test of getDateFieldLength method, of class DBUtil.
   */
  @Test
  public void testGetDateFieldLength() {
    assertThat(DBUtil.getDateFieldLength(), is(10));
  }

  /**
   * Test of getTextMaxiLength method, of class DBUtil.
   */
  @Test
  public void testGetTextMaxiLength() {
    assertThat(DBUtil.getTextMaxiLength(), is(4000));
  }

  /**
   * Test of getTextAreaLength method, of class DBUtil.
   */
  @Test
  public void testGetTextAreaLength() {
    assertThat(DBUtil.getTextAreaLength(), is(2000));
  }

  /**
   * Test of getTextFieldLength method, of class DBUtil.
   */
  @Test
  public void testGetTextFieldLength() {
    assertThat(DBUtil.getTextFieldLength(), is(1000));
  }

  /**
   * Test of makeConnection method, of class DBUtil.
   */
  @Test
  public void testMakeConnection() throws SQLException {
    Connection connection = DBUtil.makeConnection(DATASOURCE_NAME);
    assertThat(connection, is(notNullValue()));
    connection.close();
  }

  /**
   * Test of getNextId method, of class DBUtil.
   */
  @Test
  public void testGetNextIdWithoutConnectionExistingLine() {
    String tableName = "sb_test_dbutil";
    String idName = "id";
    int result = DBUtil.getNextId(tableName, idName);
    assertThat(result, is(501));
  }

  /**
   * Test of getNextId method, of class DBUtil when the line doesn't exist in the table.
   */
  @Test
  public void testGetNextIdWithoutConnectionInexistingLine() {
    String tableName = "sb_document_doc";
    String idName = "id";
    int result = DBUtil.getNextId(tableName, idName);
    assertThat(result, is(1));
  }

  /**
   * Test of getNextId method, of class DBUtil.
   */
  @Test
  public void testGetNextIdWithConnectionInexistingLine() throws Exception {
    String tableName = "sb_simple_document";
    String idName = "id";
    Connection connection = dataSource.getConnection();
    try {
      connection.setAutoCommit(false);
      int result = DBUtil.getNextId(connection, tableName, idName);
      assertThat(result, is(1));
    } finally {
      connection.close();
    }
  }

  /**
   * Test of getNextId method, of class DBUtil.
   */
  @Test
  public void testGetNextIdWithConnectionExistingLine() throws Exception {
    String tableName = "sb_test_dbutil_connection";
    String idName = "id";
    Connection connection = dataSource.getConnection();
    try {
      connection.setAutoCommit(false);
      int result = DBUtil.getNextId(connection, tableName, idName);
      assertThat(result, is(501));
    } finally {
      connection.close();
    }
  }

  /**
   * Test of getMaxId method, of class DBUtil.
   */
  @Test
  public void testGetMaxIdWithExistingLine() throws Exception {
    Connection connection = dataSource.getConnection();
    try {
      connection.setAutoCommit(false);
      String tableName = "sb_test_dbutil_connection";
      String idName = "id";
      int result = DBUtil.getMaxId(connection, tableName, idName);
      assertThat(result, is(501));
    } finally {
      connection.close();
    }
  }

  /**
   * Test of getMaxId method, of class DBUtil.
   */
  @Test
  public void testGetMaxIdWithInexistingLine() throws Exception {
    Connection connection = dataSource.getConnection();
    try {
      connection.setAutoCommit(false);
      String tableName = "sb_simple_document";
      String idName = "id";
      int result = DBUtil.getMaxId(connection, tableName, idName);
      assertThat(result, is(1));
    } finally {
      connection.close();
    }
  }

  /**
   * Test of getMaxFromTable method, of class DBUtil.
   */
  @Test
  public void testGetMaxFromTable() throws Exception {

    Connection connection = dataSource.getConnection();
    try {
      connection.setAutoCommit(false);
      String tableName = "sb_test_dbutil_connection";
      String idName = "id";
      int result = DBUtil.getMaxFromTable(connection, tableName, idName);
      assertThat(result, is(1));
    } finally {
      connection.close();
    }
  }

  @Test
  public void testGetAllTableNames() {
    assertThat(29, is(DBUtil.getAllTableNames().size()));
  }

  @Test
  public void nextUniqueIdUpdateForAnExistingTablesShouldWorkAndConcurrency() throws Exception {
    final int nbSetOfThreads = 150;
    final List<Object> count = new ArrayList<Object>();
    final List<Callable<Pair<String, Integer>>> listsOfGetNextId =
        new ArrayList<Callable<Pair<String, Integer>>>(150);
    for (int i = 0; i < 150; i++) {
      listsOfGetNextId.add(new Callable<Pair<String, Integer>>() {
        @Override
        public Pair<String, Integer> call() throws Exception {
          String tableName;
          synchronized (count) {
            count.add("");
            tableName = "User_" + count.size() + "_table";
          }
          return Pair.of(tableName,
              nextUniqueIdUpdateForAnExistingTableShouldWorkAndConcurrency(tableName,
                  nbSetOfThreads));
        }
      });
    }
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    List<Pair<String, Integer>> tableNextIdsInError = new ArrayList<Pair<String, Integer>>();
    try {
      for (Future<Pair<String, Integer>> aTreatment : executorService.invokeAll(listsOfGetNextId)) {
        Pair<String, Integer> tableIdValue = aTreatment.get();
        if (tableIdValue.getRight() != nbSetOfThreads) {
          if (tableNextIdsInError.isEmpty()) {
            Logger.getAnonymousLogger().severe("Some errors...");
          }
          tableNextIdsInError.add(tableIdValue);
          Logger.getAnonymousLogger().severe(
              "Next id value must be " + nbSetOfThreads + " for table " + tableIdValue.getLeft() +
                  ", but was " + tableIdValue.getRight());
        }
      }
    } finally {
      executorService.shutdown();
    }
    if (!tableNextIdsInError.isEmpty()) {
      fail("The next id of " + tableNextIdsInError.size() + " tables is in error.");
    }
  }

  private int nextUniqueIdUpdateForAnExistingTableShouldWorkAndConcurrency(final String tableName,
      final int nbThreads) throws SQLException, InterruptedException {
    Logger.getAnonymousLogger().info(
        "Start at " + System.currentTimeMillis() + " with " + nbThreads + " threads for table " +
            tableName);
    final Thread[] threads = new Thread[nbThreads];
    for (int i = 0; i < nbThreads; i++) {
      threads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            int nextId = DBUtil.getNextId(tableName, "id");
            Logger.getAnonymousLogger().info("Next id for " + tableName + " is " + nextId + " at " +
                System.currentTimeMillis());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      });
    }

    try {
      for (Thread thread : threads) {
        thread.start();
      }
      for (Thread thread : threads) {
        thread.join(60000);
      }
      return actualMaxIdInUniqueIdFor(tableName);
    } finally {
      for (Thread thread : threads) {
        thread.interrupt();
      }
    }
  }

  private int actualMaxIdInUniqueIdFor(String tableName) throws SQLException {
    final String query = "select maxId from UniqueId where tableName = ?";
    Connection connection = dataSource.getConnection();
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.prepareStatement(query);
      statement.setString(1, tableName.toLowerCase());
      resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return resultSet.getInt(1);
      } else {
        return 0;
      }
    } finally {
      DBUtil.close(resultSet, statement);
      DBUtil.close(connection);
    }
  }
}

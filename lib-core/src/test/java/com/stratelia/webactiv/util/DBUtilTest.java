/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.silverpeas.jndi.SimpleMemoryContextFactory;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-jdbc-datasource.xml"})
public class DBUtilTest {

  private static final String DATASOURCE_NAME = "SilverpeasDB";
  @Inject
  private DataSource dataSource;

  public DBUtilTest() {
  }

  @BeforeClass
  public static void setUpClass() {
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  @AfterClass
  public static void tearDownClass() {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  @Before
  public void setUp() throws SQLException, NamingException, DataSetException, DatabaseUnitException {
    InitialContext ic = new InitialContext();
    ic.bind(DATASOURCE_NAME, dataSource);
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        DBUtilTest.class.getClassLoader().getResourceAsStream(
        "com/stratelia/webactiv/util/dbutil-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);

    Connection connection = dataSource.getConnection();
    try {
      IDatabaseConnection databaseConnection = new DatabaseConnection(connection);
      DatabaseOperation.CLEAN_INSERT.execute(databaseConnection, dataSet);
    } finally {
      connection.close();
    }
  }

  @After
  public void tearDown() throws NamingException, DataSetException, DatabaseUnitException,
      SQLException {
    InitialContext ic = new InitialContext();
    ic.unbind(DATASOURCE_NAME);

    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        DBUtilTest.class.getClassLoader().getResourceAsStream(
        "com/stratelia/webactiv/util/dbutil-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    Connection connection = dataSource.getConnection();
    try {
      IDatabaseConnection databaseConnection = new DatabaseConnection(connection);
      DatabaseOperation.DELETE.execute(databaseConnection, dataSet);
    } finally {
      connection.close();
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

  
}

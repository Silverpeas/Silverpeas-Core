/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.contribution.content.wysiwyg.dynamicvalue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.dbunit.JdbcBasedDBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.silverpeas.util.DBUtil;

/**
 * base class for Junit test
 */
abstract class AbstractBaseDynamicValue extends JdbcBasedDBTestCase {

  final static Logger logger = LoggerFactory.getLogger(AbstractBaseDynamicValue.class);
  private Properties properties = null;

  /**
   * @throws java.lang.Exception
   */
  @Override
  @Before
  public void setUp() throws Exception {
    properties = new Properties();
    properties.load(TestDynamicValueDAO.class.getClassLoader().
        getResourceAsStream("jdbc.properties"));
    // create the table to execute the test
    Connection con = null;
    Statement statement = null;

    try {
      con = getConnection().getConnection();
      statement = con.createStatement();

      try {
        statement.executeUpdate("DROP TABLE val_dyn1");
      } catch (SQLException e) {
        logger.info("Couldn't drop the table val_dyn1", e);
      }
      String sql = "CREATE TABLE val_dyn1 ( \"value\" character varying(256) NOT NULL,"
          + "keyword character varying(100) NOT NULL, start_date date NOT NULL, end_date date )"
          + "WITH (OIDS=FALSE);"
          + "ALTER TABLE val_dyn1 OWNER TO " + getUsername();
      statement.executeUpdate(sql);
    } finally {
      DBUtil.close(statement);
      DBUtil.close(con);
    }
    super.setUp();
  }

  /**
   * Returns the password for the connection.<br/> Subclasses may override this method to provide a
   * custom password.<br/> Default implementations returns null.
   */
  @Override
  protected String getPassword() {
    return properties.getProperty("jdbc.password", "postgres");
  }

  /**
   * Returns the username for the connection.<br/> Subclasses may override this method to provide a
   * custom username.<br/> Default implementations returns null.
   */
  @Override
  protected String getUsername() {
    return properties.getProperty("jdbc.username", "postgres");
  }

  @Override
  protected DatabaseOperation getTearDownOperation() throws Exception {
    return DatabaseOperation.DELETE_ALL;
  }

  @Override
  protected IDataSet getDataSet() throws Exception {
    IDataSet dataSet = new FlatXmlDataSetBuilder().build(this.getClass().getResourceAsStream(
        "test-dynamicvalue-dataset.xml"));
    return dataSet;
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  @Override
  public void tearDown() throws Exception {
    Connection con = null;
    Statement statement = null;
    try {
      // delete the table created in the setup.
      con = getConnection().getConnection();
      statement = con.createStatement();
      String sql = "DROP TABLE val_dyn1";
      statement.executeUpdate(sql);
    } finally {
      DBUtil.close(statement);
      DBUtil.close(con);
    }
  }

  @Override
  protected String getConnectionUrl() {
    return properties.getProperty("jdbc.url", "jdbc:postgresql://localhost:5432/postgres");
  }

  @Override
  protected String getDriverClass() {
    return properties.getProperty("jdbc.driver", "org.postgresql.Driver");
  }
}

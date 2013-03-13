/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.components.model;

import java.io.InputStream;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.naming.InitialContext;
import java.util.Properties;

import javax.sql.DataSource;
import org.apache.commons.io.IOUtils;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

/**
 * @author ehugonnet
 */

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractSpringJndiDaoTest {

  private static String jndiName = "";
  @Inject
  protected DataSource dataSource;

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  

  protected DatabaseOperation getTearDownOperation() throws Exception {
    return DatabaseOperation.DELETE_ALL;
  }

  protected DatabaseOperation getSetUpOperation() throws Exception {
    return DatabaseOperation.CLEAN_INSERT;
  }

  @Before
  public void init() throws Exception {
    InitialContext ic = new InitialContext();
    Properties props = new Properties();
    props.load(AbstractTestDao.class.getClassLoader().getResourceAsStream("jdbc.properties"));
    jndiName ="jdbc/Silverpeas";
    ic.rebind(jndiName, this.dataSource);
    IDatabaseConnection connection = getConnection();
    getSetUpOperation().execute(connection, getDataSet());
    connection.close();
  }

  @After
  public void after() throws Exception {
    IDatabaseConnection connection = getConnection();
    getTearDownOperation().execute(connection, getDataSet());
    connection.close();
    InitialContext ic = new InitialContext();
    ((EmbeddedDatabase)ic.lookup("jdbc/Silverpeas")).shutdown();
  }

  private IDatabaseConnection getConnection() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection;
  }


  protected IDataSet getDataSet() throws Exception {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream(getDatasetFileName());
    try {
      FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
      ReplacementDataSet dataSet = new ReplacementDataSet(builder.build(in));
      dataSet.addReplacementObject("[NULL]", null);
      return dataSet;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  protected abstract String getDatasetFileName();

}
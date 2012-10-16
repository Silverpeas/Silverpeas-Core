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

import com.silverpeas.jndi.SimpleMemoryContextFactory;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.dbunit.JndiBasedDBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

/**
 * @author ehugonnet
 */
public abstract class AbstractTestDao extends JndiBasedDBTestCase {

  private static String jndiName = "";

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    configureJNDIDatasource();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  /**
   * This is called directly when running under JUnit 3.
   * @throws Exception if an error occurs while tearing down the resources.
   */
  @Override
  public void tearDown() throws Exception {
    cleanData();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  /**
   * Frees the previously created data in the database. This is called directly by JUnit 4 or by the
   * tearDown() method when running in JUnit 3.
   * @throws Exception if an error occurs while cleaning data.
   */
  @After
  public void cleanData() throws Exception {
    super.tearDown();
  }

  /**
   * This is called directly when running under JUnit 3.
   * @throws Exception if an error occurs while setting up the resources required by the tests.
   */
  @Override
  public void setUp() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    configureJNDIDatasource();
    prepareData();
  }

  /**
   * Prepares the data for the tests in the database. This is called directly by JUnit 4 or by the
   * setUp() method when running in JUnit 3.
   * @throws Exception if an error occurs while preparing the data required by the tests.
   */
  @Before
  public void prepareData() throws Exception {
    super.setUp();
  }

  /**
   * Configure the data source from a JNDI context. This is called directly by JUnit 4 at test class
   * loading or by the setUp() method at each test invocation in JUnit 3.
   * @throws IOException if an error occurs while communicating with the JNDI context.
   * @throws NamingException if the data source cannot be found in the JNDI context.
   * @throws Exception if the data source cannot be created.
   */
  public static void configureJNDIDatasource() throws Exception {
    InitialContext ic = new InitialContext();
    Properties props = new Properties();
    props.load(AbstractTestDao.class.getClassLoader().getResourceAsStream("jdbc.properties"));
    DataSource ds = BasicDataSourceFactory.createDataSource(props);
    jndiName = props.getProperty("jndi.name");
    rebind(ic, jndiName, ds);
    ic.rebind(jndiName, ds);
  }

  @Override
  protected DatabaseOperation getTearDownOperation() throws Exception {
    return DatabaseOperation.DELETE_ALL;
  }

  @Override
  protected String getLookupName() {
    return jndiName;
  }

  @Override
  protected IDataSet getDataSet() throws Exception {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    ReplacementDataSet dataSet = new ReplacementDataSet(builder.build(this.getClass().
        getResourceAsStream(getDatasetFileName())));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  /**
   * Workaround to be able to use Sun's JNDI file system provider on Unix
   * @param ic : the JNDI initial context
   * @param jndiName : the binding name
   * @param ref : the reference to be bound
   * @throws NamingException
   */
  protected static void rebind(InitialContext ic, String jndiName, Object ref) throws
      NamingException {
    Context currentContext = ic;
    StringTokenizer tokenizer = new StringTokenizer(jndiName, "/", false);
    while (tokenizer.hasMoreTokens()) {
      String name = tokenizer.nextToken();
      if (tokenizer.hasMoreTokens()) {
        try {
          currentContext = (Context) currentContext.lookup(name);
        } catch (javax.naming.NameNotFoundException nnfex) {
          currentContext = currentContext.createSubcontext(name);
        }
      } else {
        currentContext.rebind(name, ref);
      }
    }
  }

  protected abstract String getDatasetFileName();
}
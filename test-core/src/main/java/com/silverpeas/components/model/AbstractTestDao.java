/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.components.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.silverpeas.jndi.SimpleMemoryContextFactory;

import org.apache.commons.io.IOUtils;
import org.dbunit.JndiBasedDBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * @author ehugonnet
 */
public abstract class AbstractTestDao extends JndiBasedDBTestCase {

  public AbstractTestDao() {
    Properties props = new Properties();
    InputStream in = null;
    try {
      in = AbstractTestDao.class.getClassLoader().getResourceAsStream("jdbc.properties");
      props.load(in);
    } catch (IOException ioex) {
      ioex.printStackTrace();
    } finally {
      IOUtils.closeQuietly(in);
    }

    jndiName = props.getProperty("jndi.name");
  }
  private String jndiName = "";
  protected EmbeddedDatabase datasource;

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  /**
   * This is called directly when running under JUnit 3.
   *
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
   *
   * @throws Exception if an error occurs while cleaning data.
   */
  @After
  public void cleanData() throws Exception {
    super.tearDown();
    this.datasource.shutdown();
  }

  /**
   * This is called directly when running under JUnit 3.
   *
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
   *
   * @throws Exception if an error occurs while preparing the data required by the tests.
   */
  @Before
  public void prepareData() throws Exception {
    super.setUp();
  }

  /**
   * Configure the data source from a JNDI context. This is called directly by JUnit 4 at test class
   * loading or by the setUp() method at each test invocation in JUnit 3.
   *
   * @throws IOException if an error occurs while communicating with the JNDI context.
   * @throws NamingException if the data source cannot be found in the JNDI context.
   * @throws Exception if the data source cannot be created.
   */
  public void configureJNDIDatasource() throws Exception {
    InitialContext ic = new InitialContext();
    EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
    datasource = builder.setType(EmbeddedDatabaseType.H2).addScript(getTableCreationScript()).
        build();
    ic.rebind(jndiName, datasource);
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
    InputStream in = null;
    try {
      in = this.getClass().getResourceAsStream(getDatasetFileName());
      ReplacementDataSet dataSet = new ReplacementDataSet(builder.build(in));
      dataSet.addReplacementObject("[NULL]", null);
      return dataSet;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  protected abstract String getDatasetFileName();

  protected abstract String getTableCreationFileName();

  protected String getTableCreationScript() {
    String filePath = "";
    try {
      filePath = this.getClass().getResource(getTableCreationFileName()).toURI().toURL().toString();
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (URISyntaxException ex) {
      ex.printStackTrace();
    }
    return filePath;
  }
}
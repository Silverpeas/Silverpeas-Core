/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

import com.silverpeas.util.PathTestUtil;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.StringRefAddr;
import javax.naming.Reference;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import javax.inject.Inject;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author ehugonnet
 */

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractSpringJndiDaoTest {

  private static String jndiName = "";
  @Inject
  private DataSource dataSource;

  /**
   * Configure the data source from a JNDI context.
   * This is called directly by JUnit 4 at test class loading or by the setUp() method at each test
   * invocation in JUnit 3.
   * @throws IOException if an error occurs while communicating with the JNDI context.
   * @throws NamingException if the data source cannot be found in the JNDI context.
   */
  @BeforeClass
  public static void configureJNDIDatasource() throws IOException, NamingException {
    prepareJndi();
    Properties env = new Properties();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
    InitialContext ic = new InitialContext(env);
    Properties props = new Properties();
    props.load(AbstractTestDao.class.getClassLoader().getResourceAsStream("jdbc.properties"));
    // Construct BasicDataSource reference
    Reference ref = new Reference("javax.sql.DataSource",
        "org.apache.commons.dbcp.BasicDataSourceFactory", null);
    ref.add(new StringRefAddr("driverClassName", props.getProperty("driverClassName")));
    ref.add(new StringRefAddr("url", props.getProperty("url")));
    ref.add(new StringRefAddr("username", props.getProperty("username")));
    ref.add(new StringRefAddr("password", props.getProperty("password")));
    ref.add(new StringRefAddr("maxActive", "4"));
    ref.add(new StringRefAddr("maxWait", "5000"));
    ref.add(new StringRefAddr("removeAbandoned", "true"));
    ref.add(new StringRefAddr("removeAbandonedTimeout", "5000"));
    jndiName = props.getProperty("jndi.name");
    rebind(ic, jndiName, ref);
    ic.rebind(jndiName, ref);
  }
  
  /**
   * Workaround to be able to use Sun's JNDI file system provider on Unix
   * @param ic : the JNDI initial context
   * @param jndiName : the binding name
   * @param ref : the reference to be bound
   * @throws NamingException
   */
  protected static void rebind(InitialContext ic, String jndiName, Reference ref) throws
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

  /**
   * Creates the directory for JNDI files ystem provider
   * @throws IOException
   */
  protected static void prepareJndi() throws IOException {
    Properties jndiProperties = new Properties();
    jndiProperties.load(PathTestUtil.class.getClassLoader().getResourceAsStream("jndi.properties"));
    String jndiDirectoryPath = jndiProperties.getProperty(Context.PROVIDER_URL).substring(7);
    File jndiDirectory = new File(jndiDirectoryPath);
    if (!jndiDirectory.exists()) {
      jndiDirectory.mkdirs();
      jndiDirectory.mkdir();
    }
  }

  protected DatabaseOperation getTearDownOperation() throws Exception {
    return DatabaseOperation.DELETE_ALL;
  }
  
  protected DatabaseOperation getSetUpOperation() throws Exception {
    return DatabaseOperation.CLEAN_INSERT;
  }
  
  
  @Before
  public void init() throws Exception {
    IDatabaseConnection connection = getConnection();
    getSetUpOperation().execute(connection, getDataSet());
    connection.close();
  }

  @After
  public void after() throws Exception {
    IDatabaseConnection connection = getConnection();
    getTearDownOperation().execute(connection, getDataSet());
    connection.close();
  }

  private IDatabaseConnection getConnection() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection;
  }

  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        this.getClass().getResourceAsStream(getDatasetFileName())));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  

  protected abstract String getDatasetFileName();

}
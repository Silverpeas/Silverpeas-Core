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
package com.stratelia.silverpeas.versioning.jcr.impl;

import com.silverpeas.util.PathTestUtil;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Hashtable;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

import org.apache.jackrabbit.JcrConstants;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.operation.DatabaseOperation;

import com.stratelia.webactiv.util.JNDINames;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-in-memory-jcr.xml"})
public abstract class AbstractJcrTestCase {

  public AbstractJcrTestCase() {
    super();
  }
  private Calendar calend;
  private DataSource datasource;

  public DataSource getDataSource() {
    return datasource;
  }

  @Resource
  public void setDataSource(DataSource datasource) {
    this.datasource = datasource;
    try {
      prepareJndi();
      Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY,
          "com.sun.jndi.fscontext.RefFSContextFactory");
      InitialContext ic = new InitialContext(env);
      Properties properties = new Properties();
      properties.load(PathTestUtil.class.getClassLoader().
          getResourceAsStream("jdbc.properties"));
      // Construct BasicDataSource reference
      Reference ref = new Reference("javax.sql.DataSource",
          "org.apache.commons.dbcp.BasicDataSourceFactory", null);
      ref.add(new StringRefAddr("driverClassName", properties.getProperty(
          "driverClassName", "org.postgresql.Driver")));
      ref.add(new StringRefAddr("url",
          properties.getProperty(
          "url", "jdbc:postgresql://localhost:5432/postgres")));
      ref.add(new StringRefAddr("username", properties.getProperty(
          "username", "postgres")));
      ref.add(new StringRefAddr("password", properties.getProperty(
          "password", "postgres")));
      ref.add(new StringRefAddr("maxActive", "4"));
      ref.add(new StringRefAddr("maxWait", "5000"));
      ref.add(new StringRefAddr("removeAbandoned", "true"));
      ref.add(new StringRefAddr("removeAbandonedTimeout", "5000"));
      rebind(ic, JNDINames.DATABASE_DATASOURCE, ref);
      rebind(ic, JNDINames.ADMIN_DATASOURCE, ref);
    } catch (NamingException nex) {
      nex.printStackTrace();
    } catch (IOException nex) {
      nex.printStackTrace();
    }
  }

  protected IDataSet getDataSet() throws Exception {
    InputStream in = this.getClass().getResourceAsStream("test-versioning-dataset.xml");
    try {
      ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(in));
      dataSet.addReplacementObject("[NULL]", null);
      return dataSet;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  protected String readFile(String path) throws IOException {
    return FileUtils.readFileToString(new File(path));
  }

  protected void createTempFile(String path, String content) throws IOException {
    File attachmentFile = new File(path);
    if (!attachmentFile.getParentFile().exists()) {
      attachmentFile.getParentFile().mkdirs();
    }
    FileUtils.write(attachmentFile, content);
  }

  protected String readFileFromNode(Node fileNode) throws IOException,
      ValueFormatException, PathNotFoundException, RepositoryException {
    CharArrayWriter writer = null;
    InputStream in = null;
    Reader reader = null;
    try {
      in = fileNode.getNode(JcrConstants.JCR_CONTENT).getProperty(
          JcrConstants.JCR_DATA).getStream();
      writer = new CharArrayWriter();
      reader = new InputStreamReader(in);
      char[] buffer = new char[8];
      int c = 0;
      while ((c = reader.read(buffer)) != -1) {
        writer.write(buffer, 0, c);
      }
      return new String(writer.toCharArray());
    } catch (IOException ioex) {
      return null;
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (in != null) {
        in.close();
      }
      if (writer != null) {
        writer.close();
      }
    }
  }

  @Before
  public void onSetUp() throws Exception {
    System.getProperties().put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.fscontext.RefFSContextFactory");
    calend = Calendar.getInstance();
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.HOUR, 9);
    calend.set(Calendar.DAY_OF_MONTH, 12);
    calend.set(Calendar.MONTH, Calendar.MARCH);
    calend.set(Calendar.YEAR, 2008);
    IDatabaseConnection connection = null;
    try {
      connection = new DatabaseConnection(datasource.getConnection());
      DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    } catch (Exception ex) {
      throw ex;
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          throw e;
        }
      }
    }
  }

  @After
  public void onTearDown() throws Exception {
    clearRepository();
    IDatabaseConnection connection = null;
    try {
      connection = new DatabaseConnection(datasource.getConnection());
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
      cleanJndi();
    } catch (Exception ex) {
      throw ex;
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          throw e;
        }
      }
    }
  }

  /**
   * Creates the directory for JNDI files ystem provider
   *
   * @throws IOException
   */
  protected void prepareJndi() throws IOException {
    Properties jndiProperties = new Properties();
    jndiProperties.load(PathTestUtil.class.getClassLoader().
        getResourceAsStream("jndi.properties"));
    String jndiDirectoryPath = jndiProperties.getProperty("java.naming.provider.url").substring(7);
    File jndiDirectory = new File(jndiDirectoryPath);
    if (!jndiDirectory.exists()) {
      jndiDirectory.mkdirs();
      jndiDirectory.mkdir();
    }
  }

  /**
   * Deletes the directory for JNDI file system provider
   *
   * @throws IOException
   */
  protected void cleanJndi() throws IOException {
    Properties jndiProperties = new Properties();
    jndiProperties.load(PathTestUtil.class.getClassLoader().
        getResourceAsStream("jndi.properties"));
    String jndiDirectoryPath = jndiProperties.getProperty("java.naming.provider.url").substring(7);
    File jndiDirectory = new File(jndiDirectoryPath);
    if (jndiDirectory.exists()) {
      jndiDirectory.delete();
    }
  }

  /**
   * Workaround to be able to use Sun's JNDI file system provider on Unix
   *
   * @param ic : the JNDI initial context
   * @param jndiName : the binding name
   * @param ref : the reference to be bound
   * @throws NamingException
   */
  protected void rebind(InitialContext ic, String jndiName, Reference ref) throws NamingException {
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

  protected abstract void clearRepository() throws Exception;
}

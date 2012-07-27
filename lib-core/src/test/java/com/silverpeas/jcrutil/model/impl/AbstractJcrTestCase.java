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

package com.silverpeas.jcrutil.model.impl;

import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.util.PathTestUtil;
import com.stratelia.webactiv.util.JNDINames;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
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
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractJcrTestCase {

  @Inject
  private DataSource datasource;

  public AbstractJcrTestCase() {
  }

  public DataSource getDataSource() {
    return datasource;
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  @Before
  public void init() throws Exception {
    InitialContext ic = new InitialContext();
    Properties properties = new Properties();
    properties.load(PathTestUtil.class.getClassLoader().getResourceAsStream("jdbc.properties"));
    DataSource ds = BasicDataSourceFactory.createDataSource(properties);
    rebind(ic, JNDINames.DATABASE_DATASOURCE, ds);
    ic.rebind(JNDINames.DATABASE_DATASOURCE, ds);
    rebind(ic, JNDINames.ADMIN_DATASOURCE, ds);
    ic.rebind(JNDINames.ADMIN_DATASOURCE, ds);
    setUpDatabase();
  }

  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(this.
        getClass().getResourceAsStream("test-attachment-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  protected String readFile(String path) throws IOException {
    CharArrayWriter writer = new CharArrayWriter();
    Reader reader = new InputStreamReader(new FileInputStream(path));
    try {
      IOUtils.copy(reader, writer);
      return new String(writer.toCharArray());
    } catch (IOException ioex) {
      return null;
    } finally {
      IOUtils.closeQuietly(reader);
      IOUtils.closeQuietly(writer);
    }
  }

  protected void createTempFile(String path, String content) throws IOException {
    File attachmentFile = new File(path);
    if (!attachmentFile.getParentFile().exists()) {
      attachmentFile.getParentFile().mkdirs();
    }
    Writer writer = new OutputStreamWriter(new FileOutputStream(attachmentFile));
    try {
      writer.write(content);
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }

  protected void deleteTempFile(String path) {
    FileUtils.deleteQuietly(new File(path));
  }

  protected String readFileFromNode(Node fileNode) throws IOException,
      ValueFormatException, PathNotFoundException, RepositoryException {
    CharArrayWriter writer = null;
    InputStream in = null;
    Reader reader = null;
    try {
      in = fileNode.getNode(JcrConstants.JCR_CONTENT).getProperty(JcrConstants.JCR_DATA).getStream();
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

  public void setUpDatabase() throws Exception {
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
  public void tearDownDatabase() throws Exception {
    clearRepository();
    IDatabaseConnection connection = null;
    try {
      connection = new DatabaseConnection(datasource.getConnection());
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
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
   * Workaround to be able to use Sun's JNDI file system provider on Unix
   *
   * @param ic : the JNDI initial context
   * @param jndiName : the binding name
   * @param ref : the reference to be bound
   * @throws NamingException
   */
  protected void rebind(InitialContext ic, String jndiName, Object ref) throws NamingException {
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

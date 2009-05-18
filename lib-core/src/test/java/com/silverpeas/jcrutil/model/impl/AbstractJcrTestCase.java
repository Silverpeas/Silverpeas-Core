package com.silverpeas.jcrutil.model.impl;

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
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.stratelia.webactiv.util.JNDINames;

public abstract class AbstractJcrTestCase extends
    AbstractDependencyInjectionSpringContextTests {

  public AbstractJcrTestCase() {
    super();
  }

  public AbstractJcrTestCase(String name) {
    super(name);
  }

  protected Calendar calend;

  protected DataSource datasource;

  public DataSource getDataSource() {
    return datasource;
  }

  public void setDataSource(DataSource datasource) {
    this.datasource = datasource;
    try {
      Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY,
          "com.sun.jndi.fscontext.RefFSContextFactory");
      InitialContext ic = new InitialContext(env);
      // Construct BasicDataSource reference
      Reference ref = new Reference("javax.sql.DataSource",
          "org.apache.commons.dbcp.BasicDataSourceFactory", null);
      ref.add(new StringRefAddr("driverClassName", "org.postgresql.Driver"));
      ref.add(new StringRefAddr("url",
          "jdbc:postgresql://localhost:5432/postgres"));
      ref.add(new StringRefAddr("username", "postgres"));
      ref.add(new StringRefAddr("password", "postgres"));
      ref.add(new StringRefAddr("maxActive", "4"));
      ref.add(new StringRefAddr("maxWait", "5000"));
      ref.add(new StringRefAddr("removeAbandoned", "true"));
      ref.add(new StringRefAddr("removeAbandonedTimeout", "5000"));
      ic.rebind(JNDINames.DATABASE_DATASOURCE, ref);
      ic.rebind(JNDINames.ADMIN_DATASOURCE, ref);
    } catch (NamingException nex) {
      nex.printStackTrace();
    }
  }

  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(this
        .getClass().getResourceAsStream("test-attachment-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  protected String readFile(String path) throws IOException {
    CharArrayWriter writer = null;
    InputStream in = null;
    Reader reader = null;
    try {
      in = new FileInputStream(path);
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

  protected void createTempFile(String path, String content) throws IOException {
    File attachmentFile = new File(path);
    attachmentFile.deleteOnExit();
    FileOutputStream out = null;
    Writer writer = null;
    try {
      out = new FileOutputStream(attachmentFile);
      writer = new OutputStreamWriter(out);
      writer.write(content);
    } finally {
      if (writer != null) {
        writer.close();
      }
      if (out != null) {
        out.close();
      }
    }
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

  protected String[] getConfigLocations() {
    return new String[] { "spring-in-memory-jcr.xml" };
  }

  protected void onSetUp() {
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
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected void onTearDown() throws Exception {
    clearRepository();
    IDatabaseConnection connection = null;
    try {
      connection = new DatabaseConnection(datasource.getConnection());
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected abstract void clearRepository() throws Exception;
}

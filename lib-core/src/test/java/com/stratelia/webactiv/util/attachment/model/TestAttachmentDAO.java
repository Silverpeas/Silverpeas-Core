package com.stratelia.webactiv.util.attachment.model;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.dbunit.JndiBasedDBTestCase;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

public class TestAttachmentDAO extends JndiBasedDBTestCase {

  private String jndiName = "";

  protected void setUp() throws Exception {
    Hashtable env = new Hashtable();
    env.put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.fscontext.RefFSContextFactory");
    InitialContext ic = new InitialContext(env);
    Properties props = new Properties();
    props.load(TestAttachmentDAO.class.getClassLoader().getResourceAsStream(
        "jdbc.properties"));
    // Construct BasicDataSource reference
    Reference ref = new Reference("javax.sql.DataSource",
        "org.apache.commons.dbcp.BasicDataSourceFactory", null);
    ref.add(new StringRefAddr("driverClassName", props
        .getProperty("driverClassName")));
    ref.add(new StringRefAddr("url", props.getProperty("url")));
    ref.add(new StringRefAddr("username", props.getProperty("username")));
    ref.add(new StringRefAddr("password", props.getProperty("password")));
    ref.add(new StringRefAddr("maxActive", "4"));
    ref.add(new StringRefAddr("maxWait", "5000"));
    ref.add(new StringRefAddr("removeAbandoned", "true"));
    ref.add(new StringRefAddr("removeAbandonedTimeout", "5000"));
    ic.rebind(props.getProperty("jndi.name"), ref);
    jndiName = props.getProperty("jndi.name");
    }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  protected String getLookupName() {
    return jndiName;
  }



  protected Properties getJNDIProperties() {
    Properties env = new Properties();
    env.put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.fscontext.RefFSContextFactory");
    return env;
  }

  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        TestAttachmentDAO.class
            .getResourceAsStream("test-attachment-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  public void testFillDb() {
    IDatabaseConnection connection = null;
    try{
      connection = getDatabaseTester().getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
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

}

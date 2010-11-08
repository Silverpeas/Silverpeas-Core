/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.components.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import org.apache.commons.lang.StringUtils;
import org.dbunit.database.IDatabaseConnection;
import org.junit.After;
import org.junit.Before;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ehugonnet
 */
public class AbstractJndiCase {
   
  protected static SilverpeasJndiCase baseTest;

  @Before
  public void setUp() throws Exception {
    baseTest.setUp();
  }

  protected static void executeDDL(IDatabaseConnection databaseConnection, String filename) {
    Connection connection = null;
    try {
      connection = databaseConnection.getConnection();
      Statement st = connection.createStatement();
      st.execute(loadDDL(filename));
      connection.commit();
    } catch (Exception e) {
      LoggerFactory.getLogger(AbstractJndiCase.class).error("Error creating tables", e);
    }
  }

  protected static String loadDDL(String filename) throws IOException {
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    BufferedReader r = new BufferedReader(new InputStreamReader(in));
    StringBuilder buffer = new StringBuilder();
    String line = null;
    String EOL = System.getProperty("line.separator");
    while ((line = r.readLine()) != null) {
      if (!StringUtils.isBlank(line) && !line.startsWith("#")) {
        buffer.append(line).append(EOL);
      }
    }
    in.close();
    return buffer.toString();
  }

  @After
  public void tearDown() throws Exception {
    baseTest.tearDown();
  }
}

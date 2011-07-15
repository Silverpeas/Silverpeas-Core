/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.com/legal/licensing"
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
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

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

package com.silverpeas.components.model;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dbunit.database.IDatabaseConnection;
import org.junit.After;
import org.junit.Before;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

/**
 * @author ehugonnet
 */
public class AbstractJndiCase {

  protected static SilverpeasJndiCase baseTest;

  @Before
  public void setUp() throws Exception {
    baseTest.setUp();
  }

  public static void executeDDL(IDatabaseConnection databaseConnection, String filename) {
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

  public static String loadDDL(String filename) throws IOException {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(
          Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)));
      StringBuilder buffer = new StringBuilder();
      String line = null;
      String EOL = System.getProperty("line.separator");
      while ((line = reader.readLine()) != null) {
        if (!StringUtils.isBlank(line) && !line.startsWith("#")) {
          buffer.append(line).append(EOL);
        }
      }
      return buffer.toString();
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  @After
  public void tearDown() throws Exception {
    baseTest.tearDown();
  }
}

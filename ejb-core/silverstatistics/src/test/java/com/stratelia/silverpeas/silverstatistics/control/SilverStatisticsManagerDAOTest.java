/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.stratelia.silverpeas.silverstatistics.control;

import com.silverpeas.components.model.SilverpeasJndiCase;
import java.io.IOException;
import javax.naming.NamingException;
import org.dbunit.database.IDatabaseConnection;
import com.silverpeas.components.model.AbstractJndiCase;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import java.sql.Connection;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class SilverStatisticsManagerDAOTest extends AbstractJndiCase {

  public SilverStatisticsManagerDAOTest() {
  }

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
    baseTest = new SilverpeasJndiCase("com/silverpeas/comment/dao/comments-dataset.xml",
        "create-database.ddl");
    baseTest.configureJNDIDatasource();
    IDatabaseConnection databaseConnection = baseTest.getDatabaseTester().getConnection();
    executeDDL(databaseConnection, baseTest.getDdlFile());
    baseTest.getDatabaseTester().closeConnection(databaseConnection);
  }

  
  @Test
  public void testGetRequestDate() {
    assertThat(SilverStatisticsManagerDAO.getRequestDate(10, 15), is("'10-15-01'"));
    assertThat(SilverStatisticsManagerDAO.getRequestDate(5, 12), is("'5-12-01'"));
    assertThat(SilverStatisticsManagerDAO.getRequestDate(20, 5), is("'20-05-01'"));
  }


  @Test
  public void testInsertDataStatsCumul() {
    assertThat(SilverStatisticsManagerDAO.getRequestDate(10, 15), is("'10-15-01'"));
    assertThat(SilverStatisticsManagerDAO.getRequestDate(5, 12), is("'5-12-01'"));
    assertThat(SilverStatisticsManagerDAO.getRequestDate(20, 5), is("'20-05-01'"));
  }

  /**
   * Test of putDataStatsCumul method, of class SilverStatisticsManagerDAO.
   */
  @Test
  public void testPutDataStatsCumul() throws Exception {
    System.out.println("putDataStatsCumul");
    Connection con = null;
    String statsType = "";
    List<String> valueKeys = null;
    StatisticsConfig conf = null;
    SilverStatisticsManagerDAO.putDataStatsCumul(con, statsType, valueKeys, conf);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of makeStatCumul method, of class SilverStatisticsManagerDAO.
   */
  @Test
  public void testMakeStatCumul() throws Exception {
    System.out.println("makeStatCumul");
    Connection con = null;
    String StatsType = "";
    StatisticsConfig conf = null;
    SilverStatisticsManagerDAO.makeStatCumul(con, StatsType, conf);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of makeStatAllCumul method, of class SilverStatisticsManagerDAO.
   */
  @Test
  public void testMakeStatAllCumul() {
    System.out.println("makeStatAllCumul");
    StatisticsConfig conf = null;
    SilverStatisticsManagerDAO.makeStatAllCumul(conf);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
}

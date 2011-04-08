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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.silverstatistics.ejb;

import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import java.sql.Connection;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class SilverStatisticsDAOTest {
  
  public SilverStatisticsDAOTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of putDataStats method, of class SilverStatisticsDAO.
   * @throws Exception 
   */
   /*@Test
 public void testPutDataStats() throws Exception {
    System.out.println("putDataStats");
    Connection con = null;
    String StatsType = "";
    List<String> valueKeys = null;
    StatisticsConfig conf = null;
    SilverStatisticsDAO.putDataStats(con, StatsType, valueKeys, conf);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }*/
  
  
  public void testInsertData() throws Exception {
    SilverStatisticsDAO.insertDataStats(null, null, null, null);
  }
}

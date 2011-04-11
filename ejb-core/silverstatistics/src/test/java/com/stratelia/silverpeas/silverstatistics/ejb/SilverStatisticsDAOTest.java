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

import java.util.Map;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import org.junit.Test;
import com.mockrunner.mock.jdbc.JDBCMockObjectFactory;
import com.google.common.collect.Lists;
import com.mockrunner.jdbc.JDBCTestModule;
import com.mockrunner.mock.jdbc.MockConnection;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import org.junit.Before;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author ehugonnet
 */
public class SilverStatisticsDAOTest {

  private StatisticsConfig config;
  private JDBCMockObjectFactory factory;
  private JDBCTestModule module;

  public SilverStatisticsDAOTest() {
  }

  @Before
  public void initialiseConfig() throws Exception {
    config = new StatisticsConfig();
    config.init();
    factory = new JDBCMockObjectFactory();
    module = new JDBCTestModule(factory);
    module.setExactMatch(true);
  }

  /**
   * Test of putDataStats method, of class SilverStatisticsDAO.
   *
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
  @Test
  public void testInsertDataForConnexion() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    String typeofStat = "Connexion";
    List<String> data = Lists.newArrayList("2011-01-17", "1620", "1", "1223229");
    SilverStatisticsDAO.insertDataStats(connexion, typeofStat, data, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getPreparedStatements();
    assertNotNull(statements);
    assertThat(statements, hasSize(1));
    MockPreparedStatement pstmt =  module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is ("INSERT INTO SB_Stat_Connection(dateStat,userId,countConnection,duration) VALUES(?,?,?,?)"));
    Map parameters = pstmt.getParameterMap();
    assertThat((String)parameters.get("dateStat"), is("2011-01-17"));
  }
}

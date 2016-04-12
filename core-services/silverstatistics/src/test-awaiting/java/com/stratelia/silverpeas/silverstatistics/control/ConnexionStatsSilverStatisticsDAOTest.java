/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.stratelia.silverpeas.silverstatistics.control;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import com.stratelia.silverpeas.silverstatistics.util.StatType;

import com.mockrunner.jdbc.JDBCTestModule;
import com.mockrunner.jdbc.StatementResultSetHandler;
import com.mockrunner.mock.jdbc.JDBCMockObjectFactory;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
public class ConnexionStatsSilverStatisticsDAOTest {

  private StatisticsConfig config;
  private JDBCMockObjectFactory factory;
  private JDBCTestModule module;
  private static final StatType typeofStat = StatType.Connexion;

  public ConnexionStatsSilverStatisticsDAOTest() {
  }

  @Before
  public void initialiseConfig() throws Exception {
    config = new StatisticsConfig();
    config.init();
    factory = new JDBCMockObjectFactory();
    module = new JDBCTestModule(factory);
    module.setExactMatch(true);
  }

  @Test
  public void testInsertData() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    List<String> data = Arrays.asList("2011-01-17", "1620", "1", "1223229");
    SilverStatisticsDAO.insertDataStats(connexion, typeofStat, data, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getPreparedStatements();
    assertNotNull(statements);
    assertThat(statements, hasSize(1));
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is("INSERT INTO SB_Stat_Connection(dateStat,userId,countConnection,"
        + "duration) VALUES(?,?,?,?)"));
    Map parameters = pstmt.getParameterMap();
    assertThat((String) parameters.get(1), is("2011-01-17"));
    assertThat((Integer) parameters.get(2), is(1620));
    assertThat((Long) parameters.get(3), is(1L));
    assertThat((Long) parameters.get(4), is(1223229L));
  }

  @Test
  public void testPutDataStatsWithExistingData() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    StatementResultSetHandler statementHandler = connexion.getStatementResultSetHandler();
    MockResultSet result = statementHandler.createResultSet();
    result.addRow(new Long[]{10000L});
    statementHandler.prepareGlobalResultSet(result);
    List<String> data = Arrays.asList("2011-01-17", "1620", "1", "1223229");
    SilverStatisticsDAO.putDataStats(connexion, typeofStat, data, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getPreparedStatements();
    assertNotNull(statements);
    assertThat(statements, hasSize(1));
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is("UPDATE SB_Stat_Connection SET countConnection=countConnection+? "
        + ",duration=duration+?  WHERE dateStat='2011-01-17' AND userId=1620"));
    Map parameters = pstmt.getParameterMap();
    assertThat((Long) parameters.get(1), is(1L));
    assertThat((Long) parameters.get(2), is(1223229L));
  }

  @Test
  public void testPutDataStatsWithoutExistingData() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    StatementResultSetHandler statementHandler = connexion.getStatementResultSetHandler();
    MockResultSet emptyResult = statementHandler.createResultSet();
    statementHandler.prepareGlobalResultSet(emptyResult);
    List<String> data = Arrays.asList("2011-01-17", "1620", "1", "1223229");
    SilverStatisticsDAO.putDataStats(connexion, typeofStat, data, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getPreparedStatements();
    assertNotNull(statements);
    assertThat(statements, hasSize(1));
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is("INSERT INTO SB_Stat_Connection(dateStat,userId,countConnection,"
        + "duration) VALUES(?,?,?,?)"));
    Map parameters = pstmt.getParameterMap();
    assertThat((String) parameters.get(1), is("2011-01-17"));
    assertThat((Integer) parameters.get(2), is(1620));
    assertThat((Long) parameters.get(3), is(1L));
    assertThat((Long) parameters.get(4), is(1223229L));
  }
}

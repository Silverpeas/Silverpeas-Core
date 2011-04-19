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

import com.mockrunner.jdbc.StatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockResultSet;
import com.mockrunner.mock.jdbc.MockConnection;
import com.google.common.collect.Lists;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import java.util.List;
import java.util.Map;
import com.mockrunner.jdbc.JDBCTestModule;
import com.mockrunner.mock.jdbc.JDBCMockObjectFactory;
import org.junit.Before;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import java.util.Calendar;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class ConnexionSilverStatisticsManagerDAOTest {

  private StatisticsConfig config;
  private JDBCMockObjectFactory factory;
  private JDBCTestModule module;
  private static final StatType typeofStat = StatType.Connexion;

  @Before
  public void initialiseConfig() throws Exception {
    config = new StatisticsConfig();
    config.init();
    factory = new JDBCMockObjectFactory();
    module = new JDBCTestModule(factory);
    module.setExactMatch(true);
  }

  public ConnexionSilverStatisticsManagerDAOTest() {
  }

  @Test
  public void testInsertDataCumul() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    List<String> data = Lists.newArrayList("2011-04-17", "1308", "512", "262");
    SilverStatisticsManagerDAO.insertDataStatsCumul(connexion, typeofStat, data, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getPreparedStatements();
    assertNotNull(statements);
    assertThat(statements, hasSize(1));
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(),is("INSERT INTO SB_Stat_ConnectionCumul(dateStat,userId,countConnection,"
        + "duration) VALUES(?,?,?,?)"));
    Map parameters = pstmt.getParameterMap();
    assertThat(parameters.size(), is(4));
    assertThat((String) parameters.get(1), is("2011-04-01"));
    assertThat((Integer) parameters.get(2), is(1308));
    assertThat((Long) parameters.get(3), is(512L));
    assertThat((Long) parameters.get(4), is(262L));
  }

  @Test
  public void testMakeStatCumulWithData() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    StatementResultSetHandler statementHandler = connexion.getStatementResultSetHandler();
    statementHandler.setExactMatch(true);
    MockResultSet result = statementHandler.createResultSet();
    result.addColumn("dateStat", new Object[]{"2011-04-17"});
    result.addColumn("userId", new Object[]{1308L});
    result.addColumn("countConnection", new Object[]{1024L});
    result.addColumn("duration", new Object[]{3005L});
    statementHandler.prepareResultSet("SELECT * FROM SB_Stat_Connection", result);
    MockResultSet cumulResult = statementHandler.createResultSet();
    cumulResult.addColumn("dateStat", new Object[]{"2011-04-01"});
    cumulResult.addColumn("userId", new Object[]{1308L});
    cumulResult.addColumn("countConnection", new Object[]{512L});
    cumulResult.addColumn("duration", new Object[]{500L});
    statementHandler.prepareResultSets("SELECT dateStat,userId,countConnection,duration FROM "
        + "SB_Stat_ConnectionCumul WHERE dateStat='2011-04-01' AND userId=1308", 
        new MockResultSet[]{cumulResult});
    SilverStatisticsManagerDAO.makeStatCumul(connexion, typeofStat, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getExecutedSQLStatements();
    assertNotNull(statements);
    assertThat(statements.size(), is(3));
    List<?> preparedStatements = module.getPreparedStatements();
    assertNotNull(preparedStatements);
    assertThat(preparedStatements, hasSize(1));
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is("UPDATE SB_Stat_ConnectionCumul SET countConnection=? ,"
        + "duration=?  WHERE dateStat='2011-04-01' AND userId=1308"));
    Map parameters = pstmt.getParameterMap();
    assertThat(parameters.size(), is(2));
    assertThat((Long) parameters.get(1), is(512L + 1024L));
    assertThat((Long) parameters.get(2), is(500L + 3005L));
  }
  
  @Test
  public void testMakeStatCumulWithoutExistingData() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    StatementResultSetHandler statementHandler = connexion.getStatementResultSetHandler();
    statementHandler.setExactMatch(true);
   MockResultSet result = statementHandler.createResultSet();
    result.addColumn("dateStat", new Object[]{"2011-04-17"});
    result.addColumn("userId", new Object[]{1308L});
    result.addColumn("countConnection", new Object[]{36L});
    result.addColumn("duration", new Object[]{12L});
    statementHandler.prepareResultSet("SELECT * FROM SB_Stat_Connection", result);
    statementHandler.prepareResultSet("SELECT * FROM SB_Stat_Access", result);
    MockResultSet cumulResult = statementHandler.createResultSet();
    statementHandler.prepareResultSets("SELECT dateStat,userId,countConnection,duration FROM "
        + "SB_Stat_ConnectionCumul WHERE dateStat='2011-04-01' AND userId=1308", 
        new MockResultSet[]{cumulResult});
    SilverStatisticsManagerDAO.makeStatCumul(connexion, typeofStat, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getExecutedSQLStatements();
    assertNotNull(statements);
    assertThat(statements.size(), is(3));
    List<?> preparedStatements = module.getPreparedStatements();
    assertNotNull(preparedStatements);
    assertThat(preparedStatements, hasSize(2));
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is("UPDATE SB_Stat_ConnectionCumul SET countConnection=? ,"
        + "duration=?  WHERE dateStat='2011-04-01' AND userId=1308"));
    Map parameters = pstmt.getParameterMap();
    assertThat(parameters.size(), is(0));
    pstmt = module.getPreparedStatement(1);
    assertThat(pstmt.getSQL(), is("INSERT INTO SB_Stat_ConnectionCumul(dateStat,userId,"
        + "countConnection,duration) VALUES(?,?,?,?)"));
    parameters = pstmt.getParameterMap();
    assertThat(parameters.size(), is(4));
    assertThat((String) parameters.get(1), is("2011-04-01"));
    assertThat((Integer) parameters.get(2), is(1308));
    assertThat((Long) parameters.get(3), is(36L));
    assertThat((Long) parameters.get(4), is(12L));
  }

  @Test
  public void testPurgeTablesCumul() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    SilverStatisticsManagerDAO.purgeTablesCumul(connexion, typeofStat, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getPreparedStatements();
    assertNotNull(statements);
    assertThat(statements, hasSize(1));
    Calendar calend = Calendar.getInstance();
    calend.add(Calendar.YEAR, -10);
    String date = SilverStatisticsManagerDAO.getRequestDate(calend.get(Calendar.YEAR),
        calend.get(Calendar.MONTH) + 1);
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is("DELETE FROM SB_Stat_ConnectionCumul WHERE dateStat<" + date));
    Map parameters = pstmt.getParameterMap();
    assertThat(parameters.size(), is(0));
  }

  @Test
  public void testDeleteTablesOfTheDay() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    SilverStatisticsManagerDAO.deleteTablesOfTheDay(connexion, typeofStat, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getPreparedStatements();
    assertNotNull(statements);
    assertThat(statements, hasSize(1));
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is("DELETE FROM SB_Stat_Connection"));
    Map parameters = pstmt.getParameterMap();
    assertThat(parameters.size(), is(0));
  }
}

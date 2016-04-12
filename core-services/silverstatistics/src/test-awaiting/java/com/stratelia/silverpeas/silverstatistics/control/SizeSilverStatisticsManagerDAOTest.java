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
import java.util.Calendar;
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
 *
 * @author ehugonnet
 */
public class SizeSilverStatisticsManagerDAOTest {

  private StatisticsConfig config;
  private JDBCMockObjectFactory factory;
  private JDBCTestModule module;
  private static final StatType typeofStat = StatType.Size;

  @Before
  public void initialiseConfig() throws Exception {
    config = new StatisticsConfig();
    config.init();
    factory = new JDBCMockObjectFactory();
    module = new JDBCTestModule(factory);
    module.setExactMatch(true);
  }

  public SizeSilverStatisticsManagerDAOTest() {
  }

  @Test
  public void testInsertDataCumul() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    List<String> data = Arrays.asList("2011-04-17", "/var/data/silverpeas/kmelia72", "262");
    SilverStatisticsManagerDAO.insertDataStatsCumul(connexion, typeofStat, data, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getPreparedStatements();
    assertNotNull(statements);
    assertThat(statements, hasSize(1));
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is("INSERT INTO SB_Stat_SizeDirCumul(dateStat,fileDir,sizeDir) "
        + "VALUES(?,?,?)"));
    Map parameters = pstmt.getParameterMap();
    assertThat(parameters.size(), is(3));
    assertThat((String) parameters.get(1), is("2011-04-01"));
    assertThat((String) parameters.get(2), is("/var/data/silverpeas/kmelia72"));
    assertThat((Long) parameters.get(3), is(262L));
  }

  @Test
  public void testPurgeTablesCumul() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    SilverStatisticsManagerDAO.purgeTablesCumul(connexion, typeofStat, config);
    module.verifyAllStatementsClosed();
    Calendar calend = Calendar.getInstance();
    calend.add(Calendar.YEAR, -10);
    String date = SilverStatisticsManagerDAO.getRequestDate(calend.get(Calendar.YEAR),
        calend.get(Calendar.MONTH) + 1);
    List<?> statements = module.getPreparedStatements();
    assertNotNull(statements);
    assertThat(statements, hasSize(1));
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is("DELETE FROM SB_Stat_SizeDirCumul WHERE dateStat<" + date));
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
    assertThat(pstmt.getSQL(), is("DELETE FROM SB_Stat_SizeDir"));
    Map parameters = pstmt.getParameterMap();
    assertThat(parameters.size(), is(0));
  }

  @Test
  public void testMakeStatCumulWithData() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    StatementResultSetHandler statementHandler = connexion.getStatementResultSetHandler();
    statementHandler.setExactMatch(true);
    MockResultSet result = statementHandler.createResultSet();
    result.addColumn("dateStat", new Object[]{"2011-04-17"});
    result.addColumn("fileDir", new Object[]{"/var/data/silverpeas/kmelia56"});
    result.addColumn("sizeDir", new Object[]{32L});
    statementHandler.prepareResultSet("SELECT * FROM SB_Stat_SizeDir", result);
    MockResultSet cumulResult = statementHandler.createResultSet();
    cumulResult.addColumn("dateStat", new Object[]{"2011-04-01"});
    cumulResult.addColumn("fileDir", new Object[]{"/var/data/silverpeas/kmelia56"});
    cumulResult.addColumn("sizeDir", new Object[]{100L});
    statementHandler.prepareResultSets("SELECT dateStat,fileDir,sizeDir FROM SB_Stat_SizeDirCumul "
        + "WHERE dateStat='2011-04-01' AND fileDir='/var/data/silverpeas/kmelia56'",
        new MockResultSet[]{
          cumulResult});
    SilverStatisticsManagerDAO.makeStatCumul(connexion, typeofStat, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getExecutedSQLStatements();
    assertNotNull(statements);
    assertThat(statements.size(), is(3));
    List<?> preparedStatements = module.getPreparedStatements();
    assertNotNull(preparedStatements);
    assertThat(preparedStatements, hasSize(1));
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is("UPDATE SB_Stat_SizeDirCumul SET sizeDir=?  WHERE "
        + "dateStat='2011-04-01' AND fileDir='/var/data/silverpeas/kmelia56'"));
    Map parameters = pstmt.getParameterMap();
    assertThat(parameters.size(), is(1));
    //Mode Replace
    assertThat((Long) parameters.get(1), is(32L));
  }

  @Test
  public void testMakeStatCumulWithoutExistingData() throws Exception {
    MockConnection connexion = factory.getMockConnection();
    StatementResultSetHandler statementHandler = connexion.getStatementResultSetHandler();
    statementHandler.setExactMatch(true);
    MockResultSet result = statementHandler.createResultSet();
    result.addColumn("dateStat", new Object[]{"2011-04-17"});
    result.addColumn("fileDir", new Object[]{"/var/data/silverpeas/kmelia36"});
    result.addColumn("sizeDir", new Object[]{543L});
    statementHandler.prepareResultSet("SELECT * FROM SB_Stat_SizeDir", result);
    MockResultSet cumulResult = statementHandler.createResultSet();
    statementHandler.prepareResultSet("SELECT dateStat,fileDir,sizeDir FROM SB_Stat_SizeDirCumul "
        + "WHERE dateStat='2011-04-01' AND fileDir='/var/data/silverpeas/kmelia36'", cumulResult);
    SilverStatisticsManagerDAO.makeStatCumul(connexion, typeofStat, config);
    module.verifyAllStatementsClosed();
    List<?> statements = module.getExecutedSQLStatements();
    assertNotNull(statements);
    assertThat(statements.size(), is(3));
    List<?> preparedStatements = module.getPreparedStatements();
    assertNotNull(preparedStatements);
    assertThat(preparedStatements, hasSize(2));
    MockPreparedStatement pstmt = module.getPreparedStatement(0);
    assertThat(pstmt.getSQL(), is("UPDATE SB_Stat_SizeDirCumul SET sizeDir=?  WHERE "
        + "dateStat='2011-04-01' AND fileDir='/var/data/silverpeas/kmelia36'"));
    Map parameters = pstmt.getParameterMap();
    assertThat(parameters.size(), is(0));
    pstmt = module.getPreparedStatement(1);
    assertThat(pstmt.getSQL(), is("INSERT INTO SB_Stat_SizeDirCumul(dateStat,fileDir,sizeDir) "
        + "VALUES(?,?,?)"));
    parameters = pstmt.getParameterMap();
    assertThat(parameters.size(), is(3));
    assertThat((String) parameters.get(1), is("2011-04-01"));
    assertThat((String) parameters.get(2), is("/var/data/silverpeas/kmelia36"));
    assertThat((Long) parameters.get(3), is(543L));

  }
}

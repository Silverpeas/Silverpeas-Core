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

package org.silverpeas.core.silverstatistics.volume.service;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.silverpeas.core.silverstatistics.volume.dao.SilverStatisticsManagerDAO;
import org.silverpeas.core.silverstatistics.volume.model.DataStatsCumul;
import org.silverpeas.core.silverstatistics.volume.model.StatisticsConfig;
import org.silverpeas.core.silverstatistics.volume.model.StatType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.DataSetTest;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.BasicWarBuilder;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class ConnexionSilverStatisticsManagerDAOTest extends DataSetTest {

  public static final Operation TABLES_CREATION =
      Operations.sequenceOf(Operations.sql("CREATE TABLE IF NOT EXISTS SB_Stat_Connection" +
          "(" +
          "    dateStat        varchar(10)  not null," +
          "    userId          integer  not null," +
          "    countConnection decimal(19)         not null," +
          "    duration        decimal(19)         not null    " +
          ")"), Operations.sql("CREATE TABLE IF NOT EXISTS SB_Stat_ConnectionCumul" +
          "(" +
          "    dateStat        varchar(10)   not null," +
          "    userId          integer  not null," +
          "    countConnection decimal(19)         not null," +
          "    duration        decimal(19)         not null" +
          ")"));

  public static final Operation DROP_ALL = Operations.sql("DROP TABLE IF EXISTS SB_Stat_Connection",
      "DROP TABLE IF EXISTS SB_Stat_ConnectionCumul");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(ConnexionSilverStatisticsManagerDAOTest.class)
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .addMavenDependencies("org.apache.tika:tika-core")
        .addMavenDependencies("org.apache.tika:tika-parsers")
        .createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud")
        .testFocusedOn(war -> {
          war.addPackages(true, "org.silverpeas.core.silverstatistics");
          war.addAsResource("org/silverpeas/silverstatistics/SilverStatisticsTest.properties");
          war.addAsResource("org/silverpeas/silverstatistics/SilverStatistics.properties");
          war.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
        }).build();
  }

  @Override
  protected Operation getDbSetupInitializations() {
    return Operations.sequenceOf(DROP_ALL, TABLES_CREATION);
  }

  private StatisticsConfig config;
  private static final StatType typeofStat = StatType.Connexion;

  @Before
  public void initialiseConfig() throws Exception {
    config = new StatisticsConfig();
    config.init();
  }

  @Test
  public void testInsertDataCumul() throws Exception {
    List<String> data = Arrays.asList("2011-04-17", "1308", "512", "262");
    try (Connection connection = getConnection()) {
      SilverStatisticsManagerDAO.insertDataStatsCumul(connection, typeofStat, data, config);
    }
    JdbcSqlQuery selectQuery = JdbcSqlQuery.createSelect("* FROM SB_Stat_ConnectionCumul");
    List<DataStatsCumul> results = selectQuery.execute(
        row -> new DataStatsCumul(row.getString(1), row.getInt(2), row.getLong(3), row.getLong(4)));
    assertThat(results, hasSize(1));
  }

  @Test
  @Ignore
  public void testMakeStatCumulWithData() throws Exception {
//    Connection connection = getConnection();
//    StatementResultSetHandler statementHandler = connection.getStatementResultSetHandler();
//    statementHandler.setExactMatch(true);
//    MockResultSet result = statementHandler.createResultSet();
//    result.addColumn("dateStat", new Object[]{"2011-04-17"});
//    result.addColumn("userId", new Object[]{1308L});
//    result.addColumn("countConnection", new Object[]{1024L});
//    result.addColumn("duration", new Object[]{3005L});
//    statementHandler.prepareResultSet("SELECT * FROM SB_Stat_Connection", result);
//    MockResultSet cumulResult = statementHandler.createResultSet();
//    cumulResult.addColumn("dateStat", new Object[]{"2011-04-01"});
//    cumulResult.addColumn("userId", new Object[]{1308L});
//    cumulResult.addColumn("countConnection", new Object[]{512L});
//    cumulResult.addColumn("duration", new Object[]{500L});
//    statementHandler.prepareResultSets("SELECT dateStat,userId,countConnection,duration FROM " +
//            "SB_Stat_ConnectionCumul WHERE dateStat='2011-04-01' AND userId=1308",
//        new MockResultSet[]{cumulResult});
//    SilverStatisticsManagerDAO.makeStatCumul(connection, typeofStat, config);
//    module.verifyAllStatementsClosed();
//    List<?> statements = module.getExecutedSQLStatements();
//    assertNotNull(statements);
//    assertThat(statements.size(), is(3));
//    List<?> preparedStatements = module.getPreparedStatements();
//    assertNotNull(preparedStatements);
//    assertThat(preparedStatements, hasSize(1));
//    MockPreparedStatement pstmt = module.getPreparedStatement(0);
//    assertThat(pstmt.getSQL(), is("UPDATE SB_Stat_ConnectionCumul SET countConnection=? ," +
//        "duration=?  WHERE dateStat='2011-04-01' AND userId=1308"));
//    Map parameters = pstmt.getParameterMap();
//    assertThat(parameters.size(), is(2));
//    assertThat((Long) parameters.get(1), is(512L + 1024L));
//    assertThat((Long) parameters.get(2), is(500L + 3005L));
  }

  @Test
  @Ignore
  public void testMakeStatCumulWithoutExistingData() throws Exception {
//    MockConnection connexion = factory.getMockConnection();
//    StatementResultSetHandler statementHandler = connexion.getStatementResultSetHandler();
//    statementHandler.setExactMatch(true);
//    MockResultSet result = statementHandler.createResultSet();
//    result.addColumn("dateStat", new Object[]{"2011-04-17"});
//    result.addColumn("userId", new Object[]{1308L});
//    result.addColumn("countConnection", new Object[]{36L});
//    result.addColumn("duration", new Object[]{12L});
//    statementHandler.prepareResultSet("SELECT * FROM SB_Stat_Connection", result);
//    statementHandler.prepareResultSet("SELECT * FROM SB_Stat_Access", result);
//    MockResultSet cumulResult = statementHandler.createResultSet();
//    statementHandler.prepareResultSets("SELECT dateStat,userId,countConnection,duration FROM " +
//            "SB_Stat_ConnectionCumul WHERE dateStat='2011-04-01' AND userId=1308",
//        new MockResultSet[]{cumulResult});
//    SilverStatisticsManagerDAO.makeStatCumul(connexion, typeofStat, config);
//    module.verifyAllStatementsClosed();
//    List<?> statements = module.getExecutedSQLStatements();
//    assertNotNull(statements);
//    assertThat(statements.size(), is(3));
//    List<?> preparedStatements = module.getPreparedStatements();
//    assertNotNull(preparedStatements);
//    assertThat(preparedStatements, hasSize(2));
//    MockPreparedStatement pstmt = module.getPreparedStatement(0);
//    assertThat(pstmt.getSQL(), is("UPDATE SB_Stat_ConnectionCumul SET countConnection=? ," +
//        "duration=?  WHERE dateStat='2011-04-01' AND userId=1308"));
//    Map parameters = pstmt.getParameterMap();
//    assertThat(parameters.size(), is(0));
//    pstmt = module.getPreparedStatement(1);
//    assertThat(pstmt.getSQL(), is("INSERT INTO SB_Stat_ConnectionCumul(dateStat,userId," +
//        "countConnection,duration) VALUES(?,?,?,?)"));
//    parameters = pstmt.getParameterMap();
//    assertThat(parameters.size(), is(4));
//    assertThat((String) parameters.get(1), is("2011-04-01"));
//    assertThat((Integer) parameters.get(2), is(1308));
//    assertThat((Long) parameters.get(3), is(36L));
//    assertThat((Long) parameters.get(4), is(12L));
  }

  @Test
  @Ignore
  public void testPurgeTablesCumul() throws Exception {
//    MockConnection connexion = factory.getMockConnection();
//    SilverStatisticsManagerDAO.purgeTablesCumul(connexion, typeofStat, config);
//    module.verifyAllStatementsClosed();
//    List<?> statements = module.getPreparedStatements();
//    assertNotNull(statements);
//    assertThat(statements, hasSize(1));
//    Calendar calend = Calendar.getInstance();
//    calend.add(Calendar.YEAR, -10);
//    String date = SilverStatisticsManagerDAO
//        .getRequestDate(calend.get(Calendar.YEAR), calend.get(Calendar.MONTH) + 1);
//    MockPreparedStatement pstmt = module.getPreparedStatement(0);
//    assertThat(pstmt.getSQL(), is("DELETE FROM SB_Stat_ConnectionCumul WHERE dateStat<" + date));
//    Map parameters = pstmt.getParameterMap();
//    assertThat(parameters.size(), is(0));
  }

  @Test
  @Ignore
  public void testDeleteTablesOfTheDay() throws Exception {
//    MockConnection connexion = factory.getMockConnection();
//    SilverStatisticsManagerDAO.deleteTablesOfTheDay(connexion, typeofStat, config);
//    module.verifyAllStatementsClosed();
//    List<?> statements = module.getPreparedStatements();
//    assertNotNull(statements);
//    assertThat(statements, hasSize(1));
//    MockPreparedStatement pstmt = module.getPreparedStatement(0);
//    assertThat(pstmt.getSQL(), is("DELETE FROM SB_Stat_Connection"));
//    Map parameters = pstmt.getParameterMap();
//    assertThat(parameters.size(), is(0));
  }
}

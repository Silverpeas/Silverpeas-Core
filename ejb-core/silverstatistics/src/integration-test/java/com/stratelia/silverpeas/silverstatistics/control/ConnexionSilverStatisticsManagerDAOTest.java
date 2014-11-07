/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import com.stratelia.silverpeas.silverstatistics.model.DataStatsCumul;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import com.stratelia.silverpeas.silverstatistics.util.StatType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.silvertrace.SilverpeasTrace;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.TransactionProvider;
import org.silverpeas.persistence.TransactionRuntimeException;
import org.silverpeas.persistence.jdbc.JdbcSqlQuery;
import org.silverpeas.test.TestSilverpeasTrace;
import org.silverpeas.test.lang.TestSystemWrapper;
import org.silverpeas.util.*;
import org.silverpeas.util.exception.FromModule;
import org.silverpeas.util.exception.RelativeFileAccessException;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import org.silverpeas.util.exception.UtilException;
import org.silverpeas.util.exception.WithNested;
import org.silverpeas.util.lang.SystemWrapper;
import org.silverpeas.util.pool.ConnectionPool;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class ConnexionSilverStatisticsManagerDAOTest {

  public ConnexionSilverStatisticsManagerDAOTest() {
  }

  @Resource(lookup = "java:/datasources/silverpeas")
  private DataSource dataSource;
  private DbSetupTracker dbSetupTracker = new DbSetupTracker();

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

//  public static final Operation INSERT_DATA = Operations.insertInto("SB_QuestionContainer_QC")
//      .columns("qcId", "qcTitle", "qcDescription", "qcComment", "qcCreatorId", "qcCreationDate",
//          "qcBeginDate", "qcEndDate", "qcIsClosed", "qcNbVoters", "qcNbQuestionsPage",
//          "qcNbMaxParticipations", "qcNbTriesBeforeSolution", "qcMaxTime", "instanceId",
//          "anonymous", "resultMode", "resultView")
//      .values(1, "Quiz express", "Description express du quiz", "Remarque express", "0",
//          "2012-01-13", "2012-01-13", "9999-99-99", 0, 1, 1, 10, 2, 0, "quizz83", 0, 1, 4)
//      .values(2, "Quiz expression sur les légumes",
//          "Expression française sur les fruits et légumes", "RAS", "0", "2012-01-12",
// "2012-01-12",
//          "9999-99-99", 0, 2, 1, 1, 1, 0, "quizz83", 0, 1, 4)
//      .values(3, "Quiz clos", "Description d'un quizz à ouvrir", "RAS", "0", "2012-01-12",
//          "2012-01-12", "9999-99-99", 1, 2, 1, 1, 1, 0, "quizz83", 0, 1, 4).build();


  @Deployment
  public static Archive<?> createTestArchive() {
    WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
        .addClasses(SilverpeasTrace.class, SilverTrace.class, TestSilverpeasTrace.class,
            WAPrimaryKey.class, ForeignPK.class);
    war.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml")
        .resolve("com.ninja-squad:DbSetup", "org.apache.commons:commons-lang3",
            "commons-codec:commons-codec", "commons-io:commons-io", "org.silverpeas.core:test-core",
            "org.quartz-scheduler:quartz").withTransitivity().asFile());
    war.addClasses(WithNested.class, FromModule.class, SilverpeasException.class,
        SilverpeasRuntimeException.class, UtilException.class);
    war.addClasses(ArrayUtil.class, StringUtil.class, MapUtil.class, CollectionUtil.class,
        ExtractionList.class, ExtractionComplexList.class, AssertArgument.class);
    war.addClasses(DBUtil.class, ConnectionPool.class, Transaction.class, TransactionProvider.class,
        TransactionRuntimeException.class);
    war.addClasses(ConfigurationClassLoader.class, ConfigurationControl.class, FileUtil.class,
        MimeTypes.class, RelativeFileAccessException.class, GeneralPropertiesManager.class,
        ResourceLocator.class, ResourceBundleWrapper.class, SystemWrapper.class,
        TestSystemWrapper.class);
    war.addClasses(DBUtil.class, ConnectionPool.class, Transaction.class, TransactionProvider.class,
        TransactionRuntimeException.class);
    war.addPackages(false, "org.silverpeas.persistence.jdbc");

    war.addPackages(true, "com.stratelia.silverpeas.silverstatistics");
    war.addPackages(true, "com.silverpeas.scheduler");

    war.addClasses(ServiceProvider.class, BeanContainer.class, CDIContainer.class)
        .addPackages(true, "org.silverpeas.initialization")
        .addAsResource("META-INF/services/test-org.silverpeas.util.BeanContainer",
            "META-INF/services/org.silverpeas.util.BeanContainer")
        .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
        .addAsResource("org/silverpeas/silverstatistics/SilverStatisticsTest.properties")
        .addAsResource("org/silverpeas/silverstatistics/SilverStatistics.properties")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    war.addAsWebInfResource("test-ds.xml", "test-ds.xml");
    return war;
  }

  @Before
  public void prepareDataSource() {
    Operation preparation = Operations.sequenceOf(DROP_ALL, TABLES_CREATION); //, INSERT_DATA
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);
  }

  private StatisticsConfig config;
  private static final StatType typeofStat = StatType.Connexion;

  @Before
  public void initialiseConfig() throws Exception {
    config = new StatisticsConfig();
    config.init();
  }

  private Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }

  @Test
  public void testInsertDataCumul() throws Exception {
    Connection connection = getConnection();
    List<String> data = Arrays.asList("2011-04-17", "1308", "512", "262");
    SilverStatisticsManagerDAO.insertDataStatsCumul(connection, typeofStat, data, config);
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

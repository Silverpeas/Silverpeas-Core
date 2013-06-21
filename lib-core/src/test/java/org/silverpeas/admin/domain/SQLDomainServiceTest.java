/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.admin.domain;

import com.silverpeas.util.PathTestUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.domains.sqldriver.SQLSettings;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.DomainDriver;
import com.stratelia.webactiv.beans.admin.DomainDriverManager;
import com.stratelia.webactiv.beans.admin.DomainDriverManagerFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.admin.domain.exception.DomainAuthenticationPropertiesAlreadyExistsException;
import org.silverpeas.admin.domain.exception.DomainPropertiesAlreadyExistsException;
import org.silverpeas.admin.domain.exception.NameAlreadyExistsInDatabaseException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author lbertin
 */
@RunWith(PowerMockRunner.class)
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
@PrepareForTest({FileRepositoryManager.class, DomainDriverManagerFactory.class,
  SilverpeasTemplateFactory.class})
public class SQLDomainServiceTest {

  private static ApplicationContext context;
  private DomainService service;
  private DataSource dataSource;
  private Domain[] allDomains = null;
  private File expectedDomainPropertiesFile;
  private File expectedDomainAuthenticationPropertiesFile;
  File tmpFile = null;

  public SQLDomainServiceTest() {
  }

  private void initAllDomains() {
    Domain domainSP =
        createDomain("1", "domainSilverpeas", "default domain for Silverpeas", "autDomainSP",
        "com.silverpeas.domains.silverpeasdriver.SilverpeasDomainDriver",
        "com.stratelia.silverpeas.domains.domainSP");
    Domain domainCustomers =
        createDomain("2", "Customers", "Customers active directory", "autDomainCustomers",
        "com.silverpeas.domains.silverpeasdriver.SilverpeasDomainDriver",
        "com.stratelia.silverpeas.domains.domainCustomers");

    allDomains = new Domain[]{domainSP, domainCustomers};
  }

  private Domain createDomain(String id, String name, String description,
      String authenticationServer, String className, String propFileName) {
    Domain newDomain = new Domain();
    newDomain.setId(id);
    newDomain.setName(name);
    newDomain.setDescription(description);
    newDomain.setAuthenticationServer(authenticationServer);
    newDomain.setDriverClassName(className);
    newDomain.setPropFileName(propFileName);
    newDomain.setSilverpeasServerURL("http://localhost:8000/silverpeas");
    newDomain.setTheTimeStamp("0");
    return newDomain;
  }

  @Before
  public void initTest() throws Exception {
    // Load Spring application context and get beans
    if (context == null) {
      context = new ClassPathXmlApplicationContext("/spring-domain.xml");
    }

    dataSource = (DataSource) context.getBean("jpaDataSource");
    service = (DomainService) context.getAutowireCapableBeanFactory().getBean("sqlDomainService");

    // Populate database
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        SQLDomainServiceTest.class.getClassLoader().getResourceAsStream(
        "org/silverpeas/admin/domain/domain-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);

    // Creates fake domains
    initAllDomains();

    // load expected properties files
    expectedDomainPropertiesFile =
        new File(PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar
        + "/org/silverpeas/admin/domain/expectedDomainPropertiesFile.properties");
    expectedDomainAuthenticationPropertiesFile =
        new File(PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar
        + "/org/silverpeas/admin/domain/expectedDomainAuthenticationPropertiesFile.properties");

    // initialize tmp directory
    tmpFile = File.createTempFile("domain", "TestCreation");
    tmpFile.delete();
    tmpFile.mkdir();

    // Mock FileRepositoryManager to generate domains/authentication properties into tmp folder
    mockStatic(FileRepositoryManager.class);
    when(FileRepositoryManager.getDomainPropertiesPath("3TestCrea"))
        .thenReturn(tmpFile.getAbsolutePath() + File.separator + "Domain3TestCrea.properties");
    when(FileRepositoryManager.getDomainAuthenticationPropertiesPath("3TestCrea"))
        .thenReturn(tmpFile.getAbsolutePath() + File.separator + "autDomain3TestCrea.properties");

    // cannot mock Admin as it is final class
    // Mock DomainDriverManager and DomainDriverManagerFactory to return test-domains list
    DomainDriver mockedDomainDriver = mock(DomainDriver.class);
    when(mockedDomainDriver.isSynchroThreaded()).thenReturn(false);

    DomainDriverManager mockedDomainDriverManager = mock(DomainDriverManager.class);
    when(mockedDomainDriverManager.getAllDomains()).thenReturn(allDomains);
    when(mockedDomainDriverManager.getNextDomainId()).thenReturn("3");
    when(mockedDomainDriverManager.createDomain(any(Domain.class))).thenReturn("3");
    when(mockedDomainDriverManager.getDomainDriver(anyInt())).thenReturn(mockedDomainDriver);

    mockStatic(DomainDriverManagerFactory.class);
    when(DomainDriverManagerFactory.getCurrentDomainDriverManager()).thenReturn(
        mockedDomainDriverManager);
  }

  @After
  public void cleanTest() throws Exception {
    ReplacementDataSet dataSet =
        new ReplacementDataSet(new FlatXmlDataSetBuilder().build(SQLDomainServiceTest.class
        .getClassLoader().getResourceAsStream(
        "org/silverpeas/admin/domain/domain-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.DELETE_ALL.execute(connection, dataSet);

    // remove tmp folder
    FileUtils.deleteQuietly(tmpFile);
  }

  @Test
  public void testGetTechnicalDomainName() {
    SQLDomainService sqlDomainService = (SQLDomainService) service;
    for (int i = 0; i < 1001; i += 100) {
      String domainId = String.valueOf(i);
      Domain domain = createDomain(domainId, "éèëêöôõòïîìñüûùçàäãâ°", null, null, null, null);
      String technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
      asserTachnicalDomainName(technicalDomainName, domainId, "eeeeooooiiinuuucaaaa");
    }
    Domain domain = createDomain("0", "ïîìñüûùçàäãâ°éèëêöôõò", null, null, null, null);
    String technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
    assertThat(technicalDomainName, is("0iiinuuuc"));

    domain = createDomain("0", "àäãâ°éèëêöôõòïîìñüûùç", null, null, null, null);
    technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
    assertThat(technicalDomainName, is("0aaaaeeee"));

    domain =
        createDomain("0", " &~#\"'{([-|`_\\^@°)]}+=¨£$¤%*µ<>?,.;/:§!€", null, null, null, null);
    technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
    assertThat(technicalDomainName, is("0"));

    domain = createDomain("0", "x²", null, null, null, null);
    technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
    assertThat(technicalDomainName, is("0x2"));

    domain = createDomain("0", "X²AbCd", null, null, null, null);
    technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
    assertThat(technicalDomainName, is("0X2AbCd"));
  }

  /**
   * Common assertion method.
   * @param technicalDomainName
   * @param domainId
   * @param normalizedDomainName
   */
  private void asserTachnicalDomainName(final String technicalDomainName, String domainId,
      String normalizedDomainName) {
    String expectedTechnicalDomainName = StringUtil
        .left(domainId + normalizedDomainName, SQLSettings.DATABASE_TABLE_NAME_MAX_LENGTH - 21);
    assertThat(technicalDomainName, is(expectedTechnicalDomainName));
  }

  @Test
  @Transactional
  public void testCreateDomain() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestCreation");

    // create domain
    String domainId = service.createDomain(domain);

    // Performs checks on id returned
    assertThat("domainId returned is NULL", domainId, is(notNullValue()));
    assertThat("domainId returned = -1", domainId, is(not("-1")));
    assertThat("domainId returned is empty", domainId, is(not("")));

    // Performs checks on generated properties files
    boolean domainPropFileFound = false;
    boolean authenticationPropFileFound = false;
    for (File file : tmpFile.listFiles()) {
      if (file.getName().equals("Domain3TestCrea.properties")) {
        domainPropFileFound = true;
        assertThat("domain properties files generated content is incorrect",
            FileUtils.contentEquals(file, expectedDomainPropertiesFile), is(true));
      } else if (file.getName().equals("autDomain3TestCrea.properties")) {
        authenticationPropFileFound = true;
        assertThat("domain authentication properties files generated content is incorrect",
            FileUtils.contentEquals(file, expectedDomainAuthenticationPropertiesFile), is(true));
      }
    }
    assertThat("domain properties files has not been generated", domainPropFileFound, is(true));
    assertThat("domain authentication properties files has not been generated",
        authenticationPropFileFound, is(true));

    // Performs checks on generated tables
    testTablesExistence(true);
  }

  @Test
  @Transactional
  public void testCreateDomainAlreadyInDB() throws Exception {
    Domain domain = new Domain();
    domain.setName("Customers");
    try {
      service.createDomain(domain);
      fail("Exception must have been thrown");
    } catch (Exception e) {
      assertThat(e instanceof NameAlreadyExistsInDatabaseException, is(true));
    }
  }

  @Test
  @Transactional
  public void testCreateDomainWithPropertiesNameConflicts() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestCreation");

    File conflictousPropertiesFile = new File(tmpFile, "Domain3TestCrea.properties");
    FileUtils.touch(conflictousPropertiesFile);
    conflictousPropertiesFile.deleteOnExit();
    try {
      service.createDomain(domain);
      fail("Exception must have been thrown");
    } catch (Exception e) {
      assertThat(e instanceof DomainPropertiesAlreadyExistsException, is(true));
    }

    conflictousPropertiesFile.delete();
    conflictousPropertiesFile = new File(tmpFile, "autDomain3TestCrea.properties");
    FileUtils.touch(conflictousPropertiesFile);
    // create domain
    try {
      service.createDomain(domain);
      fail("Exception must have been thrown");
    } catch (Exception e) {
      assertThat(e instanceof DomainAuthenticationPropertiesAlreadyExistsException, is(true));
    }
  }

  private void testTablesExistence(boolean mustExists) throws SQLException {
    Connection connection = null;
    Statement stat = null;
    ResultSet rs = null;

    boolean userTableFound = false;
    boolean groupTableFound = false;
    boolean groupUserRelTableFound = false;

    try {
      connection = dataSource.getConnection();
      stat = connection.createStatement();
      rs = stat.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES");

      while (rs.next()) {
        String tableName = rs.getString("TABLE_NAME");
        if (tableName.equalsIgnoreCase("domain3TestCrea_User")) {
          userTableFound = true;
        } else if (tableName.equalsIgnoreCase("domain3TestCrea_Group")) {
          groupTableFound = true;
        } else if (tableName.equalsIgnoreCase("domain3TestCrea_Group_User_Rel")) {
          groupUserRelTableFound = true;
        }
      }
    } finally {
      DBUtil.close(rs, stat);
    }

    // Performs checks
    if (mustExists) {
      assertThat("User table has not been created", userTableFound, is(true));
      assertThat("Group table has not been created", groupTableFound, is(true));
      assertThat("Group_User_Rel table has not been created", groupUserRelTableFound, is(true));
    } else {
      assertThat("User table has not been dropped", userTableFound, is(false));
      assertThat("Group table has not been dropped", groupTableFound, is(false));
      assertThat("Group_User_Rel table has not been dropped", groupUserRelTableFound, is(false));
    }
  }
}

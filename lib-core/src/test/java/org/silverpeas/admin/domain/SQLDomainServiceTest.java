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

package org.silverpeas.admin.domain;

import static com.silverpeas.util.template.SilverpeasTemplate.TEMPLATE_CUSTOM_DIR;
import static com.silverpeas.util.template.SilverpeasTemplate.TEMPLATE_ROOT_DIR;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.admin.domain.exception.DomainAuthenticationPropertiesAlreadyExistsException;
import org.silverpeas.admin.domain.exception.DomainPropertiesAlreadyExistsException;
import org.silverpeas.admin.domain.exception.NameAlreadyExistsInDatabaseException;
import org.silverpeas.admin.domain.exception.NonAlphaNumericDetectedException;
import org.silverpeas.admin.domain.exception.WhiteSpacesDetectedException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.silverpeas.util.PathTestUtil;
import com.silverpeas.util.template.SilverpeasStringTemplate;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.DomainDriver;
import com.stratelia.webactiv.beans.admin.DomainDriverManager;
import com.stratelia.webactiv.beans.admin.DomainDriverManagerFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;

/**
 * @author lbertin
 */
@RunWith(PowerMockRunner.class)
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
@PrepareForTest({ FileRepositoryManager.class, DomainDriverManagerFactory.class,
    SilverpeasTemplateFactory.class })
public class SQLDomainServiceTest {
  private static ApplicationContext context;
  private DomainService service;
  private DataSource dataSource;

  private Domain[] allDomains = null;
  private File expectedDomainPropertiesFile;
  private File expectedDomainAuthenticationPropertiesFile;
  File tmpFile = null;

  private static String rootDir = PathTestUtil.TARGET_DIR +
      "test-classes" + File.separatorChar + "templates" + File.separatorChar;
  private static Properties templateConfiguration = new Properties();

  @BeforeClass
  public static void setUp() {
    templateConfiguration.setProperty(TEMPLATE_ROOT_DIR, rootDir);
    templateConfiguration.setProperty(TEMPLATE_CUSTOM_DIR, rootDir);
  }

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

    allDomains = new Domain[] { domainSP, domainCustomers };
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
        new File(PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar +
            "/org/silverpeas/admin/domain/expectedDomainPropertiesFile.properties");
    expectedDomainAuthenticationPropertiesFile =
        new File(PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar +
            "/org/silverpeas/admin/domain/expectedDomainAuthenticationPropertiesFile.properties");

    // initialize tmp directory
    tmpFile = File.createTempFile("domain", "TestCreation");
    tmpFile.delete();
    tmpFile.mkdir();

    // Mock FileRepositoryManager to generate domains/authentication properties into tmp folder
    mockStatic(FileRepositoryManager.class);
    when(FileRepositoryManager.getDomainPropertiesPath("TestCreation")).thenReturn(
        tmpFile.getAbsolutePath() + File.separator + "DomainTestCreation.properties");
    when(FileRepositoryManager.getDomainAuthenticationPropertiesPath("TestCreation")).thenReturn(
        tmpFile.getAbsolutePath() + File.separator + "autDomainTestCreation.properties");

    // cannot mock Admin as it is final class
    // Mock DomainDriverManager and DomainDriverManagerFactory to return test-domains list
    DomainDriver mockedDomainDriver = mock(DomainDriver.class);
    when(mockedDomainDriver.isSynchroThreaded()).thenReturn(false);

    DomainDriverManager mockedDomainDriverManager = mock(DomainDriverManager.class);
    when(mockedDomainDriverManager.getAllDomains()).thenReturn(allDomains);
    when(mockedDomainDriverManager.createDomain(any(Domain.class))).thenReturn("3");
    when(mockedDomainDriverManager.getDomainDriver(anyInt())).thenReturn(mockedDomainDriver);

    mockStatic(DomainDriverManagerFactory.class);
    when(DomainDriverManagerFactory.getCurrentDomainDriverManager()).thenReturn(
        mockedDomainDriverManager);

    mockStatic(SilverpeasTemplateFactory.class);
    when(SilverpeasTemplateFactory.createSilverpeasTemplate((Properties) Mockito.anyObject()))
        .thenReturn(new SilverpeasStringTemplate(templateConfiguration));
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
    FileUtils.deleteDirectory(tmpFile);
  }

  @Test
  @Transactional
  public void testCreateDomain() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestCreation");

    // create domain
    String domainId = service.createDomain(domain);

    // Performs checks on id returned
    Assert.assertNotNull("domainId returned is NULL", domainId);
    Assert.assertNotSame("domainId returned = -1", "-1", domainId);
    Assert.assertNotSame("domainId returned is empty", "", domainId);

    // Performs checks on generated properties files
    boolean domainPropFileFound = false;
    boolean authenticationPropFileFound = false;
    for (File file : tmpFile.listFiles()) {
      if (file.getName().equals("DomainTestCreation.properties")) {
        domainPropFileFound = true;
        Assert.assertTrue("domain properties files generated content is incorrect",
            FileUtils.contentEquals(file, expectedDomainPropertiesFile));
      }
      else if (file.getName().equals("autDomainTestCreation.properties")) {
        authenticationPropFileFound = true;
        Assert.assertTrue("domain authentication properties files generated content is incorrect",
            FileUtils.contentEquals(file, expectedDomainAuthenticationPropertiesFile));
      }
    }
    Assert.assertTrue("domain properties files has not been generated", domainPropFileFound);
    Assert.assertTrue("domain authentication properties files has not been generated",
        authenticationPropFileFound);

    // Performs checks on generated tables
    testTablesExistence(true);
  }

  @Test
  @Transactional
  public void testCreateDomainWithWhiteSpaces() throws Exception {
    Domain domain = new Domain();
    domain.setName("Test Creation");

    // create domain
    String domainId;
    try {
      domainId = service.createDomain(domain);
      Assert.fail("Exception must have been thrown");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof WhiteSpacesDetectedException);
    }
  }

  @Test
  @Transactional
  public void testCreateDomainWithNonAlphaChars() throws Exception {
    Domain domain = new Domain();
    domain.setName("Test+Creation");

    // create domain
    String domainId;
    try {
      domainId = service.createDomain(domain);
      Assert.fail("Exception must have been thrown");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof NonAlphaNumericDetectedException);
    }
  }

  @Test
  @Transactional
  public void testCreateDomainAlreadyInDB() throws Exception {
    Domain domain = new Domain();
    domain.setName("Customers");

    // create domain
    String domainId;
    try {
      domainId = service.createDomain(domain);
      Assert.fail("Exception must have been thrown");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof NameAlreadyExistsInDatabaseException);
    }
  }

  @Test
  @Transactional
  public void testCreateDomainWithPropertiesNameConflicts() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestCreation");

    File conflictousPropertiesFile = new File(tmpFile, "DomainTestCreation.properties");
    FileUtils.touch(conflictousPropertiesFile);
    conflictousPropertiesFile.deleteOnExit();

    // create domain
    String domainId;
    try {
      domainId = service.createDomain(domain);
      Assert.fail("Exception must have been thrown");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof DomainPropertiesAlreadyExistsException);
    }

    conflictousPropertiesFile.delete();
    conflictousPropertiesFile = new File(tmpFile, "autDomainTestCreation.properties");
    FileUtils.touch(conflictousPropertiesFile);
    // create domain
    try {
      domainId = service.createDomain(domain);
      Assert.fail("Exception must have been thrown");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof DomainAuthenticationPropertiesAlreadyExistsException);
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
        if (tableName.equalsIgnoreCase("domainTestCreation_User")) {
          userTableFound = true;
        }
        else if (tableName.equalsIgnoreCase("domainTestCreation_Group")) {
          groupTableFound = true;
        }
        else if (tableName.equalsIgnoreCase("domainTestCreation_Group_User_Rel")) {
          groupUserRelTableFound = true;
        }
      }
    } finally {
      DBUtil.close(rs, stat);
    }

    // Performs checks
    if (mustExists) {
      Assert.assertTrue("User table has not been created", userTableFound);
      Assert.assertTrue("Group table has not been created", groupTableFound);
      Assert.assertTrue("Group_User_Rel table has not been created", groupUserRelTableFound);
    }
    else {
      Assert.assertFalse("User table has not been dropped", userTableFound);
      Assert.assertFalse("Group table has not been dropped", groupTableFound);
      Assert.assertFalse("Group_User_Rel table has not been dropped", groupUserRelTableFound);
    }
  }

  private void insertDomainTables() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        SQLDomainServiceTest.class.getClassLoader().getResourceAsStream(
            "org/silverpeas/admin/domain/repository/domain-dataset-TestCreation.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
  }

  private SilverpeasTemplate getNewTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
  }

}

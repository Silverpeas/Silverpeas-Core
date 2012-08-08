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

package org.silverpeas.admin.domain.repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author lbertin
 */
@RunWith(PowerMockRunner.class)
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
@PrepareForTest(FileRepositoryManager.class)
@PowerMockIgnore({"org.apache.log4j", "javax.xml.parsers"})
public class SQLInternalDomainRepositoryTest {
  private static ApplicationContext context;
  private SQLDomainRepository dao;
  private DataSource dataSource;

  public SQLInternalDomainRepositoryTest() {

  }

  @BeforeClass
  public static void generalSetUp() {
  }

  @Before
  public void initTest() throws Exception {
    // Load Spring application context and get beans
    if (context == null) {
      context = new ClassPathXmlApplicationContext("/spring-domain.xml");
    }

    dataSource = (DataSource) context.getBean("jpaDataSource");
    dao =
        (SQLDomainRepository) context.getAutowireCapableBeanFactory().getBean(
            "sqlInternalDomainRepository");

    // Populate database
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        SQLInternalDomainRepositoryTest.class.getClassLoader().getResourceAsStream(
            "org/silverpeas/admin/domain/repository/domain-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
  }

  @After
  public void cleanTest() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        SQLInternalDomainRepositoryTest.class.getClassLoader().getResourceAsStream(
            "org/silverpeas/admin/domain/repository/domain-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
  }

  @Test
  @Transactional
  public void testCreateDomainStorage() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestCreation");

    File tmpFile = null;
    try {
      tmpFile = File.createTempFile("domain", "TestCreation");
      populateFile(tmpFile);

      // Mock FileRepositoryManager
      mockStatic(FileRepositoryManager.class);
      when(FileRepositoryManager.getDomainPropertiesPath("TestCreation")).thenReturn(
          tmpFile.getAbsolutePath());

      // Create domain storage
      dao.createDomainStorage(domain);

      // Looks for domain created tables
      testTablesExistence(true);

    } catch (Exception e) {
      tmpFile.delete();
      throw e;
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

  @Test
  @Transactional
  public void testDeleteDomainStorage() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestCreation");

    testTablesExistence(true);

    // Create domain storage
    dao.deleteDomainStorage(domain);

    // Looks for domain created tables
    testTablesExistence(false);
  }

  private void populateFile(File tmpFile) throws IOException {
    FileWriter writer = null;

    try {
      writer = new FileWriter(tmpFile);
      writer.append("property.Number = 4\n");
      writer
          .append("property.ResourceFile = com.stratelia.silverpeas.domains.multilang.templateDomainSQLBundle\n");

      writer.append("property_1.Name = title\n");
      writer.append("property_1.Type = STRING\n");
      writer.append("property_1.MapParameter = title\n");

      writer.append("property_2.Name = company\n");
      writer.append("property_2.Type = STRING\n");
      writer.append("property_2.MapParameter = company\n");

      writer.append("property_3.Name = position\n");
      writer.append("property_3.Type = STRING\n");
      writer.append("property_3.MapParameter = position\n");

      writer.append("property_4.Name = boss\n");
      writer.append("property_4.Type = USERID\n");
      writer.append("property_4.MapParameter = boss\n");
    } finally {
      writer.close();
    }
  }

}

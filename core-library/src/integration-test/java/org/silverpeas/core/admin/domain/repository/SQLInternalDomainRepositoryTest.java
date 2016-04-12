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
package org.silverpeas.core.admin.domain.repository;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.silverpeas.core.admin.domain.model.Domain;
import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.DataSetTest;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.lang.SystemWrapper;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import static org.apache.commons.io.FileUtils.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.createCountFor;

/**
 * @author lbertin
 */
@RunWith(Arquillian.class)
public class SQLInternalDomainRepositoryTest extends DataSetTest {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Inject
  private SQLDomainRepository dao;

  private File silverpeasHome;

  public static final Operation TABLES_CREATION = Operations
      .sql("CREATE TABLE st_domain (id int PRIMARY KEY NOT NULL , name varchar(100) not NULL, " +
              "description varchar(400) not NULL, propfilename varchar(100) not NULL, " +
              "classname varchar(100) not NULL, authenticationserver varchar(100) not NULL, " +
              "thetimestamp varchar(100) not NULL, silverpeasserverurl varchar(400) not NULL )",
          "CREATE TABLE domainTestDeletion_user (id integer NOT NULL, " +
              "firstname character varying(100), lastname character varying(100) )",
          "CREATE TABLE domainTestDeletion_group (id integer NOT NULL, " +
              "name character varying(100) NOT NULL)",
          "CREATE TABLE domainTestDeletion_group_user_rel (groupid integer NOT NULL, " +
              "userid integer NOT NULL)");
  public static final Operation DROP_ALL = Operations
      .sql("DROP TABLE IF EXISTS st_domain", "DROP TABLE IF EXISTS domainTestDeletion_user",
          "DROP TABLE IF EXISTS domainTestDeletion_group",
          "DROP TABLE IF EXISTS domainTestDeletion_group_user_rel");
  public static final Operation DEFAULT_DOMAIN_SET_UP = Operations.insertInto("st_domain")
      .columns("id", " name", " description", " propfilename", " classname",
          " authenticationserver", " thetimestamp", " silverpeasserverurl")
      .values(1, "domainSilverpeas", "Domain interne à Silverpeas",
          "org.silverpeas.domains.domainSP",
          "org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver", "autDomainSP", "0",
          "autDomainSP").build();
  public static final Operation TEST_DELETION_TABLES = Operations.sequenceOf(
      Operations.insertInto("domainTestDeletion_user").columns("id", "firstname", "lastname")
          .values(1, "Fernand", "Naudin").build(),
      Operations.insertInto("domainTestDeletion_group").columns("id", "name")
          .values(1, "Tontons Flingueurs").build(),
      Operations.insertInto("domainTestDeletion_group_user_rel").columns("groupid", "userid")
          .values(1, 1).build());

  @Override
  protected Operation getDbSetupInitializations() {
    return Operations
        .sequenceOf(DROP_ALL, TABLES_CREATION, DEFAULT_DOMAIN_SET_UP, TEST_DELETION_TABLES);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SQLInternalDomainRepositoryTest.class)
        .addSilverpeasExceptionBases()
        .addFileRepositoryFeatures()
        .addJpaPersistenceFeatures()
        .addAdministrationFeatures()
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.core.admin.domain");
        }).build();
  }

  @Before
  public void setup() throws Exception {
    silverpeasHome = getFile(mavenTargetDirectoryRule.getBuildDirFile(), "SILVERPEAS_HOME");
    SystemWrapper.get().getenv().put("SILVERPEAS_HOME", silverpeasHome.getPath());
  }

  @After
  public void unsetSilverpeasHome() throws Exception {
    deleteQuietly(silverpeasHome);
  }

  @Test
  public void testCreateDomainStorage() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestCreation");

    try {

      File testCreationFile =
          getFile(FileRepositoryManager.getDomainPropertiesPath("TestCreation"));
      touch(testCreationFile);
      populateFile(testCreationFile);

      // Create domain storage
      Transaction.performInOne(() -> {
        dao.createDomainStorage(domain);
        return null;
      });

      // Looks for domain created tables
      testTablesExistence(domain.getName(), true);

    } catch (Exception e) {
      throw e;
    }
  }

  private void testTablesExistence(final String name, boolean mustExists) throws SQLException {
    boolean userTableFound = createCountFor("INFORMATION_SCHEMA.TABLES")
        .where("lower(TABLE_NAME) = lower(?)", "domain" + name + "_User").execute() == 1;
    boolean groupTableFound = createCountFor("INFORMATION_SCHEMA.TABLES")
        .where("lower(TABLE_NAME) = lower(?)", "domain" + name + "_Group").execute() == 1;
    boolean groupUserRelTableFound = createCountFor("INFORMATION_SCHEMA.TABLES")
        .where("lower(TABLE_NAME) = lower(?)", "domain" + name + "_Group_User_Rel").execute() == 1;

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

  @Test
  public void testDeleteDomainStorage() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestDeletion");

    testTablesExistence(domain.getName(), true);

    // Delete domain storage
    Transaction.performInOne(() -> {
      dao.deleteDomainStorage(domain);
      return null;
    });

    // Looks for domain created tables
    testTablesExistence(domain.getName(), false);
  }

  private void populateFile(File tmpFile) throws IOException {
    FileWriter writer = new FileWriter(tmpFile);
    try {
      writer.append("property.Number = 5\n");
      writer.
          append("property.ResourceFile = org.silverpeas.domains.multilang" +
              ".templateDomainSQLBundle\n");

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

      writer.append("property_5.Name = test\n");
      writer.append("property_5.Type = BOOLEAN\n");
      writer.append("property_5.MapParameter = test\n");
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }
}

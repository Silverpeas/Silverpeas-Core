/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.repository;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.silverpeas.core.admin.domain.model.Domain;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.test.integration.DataSetTest;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.integration.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.kernel.util.SystemWrapper;

import jakarta.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.io.FileUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.fail;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.countAll;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.insertInto;

/**
 * @author lbertin
 */
@RunWith(Arquillian.class)
public class SQLInternalDomainRepositoryIT extends DataSetTest {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  /**
   * The technical name of a SQL domain is at most 9 characters long (see
   * SQLDomainService#getTechnicalDomainName), so that the names of its tables don't exceed the
   * 30 characters allowed by some databases like Oracle.
   */
  private static final String CREATED_DOMAIN_NAME = "5RepoCrea";
  private static final String CREATED_DOMAIN_TABLE = "domain" + CREATED_DOMAIN_NAME;
  private static final int MAX_IDENTIFIER_LENGTH = 30;

  @Inject
  private SQLDomainRepository dao;

  private File silverpeasHome;

  public static final Operation TABLES_CREATION = Operations
      .sql("CREATE TABLE st_domain (id int PRIMARY KEY NOT NULL , name varchar(100) not NULL, " +
              "description varchar(400) not NULL, propfilename varchar(100) not NULL, " +
              "classname varchar(100) not NULL, authenticationserver varchar(100) not NULL, " +
              "thetimestamp varchar(100) not NULL, silverpeasserverurl varchar(400) not NULL )",
          "CREATE TABLE domainTestDeletion_user (id integer NOT NULL, " +
              "firstname character varying(100), lastname character varying(100), " +
              "CONSTRAINT PK_DomainTestDeletion_User PRIMARY KEY (id))",
          "CREATE TABLE domainTestDeletion_group (id integer NOT NULL, " +
              "name character varying(100) NOT NULL, " +
              "CONSTRAINT PK_DomainTestDeletion_Group PRIMARY KEY (id))",
          "CREATE TABLE domainTestDeletion_group_user_rel (groupid integer NOT NULL, " +
              "userid integer NOT NULL, " +
              "CONSTRAINT FK_DomainTestDeletion_GroupUser_1 FOREIGN KEY (groupid) " +
              "REFERENCES domainTestDeletion_group(id), " +
              "CONSTRAINT FK_DomainTestDeletion_GroupUser_2 FOREIGN KEY (userid) " +
              "REFERENCES domainTestDeletion_user(id))");
  public static final Operation DROP_ALL = Operations
      .sql("DROP TABLE IF EXISTS st_domain",
          "DROP TABLE IF EXISTS domainTestDeletion_group_user_rel",
          "DROP TABLE IF EXISTS domainTestDeletion_group",
          "DROP TABLE IF EXISTS domainTestDeletion_user",
          "DROP TABLE IF EXISTS " + CREATED_DOMAIN_TABLE + "_group_user_rel",
          "DROP TABLE IF EXISTS " + CREATED_DOMAIN_TABLE + "_group",
          "DROP TABLE IF EXISTS " + CREATED_DOMAIN_TABLE + "_user");
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
    return LibCoreWarBuilder.onFullWarForTestClass(SQLInternalDomainRepositoryIT.class)
        .build();
  }

  @Before
  public void setup() throws Exception {
    silverpeasHome = getFile(mavenTargetDirectoryRule.getBuildDirFile(), "SILVERPEAS_HOME");
    SystemWrapper.getInstance().getenv().put("SILVERPEAS_HOME", silverpeasHome.getPath());
  }

  @After
  public void unsetSilverpeasHome() {
    deleteQuietly(new File(FileRepositoryManager.getDomainPropertiesPath(CREATED_DOMAIN_NAME)));
    deleteQuietly(silverpeasHome);
  }

  @Test
  public void testCreateDomainStorage() throws Exception {
    createDomainStorage();

    // Looks for domain created tables
    testTablesExistence(CREATED_DOMAIN_NAME, true);
  }

  @Test
  public void createdDomainStorageHasSameIntegrityConstraintsAsTheDefaultDomain()
      throws Exception {
    createDomainStorage();

    String prefix = "Domain" + CREATED_DOMAIN_NAME;
    assertThat(constraintsOf(CREATED_DOMAIN_TABLE + "_User"), containsInAnyOrder(
        constraint("PK_" + prefix + "_User", "PRIMARY KEY"),
        constraint("UN_" + prefix + "_User_1", "UNIQUE")));
    assertThat(constraintsOf(CREATED_DOMAIN_TABLE + "_Group"), containsInAnyOrder(
        constraint("PK_" + prefix + "_Group", "PRIMARY KEY"),
        constraint("UN_" + prefix + "_Group_1", "UNIQUE"),
        constraint("FK_" + prefix + "_Group_1", "FOREIGN KEY")));
    assertThat(constraintsOf(CREATED_DOMAIN_TABLE + "_Group_User_Rel"), containsInAnyOrder(
        constraint("PK_" + prefix + "_GroupUser", "PRIMARY KEY"),
        constraint("FK_" + prefix + "_GroupUser_1", "FOREIGN KEY"),
        constraint("FK_" + prefix + "_GroupUser_2", "FOREIGN KEY")));
  }

  @Test
  public void constraintNamesDoNotExceedTheMaxLengthOfIdentifiersInOracle() throws Exception {
    createDomainStorage();

    List<String> constraints = new ArrayList<>();
    constraints.addAll(constraintsOf(CREATED_DOMAIN_TABLE + "_User"));
    constraints.addAll(constraintsOf(CREATED_DOMAIN_TABLE + "_Group"));
    constraints.addAll(constraintsOf(CREATED_DOMAIN_TABLE + "_Group_User_Rel"));
    assertThat(constraints, hasSize(8));
    constraints.forEach(c -> assertThat(c, c.substring(0, c.indexOf(' ')).length(),
        lessThanOrEqualTo(MAX_IDENTIFIER_LENGTH)));
  }

  @Test
  public void createdDomainStorageRejectsInconsistentData() throws Exception {
    createDomainStorage();
    String userTable = CREATED_DOMAIN_TABLE + "_User";
    String groupTable = CREATED_DOMAIN_TABLE + "_Group";
    String relTable = CREATED_DOMAIN_TABLE + "_Group_User_Rel";
    execute(insertInto(userTable).withInsertParam("id", 1).withInsertParam("login", "fnaudin")
        .withInsertParam("test", 0));
    execute(insertInto(groupTable).withInsertParam("id", 1).withInsertParam("name", "Tontons")
        );
    execute(insertInto(relTable).withInsertParam("groupId", 1).withInsertParam("userId", 1));

    // same id
    assertRejected(insertInto(userTable).withInsertParam("id", 1)
        .withInsertParam("login", "mvolfoni").withInsertParam("test", 0));
    // same login
    assertRejected(insertInto(userTable).withInsertParam("id", 2)
        .withInsertParam("login", "fnaudin").withInsertParam("test", 0));
    // same id
    assertRejected(insertInto(groupTable).withInsertParam("id", 1)
        .withInsertParam("name", "Flingueurs"));
    // unknown parent group
    assertRejected(insertInto(groupTable).withInsertParam("id", 2)
        .withInsertParam("superGroupId", 3).withInsertParam("name", "Flingueurs"));
    // same name in the same parent group
    execute(insertInto(groupTable).withInsertParam("id", 2).withInsertParam("superGroupId", 1)
        .withInsertParam("name", "Flingueurs"));
    assertRejected(insertInto(groupTable).withInsertParam("id", 3)
        .withInsertParam("superGroupId", 1).withInsertParam("name", "Flingueurs"));
    // same membership
    assertRejected(insertInto(relTable).withInsertParam("groupId", 1)
        .withInsertParam("userId", 1));
    // unknown group
    assertRejected(insertInto(relTable).withInsertParam("groupId", 5)
        .withInsertParam("userId", 1));
    // unknown user
    assertRejected(insertInto(relTable).withInsertParam("groupId", 1)
        .withInsertParam("userId", 5));
  }

  private void createDomainStorage() throws Exception {
    Domain domain = new Domain();
    domain.setName(CREATED_DOMAIN_NAME);

    File domainFile = getFile(FileRepositoryManager.getDomainPropertiesPath(CREATED_DOMAIN_NAME));
    touch(domainFile);
    populateFile(domainFile);

    Transaction.performInOne(() -> {
      dao.createDomainStorage(domain);
      return null;
    });
  }

  private static String constraint(final String name, final String type) {
    return name.toUpperCase() + " " + type;
  }

  private List<String> constraintsOf(final String tableName) throws SQLException {
    return JdbcSqlQuery.select("CONSTRAINT_NAME, CONSTRAINT_TYPE")
        .from("INFORMATION_SCHEMA.TABLE_CONSTRAINTS")
        .where("lower(TABLE_NAME) = lower(?)", tableName)
        .execute(r -> r.getString(1).toUpperCase() + " " + r.getString(2));
  }

  private static void assertRejected(final JdbcSqlQuery insertion) {
    try {
      execute(insertion);
    } catch (Exception e) {
      assertThat(ExceptionUtils.indexOfType(e, SQLException.class), greaterThanOrEqualTo(0));
      return;
    }
    fail("The insertion should be rejected by an integrity constraint");
  }

  private void testTablesExistence(final String name, boolean mustExists) throws SQLException {
    boolean userTableFound = countAll().from("INFORMATION_SCHEMA.TABLES")
        .where("lower(TABLE_NAME) = lower(?)", "domain" + name + "_User").execute() == 1;
    boolean groupTableFound = countAll().from("INFORMATION_SCHEMA.TABLES")
        .where("lower(TABLE_NAME) = lower(?)", "domain" + name + "_Group").execute() == 1;
    boolean groupUserRelTableFound = countAll().from("INFORMATION_SCHEMA.TABLES")
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

  private static void execute(final JdbcSqlQuery modification) {
    Transaction.performInOne(modification::execute);
  }
}

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
package org.silverpeas.core.admin.domain;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.silverpeas.core.admin.domain.driver.sqldriver.SQLSettings;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.DataSetTest;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.admin.domain.exception.DomainAuthenticationPropertiesAlreadyExistsException;
import org.silverpeas.core.admin.domain.exception.DomainPropertiesAlreadyExistsException;
import org.silverpeas.core.admin.domain.exception.NameAlreadyExistsInDatabaseException;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.lang.SystemWrapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.sql.SQLException;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.getFile;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.createCountFor;

/**
 * @author lbertin
 */
@RunWith(Arquillian.class)
public class SQLDomainServiceTest extends DataSetTest {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Inject
  @Named("sqlDomainService")
  private DomainService service;

  private File silverpeasHome;

  private File expectedDomainPropertiesFile;
  private File expectedDomainAuthenticationPropertiesFile;
  File domainPropertyPath = null;
  File autDomainPropertyPath = null;

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

  public static final Operation TABLES_CREATION = Operations
      .sql("CREATE TABLE st_domain (id int PRIMARY KEY NOT NULL , name varchar(100) not NULL, " +
              "description varchar(400), propfilename varchar(100) not NULL, " +
              "classname varchar(100) not NULL, authenticationserver varchar(100) not NULL, " +
              "thetimestamp varchar(100) not NULL, silverpeasserverurl varchar(400) not NULL )",
          "CREATE TABLE ST_Space (id int NOT NULL, domainFatherId int, " +
              "name varchar(100) NOT NULL," +
              " description varchar(400), createdBy int, firstPageType int NOT NULL, " +
              "firstPageExtraParam	varchar(400), orderNum int DEFAULT (0) NOT NULL, " +
              "createTime varchar(20), updateTime varchar(20), removeTime varchar(20), " +
              "spaceStatus char(1), updatedBy int, removedBy int, lang char(2), " +
              "isInheritanceBlocked	int	 default(0) NOT NULL, look varchar(50), " +
              "displaySpaceFirst smallint, isPersonal smallint)",
          "CREATE TABLE ST_SpaceI18N (id int NOT NULL, spaceId int NOT NULL, " +
              "lang char(2) NOT NULL, name varchar(100)	NOT NULL, description varchar(400))",
          "CREATE TABLE IF NOT EXISTS uniqueId ( maxId INT NOT NULL, " +
              "tableName VARCHAR(100) NOT NULL )");
  public static final Operation DROP_ALL = Operations
      .sql("DROP TABLE IF EXISTS st_domain", "DROP TABLE IF EXISTS ST_Space",
          "DROP TABLE IF EXISTS ST_SpaceI18N", "DROP TABLE IF EXISTS uniqueId");
  public static final Operation DEFAULT_DOMAIN_SET_UP = Operations.insertInto("st_domain")
      .columns("id", " name", " description", " propfilename", " classname",
          " authenticationserver", " thetimestamp", " silverpeasserverurl")
      .values(1, "domainSilverpeas", "default domain for Silverpeas",
          "org.silverpeas.domains.domainSP",
          "org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver", "autDomainSP", "0",
          "autDomainSP").values(2, "Customers", "Customers active directory",
          "org.silverpeas.domains.domainSP",
          "org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver", "autDomainCustomers",
          "0", "autDomainCustomers").build();

  @Override
  protected Operation getDbSetupInitializations() {
    return Operations.sequenceOf(DROP_ALL, TABLES_CREATION, DEFAULT_DOMAIN_SET_UP);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SQLDomainServiceTest.class)
        .addCommonBasicUtilities()
        .addSilverpeasExceptionBases()
        .addFileRepositoryFeatures()
        .addAdministrationFeatures()
        .addClasses(FileServerUtils.class)
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.admin.domain");
          warBuilder.addAsResource("org/silverpeas/domains/templateDomainSQL.properties");
        }).build();
  }

  @After
  public void unsetSilverpeasHome() throws Exception {
    deleteQuietly(silverpeasHome);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Before
  public void initTest() throws Exception {
    silverpeasHome = getFile(mavenTargetDirectoryRule.getBuildDirFile(), "SILVERPEAS_HOME");
    SystemWrapper.get().getenv().put("SILVERPEAS_HOME", silverpeasHome.getPath());

    // load expected properties files
    expectedDomainPropertiesFile =
        getFile(mavenTargetDirectoryRule.getBuildDirFile(), "test-classes",
            "/org/silverpeas/core/admin/domain/expectedDomainPropertiesFile.properties");
    expectedDomainAuthenticationPropertiesFile =
        getFile(mavenTargetDirectoryRule.getBuildDirFile(), "test-classes",
            "/org/silverpeas/core/admin/domain/expectedDomainAuthenticationPropertiesFile.properties");

    // initialize the creation tests directory
    domainPropertyPath =
        getFile(FileRepositoryManager.getDomainPropertiesPath("TestCreation")).getParentFile();
    domainPropertyPath.mkdirs();
    autDomainPropertyPath =
        getFile(FileRepositoryManager.getDomainAuthenticationPropertiesPath("TestCreation"))
            .getParentFile();
    autDomainPropertyPath.mkdirs();
  }

  @Test
  public void testGetTechnicalDomainName() {
    SQLDomainService sqlDomainService = (SQLDomainService) service;
    for (int i = 0; i < 1001; i += 100) {
      String domainId = String.valueOf(i);
      Domain domain = createDomain(domainId, "éèëêöôõòïîìñüûùçàäãâ°", null, null, null, null);
      String technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
      asserTechnicalDomainName(technicalDomainName, domainId, "eeeeooooiiinuuucaaaa");
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
   */
  private void asserTechnicalDomainName(final String technicalDomainName, String domainId,
      String normalizedDomainName) {
    String expectedTechnicalDomainName = StringUtil
        .left(domainId + normalizedDomainName, SQLSettings.DATABASE_TABLE_NAME_MAX_LENGTH - 21);
    assertThat(technicalDomainName, is(expectedTechnicalDomainName));
  }

  @SuppressWarnings("ConstantConditions")
  @Test
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
    File[] domainPropertyFiles = domainPropertyPath.listFiles();
    assertThat(domainPropertyFiles, arrayWithSize(1));
    assertThat("domain properties files has not been generated", domainPropertyFiles[0].getName(),
        is("domain3TestCrea.properties"));
    assertThat(FileUtils.contentEquals(domainPropertyFiles[0], expectedDomainPropertiesFile),
        is(true));
    File[] autDomainPropertyFiles = autDomainPropertyPath.listFiles();
    assertThat(autDomainPropertyFiles, arrayWithSize(1));
    assertThat("domain authentication properties files has not been generated",
        autDomainPropertyFiles[0].getName(), is("autDomain3TestCrea.properties"));
    assertThat(FileUtils
        .contentEquals(autDomainPropertyFiles[0], expectedDomainAuthenticationPropertiesFile),
        is(true));

    // Performs checks on generated tables
    testTablesExistence("3TestCrea", true);
  }

  @Test
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

  @Test(expected = DomainPropertiesAlreadyExistsException.class)
  public void testCreateDomainWithPropertiesNameConflictsOnDomainProperties() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestCreation");

    File conflictingPropertiesFile = new File(domainPropertyPath, "domain3TestCrea.properties");
    FileUtils.touch(conflictingPropertiesFile);
    service.createDomain(domain);
    fail("Exception must have been thrown");
  }

  @Test(expected = DomainAuthenticationPropertiesAlreadyExistsException.class)
  public void testCreateDomainWithPropertiesNameConflictsOnAutDomainProperties() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestCreation");

    File conflictingPropertiesFile =
        new File(autDomainPropertyPath, "autDomain3TestCrea.properties");
    FileUtils.touch(conflictingPropertiesFile);
    // create domain
    service.createDomain(domain);
    fail("Exception must have been thrown");
  }

  private void testTablesExistence(String name, boolean mustExists) throws SQLException {
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
}

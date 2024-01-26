/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.admin.domain;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.domain.exception.*;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.integration.DataSetTest;
import org.silverpeas.core.test.integration.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.apache.commons.io.FileUtils.getFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.countAll;

/**
 * @author lbertin
 */
@RunWith(Arquillian.class)
public class SQLDomainServiceIT extends DataSetTest {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Inject
  @Named("sqlDomainService")
  private DomainService service;

  private File expectedDomainPropertiesFile;
  private File expectedDomainAuthenticationPropertiesFile;

  private Domain createDomain(String id, String name) {
    Domain newDomain = new Domain();
    newDomain.setId(id);
    newDomain.setName(name);
    newDomain.setDescription(null);
    newDomain.setAuthenticationServer(null);
    newDomain.setDriverClassName(null);
    newDomain.setPropFileName(null);
    newDomain.setSilverpeasServerURL("http://localhost:8000/silverpeas");
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

  @SuppressWarnings("unchecked")
  @Override
  protected Operation getDbSetupInitializations() {
    return Operations.sequenceOf(DROP_ALL, TABLES_CREATION, DEFAULT_DOMAIN_SET_UP);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SQLDomainServiceIT.class)
        .addCommonBasicUtilities()
        .addSilverpeasExceptionBases()
        .addFileRepositoryFeatures()
        .addAdministrationFeatures()
        .addPublicationTemplateFeatures()
        .addClasses(FileServerUtils.class)
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.admin.domain");
          warBuilder.addAsResource("org/silverpeas/domains/templateDomainSQL.properties");
        }).build();
  }

  @Before
  public void initTest() {
    // load expected properties files
    expectedDomainPropertiesFile =
        getFile(mavenTargetDirectoryRule.getBuildDirFile(), "test-classes",
            "/org/silverpeas/core/admin/domain/expectedDomainPropertiesFile.properties");
    expectedDomainAuthenticationPropertiesFile =
        getFile(mavenTargetDirectoryRule.getBuildDirFile(), "test-classes",
            "/org/silverpeas/core/admin/domain/expectedDomainAuthenticationPropertiesFile" +
                ".properties");
  }

  @Test
  public void checkTechnicalDomainName() {
    SQLDomainService sqlDomainService = (SQLDomainService) service;
    for (int i = 0; i < 1001; i += 100) {
      String domainId = String.valueOf(i);
      Domain domain = createDomain(domainId, "éèëêöôõòïîìñüûùçàäãâ°");
      String technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
      assertTechnicalDomainName(technicalDomainName, domainId);
    }
    Domain domain = createDomain("0", "ïîìñüûùçàäãâ°éèëêöôõò");
    String technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
    assertThat(technicalDomainName, is("0iiinuuuc"));

    domain = createDomain("0", "àäãâ°éèëêöôõòïîìñüûùç");
    technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
    assertThat(technicalDomainName, is("0aaaaeeee"));

    domain =
        createDomain("0", " &~#\"'{([-|`_\\^@°)]}+=¨£$¤%*µ<>?,.;/:§!€");
    technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
    assertThat(technicalDomainName, is("0"));

    domain = createDomain("0", "x²");
    technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
    assertThat(technicalDomainName, is("0x2"));

    domain = createDomain("0", "X²AbCd");
    technicalDomainName = sqlDomainService.getTechnicalDomainName(domain);
    assertThat(technicalDomainName, is("0X2AbCd"));
  }

  /**
   * Common assertion method.
   */
  private void assertTechnicalDomainName(final String technicalDomainName, String domainId) {
    String expectedTechnicalDomainName = left(domainId + "eeeeooooiiinuuucaaaa"
    );
    assertThat(technicalDomainName, is(expectedTechnicalDomainName));
  }

  private static String left(final String str) {
    if (str == null) {
      return null;
    }
    if (str.length() <= 9) {
      return str;
    }
    return str.substring(0, 9);
  }

  @Test
  public void createDomain() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestCreation");

    // create domain
    String domainId = service.createDomain(domain);

    // Performs checks on id returned
    assertThat("domainId returned is NULL", domainId, is(notNullValue()));
    assertThat("domainId returned = -1", domainId, is(not("-1")));
    assertThat("domainId returned is empty", domainId, is(not("")));

    // Performs checks on generated properties files
    final String domainNameKey = "3TestCrea";
    String domainDescriptor = FileRepositoryManager.getDomainPropertiesPath(domainNameKey);
    Path domainPath = Path.of(domainDescriptor);
    assertThat(Files.exists(domainPath), is(true));
    assertThat(FileUtils.contentEquals(domainPath.toFile(), expectedDomainPropertiesFile),
        is(true));

    String authDomainDescriptor =
        FileRepositoryManager.getDomainAuthenticationPropertiesPath(domainNameKey);
    Path authDomainPath = Path.of(authDomainDescriptor);
    assertThat(Files.exists(authDomainPath), is(true));
    assertThat(FileUtils
            .contentEquals(authDomainPath.toFile(), expectedDomainAuthenticationPropertiesFile),
        is(true));

    // Performs checks on generated tables
    assertTables3TestCreaExists();
  }

  @Test(expected = NameAlreadyExistsInDatabaseException.class)
  public void createDomainAlreadyInDB() throws DomainCreationException,
      DomainConflictException {
    Domain domain = new Domain();
    domain.setName("Customers");
    service.createDomain(domain);
    fail("Exception must have been thrown");
  }

  @Test(expected = DomainPropertiesAlreadyExistsException.class)
  public void createDomainWithPropertiesNameConflictsOnDomainProperties() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestFail");

    Path domainPath = Path.of(FileRepositoryManager.getDomainPropertiesPath("3TestFail"));
    File conflictingPropertiesFile = domainPath.toFile();
    FileUtils.touch(conflictingPropertiesFile);
    service.createDomain(domain);
    fail("Exception must have been thrown");
  }

  @Test(expected = DomainAuthenticationPropertiesAlreadyExistsException.class)
  public void createDomainWithPropertiesNameConflictsOnAutDomainProperties() throws Exception {
    Domain domain = new Domain();
    domain.setName("TestFail");

    Path domainPath = Path.of(FileRepositoryManager.getDomainAuthenticationPropertiesPath(
        "3TestFail"));
    File conflictingPropertiesFile = domainPath.toFile();
    FileUtils.touch(conflictingPropertiesFile);
    // create domain
    service.createDomain(domain);
    fail("Exception must have been thrown");
  }

  private void assertTables3TestCreaExists() throws SQLException {
    boolean userTableFound = countAll().from("INFORMATION_SCHEMA.TABLES")
        .where("lower(TABLE_NAME) = lower(?)", "domain" + "3TestCrea" + "_User").execute() == 1;
    boolean groupTableFound = countAll().from("INFORMATION_SCHEMA.TABLES")
        .where("lower(TABLE_NAME) = lower(?)", "domain" + "3TestCrea" + "_Group").execute() == 1;
    boolean groupUserRelTableFound = countAll().from("INFORMATION_SCHEMA.TABLES")
        .where("lower(TABLE_NAME) = lower(?)", "domain" + "3TestCrea" + "_Group_User_Rel").execute() == 1;

    // Performs checks
    assertThat("User table has not been created", userTableFound, is(true));
    assertThat("Group table has not been created", groupTableFound, is(true));
    assertThat("Group_User_Rel table has not been created", groupUserRelTableFound, is(true));
  }
}

/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.organization;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.persistence.PersistenceException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.WarBuilder4LibCore;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class UserFavoriteSpaceDAOTest {

  @Resource(lookup = "java:/datasources/silverpeas")
  private DataSource dataSource;
  private DbSetupTracker dbSetupTracker = new DbSetupTracker();

  public static final Operation TABLE_ST_USER_CREATION =
      Operations.sql("CREATE TABLE IF NOT EXISTS ST_User" +
          "(\n" +
          "  id                            INT PRIMARY KEY      NOT NULL,\n" +
          "  domainId                      INT                  NOT NULL,\n" +
          "  specificId                    VARCHAR(500)         NOT NULL,\n" +
          "  firstName                     VARCHAR(100),\n" +
          "  lastName                      VARCHAR(100)         NOT NULL,\n" +
          "  email                         VARCHAR(100),\n" +
          "  login                         VARCHAR(50)          NOT NULL,\n" +
          "  loginMail                     VARCHAR(100),\n" +
          "  accessLevel                   CHAR(1) DEFAULT 'U'  NOT NULL,\n" +
          "  loginquestion                 VARCHAR(200),\n" +
          "  loginanswer                   VARCHAR(200),\n" +
          "  creationDate                  TIMESTAMP,\n" +
          "  saveDate                      TIMESTAMP,\n" +
          "  version                       INT DEFAULT 0        NOT NULL,\n" +
          "  tosAcceptanceDate             TIMESTAMP,\n" +
          "  lastLoginDate                 TIMESTAMP,\n" +
          "  nbSuccessfulLoginAttempts     INT DEFAULT 0        NOT NULL,\n" +
          "  lastLoginCredentialUpdateDate TIMESTAMP,\n" +
          "  expirationDate                TIMESTAMP,\n" +
          "  state                         VARCHAR(30)          NOT NULL,\n" +
          "  stateSaveDate                 TIMESTAMP            NOT NULL\n" +
          ")");

  public static final Operation TABLE_ST_SPACE_CREATION =
      Operations.sql("CREATE TABLE IF NOT EXISTS ST_Space" +
          "(\n" +
          "    id int PRIMARY KEY NOT NULL,\n" +
          "    domainFatherId\t\tint,\n" +
          "    name\t\t\t\tvarchar(100)  NOT NULL,\n" +
          "    description\t\t\tvarchar(400),\n" +
          "    createdBy\t\t\tint,\n" +
          "    firstPageType\t\tint           NOT NULL,\n" +
          "    firstPageExtraParam\tvarchar(400),\n" +
          "    orderNum \t\t\tint DEFAULT (0) NOT NULL,\n" +
          "    createTime \t\t\tvarchar(20),\n" +
          "    updateTime \t\t\tvarchar(20),\n" +
          "    removeTime \t\t\tvarchar(20),\n" +
          "    spaceStatus \t\tchar(1),\n" +
          "    updatedBy \t\t\tint,\n" +
          "    removedBy \t\t\tint,\n" +
          "    lang\t\t\tchar(2),\n" +
          "    isInheritanceBlocked\tint\t      default(0) NOT NULL,\n" +
          "    look\t\t\tvarchar(50),\n" +
          "    displaySpaceFirst\t\tsmallint,\n" +
          "    isPersonal\t\t\tsmallint\n" +
          ")");

  public static final Operation TABLE_ST_USERFS_CREATION =
      Operations.sql("CREATE TABLE IF NOT EXISTS ST_UserFavoriteSpaces" +
          "(\n" +
          "  id          INT PRIMARY KEY NOT NULL,\n" +
          "  userid      INT   NOT NULL,\n" +
          "  spaceid     INT   NOT NULL\n" +
          ")");

  public static final Operation ADD_CONSTRAINTS_ST_USERFS = Operations.sequenceOf(Operations.sql(
      "ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT FK_UserFavoriteSpaces_1 FOREIGN KEY " +
          "(userid) REFERENCES ST_User(id)"), Operations.sql(
      "ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT FK_UserFavoriteSpaces_2 " +
          "FOREIGN KEY (spaceid) REFERENCES ST_Space(id)"));

  public static final Operation DROP_CONSTRAINTS_ST_USERFS = Operations.sequenceOf(Operations.sql(
          "ALTER TABLE ST_UserFavoriteSpaces DROP CONSTRAINT IF EXISTS FK_UserFavoriteSpaces_1"),
      Operations.sql(
          "ALTER TABLE ST_UserFavoriteSpaces DROP CONSTRAINT IF EXISTS FK_UserFavoriteSpaces_2"));


  public static final Operation TABLE_UNIQUE_CREATION =
      Operations.sql("CREATE TABLE IF NOT EXISTS UniqueId (\n" +
          " maxId int NOT NULL ," +
          " tableName varchar(100) NOT NULL" +
          ")");

  public static final Operation ST_USER_SET_UP = Operations.insertInto("st_user")
      .columns("id", "domainid", "specificid", "lastname", "login", "accesslevel", "state",
          "stateSaveDate")
      .values(0, 0, "0", "Administrateur", "SilverAdmin", "A", "VALID", "2012-01-01 00:00:00")
      .build();

  public static final Operation ST_SPACE_SET_UP = Operations.insertInto("st_space")
      .columns("id", "name", "description", "createdby", "firstpagetype", "firstpageextraparam",
          "ordernum", "createtime", "updatetime", "spacestatus", "domainfatherid", "updatedby",
          "lang", "isinheritanceblocked", "look")
      .values(1, "Espace de Tests", "", 0, 1, "indicateurs16", 1, "1194276699849", "1205750893265",
          null, null, 0, "fr", 0, "")
      .values(2, "Espace de Tests", "", 0, 1, "indicateurs32", 2, "1194276000000", "1205750000000",
          null, null, 0, "fr", 0, "").build();

  public static final Operation USER_FS_SET_UP =
      Operations.insertInto("ST_UserFavoriteSpaces").columns("id", "userid", "spaceid")
          .values(0, 0, 1).build();

  public static final Operation CLEAN_UP =
      Operations.deleteAllFrom("ST_UserFavoriteSpaces", "ST_Space", "ST_User", "UniqueId");

  @Before
  public void prepareDataSource() {
    Operation preparation = Operations
        .sequenceOf(TABLE_UNIQUE_CREATION, TABLE_ST_USER_CREATION, TABLE_ST_SPACE_CREATION,
            TABLE_ST_USERFS_CREATION, DROP_CONSTRAINTS_ST_USERFS, ADD_CONSTRAINTS_ST_USERFS,
            CLEAN_UP, ST_USER_SET_UP, ST_SPACE_SET_UP, USER_FS_SET_UP);
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWar().addJdbcPersistenceFeatures().addSilverpeasExceptionBases()
        .testFocusedOn((warBuilder) -> {
          warBuilder.addClasses(AdminException.class, PersistenceException.class);
          warBuilder.addPackages(true, "com.stratelia.webactiv.persistence");
          warBuilder.addPackages(true, "com.stratelia.webactiv.organization");
        }).build();
  }


  @Test
  public void testGetListUserFavoriteSpace() {
    UserFavoriteSpaceDAOImpl ufsDAO = new UserFavoriteSpaceDAOImpl();
    List<UserFavoriteSpaceVO> listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertThat(listUFS.size(), is(1));
  }

  @Test
  public void testAddUserFavoriteSpace() {
    UserFavoriteSpaceDAOImpl ufsDAO = new UserFavoriteSpaceDAOImpl();
    UserFavoriteSpaceVO ufsVO = new UserFavoriteSpaceVO(0, 2);
    boolean result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertThat(result, is(true));

    // Check the new records inside database
    List<UserFavoriteSpaceVO> listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertThat(listUFS.size(), is(2));

    // Check database constraint on existing userid and space id
    ufsVO = new UserFavoriteSpaceVO(10, 10);
    result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertThat(result, is(false));

    // Check default userFavoriteSpaceVO
    ufsVO = new UserFavoriteSpaceVO();
    assertThat(ufsVO.getSpaceId(), is(-1));
    assertThat(ufsVO.getUserId(), is(-1));
    result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertThat(result, is(false));
  }

  @Test
  public void testRemoveUserFavoriteSpace() {
    UserFavoriteSpaceDAOImpl ufsDAO = new UserFavoriteSpaceDAOImpl();
    UserFavoriteSpaceVO ufsVO = new UserFavoriteSpaceVO(0, 2);
    boolean result = ufsDAO.removeUserFavoriteSpace(ufsVO);
    assertThat(result, is(true));

    // Check result
    List<UserFavoriteSpaceVO> listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertThat(listUFS.size(), is(1));

    result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertThat(result, is(true));

    // Delete all favorite space of current user
    ufsDAO.removeUserFavoriteSpace(new UserFavoriteSpaceVO(0, -1));
    listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertThat(listUFS.size(), is(0));
  }

}

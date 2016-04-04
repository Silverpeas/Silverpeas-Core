/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.space.UserFavoriteSpaceServiceImpl;
import org.silverpeas.core.admin.space.model.UserFavoriteSpaceVO;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class UserFavoriteSpaceServiceTest {

  public static final Operation ST_ACCESSLEVEL_SET_UP = Operations.insertInto("st_accesslevel")
      .columns("id", "name")
      .values("U", "User")
      .values("A", "Administrator")
      .values("G", "Guest")
      .values("R", "Removed")
      .values("K", "KMManager")
      .values("D", "DomainManager")
      .build();

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

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("/org/silverpeas/core/admin/create_table_favorit_space.sql")
          .loadInitialDataSetFrom(ST_ACCESSLEVEL_SET_UP, ST_USER_SET_UP, ST_SPACE_SET_UP,
              USER_FS_SET_UP);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(UserFavoriteSpaceServiceTest.class)
        .addDatabaseToolFeatures()
        .addSilverpeasExceptionBases()
        .addOrganisationFeatures()
        .testFocusedOn((warBuilder) -> {
          warBuilder.addClasses(AdminException.class, PersistenceException.class);
        }).build();
  }

  @Test
  public void testGetListUserFavoriteSpace() {
    UserFavoriteSpaceServiceImpl ufsDAO = new UserFavoriteSpaceServiceImpl();
    List<UserFavoriteSpaceVO> listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertThat(listUFS.size(), is(1));
  }

  @Test
  public void testAddUserFavoriteSpace() {
    UserFavoriteSpaceServiceImpl ufsDAO = new UserFavoriteSpaceServiceImpl();
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
    UserFavoriteSpaceServiceImpl ufsDAO = new UserFavoriteSpaceServiceImpl();
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

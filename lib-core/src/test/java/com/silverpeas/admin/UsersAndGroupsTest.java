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
package com.silverpeas.admin;

import org.apache.commons.lang.time.DateUtils;
import org.silverpeas.admin.user.constant.UserAccessLevel;
import org.silverpeas.admin.user.constant.UserState;

import java.util.ArrayList;
import java.util.Date;

import java.io.InputStream;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.silverpeas.jndi.SimpleMemoryContextFactory;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.GroupProfileInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class UsersAndGroupsTest {

  private static DataSource dataSource;
  private static ClassPathXmlApplicationContext context;

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    context = new ClassPathXmlApplicationContext(new String[]{
      "spring-domains-embbed-datasource.xml", "spring-domains.xml"});
    dataSource = context.getBean("jpaDataSource", DataSource.class);
    InitialContext ic = new InitialContext();
    ic.rebind("jdbc/Silverpeas", dataSource);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    DBUtil.clearTestInstance();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
    context.close();
  }

  @Before
  public void init() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    connection.close();
  }

  @After
  public void after() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    connection.close();
  }

  private IDatabaseConnection getConnection() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection;
  }

  protected IDataSet getDataSet() throws Exception {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream(
        "com/silverpeas/admin/test-usersandgroups-dataset.xml");
    try {
      FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
      ReplacementDataSet dataSet = new ReplacementDataSet(builder.build(in));
      dataSet.addReplacementObject("[NULL]", null);
      return dataSet;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Test
  public void shouldAddNewUser() {
    Date now = new Date();
    Date tosAcceptanceDate = DateUtils.addDays(now, 1);
    Date lastLoginDate = DateUtils.addDays(now, 2);
    Date lastLoginCredentialUpdateDate = DateUtils.addDays(now, 3);
    Date expirationDate = DateUtils.addDays(now, 4);
    Date stateSaveDate = DateUtils.addDays(now, 5);

    UserDetail user = new UserDetail();
    user.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    user.setDomainId("0");
    user.seteMail("nicolas.eysseric@silverpeas.com");
    user.setFirstName("Nicolas");
    user.setLastName("EYSSERIC");
    user.setLogin("neysseri");
    user.setTosAcceptanceDate(tosAcceptanceDate);
    user.setLastLoginDate(lastLoginDate);
    user.setNbSuccessfulLoginAttempts(7);
    user.setLastLoginCredentialUpdateDate(lastLoginCredentialUpdateDate);
    user.setExpirationDate(expirationDate);
    user.setState(UserState.EXPIRED);
    user.setStateSaveDate(stateSaveDate);
    
    String newUserId = "5";

    AdminController ac = getAdminController();
    String userId = ac.addUser(user);
    assertThat(userId, is(newUserId));

    user = ac.getUserDetail(newUserId);
    assertThat(user.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(user.getSaveDate(), greaterThan(now));
    assertThat(user.getVersion(), is(0));
    assertThat(user.getTosAcceptanceDate().getTime(), is(tosAcceptanceDate.getTime()));
    assertThat(user.getLastLoginDate().getTime(), is(lastLoginDate.getTime()));
    assertThat(user.getNbSuccessfulLoginAttempts(), is(7));
    assertThat(user.getLastLoginCredentialUpdateDate().getTime(),
        is(lastLoginCredentialUpdateDate.getTime()));
    assertThat(user.getExpirationDate().getTime(), is(expirationDate.getTime()));
    assertThat(user.getState(), is(UserState.EXPIRED));
    assertThat(user.getStateSaveDate(), greaterThan(now));
  }

  @Test
  public void shouldUpdateUser() {
    Date now = new Date();
    Date tosAcceptanceDate = DateUtils.addDays(now, 1);
    Date lastLoginDate = DateUtils.addDays(now, 2);
    Date lastLoginCredentialUpdateDate = DateUtils.addDays(now, 3);
    Date expirationDate = DateUtils.addDays(now, 4);
    Date stateSaveDate = DateUtils.addDays(now, 5);
    AdminController ac = getAdminController();
    
    String updatedUserId = "1";
    UserDetail user = ac.getUserDetail(updatedUserId);

    assertThat(user.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(user.isAccessAdmin(), is(true));
    assertThat(user.isAccessDomainManager(), is(false));
    assertThat(user.isAccessSpaceManager(), is(false));
    assertThat(user.isAccessPdcManager(), is(false));
    assertThat(user.isAccessUser(), is(false));
    assertThat(user.isAccessGuest(), is(false));
    assertThat(user.getSaveDate(), nullValue());
    assertThat(user.getVersion(), is(0));
    assertThat(user.getTosAcceptanceDate(), nullValue());
    assertThat(user.getLastLoginDate(), nullValue());
    assertThat(user.getNbSuccessfulLoginAttempts(), is(0));
    assertThat(user.getLastLoginCredentialUpdateDate(), nullValue());
    assertThat(user.getExpirationDate(), nullValue());
    assertThat(user.getState(), is(UserState.VALID));
    assertThat(user.isExpiredState(), is(false));
    assertThat(user.getStateSaveDate(), lessThan(now));

    String newEmail = "ney@silverpeas.com";
    user.seteMail(newEmail);
    user.setAccessLevel(UserAccessLevel.USER);
    user.setTosAcceptanceDate(tosAcceptanceDate);
    user.setLastLoginDate(lastLoginDate);
    user.setNbSuccessfulLoginAttempts(7);
    user.setLastLoginCredentialUpdateDate(lastLoginCredentialUpdateDate);
    user.setExpirationDate(expirationDate);
    ac.updateUser(user);

    user = ac.getUserDetail(updatedUserId);
    assertThat(user.geteMail(), is(newEmail));
    assertThat(user.getAccessLevel(), is(UserAccessLevel.USER));
    assertThat(user.isAccessAdmin(), is(false));
    assertThat(user.isAccessDomainManager(), is(false));
    assertThat(user.isAccessSpaceManager(), is(false));
    assertThat(user.isAccessPdcManager(), is(false));
    assertThat(user.isAccessUser(), is(true));
    assertThat(user.isAccessGuest(), is(false));
    assertThat(user.getSaveDate(), greaterThan(now));
    assertThat(user.getVersion(), is(1));
    assertThat(user.getTosAcceptanceDate().getTime(), is(tosAcceptanceDate.getTime()));
    assertThat(user.getLastLoginDate().getTime(), is(lastLoginDate.getTime()));
    assertThat(user.getNbSuccessfulLoginAttempts(), is(7));
    assertThat(user.getLastLoginCredentialUpdateDate().getTime(),
        is(lastLoginCredentialUpdateDate.getTime()));
    assertThat(user.getExpirationDate().getTime(), is(expirationDate.getTime()));
    assertThat(user.getState(), is(UserState.VALID));
    assertThat(user.isExpiredState(), is(false));
    assertThat(user.isDeletedState(), is(false));
    assertThat(user.getStateSaveDate(), lessThan(now));

    expirationDate = DateUtils.addDays(now, -4);
    user.setExpirationDate(expirationDate);
    ac.updateUser(user);

    user = ac.getUserDetail(updatedUserId);
    assertThat(user.getVersion(), is(2));
    assertThat(user.isExpiredState(), is(true));


    user.setAccessLevel(UserAccessLevel.GUEST);
    user.setExpirationDate(null);
    user.setState(UserState.EXPIRED);
    user.setStateSaveDate(stateSaveDate);
    ac.updateUser(user);

    user = ac.getUserDetail(updatedUserId);
    assertThat(user.getAccessLevel(), is(UserAccessLevel.GUEST));
    assertThat(user.isAccessAdmin(), is(false));
    assertThat(user.isAccessDomainManager(), is(false));
    assertThat(user.isAccessSpaceManager(), is(false));
    assertThat(user.isAccessPdcManager(), is(false));
    assertThat(user.isAccessUser(), is(false));
    assertThat(user.isAccessGuest(), is(true));
    assertThat(user.getSaveDate(), greaterThan(now));
    assertThat(user.getVersion(), is(3));
    assertThat(user.getExpirationDate(), nullValue());
    assertThat(user.getState(), is(UserState.EXPIRED));
    assertThat(user.isExpiredState(), is(true));
    assertThat(user.isDeletedState(), is(false));
    assertThat(user.getStateSaveDate().getTime(), is(stateSaveDate.getTime()));

    user.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    ac.updateUser(user);

    user = ac.getUserDetail(updatedUserId);
    assertThat(user.getVersion(), is(4));
    assertThat(user.getAccessLevel(), is(UserAccessLevel.DOMAIN_ADMINISTRATOR));
    assertThat(user.isAccessAdmin(), is(false));
    assertThat(user.isAccessDomainManager(), is(true));
    assertThat(user.isAccessSpaceManager(), is(false));
    assertThat(user.isAccessPdcManager(), is(false));
    assertThat(user.isAccessUser(), is(false));
    assertThat(user.isAccessGuest(), is(false));

    user.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    ac.updateUser(user);

    user = ac.getUserDetail(updatedUserId);
    assertThat(user.getVersion(), is(5));
    assertThat(user.getAccessLevel(), is(UserAccessLevel.PDC_MANAGER));
    assertThat(user.isAccessAdmin(), is(false));
    assertThat(user.isAccessDomainManager(), is(false));
    assertThat(user.isAccessSpaceManager(), is(false));
    assertThat(user.isAccessPdcManager(), is(true));
    assertThat(user.isAccessUser(), is(false));
    assertThat(user.isAccessGuest(), is(false));
  }

  @Test
  public void shouldDeleteUser() {
    AdminController ac = getAdminController();
    String userIdToDelete = "1";
    String userId = ac.deleteUser(userIdToDelete);
    assertThat(userId, is(userIdToDelete));
    UserDetail user = ac.getUserDetail(userId);
    assertThat(user.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(user.getState(), is(UserState.DELETED));
    assertThat(user.isDeletedState(), is(true));
  }
  
  @Test
  public void testGetUsers() {
    OrganizationController oc = new OrganizationController();
    @SuppressWarnings("unchecked")
    List<UserDetail> users = Arrays.asList(oc.getAllUsers());
    assertThat(users.size(), is(3));
    assertThat(users.get(0).getId(), is("1"));
    assertThat(users.get(1).getId(), is("3"));
    assertThat(users.get(2).getId(), is("2"));
    
    users = oc.getAllUsersFromNewestToOldest();
    assertThat(users.size(), is(3));
    assertThat(users.get(0).getId(), is("3"));
    assertThat(users.get(1).getId(), is("2"));
    assertThat(users.get(2).getId(), is("1"));
    
    List<String> domainIds = new ArrayList<String>();
    domainIds.add("0");
    users = oc.getUsersOfDomains(domainIds);
    assertThat(users.size(), is(3));
    assertThat(users.get(0).getId(), is("1"));
    assertThat(users.get(1).getId(), is("3"));
    assertThat(users.get(2).getId(), is("2"));
    
    users = oc.getUsersOfDomainsFromNewestToOldest(domainIds);
    assertThat(users.get(0).getId(), is("3"));
    assertThat(users.get(1).getId(), is("2"));
    assertThat(users.get(2).getId(), is("1"));
  }

  @Test
  public void shouldAddGroup() {
    AdminController ac = getAdminController();
    Group group = new Group();
    group.setDomainId("0");
    group.setName("Groupe 2");
    String groupId = ac.addGroup(group);
    assertThat(groupId, is("2"));
  }

  @Test
  public void testUpdateGroup() {
    AdminController ac = getAdminController();
    String desc = "New description";
    Group group = ac.getGroupById("1");
    group.setDescription(desc);
    ac.updateGroup(group);
    group = ac.getGroupById("1");
    assertThat(group.getDescription(), is(desc));
  }

  @Test
  public void shouldDeleteGroup() {
    AdminController ac = getAdminController();
    Group group = ac.getGroupById("1");
    assertThat(group.getId(), is("1"));
    ac.deleteGroupById("1");
    group = ac.getGroupById("1");
    assertThat(group.getId(), is(nullValue()));
  }

  @Test
  public void shouldFindUsersInGroup() {
    AdminController ac = getAdminController();
    Group subGroup = new Group();
    subGroup.setDomainId("0");
    subGroup.setName("Groupe 1-1");
    subGroup.setSuperGroupId("1");
    String groupId = ac.addGroup(subGroup);
    assertThat(groupId, is("2"));

    String[] subGroupIds = ac.getAllSubGroupIds("1");
    assertThat(subGroupIds.length, is(1));

    String[] userIds = new String[1];
    userIds[0] = "1";
    subGroup = ac.getGroupById(groupId);
    subGroup.setUserIds(userIds);
    ac.updateGroup(subGroup);

    // test if users of subgroups are indirectly attach to root group
    UserDetail[] users = ac.getAllUsersOfGroup("1");
    assertThat(users.length, is(1));
    subGroup.setUserIds(new String[0]);
    ac.updateGroup(subGroup);
    users = ac.getAllUsersOfGroup("1");
    assertThat(users.length, is(0));
  }

  @Test
  public void testGroupManager() throws AdminException {
    AdminController ac = getAdminController();
    GroupProfileInst profile = ac.getGroupProfile("1");
    profile.addUser("1");
    ac.updateGroupProfile(profile);
    List<String> managerIds = AdminReference.getAdminService().getUserManageableGroupIds("1");
    assertThat(managerIds, hasSize(1));
  }

  private AdminController getAdminController() {
    AdminController ac = new AdminController(null);
    ac.reloadAdminCache();
    return ac;
  }
}
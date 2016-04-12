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
package org.silverpeas.core.admin;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.lang3.time.DateUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.space.SpaceServiceProvider;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.index.search.model.SearchEngineException;
import org.silverpeas.core.index.search.model.IndexSearcher;
import org.silverpeas.core.index.search.SearchEnginePropertiesManager;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.exception.TokenRuntimeException;
import org.silverpeas.core.persistence.jdbc.AbstractTable;
import org.silverpeas.core.admin.component.ComponentHelper;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.memory.MemoryData;
import org.silverpeas.core.util.memory.MemoryUnit;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class UsersAndGroupsTest {

  @Inject
  private Administration admin;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("/org/silverpeas/core/admin/domain/driver/create_table.sql")
          .loadInitialDataSetFrom("test-usersandgroups-dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SpacesAndComponentsTest.class)
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addSynchAndAsynchResourceEventFeatures()
        .addIndexEngineFeatures()
        .addSilverpeasUrlFeatures()
        .addAsResource("org/silverpeas/jobStartPagePeas/settings")
        .addAsResource("org/silverpeas/core/admin/domain/driver")
        .addAsResource("org/silverpeas/core/index/search")
        .addPackages(true, "org.silverpeas.core.index.search.model")
        .addPackages(false, "org.silverpeas.core.admin.space.quota")
        .addPackages(false, "org.silverpeas.core.contribution.contentcontainer.container")
        .addPackages(false, "org.silverpeas.core.contribution.contentcontainer.content")
        .addPackages(true, "org.silverpeas.core.notification.user")
        .addClasses(FileRepositoryManager.class, FileFolderManager.class, MemoryUnit.class,
            MemoryData.class, SpaceServiceProvider.class, ComponentHelper.class,
            ParseException.class,
            SearchEngineException.class, IndexSearcher.class, TokenException.class,
            SearchEnginePropertiesManager.class, TokenRuntimeException.class, AbstractTable.class)
        .build();
  }

  @Before
  public void reloadCache() {
    admin.reloadCache();
  }

  @Test
  public void shouldAddNewUser() throws Exception {
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
    String userId = admin.addUser(user);
    assertThat(userId, is(newUserId));

    user = admin.getUserDetail(newUserId);
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
  public void shouldUpdateUser() throws Exception {
    Date now = new Date();
    Date tosAcceptanceDate = DateUtils.addDays(now, 1);
    Date lastLoginDate = DateUtils.addDays(now, 2);
    Date lastLoginCredentialUpdateDate = DateUtils.addDays(now, 3);
    Date expirationDate = DateUtils.addDays(now, 4);
    Date stateSaveDate = DateUtils.addDays(now, 5);

    String updatedUserId = "1";
    UserDetail user = admin.getUserDetail(updatedUserId);

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
    admin.updateUser(user);

    user = admin.getUserDetail(updatedUserId);
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
    admin.updateUser(user);

    user = admin.getUserDetail(updatedUserId);
    assertThat(user.getVersion(), is(2));
    assertThat(user.isExpiredState(), is(true));


    user.setAccessLevel(UserAccessLevel.GUEST);
    user.setExpirationDate(null);
    user.setState(UserState.EXPIRED);
    user.setStateSaveDate(stateSaveDate);
    admin.updateUser(user);

    user = admin.getUserDetail(updatedUserId);
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
    admin.updateUser(user);

    user = admin.getUserDetail(updatedUserId);
    assertThat(user.getVersion(), is(4));
    assertThat(user.getAccessLevel(), is(UserAccessLevel.DOMAIN_ADMINISTRATOR));
    assertThat(user.isAccessAdmin(), is(false));
    assertThat(user.isAccessDomainManager(), is(true));
    assertThat(user.isAccessSpaceManager(), is(false));
    assertThat(user.isAccessPdcManager(), is(false));
    assertThat(user.isAccessUser(), is(false));
    assertThat(user.isAccessGuest(), is(false));

    user.setAccessLevel(UserAccessLevel.PDC_MANAGER);
    admin.updateUser(user);

    user = admin.getUserDetail(updatedUserId);
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
  public void shouldDeleteUser() throws Exception {
    String userIdToDelete = "1";
    String userId = admin.deleteUser(userIdToDelete);
    assertThat(userId, is(userIdToDelete));
    UserDetail user = admin.getUserDetail(userId);
    assertThat(user.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(user.getState(), is(UserState.DELETED));
    assertThat(user.isDeletedState(), is(true));
  }


  @Test
  public void testGetUsers() throws Exception {
    List<UserDetail> users = admin.getAllUsers();
    assertThat(users.size(), is(3));
    assertThat(users.get(0).getId(), is("1"));
    assertThat(users.get(1).getId(), is("3"));
    assertThat(users.get(2).getId(), is("2"));

    users = admin.getAllUsersFromNewestToOldest();
    assertThat(users.size(), is(3));
    assertThat(users.get(0).getId(), is("3"));
    assertThat(users.get(1).getId(), is("2"));
    assertThat(users.get(2).getId(), is("1"));

    List<String> domainIds = new ArrayList<>();
    domainIds.add("0");
    users = admin.getUsersOfDomains(domainIds);
    assertThat(users.size(), is(3));
    assertThat(users.get(0).getId(), is("1"));
    assertThat(users.get(1).getId(), is("3"));
    assertThat(users.get(2).getId(), is("2"));

    users = admin.getUsersOfDomainsFromNewestToOldest(domainIds);
    assertThat(users.get(0).getId(), is("3"));
    assertThat(users.get(1).getId(), is("2"));
    assertThat(users.get(2).getId(), is("1"));
  }


  @Test
  public void shouldAddGroup() throws Exception {
    Group group = new Group();
    group.setDomainId("0");
    group.setName("Groupe 2");
    String groupId = admin.addGroup(group);
    assertThat(groupId, is("2"));
  }


  @Test
  public void testUpdateGroup() throws Exception {
    String desc = "New description";
    Group group = admin.getGroup("1");
    group.setDescription(desc);
    admin.updateGroup(group);
    group = admin.getGroup("1");
    assertThat(group.getDescription(), is(desc));
  }


  @Test
  public void shouldDeleteGroup() throws Exception {
    Group group = admin.getGroup("1");
    assertThat(group.getId(), is("1"));
    admin.deleteGroupById("1");
    group = admin.getGroup("1");
    assertThat(group.getId(), is(nullValue()));
  }


  @Test
  public void shouldFindUsersInGroup() throws Exception {
    Group subGroup = new Group();
    subGroup.setDomainId("0");
    subGroup.setName("Groupe 1-1");
    subGroup.setSuperGroupId("1");
    String groupId = admin.addGroup(subGroup);
    assertThat(groupId, is("2"));

    String[] subGroupIds = admin.getAllSubGroupIds("1");
    assertThat(subGroupIds.length, is(1));

    String[] userIds = new String[1];
    userIds[0] = "1";
    subGroup = admin.getGroup(groupId);
    subGroup.setUserIds(userIds);
    admin.updateGroup(subGroup);

    // test if users of subgroups are indirectly attach to root group
    UserDetail[] users = admin.getAllUsersOfGroup("1");
    assertThat(users.length, is(1));
    subGroup.setUserIds(new String[0]);
    admin.updateGroup(subGroup);
    users = admin.getAllUsersOfGroup("1");
    assertThat(users.length, is(0));
  }


  @Test
  public void testGroupManager() throws AdminException {
    GroupProfileInst profile = admin.getGroupProfileInst("1");
    profile.addUser("1");
    admin.updateGroupProfileInst(profile);
    List<String> managerIds = admin.getUserManageableGroupIds("1");
    assertThat(managerIds, hasSize(1));
  }

}
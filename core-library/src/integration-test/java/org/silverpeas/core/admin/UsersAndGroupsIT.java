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
package org.silverpeas.core.admin;

import org.apache.commons.lang3.time.DateUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.space.SpaceServiceProvider;
import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.GroupProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.index.search.SearchEnginePropertiesManager;
import org.silverpeas.core.index.search.model.IndexSearcher;
import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.index.search.model.SearchEngineException;
import org.silverpeas.core.persistence.jdbc.AbstractTable;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.exception.TokenRuntimeException;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.memory.MemoryData;
import org.silverpeas.core.util.memory.MemoryUnit;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnDeleting;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;

@RunWith(Arquillian.class)
public class UsersAndGroupsIT {

  @Inject
  private Administration admin;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("/org/silverpeas/core/admin/domain/driver/create_table.sql")
          .loadInitialDataSetFrom("test-usersandgroups-dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(UsersAndGroupsIT.class)
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addSynchAndAsynchResourceEventFeatures()
        .addIndexEngineFeatures()
        .addSilverpeasUrlFeatures()
        .addProcessFeatures()
        .addPublicationTemplateFeatures()
        .addAsResource("org/silverpeas/jobStartPagePeas/settings")
        .addAsResource("org/silverpeas/core/admin/domain/driver")
        .addAsResource("org/silverpeas/index/search")
        .addPackages(true, "org.silverpeas.core.index.search.model")
        .addPackages(false, "org.silverpeas.core.admin.space.quota")
        .addPackages(false, "org.silverpeas.core.contribution.contentcontainer.container")
        .addPackages(false, "org.silverpeas.core.contribution.contentcontainer.content")
        .addPackages(true, "org.silverpeas.core.notification.user")
        .addClasses(FileRepositoryManager.class, FileFolderManager.class, MemoryUnit.class,
            MemoryData.class, SpaceServiceProvider.class,
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
    user.setEmailAddress("nicolas.eysseric@silverpeas.com");
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

    String newUserId = "12";
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
    user.setEmailAddress(newEmail);
    user.setAccessLevel(UserAccessLevel.USER);
    user.setTosAcceptanceDate(tosAcceptanceDate);
    user.setLastLoginDate(lastLoginDate);
    user.setNbSuccessfulLoginAttempts(7);
    user.setLastLoginCredentialUpdateDate(lastLoginCredentialUpdateDate);
    user.setExpirationDate(expirationDate);
    admin.updateUser(user);

    user = admin.getUserDetail(updatedUserId);
    assertThat(user.getEmailAddress(), is(newEmail));
    assertThat(user.getAccessLevel(), is(UserAccessLevel.USER));
    assertThat(user.isAccessAdmin(), is(false));
    assertThat(user.isAccessDomainManager(), is(false));
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
    assertThat(user.isAccessPdcManager(), is(true));
    assertThat(user.isAccessUser(), is(false));
    assertThat(user.isAccessGuest(), is(false));
  }

  @Test
  public void shouldRemoveAndRestoreUser() throws Exception {
    final String userIdToRemove = "1";
    final String userIdToRestore = "1";

    String userId = admin.removeUser(userIdToRemove);
    assertThat(userId, is(userIdToRemove));
    UserDetail user = admin.getUserDetail(userId);
    assertThat(user.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(user.getState(), is(UserState.REMOVED));
    assertThat(user.isRemovedState(), is(true));
    assertThat(user.isValidState(), is(false));

    userId = admin.restoreUser(userIdToRestore);
    assertThat(userId, is(userIdToRestore));
    user = admin.getUserDetail(userId);
    assertThat(user.getAccessLevel(), is(UserAccessLevel.ADMINISTRATOR));
    assertThat(user.getState(), is(UserState.VALID));
    assertThat(user.isRemovedState(), is(false));
    assertThat(user.isValidState(), is(true));
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
  public void getUsers() throws Exception {
    List<UserDetail> users = admin.getAllUsers();
    assertThat(users.size(), is(4));
    assertThat(users.get(0).getId(), is("1"));
    assertThat(users.get(1).getId(), is("3"));
    assertThat(users.get(2).getId(), is("2"));
    assertThat(users.get(3).getId(), is("10"));

    users = admin.getAllUsersFromNewestToOldest();
    assertThat(users.size(), is(4));
    assertThat(users.get(0).getId(), is("10"));
    assertThat(users.get(1).getId(), is("3"));
    assertThat(users.get(2).getId(), is("2"));
    assertThat(users.get(3).getId(), is("1"));

    List<String> domainIds = new ArrayList<>();
    domainIds.add("0");
    users = admin.getUsersOfDomains(domainIds);
    assertThat(users.size(), is(3));
    assertThat(users.get(0).getId(), is("1"));
    assertThat(users.get(1).getId(), is("3"));
    assertThat(users.get(2).getId(), is("2"));

    users = admin.getUsersOfDomainsFromNewestToOldest(domainIds);
    assertThat(users.size(), is(3));
    assertThat(users.get(0).getId(), is("3"));
    assertThat(users.get(1).getId(), is("2"));
    assertThat(users.get(2).getId(), is("1"));

    domainIds.set(0, "1");
    users = admin.getUsersOfDomains(domainIds);
    assertThat(users.size(), is(1));
    assertThat(users.get(0).getId(), is("10"));

    users = admin.getUsersOfDomainsFromNewestToOldest(domainIds);
    assertThat(users.size(), is(1));
    assertThat(users.get(0).getId(), is("10"));
  }

  @Test
  public void shouldAddGroup() throws Exception {
    GroupDetail group = new GroupDetail();
    group.setDomainId("0");
    group.setName("Groupe 3");
    String groupId = admin.addGroup(group);
    assertThat(groupId, is("21"));
  }

  @Test
  public void updateGroup() throws Exception {
    String desc = "New description";
    GroupDetail group = admin.getGroup("1");
    group.setDescription(desc);
    admin.updateGroup(group);
    group = admin.getGroup("1");
    assertThat(group.getDescription(), is(desc));
  }

  @Test
  public void shouldRemoveAndRestoreGroup() throws Exception {
    final String groupIdToRemove = "1";
    final String groupIdToRestore = "1";

    List<GroupDetail> groups = admin.removeGroup(groupIdToRemove);
    assertThat(groups, hasSize(1));
    String groupId = groups.iterator().next().getId();
    assertThat(groupId, is(groupIdToRemove));
    GroupDetail group = admin.getGroup(groupId);
    assertThat(group.getState(), is(GroupState.REMOVED));
    assertThat(group.isRemovedState(), is(true));
    assertThat(group.isValidState(), is(false));

    final List<GroupDetail> restoredGroups = admin.restoreGroup(groupIdToRestore);
    assertThat(restoredGroups, hasSize(1));
    groupId = restoredGroups.get(0).getId();
    assertThat(groupId, is(groupIdToRestore));
    group = admin.getGroup(groupId);
    assertThat(group.getState(), is(GroupState.VALID));
    assertThat(group.isRemovedState(), is(false));
    assertThat(group.isValidState(), is(true));
  }

  @Test
  public void shouldDeleteAllSubGroups() throws Exception {
    final List<GroupDetail> path = createSubGroupsAndGetSortedPath();
    // Removing first (group1)
    awaitUntil(1, SECONDS);
    final List<GroupDetail> deleted = admin.deleteGroupById(path.get(0).getId());
    // Verifying all deleted
    assertThat(deleted, hasSize(path.size()));
    int bound = path.size();
    for (int i = 0; i < bound; i++) {
      final GroupDetail previous = path.get(i);
      final GroupDetail returnDeleted = deleted.get(i);
      assertThat(returnDeleted, is(previous));
      final GroupDetail actual = admin.getGroup(previous.getId());
      assertThat(actual, nullValue());
    }
    // Verifying deleting already deleted groups
    try {
      admin.deleteGroupById(path.get(0).getId());
    } catch (AdminException e) {
      if (e.getMessage().equals(failureOnDeleting("group", path.get(0).getId()))) {
        return;
      }
    }
    fail("Should not be here");
  }

  @Test
  public void shouldRemoveAllSubGroups() throws Exception {
    final List<GroupDetail> path = createSubGroupsAndGetSortedPath();
    // Removing first (group1)
    awaitUntil(1, SECONDS);
    List<GroupDetail> removed = admin.removeGroup(path.get(0).getId());
    // Verifying all removed
    assertThat(removed, hasSize(path.size()));
    int bound = path.size();
    for (int i = 0; i < bound; i++) {
      final GroupDetail previous = path.get(i);
      final GroupDetail returnRemoved = removed.get(i);
      assertThat(returnRemoved.getId(), is(previous.getId()));
      final GroupDetail actual = admin.getGroup(previous.getId());
      assertThat(returnRemoved.getSuperGroupId(), is(previous.getSuperGroupId()));
      assertThat(actual.getSuperGroupId(), is(previous.getSuperGroupId()));
      assertThat(returnRemoved.getName(), is(previous.getName()));
      assertThat(actual.getName(), is(previous.getName()));
      assertThat(returnRemoved.getDescription(), is(previous.getDescription()));
      assertThat(actual.getDescription(), is(previous.getDescription()));
      assertThat(returnRemoved.getState(), is(GroupState.REMOVED));
      assertThat(actual.getState(), is(GroupState.REMOVED));
      assertThat(returnRemoved.getStateSaveDate(), greaterThan(previous.getStateSaveDate()));
      assertThat(actual.getStateSaveDate().getTime(), is(returnRemoved.getStateSaveDate().getTime()));
      assertThat(returnRemoved.getSaveDate(), is(returnRemoved.getStateSaveDate()));
      assertThat(actual.getSaveDate().getTime(), is(returnRemoved.getSaveDate().getTime()));
    }
    // Verifying removing already removed groups
    removed = admin.removeGroup(path.get(0).getId());
    assertThat(removed, empty());
  }

  @Test
  public void shouldRestoreAllParentGroups() throws Exception {
    // Removing first
    shouldRemoveAllSubGroups();
    // Current group path
    final List<GroupDetail> path = Stream.of("22", "21", "1").map(i -> {
      try {
        return admin.getGroup(i);
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }).collect(toList());
    assertThat(path.get(0).getSuperGroupId(), is(path.get(1).getId()));
    assertThat(path.get(1).getSuperGroupId(), is(path.get(2).getId()));
    // Restoring Sub Group Of Sub Group Of 1
    awaitUntil(1, SECONDS);
    List<GroupDetail> restored = admin.restoreGroup(path.get(0).getId());
    // Verifying all restored
    assertThat(restored, hasSize(path.size()));
    int bound = path.size();
    for (int i = 0; i < bound; i++) {
      final GroupDetail previous = path.get(i);
      final GroupDetail returnRemoved = restored.get(i);
      assertThat(returnRemoved.getId(), is(previous.getId()));
      final GroupDetail actual = admin.getGroup(previous.getId());
      assertThat(returnRemoved.getSuperGroupId(), is(previous.getSuperGroupId()));
      assertThat(actual.getSuperGroupId(), is(previous.getSuperGroupId()));
      assertThat(returnRemoved.getName(), is(previous.getName()));
      assertThat(actual.getName(), is(previous.getName()));
      assertThat(returnRemoved.getDescription(), is(previous.getDescription()));
      assertThat(actual.getDescription(), is(previous.getDescription()));
      assertThat(returnRemoved.getState(), is(GroupState.VALID));
      assertThat(actual.getState(), is(GroupState.VALID));
      assertThat(returnRemoved.getStateSaveDate(), greaterThan(previous.getStateSaveDate()));
      assertThat(actual.getStateSaveDate().getTime(), is(returnRemoved.getStateSaveDate().getTime()));
      assertThat(returnRemoved.getSaveDate(), is(returnRemoved.getStateSaveDate()));
      assertThat(actual.getSaveDate().getTime(), is(returnRemoved.getSaveDate().getTime()));
    }
    // Verifying removing already restored groups
    restored = admin.restoreGroup(path.get(0).getId());
    assertThat(restored, empty());
  }

  @Test
  public void shouldDeleteGroup() throws Exception {
    Group group = admin.getGroup("1");
    assertThat(group.getId(), is("1"));
    final List<GroupDetail> deletedGroups = admin.deleteGroupById("1");
    assertThat(deletedGroups, hasSize(1));
    group = admin.getGroup("1");
    assertThat(group, is(nullValue()));
  }

  @Test
  public void shouldFindUsersInGroup() throws Exception {
    GroupDetail subGroup = new GroupDetail();
    subGroup.setDomainId("0");
    subGroup.setName("Groupe 1-1");
    subGroup.setSuperGroupId("1");
    String groupId = admin.addGroup(subGroup);
    assertThat(groupId, is("21"));

    GroupDetail[] subGroups = admin.getAllSubGroups("1");
    assertThat(subGroups.length, is(1));

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
  public void getGroups() throws Exception {
    List<String> groupIds = admin.getAllGroups()
        .stream()
        .map(GroupDetail::getId)
        .sorted()
        .collect(toList());
    assertThat(groupIds, contains("1", "10"));

    groupIds = Stream.of(admin.getRootGroupsOfDomain("0"))
        .map(GroupDetail::getId)
        .sorted()
        .collect(toList());
    assertThat(groupIds, contains("1"));

    groupIds = Stream.of(admin.getRootGroupsOfDomain("1"))
        .map(GroupDetail::getId)
        .sorted()
        .collect(toList());
    assertThat(groupIds, contains("10"));
  }

  @Test
  public void getGroupsWithChildren() throws Exception {
    createSubGroupsAndGetSortedPath();
    List<String> groupIds = admin.getAllRootGroups()
        .stream()
        .map(GroupDetail::getId)
        .sorted()
        .collect(toList());

    assertThat(groupIds, contains("1", "10"));groupIds = admin.getAllGroups()
        .stream()
        .map(GroupDetail::getId)
        .sorted()
        .collect(toList());
    assertThat(groupIds, contains("1", "10", "21", "22"));

    groupIds = Stream.of(admin.getRootGroupsOfDomain("0"))
        .map(GroupDetail::getId)
        .sorted()
        .collect(toList());
    assertThat(groupIds, contains("1"));

    groupIds = Stream.of(admin.getRootGroupsOfDomain("1"))
        .map(GroupDetail::getId)
        .sorted()
        .collect(toList());
    assertThat(groupIds, contains("10"));
  }

  @Test
  public void isGroupExist() throws Exception {
    // Valid one
    assertThat(admin.isGroupExist("Groupe 10"), is(true));
    // Removed one
    assertThat(admin.isGroupExist("Groupe 20"), is(false));
  }

  @Test
  public void groupManager() throws AdminException {
    GroupProfileInst profile = admin.getGroupProfileInst("1");
    profile.addUser("1");
    admin.updateGroupProfileInst(profile);
    List<String> managerIds = admin.getUserManageableGroupIds("1");
    assertThat(managerIds, hasSize(1));
  }

  /**
   * Creating this structure from existing group1:
   * - Group 1
   * --- Sub Group Of 1
   * ------ Sub Group Of Sub Group Of 1
   * @throws AdminException if an error occurs
   */
  private List<GroupDetail> createSubGroupsAndGetSortedPath() throws AdminException {
    GroupDetail group1 = admin.getGroup("1");
    assertThat(group1, notNullValue());
    assertThat(group1.getId(), is("1"));
    assertThat(group1.getSuperGroupId(), nullValue());
    assertThat(group1.getState(), is(GroupState.VALID));
    GroupDetail subgroupOf1 = new GroupDetail();
    subgroupOf1.setDomainId("0");
    subgroupOf1.setName("Sub Group Of 1");
    subgroupOf1.setSuperGroupId(group1.getId());
    subgroupOf1 = admin.getGroup(admin.addGroup(subgroupOf1));
    assertThat(subgroupOf1, notNullValue());
    assertThat(subgroupOf1.getId(), is("21"));
    assertThat(subgroupOf1.getSuperGroupId(), is("1"));
    assertThat(subgroupOf1.getCreationDate(), notNullValue());
    assertThat(subgroupOf1.getSaveDate(), notNullValue());
    assertThat(subgroupOf1.getState(), is(GroupState.VALID));
    assertThat(subgroupOf1.getStateSaveDate(), notNullValue());
    GroupDetail subgroupOfSubGroupOf1 = new GroupDetail();
    subgroupOfSubGroupOf1.setDomainId("0");
    subgroupOfSubGroupOf1.setName("Sub Group Of Sub Group Of 1");
    subgroupOfSubGroupOf1.setSuperGroupId(subgroupOf1.getId());
    subgroupOfSubGroupOf1 = admin.getGroup(admin.addGroup(subgroupOfSubGroupOf1));
    assertThat(subgroupOfSubGroupOf1, notNullValue());
    assertThat(subgroupOfSubGroupOf1.getId(), is("22"));
    assertThat(subgroupOfSubGroupOf1.getSuperGroupId(), is(subgroupOf1.getId()));
    return List.of(group1, subgroupOf1, subgroupOfSubGroupOf1);
  }
}
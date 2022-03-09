/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.service;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.service.DefaultAdministration.CheckoutGroupDescriptor;
import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.admin.service.DefaultAdministration.CheckoutGroupDescriptor.synchronizingOneGroupWithSuperGroupId;
import static org.silverpeas.core.admin.service.DefaultAdministration.mergeDistantUserIntoSilverpeasUser;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class DefaultAdministrationTest {

  @TestManagedMock
  private DomainDriverManager driverManager;

  @Test
  void mergeDistantUserIntoSilverpeasUserWithoutChange() {
    final UserDetail user1 = aUser(1);
    final UserDetail user2 = aUser(1);
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(user1, user2);
    assertThat(dataChanged, is(false));
  }

  @Test
  void mergeDistantUserIntoSilverpeasUserWithSpecificChange() {
    final UserDetail user1 = aUser(1);
    final UserDetail user2 = aUser(1);
    user2.setSpecificId("newSpecificId");
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(user1, user2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDistantUserIntoSilverpeasUserWithFirstNameChange() {
    final UserDetail user1 = aUser(1);
    final UserDetail user2 = aUser(1);
    user2.setFirstName("newFirstName");
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(user1, user2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDistantUserIntoSilverpeasUserWithLastNameChange() {
    final UserDetail user1 = aUser(1);
    final UserDetail user2 = aUser(1);
    user2.setLastName("newLastName");
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(user1, user2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDistantUserIntoSilverpeasUserWithEmailChange() {
    final UserDetail user1 = aUser(1);
    final UserDetail user2 = aUser(1);
    user2.seteMail("newEmail");
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(user1, user2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDistantUserIntoSilverpeasUserWithLoginChange() {
    final UserDetail user1 = aUser(1);
    final UserDetail user2 = aUser(1);
    user2.setLogin("newLogin");
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(user1, user2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDistantUserIntoSilverpeasUserWithStateChange() {
    final UserDetail user1 = aUser(1);
    final UserDetail user2 = aUser(1);
    user2.setState(UserState.DEACTIVATED);
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(user1, user2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDistantUserIntoSilverpeasUserAllChanges() {
    final UserDetail user1 = aUser(1);
    final UserDetail user2 = aUser(1);
    user2.setSpecificId("newSpecificId");
    user2.setFirstName("newFirstName");
    user2.setLastName("newLastName");
    user2.seteMail("newEmail");
    user2.setLogin("newLogin");
    user2.setState(UserState.BLOCKED);
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(user1, user2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDeactivatedDistantUserIntoValidSilverpeasUser() {
    final UserDetail distant = aUser(1);
    distant.setState(UserState.DEACTIVATED);
    final UserDetail silverpeas = aUser(1);
    silverpeas.setState(UserState.VALID);
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(distant, silverpeas);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDeactivatedDistantUserIntoBlockedSilverpeasUser() {
    final UserDetail distant = aUser(1);
    distant.setState(UserState.DEACTIVATED);
    final UserDetail silverpeas = aUser(1);
    silverpeas.setState(UserState.BLOCKED);
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(distant, silverpeas);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeValidDistantUserIntoBlockedSilverpeasUser() {
    final UserDetail distant = aUser(1);
    distant.setState(UserState.VALID);
    final UserDetail silverpeas = aUser(1);
    silverpeas.setState(UserState.BLOCKED);
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(distant, silverpeas);
    assertThat(dataChanged, is(false));
  }

  @Test
  void mergeValidDistantUserIntoDeactivatedSilverpeasUser() {
    final UserDetail distant = aUser(1);
    distant.setState(UserState.VALID);
    final UserDetail silverpeas = aUser(1);
    silverpeas.setState(UserState.DEACTIVATED);
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(distant, silverpeas);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeValidDistantUserIntoRemovedSilverpeasUser() {
    final UserDetail distant = aUser(1);
    distant.setState(UserState.VALID);
    final UserDetail silverpeas = aUser(1);
    silverpeas.setState(UserState.REMOVED);
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(distant, silverpeas);
    assertThat(dataChanged, is(false));
  }

  @Test
  void mergeDeactivatedDistantUserIntoRemovedSilverpeasUser() {
    final UserDetail distant = aUser(1);
    distant.setState(UserState.DEACTIVATED);
    final UserDetail silverpeas = aUser(1);
    silverpeas.setState(UserState.REMOVED);
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(distant, silverpeas);
    assertThat(dataChanged, is(false));
  }

  @Test
  void mergeDeactivatedDistantUserIntoDeactivatedSilverpeasUser() {
    final UserDetail distant = aUser(1);
    distant.setState(UserState.DEACTIVATED);
    final UserDetail silverpeas = aUser(1);
    silverpeas.setState(UserState.DEACTIVATED);
    final boolean dataChanged = mergeDistantUserIntoSilverpeasUser(distant, silverpeas);
    assertThat(dataChanged, is(false));
  }

  @SuppressWarnings("SameParameterValue")
  private UserDetail aUser(final int i) {
    final UserDetail user = new UserDetail();
    user.setId(String.valueOf(i));
    user.setSpecificId("specificId_" + i);
    user.setLastName("LastName_" + i);
    user.setFirstName("FirstName_" + i);
    user.seteMail("email_" + i);
    user.setLogin("login_" + i);
    user.setState(UserState.VALID);
    return user;
  }

  @Test
  void mergeDistantGroupIntoSilverpeasGroupWithoutChange() {
    final GroupDetail group1 = aGroup(1);
    final GroupDetail group2 = aGroup(1);
    final boolean dataChanged = mergeDistantGroupIntoSilverpeasGroup(group1, group2);
    assertThat(dataChanged, is(false));
  }

  @Test
  void mergeDistantGroupIntoSilverpeasGroupWithSpecificChange() {
    final GroupDetail group1 = aGroup(1);
    final GroupDetail group2 = aGroup(1);
    group2.setSpecificId("newSpecificId");
    final boolean dataChanged = mergeDistantGroupIntoSilverpeasGroup(group1, group2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDistantGroupIntoSilverpeasGroupWithSuperGroupChange() {
    final GroupDetail group1 = aGroup(1);
    final GroupDetail group2 = aGroup(1);
    group2.setSuperGroupId("newSuperGroupId");
    final boolean dataChanged = mergeDistantGroupIntoSilverpeasGroup(group1, group2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDistantGroupIntoSilverpeasGroupWithSilverpeasUsersChange() {
    final GroupDetail group1 = aGroup(1);
    final GroupDetail group2 = aGroup(1);
    boolean dataChanged = mergeDistantGroupIntoSilverpeasGroup(group1, group2);
    assertThat(dataChanged, is(false));
    group2.setUserIds(new String[]{"User1", "User2"});
    dataChanged = mergeDistantGroupIntoSilverpeasGroup(group1, group2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDistantGroupIntoSilverpeasGroupWithDistantUsersChange() {
    final GroupDetail group1 = aGroup(1);
    final GroupDetail group2 = aGroup(1);
    boolean dataChanged = mergeDistantGroupIntoSilverpeasGroup(group1, group2);
    assertThat(dataChanged, is(false));
    group1.setUserIds(new String[]{"User1", "User2"});
    dataChanged = mergeDistantGroupIntoSilverpeasGroup(group1, group2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDistantGroupIntoSilverpeasGroupWithNameChange() {
    final GroupDetail group1 = aGroup(1);
    final GroupDetail group2 = aGroup(1);
    group2.setName("newName");
    final boolean dataChanged = mergeDistantGroupIntoSilverpeasGroup(group1, group2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeDistantGroupIntoSilverpeasGroupWithDescriptionChange() {
    final GroupDetail group1 = aGroup(1);
    final GroupDetail group2 = aGroup(1);
    group2.setDescription("newDescription");
    final boolean dataChanged = mergeDistantGroupIntoSilverpeasGroup(group1, group2);
    assertThat(dataChanged, is(true));
  }

  @Test
  void mergeValidDistantGroupIntoRemovedSilverpeasGroup() {
    final GroupDetail distant = aGroup(1);
    distant.setState(GroupState.VALID);
    final GroupDetail silverpeas = aGroup(1);
    silverpeas.setState(GroupState.REMOVED);
    final boolean dataChanged = mergeDistantGroupIntoSilverpeasGroup(distant, silverpeas);
    assertThat(dataChanged, is(false));
  }

  @SuppressWarnings("SameParameterValue")
  private GroupDetail aGroup(final int i) {
    final GroupDetail group = new GroupDetail();
    group.setId(String.valueOf(i));
    group.setSpecificId("specificId_" + i);
    group.setName("Name_" + i);
    group.setDescription("Description_" + i);
    group.setState(GroupState.VALID);
    return group;
  }

  @SuppressWarnings("unchecked")
  private boolean mergeDistantGroupIntoSilverpeasGroup(final GroupDetail group1,
      final GroupDetail group2) {
    final Map<String, String> userIdsMapping = Mockito.mock(Map.class);
    when(userIdsMapping.get(anyString())).then(i -> i.getArgument(0));
    final SyncOfGroupsContext context = new SyncOfGroupsContext("domainId", userIdsMapping);
    final CheckoutGroupDescriptor descriptor = synchronizingOneGroupWithSuperGroupId(null);
    try {
      final DefaultAdministration admin = new DefaultAdministration();
      FieldUtils.writeDeclaredField(admin, "domainDriverManager", driverManager, true);
      return admin.mergeDistantGroupIntoSilverpeasGroup(context, descriptor, group1, group2);
    } catch (Exception e) {
      assertThat("no exception attempted: " + e, false, is(true));
    }
    return false;
  }
}
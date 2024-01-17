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
package org.silverpeas.core.security.authorization;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.UserSpaceAvailabilityChecker;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.test.unit.UnitTest;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedMock;

import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.*;
import static org.silverpeas.core.security.authorization.AccessControlContext.init;
import static org.silverpeas.core.security.authorization.AccessControlOperation.*;
import static org.silverpeas.core.security.authorization.SpaceAccessControllerTest.ExpectedResult.authorized;
import static org.silverpeas.core.security.authorization.SpaceAccessControllerTest.ExpectedResult.notAuthorized;

/**
 * @author silveryocha
 */
@UnitTest
@EnableSilverTestEnv
class SpaceAccessControllerTest {

  private static final String SPACE_ID = "23";
  private static final String USER_ID = "26";

  @TestManagedMock
  private OrganizationController controller;

  @TestManagedMock
  private UserSpaceAvailabilityChecker checker;

  private SpaceAccessControl instance;
  private User user;

  @BeforeEach
  void setup() {
    when(controller.getUserSpaceAvailabilityChecker(USER_ID)).thenReturn(checker);
    when(controller.getUserManageableSpaceIds(USER_ID)).thenReturn(new String[0]);
    user = mock(User.class);
    when(UserProvider.get().getUser(USER_ID)).thenReturn(user);
  }

  @DisplayName("The user is not a space manager and no space is available to him")
  @Test
  void testUserIsNotAuthorizedToAccessAndToModify() {
    final ExpectedResult notAuthorized = notAuthorized().andNoUserRole();
    assertAuthorizedAndGetUserRolesForNormalAccess(notAuthorized);
    assertAuthorizedAndGetUserRolesForManagementAccess(notAuthorized);
  }

  @DisplayName("The user is not a space manager, no space is available to him, but he has admin access")
  @Test
  void testUserHasAdminAccessIsNotAuthorizedToAccessButAuthorizedToModify() {
    userHasAdminAccess();
    final ExpectedResult notAuthorized = notAuthorized().andNoUserRole();
    assertAuthorizedAndGetUserRolesForNormalAccess(notAuthorized);
    final ExpectedResult authorized = authorized().withUserRole(MANAGER);
    assertAuthorizedAndGetUserRolesForManagementAccess(authorized);
  }

  @DisplayName("The user is not a space manager but space is available to him")
  @Test
  void testUserIsAuthorizedToAccessButNotToModify() {
    spaceIsAvailableToUserAs(USER);
    final ExpectedResult authorized = authorized().withUserRole(USER);
    assertAuthorizedAndGetUserRolesForNormalAccess(authorized);
    final ExpectedResult notAuthorized = notAuthorized().andNoUserRole();
    assertAuthorizedAndGetUserRolesForManagementAccess(notAuthorized);
  }

  @DisplayName("The user is not a space manager but space is available to him")
  @Test
  void testUserIsAuthorizedToModifyButNotToAccess() {
    userIsSpaceManager();
    final ExpectedResult notAuthorized = notAuthorized().andNoUserRole();
    assertAuthorizedAndGetUserRolesForNormalAccess(notAuthorized);
    final ExpectedResult authorized = authorized().withUserRole(MANAGER);
    assertAuthorizedAndGetUserRolesForManagementAccess(authorized);
  }

  @DisplayName("The user is a space manager and space is available to him")
  @Test
  void testUserIsAuthorizedToAccessAndToModify() {
    spaceIsAvailableToUserAs(USER, WRITER, MANAGER);
    userIsSpaceManager();
    final ExpectedResult authorized = authorized().withUserRole(USER, WRITER);
    assertAuthorizedAndGetUserRolesForNormalAccess(authorized);
    assertAuthorizedAndGetUserRolesForManagementAccess(authorized.withUserRole(MANAGER));
  }

  @DisplayName("The user is a space manager and space is available to him because of a public " +
      "component instance")
  @Test
  void testUserIsAuthorizedToAccessInCaseOfPublicComponentInstanceAndToModify() {
    spaceIsAvailableToUserBecauseOfPublicComponent();
    userIsSpaceManager();
    final ExpectedResult authorized = authorized().withUserRole(USER);
    assertAuthorizedAndGetUserRolesForNormalAccess(authorized);
    assertAuthorizedAndGetUserRolesForManagementAccess(authorized.withUserRole(MANAGER));
  }

  @DisplayName("The user has not space management authorization")
  @Test
  void testUserHasNotSpaceManagementAuthorization() {
    initTest();
    assertUserSpaceManagementAuthorization(false);
  }

  @DisplayName("The user has admin access and so he has space management authorization")
  @Test
  void testUserWithAdminAccessHasSpaceManagementAuthorization() {
    initTest();
    userHasAdminAccess();
    assertUserSpaceManagementAuthorization(true);
  }

  @DisplayName("The user has space management authorization")
  @Test
  void testUserHasSpaceManagementAuthorization() {
    initTest();
    userIsSpaceManager();
    assertUserSpaceManagementAuthorization(true);
  }

  void assertUserSpaceManagementAuthorization(final boolean expected) {
    initTest();
    AccessControlContext context = init();
    assertThat(instance.hasUserSpaceManagementAuthorization(USER_ID, SPACE_ID, context), is(expected));
    assertThat(context.getOperations(), not(contains(MODIFICATION)));
    initTest();
    context = init().onOperationsOf(MODIFICATION);
    assertThat(instance.hasUserSpaceManagementAuthorization(USER_ID, SPACE_ID, context), is(expected));
    assertThat(context.getOperations(), contains(MODIFICATION));
    initTest();
    context = init().onOperationsOf(SEARCH, MODIFICATION);
    assertThat(instance.hasUserSpaceManagementAuthorization(USER_ID, SPACE_ID, context), is(expected));
    assertThat(context.getOperations(), containsInAnyOrder(SEARCH, MODIFICATION));
  }

  void assertAuthorizedAndGetUserRolesForNormalAccess(final ExpectedResult expected) {
    assertNormalAccess(expected.authorized);
    assertGetUserRolesForNormalAccess(expected.userRoles);
  }

  void assertNormalAccess(final boolean expected) {
    initTest();
    assertThat(instance.isUserAuthorized(USER_ID, SPACE_ID), is(expected));
    EnumSet.allOf(AccessControlOperation.class)
        .stream()
        .filter(o -> o != UNKNOWN && o != CREATION && o != MODIFICATION && o != DELETION)
        .forEach(o -> {
          initTest();
          assertThat(
              instance.isUserAuthorized(USER_ID, SPACE_ID, init().onOperationsOf(o)),
              is(expected));
        });
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  void assertGetUserRolesForNormalAccess(final SilverpeasRole... expectedRoles) {
    initTest();
    final Matcher matcher = expectedRoles.length > 0 ? containsInAnyOrder(expectedRoles) : empty();
    assertThat(instance.getUserRoles(USER_ID, SPACE_ID,  init()), matcher);
    EnumSet.allOf(AccessControlOperation.class)
        .stream()
        .filter(o -> o != UNKNOWN && o != CREATION && o != MODIFICATION && o != DELETION)
        .forEach(o -> {
          initTest();
          assertThat(
              instance.getUserRoles(USER_ID, SPACE_ID, init().onOperationsOf(o)),
              matcher);
        });
  }

  void assertAuthorizedAndGetUserRolesForManagementAccess(final ExpectedResult expected) {
    assertManagementAccess(expected.authorized);
    assertGetUserRolesForManagementAccess(expected.userRoles);
  }

  void assertManagementAccess(final boolean expected) {
    initTest();
    assertThat(instance.isUserAuthorized(USER_ID, SPACE_ID, init().onOperationsOf(MODIFICATION)), is(expected));
    EnumSet.of(CREATION, DELETION)
        .stream()
        .forEach(o -> {
          initTest();
          assertThat(
              instance.isUserAuthorized(USER_ID, SPACE_ID, init().onOperationsOf(o)),
              is(false));
        });
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  void assertGetUserRolesForManagementAccess(final SilverpeasRole... expectedRoles) {
    initTest();
    final Matcher matcher = expectedRoles.length > 0 ? containsInAnyOrder(expectedRoles) : empty();
    assertThat(instance.getUserRoles(USER_ID, SPACE_ID, init().onOperationsOf(MODIFICATION)), matcher);
    EnumSet.of(CREATION, DELETION)
        .stream()
        .forEach(o -> {
          initTest();
          assertThat(
              instance.getUserRoles(USER_ID, SPACE_ID, init().onOperationsOf(o)),
              empty());
        });
  }

  private void userHasAdminAccess() {
    when(user.isAccessAdmin()).thenReturn(true);
  }

  private void spaceIsAvailableToUserAs(SilverpeasRole role) {
    spaceIsAvailableToUserAs(role, null);
  }

  private void spaceIsAvailableToUserAs(SilverpeasRole role1, SilverpeasRole role2) {
    spaceIsAvailableToUserAs(role1, role2, null);
  }

  private void spaceIsAvailableToUserAs(SilverpeasRole role1, SilverpeasRole role2,
      SilverpeasRole role3) {
    when(checker.isAvailable(SPACE_ID)).thenReturn(true);
    when(controller.getSpaceUserProfilesBySpaceId(USER_ID, SPACE_ID)).thenReturn(
        Stream.of(role1, role2, role3)
            .filter(Objects::nonNull)
            .map(SilverpeasRole::toString)
            .collect(Collectors.toList()));
  }

  private void spaceIsAvailableToUserBecauseOfPublicComponent() {
    when(checker.isAvailable(SPACE_ID)).thenReturn(true);
  }

  private void userIsSpaceManager() {
    when(controller.getUserManageableSpaceIds(USER_ID)).thenReturn(new String[]{SPACE_ID});
  }

  private void initTest() {
    instance = new SpaceAccessController();
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  static class ExpectedResult {
    private final boolean authorized;
    private SilverpeasRole[] userRoles = null;

    private ExpectedResult(final boolean authorized) {
      this.authorized = authorized;
    }

    static ExpectedResult notAuthorized() {
      return new ExpectedResult(false);
    }

    static ExpectedResult authorized() {
      return new ExpectedResult(true);
    }

    ExpectedResult andNoUserRole() {
      userRoles = new SilverpeasRole[0];
      return this;
    }

    ExpectedResult withUserRole(final SilverpeasRole... expectedRoles) {
      userRoles = expectedRoles;
      return this;
    }
  }
}

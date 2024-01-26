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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.UserSpaceAvailabilityChecker;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.UnitTest;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.core.util.CollectionUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author silveryocha
 */
@UnitTest
@EnableSilverTestEnv(context = JEETestContext.class)
class SpaceAccessControllerFilterTest {

  private static final String SPACE_ID_1 = "1";
  private static final String SPACE_ID_2 = "2";
  private static final List<String> ALL_SPACES = List.of(SPACE_ID_1, SPACE_ID_2);
  private static final String USER_ID = "26";

  @TestManagedMock
  private OrganizationController controller;

  @TestManagedMock
  private UserSpaceAvailabilityChecker checker;

  private SpaceAccessControl testInstance;
  private TestContext testContext;
  private User user;

  @BeforeEach
  void setup() {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    when(controller.getUserSpaceAvailabilityChecker(USER_ID)).thenReturn(checker);
    when(controller.getUserManageableSpaceIds(USER_ID)).thenReturn(new String[0]);
    user = mock(User.class);
    when(UserProvider.get().getUser(USER_ID)).thenReturn(user);
    testContext = new TestContext();
  }

  @Test
  void filterAuthorizedByUserShouldLoadCaches() {
    executeFilterAuthorizedByUserWithSpaceIds(ALL_SPACES);
    assertFilterAuthorizedByUserShouldLoadCaches();
  }

  private void assertFilterAuthorizedByUserShouldLoadCaches() {
    final SpaceAccessControllerFilterTest.TestVerifyResults results = testContext.results();
    final SpaceAccessController.DataManager spaceDataManager = results.getSpaceDataManager();
    assertSpaceControllerCalls();
    assertThat(spaceDataManager.spaceUserProfiles.keySet(), containsInAnyOrder(SPACE_ID_1, SPACE_ID_2));
  }

  @Test
  void filterAuthorizedByUserOnSingleSpaceShouldLoadCaches() {
    executeFilterAuthorizedByUserWithSpaceIds(List.of(SPACE_ID_1));
    assertFilterAuthorizedByUserOnSingleSpaceShouldLoadCaches();
  }

  private void assertFilterAuthorizedByUserOnSingleSpaceShouldLoadCaches() {
    final SpaceAccessControllerFilterTest.TestVerifyResults results = testContext.results();
    final SpaceAccessController.DataManager spaceDataManager = results.getSpaceDataManager();
    assertSpaceControllerCalls();
    assertThat(spaceDataManager.spaceUserProfiles.keySet(), containsInAnyOrder(SPACE_ID_1));
  }

  private void assertSpaceControllerCalls() {
    verify(controller, times(1)).getSpaceUserProfilesBySpaceIds(anyString(), anyCollection());
  }

  /**
   * Centralization.
   */
  private void executeFilterAuthorizedByUserWithSpaceIds(
      final List<String> listToFilter,
      final AccessControlOperation... operations) {
    testContext.setup();
    Stream.of(operations).forEach(o -> testContext.accessControlContext.onOperationsOf(o));
    testInstance.filterAuthorizedByUser(listToFilter, USER_ID, testContext.accessControlContext);
  }

  private class TestContext {
    private SpaceAccessControl spaceAccessController;
    private AccessControlContext accessControlContext;
    private SpaceAccessControllerFilterTest.TestVerifyResults testVerifyResults;

    public void clear() {
      CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
      Mockito.reset(user, controller);
      spaceAccessController = mock(SpaceAccessControl.class);
      testInstance = new SpaceAccessController();
      accessControlContext = AccessControlContext.init();
      testVerifyResults = new SpaceAccessControllerFilterTest.TestVerifyResults(this);
    }

    public void setup() {
      clear();
      when(spaceAccessController
          .getUserRoles(anyString(), anyString(), ArgumentMatchers.any(AccessControlContext.class)))
          .then(new Returns(singleton(SilverpeasRole.USER)));
      when(spaceAccessController.isUserAuthorized(anySet())).then(new Returns(true));
      when(controller.getSpaceUserProfilesBySpaceIds(anyString(), anyCollection())).thenAnswer(a -> {
        final Collection<String> spaceIds = a.getArgument(1);
        final Map<String, Set<String>> result = new HashMap<>(spaceIds.size());
        spaceIds.forEach(i -> result.put(i, CollectionUtil.asSet(SilverpeasRole.USER.getName())));
        return result;
      });
    }

    public SpaceAccessControllerFilterTest.TestVerifyResults results() {
      return testVerifyResults;
    }
  }

  private static class TestVerifyResults {
    final SpaceAccessControllerFilterTest.TestContext testContext;

    TestVerifyResults(final SpaceAccessControllerFilterTest.TestContext testContext) {
      this.testContext = testContext;
    }

    SpaceAccessController.DataManager getSpaceDataManager() {
      return SpaceAccessController.getDataManager(testContext.accessControlContext);
    }
  }
}

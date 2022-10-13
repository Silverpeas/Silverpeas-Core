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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.RemovedSpaceAndComponentInstanceChecker;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.security.authorization.AccessControlOperation.MODIFICATION;
import static org.silverpeas.core.security.authorization.AccessControlOperation.SEARCH;

/**
 * @author silveryocha
 */
@UnitTest
@EnableSilverTestEnv
class TestComponentAccessControllerFilter {

  private static final String USER_ID = "bart";
  // NO RIGHT ON TOPIC
  private static final String KMELIA_38 = "kmelia38";
  // RIGHTS ON TOPIC
  private static final String KMELIA_83 = "kmelia83";

  private static final List<String> ALL_COMPONENTS = Arrays
      .asList(KMELIA_38, KMELIA_83);

  private static final List<String> COMPONENTS_WITH_INHERITED_RIGHTS = Collections
      .singletonList(KMELIA_38);

  private static final List<String> COMPONENTS_WITH_SPECIFIC_RIGHTS = Collections
      .singletonList(KMELIA_83);

  @TestManagedMock
  private OrganizationController organizationController;
  @TestManagedMock
  private RemovedSpaceAndComponentInstanceChecker checker;
  private ComponentAccessControl testInstance;
  private TestContext testContext;
  private User user;

  @BeforeEach
  void setup() {
    when(ServiceProvider.getService(RemovedSpaceAndComponentInstanceChecker.class)).thenReturn(checker);
    when(checker.resetWithCacheSizeOf(any(Integer.class))).thenReturn(checker);
    user = mock(User.class);
    when(UserProvider.get().getUser(USER_ID)).thenReturn(user);
    testContext = new TestContext();
  }

  @Test
  void filterAuthorizedByUserShouldLoadCaches() {
    executeFilterAuthorizedByUserWithComponentIds(ALL_COMPONENTS);
    assertFilterAuthorizedByUserShouldLoadCaches();
  }

  private void assertFilterAuthorizedByUserShouldLoadCaches() {
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    assertAvailableComponentCache(componentDataManager);
    assertThat(componentDataManager.userProfiles.keySet(), containsInAnyOrder(KMELIA_38, KMELIA_83));
    assertThat(componentDataManager.componentParameterValueCache.keySet(), containsInAnyOrder(KMELIA_38, KMELIA_83));
  }

  @Test
  void filterAuthorizedByUserOnInheritedRightComponentShouldLoadCaches() {
    executeFilterAuthorizedByUserWithComponentIds(COMPONENTS_WITH_INHERITED_RIGHTS);
    assertFilterAuthorizedByUserOnInheritedRightComponentShouldLoadCaches();
  }

  private void assertFilterAuthorizedByUserOnInheritedRightComponentShouldLoadCaches() {
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    assertAvailableComponentCache(componentDataManager);
    assertThat(componentDataManager.userProfiles.keySet(), containsInAnyOrder(KMELIA_38));
    assertThat(componentDataManager.componentParameterValueCache.keySet(), containsInAnyOrder(KMELIA_38));
  }

  @Test
  void filterAuthorizedByUserOnSpecificRightOnTopicComponentShouldLoadCaches() {
    executeFilterAuthorizedByUserWithComponentIds(COMPONENTS_WITH_SPECIFIC_RIGHTS);
    assertFilterAuthorizedByUserOnSpecificRightOnTopicComponentShouldLoadCaches();
  }

  private void assertFilterAuthorizedByUserOnSpecificRightOnTopicComponentShouldLoadCaches() {
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    assertAvailableComponentCache(componentDataManager);
    assertThat(componentDataManager.userProfiles.keySet(), containsInAnyOrder(KMELIA_83));
    assertThat(componentDataManager.componentParameterValueCache.keySet(), containsInAnyOrder(KMELIA_83));
  }

  @Test
  void filterAuthorizedByUserWithSearchContextShouldLoadCaches() {
    executeFilterAuthorizedByUserWithComponentIds(ALL_COMPONENTS, SEARCH);
    assertFilterAuthorizedByUserWithSearchContextShouldLoadCaches();
  }

  private void assertFilterAuthorizedByUserWithSearchContextShouldLoadCaches() {
    assertFilterAuthorizedByUserShouldLoadCaches();
  }

  @Test
  void filterAuthorizedByUserWithModifyContextShouldLoadCaches() {
    executeFilterAuthorizedByUserWithComponentIds(ALL_COMPONENTS, MODIFICATION);
    assertFilterAuthorizedByUserWithModifyContextShouldLoadCaches();
  }

  private void assertFilterAuthorizedByUserWithModifyContextShouldLoadCaches() {
    assertFilterAuthorizedByUserShouldLoadCaches();
  }

  private void assertAvailableComponentCache( final ComponentAccessController.DataManager componentDataManager) {
    assertThat(componentDataManager.availableComponentCache, containsInAnyOrder(KMELIA_38, KMELIA_83));
    verify(organizationController, times(1)).getAvailableComponentsByUser(anyString());
    verify(organizationController, times(1)).getUserProfilesByComponentId(anyString(), anyCollection());
  }

  /**
   * Centralization.
   */
  private void executeFilterAuthorizedByUserWithComponentIds(
      final List<String> listToFilter,
      final AccessControlOperation... operations) {
    testContext.setup();
    Stream.of(operations).forEach(o -> testContext.accessControlContext.onOperationsOf(o));
    testInstance.filterAuthorizedByUser(listToFilter, USER_ID, testContext.accessControlContext);
  }

  private class TestContext {
    private ComponentAccessControl componentAccessController;
    private AccessControlContext accessControlContext;
    private TestVerifyResults testVerifyResults;

    public void clear() {
      CacheServiceProvider.clearAllThreadCaches();
      Mockito.reset(user, organizationController);
      componentAccessController = mock(ComponentAccessControl.class);
      testInstance = new ComponentAccessController(organizationController);
      accessControlContext = AccessControlContext.init();
      testVerifyResults = new TestVerifyResults(this);
    }

    public void setup() {
      clear();
      when(user.getId()).thenReturn(USER_ID);
      when(user.isAnonymous()).thenReturn(false);
      when(componentAccessController
          .getUserRoles(anyString(), anyString(), ArgumentMatchers.any(AccessControlContext.class)))
          .then(new Returns(singleton(SilverpeasRole.USER)));
      when(componentAccessController.isUserAuthorized(anySet())).then(new Returns(true));
      when(organizationController.getComponentInstance(anyString()))
          .thenAnswer(a -> {
            final String i = a.getArgument(0);
            final SilverpeasComponentInstance instance = mock(SilverpeasComponentInstance.class);
            when(instance.isTopicTracker()).then(new Returns(i.startsWith("kmelia") || i.startsWith("kmax") || i.startsWith("toolbox")));
            return Optional.of(instance);
          });
      when(organizationController.getParameterValuesByComponentIdThenByParamName(anyCollection(), eq(Arrays
          .asList("rightsOnTopics", "usePublicationSharing", "useFileSharing",
              "useFolderSharing", "coWriting", "publicFiles"))))
          .thenAnswer(a -> {
            final Map<String, Map<String, String>> result = new HashMap<>(1);
            final Collection<String> instanceIds = a.getArgument(0);
            instanceIds.forEach(i ->  {
              final Map<String, String> paramValues = new HashMap<>(3);
              paramValues.put("rightsOnTopics", KMELIA_83.equals(i) ? "1" : "0");
              result.put(i, paramValues);
            });
            return result;
          });
      when(organizationController.getUserProfilesByComponentId(anyString(), anyCollection())).thenAnswer(a -> {
        final Collection<String> instanceIds = a.getArgument(1);
        final Map<String, Set<String>> result = new HashMap<>(instanceIds.size());
        instanceIds.forEach(i -> result.put(i, CollectionUtil.asSet(SilverpeasRole.USER.getName())));
        return result;
      });
      when(organizationController.getAvailableComponentsByUser(anyString()))
          .thenAnswer(a -> Arrays.asList(KMELIA_38, KMELIA_83));
      ((SessionCacheService) CacheServiceProvider.getSessionCacheService()).newSessionCache(user);
    }

    public TestVerifyResults results() {
      return testVerifyResults;
    }
  }

  private class TestVerifyResults {
    final TestContext testContext;

    TestVerifyResults(final TestContext testContext) {
      this.testContext = testContext;
    }

    ComponentAccessController.DataManager getComponentDataManager() {
      return ComponentAccessController.getDataManager(testContext.accessControlContext);
    }
  }
}

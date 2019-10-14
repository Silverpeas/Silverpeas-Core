/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.security.authorization;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.silverpeas.core.admin.ObjectType;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.test.rule.LibCoreCommonAPI4Test;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.security.authorization.AccessControlOperation.modification;
import static org.silverpeas.core.security.authorization.AccessControlOperation.search;

/**
 * @author silveryocha
 */
@UnitTest
public class TestNodeAccessControllerFilter {

  private static final String USER_ID = "bart";
  // NO RIGHT ON TOPIC
  private static final String KMELIA_38 = "kmelia38";
  private static final NodeDetail4Test NODE_38_26 = new NodeDetail4Test("3826", KMELIA_38);
  private static final NodeDetail4Test NODE_38_62 = new NodeDetail4Test("3862", KMELIA_38);
  // RIGHTS ON TOPIC
  private static final String KMELIA_83 = "kmelia83";
  // INHERITED RIGHTS
  private static final NodeDetail4Test NODE_83_260 = new NodeDetail4Test("83260", KMELIA_83);
  // SPECIFIC RIGHTS
  private static final NodeDetail4Test NODE_83_620 = new NodeDetail4Test("83620", KMELIA_83, true);

  private static final List<NodeDetail4Test> ALL_NODES = Arrays
      .asList(NODE_38_26, NODE_38_62, NODE_83_260, NODE_83_620);

  private static final List<NodeDetail4Test> NODES_WITH_INHERITED_RIGHTS = Arrays
      .asList(NODE_38_26, NODE_38_62);

  private static final List<NodeDetail4Test> NODES_WITH_SPECIFIC_RIGHTS = Arrays
      .asList(NODE_83_260, NODE_83_620);

  private NodeService nodeService;
  private OrganizationController organizationController;
  private NodeAccessControl testInstance;
  private TestContext testContext;
  private User user;

  @Rule
  public LibCoreCommonAPI4Test commonAPI4Test = new LibCoreCommonAPI4Test();

  @Before
  public void setup() {
    user = mock(User.class);
    when(UserProvider.get().getUser(USER_ID)).thenReturn(user);
    nodeService = mock(NodeService.class);
    commonAPI4Test.injectIntoMockedBeanContainer(nodeService);
    organizationController = mock(OrganizationController.class);
    commonAPI4Test.injectIntoMockedBeanContainer(organizationController);
    testContext = new TestContext();
  }

  @Test
  public void filterAuthorizedByUserShouldLoadCaches() {
    executeFilterAuthorizedByUserWithNodePks(toNodePks(ALL_NODES));
    assertFilterAuthorizedByUserShouldLoadCaches();
  }

  @SuppressWarnings("unchecked")
  private void assertFilterAuthorizedByUserShouldLoadCaches() {
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    final NodeAccessController.DataManager nodeDataManager = results.getNodeDataManager();
    assertAvailableComponentCache(componentDataManager);
    assertThat(componentDataManager.userProfiles.keySet(), containsInAnyOrder(KMELIA_38, KMELIA_83));
    assertThat(componentDataManager.componentParameterValueCache.keySet(), containsInAnyOrder(KMELIA_38, KMELIA_83));
    assertThat(nodeDataManager.nodeDetailCache.values(), containsInAnyOrder(NODE_83_260, NODE_83_620));
    assertThat(nodeDataManager.instanceIdsWithRightsOnTopic, containsInAnyOrder(KMELIA_83));
    assertThat(nodeDataManager.userProfiles.keySet(), containsInAnyOrder(
        Pair.of(KMELIA_83, NODE_83_620.getId())));
    // Node level
    verify(nodeService, times(1)).getMinimalDataByInstances(anyCollection());
    final ArgumentCaptor<Collection<Integer>> nodeIds = ArgumentCaptor.forClass(Collection.class);
    verify(organizationController, times(1))
        .getUserProfilesByComponentAndObject(anyString(), anyCollection(), nodeIds.capture(),
            eq(ObjectType.NODE));
    final Collection<Integer> capturedNodeIds = nodeIds.getValue();
    assertThat(capturedNodeIds, not(hasItem(-1)));
  }

  @Test
  public void filterAuthorizedByUserOnInheritedRightComponentShouldLoadCaches() {
    executeFilterAuthorizedByUserWithNodePks(toNodePks(NODES_WITH_INHERITED_RIGHTS));
    assertFilterAuthorizedByUserOnInheritedRightComponentShouldLoadCaches();
  }

  private void assertFilterAuthorizedByUserOnInheritedRightComponentShouldLoadCaches() {
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    final NodeAccessController.DataManager nodeDataManager = results.getNodeDataManager();
    assertAvailableComponentCache(componentDataManager);
    assertThat(componentDataManager.userProfiles.keySet(), containsInAnyOrder(KMELIA_38));
    assertThat(componentDataManager.componentParameterValueCache.keySet(), containsInAnyOrder(KMELIA_38));
    assertThat(nodeDataManager.nodeDetailCache.values(), empty());
    assertThat(nodeDataManager.instanceIdsWithRightsOnTopic, empty());
    assertThat(nodeDataManager.userProfiles.keySet(), empty());
    // Node level
    verify(nodeService, times(0)).getMinimalDataByInstances(anyCollection());
    verify(organizationController, times(0))
        .getUserProfilesByComponentAndObject(anyString(), anyCollection(), anyCollection(),
            eq(ObjectType.NODE));
  }

  @Test
  public void filterAuthorizedByUserOnSpecificRightOnTopicComponentShouldLoadCaches() {
    executeFilterAuthorizedByUserWithNodePks(toNodePks(NODES_WITH_SPECIFIC_RIGHTS));
    assertFilterAuthorizedByUserOnSpecificRightOnTopicComponentShouldLoadCaches();
  }

  @SuppressWarnings("unchecked")
  private void assertFilterAuthorizedByUserOnSpecificRightOnTopicComponentShouldLoadCaches() {
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    final NodeAccessController.DataManager nodeDataManager = results.getNodeDataManager();
    assertAvailableComponentCache(componentDataManager);
    assertThat(componentDataManager.userProfiles.keySet(), containsInAnyOrder(KMELIA_83));
    assertThat(componentDataManager.componentParameterValueCache.keySet(), containsInAnyOrder(KMELIA_83));
    assertThat(nodeDataManager.nodeDetailCache.values(), containsInAnyOrder(NODE_83_260, NODE_83_620));
    assertThat(nodeDataManager.instanceIdsWithRightsOnTopic, containsInAnyOrder(KMELIA_83));
    assertThat(nodeDataManager.userProfiles.keySet(), containsInAnyOrder(Pair.of(KMELIA_83, NODE_83_620.getId())));
    // Node level
    verify(nodeService, times(1)).getMinimalDataByInstances(anyCollection());
    final ArgumentCaptor<Collection<Integer>> nodeIds = ArgumentCaptor.forClass(Collection.class);
    verify(organizationController, times(1))
        .getUserProfilesByComponentAndObject(anyString(), anyCollection(), nodeIds.capture(),
            eq(ObjectType.NODE));
    final Collection<Integer> capturedNodeIds = nodeIds.getValue();
    assertThat(capturedNodeIds, not(hasItem(-1)));
  }

  @Test
  public void filterAuthorizedByUserWithSearchContextShouldLoadCaches() {
    executeFilterAuthorizedByUserWithNodePks(toNodePks(ALL_NODES), search);
    assertFilterAuthorizedByUserWithSearchContextShouldLoadCaches();
  }

  private void assertFilterAuthorizedByUserWithSearchContextShouldLoadCaches() {
    assertFilterAuthorizedByUserShouldLoadCaches();
  }

  @Test
  public void filterAuthorizedByUserWithModifyContextShouldLoadCaches() {
    executeFilterAuthorizedByUserWithNodePks(toNodePks(ALL_NODES), modification);
    assertFilterAuthorizedByUserWithModifyContextShouldLoadCaches();
  }

  private void assertFilterAuthorizedByUserWithModifyContextShouldLoadCaches() {
    assertFilterAuthorizedByUserShouldLoadCaches();
  }

  private void assertAvailableComponentCache( final ComponentAccessController.DataManager componentDataManager) {
    assertThat(componentDataManager.availableComponentCache, containsInAnyOrder(KMELIA_38, KMELIA_83));
    verify(organizationController, times(1)).getAvailableComponentsByUser(anyString());
    verify(organizationController, times(1)).getUserProfilesByComponent(anyString(), anyCollection());
  }

  private static List<NodePK> toNodePks(final List<NodeDetail4Test> nodes) {
    return nodes.stream().map(NodeDetail::getNodePK).collect(Collectors.toList());
  }

  /**
   * Centralization.
   */
  private void executeFilterAuthorizedByUserWithNodePks(
      final List<NodePK> listToFilter,
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
      Mockito.reset(user, organizationController, nodeService);
      componentAccessController = mock(ComponentAccessControl.class);
      testInstance = new NodeAccessController(componentAccessController);
      accessControlContext = AccessControlContext.init();
      testVerifyResults = new TestVerifyResults(this);
    }

    @SuppressWarnings("unchecked")
    public void setup() {
      clear();
      when(user.getId()).thenReturn(USER_ID);
      when(user.isAnonymous()).thenReturn(false);
      when(componentAccessController
          .getUserRoles(anyString(), anyString(), ArgumentMatchers.any(AccessControlContext.class)))
          .then(new Returns(singleton(SilverpeasRole.user)));
      when(componentAccessController.isUserAuthorized(anySet())).then(new Returns(true));
      when(organizationController.getComponentInstance(anyString()))
          .thenAnswer(a -> {
            final String i = a.getArgument(0);
            final SilverpeasComponentInstance instance = mock(SilverpeasComponentInstance.class);
            when(instance.isTopicTracker()).then(new Returns(i.startsWith("kmelia") || i.startsWith("kmax") || i.startsWith("toolbox")));
            return Optional.of(instance);
          });
      when(organizationController.getParameterValuesByComponentAndByParamName(anyCollection(), eq(Arrays
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
      when(organizationController.getUserProfilesByComponent(anyString(), anyCollection())).thenAnswer(a -> {
        final Collection<String> instanceIds = a.getArgument(1);
        final Map<String, Set<String>> result = new HashMap<>(instanceIds.size());
        instanceIds.forEach(i -> result.put(i, CollectionUtil.asSet(SilverpeasRole.user.getName())));
        return result;
      });
      when(organizationController
          .getUserProfilesByComponentAndObject(anyString(), anyCollection(), anyCollection(),
              eq(ObjectType.NODE))).thenAnswer(a -> {
        final Collection<String> instanceIds = a.getArgument(1);
        final Collection<Integer> objectIds = a.getArgument(2);
        final Map<Pair<String, Integer>, Set<String>> result = new HashMap<>();
        ALL_NODES.stream()
            .filter(n -> instanceIds.contains(n.getNodePK().getInstanceId()))
            .filter(n -> objectIds.contains(n.getId()))
            .map(NodeDetail::getNodePK)
            .forEach(p -> result.put(Pair.of(p.getInstanceId(), Integer.parseInt(p.getId())),
                CollectionUtil.asSet(SilverpeasRole.user.getName())));
        return result;
      });
      when(organizationController.getAvailableComponentsByUser(anyString()))
          .thenAnswer(a -> Arrays.asList(KMELIA_38, KMELIA_83));
      when(nodeService.getMinimalDataByInstances(anyCollection())).then(
          a -> ((Collection<String>) a.getArgument(0)).stream()
              .flatMap(i -> ALL_NODES.stream().filter(l -> l.getNodePK().getInstanceId().equals(i)))
              .collect(Collectors.toList()));
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

    NodeAccessController.DataManager getNodeDataManager() {
      return NodeAccessController.getDataManager(testContext.accessControlContext);
    }
  }
}

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

package org.silverpeas.core.security.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.silverpeas.core.admin.ProfiledObjectIds;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.security.authorization.AccessControlOperation.MODIFICATION;
import static org.silverpeas.core.security.authorization.AccessControlOperation.SEARCH;

/**
 * @author silveryocha
 */
@UnitTest
@EnableSilverTestEnv
class TestPublicationAccessControllerFilter {

  private static final String USER_ID = "bart";
  // NO RIGHT ON TOPIC
  private static final String KMELIA_38 = "kmelia38";
  private static final TestNodeDetail NODE_38_26 = new TestNodeDetail("3826", KMELIA_38);
  private static final TestNodeDetail NODE_38_62 = new TestNodeDetail("3862", KMELIA_38);

  // RIGHTS ON TOPIC
  private static final String KMELIA_83 = "kmelia83";
  // INHERITED RIGHTS
  private static final TestNodeDetail NODE_83_260 = new TestNodeDetail("83260", KMELIA_83);
  // SPECIFIC RIGHTS
  private static final TestNodeDetail NODE_83_620 = new TestNodeDetail("83620", KMELIA_83, true);

  // OTHER ALIAS GED, RIGHTS ON TOPIC
  private static final String KMELIA_ALIAS_OTHER = "kmelia100";

  // SPECIFIC RIGHTS
  private static final TestNodeDetail
      NODE_ALIAS_OTHER_SPE = new TestNodeDetail("10010", KMELIA_ALIAS_OTHER, true);

  private static final List<TestNodeDetail> ALL_NODES = asList(NODE_38_26, NODE_38_62, NODE_83_260, NODE_83_620, NODE_ALIAS_OTHER_SPE);

  private static final PublicationDetail4Test PUB_1_NODE_38_26_SAME_GED_ALIAS = new PublicationDetail4Test("1", NODE_38_26);
  private static final PublicationDetail4Test PUB_2_NODE_38_62 = new PublicationDetail4Test("2", NODE_38_62);
  private static final PublicationDetail4Test PUB_3_NODE_83_260 = new PublicationDetail4Test("3", NODE_83_260);
  private static final PublicationDetail4Test PUB_4_NODE_83_620_OTHER_GED_ALIAS_SPE = new PublicationDetail4Test("4", NODE_83_620);
  private static final PublicationDetail4Test PUB_26_NODE_38_62_HAS_CLONE = new PublicationDetail4Test("26", NODE_38_62, "27");
  private static final PublicationDetail4Test PUB_27_NODE_38_62_IS_CLONE = new PublicationDetail4Test("27", NODE_38_62, "26", true);

  private static final List<PublicationDetail4Test> ALL_PUBLICATIONS = asList(
      PUB_1_NODE_38_26_SAME_GED_ALIAS,
      PUB_2_NODE_38_62,
      PUB_3_NODE_83_260,
      PUB_4_NODE_83_620_OTHER_GED_ALIAS_SPE,
      PUB_26_NODE_38_62_HAS_CLONE,
      PUB_27_NODE_38_62_IS_CLONE);

  private static final List<PublicationDetail4Test> ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE =  asList(
      PUB_1_NODE_38_26_SAME_GED_ALIAS,
      PUB_2_NODE_38_62,
      PUB_3_NODE_83_260,
      PUB_4_NODE_83_620_OTHER_GED_ALIAS_SPE,
      PUB_27_NODE_38_62_IS_CLONE);

  private static final List<PublicationDetail4Test> ALL_PUBLICATIONS_FOR_TEST_ON_INHERITED_RIGHTS_WITH_ONE_CLONE = asList(
      PUB_1_NODE_38_26_SAME_GED_ALIAS,
      PUB_2_NODE_38_62,
      PUB_27_NODE_38_62_IS_CLONE);

  private static final List<PublicationDetail4Test> ALL_PUBLICATIONS_FOR_TEST_ON_SPECIFIC_RIGHTS = asList(
      PUB_3_NODE_83_260,
      PUB_4_NODE_83_620_OTHER_GED_ALIAS_SPE);

  @TestManagedMock
  private PublicationService publicationService;
  @TestManagedMock
  private NodeService nodeService;
  @TestManagedMock
  private OrganizationController organizationController;
  private PublicationAccessControl testInstance;
  private TestContext testContext;
  private User user;

  @BeforeEach
  void setup() {
    user = mock(User.class);
    when(user.getDisplayedName()).thenReturn(USER_ID);
    when(UserProvider.get().getUser(anyString())).thenReturn(user);
    testContext = new TestContext();
    PUB_1_NODE_38_26_SAME_GED_ALIAS.clearAliases().addAliasLocation(NODE_38_62);
    PUB_4_NODE_83_620_OTHER_GED_ALIAS_SPE.clearAliases().addAliasLocation(NODE_ALIAS_OTHER_SPE);
  }

  @Test
  void isUserAuthorizedWithPubPkShouldNotUseCaches() {
    testContext.setup();
    testInstance.isUserAuthorized(USER_ID, ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE.iterator().next().getPK(),
        testContext.accessControlContext);
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    final NodeAccessController.DataManager nodeDataManager = results.getNodeDataManager();
    final PublicationAccessController.DataManager publicationDataManager = results.getPublicationDataManager();
    assertThat(componentDataManager.availableComponentCache, nullValue());
    assertThat(componentDataManager.userProfiles, nullValue());
    assertThat(componentDataManager.componentParameterValueCache, nullValue());
    assertThat(nodeDataManager.nodeDetailCache, nullValue());
    assertThat(nodeDataManager.userProfiles, nullValue());
    assertThat(publicationDataManager.lotOfDataMode, is(false));
    assertThat(publicationDataManager.givenPublicationPks, nullValue());
    assertThat(publicationDataManager.publicationCache, nullValue());
    assertThat(publicationDataManager.locationsByPublicationCache, nullValue());
    verify(organizationController, times(0)).getAvailableComponentsByUser(anyString());
    verify(organizationController, times(0)).getUserProfilesByComponentId(anyString(), anyCollection());
  }

  @Test
  void isUserAuthorizedWithPublicationShouldUseOnlyPublicationCache() {
    testContext.setup();
    final PublicationDetail4Test publication = ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE.iterator().next();
    testInstance.isUserAuthorized(USER_ID, publication, testContext.accessControlContext);
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    final NodeAccessController.DataManager nodeDataManager = results.getNodeDataManager();
    final PublicationAccessController.DataManager publicationDataManager = results.getPublicationDataManager();
    assertThat(componentDataManager.availableComponentCache, nullValue());
    assertThat(componentDataManager.userProfiles, nullValue());
    assertThat(componentDataManager.componentParameterValueCache, nullValue());
    assertThat(nodeDataManager.nodeDetailCache, nullValue());
    assertThat(nodeDataManager.userProfiles, nullValue());
    assertThat(publicationDataManager.lotOfDataMode, is(false));
    assertThat(publicationDataManager.givenPublicationPks, contains(publication.getPK()));
    assertThat(publicationDataManager.publicationCache, hasEntry(publication.getPK(), publication));
    assertThat(publicationDataManager.locationsByPublicationCache, nullValue());
  }

  @Test
  void filterAuthorizedByUserShouldLoadCaches() {
    executeFilterAuthorizedByUserWithPubs(ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE);
    assertFilterAuthorizedByUserShouldLoadCaches(false);
    executeFilterAuthorizedByUserWithPks(toPubPks(ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE));
    assertFilterAuthorizedByUserShouldLoadCaches(true);
    executeFilterAuthorizedByUserAndGetPublicationsWithPks(toPubPks(ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE));
    assertFilterAuthorizedByUserShouldLoadCaches(true);
  }

  private void assertFilterAuthorizedByUserShouldLoadCaches(final boolean fromPks) {
    final PublicationPK[] pksOfTest = ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE
        .stream()
        .map(PublicationDetail::getPK)
        .toArray(PublicationPK[]::new);
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    final NodeAccessController.DataManager nodeDataManager = results.getNodeDataManager();
    final PublicationAccessController.DataManager publicationDataManager = results.getPublicationDataManager();
    assertAvailableComponentCache(componentDataManager, true);
    assertThat(componentDataManager.userProfiles.keySet(), containsInAnyOrder(KMELIA_38, KMELIA_83, KMELIA_ALIAS_OTHER));
    assertThat(componentDataManager.componentParameterValueCache.keySet(), containsInAnyOrder(KMELIA_38, KMELIA_83, KMELIA_ALIAS_OTHER));
    assertThat(nodeDataManager.nodeDetailCache.values(), containsInAnyOrder(NODE_83_260, NODE_83_620, NODE_ALIAS_OTHER_SPE));
    assertThat(nodeDataManager.userProfiles.keySet(), containsInAnyOrder(Pair.of(KMELIA_83, NODE_83_620.getId()),
        Pair.of(KMELIA_ALIAS_OTHER, NODE_ALIAS_OTHER_SPE.getId())));
    assertThat(publicationDataManager.lotOfDataMode, is(true));
    assertThat(publicationDataManager.givenPublicationPks, containsInAnyOrder(pksOfTest));
    final int publicationForTestPlusOneClone = ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE.size() + 1;
    assertThat(publicationDataManager.publicationCache.size(), is(publicationForTestPlusOneClone));
    assertThat(publicationDataManager.locationsByPublicationCache.keySet(), containsInAnyOrder("1", "2", "3", "4", "26"));
    // Publication level
    if (fromPks) {
      // One to load the publications, and an other one to load the master of clones
      verify(publicationService, times(2)).getMinimalDataByIds(anyCollection());
    } else {
      // One to load the master of clones
      verify(publicationService, times(1)).getMinimalDataByIds(anyCollection());
    }
    verify(publicationService, times(1)).getAllLocationsByPublicationIds(anyCollection());
    // Node level
    verify(nodeService, times(2)).getMinimalDataByInstances(anyCollection());
    final ArgumentCaptor<ProfiledObjectIds> nodeIds = ArgumentCaptor.forClass(ProfiledObjectIds.class);
    verify(organizationController, times(2))
        .getUserProfilesByComponentIdAndObjectId(anyString(), anyCollection(), nodeIds.capture());
    final ProfiledObjectIds capturedNodeIds = nodeIds.getValue();
    assertThat(capturedNodeIds, not(hasItem(NodePK.UNDEFINED_NODE_ID)));
    assertThat(capturedNodeIds.getType(), is(ProfiledObjectType.NODE));
  }

  @Test
  void filterAuthorizedByUserOnInheritedRightComponentShouldLoadCaches() {
    executeFilterAuthorizedByUserWithPubs(ALL_PUBLICATIONS_FOR_TEST_ON_INHERITED_RIGHTS_WITH_ONE_CLONE);
    assertFilterAuthorizedByUserOnInheritedRightComponentShouldLoadCaches(false);
    executeFilterAuthorizedByUserWithPks(toPubPks(ALL_PUBLICATIONS_FOR_TEST_ON_INHERITED_RIGHTS_WITH_ONE_CLONE));
    assertFilterAuthorizedByUserOnInheritedRightComponentShouldLoadCaches(true);
    executeFilterAuthorizedByUserAndGetPublicationsWithPks(toPubPks(ALL_PUBLICATIONS_FOR_TEST_ON_INHERITED_RIGHTS_WITH_ONE_CLONE));
    assertFilterAuthorizedByUserOnInheritedRightComponentShouldLoadCaches(true);
  }

  private void assertFilterAuthorizedByUserOnInheritedRightComponentShouldLoadCaches(final boolean fromPks) {
    final PublicationPK[] pksOfTest = ALL_PUBLICATIONS_FOR_TEST_ON_INHERITED_RIGHTS_WITH_ONE_CLONE
        .stream()
        .map(PublicationDetail::getPK)
        .toArray(PublicationPK[]::new);
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    final NodeAccessController.DataManager nodeDataManager = results.getNodeDataManager();
    final PublicationAccessController.DataManager publicationDataManager = results.getPublicationDataManager();
    assertAvailableComponentCache(componentDataManager, false);
    assertThat(componentDataManager.userProfiles.keySet(), containsInAnyOrder(KMELIA_38));
    assertThat(componentDataManager.componentParameterValueCache.keySet(), containsInAnyOrder(KMELIA_38));
    assertThat(nodeDataManager.nodeDetailCache.values(), empty());
    assertThat(nodeDataManager.userProfiles.keySet(), empty());
    assertThat(publicationDataManager.lotOfDataMode, is(true));
    assertThat(publicationDataManager.givenPublicationPks, containsInAnyOrder(pksOfTest));
    final int publicationForTestPlusOneClone = ALL_PUBLICATIONS_FOR_TEST_ON_INHERITED_RIGHTS_WITH_ONE_CLONE.size() + 1;
    assertThat(publicationDataManager.publicationCache.size(), is(publicationForTestPlusOneClone));
    assertThat(publicationDataManager.locationsByPublicationCache.keySet(), containsInAnyOrder("1", "2", "26"));
    // Publication level
    if (fromPks) {
      // One to load the publications, and an other one to load the master of clones
      verify(publicationService, times(2)).getMinimalDataByIds(anyCollection());
    } else {
      // One to load the master of clones
      verify(publicationService, times(1)).getMinimalDataByIds(anyCollection());
    }
    verify(publicationService, times(1)).getAllLocationsByPublicationIds(anyCollection());
    // Node level
    verify(nodeService, times(0)).getMinimalDataByInstances(anyCollection());
    verify(organizationController, times(0))
        .getUserProfilesByComponentIdAndObjectId(anyString(), anyCollection(), any(ProfiledObjectIds.class));
  }

  @Test
  void filterAuthorizedByUserOnSpecificRightOnTopicComponentShouldLoadCaches() {
    executeFilterAuthorizedByUserWithPubs(ALL_PUBLICATIONS_FOR_TEST_ON_SPECIFIC_RIGHTS);
    assertFilterAuthorizedByUserOnSpecificRightOnTopicComponentShouldLoadCaches(false);
    executeFilterAuthorizedByUserWithPks(toPubPks(ALL_PUBLICATIONS_FOR_TEST_ON_SPECIFIC_RIGHTS));
    assertFilterAuthorizedByUserOnSpecificRightOnTopicComponentShouldLoadCaches(true);
    executeFilterAuthorizedByUserAndGetPublicationsWithPks(toPubPks(ALL_PUBLICATIONS_FOR_TEST_ON_SPECIFIC_RIGHTS));
    assertFilterAuthorizedByUserOnSpecificRightOnTopicComponentShouldLoadCaches(true);
  }

  private void assertFilterAuthorizedByUserOnSpecificRightOnTopicComponentShouldLoadCaches(final boolean fromPks) {
    final PublicationPK[] pksOfTest = ALL_PUBLICATIONS_FOR_TEST_ON_SPECIFIC_RIGHTS
        .stream()
        .map(PublicationDetail::getPK)
        .toArray(PublicationPK[]::new);
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    final NodeAccessController.DataManager nodeDataManager = results.getNodeDataManager();
    final PublicationAccessController.DataManager publicationDataManager = results.getPublicationDataManager();
    assertAvailableComponentCache(componentDataManager, true);
    assertThat(componentDataManager.userProfiles.keySet(), containsInAnyOrder(KMELIA_83, KMELIA_ALIAS_OTHER));
    assertThat(componentDataManager.componentParameterValueCache.keySet(), containsInAnyOrder(KMELIA_83, KMELIA_ALIAS_OTHER));
    assertThat(nodeDataManager.nodeDetailCache.values(), containsInAnyOrder(NODE_83_260, NODE_83_620, NODE_ALIAS_OTHER_SPE));
    assertThat(nodeDataManager.userProfiles.keySet(), containsInAnyOrder(Pair.of(KMELIA_83, NODE_83_620.getId()),
        Pair.of(KMELIA_ALIAS_OTHER, NODE_ALIAS_OTHER_SPE.getId())));
    assertThat(publicationDataManager.lotOfDataMode, is(true));
    assertThat(publicationDataManager.givenPublicationPks, containsInAnyOrder(pksOfTest));
    final int publicationForTestWithoutAnyClone = ALL_PUBLICATIONS_FOR_TEST_ON_SPECIFIC_RIGHTS.size();
    assertThat(publicationDataManager.publicationCache.size(), is(publicationForTestWithoutAnyClone));
    assertThat(publicationDataManager.locationsByPublicationCache.keySet(), containsInAnyOrder("3", "4"));
    // Publication level
    if (fromPks) {
      // One to load the master of clones
      verify(publicationService, times(1)).getMinimalDataByIds(anyCollection());
    } else {
      // As there is no clone, there is no data request
      verify(publicationService, times(0)).getMinimalDataByIds(anyCollection());
    }
    verify(publicationService, times(1)).getAllLocationsByPublicationIds(anyCollection());
    // Node level
    verify(nodeService, times(2)).getMinimalDataByInstances(anyCollection());
    final ArgumentCaptor<ProfiledObjectIds> nodeIds = ArgumentCaptor.forClass(ProfiledObjectIds.class);
    verify(organizationController, times(2))
        .getUserProfilesByComponentIdAndObjectId(anyString(), anyCollection(), nodeIds.capture());
    final ProfiledObjectIds capturedNodeIds = nodeIds.getValue();
    assertThat(capturedNodeIds, not(hasItem(NodePK.UNDEFINED_NODE_ID)));
    assertThat(capturedNodeIds.getType(), is(ProfiledObjectType.NODE));
  }

  @Test
  void filterAuthorizedByUserWithSearchContextShouldLoadCaches() {
    executeFilterAuthorizedByUserWithPubs(ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE, SEARCH);
    assertFilterAuthorizedByUserWithSearchContextShouldLoadCaches();
    executeFilterAuthorizedByUserWithPks(toPubPks(ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE), SEARCH);
    assertFilterAuthorizedByUserWithSearchContextShouldLoadCaches();
    executeFilterAuthorizedByUserAndGetPublicationsWithPks(toPubPks(ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE),
        SEARCH);
    assertFilterAuthorizedByUserWithSearchContextShouldLoadCaches();
  }

  private void assertFilterAuthorizedByUserWithSearchContextShouldLoadCaches() {
    final PublicationPK[] pksOfTest = ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE
        .stream()
        .map(PublicationDetail::getPK)
        .toArray(PublicationPK[]::new);
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    final NodeAccessController.DataManager nodeDataManager = results.getNodeDataManager();
    final PublicationAccessController.DataManager publicationDataManager = results.getPublicationDataManager();
    assertAvailableComponentCache(componentDataManager, true);
    assertThat(componentDataManager.userProfiles.keySet(), containsInAnyOrder(KMELIA_38, KMELIA_83, KMELIA_ALIAS_OTHER));
    assertThat(componentDataManager.componentParameterValueCache.keySet(), containsInAnyOrder(KMELIA_38, KMELIA_83, KMELIA_ALIAS_OTHER));
    assertThat(nodeDataManager.nodeDetailCache.values(), containsInAnyOrder(NODE_83_260, NODE_83_620, NODE_ALIAS_OTHER_SPE));
    assertThat(nodeDataManager.userProfiles.keySet(), containsInAnyOrder(Pair.of(KMELIA_83, NODE_83_620.getId()),
        Pair.of(KMELIA_ALIAS_OTHER, NODE_ALIAS_OTHER_SPE.getId())));
    assertThat(publicationDataManager.lotOfDataMode, is(true));
    assertThat(publicationDataManager.givenPublicationPks, containsInAnyOrder(pksOfTest));
    assertThat(publicationDataManager.publicationCache.size(), is(0));
    assertThat(publicationDataManager.locationsByPublicationCache.keySet(), containsInAnyOrder("1", "2", "3", "4", "27"));
    // Publication level
    verify(publicationService, times(0)).getMinimalDataByIds(anyCollection());
    verify(publicationService, times(1)).getAllLocationsByPublicationIds(anyCollection());
    // Node level
    verify(nodeService, times(2)).getMinimalDataByInstances(anyCollection());
    final ArgumentCaptor<ProfiledObjectIds> nodeIds = ArgumentCaptor.forClass(ProfiledObjectIds.class);
    verify(organizationController, times(2))
        .getUserProfilesByComponentIdAndObjectId(anyString(), anyCollection(), nodeIds.capture());
    final ProfiledObjectIds capturedNodeIds = nodeIds.getValue();
    assertThat(capturedNodeIds, not(hasItem(NodePK.UNDEFINED_NODE_ID)));
    assertThat(capturedNodeIds.getType(), is(ProfiledObjectType.NODE));
  }

  @Test
  void filterAuthorizedByUserWithModifyContextShouldLoadCaches() {
    executeFilterAuthorizedByUserWithPubs(ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE, MODIFICATION);
    assertFilterAuthorizedByUserWithModifyContextShouldLoadCaches(false);
    executeFilterAuthorizedByUserWithPks(toPubPks(ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE),
        MODIFICATION);
    assertFilterAuthorizedByUserWithModifyContextShouldLoadCaches(true);
    executeFilterAuthorizedByUserAndGetPublicationsWithPks(toPubPks(ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE),
        MODIFICATION);
    assertFilterAuthorizedByUserWithModifyContextShouldLoadCaches(true);
  }

  private void assertFilterAuthorizedByUserWithModifyContextShouldLoadCaches(final boolean fromPks) {
    final PublicationPK[] pksOfTest = ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE
        .stream()
        .map(PublicationDetail::getPK)
        .toArray(PublicationPK[]::new);
    final TestVerifyResults results = testContext.results();
    final ComponentAccessController.DataManager componentDataManager = results.getComponentDataManager();
    final NodeAccessController.DataManager nodeDataManager = results.getNodeDataManager();
    final PublicationAccessController.DataManager publicationDataManager = results.getPublicationDataManager();
    assertAvailableComponentCache(componentDataManager, true);
    assertThat(componentDataManager.userProfiles.keySet(), containsInAnyOrder(KMELIA_38, KMELIA_83, KMELIA_ALIAS_OTHER));
    assertThat(componentDataManager.componentParameterValueCache.keySet(), containsInAnyOrder(KMELIA_38, KMELIA_83, KMELIA_ALIAS_OTHER));
    assertThat(nodeDataManager.nodeDetailCache.values(), containsInAnyOrder(NODE_83_260, NODE_83_620, NODE_ALIAS_OTHER_SPE));
    assertThat(nodeDataManager.userProfiles.keySet(), containsInAnyOrder(Pair.of(KMELIA_83, NODE_83_620.getId()),
        Pair.of(KMELIA_ALIAS_OTHER, NODE_ALIAS_OTHER_SPE.getId())));
    assertThat(publicationDataManager.lotOfDataMode, is(true));
    assertThat(publicationDataManager.givenPublicationPks, containsInAnyOrder(pksOfTest));
    final int publicationForTestPlusOneClone = ALL_PUBLICATIONS_FOR_TEST_WITH_ONE_CLONE.size() + 1;
    assertThat(publicationDataManager.publicationCache.size(), is(publicationForTestPlusOneClone));
    assertThat(publicationDataManager.locationsByPublicationCache.keySet(), containsInAnyOrder("1", "2", "3", "4", "26"));
    // Publication level
    if (fromPks) {
      // One to load the publications, and an other one to load the master of clones
      verify(publicationService, times(2)).getMinimalDataByIds(anyCollection());
    } else {
      // One to load the master of clones
      verify(publicationService, times(1)).getMinimalDataByIds(anyCollection());
    }
    verify(publicationService, times(1)).getAllLocationsByPublicationIds(anyCollection());
    // Node level
    verify(nodeService, times(2)).getMinimalDataByInstances(anyCollection());
    final ArgumentCaptor<ProfiledObjectIds> nodeIds = ArgumentCaptor.forClass(ProfiledObjectIds.class);
    verify(organizationController, times(2))
        .getUserProfilesByComponentIdAndObjectId(anyString(), anyCollection(), nodeIds.capture());
    final ProfiledObjectIds capturedNodeIds = nodeIds.getValue();
    assertThat(capturedNodeIds, not(hasItem(NodePK.UNDEFINED_NODE_ID)));
    assertThat(capturedNodeIds.getType(), is(ProfiledObjectType.NODE));
  }

  private void assertAvailableComponentCache(
      final ComponentAccessController.DataManager componentDataManager,
      final boolean cacheCompletion) {
    assertThat(componentDataManager.availableComponentCache, containsInAnyOrder(KMELIA_38, KMELIA_83));
    verify(organizationController, times(1)).getAvailableComponentsByUser(anyString());
    verify(organizationController, times(cacheCompletion ? 2 : 1)).getUserProfilesByComponentId(anyString(), anyCollection());
  }

  private static List<PublicationPK> toPubPks(final List<PublicationDetail4Test> publications) {
    return publications.stream().map(PublicationDetail::getPK).collect(Collectors.toList());
  }

  /**
   * Centralization.
   */
  @SuppressWarnings("unchecked")
  private void executeFilterAuthorizedByUserWithPubs(
      final List<? extends PublicationDetail> listToFilter,
      final AccessControlOperation... operations) {
    testContext.setup();
    Stream.of(operations).forEach(o -> testContext.accessControlContext.onOperationsOf(o));
    testInstance.filterAuthorizedByUser(USER_ID, (Collection<PublicationDetail>) listToFilter, testContext.accessControlContext);
  }

  /**
   * Centralization.
   */
  private void executeFilterAuthorizedByUserWithPks(final List<PublicationPK> listToFilter,
      final AccessControlOperation... operations) {
    testContext.setup();
    Stream.of(operations).forEach(o -> testContext.accessControlContext.onOperationsOf(o));
    testInstance.filterAuthorizedByUser(listToFilter, USER_ID, testContext.accessControlContext);
  }

  /**
   * Centralization.
   */
  private void executeFilterAuthorizedByUserAndGetPublicationsWithPks(final List<PublicationPK> listToFilter,
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
      Mockito.reset(user, organizationController, nodeService, publicationService);
      componentAccessController = mock(ComponentAccessControl.class);
      final NodeAccessController nodeAccessController = new NodeAccessController(componentAccessController);
      testInstance = new PublicationAccessController4Test(componentAccessController, nodeAccessController);
      accessControlContext = AccessControlContext.init();
      testVerifyResults = new TestVerifyResults(this);
    }

    @SuppressWarnings("unchecked")
    public void setup() {
      clear();
      when(user.getId()).thenReturn(USER_ID);
      when(user.isAnonymous()).thenReturn(false);
      when(componentAccessController
          .getUserRoles(anyString(), anyString(), any(AccessControlContext.class)))
          .then(new Returns(singleton(SilverpeasRole.USER)));
      when(componentAccessController.isUserAuthorized(anySet())).then(new Returns(true));
      when(organizationController.getComponentInstance(anyString()))
          .thenAnswer(a -> {
            final String i = a.getArgument(0);
            final SilverpeasComponentInstance instance = mock(SilverpeasComponentInstance.class);
            when(instance.isTopicTracker()).then(new Returns(i.startsWith("kmelia") || i.startsWith("kmax") || i.startsWith("toolbox")));
            return Optional.of(instance);
          });
      when(organizationController.getParameterValuesByComponentIdThenByParamName(anyCollection(), eq(
          asList("rightsOnTopics", "usePublicationSharing", "useFileSharing",
              "useFolderSharing", "coWriting", "publicFiles"))))
          .thenAnswer(a -> {
            final Map<String, Map<String, String>> result = new HashMap<>(1);
            final Collection<String> instanceIds = a.getArgument(0);
            instanceIds.forEach(i ->  {
              final Map<String, String> paramValues = new HashMap<>(3);
              paramValues.put("rightsOnTopics", KMELIA_83.equals(i) || KMELIA_ALIAS_OTHER.equals(i) ? "1" : "0");
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
      when(organizationController
          .getUserProfilesByComponentIdAndObjectId(anyString(), anyCollection(), any(ProfiledObjectIds.class))).thenAnswer(a -> {
        final Collection<String> instanceIds = a.getArgument(1);
        final ProfiledObjectIds profiledObjectIds = a.getArgument(2);
        final Map<Pair<String, String>, Set<String>> result = new HashMap<>();
        ALL_NODES.stream()
            .filter(n -> instanceIds.contains(n.getNodePK().getInstanceId()))
            .filter(n -> profiledObjectIds.contains(n.getId()))
            .map(NodeDetail::getNodePK)
            .forEach(p -> result.put(Pair.of(p.getInstanceId(), p.getId()),
                CollectionUtil.asSet(SilverpeasRole.USER.getName())));
        return result;
      });
      when(organizationController.getAvailableComponentsByUser(anyString()))
          .thenAnswer(a -> asList(KMELIA_38, KMELIA_83));
      when(nodeService.getMinimalDataByInstances(anyCollection())).then(
          a -> ((Collection<String>) a.getArgument(0)).stream()
              .flatMap(i -> ALL_NODES.stream().filter(l -> l.getNodePK().getInstanceId().equals(i)))
              .collect(Collectors.toList()));
      when(publicationService.getMinimalDataByIds(anyCollection())).then(
          a -> ((Collection<PublicationPK>) a.getArgument(0)).stream()
              .flatMap(p -> ALL_PUBLICATIONS.stream().filter(pu -> pu.getPK().equals(p)))
              .map(pu -> (PublicationDetail) pu)
              .collect(Collectors.toList()));
      when(publicationService.getAllLocationsByPublicationIds(anyCollection())).then(a -> {
          final Map<String, Set<Location>> result = new HashMap<>();
          ((Collection<String>) a.getArgument(0)).stream()
              .flatMap(p -> ALL_PUBLICATIONS.stream().filter(pu -> pu.getPK().getId().equals(p)))
              .forEach(pu -> result.put(pu.getId(), new HashSet<>(pu.getLocations())));
          return result;
      });
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

    PublicationAccessController.DataManager getPublicationDataManager() {
      return PublicationAccessController.getDataManager(testContext.accessControlContext);
    }
  }

  private static class PublicationAccessController4Test extends PublicationAccessController {

    PublicationAccessController4Test(final ComponentAccessControl componentAccessController,
        final NodeAccessControl nodeAccessController) {
      super(componentAccessController, nodeAccessController);
    }

    @Override
    ComponentInstancePublicationAccessControlExtension getComponentExtension(
        final String instanceId) {
      return new DefaultInstancePublicationAccessControlExtension4Test();
    }
  }

  private static class DefaultInstancePublicationAccessControlExtension4Test
      extends DefaultInstancePublicationAccessControlExtension {

    DefaultInstancePublicationAccessControlExtension4Test() {
    }

    @Override
    protected boolean isDraftVisibleWithCoWriting() {
      return false;
    }
  }
}

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

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.RemovedSpaceAndComponentInstanceChecker;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.kernel.test.UnitTest;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.function.Predicate.isEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.*;
import static org.silverpeas.core.node.model.NodeDetail.NO_RIGHTS_DEPENDENCY;
import static org.silverpeas.core.security.authorization.AccessControlOperation.*;

/**
 * @author ehugonnet
 */
@UnitTest
@EnableSilverTestEnv(context = JEETestContext.class)
class NodeAccessControllerTest {

  private final static String A_NODE_ID = "26";
  private final static String userId = "5";
  private final static String componentId = "kmelia18";

  @TestManagedMock
  private OrganizationController organizationController;
  @TestManagedMock
  private ComponentAccessControl componentAccessController;
  @TestManagedMock
  private NodeService nodeService;
  @TestManagedMock
  private RemovedSpaceAndComponentInstanceChecker checker;

  @BeforeEach
  void setup() {
    when(checker.resetWithCacheSizeOf(any(Integer.class))).thenReturn(checker);
    User user = mock(User.class);
    when(UserProvider.get().getUser(userId)).thenReturn(user);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedWhenNodeHaveNoSpecificRightsOnNode() {
    withUserHavingComponentRole(USER)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedWhenNodeHaveNoSpecificRightsOnNodeByGivingDirectlyNodeDetail() {
    withUserHavingComponentRole(USER)
        .assertAccessIsAuthorizedByGivingNodeInstanceDirectly(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleWhenNodeHaveNoSpecificRightsOnNode() {
    withUserHavingComponentRole(USER)
        .locatedOnNode(NodePK.ROOT_NODE_ID)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleWhenNodeHaveNoSpecificRightsOnNodeByGivingDirectlyNodeDetail() {
    withUserHavingComponentRole(USER)
        .locatedOnNode(NodePK.ROOT_NODE_ID)
        .assertAccessIsAuthorizedByGivingNodeInstanceDirectly(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedOnRootNodeWithWriterRoleWhenNodeHaveNoSpecificRightsOnNode() {
    withUserHavingComponentRole(WRITER)
        .locatedOnNode(NodePK.ROOT_NODE_ID)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsNotAuthorizedOnTrashWithUserRoleWhenNodeHaveNoSpecificRightsOnNode() {
    withUserHavingComponentRole(USER)
        .locatedOnNode(NodePK.BIN_NODE_ID)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedOnTrashWithWriterRoleWhenNodeHaveNoSpecificRightsOnNode() {
    withUserHavingComponentRole(WRITER)
        .locatedOnNode(NodePK.BIN_NODE_ID)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsNotAuthorizedOnSharingContextIfNotEnabled() {
    withUserHavingComponentRole(USER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsNotAuthorizedOnSharingContextIfEnabledForAdmin() {
    withUserHavingComponentRole(USER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(ADMIN)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsNotAuthorizedOnSharingContextIfEnabledForContributors() {
    withUserHavingComponentRole(USER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(WRITER)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedOnSharingContextIfEnabledForAll() {
    withUserHavingComponentRole(USER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(USER)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedWhenNodeHaveNoSpecificRightsOnComponent() {
    withUserHavingComponentRole(null)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedWithRightOnTopicEnableButNoRightsDependOn() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedWithRightOnTopicEnableButNoRightsDependOnByGivingDirectlyNodeDetail() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .assertAccessIsAuthorizedByGivingNodeInstanceDirectly(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedWithRightsDependOn() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, USER)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedWithRightsDependOnByGivingDirectlyNodeDetail() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, USER)
        .assertAccessIsAuthorizedByGivingNodeInstanceDirectly(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsNotAuthorizedWithRightsDependOn() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, null)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndAdminOnNode() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, ADMIN)
        .assertAccessIsAuthorized(true);
  }

  /*
   * ABOUT MODIFICATION
   */

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndAdminOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, ADMIN)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndUserOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, USER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndWriterOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, WRITER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndPublisherOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, PUBLISHER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndAdminOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, ADMIN)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndUserOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, USER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndWriterOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, WRITER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndPublisherOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, PUBLISHER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndAdminOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, ADMIN)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndUserOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, USER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndWriterOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, WRITER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndPublisherOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, PUBLISHER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndAdminOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, ADMIN)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndUserOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, USER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndWriterOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, WRITER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndPublisherOnNodeAboutModification() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, PUBLISHER)
        .aboutOperation(MODIFICATION)
        .assertAccessIsAuthorized(true);
  }

  /*
   * ABOUT SHARING
   */

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndAdminOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, ADMIN)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, ADMIN)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, ADMIN)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(USER)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndUserOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, USER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, USER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, USER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(USER)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndWriterOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, WRITER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, WRITER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, WRITER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(USER)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndPublisherOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, PUBLISHER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, PUBLISHER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, PUBLISHER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(USER)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndAdminOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, ADMIN)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER, USER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, ADMIN)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndUserOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, USER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER, USER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, USER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndWriterOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, WRITER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER, USER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, WRITER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndPublisherOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, PUBLISHER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER, USER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, PUBLISHER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndAdminOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, ADMIN)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, ADMIN)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, ADMIN)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(USER)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndUserOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, USER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, USER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, USER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(USER)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndWriterOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, WRITER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, WRITER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, WRITER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(USER)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndPublisherOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, PUBLISHER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, PUBLISHER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, PUBLISHER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(USER)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndAdminOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, ADMIN)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER, USER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(A_NODE_ID, ADMIN)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(true);
    }
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndUserOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, USER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(ADMIN, WRITER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(A_NODE_ID, USER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, USER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(USER)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndWriterOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, WRITER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, WRITER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(ADMIN)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(WRITER, USER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(A_NODE_ID, WRITER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(true);
    }
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndPublisherOnNodeAboutSharing() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, PUBLISHER)
        .aboutOperation(SHARING)
        .assertAccessIsAuthorized(false);
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, PUBLISHER)
        .aboutOperation(SHARING)
        .enableNodeSharingRole(ADMIN)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(WRITER, USER)) {
      withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(A_NODE_ID, PUBLISHER)
          .aboutOperation(SHARING)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(true);
    }
  }

  /*
   * ABOUT DOWNLOAD
   */

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndAdminOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, ADMIN)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndUserOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, USER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndWriterOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, WRITER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndPublisherOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, PUBLISHER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndAdminOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, ADMIN)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndUserOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, USER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndWriterOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, WRITER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndPublisherOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, PUBLISHER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(false);
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndAdminOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, ADMIN)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndUserOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, USER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndWriterOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, WRITER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndPublisherOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, PUBLISHER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndAdminOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, ADMIN)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndUserOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, USER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndWriterOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, WRITER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  @Test
  void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndPublisherOnNodeAboutDownload() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, PUBLISHER)
        .aboutOperation(DOWNLOAD)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsNotAuthorizedOnTrashWithUserRoleOnComponentAndAdminOnNode() {
    withUserHavingComponentRole(USER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, ADMIN)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  void userIsAuthorizedOnTrashWithWriterRoleOnComponentAndAdminOnNode() {
    withUserHavingComponentRole(WRITER)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, ADMIN)
        .assertAccessIsAuthorized(true);
  }

  /*
  TEST TOOLS
   */

  private TestCaseBuilder withUserHavingComponentRole(final SilverpeasRole role) {
    return new TestCaseBuilder(this, role);
  }

  /**
   * Building a readable test case.
   */
  static class TestCaseBuilder {
    private final OrganizationController organizationController;
    private final ComponentAccessControl componentAccessController;
    private final NodeService nodeService;
    private final NodeAccessControl instance;
    private final AccessControlContext context;
    private final SilverpeasRole componentRole;
    private final Set<AccessControlOperation> operations = new HashSet<>();
    private SilverpeasRole nodeSharingRole;
    private String nodeId;
    private SilverpeasRole specificNodeRole;
    private boolean rightsOnTopicEnabled = false;
    private boolean givingNodeInstanceDirectly = false;

    TestCaseBuilder(final NodeAccessControllerTest testInstance, final SilverpeasRole componentRole) {
      this.organizationController = testInstance.organizationController;
      this.componentAccessController = testInstance.componentAccessController;
      this.nodeService = testInstance.nodeService;
      this.instance = new NodeAccessController(componentAccessController);
      this.context = AccessControlContext.init();
      this.componentRole = componentRole;
      initializeMocks();
    }

    private void initializeMocks() {
      CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
      Mockito.reset(componentAccessController, organizationController, nodeService);
      when(organizationController.getComponentParameterValue(anyString(), eq("rightsOnTopics")))
          .then(new Returns("false"));
      when(organizationController.getComponentInstance(anyString()))
          .thenAnswer(a -> {
            final String i = a.getArgument(0);
            final SilverpeasComponentInstance instance = mock(SilverpeasComponentInstance.class);
            when(instance.isTopicTracker()).then(new Returns(i.startsWith("kmelia") || i.startsWith("kmax") || i.startsWith("toolbox")));
            return Optional.of(instance);
          });
    }

    TestCaseBuilder andSpecificRightsEnabledOnTopic() {
      this.rightsOnTopicEnabled = true;
      return this;
    }

    TestCaseBuilder locatedOnNode(final String nodeId) {
      this.nodeId = nodeId;
      return this;
    }

    TestCaseBuilder locatedOnNodeWithSpecificRole(final String nodeId, final SilverpeasRole role) {
      this.nodeId = nodeId;
      this.specificNodeRole = role;
      return this;
    }

    TestCaseBuilder aboutOperation(final AccessControlOperation operation) {
      this.operations.add(operation);
      return this;
    }

    TestCaseBuilder enableNodeSharingRole(SilverpeasRole role) {
      this.nodeSharingRole = role;
      return this;
    }

    void assertAccessIsAuthorizedByGivingNodeInstanceDirectly(final boolean authorized) {
      givingNodeInstanceDirectly = true;
      assertAccessIsAuthorized(authorized);
    }

    void assertAccessIsAuthorized(final boolean authorized) {
      nodeId = nodeId != null ? nodeId : "10";
      if (componentRole != null) {
        if (AccessControlOperation.isPersistActionFrom(operations)) {
          setComponentInstanceUserRoleAboutOperation(componentRole);
        } else {
          setComponentInstanceUserRole(componentRole);
        }
      }
      if (rightsOnTopicEnabled) {
        setComponentRightOnTopicEnabled();
      }
      NodePK nodePk = new NodePK(nodeId, componentId);
      NodeDetail node = new NodeDetail();
      node.setNodePK(nodePk);
      node.setRightsDependsOn(nodeId.equals("10") ? NO_RIGHTS_DEPENDENCY : "5");
      when(organizationController.getComponentParameterValue(anyString(), eq("useFolderSharing")))
          .thenAnswer((Answer<String>) i -> {
            if (nodeSharingRole != null) {
              if (nodeSharingRole.equals(SilverpeasRole.ADMIN)) {
                return "1";
              } else if (nodeSharingRole.equals(SilverpeasRole.WRITER)) {
                return "2";
              } else {
                return "3";
              }
            }
            return null;
          });
      if (specificNodeRole != null) {
        when(organizationController.getUserProfiles(userId, componentId,
            ProfiledObjectId.fromNode("5"))).thenReturn(new String[]{specificNodeRole.getName()});
      }
      when(nodeService.getHeader(nodePk, false)).thenReturn(node);
      final boolean result;
      if (givingNodeInstanceDirectly) {
        result = executeIsUserAuthorized(node,
            c -> c.onOperationsOf(operations.toArray(AccessControlOperation[]::new)));
      } else {
        result = executeIsUserAuthorized(nodePk,
            c -> c.onOperationsOf(operations.toArray(AccessControlOperation[]::new)));
      }
      assertThat(result, Matchers.is(authorized));
      final boolean isNodeDataLoad = rightsOnTopicEnabled && !nodeId.equals(NodePK.ROOT_NODE_ID) &&
          !nodeId.equals(NodePK.BIN_NODE_ID) && !nodeId.equals(NodePK.UNCLASSED_NODE_ID);
      final int nbCalls =
          isNodeDataLoad && !node.getRightsDependsOn().equals(NO_RIGHTS_DEPENDENCY) ? 1 : 0;
      verify(organizationController, times(nbCalls))
          .getUserProfiles(anyString(), anyString(), any(ProfiledObjectId.class));
      verify(nodeService, times(!givingNodeInstanceDirectly && isNodeDataLoad ? 1 : 0)).getHeader(any(NodePK.class), anyBoolean());
    }

    private void setComponentInstanceUserRoleAboutOperation(SilverpeasRole... roles) {
      setUserHasReadAccessAtLeastIfNotAlreadyDone(roles.length > 0);
      setComponentInstanceUserRole(Stream.of(roles)
          .filter(isEqual(USER).negate())
          .toArray(SilverpeasRole[]::new));
    }

    private void setComponentInstanceUserRole(SilverpeasRole... roles) {
      final Set<SilverpeasRole> roleSet = CollectionUtil.asSet(roles);
      setUserHasReadAccessAtLeastIfNotAlreadyDone(!roleSet.isEmpty());
      when(componentAccessController
          .getUserRoles(anyString(), anyString(), any(AccessControlContext.class)))
          .then(new Returns(roleSet));
      when(componentAccessController
          .isUserAuthorized(anySet()))
          .then(new Returns(!roleSet.isEmpty()));
    }

    private void setUserHasReadAccessAtLeastIfNotAlreadyDone(final boolean hasAccess) {
      if (context.get("setUserHasReadAccessAtLeastCalled", Object.class) == null) {
        ComponentAccessController.getDataManager(context)
            .setUserHasReadAccessAtLeast(componentId, hasAccess);
        context.put("setUserHasReadAccessAtLeastCalled", true);
      }
    }

    private void setComponentRightOnTopicEnabled() {
      when(organizationController.getComponentParameterValue(anyString(), eq("rightsOnTopics")))
          .then(new Returns("true"));
    }

    private boolean executeIsUserAuthorized(final NodePK nodPk,
        final Consumer<AccessControlContext> configurator) {
      configurator.accept(context);
      setUserHasReadAccessAtLeastIfNotAlreadyDone(
          !componentAccessController.getUserRoles(userId, componentId, context).isEmpty());
      return instance.isUserAuthorized(userId, nodPk, context);
    }

    private boolean executeIsUserAuthorized(final NodeDetail nodeDetail,
        final Consumer<AccessControlContext> configurator) {
      configurator.accept(context);
      setUserHasReadAccessAtLeastIfNotAlreadyDone(
          !componentAccessController.getUserRoles(userId, componentId, context).isEmpty());
      return instance.isUserAuthorized(userId, nodeDetail, context);
    }
  }
}

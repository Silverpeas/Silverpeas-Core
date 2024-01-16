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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.test.rule.LibCoreCommonAPI4Test;
import org.silverpeas.core.util.CollectionUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.function.Predicate.isEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.*;
import static org.silverpeas.core.security.authorization.AccessControlOperation.*;

/**
 * @author ehugonnet
 */
@UnitTest
public class TestNodeAccessController {

  private final static String NO_RIGHTS_DEPENDENCY = "-1";
  private final static String A_NODE_ID = "26";
  private final static String userId = "5";
  private final static String componentId = "kmelia18";

  private OrganizationController organizationController;
  private ComponentAccessControl componentAccessController;
  private NodeService nodeService;

  @Rule
  public LibCoreCommonAPI4Test commonAPI4Test = new LibCoreCommonAPI4Test();

  @Before
  public void setup() {
    User user = mock(User.class);
    when(UserProvider.get().getUser(userId)).thenReturn(user);
    organizationController = mock(OrganizationController.class);
    commonAPI4Test.injectIntoMockedBeanContainer(organizationController);
    componentAccessController = mock(ComponentAccessControl.class);
    commonAPI4Test.injectIntoMockedBeanContainer(componentAccessController);
    nodeService = mock(NodeService.class);
    commonAPI4Test.injectIntoMockedBeanContainer(nodeService);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWhenNodeHaveNoSpecificRightsOnNode() {
    withUserHavingComponentRole(user)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWhenNodeHaveNoSpecificRightsOnNodeByGivingDirectlyNodeDetail() {
    withUserHavingComponentRole(user)
        .assertAccessIsAuthorizedByGivingNodeInstanceDirectly(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleWhenNodeHaveNoSpecificRightsOnNode() {
    withUserHavingComponentRole(user)
        .locatedOnNode(NodePK.ROOT_NODE_ID)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleWhenNodeHaveNoSpecificRightsOnNodeByGivingDirectlyNodeDetail() {
    withUserHavingComponentRole(user)
        .locatedOnNode(NodePK.ROOT_NODE_ID)
        .assertAccessIsAuthorizedByGivingNodeInstanceDirectly(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnRootNodeWithWriterRoleWhenNodeHaveNoSpecificRightsOnNode() {
    withUserHavingComponentRole(writer)
        .locatedOnNode(NodePK.ROOT_NODE_ID)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedOnTrashWithUserRoleWhenNodeHaveNoSpecificRightsOnNode() {
    withUserHavingComponentRole(user)
        .locatedOnNode(NodePK.BIN_NODE_ID)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnTrashWithWriterRoleWhenNodeHaveNoSpecificRightsOnNode() {
    withUserHavingComponentRole(writer)
        .locatedOnNode(NodePK.BIN_NODE_ID)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedOnSharingContextIfNotEnabled() {
    withUserHavingComponentRole(user)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedOnSharingContextIfEnabledForAdmin() {
    withUserHavingComponentRole(user)
        .aboutOperation(sharing)
        .enableNodeSharingRole(admin)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedOnSharingContextIfEnabledForContributors() {
    withUserHavingComponentRole(user)
        .aboutOperation(sharing)
        .enableNodeSharingRole(writer)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnSharingContextIfEnabledForAll() {
    withUserHavingComponentRole(user)
        .aboutOperation(sharing)
        .enableNodeSharingRole(user)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWhenNodeHaveNoSpecificRightsOnComponent() {
    withUserHavingComponentRole(null)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWithRightOnTopicEnableButNoRightsDependOn() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWithRightOnTopicEnableButNoRightsDependOnByGivingDirectlyNodeDetail() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .assertAccessIsAuthorizedByGivingNodeInstanceDirectly(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWithRightsDependOn() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, user)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWithRightsDependOnByGivingDirectlyNodeDetail() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, user)
        .assertAccessIsAuthorizedByGivingNodeInstanceDirectly(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedWithRightsDependOn() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, null)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndAdminOnNode() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, admin)
        .assertAccessIsAuthorized(true);
  }

  /*
   * ABOUT MODIFICATION
   */

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndAdminOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, admin)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndUserOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, user)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndWriterOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, writer)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndPublisherOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, publisher)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndAdminOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, admin)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndUserOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, user)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndWriterOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, writer)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndPublisherOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, publisher)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndAdminOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, admin)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndUserOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, user)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndWriterOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, writer)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndPublisherOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, publisher)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndAdminOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, admin)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndUserOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, user)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndWriterOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, writer)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndPublisherOnNodeAboutModification() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, publisher)
        .aboutOperation(modification)
        .assertAccessIsAuthorized(true);
  }

  /*
   * ABOUT SHARING
   */

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndAdminOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, admin)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, admin)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, admin)
        .aboutOperation(sharing)
        .enableNodeSharingRole(user)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndUserOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, user)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, user)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, user)
        .aboutOperation(sharing)
        .enableNodeSharingRole(user)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndWriterOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, writer)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, writer)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, writer)
        .aboutOperation(sharing)
        .enableNodeSharingRole(user)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndPublisherOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, publisher)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, publisher)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, publisher)
        .aboutOperation(sharing)
        .enableNodeSharingRole(user)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndAdminOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, admin)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer, user)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, admin)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndUserOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, user)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer, user)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, user)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndWriterOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, writer)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer, user)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, writer)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndPublisherOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, publisher)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer, user)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, publisher)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndAdminOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, admin)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, admin)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, admin)
        .aboutOperation(sharing)
        .enableNodeSharingRole(user)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndUserOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, user)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, user)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, user)
        .aboutOperation(sharing)
        .enableNodeSharingRole(user)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndWriterOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, writer)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, writer)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, writer)
        .aboutOperation(sharing)
        .enableNodeSharingRole(user)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndPublisherOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, publisher)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, publisher)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, publisher)
        .aboutOperation(sharing)
        .enableNodeSharingRole(user)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndAdminOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, admin)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer, user)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(A_NODE_ID, admin)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(true);
    }
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndUserOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, user)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(admin, writer)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(A_NODE_ID, user)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(false);
    }
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, user)
        .aboutOperation(sharing)
        .enableNodeSharingRole(user)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndWriterOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, writer)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, writer)
        .aboutOperation(sharing)
        .enableNodeSharingRole(admin)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(writer, user)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(A_NODE_ID, writer)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(true);
    }
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndPublisherOnNodeAboutSharing() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, publisher)
        .aboutOperation(sharing)
        .assertAccessIsAuthorized(false);
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, publisher)
        .aboutOperation(sharing)
        .enableNodeSharingRole(admin)
        .assertAccessIsAuthorized(false);
    for(SilverpeasRole sharingRole : List.of(writer, user)) {
      withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
          .locatedOnNodeWithSpecificRole(A_NODE_ID, publisher)
          .aboutOperation(sharing)
          .enableNodeSharingRole(sharingRole)
          .assertAccessIsAuthorized(true);
    }
  }

  /*
   * ABOUT DOWNLOAD
   */

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndAdminOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, admin)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndUserOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, user)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndWriterOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, writer)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndPublisherOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.ROOT_NODE_ID, publisher)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndAdminOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, admin)
        .aboutOperation(download)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndUserOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, user)
        .aboutOperation(download)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndWriterOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, writer)
        .aboutOperation(download)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnTrashNodeWithUserRoleOnComponentAndPublisherOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, publisher)
        .aboutOperation(download)
        .assertAccessIsAuthorized(false);
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndAdminOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, admin)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndUserOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, user)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndWriterOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, writer)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnUnclassedNodeWithUserRoleOnComponentAndPublisherOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.UNCLASSED_NODE_ID, publisher)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndAdminOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, admin)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndUserOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, user)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndWriterOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, writer)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  @Test
  public void userIsAuthorizedOnANodeNodeWithUserRoleOnComponentAndPublisherOnNodeAboutDownload() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(A_NODE_ID, publisher)
        .aboutOperation(download)
        .assertAccessIsAuthorized(true);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedOnTrashWithUserRoleOnComponentAndAdminOnNode() {
    withUserHavingComponentRole(user)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, admin)
        .assertAccessIsAuthorized(false);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnTrashWithWriterRoleOnComponentAndAdminOnNode() {
    withUserHavingComponentRole(writer)
        .andSpecificRightsEnabledOnTopic()
        .locatedOnNodeWithSpecificRole(NodePK.BIN_NODE_ID, admin)
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

    TestCaseBuilder(final TestNodeAccessController testInstance, final SilverpeasRole componentRole) {
      this.organizationController = testInstance.organizationController;
      this.componentAccessController = testInstance.componentAccessController;
      this.nodeService = testInstance.nodeService;
      this.instance = new NodeAccessController(componentAccessController);
      this.context = AccessControlContext.init();
      this.componentRole = componentRole;
      initializeMocks();
    }

    private void initializeMocks() {
      CacheServiceProvider.clearAllThreadCaches();
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
      node.setRightsDependsOn(Integer.parseInt(nodeId.equals("10") ? NO_RIGHTS_DEPENDENCY : "5"));
      when(organizationController.getComponentParameterValue(anyString(), eq("useFolderSharing")))
          .thenAnswer((Answer<String>) i -> {
            if (nodeSharingRole != null) {
              if (nodeSharingRole.equals(SilverpeasRole.admin)) {
                return "1";
              } else if (nodeSharingRole.equals(SilverpeasRole.writer)) {
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
          isNodeDataLoad && !Integer.toString(node.getRightsDependsOn()).equals(NO_RIGHTS_DEPENDENCY) ? 1 : 0;
      verify(organizationController, times(nbCalls))
          .getUserProfiles(anyString(), anyString(), any(ProfiledObjectId.class));
      verify(nodeService, times(!givingNodeInstanceDirectly && isNodeDataLoad ? 1 : 0)).getHeader(any(NodePK.class), anyBoolean());
    }

    private void setComponentInstanceUserRoleAboutOperation(SilverpeasRole... roles) {
      setUserHasReadAccessAtLeastIfNotAlreadyDone(roles.length > 0);
      setComponentInstanceUserRole(Stream.of(roles)
          .filter(isEqual(user).negate())
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

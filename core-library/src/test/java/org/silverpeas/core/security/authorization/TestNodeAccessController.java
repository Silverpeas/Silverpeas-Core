/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.test.rule.LibCoreCommonAPIRule;
import org.silverpeas.core.util.CollectionUtil;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.node.model.NodeDetail.NO_RIGHTS_DEPENDENCY;

/**
 * @author ehugonnet
 */
@UnitTest
public class TestNodeAccessController {

  private final String userId = "5";
  private final String componentId = "kmelia18";

  private OrganizationController organizationController;
  private ComponentAccessControl componentAccessController;
  private NodeService nodeService;

  private NodeAccessControl instance;

  @Rule
  public LibCoreCommonAPIRule commonAPIRule = new LibCoreCommonAPIRule();

  @Before
  public void setup() {
    User user = mock(User.class);
    when(UserProvider.get().getUser(userId)).thenReturn(user);
    organizationController = mock(OrganizationController.class);
    commonAPIRule.injectIntoMockedBeanContainer(organizationController);
    componentAccessController = mock(ComponentAccessControl.class);
    commonAPIRule.injectIntoMockedBeanContainer(componentAccessController);
    nodeService = mock(NodeService.class);
    commonAPIRule.injectIntoMockedBeanContainer(nodeService);
    instance = new NodeAccessController(componentAccessController);
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

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWhenNodeHaveNoSpecificRightsOnNode() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWhenNodeHaveNoSpecificRightsOnNodeByGivingDirectlyNodeDetail() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleWhenNodeHaveNoSpecificRightsOnNode() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    NodePK nodPk = new NodePK(NodePK.ROOT_NODE_ID, componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleWhenNodeHaveNoSpecificRightsOnNodeByGivingDirectlyNodeDetail() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    NodePK nodPk = new NodePK(NodePK.ROOT_NODE_ID, componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    when(nodeService.getHeader(nodPk, false)).thenReturn(node);
    boolean result = instance.isUserAuthorized(userId, node);
    assertThat(result, Matchers.is(true));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnRootNodeWithWriterRoleWhenNodeHaveNoSpecificRightsOnNode() {
    setComponentInstanceUserRole(SilverpeasRole.WRITER);
    NodePK nodPk = new NodePK(NodePK.ROOT_NODE_ID, componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedOnTrashWithUserRoleWhenNodeHaveNoSpecificRightsOnNode() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    NodePK nodPk = new NodePK(NodePK.BIN_NODE_ID, componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(false));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnTrashWithWriterRoleWhenNodeHaveNoSpecificRightsOnNode() {
    setComponentInstanceUserRole(SilverpeasRole.WRITER);
    NodePK nodPk = new NodePK(NodePK.BIN_NODE_ID, componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedOnSharingContextIfNotEnabled() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    AccessControlContext context = AccessControlContext.init();
    context.onOperationsOf(AccessControlOperation.SHARING);
    boolean result = instance.isUserAuthorized(userId, nodPk, context);
    assertThat(result, Matchers.is(false));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedOnSharingContextIfEnabledForAdmin() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    when(organizationController.getComponentParameterValue(anyString(), eq("useFolderSharing")))
        .thenAnswer(i -> "1");
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    AccessControlContext context = AccessControlContext.init();
    context.onOperationsOf(AccessControlOperation.SHARING);
    boolean result = instance.isUserAuthorized(userId, nodPk, context);
    assertThat(result, Matchers.is(false));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedOnSharingContextIfEnabledForContributors() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    when(organizationController.getComponentParameterValue(anyString(), eq("useFolderSharing")))
        .thenAnswer(i -> "2");
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    AccessControlContext context = AccessControlContext.init();
    context.onOperationsOf(AccessControlOperation.SHARING);
    boolean result = instance.isUserAuthorized(userId, nodPk, context);
    assertThat(result, Matchers.is(false));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnSharingContextIfEnabledForAll() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    when(organizationController.getComponentParameterValue(anyString(), eq("useFolderSharing")))
        .thenAnswer(i -> "3");
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    AccessControlContext context = AccessControlContext.init();
    context.onOperationsOf(AccessControlOperation.SHARING);
    boolean result = instance.isUserAuthorized(userId, nodPk, context);
    assertThat(result, Matchers.is(true));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWhenNodeHaveNoSpecificRightsOnComponent() {
    setComponentRightOnTopicEnabled();
    NodePK nodePk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodePk);
    boolean result = instance.isUserAuthorized(userId, nodePk);
    assertThat(result, Matchers.is(false));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWithRightOnTopicEnableButNoRightsDependOn() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    setComponentRightOnTopicEnabled();
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn(NO_RIGHTS_DEPENDENCY);
    when(nodeService.getHeader(nodPk, false)).thenReturn(node);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
    verify(organizationController, times(0))
        .getUserProfiles(anyString(), anyString(), any(ProfiledObjectId.class));
    verify(nodeService, times(1)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWithRightOnTopicEnableButNoRightsDependOnByGivingDirectlyNodeDetail() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    setComponentRightOnTopicEnabled();
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn(NO_RIGHTS_DEPENDENCY);
    boolean result = instance.isUserAuthorized(userId, node);
    assertThat(result, Matchers.is(true));
    verify(organizationController, times(0))
        .getUserProfiles(anyString(), anyString(), any(ProfiledObjectId.class));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWithRightsDependOn() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    setComponentRightOnTopicEnabled();
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn("5");
    when(nodeService.getHeader(nodPk, false)).thenReturn(node);
    when(organizationController.getUserProfiles(userId, componentId, ProfiledObjectId.fromNode("5")))
        .thenReturn(new String[]{SilverpeasRole.USER.getName()});
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
    verify(organizationController, times(1))
        .getUserProfiles(userId, componentId, ProfiledObjectId.fromNode("5"));
    verify(nodeService, times(1)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedWithRightsDependOnByGivingDirectlyNodeDetail() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    setComponentRightOnTopicEnabled();
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn("5");
    when(organizationController.getUserProfiles(userId, componentId, ProfiledObjectId.fromNode("5")))
        .thenReturn(new String[]{SilverpeasRole.USER.getName()});
    boolean result = instance.isUserAuthorized(userId, node);
    assertThat(result, Matchers.is(true));
    verify(organizationController, times(1))
        .getUserProfiles(userId, componentId, ProfiledObjectId.fromNode("5"));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedWithRightsDependOn() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    setComponentRightOnTopicEnabled();
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn("5");
    when(nodeService.getHeader(nodPk, false)).thenReturn(node);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(false));
    verify(organizationController, times(1))
        .getUserProfiles(userId, componentId, ProfiledObjectId.fromNode("5"));
    verify(nodeService, times(1)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnRootNodeWithUserRoleOnComponentAndAdminOnNode() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    setComponentRightOnTopicEnabled();
    NodePK nodPk = new NodePK(NodePK.ROOT_NODE_ID, componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn("5");
    when(organizationController.getUserProfiles(userId, componentId, ProfiledObjectId.fromNode("5")))
        .thenReturn(new String[]{SilverpeasRole.ADMIN.getName()});
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
    verify(organizationController, times(0))
        .getUserProfiles(anyString(), anyString(), any(ProfiledObjectId.class));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnRootNodeWithWriterRoleOnComponentAndAdminOnNode() {
    setComponentInstanceUserRole(SilverpeasRole.WRITER);
    setComponentRightOnTopicEnabled();
    NodePK nodPk = new NodePK(NodePK.ROOT_NODE_ID, componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn("5");
    when(organizationController.getUserProfiles(userId, componentId, ProfiledObjectId.fromNode("5")))
        .thenReturn(new String[]{SilverpeasRole.ADMIN.getName()});
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
    verify(organizationController, times(0))
        .getUserProfiles(anyString(), anyString(), any(ProfiledObjectId.class));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsNotAuthorizedOnTrashWithUserRoleOnComponentAndAdminOnNode() {
    setComponentInstanceUserRole(SilverpeasRole.USER);
    setComponentRightOnTopicEnabled();
    NodePK nodPk = new NodePK(NodePK.BIN_NODE_ID, componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn("5");
    when(organizationController.getUserProfiles(userId, componentId, ProfiledObjectId.fromNode("5")))
        .thenReturn(new String[]{SilverpeasRole.ADMIN.getName()});
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(false));
    verify(organizationController, times(0))
        .getUserProfiles(anyString(), anyString(), any(ProfiledObjectId.class));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   */
  @Test
  public void userIsAuthorizedOnTrashWithWriterRoleOnComponentAndAdminOnNode() {
    setComponentInstanceUserRole(SilverpeasRole.WRITER);
    setComponentRightOnTopicEnabled();
    NodePK nodPk = new NodePK(NodePK.BIN_NODE_ID, componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn("5");
    when(organizationController.getUserProfiles(userId, componentId, ProfiledObjectId.fromNode("5")))
        .thenReturn(new String[]{SilverpeasRole.ADMIN.getName()});
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
    verify(organizationController, times(0))
        .getUserProfiles(anyString(), anyString(), any(ProfiledObjectId.class));
    verify(nodeService, times(0)).getHeader(any(NodePK.class), anyBoolean());
  }

  private void setComponentInstanceUserRole(SilverpeasRole... roles) {
    final Set<SilverpeasRole> roleSet = CollectionUtil.asSet(roles);
    when(componentAccessController
        .getUserRoles(anyString(), anyString(), any(AccessControlContext.class)))
        .then(new Returns(roleSet));
    when(componentAccessController
        .isUserAuthorized(anySet()))
        .then(new Returns(!roleSet.isEmpty()));
  }

  private void setComponentRightOnTopicEnabled() {
    when(organizationController.getComponentParameterValue(anyString(), eq("rightsOnTopics")))
        .then(new Returns("true"));
  }
}
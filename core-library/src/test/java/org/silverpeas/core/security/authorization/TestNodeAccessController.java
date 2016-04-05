/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security.authorization;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.ObjectType;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.LibCoreCommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.CollectionUtil;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author ehugonnet
 */
public class TestNodeAccessController {

  private final String userId = "5";
  private final String componentId = "kmelia18";

  private OrganizationController organizationController;
  private ComponentAccessController componentAccessController;
  private NodeService nodeService;

  private NodeAccessControl instance;

  @Rule
  public LibCoreCommonAPI4Test commonAPI4Test = new LibCoreCommonAPI4Test();

  @Rule
  public MockByReflectionRule mockByReflectionRule = new MockByReflectionRule();


  @Before
  public void setup() {
    instance = new NodeAccessController();

    organizationController =
        mockByReflectionRule.mockField(instance, OrganizationController.class, "controller");
    commonAPI4Test.injectIntoMockedBeanContainer(organizationController);
    componentAccessController = mockByReflectionRule
        .spyField(instance, ComponentAccessController.class, "componentAccessController");
    nodeService = mockByReflectionRule.mockField(instance, NodeService.class, "nodeService");

    mockByReflectionRule.setField(componentAccessController, organizationController, "controller");

    doAnswer(invocation -> null).when(componentAccessController)
        .fillUserRoles(any(Set.class), any(AccessControlContext.class), anyString(), anyString());
    doReturn(false).
        when(componentAccessController).isRightOnTopicsEnabled(anyString());
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   * @throws Exception s
   */
  @Test
  public void userIsAuthorizedWhenNodeHaveNoSpecificRightsOnNode() throws Exception {
    setComponentInstanceUserRole(SilverpeasRole.user);
    when(organizationController.getUserProfiles(userId, componentId))
        .thenReturn(new String[]{SilverpeasRole.user.name()});
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    when(nodeService.getHeader(nodPk, false)).thenReturn(node);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   * @throws Exception s
   */
  @Test
  public void userIsAuthorizedWhenNodeHaveNoSpecificRightsOnComponent() throws Exception {
    setComponentRightOnTopicEnabled();
    NodePK nodePk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodePk);
    when(nodeService.getHeader(nodePk, false)).thenReturn(node);
    boolean result = instance.isUserAuthorized(userId, nodePk);
    assertThat(result, Matchers.is(false));
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   * @throws Exception
   */
  @Test
  public void userIsAuthorizedWithRightsDependOn() throws Exception {
    setComponentInstanceUserRole(SilverpeasRole.user);
    setComponentRightOnTopicEnabled();
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn(5);
    when(nodeService.getHeader(nodPk, false)).thenReturn(node);
    when(organizationController.getUserProfiles(userId, componentId, 5, ObjectType.NODE))
        .thenReturn(new String[]{SilverpeasRole.user.name()});
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(true));
    verify(organizationController, times(1))
        .getUserProfiles(userId, componentId, 5, ObjectType.NODE);
  }

  /**
   * Test of isUserAuthorized method, of class NodeAccessController.
   * @throws Exception
   */
  @Test
  public void userIsNotAuthorizedWithRightsDependOn() throws Exception {
    setComponentInstanceUserRole(SilverpeasRole.user);
    setComponentRightOnTopicEnabled();
    NodePK nodPk = new NodePK("10", componentId);
    NodeDetail node = new NodeDetail();
    node.setNodePK(nodPk);
    node.setRightsDependsOn(5);
    when(nodeService.getHeader(nodPk, false)).thenReturn(node);
    boolean result = instance.isUserAuthorized(userId, nodPk);
    assertThat(result, Matchers.is(false));
    verify(organizationController, times(1))
        .getUserProfiles(userId, componentId, 5, ObjectType.NODE);
  }

  @SuppressWarnings("unchecked")
  private void setComponentInstanceUserRole(SilverpeasRole... roles) {
    final Set<SilverpeasRole> roleSet = CollectionUtil.asSet(roles);
    doAnswer(invocation -> {
      Set<SilverpeasRole> userRoles = (Set) invocation.getArguments()[0];
      userRoles.addAll(roleSet);
      return null;
    }).when(componentAccessController)
        .fillUserRoles(any(Set.class), any(AccessControlContext.class), anyString(), anyString());
  }

  private void setComponentRightOnTopicEnabled() {
    doReturn(true).
        when(componentAccessController).isRightOnTopicsEnabled(anyString());
  }
}
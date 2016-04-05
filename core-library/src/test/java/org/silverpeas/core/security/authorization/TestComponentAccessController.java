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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.test.rule.LibCoreCommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;

import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author ehugonnet
 */
public class TestComponentAccessController {
  private static final String toolId = "toolId";
  private static final String componentAdminId = "ADMIN";
  private static final String componentId = "kmelia18";
  private static final String publicComponentId = "kmelia26";
  private static final String publicFilesComponentId = "kmelia20";
  private static final String publicComponentIdWithUserRole = "kmelia326";
  private static final String publicFilesComponentIdWithUserRole = "kmelia320";
  private static final String forbiddenComponent = "yellowpages154";
  private static final String componentIdWithTopicRigths = "kmelia200";
  private static final String componentIdWithoutTopicRigths = "kmelia201";

  private static final String userId = "bart";

  @Rule
  public LibCoreCommonAPI4Test commonAPI4Test = new LibCoreCommonAPI4Test();

  private OrganizationController controller;

  private ComponentAccessControl instance;
  private AccessControlContext accessControlContext;

  @Rule
  public MockByReflectionRule mockByReflectionRule = new MockByReflectionRule();

  @Before
  public void setup() {
    instance = new ComponentAccessController();
    accessControlContext = AccessControlContext.init();
    controller =
        mockByReflectionRule.mockField(instance, OrganizationController.class, "controller");
    commonAPI4Test.injectIntoMockedBeanContainer(controller);
    when(controller.getComponentInst(anyString())).thenAnswer(invocation -> {
      String instanceIdArg = (String) invocation.getArguments()[0];
      ComponentInst componentInst = mock(ComponentInst.class);
      when(componentInst.isPublic()).thenAnswer(
          invocation1 -> publicComponentId.equals(instanceIdArg) ||
              publicComponentIdWithUserRole.equals(instanceIdArg));
      when(componentInst.isTopicTracker()).thenAnswer(
          invocation1 -> instanceIdArg.startsWith("kmelia") || instanceIdArg.startsWith("kmax") ||
              instanceIdArg.startsWith("toolbox"));
      return componentInst;
    });

    final UserDetail user = new UserDetail();
    when(controller.getUserDetail(anyString())).thenReturn(user);
    when(controller.isToolAvailable(toolId)).thenReturn(true);
    when(controller.getComponentParameterValue(publicFilesComponentId, "publicFiles"))
        .thenReturn("true");
    when(controller.getComponentParameterValue(publicFilesComponentIdWithUserRole, "publicFiles"))
        .thenReturn("true");
    when(controller.getComponentParameterValue(componentId, "rightsOnTopics")).
        thenReturn("false");
    when(controller.getComponentParameterValue(forbiddenComponent, "rightsOnTopics")).
        thenReturn("false");
    when(controller.getComponentParameterValue(componentIdWithTopicRigths, "rightsOnTopics"))
        .thenReturn("true");
    when(controller.getComponentParameterValue(componentIdWithoutTopicRigths, "rightsOnTopics")).
        thenReturn("false");

    when(controller.isComponentAvailable(componentId, userId)).thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailable(publicComponentId, userId)).thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailable(publicComponentIdWithUserRole, userId))
        .thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailable(componentIdWithTopicRigths, userId))
        .thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailable(componentIdWithoutTopicRigths, userId))
        .thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailable(publicFilesComponentId, userId)).thenReturn(Boolean.FALSE);
    when(controller.isComponentAvailable(publicFilesComponentIdWithUserRole, userId))
        .thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailable(forbiddenComponent, userId)).thenReturn(Boolean.FALSE);
  }

  /**
   * Test of isRightOnTopicsEnabled method, of class ComponentAccessController.
   */
  @Test
  public void testIsRightOnTopicsEnabled() {
    boolean result = instance.isRightOnTopicsEnabled(componentId);
    assertEquals(false, result);

    result = instance.isRightOnTopicsEnabled(componentIdWithoutTopicRigths);
    assertEquals(false, result);

    result = instance.isRightOnTopicsEnabled(componentIdWithTopicRigths);
    assertEquals(true, result);
  }

  /**
   * Test of isUserAuthorized method, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testIsUserAuthorized() throws Exception {
    boolean result = instance.isUserAuthorized(userId, null);
    assertEquals(true, result);

    result = instance.isUserAuthorized(userId, publicComponentId);
    assertEquals(true, result);

    result = instance.isUserAuthorized(userId, publicFilesComponentId);
    assertEquals(true, result);

    result = instance.isUserAuthorized(userId, componentId);
    assertEquals(true, result);

    result = instance.isUserAuthorized(userId, forbiddenComponent);
    assertEquals(false, result);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndUnknownContext()
      throws Exception {
    setupUser(SilverpeasRole.admin);

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true,
        SilverpeasRole.admin);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndPersistContext()
      throws Exception {
    setupUser(SilverpeasRole.admin);
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.admin,
        SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.admin, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true,
        SilverpeasRole.admin);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndDownloadContext()
      throws Exception {
    setupUser(SilverpeasRole.admin);
    accessControlContext.onOperationsOf(AccessControlOperation.download);

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.admin,
        SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.admin, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true,
        SilverpeasRole.admin);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndSharingContext()
      throws Exception {
    setupUser(SilverpeasRole.admin);
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.admin,
        SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.admin, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true,
        SilverpeasRole.admin);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndUnknownContext()
      throws Exception {
    setupUser(SilverpeasRole.writer);

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true,
        SilverpeasRole.writer);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndPersistContext()
      throws Exception {
    setupUser(SilverpeasRole.writer);
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true,
        SilverpeasRole.writer, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.writer, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true,
        SilverpeasRole.writer);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndDownloadContext()
      throws Exception {
    setupUser(SilverpeasRole.writer);
    accessControlContext.onOperationsOf(AccessControlOperation.download);

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true,
        SilverpeasRole.writer, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.writer, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true,
        SilverpeasRole.writer);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndSharingContext()
      throws Exception {
    setupUser(SilverpeasRole.writer);
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true,
        SilverpeasRole.writer, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.writer, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true,
        SilverpeasRole.writer);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndUnknownContext()
      throws Exception {
    setupUser(SilverpeasRole.user);

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true, SilverpeasRole.user);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndPersistContext()
      throws Exception {
    setupUser(SilverpeasRole.user);
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true, SilverpeasRole.user);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndDownloadContext()
      throws Exception {
    setupUser(SilverpeasRole.user);
    accessControlContext.onOperationsOf(AccessControlOperation.download);

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true, SilverpeasRole.user);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   * @throws Exception
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndSharingContext()
      throws Exception {
    setupUser(SilverpeasRole.user);
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);

    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true,
        SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true, SilverpeasRole.user);
  }

  /**
   * Centralization.
   * @param instanceId
   * @param expectedUserAuthorization
   * @param expectedUserRoles
   */
  private void assertGetUserRolesAndIsUserAuthorized(String instanceId,
      boolean expectedUserAuthorization, SilverpeasRole... expectedUserRoles) {
    CacheServiceProvider.getRequestCacheService().clear();
    Set<SilverpeasRole> componentUserRole =
        instance.getUserRoles(userId, instanceId, accessControlContext);
    if (expectedUserRoles.length > 0) {
      assertThat("User roles on " + instanceId, componentUserRole, contains(expectedUserRoles));
    } else {
      assertThat("User roles on " + instanceId, componentUserRole, empty());
    }
    boolean result = instance.isUserAuthorized(componentUserRole);
    assertEquals("User authorized on " + instanceId, expectedUserAuthorization, result);
  }

  private void setupUser(SilverpeasRole componentRole) {
    UserDetail user = new UserDetail();
    user.setId(userId);
    if (SilverpeasRole.admin == componentRole) {
      user.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    } else {
      user.setAccessLevel(UserAccessLevel.USER);
    }
    when(controller.getUserDetail(userId)).thenReturn(user);
    for (String instanceId : new String[]{publicComponentIdWithUserRole,
        publicFilesComponentIdWithUserRole, componentId, componentIdWithTopicRigths,
        componentIdWithoutTopicRigths}) {
      when(controller.getUserProfiles(userId, instanceId))
          .thenReturn(new String[]{componentRole.name()});
    }
  }
}
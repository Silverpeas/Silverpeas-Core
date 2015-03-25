/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.accesscontrol;

import com.silverpeas.admin.components.Instanciateur;
import com.silverpeas.admin.components.WAComponent;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.accesscontrol.ComponentAccessController;
import org.silverpeas.admin.user.constant.UserAccessLevel;
import org.silverpeas.cache.service.CacheServiceProvider;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.test.rule.MockByReflectionRule;

import java.util.HashMap;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 *
 * @author ehugonnet
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Instanciateur.class})
public class ComponentAccessControllerTest {
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

  private OrganizationController controller;

  private ComponentAccessController instance;
  private AccessControlContext accessControlContext;

  @Rule
  public MockByReflectionRule mockByReflectionRule = new MockByReflectionRule();

  @Before
  public void setup() {
    mockStatic(Instanciateur.class);
    WAComponent kmeliaComponent = new WAComponent();
    kmeliaComponent.setName("kmelia");
    HashMap<String, String> label = new HashMap<String, String>();
    label.put("en", "kmelia");
    label.put("fr", "kmelia");
    kmeliaComponent.setLabel(label);
    kmeliaComponent.setVisible(true);
    kmeliaComponent.setPortlet(true);
    WAComponent yellowComponent = new WAComponent();
    yellowComponent.setName("yellowpages");
    HashMap<String, String> label2 = new HashMap<String, String>();
    label2.put("en", "yellowpages");
    label2.put("fr", "yellowpages");
    yellowComponent.setLabel(label2);
    yellowComponent.setVisible(true);
    yellowComponent.setPortlet(true);
    when(Instanciateur.getWAComponent("kmelia")).thenReturn(kmeliaComponent);
    when(Instanciateur.getWAComponent("yellowpages")).thenReturn(yellowComponent);

    controller = mock(OrganizationController.class);
    mockByReflectionRule.setField(OrganisationControllerFactory.class, controller,
        "instance.organisationController");

    when(controller.getComponentInst(anyString())).thenAnswer(new Answer<ComponentInst>() {
      @Override
      public ComponentInst answer(final InvocationOnMock invocation) throws Throwable {
        String instanceIdArg = (String) invocation.getArguments()[0];
        ComponentInst componentInst = new ComponentInst();
        if (publicComponentId.equals(instanceIdArg) ||
            publicComponentIdWithUserRole.equals(instanceIdArg)) {
          componentInst.setPublic(true);
        }
        return componentInst;
      }
    });

    when(controller.getUserDetail(anyString())).thenReturn(new UserDetail());
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

    // instance on which tests are performed.
    instance = new ComponentAccessController();
    instance.setOrganizationController(controller);
    accessControlContext = AccessControlContext.init();
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
  public void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndDonwloadContext()
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
  public void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndDonwloadContext()
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
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndDonwloadContext()
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
   * Centralization.
   * @param instanceId
   * @param expectedUserAuthorization
   * @param expectedUserRoles
   */
  private void assertGetUserRolesAndIsUserAuthorized(String instanceId,
      boolean expectedUserAuthorization, SilverpeasRole... expectedUserRoles) {
    CacheServiceProvider.getRequestCacheService().clear();
    Set<SilverpeasRole> componentUserRole =
        instance.getUserRoles(accessControlContext, userId, instanceId);
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
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
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.admin.component.PersonalComponentRegistry;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.test.rule.LibCoreCommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author ehugonnet
 */
@UnitTest
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
  private static final String personalComponentId = "personalComponent26_PCI";

  private static final String USER_ID = "26";
  private static final String ANONYMOUS_ID = "26598";

  @Rule
  public LibCoreCommonAPI4Test commonAPI4Test = new LibCoreCommonAPI4Test();

  private OrganizationController controller;

  private ComponentAccessControl instance;
  private AccessControlContext accessControlContext;

  @Rule
  public MockByReflectionRule mockByReflectionRule = new MockByReflectionRule();

  private String currentUserId;

  @Before
  public void setup() {
    instance = new ComponentAccessController();
    controller =
        mockByReflectionRule.mockField(instance, OrganizationController.class, "controller");
    commonAPI4Test.injectIntoMockedBeanContainer(controller);
    accessControlContext = AccessControlContext.init();
    final UserDetail user = new UserDetail();
    user.setId(USER_ID);
    when(UserProvider.get().getUser(USER_ID)).thenReturn(user);
    when(controller.getUserDetail(USER_ID)).thenReturn(user);
    final UserDetail anonymous = new UserDetail();
    anonymous.setId(ANONYMOUS_ID);
    when(UserProvider.get().getUser(ANONYMOUS_ID)).thenReturn(anonymous);
    when(controller.getUserDetail(ANONYMOUS_ID)).thenReturn(anonymous);
    final PersonalComponentRegistry personalComponentRegistry = commonAPI4Test
        .injectIntoMockedBeanContainer(mock(PersonalComponentRegistry.class));
    final PersonalComponent personalComponent = mock(PersonalComponent.class);
    when(personalComponentRegistry.getPersonalComponent("personalComponent"))
        .then(new Returns(Optional.of(personalComponent)));
    final Answer<ComponentInst> componentInstanceAnswer = invocation -> {
      final String instanceIdArg = (String) invocation.getArguments()[0];
      final ComponentInst componentInst = mock(ComponentInst.class);
      when(componentInst.isPublic()).thenAnswer(
          i -> publicComponentId.equals(instanceIdArg) || publicComponentIdWithUserRole.equals(instanceIdArg));
      when(componentInst.isTopicTracker()).thenAnswer(
          i -> instanceIdArg.startsWith("kmelia") || instanceIdArg.startsWith("kmax") || instanceIdArg.startsWith("toolbox"));
      when(componentInst.isPersonal()).thenAnswer(
          i -> instanceIdArg.equals(personalComponentId));
      when(componentInst.getSilverpeasRolesFor(any(User.class))).thenAnswer( i -> {
        final User u = (User) i.getArguments()[0];
        if (componentInst.isPersonal()) {
          return PersonalComponentInstance.from(personalComponentId)
              .orElseThrow(() -> new NotSupportedException("")).getSilverpeasRolesFor(u);
        }
        throw new NotSupportedException("this case is not yet handled");
      });
      return componentInst;
    };
    when(controller.getComponentInst(anyString())).thenAnswer(componentInstanceAnswer);
    when(controller.getComponentInstance(anyString())).thenAnswer(invocation -> {
      ComponentInst componentInst = componentInstanceAnswer.answer(invocation);
      return Optional.ofNullable(componentInst);
    });

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

    when(controller.isComponentAvailableToUser(componentId, USER_ID)).thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailableToUser(publicComponentId, USER_ID)).thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailableToUser(publicComponentIdWithUserRole, USER_ID))
        .thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailableToUser(componentIdWithTopicRigths, USER_ID))
        .thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailableToUser(componentIdWithoutTopicRigths, USER_ID))
        .thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailableToUser(publicFilesComponentId, USER_ID)).thenReturn(Boolean.FALSE);
    when(controller.isComponentAvailableToUser(publicFilesComponentIdWithUserRole, USER_ID))
        .thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailableToUser(forbiddenComponent, USER_ID)).thenReturn(Boolean.FALSE);
  }

  /**
   * Test of isRightOnTopicsEnabled method, of class ComponentAccessController.
   */
  @Test
  public void testIsRightOnTopicsEnabled() {
    boolean result = instance.isRightOnTopicsEnabled(componentId);
    assertFalse(result);

    result = instance.isRightOnTopicsEnabled(componentIdWithoutTopicRigths);
    assertFalse(result);

    result = instance.isRightOnTopicsEnabled(componentIdWithTopicRigths);
    assertTrue(result);
  }

  /**
   * Test of isUserAuthorized method, of class ComponentAccessController.
   */
  @Test
  public void testIsUserAuthorized() {
    final UserDetail user = controller.getUserDetail(USER_ID);
    final UserDetail anonymous = controller.getUserDetail(ANONYMOUS_ID);

    // USER DOES NOT EXIST
    when(UserProvider.get().getUser(USER_ID)).thenReturn(null);
    when(controller.getUserDetail(USER_ID)).thenReturn(null);
    when(UserProvider.get().getUser(ANONYMOUS_ID)).thenReturn(null);
    when(controller.getUserDetail(ANONYMOUS_ID)).thenReturn(null);

    boolean result = instance.isUserAuthorized(USER_ID, null);
    assertFalse(result);

    result = instance.isUserAuthorized(USER_ID, publicComponentId);
    assertFalse(result);

    result = instance.isUserAuthorized(USER_ID, publicFilesComponentId);
    assertFalse(result);

    result = instance.isUserAuthorized(USER_ID, componentId);
    assertFalse(result);

    result = instance.isUserAuthorized(USER_ID, forbiddenComponent);
    assertFalse(result);

    // USER EXIST
    when(UserProvider.get().getUser(USER_ID)).thenReturn(user);
    when(controller.getUserDetail(USER_ID)).thenReturn(user);
    when(UserProvider.get().getUser(ANONYMOUS_ID)).thenReturn(anonymous);
    when(controller.getUserDetail(ANONYMOUS_ID)).thenReturn(anonymous);
    setupUser(null);

    result = instance.isUserAuthorized(USER_ID, null);
    assertTrue(result);

    result = instance.isUserAuthorized(USER_ID, publicComponentId);
    assertTrue(result);

    result = instance.isUserAuthorized(USER_ID, publicFilesComponentId);
    assertTrue(result);

    result = instance.isUserAuthorized(USER_ID, componentId);
    assertFalse(result);

    result = instance.isUserAuthorized(USER_ID, forbiddenComponent);
    assertFalse(result);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithSpecificRoleRoleAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForSpecificRole();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithSpecificRoleRoleAndPersistContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    assertGetUserRolesAndIsUserAuthorizedForSpecificRole();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithSpecificRoleRoleAndDownloadContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.download);
    assertGetUserRolesAndIsUserAuthorizedForSpecificRole();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithSpecificRoleRoleAndSharingContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);
    assertGetUserRolesAndIsUserAuthorizedForSpecificRole();
  }

  private void assertGetUserRolesAndIsUserAuthorizedForSpecificRole() {
    setupUser("specificRole", UserState.VALID);
    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, true, SilverpeasRole.admin);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForAdmin();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndPersistContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    assertGetUserRolesAndIsUserAuthorizedForAdmin();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndDownloadContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.download);
    assertGetUserRolesAndIsUserAuthorizedForAdmin();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndSharingContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);
    assertGetUserRolesAndIsUserAuthorizedForAdmin();
  }

  private void assertGetUserRolesAndIsUserAuthorizedForAdmin() {
    setupUser(SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.admin, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true, SilverpeasRole.admin, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, true, SilverpeasRole.admin);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForWriter();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndPersistContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    assertGetUserRolesAndIsUserAuthorizedForWriter();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndDownloadContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.download);
    assertGetUserRolesAndIsUserAuthorizedForWriter();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndSharingContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);
    assertGetUserRolesAndIsUserAuthorizedForWriter();
  }

  private void assertGetUserRolesAndIsUserAuthorizedForWriter() {
    setupUser(SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.writer, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true, SilverpeasRole.writer, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true, SilverpeasRole.writer);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, true, SilverpeasRole.admin);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndPersistContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    assertGetUserRolesAndIsUserAuthorizedForReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndDownloadContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.download);
    assertGetUserRolesAndIsUserAuthorizedForReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndSharingContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);
    assertGetUserRolesAndIsUserAuthorizedForReader();
  }

  private void assertGetUserRolesAndIsUserAuthorizedForReader() {
    setupUser(SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, true, SilverpeasRole.admin);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButBlockedAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForBlockedReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButBlockedAndPersistContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    assertGetUserRolesAndIsUserAuthorizedForBlockedReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButBlockedAndDownloadContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.download);
    assertGetUserRolesAndIsUserAuthorizedForBlockedReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButBlockedAndSharingContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);
    assertGetUserRolesAndIsUserAuthorizedForBlockedReader();
  }

  private void assertGetUserRolesAndIsUserAuthorizedForBlockedReader() {
    setupUser(SilverpeasRole.user, UserState.BLOCKED);
    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, true, SilverpeasRole.admin);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButExpiredAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForExpiredReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButExpiredAndPersistContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    assertGetUserRolesAndIsUserAuthorizedForExpiredReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButExpiredAndDownloadContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.download);
    assertGetUserRolesAndIsUserAuthorizedForExpiredReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButExpiredAndSharingContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);
    assertGetUserRolesAndIsUserAuthorizedForExpiredReader();
  }

  private void assertGetUserRolesAndIsUserAuthorizedForExpiredReader() {
    setupUser(SilverpeasRole.user, UserState.EXPIRED);
    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, true, SilverpeasRole.admin);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndAnonymousUserButExpiredAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForAnonymous();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndAnonymousUserButExpiredAndPersistContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    assertGetUserRolesAndIsUserAuthorizedForAnonymous();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndAnonymousUserButExpiredAndDownloadContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.download);
    assertGetUserRolesAndIsUserAuthorizedForAnonymous();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndAnonymousUserButExpiredAndSharingContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);
    assertGetUserRolesAndIsUserAuthorizedForAnonymous();
  }

  private void assertGetUserRolesAndIsUserAuthorizedForAnonymous() {
    setupAnonymousUser();
    assertGetUserRolesAndIsUserAuthorized(null, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(toolId, true, SilverpeasRole.admin);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, true, SilverpeasRole.user);
    assertGetUserRolesAndIsUserAuthorized(componentId, false);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, false);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, false);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeletedAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForDeletedReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeletedAndPersistContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    assertGetUserRolesAndIsUserAuthorizedForDeletedReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeletedAndDownloadContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.download);
    assertGetUserRolesAndIsUserAuthorizedForDeletedReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeletedAndSharingContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);
    assertGetUserRolesAndIsUserAuthorizedForDeletedReader();
  }

  private void assertGetUserRolesAndIsUserAuthorizedForDeletedReader() {
    setupUser(SilverpeasRole.user, UserState.DELETED);
    assertGetUserRolesAndIsUserAuthorized(null, false);
    assertGetUserRolesAndIsUserAuthorized(toolId, false);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, false);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, false);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, false);
    assertGetUserRolesAndIsUserAuthorized(componentId, false);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, false);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, false);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeactivatedAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForDeactivatedReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeactivatedAndPersistContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    assertGetUserRolesAndIsUserAuthorizedForDeactivatedReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeactivatedAndDownloadContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.download);
    assertGetUserRolesAndIsUserAuthorizedForDeactivatedReader();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeactivatedAndSharingContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);
    assertGetUserRolesAndIsUserAuthorizedForDeactivatedReader();
  }

  private void assertGetUserRolesAndIsUserAuthorizedForDeactivatedReader() {
    setupUser(SilverpeasRole.user, UserState.DEACTIVATED);
    assertGetUserRolesAndIsUserAuthorized(null, false);
    assertGetUserRolesAndIsUserAuthorized(toolId, false);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, false);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, false);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, false);
    assertGetUserRolesAndIsUserAuthorized(componentId, false);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, false);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, false);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesWithoutUserAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedWithourUser();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesWithoutUserAndPersistContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.PERSIST_ACTIONS.iterator().next());
    assertGetUserRolesAndIsUserAuthorizedWithourUser();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesWithoutUserAndDownloadContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.download);
    assertGetUserRolesAndIsUserAuthorizedWithourUser();
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  public void testGetUserRolesWithoutUserAndSharingContext() {
    accessControlContext.onOperationsOf(AccessControlOperation.sharing);
    assertGetUserRolesAndIsUserAuthorizedWithourUser();
  }

  private void assertGetUserRolesAndIsUserAuthorizedWithourUser() {
    assertGetUserRolesAndIsUserAuthorized(null, false);
    assertGetUserRolesAndIsUserAuthorized(toolId, false);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, false);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, false);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, false);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, false);
    assertGetUserRolesAndIsUserAuthorized(componentId, false);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, false);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, false);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, false);
  }

  /**
   * Centralization.
   */
  private void assertGetUserRolesAndIsUserAuthorized(String instanceId,
      boolean expectedUserAuthorization, SilverpeasRole... expectedUserRoles) {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    Set<SilverpeasRole> componentUserRole =
        instance.getUserRoles(currentUserId, instanceId, accessControlContext);
    if (expectedUserRoles.length > 0) {
      assertThat("User roles on " + instanceId, componentUserRole, contains(expectedUserRoles));
    } else {
      assertThat("User roles on " + instanceId, componentUserRole, empty());
    }
    boolean result = instance.isUserAuthorized(componentUserRole);
    assertEquals("User authorized on " + instanceId, expectedUserAuthorization, result);
  }

  private void setupUser(SilverpeasRole componentRole) {
    setupUser(componentRole, UserState.VALID);
  }

  private void setupUser(SilverpeasRole componentRole, UserState userState) {
    setupUser(componentRole != null ? componentRole.name() : null, userState);
  }

  private void setupUser(String componentRole, UserState userState) {
    final UserDetail user = controller.getUserDetail(USER_ID);
    prepareUser(user, componentRole, userState);
    if (componentRole != null) {
      for (String instanceId : new String[]{publicComponentIdWithUserRole,
          publicFilesComponentIdWithUserRole, componentId, componentIdWithTopicRigths,
          componentIdWithoutTopicRigths}) {
        when(controller.getUserProfiles(USER_ID, instanceId))
            .thenReturn(new String[]{componentRole});
      }
    }
  }

  private void setupAnonymousUser() {
    final UserDetail user = controller.getUserDetail(ANONYMOUS_ID);
    prepareUser(user, null, UserState.VALID);
    user.setAccessLevel(UserAccessLevel.GUEST);
  }

  private void prepareUser(final UserDetail user, final String componentRoleAsString, final UserState userState) {
    currentUserId = user.getId();
    final SilverpeasRole componentRole = SilverpeasRole.from(componentRoleAsString);
    if (SilverpeasRole.admin == componentRole) {
      user.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    } else {
      user.setAccessLevel(UserAccessLevel.USER);
    }
    user.setState(userState);
  }
}
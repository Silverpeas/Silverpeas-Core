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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.component.PersonalComponentRegistry;
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.DefaultSilverpeasComponentDataProvider;
import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.component.service.DefaultSilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.RemovedSpaceAndComponentInstanceChecker;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.exception.NotSupportedException;
import org.silverpeas.kernel.test.UnitTest;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author ehugonnet
 */
@UnitTest
@EnableSilverTestEnv(context = JEETestContext.class)
class ComponentAccessControllerTest {
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
  private static final String GUEST_ID = "38895";
  private static final AccessControlOperation NONE = null;
  private static final AccessControlOperation A_PERSIST_ACTION = AccessControlOperation.PERSIST_ACTIONS.iterator().next();

  @TestManagedMock
  private OrganizationController controller;
  @TestManagedMock
  private PersonalComponentRegistry personalComponentRegistry;
  @TestManagedMock
  private RemovedSpaceAndComponentInstanceChecker checker;
  @TestManagedBean
  private DefaultSilverpeasComponentInstanceProvider componentInstanceProvider;
  @TestManagedBean
  private DefaultSilverpeasComponentDataProvider componentDataProvider;
  @TestManagedBean
  private WAComponentRegistry componentRegistry;
  @TestManagedBean
  private DefaultInstanceAccessControlExtension defaultInstanceAccessControlExtension;

  private ComponentAccessControl instance;

  private String currentUserId;

  @BeforeEach
  void setup() {
    when(checker.resetWithCacheSizeOf(any(Integer.class))).thenReturn(checker);
    final UserDetail user = new UserDetail();
    user.setId(USER_ID);
    when(UserProvider.get().getUser(USER_ID)).thenReturn(user);
    when(controller.getUserDetail(USER_ID)).thenReturn(user);
    final UserDetail anonymous = new UserDetail();
    anonymous.setId(ANONYMOUS_ID);
    when(UserProvider.get().getUser(ANONYMOUS_ID)).thenReturn(anonymous);
    when(controller.getUserDetail(ANONYMOUS_ID)).thenReturn(anonymous);
    final UserDetail aGuest = new UserDetail();
    aGuest.setId(GUEST_ID);
    when(UserProvider.get().getUser(GUEST_ID)).thenReturn(aGuest);
    when(controller.getUserDetail(GUEST_ID)).thenReturn(aGuest);
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
    initTest();
  }

  /**
   * Test of isRightOnTopicsEnabled method, of class ComponentAccessController.
   */
  @Test
  void testIsRightOnTopicsEnabled() {
    initTest();
    boolean result = instance.isRightOnTopicsEnabled(componentId);
    assertFalse(result);

    initTest();
    result = instance.isRightOnTopicsEnabled(componentIdWithoutTopicRigths);
    assertFalse(result);

    initTest();
    result = instance.isRightOnTopicsEnabled(componentIdWithTopicRigths);
    assertTrue(result);
  }

  /**
   * Test of isUserAuthorized method, of class ComponentAccessController.
   */
  @Test
  void testIsUserAuthorized() {
    final UserDetail user = controller.getUserDetail(USER_ID);
    final UserDetail anonymous = controller.getUserDetail(ANONYMOUS_ID);
    final UserDetail aGuest = controller.getUserDetail(GUEST_ID);

    // USER DOES NOT EXIST
    when(UserProvider.get().getUser(USER_ID)).thenReturn(null);
    when(controller.getUserDetail(USER_ID)).thenReturn(null);
    when(UserProvider.get().getUser(ANONYMOUS_ID)).thenReturn(null);
    when(controller.getUserDetail(ANONYMOUS_ID)).thenReturn(null);
    when(UserProvider.get().getUser(GUEST_ID)).thenReturn(null);
    when(controller.getUserDetail(GUEST_ID)).thenReturn(null);

    initTest();
    boolean result = instance.isUserAuthorized(USER_ID, (String)null);
    assertFalse(result);

    initTest();
    result = instance.isUserAuthorized(USER_ID, publicComponentId);
    assertFalse(result);

    initTest();
    result = instance.isUserAuthorized(USER_ID, publicFilesComponentId);
    assertFalse(result);

    initTest();
    result = instance.isUserAuthorized(USER_ID, componentId);
    assertFalse(result);

    initTest();
    result = instance.isUserAuthorized(USER_ID, forbiddenComponent);
    assertFalse(result);

    // USER EXIST
    when(UserProvider.get().getUser(USER_ID)).thenReturn(user);
    when(controller.getUserDetail(USER_ID)).thenReturn(user);
    when(UserProvider.get().getUser(ANONYMOUS_ID)).thenReturn(anonymous);
    when(controller.getUserDetail(ANONYMOUS_ID)).thenReturn(anonymous);
    when(UserProvider.get().getUser(GUEST_ID)).thenReturn(aGuest);
    when(controller.getUserDetail(GUEST_ID)).thenReturn(aGuest);
    setupUser(null);

    initTest();
    result = instance.isUserAuthorized(USER_ID, (String)null);
    assertTrue(result);

    initTest();
    result = instance.isUserAuthorized(USER_ID, publicComponentId);
    assertTrue(result);

    initTest();
    result = instance.isUserAuthorized(USER_ID, publicFilesComponentId);
    assertTrue(result);

    initTest();
    result = instance.isUserAuthorized(USER_ID, componentId);
    assertFalse(result);

    initTest();
    result = instance.isUserAuthorized(USER_ID, forbiddenComponent);
    assertFalse(result);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithSpecificRoleRoleAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForSpecificRole(NONE);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithSpecificRoleRoleAndPersistContext() {
    assertGetUserRolesAndIsUserAuthorizedForSpecificRole(A_PERSIST_ACTION);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithSpecificRoleRoleAndDownloadContext() {
    assertGetUserRolesAndIsUserAuthorizedForSpecificRole(AccessControlOperation.DOWNLOAD);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithSpecificRoleRoleAndSharingContext() {
    assertGetUserRolesAndIsUserAuthorizedForSpecificRole(AccessControlOperation.SHARING);
  }

  private void assertGetUserRolesAndIsUserAuthorizedForSpecificRole(
      final AccessControlOperation operation) {
    setupUser("specificRole", UserState.VALID);
    assertGetUserRolesAndIsUserAuthorized(null, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(toolId, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, operation);
    if (isPersistOperation(operation)) {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation);
      assertGetUserRolesAndIsUserAuthorized(componentId, operation);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation);
    } else {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation, SilverpeasRole.USER);
    }
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, operation);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, operation, SilverpeasRole.ADMIN);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForAdmin(NONE);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndPersistContext() {
    assertGetUserRolesAndIsUserAuthorizedForAdmin(A_PERSIST_ACTION);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndDownloadContext() {
    assertGetUserRolesAndIsUserAuthorizedForAdmin(AccessControlOperation.DOWNLOAD);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithAdminUserRoleAndSharingContext() {
    assertGetUserRolesAndIsUserAuthorizedForAdmin(AccessControlOperation.SHARING);
  }

  private void assertGetUserRolesAndIsUserAuthorizedForAdmin(final AccessControlOperation operation) {
    setupUser(SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(null, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(toolId, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, operation, SilverpeasRole.ADMIN);
    if(isPersistOperation(operation)) {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation, SilverpeasRole.ADMIN);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation, SilverpeasRole.ADMIN);
    } else {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation, SilverpeasRole.ADMIN, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation, SilverpeasRole.ADMIN, SilverpeasRole.USER);
    }
    assertGetUserRolesAndIsUserAuthorized(componentId, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, operation, SilverpeasRole.ADMIN);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForWriter(NONE);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndPersistContext() {
    assertGetUserRolesAndIsUserAuthorizedForWriter(A_PERSIST_ACTION);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndDownloadContext() {
    assertGetUserRolesAndIsUserAuthorizedForWriter(AccessControlOperation.DOWNLOAD);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithWriterUserRoleAndSharingContext() {
    assertGetUserRolesAndIsUserAuthorizedForWriter(AccessControlOperation.SHARING);
  }

  private void assertGetUserRolesAndIsUserAuthorizedForWriter(
      final AccessControlOperation operation) {
    setupUser(SilverpeasRole.WRITER);
    assertGetUserRolesAndIsUserAuthorized(null, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(toolId, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, operation);
    if (isPersistOperation(operation)) {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation, SilverpeasRole.WRITER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation, SilverpeasRole.WRITER);
    } else {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation, SilverpeasRole.WRITER, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation, SilverpeasRole.WRITER, SilverpeasRole.USER);
    }
    assertGetUserRolesAndIsUserAuthorized(componentId, operation, SilverpeasRole.WRITER);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation, SilverpeasRole.WRITER);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation, SilverpeasRole.WRITER);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, operation, SilverpeasRole.ADMIN);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForReader(NONE);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndPersistContext() {
    assertGetUserRolesAndIsUserAuthorizedForReader(A_PERSIST_ACTION);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndDownloadContext() {
    assertGetUserRolesAndIsUserAuthorizedForReader(AccessControlOperation.DOWNLOAD);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleAndSharingContext() {
    assertGetUserRolesAndIsUserAuthorizedForReader(AccessControlOperation.SHARING);
  }

  private void assertGetUserRolesAndIsUserAuthorizedForReader(
      final AccessControlOperation operation) {
    setupUser(SilverpeasRole.USER);
    assertGetUserRolesAndIsUserAuthorized(null, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(toolId, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, operation);
    if (isPersistOperation(operation)) {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation);
      assertGetUserRolesAndIsUserAuthorized(componentId, operation);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation);
    } else {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation, SilverpeasRole.USER);
    }
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, operation);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButBlockedAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForBlockedReader(NONE);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButBlockedAndPersistContext() {
    assertGetUserRolesAndIsUserAuthorizedForBlockedReader(A_PERSIST_ACTION);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButBlockedAndDownloadContext() {
    assertGetUserRolesAndIsUserAuthorizedForBlockedReader(AccessControlOperation.DOWNLOAD);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButBlockedAndSharingContext() {
    assertGetUserRolesAndIsUserAuthorizedForBlockedReader(AccessControlOperation.SHARING);
  }

  private void assertGetUserRolesAndIsUserAuthorizedForBlockedReader(
      final AccessControlOperation operation) {
    setupUser(SilverpeasRole.USER, UserState.BLOCKED);
    assertGetUserRolesAndIsUserAuthorized(null, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(toolId, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, operation);
    if (isPersistOperation(operation)) {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation);
      assertGetUserRolesAndIsUserAuthorized(componentId, operation);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation);
    } else {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation, SilverpeasRole.USER);
    }
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, operation);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, operation, SilverpeasRole.ADMIN);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButExpiredAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForExpiredReader(NONE);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButExpiredAndPersistContext() {
    assertGetUserRolesAndIsUserAuthorizedForExpiredReader(A_PERSIST_ACTION);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButExpiredAndDownloadContext() {
    assertGetUserRolesAndIsUserAuthorizedForExpiredReader(AccessControlOperation.DOWNLOAD);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButExpiredAndSharingContext() {
    assertGetUserRolesAndIsUserAuthorizedForExpiredReader(AccessControlOperation.SHARING);
  }

  private void assertGetUserRolesAndIsUserAuthorizedForExpiredReader(
      final AccessControlOperation operation) {
    setupUser(SilverpeasRole.USER, UserState.EXPIRED);
    assertGetUserRolesAndIsUserAuthorized(null, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(toolId, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, operation);
    if (isPersistOperation(operation)) {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation);
      assertGetUserRolesAndIsUserAuthorized(componentId, operation);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation);
    } else {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation, SilverpeasRole.USER);
    }
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, operation);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, operation, SilverpeasRole.ADMIN);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndAnonymousUserButExpiredAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForAnonymous(NONE);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndAnonymousUserButExpiredAndPersistContext() {
    assertGetUserRolesAndIsUserAuthorizedForAnonymous(A_PERSIST_ACTION);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndAnonymousUserButExpiredAndDownloadContext() {
    assertGetUserRolesAndIsUserAuthorizedForAnonymous(AccessControlOperation.DOWNLOAD);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndAnonymousUserButExpiredAndSharingContext() {
    assertGetUserRolesAndIsUserAuthorizedForAnonymous(AccessControlOperation.SHARING);
  }

  private void assertGetUserRolesAndIsUserAuthorizedForAnonymous(
      final AccessControlOperation operation) {
    setupAnonymousUser();
    assertGetUserRolesAndIsUserAuthorized(null, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(toolId, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, operation);
    if (isPersistOperation(operation)) {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation);
    } else {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation, SilverpeasRole.USER);
    }
    assertGetUserRolesAndIsUserAuthorized(componentId, operation);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, operation);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndGuestUserButExpiredAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForGuest(NONE);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndGuestUserButExpiredAndPersistContext() {
    assertGetUserRolesAndIsUserAuthorizedForGuest(A_PERSIST_ACTION);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndGuestUserButExpiredAndDownloadContext() {
    assertGetUserRolesAndIsUserAuthorizedForGuest(AccessControlOperation.DOWNLOAD);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndGuestUserButExpiredAndSharingContext() {
    assertGetUserRolesAndIsUserAuthorizedForGuest(AccessControlOperation.SHARING);
  }

  private void assertGetUserRolesAndIsUserAuthorizedForGuest(
      final AccessControlOperation operation) {
    setupGuestUser();
    assertGetUserRolesAndIsUserAuthorized(null, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(toolId, operation, SilverpeasRole.ADMIN);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, operation);
    if (isPersistOperation(operation)) {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation);
    } else {
      assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation, SilverpeasRole.USER);
      assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation, SilverpeasRole.USER);
    }
    assertGetUserRolesAndIsUserAuthorized(componentId, operation);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, operation);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeletedAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForDeletedReader(NONE);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeletedAndPersistContext() {
    assertGetUserRolesAndIsUserAuthorizedForDeletedReader(A_PERSIST_ACTION);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeletedAndDownloadContext() {
    assertGetUserRolesAndIsUserAuthorizedForDeletedReader(AccessControlOperation.DOWNLOAD);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeletedAndSharingContext() {
    assertGetUserRolesAndIsUserAuthorizedForDeletedReader(AccessControlOperation.SHARING);
  }

  private void assertGetUserRolesAndIsUserAuthorizedForDeletedReader(
      final AccessControlOperation operation) {
    setupUser(SilverpeasRole.USER, UserState.DELETED);
    assertGetUserRolesAndIsUserAuthorized(null, operation);
    assertGetUserRolesAndIsUserAuthorized(toolId, operation);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, operation);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation);
    assertGetUserRolesAndIsUserAuthorized(componentId, operation);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, operation);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeactivatedAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedForDeactivatedReader(NONE);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeactivatedAndPersistContext() {
    assertGetUserRolesAndIsUserAuthorizedForDeactivatedReader(A_PERSIST_ACTION);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeactivatedAndDownloadContext() {
    assertGetUserRolesAndIsUserAuthorizedForDeactivatedReader(AccessControlOperation.DOWNLOAD);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesAndIsUserAuthorizedWithReaderUserRoleButDeactivatedAndSharingContext() {
    assertGetUserRolesAndIsUserAuthorizedForDeactivatedReader(AccessControlOperation.SHARING);
  }

  private void assertGetUserRolesAndIsUserAuthorizedForDeactivatedReader(
      final AccessControlOperation operation) {
    setupUser(SilverpeasRole.USER, UserState.DEACTIVATED);
    assertGetUserRolesAndIsUserAuthorized(null, operation);
    assertGetUserRolesAndIsUserAuthorized(toolId, operation);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, operation);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation);
    assertGetUserRolesAndIsUserAuthorized(componentId, operation);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, operation);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesWithoutUserAndUnknownContext() {
    assertGetUserRolesAndIsUserAuthorizedWithoutUser(NONE);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesWithoutUserAndPersistContext() {
    assertGetUserRolesAndIsUserAuthorizedWithoutUser(A_PERSIST_ACTION);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesWithoutUserAndDownloadContext() {
    assertGetUserRolesAndIsUserAuthorizedWithoutUser(AccessControlOperation.DOWNLOAD);
  }

  /**
   * Test of getUserRoles and isUserAuthorized methods, of class ComponentAccessController.
   */
  @Test
  void testGetUserRolesWithoutUserAndSharingContext() {
    assertGetUserRolesAndIsUserAuthorizedWithoutUser(AccessControlOperation.SHARING);
  }

  private void assertGetUserRolesAndIsUserAuthorizedWithoutUser(
      final AccessControlOperation operation) {
    assertGetUserRolesAndIsUserAuthorized(null, operation);
    assertGetUserRolesAndIsUserAuthorized(toolId, operation);
    assertGetUserRolesAndIsUserAuthorized(componentAdminId, operation);
    assertGetUserRolesAndIsUserAuthorized(publicComponentId, operation);
    assertGetUserRolesAndIsUserAuthorized(publicComponentIdWithUserRole, operation);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentId, operation);
    assertGetUserRolesAndIsUserAuthorized(publicFilesComponentIdWithUserRole, operation);
    assertGetUserRolesAndIsUserAuthorized(componentId, operation);
    assertGetUserRolesAndIsUserAuthorized(forbiddenComponent, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithTopicRigths, operation);
    assertGetUserRolesAndIsUserAuthorized(componentIdWithoutTopicRigths, operation);
    assertGetUserRolesAndIsUserAuthorized(personalComponentId, operation);
  }

  private boolean isPersistOperation(final AccessControlOperation operation) {
    return AccessControlOperation.PERSIST_ACTIONS.contains(operation);
  }

  /**
   * Centralization.
   */
  private void assertGetUserRolesAndIsUserAuthorized(String instanceId,
      final AccessControlOperation operation, SilverpeasRole... expectedUserRoles) {
    initTest();
    final AccessControlContext accessControlContext = AccessControlContext.init();
    if (operation != null) {
      accessControlContext.onOperationsOf(operation);
    }
    final Set<SilverpeasRole> componentUserRole =
        instance.getUserRoles(currentUserId, instanceId, accessControlContext);
    final boolean expectedUserAuthorization;
    if (expectedUserRoles.length > 0) {
      assertThat("User roles on " + instanceId, componentUserRole, contains(expectedUserRoles));
      expectedUserAuthorization = true;
    } else {
      assertThat("User roles on " + instanceId, componentUserRole, empty());
      expectedUserAuthorization = false;
    }
    boolean result = instance.isUserAuthorized(componentUserRole);
    Assertions.assertEquals(expectedUserAuthorization, result, "User authorized on " + instanceId);
  }

  private void setupUser(SilverpeasRole componentRole) {
    setupUser(componentRole, UserState.VALID);
  }

  private void setupUser(SilverpeasRole componentRole, UserState userState) {
    setupUser(componentRole != null ? componentRole.getName() : null, userState);
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

  private void setupGuestUser() {
    final UserDetail user = controller.getUserDetail(GUEST_ID);
    prepareUser(user, null, UserState.VALID);
    user.setAccessLevel(UserAccessLevel.GUEST);
  }

  private void prepareUser(final UserDetail user, final String componentRoleAsString, final UserState userState) {
    currentUserId = user.getId();
    final SilverpeasRole componentRole = SilverpeasRole.fromString(componentRoleAsString);
    if (SilverpeasRole.ADMIN == componentRole) {
      user.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    } else {
      user.setAccessLevel(UserAccessLevel.USER);
    }
    user.setState(userState);
  }

  private void initTest() {
    instance = new ComponentAccessController(controller);
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }
}

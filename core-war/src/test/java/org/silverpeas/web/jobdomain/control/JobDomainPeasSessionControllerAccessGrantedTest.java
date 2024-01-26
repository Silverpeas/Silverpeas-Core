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

package org.silverpeas.web.jobdomain.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.ComponentContext;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.admin.domain.model.Domain.MIXED_DOMAIN_ID;
import static org.silverpeas.core.admin.user.constant.UserAccessLevel.USER;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class JobDomainPeasSessionControllerAccessGrantedTest {

  private static final String LOGGED_USER_DOMAIN_ID = "26";
  private static final String OTHER_DOMAIN_ID = "38";
  private static final String USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID = "7";
  private static final String USER_ID_ON_OTHER_DOMAIN = "8";
  private static final String USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE = "9";
  private static final String GROUP_ID_ON_DOMAIN_OF_LOGGED_USER_ID = LOGGED_USER_DOMAIN_ID;
  private static final String GROUP_ID_ON_OTHER_DOMAIN = OTHER_DOMAIN_ID;
  private static final String GROUP_ID_ON_MIXED_DOMAIN = USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID;

  @TestManagedMock
  private Domain rightDomain;

  @TestManagedMock
  private Domain wrongDomain;

  @TestedBean
  private JobDomainPeasSessionController4Test controller;

  @BeforeEach
  void setup(@TestManagedMock OrganizationController orgaController,
      @TestManagedMock UserProvider userProvider) {
    when(rightDomain.getId()).thenReturn(LOGGED_USER_DOMAIN_ID);
    when(wrongDomain.getId()).thenReturn(OTHER_DOMAIN_ID);
    controller.setupDefaultLoggedUser();
    final Answer<Object> getUserAnswer = i -> {
      final String id = i.getArgument(0);
      final UserDetail user = mock(UserDetail.class);
      when(user.getId()).thenReturn(id);
      when(user.getDomainId()).thenAnswer(a -> {
        switch (id) {
          case USER_ID_ON_OTHER_DOMAIN:
            return OTHER_DOMAIN_ID;
          case USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE:
            return MIXED_DOMAIN_ID;
          default:
            return LOGGED_USER_DOMAIN_ID;
        }
      });
      return user;
    };
    when(userProvider.getUser(anyString())).thenAnswer(getUserAnswer);
    when(orgaController.getUserDetail(anyString())).thenAnswer(getUserAnswer);
    when(orgaController.getGroup(anyString())).thenAnswer(i -> {
      final String id = i.getArgument(0);
      final int index = id.indexOf("_");
      final String topLevelGroupId = index > 0 ? id.substring(0, index) : id;
      final Group group = mock(Group.class);
      when(group.getId()).thenReturn(id);
      when(group.getDomainId()).thenAnswer(a -> {
        switch (topLevelGroupId) {
          case GROUP_ID_ON_OTHER_DOMAIN:
            return OTHER_DOMAIN_ID;
          case GROUP_ID_ON_MIXED_DOMAIN:
            return MIXED_DOMAIN_ID;
          default:
            return LOGGED_USER_DOMAIN_ID;
        }
      });
      if (index > 0) {
        when(group.getSuperGroupId()).thenReturn(topLevelGroupId);
      }
      return group;
    });
    when(orgaController.getPathToGroup(anyString())).thenAnswer(i -> {
      final String id = i.getArgument(0);
      final int index = id.indexOf("_");
      if (index > 0) {
        return List.of(id.substring(0, index));
      } else {
        return List.of();
      }
    });
  }

  /**
   * DOMAIN
   */

  @DisplayName("User with admin access level has always granted access to a domain")
  @Test
  void adminDomainAccessGranted() {
    when(controller.getUserDetail().isAccessAdmin()).thenReturn(true);
    controller.checkDomainAccessGranted(MIXED_DOMAIN_ID);
    controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false);
    controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID);
    controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false);
    controller.checkDomainAccessGranted(OTHER_DOMAIN_ID);
    controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false);
  }

  @DisplayName("User with user access level has never granted access to a domain")
  @Test
  void simpleUserDomainAccessNotGranted() {
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false));
  }

  @DisplayName("User with domain access level, without managed group, has granted access if " +
      "accessed domain corresponds to its domain")
  @Test
  void domainAdminUserDomainAccess() {
    when(controller.getUserDetail().isAccessDomainManager()).thenReturn(true);
    assertDomainAdminUserDomainAccess();
    when(controller.getUserDetail().isDomainAdminRestricted()).thenReturn(true);
    assertDomainAdminUserDomainAccess();
  }

  private void assertDomainAdminUserDomainAccess() {
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID);
    controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false);
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false));
  }

  @DisplayName("User with domain access level, and with managed groups, has granted access if " +
      "accessed domain does not correspond to its domain")
  @Test
  void domainAdminUserDomainAccessWithManageableGroups() {
    when(controller.getUserDetail().isAccessDomainManager()).thenReturn(true);
    when(controller.getUserDetail().getDomainId()).thenReturn(OTHER_DOMAIN_ID);
    assertDomainAdminUserDomainAccessWithManageableGroups(false);
    when(controller.getUserDetail().isDomainAdminRestricted()).thenReturn(true);
    assertDomainAdminUserDomainAccessWithManageableGroups(true);
  }

  private void assertDomainAdminUserDomainAccessWithManageableGroups(boolean domainRestricted) {
    controller.setManageableGroupIds();
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    controller.checkDomainAccessGranted(OTHER_DOMAIN_ID);
    controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false);
    // setting now the managed groups on mixed domain
    controller.setManageableGroupIds(MIXED_DOMAIN_ID);
    controller.checkDomainAccessGranted(MIXED_DOMAIN_ID);
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    controller.checkDomainAccessGranted(OTHER_DOMAIN_ID);
    controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false);
    // setting now the managed groups on same domain of LOGGED user domain
    controller.setManageableGroupIds(LOGGED_USER_DOMAIN_ID);
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    if (domainRestricted) {
      assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID));
    } else {
      controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID);
    }
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    controller.checkDomainAccessGranted(OTHER_DOMAIN_ID);
    controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false);
    // setting now the managed groups on other domain than the one of LOGGED user domain
    controller.setManageableGroupIds(OTHER_DOMAIN_ID);
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    controller.checkDomainAccessGranted(OTHER_DOMAIN_ID);
    controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false);
  }

  @DisplayName("User with user access level, and with managed groups, has granted access if " +
      "accessed domain does not correspond to its domain")
  @Test
  void simpleUserDomainAccessWithManageableGroups() {
    when(controller.getUserDetail().getDomainId()).thenReturn(OTHER_DOMAIN_ID);
    assertSimpleUserDomainAccessWithManageableGroups(false);
    when(controller.getUserDetail().isDomainAdminRestricted()).thenReturn(true);
    assertSimpleUserDomainAccessWithManageableGroups(true);
  }

  private void assertSimpleUserDomainAccessWithManageableGroups(boolean domainRestricted) {
    controller.setManageableGroupIds();
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false));
    // setting now the managed groups on mixed domain
    controller.setManageableGroupIds(MIXED_DOMAIN_ID);
    controller.checkDomainAccessGranted(MIXED_DOMAIN_ID);
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false));
    // setting now the managed groups on same domain of LOGGED user domain
    controller.setManageableGroupIds(LOGGED_USER_DOMAIN_ID);
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    if (domainRestricted) {
      assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID));
    } else {
      controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID);
    }
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false));
    // setting now the managed groups on other domain than the one of LOGGED user domain
    controller.setManageableGroupIds(OTHER_DOMAIN_ID);
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    controller.checkDomainAccessGranted(OTHER_DOMAIN_ID);
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false));
  }

  @DisplayName("User with user access level, without managed groups and with space management " +
      "right, has granted access in read only mode")
  @Test
  void simpleUserDomainAccessWhenSpaceManager() {
    controller.setManageableSpaceIds();
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false));
    controller.setManageableSpaceIds("3");
    controller.checkDomainAccessGranted(MIXED_DOMAIN_ID);
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID);
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false));
  }

  @DisplayName("User with user access level, without managed groups and community manager " +
      "right, has granted access in read only mode")
  @Test
  void simpleUserDomainAccessWhenCommunityManager() {
    controller.setCommunityManager(true);
    controller.checkDomainAccessGranted(MIXED_DOMAIN_ID);
    assertForbidden(() -> controller.checkDomainAccessGranted(MIXED_DOMAIN_ID, false));
    controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID);
    assertForbidden(() -> controller.checkDomainAccessGranted(LOGGED_USER_DOMAIN_ID, false));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID));
    assertForbidden(() -> controller.checkDomainAccessGranted(OTHER_DOMAIN_ID, false));
  }

  /**
   * USER
   */

  @DisplayName("When admin modifying a user, domain access in write mode is verified and " +
      "target domain MUST be set to the right domain")
  @Test
  void adminUserAccessGranted() {
    controller.setManageableGroupIds(GROUP_ID_ON_MIXED_DOMAIN, GROUP_ID_ON_DOMAIN_OF_LOGGED_USER_ID,
        GROUP_ID_ON_OTHER_DOMAIN);
    assertAdminUserAccessGranted();
    controller.setManageableSpaceIds("3");
    assertAdminUserAccessGranted();
  }

  private void assertAdminUserAccessGranted() {
    controller.setTargetDomain(null);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, false));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, false));
    // target domain is the mixed one
    controller.setTargetDomain(MIXED_DOMAIN_ID);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, false));
    controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false));
    controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, true);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, false));
    // target domain is the one of the admin
    controller.setTargetDomain(LOGGED_USER_DOMAIN_ID);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, false));
    controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, false));
    // target domain is not the one of the admin
    controller.setTargetDomain(OTHER_DOMAIN_ID);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, false));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false));
    controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, true);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, false));
  }

  @DisplayName("When user has domain access level, DATA from mixed domain can be read but not written")
  @Test
  void groupManagerOnMixedDomainOnlyHasUserReadOnlyAccessGranted() {
    controller.setManageableGroupIds(GROUP_ID_ON_MIXED_DOMAIN);
    assertGroupManagerOnMixedDomainHasUserAccessGranted();
    controller.setManageableSpaceIds("3");
    assertGroupManagerOnMixedDomainHasUserAccessGranted();
  }

  private void assertGroupManagerOnMixedDomainHasUserAccessGranted() {
    controller.setTargetDomain(null);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, false));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, false));
    // target domain is the mixed one
    controller.setTargetDomain(MIXED_DOMAIN_ID);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, false));
    controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false));
    controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, true);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, false));
    // target domain is the one of the admin
    controller.setTargetDomain(LOGGED_USER_DOMAIN_ID);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, false));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, false));
    // target domain is not the one of the admin
    controller.setTargetDomain(OTHER_DOMAIN_ID);
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_MIXED_DOMAIN_EVEN_IF_NOT_POSSIBLE, false));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, true));
    assertForbidden(() -> controller.checkUserAccessGranted(USER_ID_ON_OTHER_DOMAIN, false));
  }

  /**
   * GROUP
   */

  @DisplayName("When admin modifying a group, domain access in write mode is verified and " +
      "target domain MUST be set to the right domain")
  @Test
  void adminGroupAccessGranted() {
    controller.setManageableGroupIds(GROUP_ID_ON_MIXED_DOMAIN, GROUP_ID_ON_DOMAIN_OF_LOGGED_USER_ID,
        GROUP_ID_ON_OTHER_DOMAIN);
    assertAdminGroupAccessGranted();
    controller.setManageableSpaceIds("4");
    assertAdminUserAccessGranted();
  }

  private void assertAdminGroupAccessGranted() {
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_MIXED_DOMAIN, true));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_MIXED_DOMAIN, false));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN, true));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN, false));
    // target domain is the mixed one
    controller.setTargetDomain(MIXED_DOMAIN_ID);
    controller.checkGroupAccessGranted(GROUP_ID_ON_MIXED_DOMAIN, true);
    controller.checkGroupAccessGranted(GROUP_ID_ON_MIXED_DOMAIN, false);
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN, true));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN, false));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN + "_sub", true));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN + "_sub", false));
    // target domain is the one of the admin
    controller.setTargetDomain(LOGGED_USER_DOMAIN_ID);
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_MIXED_DOMAIN, true));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_MIXED_DOMAIN, false));
    controller.checkGroupAccessGranted(GROUP_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true);
    controller.checkGroupAccessGranted(GROUP_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false);
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN, true));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN, false));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN + "_sub", true));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN + "_sub", false));
    // target domain is not the one of the admin
    controller.setTargetDomain(OTHER_DOMAIN_ID);
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_MIXED_DOMAIN, true));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_MIXED_DOMAIN, false));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_DOMAIN_OF_LOGGED_USER_ID, true));
    assertForbidden(() -> controller.checkGroupAccessGranted(GROUP_ID_ON_DOMAIN_OF_LOGGED_USER_ID, false));
    controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN, true);
    controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN, false);
    controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN + "_sub", true);
    controller.checkGroupAccessGranted(GROUP_ID_ON_OTHER_DOMAIN + "_sub", false);
  }

  private void assertForbidden(final Executable executable) {
    assertThrows(ForbiddenRuntimeException.class, executable);
  }

  private static class JobDomainPeasSessionController4Test extends JobDomainPeasSessionController {
    private static final long serialVersionUID = -2464677549792058075L;

    private final UserDetail loggedUser = mock(UserDetail.class);
    private boolean communityManager = false;
    private String[] manageableSpaceIds = new String[]{};
    private List<String> manageableGroupIds = List.of();
    private Domain targetDomain = null;

    public JobDomainPeasSessionController4Test() {
      super(null, mockedContext(), null, null, null);
    }

    private static ComponentContext mockedContext() {
      final ComponentContext mock = mock(ComponentContext.class);
      when(mock.getCurrentComponentName()).thenReturn("unknown");
      return mock;
    }

    private void setupDefaultLoggedUser() {
      when(loggedUser.getId()).thenReturn("2");
      when(loggedUser.getDomainId()).thenReturn(LOGGED_USER_DOMAIN_ID);
      when(loggedUser.getAccessLevel()).thenReturn(USER);
    }

    public void setCommunityManager(final boolean communityManager) {
      this.communityManager = communityManager;
    }

    public void setManageableSpaceIds(String... spaceIds) {
      manageableSpaceIds = spaceIds;
    }

    /**
     * Sets the manageable group identifiers.
     * <p>
     * IMPORTANT: the given identifier is used to set domain id too. So giving the identifier
     * of domain.
     * </p>
     * @param groupIds several identifiers.
     */
    public void setManageableGroupIds(String... groupIds) {
      manageableGroupIds = List.of(groupIds);
    }

    @Override
    public void setTargetDomain(final String targetDomainId) {
      if (StringUtil.isDefined(targetDomainId)) {
        this.targetDomain = mock(Domain.class);
        when(this.targetDomain.getId()).thenReturn(targetDomainId);
      } else {
        this.targetDomain = null;
      }
    }

    @Override
    public Domain getTargetDomain() {
      return targetDomain;
    }

    @Override
    public boolean isCommunityManager() {
      return communityManager;
    }

    @Override
    public UserDetail getUserDetail() {
      return loggedUser;
    }

    @Override
    protected String[] getUserManageableSpaceIds() {
      return manageableSpaceIds;
    }

    @Override
    protected List<String> getUserManageableGroupIds() {
      return manageableGroupIds;
    }

    @Override
    public List<Group> getUserManageableGroups() {
      return manageableGroupIds.stream().map(i -> {
        Group group = mock(Group.class);
        when(group.getId()).thenReturn(i);
        when(group.getDomainId()).thenReturn(
            GROUP_ID_ON_MIXED_DOMAIN.equals(i) ? null : i);
        return group;
      }).collect(toList());
    }
  }
}
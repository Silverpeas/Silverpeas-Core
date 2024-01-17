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

package org.silverpeas.web.jobstartpage.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedMock;
import org.silverpeas.core.test.unit.extention.TestedBean;
import org.silverpeas.core.web.mvc.controller.ComponentContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.admin.space.SpaceInst.SPACE_KEY_PREFIX;
import static org.silverpeas.core.admin.user.constant.UserAccessLevel.USER;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class JobStartPagePeasSessionControllerTest {

  private static final String LOGGED_USER_DOMAIN_ID = "26";

  private static final String SPACE_A = "WA1";
  private static final String SPACE_A_A1 = "WA11";
  private static final String SPACE_A_A1_A11 = "WA111";
  private static final String SPACE_A_A2 = "WA12";
  private static final String SPACE_B = "WA2";
  private static final List<String> ALL_SPACE_IDS = List.of(
      SPACE_A, SPACE_A_A1, SPACE_A_A1_A11, SPACE_A_A2,
      SPACE_B);
  private static final Map<String, String> SPACE_SPACE = Map.of(
      SPACE_A_A1, SPACE_A,
      SPACE_A_A1_A11, SPACE_A_A1,
      SPACE_A_A2, SPACE_A);

  private static final String INSTANCE_AA = "kmelia1";
  private static final String INSTANCE_A_A1A = "kmelia11";
  private static final String INSTANCE_A_A1B = "kmelia12";
  private static final String INSTANCE_A_A1_A11A = "kmelia111";
  private static final String INSTANCE_BA = "kmelia2";
  private static final List<String> ALL_INSTANCE_IDS = List.of(
      INSTANCE_AA, INSTANCE_A_A1A, INSTANCE_A_A1B, INSTANCE_A_A1_A11A,
      INSTANCE_BA);
  private static final Map<String, String> INSTANCE_SPACE = Map.of(
      INSTANCE_AA, SPACE_A,
      INSTANCE_A_A1A, SPACE_A_A1,
      INSTANCE_A_A1B, SPACE_A_A1,
      INSTANCE_A_A1_A11A, SPACE_A_A1_A11,
      INSTANCE_BA, SPACE_B);

  @TestedBean
  private JobStartPagePeasSessionController4Test controller;

  @BeforeEach
  void setup(@TestManagedMock OrganizationController orgaController) {
    when(orgaController.getSpaceInstById(anyString())).thenAnswer(i -> {
      final String id = i.getArgument(0);
      final SpaceInst space = mock(SpaceInst.class);
      when(space.getId()).thenReturn(id);
      when(space.getLocalId()).thenReturn(parseInt(id.substring(SPACE_KEY_PREFIX.length())));
      ofNullable(SPACE_SPACE.get(id)).ifPresentOrElse(
          p -> when(space.getDomainFatherId()).thenReturn(p),
          () -> when(space.isRoot()).thenReturn(true));
      return space;
    });
    when(orgaController.getSpaceInstLightById(anyString())).thenAnswer(i -> {
      final String id = i.getArgument(0);
      final SpaceInstLight space = mock(SpaceInstLight.class);
      when(space.getId()).thenReturn(id);
      when(space.getLocalId()).thenReturn(parseInt(id.substring(SPACE_KEY_PREFIX.length())));
      ofNullable(SPACE_SPACE.get(id)).ifPresentOrElse(
          p -> when(space.getFatherId()).thenReturn(p),
          () -> when(space.isRoot()).thenReturn(true));
      return space;
    });
    when(orgaController.getComponentInstance(anyString())).thenAnswer(i -> {
      final String id = i.getArgument(0);
      final SilverpeasComponentInstance instance = mock(SilverpeasComponentInstance.class);
      when(instance.getId()).thenReturn(id);
      ofNullable(INSTANCE_SPACE.get(id)).ifPresent(p -> when(instance.getSpaceId()).thenReturn(p));
      return of(instance);
    });
    when(orgaController.getPathToSpace(anyString())).thenAnswer(i -> {
      final String id = i.getArgument(0, String.class);
      List<SpaceInstLight> path = new ArrayList<>();
      SpaceInstLight spaceInst = orgaController.getSpaceInstLightById(id);
      if (spaceInst != null) {
        if (!spaceInst.isRoot()) {
          path.addAll(orgaController.getPathToSpace(spaceInst.getFatherId()));
        }
        path.add(0, spaceInst);
      }
      return path;
    });
  }

  @DisplayName("User with admin access level has always granted access to spaces and component " +
      "instances, even if space and instance ids does not exist")
  @Test
  void adminOfSpaceAndInstanceAccessGranted() {
    when(controller.getUserDetail().isAccessAdmin()).thenReturn(true);
    Stream.concat(Stream.of((String) null), ALL_SPACE_IDS.stream())
        .forEach(s -> Stream.concat(Stream.of((String) null), ALL_INSTANCE_IDS.stream())
            .forEach(i -> {
              controller.checkAccessGranted(s, i, true);
              controller.checkAccessGranted(s, i, false);
            }));
  }

  @DisplayName(
      "User with user access level has never granted access to spaces and component instances")
  @Test
  void simpleUserOfSpaceAndInstanceAccessNotGranted() {
    Stream.concat(Stream.of((String) null), ALL_SPACE_IDS.stream())
        .forEach(s -> Stream.concat(Stream.of((String) null), ALL_INSTANCE_IDS.stream())
            .forEach(i -> {
              assertForbidden(() -> controller.checkAccessGranted(s, i, true));
              assertForbidden(() -> controller.checkAccessGranted(s, i, false));
            }));
  }

  @DisplayName("Manager has granted access to aimed spaces")
  @Test
  void managerOfSpaceAccessGranted() {
    // ON SPACE A
    controller.setManageableSpaceIds(SPACE_A);
    controller.checkAccessGranted(null, null, true);
    assertForbidden(() -> controller.checkAccessGranted(null, null, false));
    controller.checkAccessGranted(SPACE_A, null, true);
    controller.checkAccessGranted(SPACE_A, null, false);
    controller.checkAccessGranted(SPACE_A_A1, null, true);
    controller.checkAccessGranted(SPACE_A_A1, null, false);
    controller.checkAccessGranted(SPACE_A_A1_A11, null, true);
    controller.checkAccessGranted(SPACE_A_A1_A11, null, false);
    controller.checkAccessGranted(SPACE_A_A2, null, true);
    controller.checkAccessGranted(SPACE_A_A2, null, false);
    assertForbidden(() -> controller.checkAccessGranted(SPACE_B, null, true));
    assertForbidden(() -> controller.checkAccessGranted(SPACE_B, null, false));
    // ON SPACE A_A1
    controller.setManageableSpaceIds(SPACE_A_A1);
    controller.checkAccessGranted(null, null, true);
    assertForbidden(() -> controller.checkAccessGranted(null, null, false));
    controller.checkAccessGranted(SPACE_A, null, true);
    assertForbidden(() -> controller.checkAccessGranted(SPACE_A, null, false));
    controller.checkAccessGranted(SPACE_A_A1, null, true);
    controller.checkAccessGranted(SPACE_A_A1, null, false);
    controller.checkAccessGranted(SPACE_A_A1_A11, null, true);
    controller.checkAccessGranted(SPACE_A_A1_A11, null, false);
    assertForbidden(() -> controller.checkAccessGranted(SPACE_A_A2, null, true));
    assertForbidden(() -> controller.checkAccessGranted(SPACE_A_A2, null, false));
    assertForbidden(() -> controller.checkAccessGranted(SPACE_B, null, true));
    assertForbidden(() -> controller.checkAccessGranted(SPACE_B, null, false));
  }

  @DisplayName("Manager has granted access to aimed components")
  @Test
  void managerOfSpaceComponentAccessGranted() {
    // ON SPACE A
    controller.setManageableSpaceIds(SPACE_A);
    controller.checkAccessGranted(null, null, true);
    assertForbidden(() -> controller.checkAccessGranted(null, null, false));
    controller.checkAccessGranted(null, INSTANCE_AA, true);
    controller.checkAccessGranted(null, INSTANCE_AA, false);
    controller.checkAccessGranted(null, INSTANCE_A_A1A, true);
    controller.checkAccessGranted(null, INSTANCE_A_A1A, false);
    controller.checkAccessGranted(null, INSTANCE_A_A1_A11A, true);
    controller.checkAccessGranted(null, INSTANCE_A_A1_A11A, false);
    controller.checkAccessGranted(null, INSTANCE_A_A1B, true);
    controller.checkAccessGranted(null, INSTANCE_A_A1B, false);
    assertForbidden(() -> controller.checkAccessGranted(null, INSTANCE_BA, true));
    assertForbidden(() -> controller.checkAccessGranted(null, INSTANCE_BA, false));
    // ON SPACE A_A1
    controller.setManageableSpaceIds(SPACE_A_A1);
    controller.checkAccessGranted(null, null, true);
    assertForbidden(() -> controller.checkAccessGranted(null, null, false));
    assertForbidden(() -> controller.checkAccessGranted(null, INSTANCE_AA, true));
    assertForbidden(() -> controller.checkAccessGranted(null, INSTANCE_AA, false));
    controller.checkAccessGranted(null, INSTANCE_A_A1A, true);
    controller.checkAccessGranted(null, INSTANCE_A_A1A, false);
    controller.checkAccessGranted(null, INSTANCE_A_A1_A11A, true);
    controller.checkAccessGranted(null, INSTANCE_A_A1_A11A, false);
    controller.checkAccessGranted(null, INSTANCE_A_A1B, true);
    controller.checkAccessGranted(null, INSTANCE_A_A1B, false);
    assertForbidden(() -> controller.checkAccessGranted(null, INSTANCE_BA, true));
    assertForbidden(() -> controller.checkAccessGranted(null, INSTANCE_BA, false));
  }

  @DisplayName("Manager has granted access if space and instance ids can be accessed")
  @Test
  void managerOfSpaceAndComponentAccessGranted() {
    // ON SPACE A
    controller.setManageableSpaceIds(SPACE_A);
    controller.checkAccessGranted(null, INSTANCE_A_A1A, false);
    controller.checkAccessGranted(SPACE_A, INSTANCE_A_A1_A11A, false);
    controller.checkAccessGranted(SPACE_A, INSTANCE_A_A1B, false);
    assertForbidden(() -> controller.checkAccessGranted(SPACE_A, INSTANCE_BA, false));
    // ON SPACE A_A1
    controller.setManageableSpaceIds(SPACE_A_A1);
    controller.checkAccessGranted(null, INSTANCE_A_A1A, false);
    assertForbidden(() -> controller.checkAccessGranted(SPACE_A, INSTANCE_A_A1_A11A, false));
    assertForbidden(() -> controller.checkAccessGranted(SPACE_A, INSTANCE_A_A1B, false));
    assertForbidden(() -> controller.checkAccessGranted(SPACE_A, INSTANCE_BA, false));
  }

  private void assertForbidden(final Executable executable) {
    assertThrows(ForbiddenRuntimeException.class, executable);
  }

  private static class JobStartPagePeasSessionController4Test
      extends JobStartPagePeasSessionController {
    private static final long serialVersionUID = 101781645406476047L;

    private final UserDetail loggedUser = mock(UserDetail.class);
    private String[] manageableSpaceIds = new String[]{};

    public JobStartPagePeasSessionController4Test() {
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

    public void setManageableSpaceIds(String... spaceIds) {
      final List<String> allIds = new ArrayList<>();
      List<String> currents = List.of(spaceIds);
      while (!currents.isEmpty()) {
        allIds.addAll(currents);
        currents = SPACE_SPACE.entrySet()
            .stream()
            .filter(e -> allIds.contains(e.getValue()))
            .map(Map.Entry::getKey)
            .filter(not(allIds::contains))
            .collect(Collectors.toList());
      }
      manageableSpaceIds = allIds.toArray(new String[0]);
    }

    @Override
    public UserDetail getUserDetail() {
      return loggedUser;
    }

    @Override
    protected String[] getUserManageableSpaceIds() {
      return manageableSpaceIds;
    }
  }
}
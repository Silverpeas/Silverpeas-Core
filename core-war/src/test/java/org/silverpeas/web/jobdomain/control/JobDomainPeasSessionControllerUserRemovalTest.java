/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.web.jobdomain.JobDomainPeasException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the removal of a user by the {@link JobDomainPeasSessionController}.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class JobDomainPeasSessionControllerUserRemovalTest {

  private static final String USER_ID = "7";

  @TestManagedMock
  private AdminController adminController;

  @TestedBean
  private JobDomainPeasSessionController4Test controller;

  @BeforeEach
  void setup(@TestManagedMock UserProvider userProvider) {
    when(userProvider.getUser(anyString())).thenAnswer(i -> {
      final UserDetail user = mock(UserDetail.class);
      when(user.getId()).thenReturn(i.getArgument(0));
      return user;
    });
    controller.setTargetUser(USER_ID);
  }

  @Test
  @DisplayName("Removing a user removes only this user")
  void removeOnlyTheGivenUser() {
    when(adminController.removeUser(anyString())).thenAnswer(i -> i.getArgument(0));

    assertDoesNotThrow(() -> controller.removeUser(USER_ID));

    verify(adminController).removeUser(USER_ID);
    verifyNoMoreInteractions(adminController);
  }

  @Test
  @DisplayName("A failure in the removal of a user is reported")
  void failureInTheUserRemovalIsReported() {
    when(adminController.removeUser(anyString())).thenReturn("");

    assertThrows(JobDomainPeasException.class, () -> controller.removeUser(USER_ID));

    verify(adminController).removeUser(USER_ID);
    verifyNoMoreInteractions(adminController);
  }

  private static class JobDomainPeasSessionController4Test extends JobDomainPeasSessionController {
    private static final long serialVersionUID = 4466011869325946386L;

    public JobDomainPeasSessionController4Test() {
      super(null, mockedContext(), null, null, null);
    }

    private static ComponentContext mockedContext() {
      final ComponentContext mock = mock(ComponentContext.class);
      when(mock.getCurrentComponentName()).thenReturn("unknown");
      return mock;
    }

    @Override
    public void checkUserAccessGranted(final String userId, final boolean readOnly) {
      // access always granted: not the concern of these tests
    }
  }
}

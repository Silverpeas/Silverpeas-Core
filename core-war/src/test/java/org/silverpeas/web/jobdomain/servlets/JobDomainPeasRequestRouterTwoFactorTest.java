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
package org.silverpeas.web.jobdomain.servlets;

import jakarta.ws.rs.NotAllowedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.web.jobdomain.control.JobDomainPeasSessionController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableSilverTestEnv(context = JEETestContext.class)
class JobDomainPeasRequestRouterTwoFactorTest {

  private final JobDomainPeasRequestRouter router = new JobDomainPeasRequestRouter();
  private final JobDomainPeasSessionController controller =
      mock(JobDomainPeasSessionController.class);
  private final HttpRequest request = mock(HttpRequest.class);

  @ParameterizedTest
  @ValueSource(strings = {"GET", "HEAD", "PUT", "DELETE", "OPTIONS"})
  void shouldRejectResetWithoutPost(String method) throws Exception {
    when(request.getMethod()).thenReturn(method);
    when(request.getParameter("Iduser")).thenReturn("42");
    when(request.getParameter("X-ATKN")).thenReturn("admin-token");

    assertEquals("/admin/jsp/errorpageMain.jsp",
        router.getAdminDestination("userResetTwoFactor", controller, request));

    verify(controller, never()).securelyApply(any(), any());
    verify(controller, never()).resetTwoFactorAuthentication(any());
    verify(request).setAttribute(eq("jakarta.servlet.jsp.jspException"),
        isA(NotAllowedException.class));
  }

  @Test
  void shouldResetByPostAfterValidatingAdminTokenAndAccess() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getParameter("Iduser")).thenReturn("42");
    when(request.getParameter("X-ATKN")).thenReturn("admin-token");
    doAnswer(invocation -> {
      invocation.getArgument(1, JobDomainPeasSessionController.AdminTask.class).run();
      return null;
    }).when(controller).securelyApply(eq("admin-token"), any());

    assertEquals("/jobDomainPeas/jsp/userContent.jsp",
        router.getAdminDestination("userResetTwoFactor", controller, request));

    verify(controller).checkUserAccessGranted("42", false);
    verify(controller).securelyApply(eq("admin-token"), any());
    verify(controller).resetTwoFactorAuthentication("42");
  }
}

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
package org.silverpeas.core.web.rs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorAuthenticationService;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.totp.TotpService;
import org.silverpeas.core.web.token.SynchronizerTokenService;
import org.silverpeas.core.webapi.twofactor.QrCodeGenerator;
import org.silverpeas.core.webapi.profile.AuthenticationResource;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class AuthenticationResourceChallengeTest {
  @TestManagedMock private UserPrivilegeValidation validation;
  @TestManagedMock private AuthenticationService authenticationService;
  @TestManagedMock private OrganizationController organizationController;
  @TestManagedMock private RESTRequestContext restRequestContext;
  @TestManagedMock private SessionManagement sessionManagement;
  @TestManagedMock private SynchronizerTokenService tokenService;
  @TestManagedMock private TwoFactorAuthenticationService twoFactorService;
  @TestManagedMock private TotpService totpService;
  @TestManagedMock private QrCodeGenerator qrCodeGenerator;
  @TestedBean private AuthenticationResource resource;

  @BeforeEach
  void setUp() {
    resource = spy(resource);
  }

  @Test
  void shouldReturnChallengeOnlyWhenSecondFactorWasRequested() {
    HttpServletRequest request = requestWithChallenge();
    doReturn(request).when(resource).getHttpServletRequest();
    doThrow(new WebApplicationException(401)).when(resource).validateUserAuthentication(any());
    var response = resource.authenticate();
    assertEquals(401, response.getStatus());
    assertEquals("true", response.getHeaderString("X-Silverpeas-2FA-Required"));
  }

  @Test
  void shouldPropagateOrdinaryUnauthorizedAndBackendErrors() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    doReturn(request).when(resource).getHttpServletRequest();
    doThrow(new WebApplicationException(401)).when(resource).validateUserAuthentication(any());
    assertEquals(401, assertThrows(WebApplicationException.class,
        resource::authenticate).getResponse().getStatus());
    request = requestWithChallenge();
    doReturn(request).when(resource).getHttpServletRequest();
    doThrow(new WebApplicationException(503)).when(resource).validateUserAuthentication(any());
    assertEquals(503, assertThrows(WebApplicationException.class,
        resource::authenticate).getResponse().getStatus());
  }

  @Test
  void shouldRejectSecondFactorWithoutPendingFirstFactor() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    doReturn(request).when(resource).getHttpServletRequest();
    var code = new AuthenticationResource.TwoFactorCodeRequest();
    code.setCode("123456");
    assertEquals(401, resource.authenticateTwoFactor(code).getStatus());
    assertEquals(401, resource.authenticateTrustedDevice().getStatus());
    assertEquals(401, resource.startEnrollment().getStatus());
    assertEquals(401, resource.confirmEnrollment(code).getStatus());
    verifyNoInteractions(authenticationService, twoFactorService);
  }

  private HttpServletRequest requestWithChallenge() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    Map<String, Object> state = new HashMap<>();
    Map<String, Object> attributes = new HashMap<>();
    when(request.getSession(anyBoolean())).thenReturn(session);
    when(request.getAttribute(anyString())).thenAnswer(i -> attributes.get(i.getArgument(0)));
    doAnswer(i -> { attributes.put(i.getArgument(0), i.getArgument(1)); return null; })
        .when(request).setAttribute(anyString(), any());
    when(session.getAttribute(anyString())).thenAnswer(i -> state.get(i.getArgument(0)));
    doAnswer(i -> { state.put(i.getArgument(0), i.getArgument(1)); return null; })
        .when(session).setAttribute(anyString(), any());
    RestTwoFactorChallenge.start(request, "alice", "0");
    return request;
  }
}

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
package org.silverpeas.core.web.authentication;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationResponse;
import org.silverpeas.core.security.totp.TotpService;
import org.silverpeas.core.webapi.twofactor.QrCodeGenerator;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class AuthenticationServletTwoFactorTest {
  @TestManagedMock private AuthenticationService authenticationService;
  @TestManagedMock private SilverpeasSessionOpener opener;
  @TestManagedMock private CredentialEncryption credentialEncryption;
  @TestManagedMock private MandatoryQuestionChecker questionChecker;
  @TestManagedMock private TotpService totpService;
  @TestManagedMock private QrCodeGenerator qrCodeGenerator;
  @TestedBean private AuthenticationServlet servlet;

  @Test
  void shouldKeepEnrollmentDisplayAfterInvalidCode() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpSession session = mock(HttpSession.class);
    RequestDispatcher dispatcher = mock(RequestDispatcher.class);
    Map<String, Object> state = new HashMap<>();
    state.put("Silverpeas_TwoFactor_Login", "alice");
    state.put("Silverpeas_TwoFactor_Domain", "0");
    state.put("Silverpeas_TwoFactor_ExpiresAt", System.currentTimeMillis() + 60000L);
    state.put("Silverpeas_TwoFactor_Attempts", 0);
    state.put("Silverpeas_TwoFactor_Enrollment", true);
    state.put("Silverpeas_TwoFactor_OtpUri", "otpauth://totp/alice");
    state.put("Silverpeas_TwoFactor_Secret", "pending-secret");
    state.put("Silverpeas_TwoFactor_QrCode", "pending-qr-code");
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute(anyString())).thenAnswer(i -> state.get(i.getArgument(0)));
    doAnswer(i -> { state.put(i.getArgument(0), i.getArgument(1)); return null; })
        .when(session).setAttribute(anyString(), any());
    when(request.getParameter("TwoFactorCode")).thenReturn("000000");
    when(request.getRequestDispatcher("/twoFactorAuthentication.jsp")).thenReturn(dispatcher);
    when(authenticationService.authenticateTwoFactorEnrollment("alice", "0", "000000"))
        .thenReturn(AuthenticationResponse.error(AuthenticationResponse.Status.TWO_FACTOR_REQUIRED));
    servlet.doPost(request, response);
    verify(request).setAttribute("twoFactorError", true);
    verify(request).setAttribute("twoFactorEnrollment", true);
    verify(request).setAttribute("twoFactorSecret", "pending-secret");
    verify(request).setAttribute("twoFactorQrCode", "pending-qr-code");
    verify(request).setAttribute(eq("twoFactorTrustedDeviceEnabled"), anyBoolean());
    verify(dispatcher).forward(request, response);
    verify(session).setAttribute("Silverpeas_TwoFactor_Attempts", 1);
  }
}

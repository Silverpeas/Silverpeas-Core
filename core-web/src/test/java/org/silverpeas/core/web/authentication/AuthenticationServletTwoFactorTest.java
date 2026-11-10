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
import jakarta.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationResponse;
import org.silverpeas.core.security.totp.TotpService;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.authentication.verifier.UserCanTryAgainToLoginVerifier;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
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
    state.put("Silverpeas_TwoFactor_QrCode", new byte[] {1, 2, 3});
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
    verify(request, never()).setAttribute(eq("twoFactorQrCode"), any());
    verify(request).setAttribute(eq("twoFactorTrustedDeviceEnabled"), anyBoolean());
    verify(dispatcher).forward(request, response);
    verify(session).setAttribute("Silverpeas_TwoFactor_Attempts", 1);
  }

  @Test
  void shouldServeOnlyTheCurrentEnrollmentImageWithoutCachingOrCreatingASession() throws Exception {
    Map<String, Object> state = enrollmentState();
    byte[] png = (byte[]) state.get("Silverpeas_TwoFactor_QrCode");
    HttpServletRequest request = imageRequest(state);
    HttpServletResponse response = mock(HttpServletResponse.class);
    ByteArrayOutputStream body = captureBody(response);

    servlet.doGet(request, response);

    assertArrayEquals(png, body.toByteArray());
    assertEquals(256, ImageIO.read(new ByteArrayInputStream(body.toByteArray())).getWidth());
    verify(response).setContentType("image/png");
    verify(response).setContentLength(png.length);
    verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    verify(response).setHeader("Pragma", "no-cache");
    verify(response).setDateHeader("Expires", 0);
    verify(response).setHeader("X-Content-Type-Options", "nosniff");
    verify(response, never()).setStatus(anyInt());
    verify(request, never()).getSession();
    verify(request, never()).getSession(true);
    verifyNoInteractions(authenticationService, opener, totpService, qrCodeGenerator);
  }

  @ParameterizedTest
  @ValueSource(strings = {"no-session", "login", "domain", "expiry", "expired", "ordinary-challenge",
      "completed-enrollment", "image", "empty-image"})
  void shouldNotDiscloseAnImageWithoutAnActiveEnrollment(String missingRequirement) throws Exception {
    Map<String, Object> state = enrollmentState();
    switch (missingRequirement) {
      case "login" -> state.remove("Silverpeas_TwoFactor_Login");
      case "domain" -> state.remove("Silverpeas_TwoFactor_Domain");
      case "expiry" -> state.remove("Silverpeas_TwoFactor_ExpiresAt");
      case "expired" -> state.put("Silverpeas_TwoFactor_ExpiresAt", System.currentTimeMillis() - 1);
      case "ordinary-challenge" -> state.put("Silverpeas_TwoFactor_Enrollment", false);
      case "completed-enrollment" -> state.clear();
      case "image" -> state.remove("Silverpeas_TwoFactor_QrCode");
      case "empty-image" -> state.put("Silverpeas_TwoFactor_QrCode", new byte[0]);
      default -> { /* The request below has no HTTP session. */ }
    }
    HttpServletRequest request = imageRequest(state);
    if ("no-session".equals(missingRequirement)) {
      when(request.getSession(false)).thenReturn(null);
    }
    HttpServletResponse response = mock(HttpServletResponse.class);

    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    verify(response, never()).getOutputStream();
    verify(request, never()).getSession();
    verify(request, never()).getSession(true);
    verifyNoInteractions(authenticationService, opener, totpService, qrCodeGenerator);
  }

  @Test
  void loadingTheImageShouldNotExtendOrConsumeTheChallenge() throws Exception {
    Map<String, Object> state = enrollmentState();
    HttpServletRequest request = imageRequest(state);
    HttpSession session = request.getSession(false);
    HttpServletResponse response = mock(HttpServletResponse.class);
    captureBody(response);

    servlet.doGet(request, response);
    servlet.doGet(request, response);

    verify(session, never()).setAttribute(anyString(), any());
    verify(session, never()).removeAttribute(anyString());
    verify(response, times(2)).setContentType("image/png");
  }

  @Test
  void completingEnrollmentShouldRemoveAccessToItsImage() throws Exception {
    Map<String, Object> state = enrollmentState();
    HttpServletRequest request = imageRequest(state);
    HttpSession session = request.getSession(false);
    doAnswer(i -> { state.remove(i.getArgument(0)); return null; })
        .when(session).removeAttribute(anyString());
    when(request.getParameter("TwoFactorCode")).thenReturn("123456");
    when(request.getAttribute("skipTermsOfServiceAcceptance")).thenReturn(true);
    var result = mock(AuthenticationResponse.class);
    when(result.getStatus()).thenReturn(AuthenticationResponse.Status.SUCCESS);
    when(result.getToken()).thenReturn("authenticated-token");
    when(authenticationService.authenticateTwoFactorEnrollment("alice", "0", "123456"))
        .thenReturn(result);
    HttpServletResponse response = mock(HttpServletResponse.class);
    var verifier = mock(UserCanTryAgainToLoginVerifier.class);

    try (var parameters = mockConstruction(AuthenticationParameters.class, (parameter, context) -> {
           when(parameter.getLogin()).thenReturn("alice");
           when(parameter.getPassword()).thenReturn("");
         });
         var users = mockStatic(UserDetail.class, CALLS_REAL_METHODS);
         var verifiers = mockStatic(AuthenticationUserVerifierFactory.class)) {
      users.when(UserDetail::getAnonymousUser).thenReturn(null);
      verifiers.when(() -> AuthenticationUserVerifierFactory.getUserCanTryAgainToLoginVerifier(nullable(AuthenticationCredential.class)))
          .thenReturn(verifier);
      servlet.doPost(request, response);
      verify(opener).openSession(any(), eq("authenticated-token"));
    }
    HttpServletResponse imageResponse = mock(HttpServletResponse.class);

    servlet.doGet(request, imageResponse);

    verify(session).removeAttribute("Silverpeas_TwoFactor_QrCode");
    verify(imageResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(imageResponse, never()).getOutputStream();
  }

  @Test
  void theRequestCannotSelectAnotherSessionsImage() throws Exception {
    Map<String, Object> alice = enrollmentState();
    Map<String, Object> bob = enrollmentState();
    bob.put("Silverpeas_TwoFactor_Login", "bob");
    byte[] bobsPng = new QrCodeGenerator().generate(
        "otpauth://totp/bob?secret=GEZDGNBVGY3TQOJQ&issuer=Silverpeas", 256);
    bob.put("Silverpeas_TwoFactor_QrCode", bobsPng);
    HttpServletRequest aliceRequest = imageRequest(alice);
    // Supplying another login in the URL never selects that account's QR code.
    when(aliceRequest.getParameter("Login")).thenReturn("bob");
    HttpServletResponse aliceResponse = mock(HttpServletResponse.class);
    ByteArrayOutputStream alicesImage = captureBody(aliceResponse);
    HttpServletResponse bobResponse = mock(HttpServletResponse.class);
    ByteArrayOutputStream bobsImage = captureBody(bobResponse);

    servlet.doGet(aliceRequest, aliceResponse);
    servlet.doGet(imageRequest(bob), bobResponse);

    assertArrayEquals((byte[]) alice.get("Silverpeas_TwoFactor_QrCode"), alicesImage.toByteArray());
    assertArrayEquals(bobsPng, bobsImage.toByteArray());
    verifyNoInteractions(authenticationService);
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void imageWriteFailureShouldNotEscapeTheServlet(boolean committed) throws Exception {
    HttpServletRequest request = imageRequest(enrollmentState());
    HttpServletResponse response = mock(HttpServletResponse.class);
    ServletOutputStream output = mock(ServletOutputStream.class);
    when(response.getOutputStream()).thenReturn(output);
    when(response.isCommitted()).thenReturn(committed);
    doThrow(new IOException("Simulated image write failure"))
        .when(output).write(any(byte[].class));

    assertDoesNotThrow(() -> servlet.doGet(request, response));

    if (committed) {
      verify(response, never()).setStatus(anyInt());
    } else {
      verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    verify(response, never()).sendError(anyInt());
    verifyNoInteractions(authenticationService, opener, totpService);
  }

  private Map<String, Object> enrollmentState() {
    Map<String, Object> state = new HashMap<>();
    state.put("Silverpeas_TwoFactor_Login", "alice");
    state.put("Silverpeas_TwoFactor_Domain", "0");
    state.put("Silverpeas_TwoFactor_ExpiresAt", System.currentTimeMillis() + 60000L);
    state.put("Silverpeas_TwoFactor_Enrollment", true);
    state.put("Silverpeas_TwoFactor_QrCode", new QrCodeGenerator().generate(
        "otpauth://totp/alice?secret=JBSWY3DPEHPK3PXP&issuer=Silverpeas", 256));
    return state;
  }

  private HttpServletRequest imageRequest(Map<String, Object> state) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getParameter("TwoFactorResource")).thenReturn("qrcode");
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute(anyString())).thenAnswer(i -> state.get(i.getArgument(0)));
    return request;
  }

  private ByteArrayOutputStream captureBody(HttpServletResponse response) throws IOException {
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    ServletOutputStream output = mock(ServletOutputStream.class);
    when(response.getOutputStream()).thenReturn(output);
    doAnswer(i -> { body.write((byte[]) i.getArgument(0)); return null; })
        .when(output).write(any(byte[].class));
    return body;
  }

}

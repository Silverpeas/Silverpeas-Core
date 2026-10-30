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
package org.silverpeas.web.socialnetwork.myprofil.servlets;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.twofactor.MissingTwoFactorEncryptionKeyException;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorAuthenticationService;
import org.silverpeas.core.security.totp.TotpService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;
import org.silverpeas.core.web.socialnetwork.user.model.SNFullUser;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.web.socialnetwork.myprofil.control.MyProfilSessionController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class MyProfilTwoFactorEnrollmentTest {
  private static final String MESSAGE = "The Silverpeas content encryption key must be configured " +
      "before enrolling in two-factor authentication";
  private static final String SETTINGS = "/socialNetwork/jsp/myProfil/myProfile.jsp";

  @TestManagedMock
  private TwoFactorAuthenticationService twoFactorService;
  @TestManagedMock
  private TotpService totpService;
  @TestedBean
  private MyProfilRequestRouter router;

  @Test
  void missingKeyShouldShowOnlyFunctionalMessageAndWarningWithoutStackTrace() {
    router = spy(router);
    HttpRequest request = mock(HttpRequest.class);
    MyProfilSessionController controller = mock(MyProfilSessionController.class);
    when(controller.getUserDetail()).thenReturn(mock(UserDetail.class));
    when(controller.getUserId()).thenReturn("42");
    when(twoFactorService.startEnrollment(42))
        .thenThrow(new MissingTwoFactorEncryptionKeyException());
    doReturn(SETTINGS).when(router).getDestination("MySettings", controller, request);
    WebMessager messager = mock(WebMessager.class);
    SilverLogger logger = mock(SilverLogger.class);
    SettingBundle settings = mock(SettingBundle.class);
    when(settings.getBoolean("twoFactorTotpEnabled", false)).thenReturn(true);

    try (var resources = mockStatic(ResourceLocator.class, CALLS_REAL_METHODS);
         var messages = mockStatic(WebMessager.class);
         var logs = mockStatic(SilverLogger.class);
         var users = mockConstruction(SNFullUser.class)) {
      resources.when(() -> ResourceLocator.getSettingBundle(
          "org.silverpeas.authentication.settings.authenticationSettings")).thenReturn(settings);
      messages.when(WebMessager::getInstance).thenReturn(messager);
      logs.when(() -> SilverLogger.getLogger(router)).thenReturn(logger);

      assertEquals(SETTINGS, router.getDestination("StartTwoFactor", controller, request));

      verify(messager).addError(MESSAGE);
      verify(logger).warn(MESSAGE);
      verifyNoMoreInteractions(logger, messager);
      verifyNoInteractions(totpService);
      verify(request, never()).setAttribute(eq("jakarta.servlet.jsp.jspException"), any());
      verify(request, never()).setAttribute(eq("twoFactorAuthentication"), any());
    }
  }

  @Test
  void unexpectedEncryptionFailureShouldRemainATechnicalError() {
    router = spy(router);
    HttpRequest request = mock(HttpRequest.class);
    MyProfilSessionController controller = mock(MyProfilSessionController.class);
    SettingBundle settings = mock(SettingBundle.class);
    when(controller.getUserDetail()).thenReturn(mock(UserDetail.class));
    when(controller.getUserId()).thenReturn("42");
    when(controller.getSettings()).thenReturn(settings);
    when(settings.getBoolean("twoFactorTotpEnabled", false)).thenReturn(true);
    IllegalStateException failure = new IllegalStateException("Unable to encrypt the secret");
    when(twoFactorService.startEnrollment(42)).thenThrow(failure);
    WebMessager messager = mock(WebMessager.class);
    SilverLogger logger = mock(SilverLogger.class);

    try (var resources = mockStatic(ResourceLocator.class, CALLS_REAL_METHODS);
         var messages = mockStatic(WebMessager.class);
         var logs = mockStatic(SilverLogger.class);
         var users = mockConstruction(SNFullUser.class)) {
      resources.when(() -> ResourceLocator.getSettingBundle(
          "org.silverpeas.authentication.settings.authenticationSettings")).thenReturn(settings);
      messages.when(WebMessager::getInstance).thenReturn(messager);
      logs.when(() -> SilverLogger.getLogger(router)).thenReturn(logger);

      assertEquals("/admin/jsp/errorpageMain.jsp",
          router.getDestination("StartTwoFactor", controller, request));

      verify(request).setAttribute("jakarta.servlet.jsp.jspException", failure);
      verifyNoInteractions(messager, logger, totpService);
    }
  }

}

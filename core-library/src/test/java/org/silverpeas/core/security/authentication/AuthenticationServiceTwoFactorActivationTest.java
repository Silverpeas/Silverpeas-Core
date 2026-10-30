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

package org.silverpeas.core.security.authentication;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorAuthenticationService;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.authentication.verifier.UserCanLoginVerifier;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.SettingBundleStub;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = AuthenticationServiceEnrollmentTest.DatabaseContext.class)
class AuthenticationServiceTwoFactorActivationTest {
  @RegisterExtension
  final SettingBundleStub settings =
      new SettingBundleStub(AuthenticationService.class, "AUTHENTICATION_SETTINGS");

  @TestManagedMock
  private TwoFactorAuthenticationService twoFactorService;
  @TestManagedMock
  private ContentEncryptionService encryptionService;
  @TestedBean
  private AuthenticationService service;

  AuthenticationServiceTwoFactorActivationTest() throws IllegalAccessException {
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void globalSwitchShouldOverrideExistingEnrollmentAndMandatoryPolicy(boolean remote)
      throws Exception {
    String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
    try (var connection = DriverManager.getConnection(url);
         var statement = connection.createStatement();
         var naming = mockStatic(InitialContext.class);
         var servers = mockStatic(AuthenticationServer.class);
         var verifiers = mockStatic(AuthenticationUserVerifierFactory.class);
         var users = mockStatic(UserDetail.class)) {
      statement.execute("create table ST_User (id int, login varchar(100), domainId int)");
      statement.execute("insert into ST_User values (42, 'alice', 0)");
      statement.execute("create table ST_Domain (id int, authenticationServer varchar(100))");
      statement.execute("insert into ST_Domain values (0, 'test-server')");
      DataSource dataSource = mock(DataSource.class);
      when(dataSource.getConnection()).thenAnswer(i -> DriverManager.getConnection(url));
      naming.when(() -> InitialContext.doLookup(anyString())).thenReturn(dataSource);
      AuthenticationServer server = mock(AuthenticationServer.class);
      servers.when(() -> AuthenticationServer.getAuthenticationServer("test-server"))
          .thenReturn(server);
      UserCanLoginVerifier verifier = mock(UserCanLoginVerifier.class);
      verifiers.when(() -> AuthenticationUserVerifierFactory.getUserCanLoginVerifier(
          any(AuthenticationCredential.class))).thenReturn(verifier);
      UserDetail user = mock(UserDetail.class);
      users.when(() -> UserDetail.getById("42")).thenReturn(user);
      var enrollment = TwoFactorAuthentication.builder(42)
          .status(TwoFactorAuthentication.Status.ENABLED).build();
      when(twoFactorService.getAuthentication(42)).thenReturn(Optional.of(enrollment));
      service = spy(service);
      doReturn("authenticated-token").when(service).getAuthToken(any());
      var credential = AuthenticationCredential.newWithAsLogin("alice")
          .withAsPassword("password").withAsDomainId("0");
      if (remote) {
        credential.setRemotelyAuthenticated();
      }
      when(encryptionService.isCipherKeyDefined()).thenReturn(true);
      settings.put("twoFactorTotpMandatory", "true");
      settings.put("twoFactorTotpEnabled", "true");
      assertEquals(AuthenticationResponse.Status.TWO_FACTOR_REQUIRED,
          service.authenticate(credential).getStatus());

      clearInvocations(twoFactorService);
      settings.put("twoFactorTotpEnabled", "false");
      var result = service.authenticate(credential);
      assertEquals(AuthenticationResponse.Status.SUCCESS, result.getStatus());
      assertEquals("authenticated-token", result.getToken());
      verifyNoInteractions(twoFactorService);
      verify(verifier, times(2)).verify();
      if (!remote) {
        verify(server, times(2)).authenticate(credential);
      }

      settings.put("twoFactorTotpEnabled", "true");
      assertEquals(AuthenticationResponse.Status.TWO_FACTOR_REQUIRED,
          service.authenticate(credential).getStatus());

      // Loss of the key must suspend 2FA without deleting or attempting to decrypt enrollment.
      clearInvocations(twoFactorService);
      when(encryptionService.isCipherKeyDefined()).thenReturn(false);
      result = service.authenticate(credential);
      assertEquals(AuthenticationResponse.Status.SUCCESS, result.getStatus());
      assertEquals("authenticated-token", result.getToken());
      assertFalse(service.isTwoFactorLocked("alice", "0"));
      assertFalse(service.isTwoFactorEnrollmentRequired("alice", "0"));
      verifyNoInteractions(twoFactorService);

      // Restoring the key must reactivate the unchanged enrollment.
      when(encryptionService.isCipherKeyDefined()).thenReturn(true);
      assertEquals(AuthenticationResponse.Status.TWO_FACTOR_REQUIRED,
          service.authenticate(credential).getStatus());
      verify(verifier, times(5)).verify();
      if (!remote) {
        verify(server, times(5)).authenticate(credential);
      }
      verify(service, times(2)).getAuthToken(credential);
    }
  }
}

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.security.authentication.password.PasswordEncryption;
import org.silverpeas.core.security.authentication.password.PasswordEncryptionProvider;
import org.silverpeas.core.security.authentication.password.encryption.Argon2idEncryption;
import org.silverpeas.core.security.authentication.password.encryption.UnixSHA512Encryption;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the SQL password migration query and its concurrent-update protection.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class AuthenticationSQLPasswordMigrationTest {

  private static final String PASSWORD = "Secret15599!";
  private static final String LOGIN = "test15599";

  private final AuthenticationSQL authentication = new AuthenticationSQL();
  private final Argon2idEncryption argon2id = new Argon2idEncryption();
  private Connection connection;
  private PreparedStatement statement;

  @BeforeEach
  void setUp() throws SQLException {
    configureMigration(true);
    connection = mock(Connection.class);
    statement = mock(PreparedStatement.class);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
  }

  @Test
  void legacyPasswordIsRehashedUsingOriginalDigestAsCondition() throws Exception {
    String legacy = new UnixSHA512Encryption().encrypt(PASSWORD);
    authentication.migratePasswordIfNeeded(connection, LOGIN, false, PASSWORD, legacy,
        () -> argon2id);

    verify(connection).prepareStatement(
        "UPDATE DomainSP_User SET password = ? WHERE login = ? AND password = ?");
    ArgumentCaptor<String> digest = ArgumentCaptor.forClass(String.class);
    verify(statement).setString(eq(1), digest.capture());
    assertTrue(argon2id.doUnderstandDigest(digest.getValue()));
    argon2id.check(PASSWORD, digest.getValue());
    verify(statement).setString(2, LOGIN);
    verify(statement).setString(3, legacy);
    verify(statement).executeUpdate();
  }

  @Test
  void migrationHonorsCaseInsensitiveLogin() throws Exception {
    String legacy = new UnixSHA512Encryption().encrypt(PASSWORD);
    authentication.migratePasswordIfNeeded(connection, LOGIN, true, PASSWORD, legacy,
        () -> argon2id);
    verify(connection).prepareStatement(
        "UPDATE DomainSP_User SET password = ? WHERE lower(login) = lower(?) AND password = ?");
    verify(statement).setString(3, legacy);
  }

  @Test
  void alreadyMigratedPasswordIsNotUpdated() throws Exception {
    String current = argon2id.encrypt(PASSWORD);
    authentication.migratePasswordIfNeeded(connection, LOGIN, false, PASSWORD, current,
        () -> argon2id);
    verify(connection, never()).prepareStatement(anyString());
  }

  @Test
  void concurrentPasswordChangeIsNotOverwritten() throws Exception {
    String legacy = new UnixSHA512Encryption().encrypt(PASSWORD);
    // A concurrent password change makes the conditional UPDATE affect zero rows.
    when(statement.executeUpdate()).thenReturn(0);
    assertDoesNotThrow(() ->
        authentication.migratePasswordIfNeeded(connection, LOGIN, false, PASSWORD, legacy,
            () -> argon2id));
    verify(statement, times(1)).executeUpdate();
  }

  @Test
  void migrationIsDisabledUnlessConfigured() {
    configureMigration(false);
    Supplier<PasswordEncryption> defaultEncryption = () -> {
      throw new AssertionError("Default encryption should not be loaded");
    };
    authentication.migratePasswordIfNeeded(connection, LOGIN, false, PASSWORD, "legacy",
        defaultEncryption);
    verifyNoInteractions(connection);
  }

  @Test
  void writeFailureDoesNotInterruptMigrationCaller() throws SQLException {
    when(statement.executeUpdate()).thenThrow(new SQLException("Read-only connection"));
    assertDoesNotThrow(() -> authentication.migratePasswordIfNeeded(
        connection, LOGIN, false, PASSWORD, "legacy", () -> argon2id));
    verify(statement).executeUpdate();
  }

  @Test
  void encryptionFailureDoesNotInterruptMigrationCaller() {
    PasswordEncryption encryption = mock(PasswordEncryption.class);
    when(encryption.encrypt(PASSWORD)).thenThrow(new IllegalStateException());
    assertDoesNotThrow(() -> authentication.migratePasswordIfNeeded(
        connection, LOGIN, false, PASSWORD, "legacy", () -> encryption));
    verifyNoInteractions(connection);
  }

  @Test
  void unsupportedDigestIsReportedAsBadCredentials() throws Exception {
    String digest = "$argon2id$v=16$m=19456,t=2,p=1$c2FsdA$aGFzaA";
    ResultSet result = mock(ResultSet.class);
    when(statement.executeQuery()).thenReturn(result);
    when(result.next()).thenReturn(true);
    when(result.getString("password")).thenReturn(digest);
    var credential = AuthenticationCredential.newWithAsLogin(LOGIN).withAsPassword(PASSWORD);
    try (var provider = mockStatic(PasswordEncryptionProvider.class)) {
      provider.when(() -> PasswordEncryptionProvider.getPasswordEncryption(digest))
          .thenReturn(argon2id);
      assertThrows(AuthenticationBadCredentialException.class,
          () -> authentication.doChangePassword(new AuthenticationConnection<>(connection),
              credential, "new password"));
    }
    verify(statement, never()).executeUpdate();
  }

  private void configureMigration(boolean enabled) {
    SettingBundle settings = mock(SettingBundle.class);
    when(settings.getString("autServer0.SQLUserTableName")).thenReturn("DomainSP_User");
    when(settings.getString("autServer0.SQLUserLoginColumnName")).thenReturn("login");
    when(settings.getString("autServer0.SQLUserPasswordColumnName")).thenReturn("password");
    if (enabled) {
      when(settings.getBoolean("autServer0.SQLPasswordRehashOnLogin", false)).thenReturn(true);
    }
    authentication.init("autServer0", settings);
  }
}

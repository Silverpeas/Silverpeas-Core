/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package org.silverpeas.core.security.authentication;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.security.authentication.password.encryption.Argon2idEncryption;
import org.silverpeas.core.security.authentication.password.encryption.UnixSHA512Encryption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the SQL password migration query and its concurrent-update protection.
 */
public class AuthenticationSQLPasswordMigrationTest {

  private static final String PASSWORD = "Secret15599!";
  private static final String LOGIN = "test15599";

  private final AuthenticationSQL authentication = new AuthenticationSQL();
  private final Argon2idEncryption argon2id = new Argon2idEncryption();
  private Connection connection;
  private PreparedStatement statement;

  @Before
  public void setUp() throws SQLException {
    authentication.userTableName = "DomainSP_User";
    authentication.loginColumnName = "login";
    authentication.passwordColumnName = "password";
    connection = mock(Connection.class);
    statement = mock(PreparedStatement.class);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
  }

  @Test
  public void legacyPasswordIsRehashedUsingOriginalDigestAsCondition() throws Exception {
    String legacy = new UnixSHA512Encryption().encrypt(PASSWORD);
    authentication.migratePasswordIfNeeded(connection, LOGIN, false, PASSWORD, legacy, argon2id);

    verify(connection).prepareStatement(
        "UPDATE DomainSP_User SET password = ? WHERE login = ? AND password = ?");
    ArgumentCaptor<String> digest = ArgumentCaptor.forClass(String.class);
    verify(statement).setString(org.mockito.ArgumentMatchers.eq(1), digest.capture());
    assertTrue(argon2id.doUnderstandDigest(digest.getValue()));
    argon2id.check(PASSWORD, digest.getValue());
    verify(statement).setString(2, LOGIN);
    verify(statement).setString(3, legacy);
    verify(statement).executeUpdate();
  }

  @Test
  public void migrationHonorsCaseInsensitiveLogin() throws Exception {
    String legacy = new UnixSHA512Encryption().encrypt(PASSWORD);
    authentication.migratePasswordIfNeeded(connection, LOGIN, true, PASSWORD, legacy, argon2id);
    verify(connection).prepareStatement(
        "UPDATE DomainSP_User SET password = ? WHERE lower(login) = lower(?) AND password = ?");
    verify(statement).setString(3, legacy);
  }

  @Test
  public void alreadyMigratedPasswordIsNotUpdated() throws Exception {
    String current = argon2id.encrypt(PASSWORD);
    authentication.migratePasswordIfNeeded(connection, LOGIN, false, PASSWORD, current, argon2id);
    verify(connection, never()).prepareStatement(anyString());
  }

  @Test
  public void concurrentPasswordChangeIsNotOverwritten() throws Exception {
    String legacy = new UnixSHA512Encryption().encrypt(PASSWORD);
    // A concurrent password change makes the conditional UPDATE affect zero rows.
    when(statement.executeUpdate()).thenReturn(0);
    authentication.migratePasswordIfNeeded(connection, LOGIN, false, PASSWORD, legacy, argon2id);
    verify(statement).setString(3, legacy);
    verify(statement).executeUpdate();
  }
}

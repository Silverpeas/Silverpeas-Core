/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation: either version 3 of the License,
 * or (at your option) any later version.
 */
package org.silverpeas.core.security.authentication.twofactor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrustedDeviceServiceImplTest {

  private static final int USER_ID = 42;

  private Connection connection;
  private PreparedStatement statement;
  private ResultSet resultSet;
  private TrustedDeviceServiceImpl service;

  @BeforeEach
  void setUp() {
    connection = mock(Connection.class);
    statement = mock(PreparedStatement.class);
    resultSet = mock(ResultSet.class);
    service = new TestableTrustedDeviceService(connection);
  }

  @Test
  void shouldCreateTrustedDevice() throws Exception {
    when(connection.prepareStatement(anyString())).thenReturn(statement);

    final String token = service.create(USER_ID, "Mozilla/5.0");

    assertNotNull(token);
    assertTrue(token.matches("[A-Za-z0-9_-]{43}"));
    verify(statement).setInt(1, USER_ID);
    verify(statement).setString(2, sha256(token));
    verify(statement).setString(6, "Mozilla/5.0");
    verify(statement).executeUpdate();
  }

  @Test
  void shouldRotateValidTrustedDevice() throws Exception {
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getLong(1)).thenReturn(123L);
    when(resultSet.getTimestamp(2)).thenReturn(
        Timestamp.from(Instant.now().plusSeconds(3600)));
    when(statement.executeUpdate()).thenReturn(1);

    final String newToken = service.validateAndRotate(USER_ID, "old-token", "Firefox");

    assertNotNull(newToken);
    assertFalse(newToken.equals("old-token"));
    verify(statement).setString(1, sha256(newToken));
    verify(statement).setString(3, "Firefox");
    verify(statement).setString(5, sha256("old-token"));
    verify(statement).setTimestamp(eq(6), org.mockito.ArgumentMatchers.any(Timestamp.class));
    verify(statement).executeUpdate();
  }

  @Test
  void shouldPreserveAbsoluteExpiryWhenRotating() throws Exception {
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getLong(1)).thenReturn(123L);
    when(resultSet.getTimestamp(2)).thenReturn(
        Timestamp.from(Instant.now().plusSeconds(3600)));
    when(statement.executeUpdate()).thenReturn(1);

    assertNotNull(service.validateAndRotate(USER_ID, "old-token", "Firefox"));

    verify(connection).prepareStatement(contains(
        "SET tokenHash = ?, lastUsedAt = ?, userAgent = ?"));
    verify(statement, never()).setTimestamp(eq(2),
        org.mockito.ArgumentMatchers.any(Timestamp.class));
  }

  @Test
  void shouldRejectExpiredTrustedDeviceAndDeleteIt() throws Exception {
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getLong(1)).thenReturn(123L);
    when(resultSet.getTimestamp(2)).thenReturn(
        Timestamp.from(Instant.now().minusSeconds(1)));

    assertNull(service.validateAndRotate(USER_ID, "expired-token", "Firefox"));

    verify(statement).setLong(1, 123L);
    verify(statement, never()).setString(1, sha256("expired-token"));
  }

  @Test
  void shouldRejectUnknownTrustedDevice() throws Exception {
    when(connection.prepareStatement(anyString())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);

    assertNull(service.validateAndRotate(USER_ID, "unknown-token", "Firefox"));
  }

  @Test
  void shouldRejectBlankTrustedDeviceToken() throws Exception {
    assertNull(service.validateAndRotate(USER_ID, " ", "Firefox"));
    verify(connection, never()).prepareStatement(anyString());
  }

  @Test
  void shouldRevokeAllTrustedDevices() throws Exception {
    when(connection.prepareStatement(anyString())).thenReturn(statement);

    service.revokeAll(USER_ID);

    verify(statement).setInt(1, USER_ID);
    verify(statement).executeUpdate();
  }

  @Test
  void shouldRejectNegativeUserIdentifier() {
    assertThrows(IllegalArgumentException.class, () -> service.create(-1, "Firefox"));
    assertThrows(IllegalArgumentException.class,
        () -> service.validateAndRotate(-1, "token", "Firefox"));
    assertThrows(IllegalArgumentException.class, () -> service.revokeAll(-1));
  }

  @Test
  void shouldTruncateLongUserAgent() throws Exception {
    when(connection.prepareStatement(anyString())).thenReturn(statement);

    service.create(USER_ID, "a".repeat(1100));

    verify(statement).setString(6, "a".repeat(1024));
  }

  private String sha256(final String value) throws Exception {
    return java.util.HexFormat.of().formatHex(
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
  }

  private static class TestableTrustedDeviceService extends TrustedDeviceServiceImpl {

    private final Connection connection;

    private TestableTrustedDeviceService(final Connection connection) {
      this.connection = connection;
    }

    @Override
    protected Connection openConnection() {
      return connection;
    }
  }
}

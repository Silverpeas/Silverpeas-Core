/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package org.silverpeas.core.security.authentication.twofactor;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

/**
 * JDBC implementation of trusted-device management.
 *
 * <p>The browser receives only the random token. The database stores its
 * SHA-256 hash, so a database disclosure does not directly disclose usable
 * browser tokens.</p>
 */
@Service
@Transactional(Transactional.TxType.SUPPORTS)
public class TrustedDeviceServiceImpl implements TrustedDeviceService {

  private static final String TABLE = "ST_User_2FA_Trusted_Device";
  private static final int TOKEN_SIZE = 32;
  private static final int MAX_USER_AGENT_LENGTH = 1024;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static final SettingBundle AUTHENTICATION_SETTINGS = ResourceLocator.getSettingBundle(
      "org.silverpeas.authentication.settings.authenticationSettings");

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String create(final int userId, final String userAgent) {
    validateUserId(userId);
    final Instant now = Instant.now();
    final Instant expiresAt = now.plusSeconds(getLifetime());
    final String token = generateToken();

    try (Connection connection = openConnection();
         PreparedStatement statement = connection.prepareStatement(
             "INSERT INTO " + TABLE +
                 " (userId, tokenHash, createdAt, expiresAt, lastUsedAt, userAgent) " +
                 " VALUES (?, ?, ?, ?, ?, ?)")) {
      statement.setInt(1, userId);
      statement.setString(2, hashToken(token));
      statement.setTimestamp(3, Timestamp.from(now));
      statement.setTimestamp(4, Timestamp.from(expiresAt));
      statement.setTimestamp(5, Timestamp.from(now));
      statement.setString(6, normalizeUserAgent(userAgent));
      statement.executeUpdate();
      return token;
    } catch (SQLException e) {
      throw new IllegalStateException("Unable to create a trusted device for user " + userId, e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String validateAndRotate(final int userId, final String token,
      final String userAgent) {
    validateUserId(userId);
    if (token == null || token.isBlank()) {
      return null;
    }

    final String tokenHash = hashToken(token);
    try (Connection connection = openConnection()) {
      final String selectSql =
          "SELECT id, expiresAt FROM " + TABLE +
              " WHERE userId = ? AND tokenHash = ?";
      try (PreparedStatement select = connection.prepareStatement(selectSql)) {
        select.setInt(1, userId);
        select.setString(2, tokenHash);
        try (ResultSet rs = select.executeQuery()) {
          if (!rs.next()) {
            return null;
          }

          final long id = rs.getLong(1);
          final Timestamp expiresAt = rs.getTimestamp(2);
          final Instant now = Instant.now();
          if (expiresAt == null || !expiresAt.toInstant().isAfter(now)) {
            delete(connection, id);
            return null;
          }

          final String newToken = generateToken();
          final Instant newExpiresAt = now.plusSeconds(getLifetime());
          try (PreparedStatement update = connection.prepareStatement(
              "UPDATE " + TABLE +
                  " SET tokenHash = ?, expiresAt = ?, lastUsedAt = ?, userAgent = ? " +
                  " WHERE id = ? AND tokenHash = ?")) {
            update.setString(1, hashToken(newToken));
            update.setTimestamp(2, Timestamp.from(newExpiresAt));
            update.setTimestamp(3, Timestamp.from(now));
            update.setString(4, normalizeUserAgent(userAgent));
            update.setLong(5, id);
            update.setString(6, tokenHash);
            if (update.executeUpdate() != 1) {
              return null;
            }
          }
          return newToken;
        }
      }
    } catch (SQLException e) {
      throw new IllegalStateException(
          "Unable to validate a trusted device for user " + userId, e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void revokeAll(final int userId) {
    validateUserId(userId);
    try (Connection connection = openConnection();
         PreparedStatement statement = connection.prepareStatement(
             "DELETE FROM " + TABLE + " WHERE userId = ?")) {
      statement.setInt(1, userId);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new IllegalStateException(
          "Unable to revoke trusted devices for user " + userId, e);
    }
  }

  protected Connection openConnection() throws SQLException {
    return DBUtil.openConnection();
  }

  private void delete(final Connection connection, final long id) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(
        "DELETE FROM " + TABLE + " WHERE id = ?")) {
      statement.setLong(1, id);
      statement.executeUpdate();
    }
  }

  private String generateToken() {
    final byte[] bytes = new byte[TOKEN_SIZE];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hashToken(final String token) {
    try {
      return java.util.HexFormat.of().formatHex(
          MessageDigest.getInstance("SHA-256")
              .digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unable to hash trusted-device token", e);
    }
  }

  private int getLifetime() {
    return Math.max(1, AUTHENTICATION_SETTINGS.getInteger(
        "twoFactorTrustedDeviceLifetime", 30 * 24 * 60 * 60));
  }

  private String normalizeUserAgent(final String userAgent) {
    if (userAgent == null) {
      return null;
    }
    return userAgent.length() <= MAX_USER_AGENT_LENGTH
        ? userAgent
        : userAgent.substring(0, MAX_USER_AGENT_LENGTH);
  }

  private void validateUserId(final int userId) {
    if (userId < 0) {
      throw new IllegalArgumentException("The user identifier must not be negative");
    }
  }
}

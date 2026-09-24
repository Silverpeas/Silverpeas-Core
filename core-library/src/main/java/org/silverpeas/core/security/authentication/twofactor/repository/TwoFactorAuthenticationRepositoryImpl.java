/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authentication.twofactor.repository;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import jakarta.inject.Inject;
import org.silverpeas.core.security.encryption.cipher.CryptoException;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/**
 * JDBC repository of native two-factor authentication configurations.
 */
@Repository
public class TwoFactorAuthenticationRepositoryImpl
        implements TwoFactorAuthenticationRepository {

    private static final String TABLE = "ST_User_2FA";

    private static final String USER_ID = "userId";
    private static final String SECRET = "secret";
    private static final String STATUS = "status";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";
    private static final String LAST_USED_AT = "lastUsedAt";
    private static final String FAILED_ATTEMPTS = "failedAttempts";
    private static final String LOCKED_UNTIL = "lockedUntil";

    private static final int CIPHER_KEY_SIZE = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Inject
    private ContentEncryptionService encryptionService;

    @Override
    public Optional<TwoFactorAuthentication> get(
            final Connection connection,
            final int userId) throws SQLException {

        final String sql =
                "SELECT userId, secret, status, createdAt, updatedAt, lastUsedAt, " +
                        "failedAttempts, lockedUntil FROM " + TABLE + " WHERE userId = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(TwoFactorAuthentication.builder(userId)
                        .secret(decryptSecret(rs.getString(SECRET)))
                        .status(TwoFactorAuthentication.Status.valueOf(rs.getString(STATUS)))
                        .createdAt(toInstant(rs.getTimestamp(CREATED_AT)))
                        .updatedAt(toInstant(rs.getTimestamp(UPDATED_AT)))
                        .lastUsedAt(toInstant(rs.getTimestamp(LAST_USED_AT)))
                        .failedAttempts(rs.getInt(FAILED_ATTEMPTS))
                        .lockedUntil(toInstant(rs.getTimestamp(LOCKED_UNTIL)))
                        .build());
            }
        }
    }

    @Override
    public void save(
            final Connection connection,
            final TwoFactorAuthentication authentication) throws SQLException {

        final String encryptedSecret = encryptSecret(authentication.getSecret());
        final Instant now = Instant.now();

        if (get(connection, authentication.getUserId()).isPresent()) {
            final String sql =
                    "UPDATE " + TABLE + " SET secret = ?, status = ?, updatedAt = ?, " +
                            "lastUsedAt = ?, failedAttempts = ?, lockedUntil = ? WHERE userId = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, encryptedSecret);
                statement.setString(2, authentication.getStatus().name());
                statement.setTimestamp(3, Timestamp.from(now));
                setTimestamp(statement, 4, authentication.getLastUsedAt());
                statement.setInt(5, authentication.getFailedAttempts());
                setTimestamp(statement, 6, authentication.getLockedUntil());
                statement.setInt(7, authentication.getUserId());
                statement.executeUpdate();
            }
        } else {
            final String sql =
                    "INSERT INTO " + TABLE +
                            " (userId, secret, status, createdAt, updatedAt, lastUsedAt, " +
                            "failedAttempts, lockedUntil) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, authentication.getUserId());
                statement.setString(2, encryptedSecret);
                statement.setString(3, authentication.getStatus().name());
                statement.setTimestamp(4, Timestamp.from(now));
                statement.setTimestamp(5, Timestamp.from(now));
                setTimestamp(statement, 6, authentication.getLastUsedAt());
                statement.setInt(7, authentication.getFailedAttempts());
                setTimestamp(statement, 8, authentication.getLockedUntil());
                statement.executeUpdate();
            }
        }
    }

    @Override
    public void delete(final Connection connection, final int userId) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM " + TABLE + " WHERE userId = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    @Override
    public void updateLastUsedAt(
            final Connection connection,
            final int userId,
            final Instant lastUsedAt) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + TABLE + " SET lastUsedAt = ?, updatedAt = ? WHERE userId = ?")) {
            setTimestamp(statement, 1, lastUsedAt);
            statement.setTimestamp(2, Timestamp.from(Instant.now()));
            statement.setInt(3, userId);
            statement.executeUpdate();
        }
    }

    @Override
    public void updateFailedAttempts(
            final Connection connection,
            final int userId,
            final int failedAttempts,
            final Instant lockedUntil) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + TABLE +
                        " SET failedAttempts = ?, lockedUntil = ?, updatedAt = ? WHERE userId = ?")) {
            statement.setInt(1, failedAttempts);
            setTimestamp(statement, 2, lockedUntil);
            statement.setTimestamp(3, Timestamp.from(Instant.now()));
            statement.setInt(4, userId);
            statement.executeUpdate();
        }
    }

    @Override
    public void resetFailedAttempts(
            final Connection connection,
            final int userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + TABLE +
                        " SET failedAttempts = 0, lockedUntil = NULL, updatedAt = ? WHERE userId = ?")) {
            statement.setTimestamp(1, Timestamp.from(Instant.now()));
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    private String encryptSecret(final String secret) {
        if (secret == null) {
            return null;
        }

        ensureCipherKeyDefined();

        try {
            return encryptionService.encryptContent(secret)[0];
        } catch (CryptoException e) {
            throw new IllegalStateException(
                    "Unable to encrypt the two-factor authentication secret",
                    e);
        }
    }

    private void ensureCipherKeyDefined() {
        synchronized (TwoFactorAuthenticationRepositoryImpl.class) {
            if (encryptionService.isCipherKeyDefined()) {
                return;
            }

            try {
                encryptionService.updateCipherKey(generateCipherKey());
            } catch (CryptoException e) {
                throw new IllegalStateException(
                        "Unable to initialize the Silverpeas content encryption key",
                        e);
            }
        }
    }

    private String generateCipherKey() {
        final byte[] key = new byte[CIPHER_KEY_SIZE];
        SECURE_RANDOM.nextBytes(key);
        return java.util.HexFormat.of().formatHex(key);
    }

    private String decryptSecret(final String encryptedSecret) {
        if (encryptedSecret == null) {
            return null;
        }

        try {
            return encryptionService.decryptContent(encryptedSecret)[0];
        } catch (CryptoException e) {
            throw new IllegalStateException(
                    "Unable to decrypt the two-factor authentication secret",
                    e);
        }
    }

    private Instant toInstant(final Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
    private void setTimestamp(final PreparedStatement statement, final int index,
            final Instant value) throws SQLException {
        if (value == null) {
            statement.setTimestamp(index, null);
        } else {
            statement.setTimestamp(index, Timestamp.from(value));
        }
    }

}

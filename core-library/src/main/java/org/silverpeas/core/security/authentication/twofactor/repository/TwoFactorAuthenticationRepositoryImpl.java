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
package org.silverpeas.core.security.authentication.twofactor.repository;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.encryption.cipher.CryptoException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/**
 * JDBC repository of native two-factor authentication configurations.
 *
 * @author Silverpeas
 */
@Repository
public class TwoFactorAuthenticationRepositoryImpl
        implements TwoFactorAuthenticationRepository {

    private static final String TABLE = "ST_UserTwoFactorAuthentication";

    private static final String USER_ID = "userId";
    private static final String SECRET = "secret";
    private static final String STATUS = "status";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";
    private static final String LAST_USED_AT = "lastUsedAt";

    @Override
    public Optional<TwoFactorAuthentication> get(
            final Connection connection, final int userId) throws SQLException {

        final TwoFactorAuthentication authentication = JdbcSqlQuery
                .select(
                        USER_ID + ", " +
                                SECRET + ", " +
                                STATUS + ", " +
                                CREATED_AT + ", " +
                                UPDATED_AT + ", " +
                                LAST_USED_AT)
                .from(TABLE)
                .where(USER_ID + " = ?", userId)
                .executeUniqueWith(connection, rs -> {
                    final Timestamp lastUsedAt = rs.getTimestamp(LAST_USED_AT);

                    return TwoFactorAuthentication.builder(userId)
                            .secret(rs.getString(SECRET))
                            .status(TwoFactorAuthentication.Status.valueOf(
                                    rs.getString(STATUS)))
                            .createdAt(rs.getTimestamp(CREATED_AT).toInstant())
                            .updatedAt(rs.getTimestamp(UPDATED_AT).toInstant())
                            .lastUsedAt(
                                    lastUsedAt == null
                                            ? null
                                            : lastUsedAt.toInstant())
                            .build();
                });

        return Optional.ofNullable(authentication);
    }

    @Override
    public void save(
            final Connection connection,
            final TwoFactorAuthentication authentication) throws SQLException {

        final Instant now = Instant.now();

        final Optional<TwoFactorAuthentication> existing =
                get(connection, authentication.getUserId());

        final String encryptedSecret = encryptSecret(authentication.getEncryptedSecret());

        if (existing.isPresent()) {
            JdbcSqlQuery
                    .update(TABLE)
                    .withUpdateParam(SECRET, encryptedSecret)
                    .withUpdateParam(STATUS, authentication.getStatus().name())
                    .withUpdateParam(UPDATED_AT, now)
                    .withUpdateParam(LAST_USED_AT, authentication.getLastUsedAt())
                    .where(USER_ID + " = ?", authentication.getUserId())
                    .executeWith(connection);
        } else {
            JdbcSqlQuery
                    .insertInto(TABLE)
                    .withInsertParam(USER_ID, authentication.getUserId())
                    .withInsertParam(SECRET, encryptedSecret)
                    .withInsertParam(STATUS, authentication.getStatus().name())
                    .withInsertParam(CREATED_AT, now)
                    .withInsertParam(UPDATED_AT, now)
                    .withInsertParam(LAST_USED_AT, authentication.getLastUsedAt())
                    .executeWith(connection);
        }
    }

    @Override
    public void delete(
            final Connection connection, final int userId) throws SQLException {

        JdbcSqlQuery
                .deleteFrom(TABLE)
                .where(USER_ID + " = ?", userId)
                .executeWith(connection);
    }

    @Override
    public void updateLastUsedAt(
            final Connection connection,
            final int userId,
            final Instant lastUsedAt) throws SQLException {

        JdbcSqlQuery
                .update(TABLE)
                .withUpdateParam(LAST_USED_AT, lastUsedAt)
                .withUpdateParam(UPDATED_AT, Instant.now())
                .where(USER_ID + " = ?", userId)
                .executeWith(connection);
    }

    private String encryptSecret(final String secret) {
        try {
            return ContentEncryptionService.get()
                    .encryptContent(secret)[0];
        } catch (CryptoException e) {
            throw new IllegalStateException(
                    "Unable to encrypt the two-factor authentication secret", e);
        }
    }

    private String decryptSecret(final String encryptedSecret) {
        if (encryptedSecret == null) {
            return null;
        }

        try {
            return ContentEncryptionService.get()
                    .decryptContent(encryptedSecret)[0];
        } catch (CryptoException e) {
            throw new IllegalStateException(
                    "Unable to decrypt the two-factor authentication secret", e);
        }
    }

    private Instant getInstant(final java.sql.Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
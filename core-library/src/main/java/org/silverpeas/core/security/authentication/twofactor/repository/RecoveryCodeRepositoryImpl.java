/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package org.silverpeas.core.security.authentication.twofactor.repository;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.security.authentication.twofactor.model.RecoveryCode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository of native two-factor authentication recovery codes.
 */
@Repository
public class RecoveryCodeRepositoryImpl implements RecoveryCodeRepository {

    private static final String TABLE = "ST_User_2FA_Recovery";

    @Override
    public List<RecoveryCode> getUnused(final Connection connection, final int userId)
            throws SQLException {
        return JdbcSqlQuery
                .select("id, userId, hash, used, createdAt, usedAt")
                .from(TABLE)
                .where("userId = ? AND used = ?", userId, false)
                .executeWith(connection, this::map);
    }

    @Override
    public void save(final Connection connection, final RecoveryCode recoveryCode)
            throws SQLException {
        JdbcSqlQuery
                .insertInto(TABLE)
                .withInsertParam("userId", recoveryCode.getUserId())
                .withInsertParam("hash", recoveryCode.getHash())
                .withInsertParam("used", recoveryCode.isUsed())
                .withInsertParam("createdAt", recoveryCode.getCreatedAt())
                .withInsertParam("usedAt", recoveryCode.getUsedAt())
                .executeWith(connection);
    }

    @Override
    public boolean consume(final Connection connection, final String hash, final Instant usedAt)
            throws SQLException {
        final long count = JdbcSqlQuery
                .update(TABLE)
                .withUpdateParam("used", true)
                .withUpdateParam("usedAt", usedAt)
                .where("hash = ? AND used = ?", hash, false)
                .executeWith(connection);
        return count == 1;
    }

    @Override
    public void deleteAll(final Connection connection, final int userId) throws SQLException {
        JdbcSqlQuery
                .deleteFrom(TABLE)
                .where("userId = ?", userId)
                .executeWith(connection);
    }

    private RecoveryCode map(final ResultSet rs) throws SQLException {
        final Timestamp createdAt = rs.getTimestamp("createdAt");
        final Timestamp usedAt = rs.getTimestamp("usedAt");
        return RecoveryCode.builder(rs.getInt("userId"))
                .id(rs.getLong("id"))
                .hash(rs.getString("hash"))
                .used(rs.getBoolean("used"))
                .createdAt(createdAt.toInstant())
                .usedAt(usedAt == null ? null : usedAt.toInstant())
                .build();
    }
}

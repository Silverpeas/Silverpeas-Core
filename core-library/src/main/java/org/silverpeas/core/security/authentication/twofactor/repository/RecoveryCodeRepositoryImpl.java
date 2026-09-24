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
import org.silverpeas.core.security.authentication.twofactor.model.RecoveryCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
        final String sql = "SELECT id, userId, hash, used, createdAt, usedAt FROM " + TABLE
                + " WHERE userId = ? AND used = ?";
        final List<RecoveryCode> codes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setBoolean(2, false);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    codes.add(map(rs));
                }
            }
        }
        return codes;
    }

    @Override
    public void save(final Connection connection, final RecoveryCode recoveryCode)
            throws SQLException {
        final String sql = "INSERT INTO " + TABLE
                + " (userId, hash, used, createdAt, usedAt) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recoveryCode.getUserId());
            statement.setString(2, recoveryCode.getHash());
            statement.setBoolean(3, recoveryCode.isUsed());
            statement.setTimestamp(4, Timestamp.from(recoveryCode.getCreatedAt()));
            setTimestamp(statement, 5, recoveryCode.getUsedAt());
            statement.executeUpdate();
        }
    }

    @Override
    public boolean consume(final Connection connection, final String hash, final Instant usedAt)
            throws SQLException {
        final String sql = "UPDATE " + TABLE
                + " SET used = ?, usedAt = ? WHERE hash = ? AND used = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, true);
            statement.setTimestamp(2, Timestamp.from(usedAt));
            statement.setString(3, hash);
            statement.setBoolean(4, false);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public void deleteAll(final Connection connection, final int userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + TABLE + " WHERE userId = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
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

    private void setTimestamp(final PreparedStatement statement, final int index,
            final Instant value) throws SQLException {
        if (value == null) {
            statement.setTimestamp(index, null);
        } else {
            statement.setTimestamp(index, Timestamp.from(value));
        }
    }
}

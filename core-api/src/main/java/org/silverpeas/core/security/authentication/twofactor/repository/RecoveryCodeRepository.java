/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package org.silverpeas.core.security.authentication.twofactor.repository;

import org.silverpeas.core.security.authentication.twofactor.model.RecoveryCode;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

/**
 * Repository of recovery codes used by the native two-factor authentication.
 */
public interface RecoveryCodeRepository {

    /**
     * Gets all unused recovery codes of a user.
     *
     * @param connection the database connection.
     * @param userId the Silverpeas user identifier.
     * @return the unused recovery codes.
     * @throws SQLException if an error occurs while accessing the database.
     */
    List<RecoveryCode> getUnused(Connection connection, int userId) throws SQLException;

    /**
     * Creates a recovery code.
     *
     * @param connection the database connection.
     * @param recoveryCode the code to persist.
     * @throws SQLException if an error occurs while accessing the database.
     */
    void save(Connection connection, RecoveryCode recoveryCode) throws SQLException;

    /**
     * Marks a recovery code as used.
     *
     * @param connection the database connection.
     * @param id the recovery code identifier.
     * @param usedAt the date at which the code was consumed.
     * @throws SQLException if an error occurs while accessing the database.
     */
    /**
     * Atomically consumes an unused recovery code.
     *
     * @param connection the database connection.
     * @param hash the hash of the recovery code.
     * @param usedAt the date at which the code was consumed.
     * @return true if an unused code was consumed.
     * @throws SQLException if an error occurs while accessing the database.
     */
    boolean consume(Connection connection, String hash, Instant usedAt) throws SQLException;

    /**
     * Deletes all recovery codes of a user.
     *
     * @param connection the database connection.
     * @param userId the Silverpeas user identifier.
     * @throws SQLException if an error occurs while accessing the database.
     */
    void deleteAll(Connection connection, int userId) throws SQLException;
}

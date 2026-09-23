/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package org.silverpeas.core.security.authentication.twofactor.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@EnableSilverTestEnv(context = JEETestContext.class)
class TwoFactorAuthenticationRepositoryTest {

    private static final int USER_ID = 42;
    private static final String SECRET = "JBSWY3DPEHPK3PXP";
    private static final String UPDATED_SECRET = "KRUGS4ZANFZSAYJA";
    private static final String JDBC_URL =
            "jdbc:h2:mem:twofactor_repository;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";

    @TestedBean
    private TwoFactorAuthenticationRepositoryImpl repository;

    @TestManagedMock
    private ContentEncryptionService encryptionService;

    @BeforeEach
    void setUp() throws SQLException, CryptoException {
        when(encryptionService.isCipherKeyDefined()).thenReturn(true);
        when(encryptionService.encryptContent(SECRET))
                .thenReturn(new String[]{"encrypted:" + SECRET});
        when(encryptionService.encryptContent(UPDATED_SECRET))
                .thenReturn(new String[]{"encrypted:" + UPDATED_SECRET});
        when(encryptionService.decryptContent("encrypted:" + SECRET))
                .thenReturn(new String[]{SECRET});
        when(encryptionService.decryptContent("encrypted:" + UPDATED_SECRET))
                .thenReturn(new String[]{UPDATED_SECRET});
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ST_User_2FA (" +
                    "userId INT NOT NULL PRIMARY KEY, secret VARCHAR(1024) NOT NULL, " +
                    "status VARCHAR(20) NOT NULL, createdAt TIMESTAMP NOT NULL, " +
                    "updatedAt TIMESTAMP NOT NULL, lastUsedAt TIMESTAMP, " +
                    "failedAttempts INT NOT NULL DEFAULT 0, lockedUntil TIMESTAMP)");
            statement.executeUpdate("DELETE FROM ST_User_2FA");
        }
    }

    @Test
    void shouldReturnEmptyWhenNoConfigurationExists() throws SQLException {
        try (Connection connection = connection()) {
            assertThat(repository.get(connection, USER_ID), is(Optional.empty()));
        }
    }

    @Test
    void shouldSaveConfiguration() throws SQLException {
        try (Connection connection = connection()) {
            repository.save(connection, authentication(SECRET));
            Optional<TwoFactorAuthentication> stored = repository.get(connection, USER_ID);
            assertThat(stored.isPresent(), is(true));
            assertThat(stored.get().getSecret(), is(SECRET));
            assertThat(stored.get().getStatus(), is(TwoFactorAuthentication.Status.PENDING));
            assertThat(stored.get().getFailedAttempts(), is(0));
            assertThat(stored.get().getLockedUntil(), nullValue());
        }
    }

    @Test
    void shouldEncryptSecretWhenSaving() throws SQLException {
        try (Connection connection = connection()) {
            repository.save(connection, authentication(SECRET));
            String storedSecret;
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT secret FROM ST_User_2FA WHERE userId = " + USER_ID)) {
                resultSet.next();
                storedSecret = resultSet.getString("secret");
            }
            assertThat(storedSecret, is(not(SECRET)));
            assertThat(storedSecret, is("encrypted:" + SECRET));
        }
    }

    @Test
    void shouldUpdateExistingConfiguration() throws SQLException {
        try (Connection connection = connection()) {
            repository.save(connection, authentication(SECRET));
            Instant lockedUntil = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);
            TwoFactorAuthentication updated = authentication(UPDATED_SECRET).toBuilder()
                    .status(TwoFactorAuthentication.Status.ENABLED)
                    .failedAttempts(2)
                    .lockedUntil(lockedUntil)
                    .build();
            repository.save(connection, updated);
            Optional<TwoFactorAuthentication> stored = repository.get(connection, USER_ID);
            assertThat(stored.get().getSecret(), is(UPDATED_SECRET));
            assertThat(stored.get().getStatus(), is(TwoFactorAuthentication.Status.ENABLED));
            assertThat(stored.get().getFailedAttempts(), is(2));
            assertThat(stored.get().getLockedUntil(), is(lockedUntil));
        }
    }

    @Test
    void shouldPersistLastUsedAt() throws SQLException {
        try (Connection connection = connection()) {
            repository.save(connection, authentication(SECRET));
            Instant lastUsedAt = Instant.parse("2026-09-22T08:00:00Z");
            repository.updateLastUsedAt(connection, USER_ID, lastUsedAt);
            assertThat(repository.get(connection, USER_ID).get().getLastUsedAt(), is(lastUsedAt));
        }
    }

    @Test
    void shouldDeleteConfiguration() throws SQLException {
        try (Connection connection = connection()) {
            repository.save(connection, authentication(SECRET));
            repository.delete(connection, USER_ID);
            assertThat(repository.get(connection, USER_ID), is(Optional.empty()));
        }
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    private TwoFactorAuthentication authentication(String secret) {
        return TwoFactorAuthentication.builder(USER_ID)
                .secret(secret)
                .status(TwoFactorAuthentication.Status.PENDING)
                .build();
    }
}

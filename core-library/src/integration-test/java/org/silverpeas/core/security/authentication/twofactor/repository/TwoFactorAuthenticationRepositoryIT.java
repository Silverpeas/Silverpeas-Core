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

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.encryption.cipher.Cipher;
import org.silverpeas.core.security.encryption.cipher.CipherFactory;
import org.silverpeas.core.security.encryption.cipher.CipherKey;
import org.silverpeas.core.security.encryption.cipher.CryptographicAlgorithmName;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.kernel.util.StringUtil;

import java.io.File;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class TwoFactorAuthenticationRepositoryIT {
    private static final int USER_ID = 42;
    private static final String SECRET = "JBSWY3DPEHPK3PXP";
    private static final String UPDATED_SECRET = "KRUGS4ZANFZSAYJA";
    private static final String JDBC_URL =
            "jdbc:h2:mem:twofactor_repository;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";

    private final TwoFactorAuthenticationRepository repository =
            new TwoFactorAuthenticationRepositoryImpl();

    @Before
    public void initializeDatabase() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        try (Connection connection = connection()) {
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ST_User_2FA (" +
                            "userId INT NOT NULL PRIMARY KEY, " +
                            "secret VARCHAR(1024) NOT NULL, " +
                            "status VARCHAR(20) NOT NULL, " +
                            "createdAt TIMESTAMP NOT NULL, " +
                            "updatedAt TIMESTAMP NOT NULL, " +
                            "lastUsedAt TIMESTAMP, " +
                            "failedAttempts INT NOT NULL DEFAULT 0, " +
                            "lockedUntil TIMESTAMP)");
            connection.createStatement().executeUpdate("DELETE FROM ST_User_2FA");
        }

        File securityDir = new File(FileRepositoryManager.getSecurityDirPath());
        FileUtils.forceMkdir(securityDir);
        securityDir.setWritable(true);
        securityDir.setReadable(true);
        securityDir.setExecutable(true);

        String key = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        Cipher cast5 = CipherFactory.getFactory().getCipher(CryptographicAlgorithmName.CAST5);
        CipherKey wrappingKey = CipherKey.aKeyFromHexText("06277d1ce530c94bd9a13a72a58342be");
        String encryptedContent = StringUtil.asBase64(wrappingKey.getRawKey()) + " " +
                StringUtil.asBase64(cast5.encrypt(key, wrappingKey));
        FileUtils.writeStringToFile(
                new File(FileRepositoryManager.getSecurityDirPath() + ".aid_key"),
                encryptedContent, Charsets.UTF_8);
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    @Test
    public void shouldReturnEmptyWhenNoConfigurationExists() throws SQLException {
        try (Connection connection = connection()) {
            assertThat(repository.get(connection, USER_ID), is(Optional.empty()));
        }
    }

    @Test
    public void shouldSaveConfiguration() throws SQLException {
        Transaction.performInOne(() -> {
            final TwoFactorAuthentication authentication = authentication(SECRET);
            try (Connection connection = connection()) {
                repository.save(connection, authentication);
                final Optional<TwoFactorAuthentication> stored = repository.get(connection, USER_ID);
                assertThat(stored.isPresent(), is(true));
                assertThat(stored.get().getUserId(), is(USER_ID));
                assertThat(stored.get().getSecret(), is(SECRET));
                assertThat(stored.get().getStatus(), is(TwoFactorAuthentication.Status.PENDING));
                assertThat(stored.get().getFailedAttempts(), is(0));
                assertThat(stored.get().getLockedUntil(), nullValue());
            }
            return null;
        });
    }

    @Test
    public void shouldEncryptSecretWhenSaving() throws SQLException {
        Transaction.performInOne(() -> {
            final TwoFactorAuthentication authentication = authentication(SECRET);
            try (Connection connection = connection()) {
                repository.save(connection, authentication);
                final String storedSecret = JdbcSqlQuery.select("secret")
                        .from("ST_User_2FA")
                        .where("userId = ?", USER_ID)
                        .executeUniqueWith(connection, rs -> rs.getString("secret"));
                assertThat(storedSecret, is(not(SECRET)));
                assertThat(storedSecret, is(not(nullValue())));
            }
            return null;
        });
    }

    @Test
    public void shouldUpdateExistingConfiguration() throws SQLException {
        Transaction.performInOne(() -> {
            final TwoFactorAuthentication authentication = authentication(SECRET);
            try (Connection connection = connection()) {
                repository.save(connection, authentication);
                final Instant lockedUntil = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);
                final TwoFactorAuthentication updated = authentication(UPDATED_SECRET).toBuilder()
                        .status(TwoFactorAuthentication.Status.ENABLED)
                        .failedAttempts(2)
                        .lockedUntil(lockedUntil)
                        .build();
                repository.save(connection, updated);
                final Optional<TwoFactorAuthentication> stored = repository.get(connection, USER_ID);
                assertThat(stored.isPresent(), is(true));
                assertThat(stored.get().getSecret(), is(UPDATED_SECRET));
                assertThat(stored.get().getStatus(), is(TwoFactorAuthentication.Status.ENABLED));
                assertThat(stored.get().getFailedAttempts(), is(2));
                assertThat(stored.get().getLockedUntil(), is(lockedUntil));
            }
            return null;
        });
    }

    @Test
    public void shouldPersistLastUsedAt() throws SQLException {
        Transaction.performInOne(() -> {
            final Instant lastUsedAt = Instant.parse("2026-09-22T08:00:00Z");
            try (Connection connection = connection()) {
                repository.save(connection, authentication(SECRET));
                repository.updateLastUsedAt(connection, USER_ID, lastUsedAt);
                final Optional<TwoFactorAuthentication> stored = repository.get(connection, USER_ID);
                assertThat(stored.isPresent(), is(true));
                assertThat(stored.get().getLastUsedAt(), is(lastUsedAt));
            }
            return null;
        });
    }

    @Test
    public void shouldDeleteConfiguration() throws SQLException {
        Transaction.performInOne(() -> {
            try (Connection connection = connection()) {
                repository.save(connection, authentication(SECRET));
                repository.delete(connection, USER_ID);
                assertThat(repository.get(connection, USER_ID), is(Optional.empty()));
            }
            return null;
        });
    }

    private TwoFactorAuthentication authentication(final String secret) {
        return TwoFactorAuthentication.builder(USER_ID)
                .secret(secret)
                .status(TwoFactorAuthentication.Status.PENDING)
                .build();
    }

}

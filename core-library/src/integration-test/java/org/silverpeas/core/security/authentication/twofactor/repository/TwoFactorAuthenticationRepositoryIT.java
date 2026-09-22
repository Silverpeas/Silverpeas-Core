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

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@RunWith(Arquillian.class)
public class TwoFactorAuthenticationRepositoryIT {

    private static final int USER_ID = 42;
    private static final String SECRET = "JBSWY3DPEHPK3PXP";
    private static final String UPDATED_SECRET = "KRUGS4ZANFZSAYJA";

    private static final Operation TABLE_CREATION = Operations.sql(
            "CREATE TABLE ST_User_2FA (" +
                    "userId INT NOT NULL, " +
                    "secret VARCHAR(1024) NOT NULL, " +
                    "status VARCHAR(20) NOT NULL, " +
                    "createdAt TIMESTAMP NOT NULL, " +
                    "updatedAt TIMESTAMP NOT NULL, " +
                    "lastUsedAt TIMESTAMP, " +
                    "failedAttempts INT NOT NULL DEFAULT 0, " +
                    "lockedUntil TIMESTAMP" +
                    ")");

    @Rule
    public DbSetupRule dbSetupRule = DbSetupRule.createDefaultTables()
            .loadInitialDataSetFrom(TABLE_CREATION);

    @Deployment
    public static Archive<?> createTestArchive() {
        return LibCoreWarBuilder.onWarForTestClass(TwoFactorAuthenticationRepositoryIT.class)
                .addPackages(true, "org.silverpeas.core.security.authentication.twofactor.repository")
                .addPackages(true, "org.silverpeas.core.security.encryption")
                .addMavenDependencies("org.bouncycastle:bcpkix-jdk18on")
                .build();
    }

    private final TwoFactorAuthenticationRepository repository =
            new TwoFactorAuthenticationRepositoryImpl();

    @Before
    public void initializeEncryptionKey() throws Exception {
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

    @Test
    public void shouldReturnEmptyWhenNoConfigurationExists() throws SQLException {
        try (Connection connection = dbSetupRule.getSafeConnection()) {
            assertThat(repository.get(connection, USER_ID), is(Optional.empty()));
        }
    }

    @Test
    public void shouldSaveConfiguration() throws SQLException {
        final TwoFactorAuthentication authentication = authentication(SECRET);

        try (Connection connection = dbSetupRule.getSafeConnection()) {
            repository.save(connection, authentication);

            final Optional<TwoFactorAuthentication> stored = repository.get(connection, USER_ID);
            assertThat(stored.isPresent(), is(true));
            assertThat(stored.get().getUserId(), is(USER_ID));
            assertThat(stored.get().getSecret(), is(SECRET));
            assertThat(stored.get().getStatus(), is(TwoFactorAuthentication.Status.PENDING));
            assertThat(stored.get().getFailedAttempts(), is(0));
            assertThat(stored.get().getLockedUntil(), nullValue());
        }
    }

    @Test
    public void shouldEncryptSecretWhenSaving() throws SQLException {
        final TwoFactorAuthentication authentication = authentication(SECRET);

        try (Connection connection = dbSetupRule.getSafeConnection()) {
            repository.save(connection, authentication);

            final String storedSecret = JdbcSqlQuery
                    .select("secret")
                    .from("ST_User_2FA")
                    .where("userId = ?", USER_ID)
                    .executeUniqueWith(connection, rs -> rs.getString("secret"));

            assertThat(storedSecret, is(not(SECRET)));
            assertThat(storedSecret, is(not(nullValue())));
        }
    }

    @Test
    public void shouldUpdateExistingConfiguration() throws SQLException {
        final TwoFactorAuthentication authentication = authentication(SECRET);

        try (Connection connection = dbSetupRule.getSafeConnection()) {
            repository.save(connection, authentication);

            final TwoFactorAuthentication updated = authentication(UPDATED_SECRET).toBuilder()
                    .status(TwoFactorAuthentication.Status.ENABLED)
                    .failedAttempts(2)
                    .lockedUntil(Instant.now().plusSeconds(60))
                    .build();

            repository.save(connection, updated);

            final Optional<TwoFactorAuthentication> stored = repository.get(connection, USER_ID);
            assertThat(stored.isPresent(), is(true));
            assertThat(stored.get().getSecret(), is(UPDATED_SECRET));
            assertThat(stored.get().getStatus(), is(TwoFactorAuthentication.Status.ENABLED));
            assertThat(stored.get().getFailedAttempts(), is(2));
            assertThat(stored.get().getLockedUntil(), is(updated.getLockedUntil()));
        }
    }

    @Test
    public void shouldPersistLastUsedAt() throws SQLException {
        final Instant lastUsedAt = Instant.parse("2026-09-22T08:00:00Z");

        try (Connection connection = dbSetupRule.getSafeConnection()) {
            repository.save(connection, authentication(SECRET));
            repository.updateLastUsedAt(connection, USER_ID, lastUsedAt);

            final Optional<TwoFactorAuthentication> stored = repository.get(connection, USER_ID);
            assertThat(stored.isPresent(), is(true));
            assertThat(stored.get().getLastUsedAt(), is(lastUsedAt));
        }
    }

    @Test
    public void shouldDeleteConfiguration() throws SQLException {
        try (Connection connection = dbSetupRule.getSafeConnection()) {
            repository.save(connection, authentication(SECRET));
            repository.delete(connection, USER_ID);

            assertThat(repository.get(connection, USER_ID), is(Optional.empty()));
        }
    }

    private TwoFactorAuthentication authentication(final String secret) {
        return TwoFactorAuthentication.builder(USER_ID)
                .secret(secret)
                .status(TwoFactorAuthentication.Status.PENDING)
                .build();
    }
}

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
package org.silverpeas.core.security.authentication.twofactor;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.authentication.twofactor.repository.TwoFactorAuthenticationRepository;
import org.silverpeas.core.security.totp.TotpService;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import java.util.Optional;

/**
 * Default implementation of the native two-factor authentication service.
 */
@Service
@Transactional(Transactional.TxType.SUPPORTS)
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService {

    private static final SettingBundle AUTHENTICATION_SETTINGS = ResourceLocator.getSettingBundle(
            "org.silverpeas.authentication.settings.authenticationSettings");

    @Inject
    private TwoFactorAuthenticationRepository repository;

    @Inject
    private TotpService totpService;

    protected TwoFactorAuthenticationServiceImpl() {
    }

    protected TwoFactorAuthenticationServiceImpl(
            final TwoFactorAuthenticationRepository repository,
            final TotpService totpService) {
        this.repository = repository;
        this.totpService = totpService;
    }

    @Override
    public Optional<TwoFactorAuthentication> getAuthentication(final int userId) {
        validateUserId(userId);
        try (Connection connection = openConnection()) {
            return repository.get(connection, userId);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Unable to get two-factor authentication for user " + userId, e);
        }
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public TwoFactorAuthentication startEnrollment(final int userId) {
        validateUserId(userId);
        try (Connection connection = openConnection()) {
            final Optional<TwoFactorAuthentication> current =
                    repository.get(connection, userId);
            if (current.isPresent() && current.get().isEnabled()) {
                throw new IllegalStateException(
                        "Two-factor authentication is already enabled for user " + userId);
            }

            final Instant now = Instant.now();
            final TwoFactorAuthentication authentication =
                    TwoFactorAuthentication.builder(userId)
                            .secret(totpService.generateSecret())
                            .status(TwoFactorAuthentication.Status.PENDING)
                            .createdAt(current.map(TwoFactorAuthentication::getCreatedAt)
                                    .orElse(now))
                            .updatedAt(now)
                            .lastUsedAt(null)
                            .failedAttempts(0)
                            .lockedUntil(null)
                            .build();

            repository.save(connection, authentication);
            return authentication;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Unable to start two-factor authentication enrollment for user " + userId,
                    e);
        }
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public boolean confirmEnrollment(final int userId, final String code) {
        validateUserId(userId);
        if (code == null || code.isBlank()) {
            return false;
        }

        try (Connection connection = openConnection()) {
            final Optional<TwoFactorAuthentication> current =
                    repository.get(connection, userId);
            if (current.isEmpty() || !current.get().isPending()) {
                return false;
            }

            final TwoFactorAuthentication authentication = current.get();
            final Instant now = Instant.now();
            if (authentication.isLocked(now)) {
                return false;
            }
            if (!totpService.validate(authentication.getSecret(), code)) {
                final int failedAttempts = authentication.getFailedAttempts() + 1;
                final int maxAttempts = AUTHENTICATION_SETTINGS.getInteger(
                        "twoFactorTotpMaxAttempts", 5);
                if (failedAttempts >= maxAttempts) {
                    final int lockDuration = AUTHENTICATION_SETTINGS.getInteger(
                            "twoFactorTotpLockDuration", 300);
                    repository.updateFailedAttempts(connection, userId, failedAttempts,
                            now.plusSeconds(lockDuration));
                } else {
                    repository.updateFailedAttempts(connection, userId, failedAttempts, null);
                }
                return false;
            }

            repository.resetFailedAttempts(connection, userId);
            repository.save(
                    connection,
                    authentication.toBuilder()
                            .status(TwoFactorAuthentication.Status.ENABLED)
                            .updatedAt(now)
                            .build());
            return true;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Unable to confirm two-factor authentication enrollment for user " + userId,
                    e);
        }
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public boolean validate(final int userId, final String code) {
        validateUserId(userId);
        if (code == null || code.isBlank()) {
            return false;
        }

        try (Connection connection = openConnection()) {
            final Optional<TwoFactorAuthentication> current =
                    repository.get(connection, userId);
            if (current.isEmpty() || !current.get().isEnabled()) {
                return false;
            }

            final TwoFactorAuthentication authentication = current.get();
            if (!totpService.validate(authentication.getSecret(), code)) {
                return false;
            }

            repository.updateLastUsedAt(connection, userId, Instant.now());
            return true;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Unable to validate two-factor authentication for user " + userId, e);
        }
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public void disable(final int userId) {
        validateUserId(userId);
        try (Connection connection = openConnection()) {
            repository.delete(connection, userId);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Unable to disable two-factor authentication for user " + userId, e);
        }
    }

    /**
     * Opens the database connection used by this service.
     *
     * <p>The method is protected to allow unit tests to provide an isolated
     * connection without mocking the static database utility.</p>
     */
    protected Connection openConnection() throws SQLException {
        return DBUtil.openConnection();
    }

    private void validateUserId(final int userId) {
        if (userId < 0) {
            throw new IllegalArgumentException("The user identifier must not be negative");
        }
    }
}

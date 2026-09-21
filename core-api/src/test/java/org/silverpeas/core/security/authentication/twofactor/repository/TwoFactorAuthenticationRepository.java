/*
 * Copyright (C) 2000 - 2026 Silverpeas
 */
package org.silverpeas.core.security.authentication.twofactor.repository;

import org.silverpeas.core.security.authentication.twofactor.model.RecoveryCode;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Persistence abstraction for native two-factor authentication.
 */
public interface TwoFactorAuthenticationRepository {

    /**
     * Returns the 2FA configuration of the user.
     */
    Optional<TwoFactorAuthentication> findByUserId(int userId);

    /**
     * Creates a new configuration.
     */
    void create(TwoFactorAuthentication configuration);

    /**
     * Updates an existing configuration.
     */
    void update(TwoFactorAuthentication configuration);

    /**
     * Deletes every 2FA configuration of the user.
     */
    void delete(int userId);

    /**
     * Updates the last successful TOTP authentication.
     */
    void updateLastUsedAt(int userId, Instant instant);

    // -------------------------------------------------------------------
    // Recovery codes
    // -------------------------------------------------------------------

    List<RecoveryCode> findRecoveryCodes(int userId);

    void saveRecoveryCodes(int userId, List<RecoveryCode> codes);

    void markRecoveryCodeAsUsed(long recoveryCodeId, Instant usedAt);

    void deleteRecoveryCodes(int userId);
}
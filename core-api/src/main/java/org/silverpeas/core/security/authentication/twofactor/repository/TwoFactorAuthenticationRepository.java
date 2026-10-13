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

import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;

import java.util.Optional;
import java.time.Instant;

/**
 * Repository of the native two-factor authentication configuration of Silverpeas users.
 *
 * @author Silverpeas
 */
public interface TwoFactorAuthenticationRepository {

    /**
     * Gets the two-factor authentication configuration of a user.
     *
     * @param userId the Silverpeas user identifier.
     * @return the configuration if it exists.
     */
    Optional<TwoFactorAuthentication> get(int userId);

    /**
     * Creates or replaces the two-factor authentication configuration of a user.
     *
     * @param authentication the configuration to persist.
     */
    void save(TwoFactorAuthentication authentication);

    /**
     * Deletes the two-factor authentication configuration of a user.
     *
     * @param userId the Silverpeas user identifier.
     */
    void delete(int userId);

    /**
     * Updates the date of the last successful second-factor authentication.
     *
     * @param userId the Silverpeas user identifier.
     * @param lastUsedAt the date of the successful authentication.
     */
    void updateLastUsedAt(int userId, Instant lastUsedAt);

    /**
     * Records a failed TOTP validation and optionally locks the authentication.
     *
     * @param userId the Silverpeas user identifier.
     * @param failedAttempts the new number of failed attempts.
     * @param lockedUntil the lock expiration, or null when the user is not locked.
     */
    void updateFailedAttempts(int userId, int failedAttempts,
            Instant lockedUntil);

    /**
     * Resets the failed TOTP attempts and removes any temporary lock.
     *
     * @param userId the Silverpeas user identifier.
     */
    void resetFailedAttempts(int userId);
}
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

import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;

import java.util.List;
import java.util.Optional;

/**
 * Service managing the native two-factor authentication of Silverpeas users.
 */
public interface TwoFactorAuthenticationService {

    /**
     * Gets the two-factor authentication configuration of a user.
     *
     * @param userId the Silverpeas user identifier.
     * @return the configuration if it exists.
     */
    Optional<TwoFactorAuthentication> getAuthentication(int userId);

    /**
     * Starts the enrollment of a user's authenticator.
     *
     * <p>The generated configuration is stored in {@link
     * TwoFactorAuthentication.Status#PENDING} state until the user confirms
     * possession of the authenticator with a valid TOTP code.</p>
     *
     * @param userId the Silverpeas user identifier.
     * @return the pending two-factor authentication configuration.
     * @throws IllegalStateException if two-factor authentication is already enabled.
     */
    TwoFactorAuthentication startEnrollment(int userId);

    /**
     * Confirms a pending enrollment with a valid TOTP code.
     *
     * @param userId the Silverpeas user identifier.
     * @param code the TOTP code entered by the user.
     * @return true if the enrollment has been enabled, false otherwise.
     */
    boolean confirmEnrollment(int userId, String code);

    /**
     * Validates a TOTP code for an enabled user.
     *
     * <p>A successful validation updates the last-used date of the second
     * factor.</p>
     *
     * @param userId the Silverpeas user identifier.
     * @param code the TOTP code entered by the user.
     * @return true if the code is valid for the enabled configuration.
     */
    boolean validate(int userId, String code);

    /**
     * Generates a new set of recovery codes for an enabled user.
     *
     * <p>The returned clear-text codes are only available at generation time.
     * Their hashes are persisted instead.</p>
     *
     * @param userId the Silverpeas user identifier.
     * @return the generated recovery codes.
     */
    List<String> generateRecoveryCodes(int userId);

    /**
     * Validates and consumes one recovery code for an enabled user.
     *
     * @param userId the Silverpeas user identifier.
     * @param code the recovery code entered by the user.
     * @return true if the code was valid and has been consumed.
     */
    boolean validateRecoveryCode(int userId, String code);

    /**
     * Disables two-factor authentication for a user.
     *
     * @param userId the Silverpeas user identifier.
     */
    void disable(int userId);
}

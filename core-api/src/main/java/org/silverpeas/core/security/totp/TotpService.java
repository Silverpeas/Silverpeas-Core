/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with the
 * Silverpeas FLOSS exception.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 */

package org.silverpeas.core.security.totp;

import java.time.Instant;

/**
 * Service implementing Time-based One-Time Passwords (TOTP).
 *
 * <p>The implementation follows RFC 6238 and uses the HOTP algorithm
 * defined by RFC 4226.</p>
 */
public interface TotpService {

    /**
     * Generates a cryptographically secure random TOTP secret.
     *
     * @return the secret encoded using RFC 4648 Base32, without padding.
     */
    String generateSecret();

    /**
     * Generates the TOTP code for the current instant.
     *
     * @param secret Base32 encoded secret.
     * @return the numeric TOTP code, zero padded to the configured number of digits.
     */
    String generateCode(String secret);

    /**
     * Generates the TOTP code for the specified instant.
     *
     * <p>This method is mainly useful for tests and for interoperability
     * verification against RFC 6238 test vectors.</p>
     *
     * @param secret Base32 encoded secret.
     * @param instant instant at which the code must be generated.
     * @return the numeric TOTP code.
     */
    String generateCode(String secret, Instant instant);

    /**
     * Validates a TOTP code against the current instant.
     *
     * <p>The configured validation window is used.</p>
     *
     * @param secret Base32 encoded secret.
     * @param code code supplied by the user.
     * @return true if the code is valid.
     */
    boolean validate(String secret, String code);

    /**
     * Validates a TOTP code against the specified instant.
     *
     * @param secret Base32 encoded secret.
     * @param code code supplied by the user.
     * @param instant instant against which the code is validated.
     * @return true if the code is valid.
     */
    boolean validate(String secret, String code, Instant instant);

    /**
     * Builds an otpauth URI suitable for authenticator applications.
     *
     * @param secret Base32 encoded secret.
     * @param issuer issuer displayed by the authenticator application.
     * @param account account displayed by the authenticator application.
     * @return an otpauth URI.
     */
    String buildOtpAuthUri(String secret, String issuer, String account);
}
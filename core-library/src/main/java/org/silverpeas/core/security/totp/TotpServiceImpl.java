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

import org.silverpeas.core.annotation.Service;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Default implementation of {@link TotpService}.
 *
 * <p>The implementation deliberately uses only JDK cryptographic APIs.
 * No external TOTP or Base32 library is required.</p>
 */
@Service
public class TotpServiceImpl implements TotpService {

    private static final String SETTINGS =
            "org.silverpeas.authentication.settings.authenticationSettings";

    private static final String BASE32_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private static final int DEFAULT_SECRET_SIZE = 20;
    private static final int DEFAULT_DIGITS = 6;
    private static final int DEFAULT_PERIOD = 30;
    private static final int DEFAULT_WINDOW = 1;

    private final SecureRandom secureRandom = new SecureRandom();

    private final int secretSize;
    private final int digits;
    private final int period;
    private final int validationWindow;
    private final String issuer;

    public TotpServiceImpl() {
        SettingBundle settings = ResourceLocator.getSettingBundle(SETTINGS);
        this.secretSize = getPositiveInt(settings, "twoFactorTotpSecretSize", DEFAULT_SECRET_SIZE);
        this.digits = getPositiveInt(settings, "twoFactorTotpDigits", DEFAULT_DIGITS);
        this.period = getPositiveInt(settings, "twoFactorTotpPeriod", DEFAULT_PERIOD);
        this.validationWindow =
                getNonNegativeInt(settings, "twoFactorTotpValidationWindow", DEFAULT_WINDOW);
        this.issuer = getString(settings, "twoFactorTotpIssuer", "Silverpeas");
    }

    @Override
    public String generateSecret() {
        byte[] secret = new byte[secretSize];
        secureRandom.nextBytes(secret);
        return Base32.encode(secret);
    }

    @Override
    public String generateCode(final String secret) {
        return generateCode(secret, Instant.now());
    }

    @Override
    public String generateCode(final String secret, final Instant instant) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("TOTP secret must not be empty");
        }
        if (instant == null) {
            throw new IllegalArgumentException("Instant must not be null");
        }

        byte[] decodedSecret = Base32.decode(secret);

        long counter = Math.floorDiv(instant.getEpochSecond(), period);

        return generateCode(decodedSecret, counter);
    }

    @Override
    public boolean validate(final String secret, final String code) {
        return validate(secret, code, Instant.now());
    }

    @Override
    public boolean validate(
            final String secret,
            final String code,
            final Instant instant) {

        if (secret == null || secret.isBlank() || code == null || code.isBlank()) {
            return false;
        }

        if (instant == null) {
            return false;
        }

        final String normalizedCode = code.trim();

        if (normalizedCode.length() != digits || !isDigits(normalizedCode)) {
            return false;
        }

        final byte[] decodedSecret;

        try {
            decodedSecret = Base32.decode(secret);
        } catch (IllegalArgumentException e) {
            return false;
        }

        final long currentCounter = Math.floorDiv(instant.getEpochSecond(), period);

        for (long offset = -validationWindow; offset <= validationWindow; offset++) {
            final String expectedCode =
                    generateCode(decodedSecret, currentCounter + offset);

            if (MessageDigest.isEqual(
                    expectedCode.getBytes(StandardCharsets.US_ASCII),
                    normalizedCode.getBytes(StandardCharsets.US_ASCII))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String buildOtpAuthUri(final String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("TOTP secret must not be empty");
        }
        return buildOtpAuthUri(secret, issuer, null);
    }

    @Override
    public String buildOtpAuthUri(
            final String secret,
            final String issuer,
            final String account) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("TOTP secret must not be empty");
        }

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("TOTP issuer must not be empty");
        }

        /*
         * For the configured application URI, the account is deliberately
         * omitted so authenticator applications display only the issuer.
         * The three-argument method keeps the generic RFC-compatible form.
         */
        final String label = account == null || account.isBlank()
                ? issuer
                : issuer + ":" + account;

        /*
         * URI encoding is deliberately performed independently for the label
         * and issuer parameters.
         *
         * URI encoding is deliberately performed independently for the label
         * and issuer parameters.
         */
        return "otpauth://totp/"
                + encode(label)
                + "?secret="
                + encode(secret)
                + "&issuer="
                + encode(issuer)
                + "&algorithm=SHA1"
                + "&digits="
                + digits
                + "&period="
                + period;
    }

    private String generateCode(
            final byte[] secret,
            final long counter) {

        try {
            byte[] counterBytes = ByteBuffer.allocate(Long.BYTES)
                    .putLong(counter)
                    .array();

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));

            byte[] hash = mac.doFinal(counterBytes);

            int offset = hash[hash.length - 1] & 0x0F;

            int binary =
                    ((hash[offset] & 0x7F) << 24)
                            | ((hash[offset + 1] & 0xFF) << 16)
                            | ((hash[offset + 2] & 0xFF) << 8)
                            | (hash[offset + 3] & 0xFF);

            int modulo = powerOfTen(digits);
            int otp = binary % modulo;

            return String.format("%0" + digits + "d", otp);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to generate TOTP code", e);
        }
    }

    private int powerOfTen(final int exponent) {
        int result = 1;

        for (int i = 0; i < exponent; i++) {
            result *= 10;
        }

        return result;
    }

    private boolean isDigits(final String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String encode(final String value) {
        return URLEncoder.encode(value, UTF_8)
                .replace("+", "%20")
                .replace("%7E", "~");
    }

    private String getString(
            final SettingBundle settings,
            final String key,
            final String defaultValue) {
        try {
            final String value = settings.getString(key);
            return value != null && !value.isBlank() ? value.trim() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int getPositiveInt(
            final SettingBundle settings,
            final String key,
            final int defaultValue) {

        try {
            int value = settings.getInteger(key);

            return value > 0 ? value : defaultValue;

        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int getNonNegativeInt(
            final SettingBundle settings,
            final String key,
            final int defaultValue) {

        try {
            int value = settings.getInteger(key);

            return value >= 0 ? value : defaultValue;

        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Minimal RFC 4648 Base32 implementation.
     *
     * <p>Padding is omitted when encoding and accepted when decoding.</p>
     */
    private static final class Base32 {

        private Base32() {
        }

        private static String encode(final byte[] data) {
            StringBuilder result = new StringBuilder((data.length * 8 + 4) / 5);

            int buffer = 0;
            int bitsLeft = 0;

            for (byte value : data) {
                buffer = (buffer << 8) | (value & 0xFF);
                bitsLeft += 8;

                while (bitsLeft >= 5) {
                    bitsLeft -= 5;
                    result.append(
                            BASE32_ALPHABET.charAt((buffer >> bitsLeft) & 0x1F));
                }
            }

            if (bitsLeft > 0) {
                result.append(
                        BASE32_ALPHABET.charAt((buffer << (5 - bitsLeft)) & 0x1F));
            }

            return result.toString();
        }

        private static byte[] decode(final String value) {
            String normalized = value
                    .replace(" ", "")
                    .replace("-", "")
                    .replace("=", "")
                    .toUpperCase();

            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("Empty Base32 value");
            }

            byte[] result = new byte[normalized.length() * 5 / 8];

            int buffer = 0;
            int bitsLeft = 0;
            int index = 0;

            for (int i = 0; i < normalized.length(); i++) {
                char current = normalized.charAt(i);

                int valueIndex = BASE32_ALPHABET.indexOf(current);

                if (valueIndex < 0) {
                    throw new IllegalArgumentException(
                            "Invalid Base32 character: " + current);
                }

                buffer = (buffer << 5) | valueIndex;
                bitsLeft += 5;

                if (bitsLeft >= 8) {
                    bitsLeft -= 8;

                    if (index >= result.length) {
                        throw new IllegalArgumentException("Invalid Base32 value");
                    }

                    result[index++] = (byte) ((buffer >> bitsLeft) & 0xFF);
                }
            }

            if (index != result.length) {
                byte[] resized = new byte[index];
                System.arraycopy(result, 0, resized, 0, index);
                return resized;
            }

            return result;
        }
    }
}
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
 */

package org.silverpeas.core.security.totp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TotpServiceImplTest {

    private static final String SECRET =
            "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    private TotpService service;

    @BeforeEach
    void setUp() {
        service = new TotpServiceImpl();
    }

    @Test
    void shouldGenerateRfc6238CodeAt59Seconds() {
        String code = service.generateCode(
                SECRET,
                Instant.ofEpochSecond(59));

        assertThat(code, is("287082"));
    }

    @Test
    void shouldGenerateRfc6238CodeAt1111111109Seconds() {
        String code = service.generateCode(
                SECRET,
                Instant.ofEpochSecond(1111111109));

        assertThat(code, is("081804"));
    }

    @Test
    void shouldGenerateRfc6238CodeAt1111111111Seconds() {
        String code = service.generateCode(
                SECRET,
                Instant.ofEpochSecond(1111111111));

        assertThat(code, is("050471"));
    }

    @Test
    void shouldGenerateRfc6238CodeAt1234567890Seconds() {
        String code = service.generateCode(
                SECRET,
                Instant.ofEpochSecond(1234567890));

        assertThat(code, is("005924"));
    }

    @Test
    void shouldGenerateRfc6238CodeAt2000000000Seconds() {
        String code = service.generateCode(
                SECRET,
                Instant.ofEpochSecond(2000000000));

        assertThat(code, is("279037"));
    }

    @Test
    void shouldGenerateRfc6238CodeAt20000000000Seconds() {
        String code = service.generateCode(
                SECRET,
                Instant.ofEpochSecond(20000000000L));

        assertThat(code, is("353130"));
    }

    @Test
    void shouldGenerateValidRandomSecret() {
        String secret = service.generateSecret();

        assertThat(secret.length(), is(32));
        assertThat(service.validate(secret, service.generateCode(secret)), is(true));
    }

    @Test
    void shouldValidateCurrentPeriodCode() {
        Instant instant = Instant.ofEpochSecond(300);
        String code = service.generateCode(SECRET, instant);

        assertThat(service.validate(SECRET, code, instant), is(true));
    }

    @Test
    void shouldValidatePreviousPeriodCodeWithinValidationWindow() {
        Instant instant = Instant.ofEpochSecond(300);
        String code = service.generateCode(SECRET, instant.minusSeconds(30));

        assertThat(service.validate(SECRET, code, instant), is(true));
    }

    @Test
    void shouldValidateNextPeriodCodeWithinValidationWindow() {
        Instant instant = Instant.ofEpochSecond(300);
        String code = service.generateCode(SECRET, instant.plusSeconds(30));

        assertThat(service.validate(SECRET, code, instant), is(true));
    }

    @Test
    void shouldRejectCodeOutsideValidationWindow() {
        Instant instant = Instant.ofEpochSecond(300);

        String previousCode = service.generateCode(SECRET, instant.minusSeconds(60));
        String nextCode = service.generateCode(SECRET, instant.plusSeconds(60));

        assertThat(service.validate(SECRET, previousCode, instant), is(false));
        assertThat(service.validate(SECRET, nextCode, instant), is(false));
    }

    @Test
    void shouldRejectInvalidCode() {
        assertThat(
                service.validate(
                        SECRET,
                        "000000",
                        Instant.ofEpochSecond(59)),
                is(false));
    }

    @Test
    void shouldRejectInvalidBase32Secret() {
        assertThat(
                service.validate(
                        "INVALID-BASE32!",
                        "000000",
                        Instant.ofEpochSecond(300)),
                is(false));
    }

    @Test
    void shouldRejectCodeWithWrongLength() {
        Instant instant = Instant.ofEpochSecond(300);
        String validCode = service.generateCode(SECRET, instant);

        assertThat(service.validate(SECRET, validCode.substring(1), instant), is(false));
        assertThat(service.validate(SECRET, "0" + validCode, instant), is(false));
    }

    @Test
    void shouldRejectCodeContainingNonDigit() {
        Instant instant = Instant.ofEpochSecond(300);
        String validCode = service.generateCode(SECRET, instant);
        String invalidCode = "A" + validCode.substring(1);

        assertThat(service.validate(SECRET, invalidCode, instant), is(false));
    }

    @Test
    void shouldAcceptCodeWithSurroundingWhitespace() {
        Instant instant = Instant.ofEpochSecond(300);
        String code = service.generateCode(SECRET, instant);

        assertThat(service.validate(SECRET, "  " + code + "  ", instant), is(true));
    }

    @Test
    void shouldRejectNullOrBlankValidationParameters() {
        Instant instant = Instant.ofEpochSecond(300);

        assertThat(service.validate(null, "123456", instant), is(false));
        assertThat(service.validate("   ", "123456", instant), is(false));
        assertThat(service.validate(SECRET, null, instant), is(false));
        assertThat(service.validate(SECRET, "   ", instant), is(false));
        assertThat(service.validate(SECRET, "123456", null), is(false));
    }

    @Test
    void shouldRejectNullOrBlankSecretWhenGeneratingCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateCode(null, Instant.ofEpochSecond(300)));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateCode("   ", Instant.ofEpochSecond(300)));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateCode(SECRET, null));
    }

    @Test
    void shouldRejectNullOrBlankParametersWhenBuildingOtpAuthUri() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.buildOtpAuthUri(null, "Silverpeas", "sebastien"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.buildOtpAuthUri("   ", "Silverpeas", "sebastien"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.buildOtpAuthUri(SECRET, null, "sebastien"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.buildOtpAuthUri(SECRET, "   ", "sebastien"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.buildOtpAuthUri(SECRET, "Silverpeas", null));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.buildOtpAuthUri(SECRET, "Silverpeas", "   "));
    }

    @Test
    void shouldGenerateOtpAuthUri() {
        String uri = service.buildOtpAuthUri(
                SECRET,
                "Silverpeas",
                "sebastien");

        assertThat(
                uri,
                is("otpauth://totp/Silverpeas%3Asebastien"
                        + "?secret=GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"
                        + "&issuer=Silverpeas"
                        + "&algorithm=SHA1"
                        + "&digits=6"
                        + "&period=30"));
    }


    @Test
    void shouldGenerateConfiguredApplicationOnlyOtpAuthUri() {
        String uri = service.buildOtpAuthUri(SECRET);

        assertThat(
                uri,
                is("otpauth://totp/Silverpeas"
                        + "?secret=GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"
                        + "&issuer=Silverpeas"
                        + "&algorithm=SHA1"
                        + "&digits=6"
                        + "&period=30"));
    }

    @Test
    void shouldEncodeOtpAuthUriLabelAndIssuerParameters() {
        String uri = service.buildOtpAuthUri(
                SECRET,
                "Silverpeas & R&D",
                "sébastien@example.com");

        assertThat(
                uri,
                is("otpauth://totp/Silverpeas%20%26%20R%26D%3As%C3%A9bastien%40example.com"
                        + "?secret=GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"
                        + "&issuer=Silverpeas%20%26%20R%26D"
                        + "&algorithm=SHA1"
                        + "&digits=6"
                        + "&period=30"));
    }
}

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

class TotpServiceImplTest {

    private TotpService service;

    @BeforeEach
    void setUp() {
        service = new TotpServiceImpl();
    }

    @Test
    void shouldGenerateRfc6238CodeAt59Seconds() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        String code = service.generateCode(
                secret,
                Instant.ofEpochSecond(59));

        assertThat(code, is("287082"));
    }

    @Test
    void shouldGenerateRfc6238CodeAt1111111109Seconds() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        String code = service.generateCode(
                secret,
                Instant.ofEpochSecond(1111111109));

        assertThat(code, is("081804"));
    }

    @Test
    void shouldGenerateRfc6238CodeAt1111111111Seconds() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        String code = service.generateCode(
                secret,
                Instant.ofEpochSecond(1111111111));

        assertThat(code, is("050471"));
    }

    @Test
    void shouldGenerateRfc6238CodeAt1234567890Seconds() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        String code = service.generateCode(
                secret,
                Instant.ofEpochSecond(1234567890));

        assertThat(code, is("005924"));
    }

    @Test
    void shouldGenerateRfc6238CodeAt2000000000Seconds() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        String code = service.generateCode(
                secret,
                Instant.ofEpochSecond(2000000000));

        assertThat(code, is("279037"));
    }

    @Test
    void shouldGenerateRfc6238CodeAt20000000000Seconds() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        String code = service.generateCode(
                secret,
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
    void shouldRejectInvalidCode() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        assertThat(
                service.validate(
                        secret,
                        "000000",
                        Instant.ofEpochSecond(59)),
                is(false));
    }

    @Test
    void shouldGenerateOtpAuthUri() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        String uri = service.buildOtpAuthUri(
                secret,
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
}
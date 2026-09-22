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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.authentication.twofactor.repository.TwoFactorAuthenticationRepository;
import org.silverpeas.core.security.totp.TotpService;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwoFactorAuthenticationServiceImplTest {

    private static final int USER_ID = 42;
    private static final String SECRET = "JBSWY3DPEHPK3PXP";
    private static final String CODE = "123456";

    @Mock
    private TwoFactorAuthenticationRepository repository;

    @Mock
    private TotpService totpService;

    @Mock
    private Connection connection;

    private TwoFactorAuthenticationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TestableTwoFactorAuthenticationService(repository, totpService, connection);
    }

    @Test
    void shouldGetAuthentication() throws Exception {
        final TwoFactorAuthentication authentication =
                authentication(TwoFactorAuthentication.Status.ENABLED);
        when(repository.get(connection, USER_ID)).thenReturn(Optional.of(authentication));

        final Optional<TwoFactorAuthentication> result = service.getAuthentication(USER_ID);

        assertEquals(Optional.of(authentication), result);
        verify(repository).get(connection, USER_ID);
    }

    @Test
    void shouldStartEnrollment() throws Exception {
        when(repository.get(connection, USER_ID)).thenReturn(Optional.empty());
        when(totpService.generateSecret()).thenReturn(SECRET);

        final TwoFactorAuthentication result = service.startEnrollment(USER_ID);

        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        assertEquals(SECRET, result.getSecret());
        assertEquals(TwoFactorAuthentication.Status.PENDING, result.getStatus());
        verify(repository).save(connection, result);
    }

    @Test
    void shouldRejectStartEnrollmentWhenAlreadyEnabled() throws Exception {
        when(repository.get(connection, USER_ID)).thenReturn(
                Optional.of(authentication(TwoFactorAuthentication.Status.ENABLED)));

        assertThrows(IllegalStateException.class, () -> service.startEnrollment(USER_ID));

        verify(repository, never()).save(any(), any());
        verify(totpService, never()).generateSecret();
    }

    @Test
    void shouldRestartPendingEnrollmentWithNewSecret() throws Exception {
        final Instant createdAt = Instant.parse("2026-09-21T08:00:00Z");
        final TwoFactorAuthentication current = TwoFactorAuthentication.builder(USER_ID)
                .secret(SECRET)
                .status(TwoFactorAuthentication.Status.PENDING)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
        when(repository.get(connection, USER_ID)).thenReturn(Optional.of(current));
        when(totpService.generateSecret()).thenReturn("NEWSECRET");

        final TwoFactorAuthentication result = service.startEnrollment(USER_ID);

        assertEquals("NEWSECRET", result.getSecret());
        assertEquals(TwoFactorAuthentication.Status.PENDING, result.getStatus());
        assertEquals(createdAt, result.getCreatedAt());
        verify(repository).save(connection, result);
    }

    @Test
    void shouldConfirmEnrollment() throws Exception {
        final TwoFactorAuthentication pending =
                authentication(TwoFactorAuthentication.Status.PENDING);
        when(repository.get(connection, USER_ID)).thenReturn(Optional.of(pending));
        when(totpService.validate(SECRET, CODE)).thenReturn(true);

        final boolean result = service.confirmEnrollment(USER_ID, CODE);

        assertTrue(result);
        verify(totpService).validate(SECRET, CODE);
        verify(repository).save(eq(connection), any(TwoFactorAuthentication.class));
    }

    @Test
    void shouldRejectConfirmationWithInvalidCode() throws Exception {
        final TwoFactorAuthentication pending =
                authentication(TwoFactorAuthentication.Status.PENDING);
        when(repository.get(connection, USER_ID)).thenReturn(Optional.of(pending));
        when(totpService.validate(SECRET, CODE)).thenReturn(false);

        assertFalse(service.confirmEnrollment(USER_ID, CODE));

        verify(repository, never()).save(any(), any());
    }

    @Test
    void shouldRejectConfirmationWhenNotPending() throws Exception {
        when(repository.get(connection, USER_ID)).thenReturn(
                Optional.of(authentication(TwoFactorAuthentication.Status.ENABLED)));

        assertFalse(service.confirmEnrollment(USER_ID, CODE));

        verify(totpService, never()).validate(any(), any());
        verify(repository, never()).save(any(), any());
    }

    @Test
    void shouldRejectBlankConfirmationCode() throws Exception {
        assertFalse(service.confirmEnrollment(USER_ID, "  "));

        verify(repository, never()).get(any(), anyInt());
    }

    @Test
    void shouldValidateEnabledAuthentication() throws Exception {
        final TwoFactorAuthentication enabled =
                authentication(TwoFactorAuthentication.Status.ENABLED);
        when(repository.get(connection, USER_ID)).thenReturn(Optional.of(enabled));
        when(totpService.validate(SECRET, CODE)).thenReturn(true);

        assertTrue(service.validate(USER_ID, CODE));

        verify(totpService).validate(SECRET, CODE);
        verify(repository).updateLastUsedAt(eq(connection), eq(USER_ID), any(Instant.class));
    }

    @Test
    void shouldRejectInvalidAuthenticationCode() throws Exception {
        final TwoFactorAuthentication enabled =
                authentication(TwoFactorAuthentication.Status.ENABLED);
        when(repository.get(connection, USER_ID)).thenReturn(Optional.of(enabled));
        when(totpService.validate(SECRET, CODE)).thenReturn(false);

        assertFalse(service.validate(USER_ID, CODE));

        verify(repository, never()).updateLastUsedAt(any(), anyInt(), any(Instant.class));
    }

    @Test
    void shouldRejectValidationWhenNotEnabled() throws Exception {
        when(repository.get(connection, USER_ID)).thenReturn(
                Optional.of(authentication(TwoFactorAuthentication.Status.PENDING)));

        assertFalse(service.validate(USER_ID, CODE));

        verify(totpService, never()).validate(any(), any());
        verify(repository, never()).updateLastUsedAt(any(), anyInt(), any(Instant.class));
    }

    @Test
    void shouldRejectBlankValidationCode() throws Exception {
        assertFalse(service.validate(USER_ID, ""));

        verify(repository, never()).get(any(), anyInt());
    }

    @Test
    void shouldDisableAuthentication() throws Exception {
        service.disable(USER_ID);

        verify(repository).delete(connection, USER_ID);
    }

    @Test
    void shouldAcceptUserIdentifierZero() throws Exception {
        when(repository.get(connection, 0)).thenReturn(Optional.empty());

        assertTrue(service.getAuthentication(0).isEmpty());

        verify(repository).get(connection, 0);
    }

    @Test
    void shouldRejectNegativeUserIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> service.getAuthentication(-1));
        assertThrows(IllegalArgumentException.class, () -> service.startEnrollment(-1));
        assertThrows(IllegalArgumentException.class, () -> service.confirmEnrollment(-1, CODE));
        assertThrows(IllegalArgumentException.class, () -> service.validate(-1, CODE));
        assertThrows(IllegalArgumentException.class, () -> service.disable(-1));
    }

    @Test
    void shouldWrapDatabaseException() throws Exception {
        when(repository.get(connection, USER_ID)).thenThrow(new SQLException("database error"));

        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> service.getAuthentication(USER_ID));

        assertTrue(exception.getMessage().contains("Unable to get two-factor authentication"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    private TwoFactorAuthentication authentication(
            final TwoFactorAuthentication.Status status) {
        return TwoFactorAuthentication.builder(USER_ID)
                .secret(SECRET)
                .status(status)
                .createdAt(Instant.parse("2026-09-21T08:00:00Z"))
                .updatedAt(Instant.parse("2026-09-21T08:05:00Z"))
                .build();
    }

    private static class TestableTwoFactorAuthenticationService
            extends TwoFactorAuthenticationServiceImpl {

        private final Connection connection;

        private TestableTwoFactorAuthenticationService(
                final TwoFactorAuthenticationRepository repository,
                final TotpService totpService,
                final Connection connection) {
            super(repository, totpService);
            this.connection = connection;
        }

        @Override
        protected Connection openConnection() {
            return connection;
        }
    }
}

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
package org.silverpeas.core.security.authentication.twofactor;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.authentication.twofactor.model.RecoveryCode;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.authentication.twofactor.repository.RecoveryCodeRepository;
import org.silverpeas.core.security.authentication.twofactor.repository.TwoFactorAuthenticationRepository;
import org.silverpeas.core.security.totp.TotpService;

import java.time.Instant;
import java.util.Optional;
import jakarta.persistence.PersistenceException;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestedBean;

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

@EnableSilverTestEnv(context = JEETestContext.class)
class TwoFactorAuthenticationServiceImplTest {
  // The Silverpeas test container supports no-argument construction only.
  // Supply constructor dependencies explicitly and register the resulting tested bean.
  TwoFactorAuthenticationServiceImplTest(
      @TestManagedMock TwoFactorAuthenticationRepository repository,
      @TestManagedMock RecoveryCodeRepository recoveryCodeRepository,
      @TestManagedMock TotpService totpService,
      @TestManagedMock TrustedDeviceService trustedDeviceService) {
    this.repository = repository;
    this.recoveryCodeRepository = recoveryCodeRepository;
    this.totpService = totpService;
    this.trustedDeviceService = trustedDeviceService;
    this.service = new TwoFactorAuthenticationServiceImpl(repository, recoveryCodeRepository, trustedDeviceService, totpService);
  }


    private static final int USER_ID = 42;
    private static final String SECRET = "JBSWY3DPEHPK3PXP";
    private static final String CODE = "123456";

    @TestManagedBean
    private TwoFactorAuthenticationRepository repository;

    @TestManagedBean
    private RecoveryCodeRepository recoveryCodeRepository;

    @TestManagedBean
    private TotpService totpService;

    @TestManagedBean
    private TrustedDeviceService trustedDeviceService;

    @TestedBean
    private TwoFactorAuthenticationServiceImpl service;

    @Test
    void shouldGetAuthentication() throws Exception {
        final TwoFactorAuthentication authentication =
                authentication(TwoFactorAuthentication.Status.ENABLED);
        when(repository.get(USER_ID)).thenReturn(Optional.of(authentication));

        final Optional<TwoFactorAuthentication> result = service.getAuthentication(USER_ID);

        assertEquals(Optional.of(authentication), result);
        verify(repository).get(USER_ID);
    }

    @Test
    void shouldStartEnrollment() throws Exception {
        when(repository.get(USER_ID)).thenReturn(Optional.empty());
        when(totpService.generateSecret()).thenReturn(SECRET);

        final TwoFactorAuthentication result = service.startEnrollment(USER_ID);

        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        assertEquals(SECRET, result.getSecret());
        assertEquals(TwoFactorAuthentication.Status.PENDING, result.getStatus());
        verify(repository).save(result);
    }

    @Test
    void shouldRejectStartEnrollmentWhenAlreadyEnabled() throws Exception {
        when(repository.get(USER_ID)).thenReturn(
                Optional.of(authentication(TwoFactorAuthentication.Status.ENABLED)));

        assertThrows(IllegalStateException.class, () -> service.startEnrollment(USER_ID));

        verify(repository, never()).save(any());
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
        when(repository.get(USER_ID)).thenReturn(Optional.of(current));
        when(totpService.generateSecret()).thenReturn("NEWSECRET");

        final TwoFactorAuthentication result = service.startEnrollment(USER_ID);

        assertEquals("NEWSECRET", result.getSecret());
        assertEquals(TwoFactorAuthentication.Status.PENDING, result.getStatus());
        assertEquals(createdAt, result.getCreatedAt());
        verify(repository).save(result);
    }

    @Test
    void shouldConfirmEnrollment() throws Exception {
        final TwoFactorAuthentication pending =
                authentication(TwoFactorAuthentication.Status.PENDING);
        when(repository.get(USER_ID)).thenReturn(Optional.of(pending));
        when(totpService.validate(SECRET, CODE)).thenReturn(true);

        final boolean result = service.confirmEnrollment(USER_ID, CODE);

        assertTrue(result);
        verify(totpService).validate(SECRET, CODE);
        verify(repository).save(any(TwoFactorAuthentication.class));
    }

    @Test
    void shouldRejectConfirmationWithInvalidCode() throws Exception {
        final TwoFactorAuthentication pending =
                authentication(TwoFactorAuthentication.Status.PENDING);
        when(repository.get(USER_ID)).thenReturn(Optional.of(pending));
        when(totpService.validate(SECRET, CODE)).thenReturn(false);

        assertFalse(service.confirmEnrollment(USER_ID, CODE));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectConfirmationWhenNotPending() throws Exception {
        when(repository.get(USER_ID)).thenReturn(
                Optional.of(authentication(TwoFactorAuthentication.Status.ENABLED)));

        assertFalse(service.confirmEnrollment(USER_ID, CODE));

        verify(totpService, never()).validate(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectBlankConfirmationCode() throws Exception {
        assertFalse(service.confirmEnrollment(USER_ID, "  "));

        verify(repository, never()).get(anyInt());
    }

    @Test
    void shouldValidateEnabledAuthentication() throws Exception {
        final TwoFactorAuthentication enabled =
                authentication(TwoFactorAuthentication.Status.ENABLED);
        when(repository.get(USER_ID)).thenReturn(Optional.of(enabled));
        when(totpService.validate(SECRET, CODE)).thenReturn(true);

        assertTrue(service.validate(USER_ID, CODE));

        verify(totpService).validate(SECRET, CODE);
        verify(repository).updateLastUsedAt(eq(USER_ID), any(Instant.class));
    }

    @Test
    void shouldRejectAuthenticationWhenLocked() throws Exception {
        final TwoFactorAuthentication locked =
                authentication(TwoFactorAuthentication.Status.ENABLED).toBuilder()
                        .failedAttempts(5)
                        .lockedUntil(Instant.now().plusSeconds(300))
                        .build();
        when(repository.get(USER_ID)).thenReturn(Optional.of(locked));

        assertFalse(service.validate(USER_ID, CODE));

        verify(totpService, never()).validate(any(), any());
        verify(repository, never()).updateFailedAttempts(anyInt(), anyInt(), any());
    }

    @Test
    void shouldRecordFailedAuthenticationAttempt() throws Exception {
        final TwoFactorAuthentication enabled =
                authentication(TwoFactorAuthentication.Status.ENABLED).toBuilder()
                        .failedAttempts(2)
                        .build();
        when(repository.get(USER_ID)).thenReturn(Optional.of(enabled));
        when(totpService.validate(SECRET, CODE)).thenReturn(false);

        assertFalse(service.validate(USER_ID, CODE));

        verify(repository).updateFailedAttempts(USER_ID, 3, null);
    }

    @Test
    void shouldLockAfterMaximumFailedAuthenticationAttempts() throws Exception {
        final TwoFactorAuthentication enabled =
                authentication(TwoFactorAuthentication.Status.ENABLED).toBuilder()
                        .failedAttempts(4)
                        .build();
        when(repository.get(USER_ID)).thenReturn(Optional.of(enabled));
        when(totpService.validate(SECRET, CODE)).thenReturn(false);

        assertFalse(service.validate(USER_ID, CODE));

        verify(repository).updateFailedAttempts(
                eq(USER_ID), eq(5), any(Instant.class));
    }

    @Test
    void shouldRejectInvalidAuthenticationCode() throws Exception {
        final TwoFactorAuthentication enabled =
                authentication(TwoFactorAuthentication.Status.ENABLED);
        when(repository.get(USER_ID)).thenReturn(Optional.of(enabled));
        when(totpService.validate(SECRET, CODE)).thenReturn(false);

        assertFalse(service.validate(USER_ID, CODE));

        verify(repository, never()).updateLastUsedAt(anyInt(), any(Instant.class));
    }

    @Test
    void shouldRejectValidationWhenNotEnabled() throws Exception {
        when(repository.get(USER_ID)).thenReturn(
                Optional.of(authentication(TwoFactorAuthentication.Status.PENDING)));

        assertFalse(service.validate(USER_ID, CODE));

        verify(totpService, never()).validate(any(), any());
        verify(repository, never()).updateLastUsedAt(anyInt(), any(Instant.class));
    }

    @Test
    void shouldRejectBlankValidationCode() throws Exception {
        assertFalse(service.validate(USER_ID, ""));

        verify(repository, never()).get(anyInt());
    }

    @Test
    void shouldGenerateRecoveryCodes() throws Exception {
        when(repository.get(USER_ID)).thenReturn(
                Optional.of(authentication(TwoFactorAuthentication.Status.ENABLED)));

        final java.util.List<String> codes = service.generateRecoveryCodes(USER_ID);

        assertEquals(10, codes.size());
        assertTrue(codes.stream().allMatch(code -> code.matches("[A-Z2-9]{10}")));
        verify(recoveryCodeRepository).deleteAll(USER_ID);
        verify(recoveryCodeRepository, org.mockito.Mockito.times(10))
                .save(any(RecoveryCode.class));
    }

    @Test
    void shouldRejectRecoveryCodeGenerationWhenTwoFactorIsNotEnabled() throws Exception {
        when(repository.get(USER_ID)).thenReturn(
                Optional.of(authentication(TwoFactorAuthentication.Status.PENDING)));

        assertThrows(IllegalStateException.class, () -> service.generateRecoveryCodes(USER_ID));

        verify(recoveryCodeRepository, never()).deleteAll(anyInt());
    }

    @Test
    void shouldValidateRecoveryCode() throws Exception {
        when(repository.get(USER_ID)).thenReturn(
                Optional.of(authentication(TwoFactorAuthentication.Status.ENABLED)));
        when(recoveryCodeRepository.consume(eq(USER_ID), any(String.class), any(Instant.class)))
                .thenReturn(true);

        assertTrue(service.validateRecoveryCode(USER_ID, "ABCD-2345-EF"));

        verify(recoveryCodeRepository).consume(eq(USER_ID), any(String.class), any(Instant.class));
    }

    @Test
    void shouldRejectInvalidRecoveryCode() throws Exception {
        when(repository.get(USER_ID)).thenReturn(
                Optional.of(authentication(TwoFactorAuthentication.Status.ENABLED)));
        when(recoveryCodeRepository.consume(eq(USER_ID), any(String.class), any(Instant.class)))
                .thenReturn(false);

        assertFalse(service.validateRecoveryCode(USER_ID, "INVALID"));
    }

    @Test
    void shouldRejectRecoveryCodeWhenLocked() throws Exception {
        final TwoFactorAuthentication locked =
                authentication(TwoFactorAuthentication.Status.ENABLED).toBuilder()
                        .failedAttempts(5)
                        .lockedUntil(Instant.now().plusSeconds(300))
                        .build();
        when(repository.get(USER_ID)).thenReturn(Optional.of(locked));

        assertFalse(service.validateRecoveryCode(USER_ID, "ABCD2345EF"));

        verify(recoveryCodeRepository, never()).consume(
                anyInt(), any(String.class), any(Instant.class));
    }

    @Test
    void shouldCountInvalidRecoveryCodeAttempt() throws Exception {
        final TwoFactorAuthentication enabled =
                authentication(TwoFactorAuthentication.Status.ENABLED).toBuilder()
                        .failedAttempts(2)
                        .build();
        when(repository.get(USER_ID)).thenReturn(Optional.of(enabled));

        assertFalse(service.validateRecoveryCode(USER_ID, "INVALID"));

        verify(repository).updateFailedAttempts(USER_ID, 3, null);
    }

    @Test
    void shouldLockAfterMaximumInvalidRecoveryCodes() throws Exception {
        final TwoFactorAuthentication enabled =
                authentication(TwoFactorAuthentication.Status.ENABLED).toBuilder()
                        .failedAttempts(4)
                        .build();
        when(repository.get(USER_ID)).thenReturn(Optional.of(enabled));

        assertFalse(service.validateRecoveryCode(USER_ID, "INVALID"));

        verify(repository).updateFailedAttempts(
                eq(USER_ID), eq(5), any(Instant.class));
    }

    @Test
    void shouldDisableAuthentication() throws Exception {
        service.disable(USER_ID);

        verify(trustedDeviceService).revokeAll(USER_ID);
        verify(repository).delete(USER_ID);
    }

    @Test
    void shouldAcceptUserIdentifierZero() throws Exception {
        when(repository.get(0)).thenReturn(Optional.empty());

        assertTrue(service.getAuthentication(0).isEmpty());

        verify(repository).get(0);
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
    void shouldPropagatePersistenceException() {
        final PersistenceException failure = new PersistenceException("database error");
        when(repository.get(USER_ID)).thenThrow(failure);
        assertEquals(failure, assertThrows(PersistenceException.class,
                () -> service.getAuthentication(USER_ID)));
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

}

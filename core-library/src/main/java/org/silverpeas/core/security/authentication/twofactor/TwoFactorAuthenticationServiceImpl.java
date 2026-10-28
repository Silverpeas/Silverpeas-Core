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

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authentication.twofactor.model.RecoveryCode;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.authentication.twofactor.repository.RecoveryCodeRepository;
import org.silverpeas.core.security.authentication.twofactor.repository.TwoFactorAuthenticationRepository;
import org.silverpeas.core.security.totp.TotpService;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

/**
 * Default implementation of the native two-factor authentication service.
 */
@Service
@Transactional(Transactional.TxType.SUPPORTS)
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService {

  private static final SettingBundle AUTHENTICATION_SETTINGS = ResourceLocator.getSettingBundle(
      "org.silverpeas.authentication.settings.authenticationSettings");

  private static final int RECOVERY_CODE_COUNT = 10;
  private static final int RECOVERY_CODE_LENGTH = 10;
  private static final String RECOVERY_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final TwoFactorAuthenticationRepository repository;
  private final RecoveryCodeRepository recoveryCodeRepository;
  private final TrustedDeviceService trustedDeviceService;
  private final TotpService totpService;

  /**
   * Creates the service with the persistence, trusted-device and TOTP collaborators.
   *
   * @param repository the authenticator configuration repository.
   * @param recoveryCodeRepository the single-use recovery code repository.
   * @param trustedDeviceService the service revoking remembered browsers on reset or disable.
   * @param totpService the authenticator secret generator and code validator.
   */
  @Inject
  protected TwoFactorAuthenticationServiceImpl(
      final TwoFactorAuthenticationRepository repository,
      final RecoveryCodeRepository recoveryCodeRepository,
      final TrustedDeviceService trustedDeviceService,
      final TotpService totpService) {
    this.repository = repository;
    this.recoveryCodeRepository = recoveryCodeRepository;
    this.trustedDeviceService = trustedDeviceService;
    this.totpService = totpService;
  }

  @Override
  public Optional<TwoFactorAuthentication> getAuthentication(final int userId) {
    validateUserId(userId);
    return repository.get(userId);
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public TwoFactorAuthentication startEnrollment(final int userId) {
    validateUserId(userId);
    final Optional<TwoFactorAuthentication> current =
        repository.get(userId);
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

    repository.save(authentication);
    return authentication;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public boolean confirmEnrollment(final int userId, final String code) {
    validateUserId(userId);
    if (code == null || code.isBlank()) {
      return false;
    }

    final Optional<TwoFactorAuthentication> current =
        repository.get(userId);
    if (current.isEmpty() || !current.get().isPending()) {
      return false;
    }

    final TwoFactorAuthentication authentication = current.get();
    if (!totpService.validate(authentication.getSecret(), code)) {
      return false;
    }

    final Instant now = Instant.now();
    repository.resetFailedAttempts(userId);
    repository.save(
        authentication.toBuilder()
            .status(TwoFactorAuthentication.Status.ENABLED)
            .updatedAt(now)
            .failedAttempts(0)
            .lockedUntil(null)
            .build());
    return true;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public boolean validate(final int userId, final String code) {
    validateUserId(userId);
    if (code == null || code.isBlank()) {
      return false;
    }

    final Optional<TwoFactorAuthentication> current =
        repository.get(userId);
    if (current.isEmpty() || !current.get().isEnabled()) {
      return false;
    }

    final TwoFactorAuthentication authentication = current.get();
    final Instant now = Instant.now();
    if (authentication.isLocked(now)) {
      return false;
    }
    if (!totpService.validate(authentication.getSecret(), code)) {
      recordFailedAttempt(userId, authentication, now);
      return false;
    }

    repository.resetFailedAttempts(userId);
    repository.updateLastUsedAt(userId, now);
    return true;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public List<String> generateRecoveryCodes(final int userId) {
    validateUserId(userId);
    final Optional<TwoFactorAuthentication> authentication =
        repository.get(userId);
    if (authentication.isEmpty() || !authentication.get().isEnabled()) {
      throw new IllegalStateException(
          "Two-factor authentication is not enabled for user " + userId);
    }

    recoveryCodeRepository.deleteAll(userId);
    final List<String> codes = new ArrayList<>(RECOVERY_CODE_COUNT);
    final Instant now = Instant.now();
    for (int i = 0; i < RECOVERY_CODE_COUNT; i++) {
      final String code = generateRecoveryCode();
      recoveryCodeRepository.save(RecoveryCode.builder(userId)
          .hash(hashRecoveryCode(code))
          .createdAt(now)
          .used(false)
          .build());
      codes.add(code);
    }
    return codes;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public boolean validateRecoveryCode(final int userId, final String code) {
    validateUserId(userId);
    if (code == null || code.isBlank()) {
      return false;
    }

    final Optional<TwoFactorAuthentication> authentication =
        repository.get(userId);
    if (authentication.isEmpty() || !authentication.get().isEnabled()) {
      return false;
    }
    final TwoFactorAuthentication current = authentication.get();
    final Instant now = Instant.now();
    if (current.isLocked(now)) {
      return false;
    }
    final boolean consumed = recoveryCodeRepository.consume(
        userId, hashRecoveryCode(normalizeRecoveryCode(code)), now);
    if (consumed) {
      repository.resetFailedAttempts(userId);
      repository.updateLastUsedAt(userId, now);
    } else {
      recordFailedAttempt(userId, current, now);
    }
    return consumed;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void resetEnrollment(final int userId) {
    validateUserId(userId);
    final Optional<TwoFactorAuthentication> authentication =
        repository.get(userId);
    if (authentication.isEmpty() || !authentication.get().isEnabled()) {
      throw new IllegalStateException(
          "Two-factor authentication is not enabled for user " + userId);
    }

    recoveryCodeRepository.deleteAll(userId);
    trustedDeviceService.revokeAll(userId);
    final Instant now = Instant.now();
    final TwoFactorAuthentication resetAuthentication =
        TwoFactorAuthentication.builder(userId)
            .secret(totpService.generateSecret())
            .status(TwoFactorAuthentication.Status.PENDING)
            .createdAt(authentication.get().getCreatedAt())
            .updatedAt(now)
            .lastUsedAt(null)
            .failedAttempts(0)
            .lockedUntil(null)
            .build();
    repository.save(resetAuthentication);
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void disable(final int userId) {
    validateUserId(userId);
    if (AUTHENTICATION_SETTINGS.getBoolean("twoFactorTotpMandatory", false)) {
      throw new IllegalStateException(
          "Two-factor authentication cannot be disabled while it is mandatory");
    }
    recoveryCodeRepository.deleteAll(userId);
    trustedDeviceService.revokeAll(userId);
    repository.delete(userId);
  }

  private void recordFailedAttempt(final int userId,
      final TwoFactorAuthentication authentication, final Instant now) {
    final int failedAttempts = authentication.getFailedAttempts() + 1;
    final int maxAttempts = AUTHENTICATION_SETTINGS.getInteger(
        "twoFactorTotpMaxAttempts", 5);
    if (failedAttempts >= maxAttempts) {
      final int lockDuration = AUTHENTICATION_SETTINGS.getInteger(
          "twoFactorTotpLockDuration", 300);
      repository.updateFailedAttempts(userId, failedAttempts,
          now.plusSeconds(lockDuration));
    } else {
      repository.updateFailedAttempts(userId, failedAttempts, null);
    }
  }

  private String generateRecoveryCode() {
    final StringBuilder code = new StringBuilder(RECOVERY_CODE_LENGTH);
    for (int i = 0; i < RECOVERY_CODE_LENGTH; i++) {
      code.append(RECOVERY_CODE_ALPHABET.charAt(
          SECURE_RANDOM.nextInt(RECOVERY_CODE_ALPHABET.length())));
    }
    return code.toString();
  }

  private String normalizeRecoveryCode(final String code) {
    return code.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
  }

  private String hashRecoveryCode(final String code) {
    try {
      return HexFormat.of().formatHex(
          MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unable to hash recovery code", e);
    }
  }

  private void validateUserId(final int userId) {
    if (userId < 0) {
      throw new IllegalArgumentException("The user identifier must not be negative");
    }
  }
}

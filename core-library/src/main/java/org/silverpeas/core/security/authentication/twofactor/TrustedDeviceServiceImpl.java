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
import java.util.Base64;
import java.util.HexFormat;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authentication.twofactor.repository.TrustedDeviceRepository;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

/**
 * JPA implementation of trusted-device management.
 *
 * <p>The browser receives only the random token. The database stores its
 * SHA-256 hash, so a database disclosure does not directly disclose usable
 * browser tokens.</p>
 */
@Service
@Transactional(Transactional.TxType.SUPPORTS)
public class TrustedDeviceServiceImpl implements TrustedDeviceService {

  private static final int TOKEN_SIZE = 32;
  private static final int MAX_USER_AGENT_LENGTH = 1024;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static final SettingBundle AUTHENTICATION_SETTINGS = ResourceLocator.getSettingBundle(
      "org.silverpeas.authentication.settings.authenticationSettings");

  private final TrustedDeviceRepository repository;

  @Inject
  protected TrustedDeviceServiceImpl(final TrustedDeviceRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String create(final int userId, final String userAgent) {
    validateUserId(userId);
    final Instant now = Instant.now();
    final Instant expiresAt = now.plusSeconds(getLifetime());
    final String token = generateToken();

    repository.save(userId, hashToken(token), now, expiresAt, normalizeUserAgent(userAgent));
    return token;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String validateAndRotate(final int userId, final String token,
      final String userAgent) {
    validateUserId(userId);
    if (token == null || token.isBlank()) {
      return null;
    }

    final String newToken = generateToken();
    return repository.rotate(userId, hashToken(token), hashToken(newToken), Instant.now(),
        normalizeUserAgent(userAgent)) ? newToken : null;
  }

  @Override
  public long getRemainingLifetime(final int userId, final String token) {
    validateUserId(userId);
    if (token == null || token.isBlank()) {
      return 0;
    }
    return repository.getExpiration(userId, hashToken(token)).map(expiresAt -> {
      final long remainingMillis = expiresAt.toEpochMilli() - Instant.now().toEpochMilli();
      return Math.max(0, (remainingMillis + 999) / 1000);
    }).orElse(0L);
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void revokeAll(final int userId) {
    validateUserId(userId);
    repository.deleteAll(userId);
  }

  private String generateToken() {
    final byte[] bytes = new byte[TOKEN_SIZE];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hashToken(final String token) {
    try {
      return HexFormat.of().formatHex(
          MessageDigest.getInstance("SHA-256")
              .digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unable to hash trusted-device token", e);
    }
  }

  private int getLifetime() {
    return Math.max(1, AUTHENTICATION_SETTINGS.getInteger(
        "twoFactorTrustedDeviceLifetime", 30 * 24 * 60 * 60));
  }

  private String normalizeUserAgent(final String userAgent) {
    if (userAgent == null) {
      return null;
    }
    return userAgent.length() <= MAX_USER_AGENT_LENGTH
        ? userAgent
        : userAgent.substring(0, MAX_USER_AGENT_LENGTH);
  }

  private void validateUserId(final int userId) {
    if (userId < 0) {
      throw new IllegalArgumentException("The user identifier must not be negative");
    }
  }
}

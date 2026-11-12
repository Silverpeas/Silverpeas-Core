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

import org.silverpeas.core.security.authentication.twofactor.repository.TwoFactorSecretRepository;
import org.silverpeas.core.security.encryption.EncryptionContentIterator;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Invalidates TOTP enrollments after a successful platform cipher-key change. TOTP secrets are
 * deliberately not renewed: users must configure a new authenticator after the change.
 */
public class TwoFactorSecretEncryptionIterator implements EncryptionContentIterator {
  private final TwoFactorSecretRepository repository;

  /**
   * Creates the lifecycle adapter registered during application initialization.
   * @param repository the repository resetting enrollment and its associated credentials.
   */
  public TwoFactorSecretEncryptionIterator(final TwoFactorSecretRepository repository) {
    this.repository = repository;
  }

  @Override
  public void init() {
    // Wait for the success callback before invalidating any enrollment.
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public Map<String, String> next() {
    throw new NoSuchElementException("TOTP enrollments are reset instead of re-encrypted");
  }

  @Override
  public void update(final Map<String, String> content) {
    throw new IllegalStateException("TOTP enrollments are reset instead of re-encrypted");
  }

  @Override
  public void onError(final Map<String, String> content, final CryptoException error) {
    throw new IllegalStateException("Unable to process the cipher-key change", error);
  }

  @Override
  public void onEncryptionKeyChanged() {
    repository.resetAll();
  }
}

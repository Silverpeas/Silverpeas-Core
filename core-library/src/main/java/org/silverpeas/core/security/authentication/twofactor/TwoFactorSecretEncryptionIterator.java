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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Includes persisted TOTP secrets in platform cipher-key renewal.
 */
public class TwoFactorSecretEncryptionIterator implements EncryptionContentIterator {
  private static final String SECRET = "secret";
  private final TwoFactorSecretRepository repository;
  private Iterator<Object[]> secrets = Collections.emptyIterator();
  private int currentUserId;

  /**
   * Creates the iterator registered during application initialization.
   * @param repository the repository of encrypted secrets.
   */
  public TwoFactorSecretEncryptionIterator(final TwoFactorSecretRepository repository) {
    this.repository = repository;
  }

  @Override
  public void init() {
    secrets = repository.getEncryptedSecrets().iterator();
  }

  @Override
  public boolean hasNext() {
    return secrets.hasNext();
  }

  @Override
  public Map<String, String> next() {
    Object[] secret = secrets.next();
    currentUserId = (Integer) secret[0];
    return Map.of(SECRET, (String) secret[1]);
  }

  @Override
  public void update(final Map<String, String> updatedContent) {
    repository.updateEncryptedSecret(currentUserId, updatedContent.get(SECRET));
  }

  @Override
  public void onError(final Map<String, String> content, final CryptoException error) {
    throw new IllegalStateException("Unable to renew the two-factor authentication secret cipher", error);
  }
}

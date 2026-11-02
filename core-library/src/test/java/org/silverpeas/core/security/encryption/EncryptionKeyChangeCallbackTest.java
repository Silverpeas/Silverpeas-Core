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

package org.silverpeas.core.security.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class EncryptionKeyChangeCallbackTest extends ContentEncryptionServiceTest {
  @BeforeEach
  @AfterEach
  void clearRegisteredIterators() throws Exception {
    ((Collection<?>) FieldUtils.readStaticField(DefaultContentEncryptionService.class,
        "contentIterators", true)).clear();
  }

  @Test
  void shouldInvokeCallbackOnlyAfterSuccessfulKeyInstallation() throws Exception {
    var iterator = mock(EncryptionContentIterator.class);
    doAnswer(i -> {
      assertNotNull(getContentEncryptionService().encryptContent("notification configuration"));
      return null;
    }).when(iterator).onEncryptionKeyChanged();
    getContentEncryptionService().registerForContentCiphering(iterator);
    getContentEncryptionService().updateCipherKey(generateAESKey());
    verify(iterator).init();
    verify(iterator).onEncryptionKeyChanged();
  }

  @Test
  void failedRenewalShouldNotInvalidateEnrollments() throws Exception {
    createKeyFileWithTheActualKey(generateAESKey());
    var iterator = mock(EncryptionContentIterator.class);
    when(iterator.hasNext()).thenReturn(true, false);
    when(iterator.next()).thenReturn(Map.of("secret", "invalid-ciphertext"));
    getContentEncryptionService().registerForContentCiphering(iterator);
    String key = generateAESKey();

    assertThrows(CryptoException.class, () -> getContentEncryptionService().updateCipherKey(key));
    verify(iterator, never()).onEncryptionKeyChanged();
  }
}

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

import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.encryption.cipher.CipherKey;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class ContentEncryptionKeyAvailabilityTest {
  @Test
  void inaccessibleKeyShouldReturnFalseAndLogOnlyAWarningWithoutSecretsOrStackTrace()
      throws Exception {
    var service = new DefaultContentEncryptionService();
    var logger = mock(SilverLogger.class);
    try (var keys = mockStatic(DefaultContentEncryptionService.class, CALLS_REAL_METHODS);
         var logs = mockStatic(SilverLogger.class)) {
      keys.when(DefaultContentEncryptionService::getActualCipherKey)
          .thenThrow(new CryptoException("Cannot read the key"));
      logs.when(() -> SilverLogger.getLogger(service)).thenReturn(logger);

      assertFalse(service.isCipherKeyDefined());

      verify(logger).warn("The Silverpeas content encryption key is missing or inaccessible");
      verifyNoMoreInteractions(logger);
    }
  }

  @Test
  void readableKeyShouldBeAvailableWithoutWarning() throws Exception {
    var service = new DefaultContentEncryptionService();
    var logger = mock(SilverLogger.class);
    try (var keys = mockStatic(DefaultContentEncryptionService.class, CALLS_REAL_METHODS);
         var logs = mockStatic(SilverLogger.class)) {
      keys.when(DefaultContentEncryptionService::getActualCipherKey)
          .thenReturn(mock(CipherKey.class));
      logs.when(() -> SilverLogger.getLogger(service)).thenReturn(logger);

      assertTrue(service.isCipherKeyDefined());
      verifyNoInteractions(logger);
    }
  }
}

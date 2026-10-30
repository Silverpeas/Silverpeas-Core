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
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.security.authentication.twofactor.repository.TwoFactorSecretRepository;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.encryption.EncryptionContentIterator;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestedBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class TwoFactorAuthenticationInitializationTest {
  // The Silverpeas test container supports no-argument construction only.
  // Supply constructor dependencies explicitly and register the resulting tested bean.
  TwoFactorAuthenticationInitializationTest(
      @TestManagedMock ContentEncryptionService encryptionService,
      @TestManagedMock TwoFactorSecretRepository repository) {
    this.encryptionService = encryptionService;
    this.repository = repository;
    this.initialization = new TwoFactorAuthenticationInitialization(encryptionService, repository);
  }

  @TestManagedBean
  private ContentEncryptionService encryptionService;
  @TestManagedBean
  private TwoFactorSecretRepository repository;
  @TestedBean
  private TwoFactorAuthenticationInitialization initialization;

  @Test
  void shouldRegisterSecretRenewalAtStartupWithoutCreatingCipherKey() throws Exception {
    initialization.init();
    ArgumentCaptor<EncryptionContentIterator> iterator =
        ArgumentCaptor.forClass(EncryptionContentIterator.class);
    verify(encryptionService).registerForContentCiphering(iterator.capture());
    assertInstanceOf(TwoFactorSecretEncryptionIterator.class, iterator.getValue());
    verify(encryptionService, never()).updateCipherKey(anyString());
    verifyNoInteractions(repository);
  }
}

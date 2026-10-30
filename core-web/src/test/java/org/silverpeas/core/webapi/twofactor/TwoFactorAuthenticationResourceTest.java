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
package org.silverpeas.core.webapi.twofactor;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import java.lang.reflect.Field;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorAuthenticationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class TwoFactorAuthenticationResourceTest {

  @Test
  void shouldGenerateRecoveryCodesForCurrentUser() throws ReflectiveOperationException {
    final TwoFactorAuthenticationService service = mock(TwoFactorAuthenticationService.class);
    final User user = mock(User.class);
    when(user.getId()).thenReturn("42");
    final var resource = new TwoFactorAuthenticationResource() {
      @Override
      protected User getUser() {
        return user;
      }
    };
    Field serviceField = TwoFactorAuthenticationResource.class.getDeclaredField("service");
    serviceField.setAccessible(true);
    serviceField.set(resource, service);
    List<String> expectedCodes = List.of("recovery-code");
    when(service.generateRecoveryCodes(42)).thenReturn(expectedCodes);

    assertSame(expectedCodes, resource.generateRecoveryCodes());
    verify(service).generateRecoveryCodes(42);
  }
}

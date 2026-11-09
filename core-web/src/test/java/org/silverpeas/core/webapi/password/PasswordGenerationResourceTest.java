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
package org.silverpeas.core.webapi.password;

import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.authentication.password.service.PasswordRulesService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class PasswordGenerationResourceTest {
  @TestManagedMock
  private PasswordRulesService service;
  @TestedBean
  private PasswordResource resource;

  @Test
  void generatesWithoutCachingTheSecret() {
    when(service.generate()).thenReturn("generated-secret");
    try (var response = resource.generate()) {
      assertEquals(200, response.getStatus());
      assertEquals("no-store", response.getHeaderString("Cache-Control"));
      assertEquals("no-cache", response.getHeaderString("Pragma"));
      assertEquals("generated-secret", ((PasswordEntity) response.getEntity()).getValue());
    }
    verify(service).generate();
    verifyNoMoreInteractions(service);
  }

  @Test
  void inconsistentPolicyReturnsServiceUnavailable() {
    when(service.generate()).thenThrow(new IllegalStateException("inconsistent policy"));
    var exception = assertThrows(WebApplicationException.class, resource::generate);
    assertEquals(503, exception.getResponse().getStatus());
  }
}

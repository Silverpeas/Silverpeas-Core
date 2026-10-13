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
import org.silverpeas.core.security.authentication.twofactor.repository.TrustedDeviceRepository;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestedBean;
import java.time.Instant;
import java.util.Optional;
import java.util.HexFormat;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class TrustedDeviceServiceImplTest {
  // The Silverpeas test container supports no-argument construction only.
  // Supply constructor dependencies explicitly and register the resulting tested bean.
  TrustedDeviceServiceImplTest(
      @TestManagedMock TrustedDeviceRepository repository) {
    this.repository = repository;
    this.service = new TrustedDeviceServiceImpl(repository);
  }

  @TestManagedBean
  private TrustedDeviceRepository repository;
  @TestedBean
  private TrustedDeviceServiceImpl service;

  @Test
  void shouldStoreOnlyHashOfRandomTokenAndTruncateUserAgent() throws Exception {
    String token = service.create(42, "a".repeat(1100));
    assertTrue(token.matches("[A-Za-z0-9_-]{43}"));
    verify(repository).save(eq(42), eq(hash(token)), any(Instant.class), any(Instant.class),
        eq("a".repeat(1024)));
  }

  @Test
  void shouldRotateOnlyValidTokens() throws Exception {
    when(repository.rotate(eq(42), eq(hash("old")), anyString(), any(), eq("Firefox")))
        .thenReturn(true);
    String rotated = service.validateAndRotate(42, "old", "Firefox");
    assertNotNull(rotated);
    assertNotEquals("old", rotated);
    verify(repository).rotate(eq(42), eq(hash("old")), eq(hash(rotated)), any(), eq("Firefox"));
    assertNull(service.validateAndRotate(42, "unknown", "Firefox"));
  }

  @Test
  void shouldRejectBlankTokensWithoutQueryingRepository() {
    assertNull(service.validateAndRotate(42, " ", "Firefox"));
    assertEquals(0, service.getRemainingLifetime(42, null));
    verifyNoInteractions(repository);
  }

  @Test
  void shouldReturnRemainingLifetimeOrZero() throws Exception {
    when(repository.getExpiration(42, hash("valid")))
        .thenReturn(Optional.of(Instant.now().plusSeconds(3600)));
    long remaining = service.getRemainingLifetime(42, "valid");
    assertTrue(remaining > 0 && remaining <= 3600);
    when(repository.getExpiration(42, hash("expired")))
        .thenReturn(Optional.of(Instant.now().minusSeconds(1)));
    assertEquals(0, service.getRemainingLifetime(42, "expired"));
    assertEquals(0, service.getRemainingLifetime(42, "unknown"));
  }

  @Test
  void shouldRevokeAllOwnersDevices() {
    service.revokeAll(42);
    verify(repository).deleteAll(42);
  }

  @Test
  void shouldRejectNegativeOwner() {
    assertThrows(IllegalArgumentException.class, () -> service.create(-1, "Firefox"));
    assertThrows(IllegalArgumentException.class, () -> service.validateAndRotate(-1, "token", null));
    assertThrows(IllegalArgumentException.class, () -> service.getRemainingLifetime(-1, "token"));
    assertThrows(IllegalArgumentException.class, () -> service.revokeAll(-1));
  }

  private String hash(String token) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
        .digest(token.getBytes(StandardCharsets.UTF_8)));
  }
}

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
package org.silverpeas.core.web.rs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.authentication.Authentication;
import org.silverpeas.core.security.authentication.AuthenticationResponse;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class RestTwoFactorChallengeTest {
  @TestManagedMock
  private Authentication authenticator;
  @TestedBean
  private HTTPAuthentication authentication;
  private HttpServletRequest request;
  private HttpSession session;
  private Map<String, Object> attributes;

  @BeforeEach
  void setUp() {
    request = mock(HttpServletRequest.class);
    session = mock(HttpSession.class);
    attributes = new HashMap<>();
    Map<String, Object> requestAttributes = new HashMap<>();
    when(request.getSession(anyBoolean())).thenReturn(session);
    when(request.getAttribute(anyString())).thenAnswer(i -> requestAttributes.get(i.getArgument(0)));
    doAnswer(i -> { requestAttributes.put(i.getArgument(0), i.getArgument(1)); return null; })
        .when(request).setAttribute(anyString(), any());
    when(session.getAttribute(anyString())).thenAnswer(i -> attributes.get(i.getArgument(0)));
    doAnswer(i -> { attributes.put(i.getArgument(0), i.getArgument(1)); return null; })
        .when(session).setAttribute(anyString(), any());
    doAnswer(i -> { attributes.remove(i.getArgument(0)); return null; })
        .when(session).removeAttribute(anyString());
  }

  @Test
  void shouldKeepFirstFactorIdentityAndExistingRetryBudget() {
    RestTwoFactorChallenge.start(request, "alice", "0");
    var challenge = RestTwoFactorChallenge.from(request).orElseThrow();
    assertEquals("alice", challenge.getLogin());
    assertEquals("0", challenge.getDomainId());
    assertTrue(RestTwoFactorChallenge.isRequired(request));
    challenge.recordFailure();
    Object expiration = attributes.get("org.silverpeas.core.web.rs.HTTPAuthentication.twoFactorExpiresAt");
    RestTwoFactorChallenge.start(request, "alice", "0");
    assertEquals(expiration, attributes.get("org.silverpeas.core.web.rs.HTTPAuthentication.twoFactorExpiresAt"));
    assertEquals(1, attributes.get("org.silverpeas.core.web.rs.HTTPAuthentication.twoFactorAttempts"));
  }

  @Test
  void shouldClearExpiredAndExhaustedChallenges() {
    RestTwoFactorChallenge.start(request, "alice", "0");
    attributes.put("org.silverpeas.core.web.rs.HTTPAuthentication.twoFactorExpiresAt", 0L);
    assertTrue(RestTwoFactorChallenge.from(request).isEmpty());
    assertTrue(attributes.isEmpty());
    RestTwoFactorChallenge.start(request, "alice", "0");
    var challenge = RestTwoFactorChallenge.from(request).orElseThrow();
    for (int attempt = 0; attempt < 5; attempt++) {
      challenge.recordFailure();
    }
    assertTrue(RestTwoFactorChallenge.from(request).isEmpty());
    assertTrue(attributes.isEmpty());
  }

  @Test
  void shouldResetChallengeWhenIdentityChanges() {
    RestTwoFactorChallenge.start(request, "alice", "0");
    RestTwoFactorChallenge.from(request).orElseThrow().recordFailure();
    RestTwoFactorChallenge.start(request, "bob", "1");
    var challenge = RestTwoFactorChallenge.from(request).orElseThrow();
    assertEquals("bob", challenge.getLogin());
    assertEquals("1", challenge.getDomainId());
    assertEquals(0, attributes.get("org.silverpeas.core.web.rs.HTTPAuthentication.twoFactorAttempts"));
  }

  @Test
  void shouldNotCreateSessionWhenLookingForChallenge() {
    when(request.getSession(false)).thenReturn(null);
    assertTrue(RestTwoFactorChallenge.from(request).isEmpty());
    verify(request, never()).getSession(true);
  }

  @Test
  void shouldRejectPendingSecondFactorOnProtectedRestResources() throws Exception {
    String credentials = Base64.getEncoder().encodeToString(
        "alice@domain0:password".getBytes(StandardCharsets.UTF_8));
    when(request.getHeader(UserPrivilegeValidation.HTTP_AUTHORIZATION))
        .thenReturn("Basic " + credentials);
    when(authenticator.authenticate(any())).thenReturn(
        AuthenticationResponse.error(AuthenticationResponse.Status.TWO_FACTOR_REQUIRED));
    var exception = assertThrows(WebApplicationException.class, () -> authentication.authenticate(
        new HTTPAuthentication.AuthenticationContext(request, mock(HttpServletResponse.class))));
    assertEquals(401, exception.getResponse().getStatus());
    assertEquals("alice", RestTwoFactorChallenge.from(request).orElseThrow().getLogin());
    assertTrue(RestTwoFactorChallenge.isRequired(request));
  }

  @Test
  void shouldRejectMissingCredentialsWithoutStartingChallenge() {
    var exception = assertThrows(WebApplicationException.class, () -> authentication.authenticate(
        new HTTPAuthentication.AuthenticationContext(request, mock(HttpServletResponse.class))));
    assertEquals(401, exception.getResponse().getStatus());
    assertFalse(RestTwoFactorChallenge.isRequired(request));
    verify(request, never()).getSession(true);
  }
}

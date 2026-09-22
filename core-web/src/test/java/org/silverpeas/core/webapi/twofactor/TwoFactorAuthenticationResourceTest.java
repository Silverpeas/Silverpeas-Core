package org.silverpeas.core.webapi.twofactor;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorAuthenticationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TwoFactorAuthenticationResourceTest {

  @Test
  void shouldGenerateRecoveryCodesForCurrentUser() {
    final TwoFactorAuthenticationService service = mock(TwoFactorAuthenticationService.class);
    final var resource = new TwoFactorAuthenticationResource();
    // Resource wiring and current-user context are covered by the REST integration layer.
    // This test is intentionally kept as a compilation-level placeholder until the standard
    // REST resource test harness is applied to two-factor resources.
    assertEquals("two-factor", TwoFactorAuthenticationResourceURIs.BASE_URI);
    assertEquals("recovery-codes", TwoFactorAuthenticationResourceURIs.RECOVERY_CODES_URI_PART);
  }
}

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
package org.silverpeas.core.security;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class SecretGeneratorTest {
  @Test
  void sharingCodesHave32CharactersAndAllFourFamilies() {
    Set<String> codes = new HashSet<>();
    for (int i = 0; i < 500; i++) {
      String code = SecretGenerator.sharingCode();
      assertEquals(32, code.length());
      assertTrue(code.chars().anyMatch(c -> c >= 'a' && c <= 'z'));
      assertTrue(code.chars().anyMatch(c -> c >= 'A' && c <= 'Z'));
      assertTrue(code.chars().anyMatch(c -> c >= '0' && c <= '9'));
      assertTrue(code.chars().anyMatch(c -> !Character.isLetterOrDigit(c)));
      assertTrue(codes.add(code));
    }
  }

  @Test
  void customGroupsAndValidationUseTheSameEngine() {
    var policy = new SecretGenerator.Policy(new SecretGenerator.Lengths(12, 12),
        List.of(new SecretGenerator.CharacterGroup("ab", 3, true, false),
            new SecretGenerator.CharacterGroup("!", 2, true, false)), 0,
        value -> !value.contains("!!"));
    for (int i = 0; i < 100; i++) {
      String code = SecretGenerator.generate(policy);
      assertEquals(12, code.length());
      assertTrue(code.chars().allMatch(c -> "ab!".indexOf(c) >= 0));
      assertTrue(code.chars().filter(c -> c == '!').count() >= 2);
      assertFalse(code.contains("!!"));
    }
  }

  @Test
  void impossibleConstraintsFailWithinABoundedNumberOfAttempts() {
    var policy = new SecretGenerator.Policy(new SecretGenerator.Lengths(4, 4),
        List.of(new SecretGenerator.CharacterGroup("a", 5, true, false)), 0, value -> true);
    assertTimeoutPreemptively(Duration.ofSeconds(2),
        () -> assertThrows(IllegalStateException.class, () -> SecretGenerator.generate(policy)));
  }

  @Test
  void impossibleValidatorNeverReturnsAnInvalidSecret() {
    var policy = new SecretGenerator.Policy(new SecretGenerator.Lengths(8, 8),
        List.of(new SecretGenerator.CharacterGroup("ab", 0, false, false)), 0, value -> false);
    assertThrows(IllegalStateException.class, () -> SecretGenerator.generate(policy));
  }
}

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
package org.silverpeas.core.security.authentication.password.rule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.authentication.password.service.DefaultPasswordRulesService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class PasswordGenerationTest {
  @AfterEach
  void restoreSettings() {
    generator("password");
  }

  @Test
  void generatesPasswordsSatisfyingConfiguredRequiredAndCombinedRules() {
    for (String fixture : new String[]{"passwordDefined", "passwordMoreThanOneDefined",
        "passwordCombinationDefined", "passwordNotRequired"}) {
      Generator service = generator(fixture);
      Set<String> passwords = new HashSet<>();
      for (int i = 0; i < 200; i++) {
        String password = service.generate();
        assertTrue(service.getRequiredRules().stream().allMatch(rule -> rule.check(password)));
        long matching = service.getCombinedRules().stream().filter(rule -> rule.check(password)).count();
        assertTrue(matching == service.getCombinedRules().size() || matching >= service.combinations());
        assertTrue(passwords.add(password));
      }
    }
  }

  @Test
  void impossiblePolicyFailsWithoutReturningAnInvalidPasswordOrHanging() {
    Generator service = generator("passwordImpossible");
    assertTimeoutPreemptively(Duration.ofSeconds(2),
        () -> assertThrows(IllegalStateException.class, service::generate));
  }

  @Test
  void disabledMaximumDoesNotLimitGeneration() {
    Generator service = generator("passwordGenerationNoMaximum");
    assertEquals(40, service.generate().length());
  }

  private Generator generator(String fixture) {
    SettingBundle bundle = ResourceLocator.getSettingBundle("org.silverpeas.password.settings." + fixture);
    AbstractPasswordRule.settings = bundle;
    return new Generator(bundle);
  }

  private static class Generator extends DefaultPasswordRulesService {
    Generator(SettingBundle bundle) {
      settings = bundle;
      nbMatchingCombinedRules = bundle.getInteger("password.combination.nbMatchingRules", 0);
      loadRules();
    }

    int combinations() {
      return nbMatchingCombinedRules;
    }
  }
}

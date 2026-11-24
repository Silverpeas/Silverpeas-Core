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

package org.silverpeas.core.admin.configuration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveConfigurationTest {

  @ParameterizedTest
  @ValueSource(strings = {"smtp.password", "LDAP.PassWord", "passwd", "db.pwd", "passPhrase",
      "clientSecret", "oauth.access_token", "credentials", "private-key", "api_key", "accessKey",
      "security.code", "securityCode", "verificationCode", "recovery-code", "signing.key",
      "encryptionKey", "motDePasse", "codeDeSécurité"})
  void detectsSensitiveNamesRegardlessOfCaseAndSeparators(String key) {
    assertTrue(SensitiveConfiguration.isSensitive(key));
  }

  @ParameterizedTest
  @ValueSource(strings = {"templatePath", "logo", "charset", "security.enabled", "domainId",
      "a11y.enable", "httpServerBase", "code.language"})
  void keepsOrdinarySettingsAvailable(String key) {
    assertFalse(SensitiveConfiguration.isSensitive(key));
  }
}

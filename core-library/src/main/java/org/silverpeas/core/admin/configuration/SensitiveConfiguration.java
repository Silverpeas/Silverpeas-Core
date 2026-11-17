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

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Shared automatic classification for console access and audit masking. */
final class SensitiveConfiguration {

  private static final Pattern SENSITIVE_KEY = Pattern.compile(
      "password|passwd|pwd|passphrase|secret|credential|token|privatekey|apikey|accesskey|" +
          "securitycode|securecode|verificationcode|validationcode|recoverycode|backupcode|" +
          "clientkey|signingkey|encryptionkey|motdepasse|codedesecurite|codesecurite");

  private SensitiveConfiguration() {
  }

  static boolean isSensitive(String key) {
    var normalized = Normalizer.normalize(key, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    return SENSITIVE_KEY.matcher(normalized).find();
  }
}

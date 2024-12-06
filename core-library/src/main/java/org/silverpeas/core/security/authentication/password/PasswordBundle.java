/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

package org.silverpeas.core.security.authentication.password;

import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;

import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.Objects;

/**
 * A l10n bundles with all the messages about the rules and the errors on password settings.
 *
 * @author mmoquillon
 */
public class PasswordBundle {

  private static final String BUNDLE_PATH = "org.silverpeas.password.multilang.passwordBundle";

  private final String language;

  public PasswordBundle(String language) {
    this.language = language;
  }

  /**
   * Gets the message mapped to the specified key by applying on it the given optional parameters.
   * @param key the key of the message in the bundle.
   * @param params optionally one or more parameters to include within the message. Those
   * parameters shouldn't be null otherwise they won't be applied.
   * @return the message mapped to the given key in the bundle.
   */
  public String getString(@NonNull final String key, final String... params) {
    LocalizationBundle messages = ResourceLocator.getLocalizationBundle(BUNDLE_PATH, language);
    String translation;
    try {
      boolean hasParams = params != null
          && params.length > 0
          && Arrays.stream(params).allMatch(Objects::nonNull);
      translation = hasParams ? messages.getStringWithParams(key, (Object[]) params) :
          messages.getString(key);
    } catch (MissingResourceException ex) {
      translation = "";
    }
    return translation;
  }
}
  
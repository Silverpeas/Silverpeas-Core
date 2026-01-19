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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.i18n;

import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * L18n/L10n settings as they are configured for the current Silverpeas.
 *
 * @author mmoquillon
 */
public class I18nSettings {

  private static final String CONTENT_LANGUAGE_SETTINGS = "org.silverpeas.util.i18n";
  private static final String USER_LANGUAGE_SETTINGS =
      "org.silverpeas.personalization.settings.personalizationPeasSettings";
  private static final String LANGUAGE_NAMES = "org.silverpeas.util.multilang.i18n";

  private final SettingBundle l10n = ResourceLocator.getSettingBundle(CONTENT_LANGUAGE_SETTINGS);
  private final SettingBundle i18n = ResourceLocator.getSettingBundle(USER_LANGUAGE_SETTINGS);

  /**
   * Gets all the current supported languages for the content of the resources managed in
   * Silverpeas.
   *
   * @return a list of ISO 639-1 code of the different languages that are supported in Silverpeas
   * for the content.
   */
  public List<String> getContentLanguages() {
    return Arrays.stream(l10n.getString("languages").split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Gets the default content language if no one is specified or for no l10n applications in
   * Silverpeas.
   *
   * @return the ISO 639-1 code of the default content language.
   */
  public String getDefaultContentLanguage() {
    return getContentLanguages().get(0);
  }

  /**
   * Is the l10n capability is enabled in Silverpeas? If true, then some l10n aware applications
   * will handle contents in multi-languages.
   *
   * @return true if there is more than one language supported in Silverpeas for the content of the
   * contributions (and of the organizational resources like the spaces).
   */
  public boolean isL10nContentEnabled() {
    return getContentLanguages().size() > 1;
  }

  /**
   * Gets all the current supported languages for the user in Silverpeas.
   *
   * @return a list of ISO 639-1 code of the languages of the users, supported by Silverpeas.
   */
  public List<String> getUserLanguages() {
    return Arrays.stream(i18n.getString("languages").split(","))
        .map(String::trim)
        .collect(Collectors.toList());
  }

  /**
   * Gets the default user language of the current Silverpeas when no one is explicitly specified.
   *
   * @return the ISO 639-1 code of the default user language.
   */
  public String getDefaultUserLanguage() {
    return i18n.getString("DefaultLanguage");
  }

  /**
   * Gets the default user Locale of the current Silverpeas when no one is explicitly specified.
   *
   * @return the default Locale of the platform for the users.
   */
  public Locale getDefaultUserLocale() {
    return new Locale(getDefaultUserLanguage());
  }

  /**
   * Gets the default user time zone of the current Silverpeas when no one is explicitly specified.
   *
   * @return the default {@link ZoneId} of the platform for the users.
   */
  public ZoneId getDefaultUserZoneId() {
    return ZoneId.of(i18n.getString("DefaultZoneId"));
  }

  /**
   * Gets for the specified ISO 839-1 language codes all the {@link Language} objects with their
   * name expressed in the specified user language.
   *
   * @param userLanguage the language in which the name of the returned languages are written.
   * @param languageCodes the ISO 639-1 code of the languages for which a {@link Language}
   * representation is asked.
   * @return a list of {@link Language} instances.
   */
  public List<Language> getTranslatedLanguages(String userLanguage, Collection<String> languageCodes) {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(LANGUAGE_NAMES, userLanguage);
    return languageCodes.stream()
        .map(c -> new Language(c, bundle.getString("language_" + c)))
        .collect(Collectors.toList());
  }
}
  
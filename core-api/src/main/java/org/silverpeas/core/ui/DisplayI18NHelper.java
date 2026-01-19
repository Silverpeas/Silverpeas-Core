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
package org.silverpeas.core.ui;

import org.silverpeas.core.i18n.I18nSettings;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class permits to handle the different languages that a user can choose to display the labels
 * of the application.<br> Be careful, this class handles possible user languages and not possible
 * content languages.<br> The different content languages are managed by
 * {@code org.silverpeas.util.i18n.I18NHelper}.
 */
public class DisplayI18NHelper {

  private static final String ZONE_ID_COMPOSED_WITH_LETTERS_ONLY = "(?i)^[a-z_]+/[a-z_]+";
  private static DisplayI18NHelper instance;
  private final List<String> languages = new ArrayList<>();
  private final String defaultLanguage;
  private final List<String> zoneIds = new ArrayList<>();
  private final ZoneId defaultZoneId;

  private DisplayI18NHelper() {
    I18nSettings i18nSettings = new I18nSettings();
    defaultLanguage = i18nSettings.getDefaultUserLanguage();
    defaultZoneId = i18nSettings.getDefaultUserZoneId();
    languages.addAll(i18nSettings.getUserLanguages());

    ZoneId.getAvailableZoneIds().stream()
        .filter(s -> s.matches(ZONE_ID_COMPOSED_WITH_LETTERS_ONLY))
        .sorted().forEach(zoneIds::add);
  }

  /**
   * Returns the default language used to display user interface (UI)
   *
   * @return a String (ie : 'fr', 'en' or another two-letters code)
   */
  public static String getDefaultLanguage() {
    return getInstance().defaultLanguage;
  }

  /**
   * Returns the default zone id used dor user interface (UI)
   *
   * @return an instance of {@link ZoneId}.
   */
  public static ZoneId getDefaultZoneId() {
    return getInstance().defaultZoneId;
  }

  /**
   * Returns all languages available to display user interface
   *
   * @return a List of String (ie : 'fr', 'en' or another two-letters code)
   */
  public static List<String> getLanguages() {
    return Collections.unmodifiableList(getInstance().languages);
  }

  /**
   * Returns all zone identifiers available for user interface
   *
   * @return a List of String.
   */
  public static List<String> getZoneIds() {
    return Collections.unmodifiableList(getInstance().zoneIds);
  }

  /**
   * Verifies if the given user language is handled by the server.
   *
   * @param language the language to verify
   * @return the given user language if it is handled by the server, the default user language
   * otherwise.
   */
  public static String verifyLanguage(String language) {
    if (getInstance().languages.contains(language)) {
      return language;
    }
    return getDefaultLanguage();
  }

  /**
   * Verifies if the given user zone id is handled by the server.
   *
   * @return the given user zone id if it is handled by the server, the default user zone id
   * otherwise.
   */
  public static ZoneId verifyZoneId(String zoneId) {
    if (getInstance().zoneIds.contains(zoneId)) {
      return ZoneId.of(zoneId);
    }
    return getDefaultZoneId();
  }

  private static DisplayI18NHelper getInstance() {
    if (instance == null) {
      instance = new DisplayI18NHelper();
    }
    return instance;
  }

}
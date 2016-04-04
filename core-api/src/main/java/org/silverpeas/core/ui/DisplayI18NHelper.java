/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.ui;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class permits to handle the different languages that a user can choose to display the
 * labels of the application.<br/>
 * Be careful, this class handles possible user languages and not possible content languages.<br/>
 * The different content languages are managed by {@code org.silverpeas.util.i18n.I18NHelper}.
 */
public class DisplayI18NHelper {

  private static List<String> languages = new ArrayList<>();
  private static String defaultLanguage;

  static {
    SettingBundle settings = ResourceLocator.getSettingBundle(
        "org.silverpeas.personalization.settings.personalizationPeasSettings");

    defaultLanguage = settings.getString("DefaultLanguage");

    String[] supportedLanguages = settings.getString("languages").split(",");
    for (String lang: supportedLanguages) {
      lang = lang.trim();
      if (!lang.isEmpty()) {
        languages.add(lang);
      }
    }
  }

  /**
   * Returns the default language used to display user interface (UI)
   * @return a String (ie : 'fr', 'en' or another two-letters code)
   */
  public static String getDefaultLanguage() {
    return defaultLanguage;
  }

  /**
   * Returns all languages available to display user interface
   * @return a List of String (ie : 'fr', 'en' or another two-letters code)
   */
  public static List<String> getLanguages() {
    return Collections.unmodifiableList(languages);
  }

  /**
   * Verifies if the given user language is handled by the server.
   * @return the given user language if it is handled by the server, the default user language
   * otherwise.
   */
  public static String verifyLanguage(String language) {
    if (languages.contains(language)) {
      return language;
    }
    return getDefaultLanguage();
  }

  private DisplayI18NHelper() {
  }
}
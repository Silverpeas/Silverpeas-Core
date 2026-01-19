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
package org.silverpeas.core.i18n;

import jakarta.annotation.PostConstruct;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.annotation.Cacheable;
import org.silverpeas.kernel.util.StringUtil;

import java.util.List;

/**
 * This interface defines all the i18n related stuff as it is configured in Silverpeas: the default
 * language, all the languages supported in the current Silverpeas, and so on. The languages are the
 * ones that are supported by Silverpeas for the content of the resources managed in Silverpeas.
 * They aren't related to the user languages.
 *
 * @author mmoquillon
 */
@Service
@Cacheable
public class I18n {

  private String defaultLanguage;
  private List<String> languages;
  private boolean enabled;

  /**
   * Gets an instance of {@link I18n}.
   *
   * @return an instance of {@link I18n}
   */
  public static I18n get() {
    return ServiceProvider.getService(I18n.class);
  }

  @PostConstruct
  private void loadI18nSettings() {
    I18nSettings settings = new I18nSettings();
    languages = settings.getContentLanguages();
    defaultLanguage = settings.getDefaultContentLanguage();
    enabled = settings.isL10nContentEnabled();
  }

  /**
   * Gets the default language of the platform when no one is explicitly specified.
   *
   * @return the ISO 639-1 code of the default language.
   */
  public String getDefaultLanguage() {
    return defaultLanguage;
  }

  /**
   * Gets the languages that are supported by the platform for the resources content.
   *
   * @return a list of ISO 639-1 codes of languages.
   */
  public List<String> getSupportedLanguageCodes() {
    return List.copyOf(languages);
  }

  /**
   * Gets the languages that are supported by the platform for the resources content with their
   * name in the specified language.
   * @param userLanguage the ISO 639-1 code of the user language in which the name of the returned
   * languages should be written
   * @return a list of {@link Language} instances with their name in the specified user language.
   */
  public List<Language> getSupportedLanguages(String userLanguage) {
    I18nSettings settings = new I18nSettings();
    return settings.getTranslatedLanguages(userLanguage, languages);
  }

  /**
   * Checks the specified language is supported by the platform otherwise, the default supported one
   * is returned.
   *
   * @param language the ISO 639-1 code of a language.
   * @return either the ISO 639-1 of the specified language if it is supported by Silverpeas or,
   * otherwise, of the default language.
   */
  public String checkLanguage(String language) {
    return StringUtil.isNotDefined(language) || !languages.contains(language) ? defaultLanguage :
        language;
  }

  /**
   * Checks the specified language is the default one. It doesn't take care of the case.
   *
   * @param language the ISO 639-1 code of a language.
   * @return true if the specified language is the default one, false otherwise.
   */
  public boolean isDefaultLanguage(String language) {
    return StringUtil.isDefined(language) && language.equalsIgnoreCase(defaultLanguage);
  }

  /**
   * Is the i18n content support enabled?
   *
   * @return true if the i18n support is enabled for the resources' content in the current
   * Silverpeas.
   */
  public boolean isEnabled() {
    return enabled;
  }
}

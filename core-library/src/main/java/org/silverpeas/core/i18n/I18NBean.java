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

import java.util.Map;

/**
 * Bean handling the different translations of its textual properties.
 * @param <T> the concrete type of the translations.
 */
public interface I18NBean<T extends Translation> extends Translatable {

  /**
   * Gets all the actually translations handled by this bean.
   * @return a map with as key the ISO 631-1 code of a language and as value the translation in that
   * language.
   */
  Map<String, T> getTranslations();

  /**
   * Browses the different supported languages for a translation. The browsing starts with the
   * default language as defined by the property {@link I18n#getDefaultLanguage()}.
   * @return a translation.
   */
  T getNextTranslation();

  /**
   * Sets the language in which the default values of the textual properties of the bean are set.
   * By default, the language of those values are in the language defined by the
   * {@link I18n#getDefaultLanguage()} method
   * @param language the language.
   */
  void setLanguage(String language);

  /**
   * Sets a unique identifier for the translation by default of the bean (the default values of the
   * textual properties of the bean).
   * @param translationId a unique identifier of the default translation.
   */
  void setTranslationId(String translationId);

  void setRemoveTranslation(boolean remove);

}

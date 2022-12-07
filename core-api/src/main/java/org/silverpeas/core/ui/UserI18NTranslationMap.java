/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.silverpeas.core.util.StringUtil.EMPTY;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * This map is dedicated to simple translation management around user languages.
 * @author silveryocha
 */
public class UserI18NTranslationMap extends HashMap<String, String> {
  private static final long serialVersionUID = 3091905250165166114L;

  public UserI18NTranslationMap() {
    super();
  }

  public UserI18NTranslationMap(final Map<String, String> other) {
    super(other);
  }

  @Override
  public boolean containsKey(final Object language) {
    return Optional.ofNullable(language)
        .map(Object::toString)
        .filter(super::containsKey)
        .isPresent();
  }

  /**
   * Gets the translation corresponding to given language.
   * <p>
   * If it does not exist a translation for the given language, then a default one is retrieved
   * by checking translation existence of handled languages. In a such case, the first existing
   * translation is considered as default.
   * </p>
   * <p>
   * If default translation is not needed, then check availability with
   * {@link #containsKey(Object)} before.
   * </p>
   * @param language instance representing the language.
   * @return a string corresponding to the translation of given language, or a default
   * translation if none. Empty string if it does not exist at least one translation.
   */
  @Override
  public String get(final Object language) {
    return Optional.ofNullable(language)
        .map(Object::toString)
        .map(super::get)
        .orElseGet(() -> DisplayI18NHelper.getLanguages()
            .stream()
            .map(super::get)
            .filter(StringUtil::isDefined)
            .findFirst()
            .orElse(EMPTY));
  }

  /**
   * Puts a translation for a language.
   * <p>
   * If the translation is not defined, then the language translation entry is removed.
   * </p>
   * @param language key with which the specified value is to be associated
   * @param translation value to be associated with the specified key
   * @return the resulting of {@link Map#put(Object, Object)} if translation is defined, or the
   * resulting of {@link Map#remove(Object)} otherwise.
   */
  @Override
  public String put(final String language, final String translation) {
    if (isDefined(translation)) {
      return super.put(language, translation);
    } else {
      return super.remove(language);
    }
  }
}

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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

/**
 * An object in Silverpeas whose the textual properties are translatable in different languages.
 * In this case, the object supports the localization (l10n).
 * @author mmoquillon
 */
public interface Translatable {

  /**
   * Gets a translation in the specified language about some textual properties of the object.
   * If no such translation exists, then returns the default translation of the object.
   * @param language the ISO 631-1 code of a language.
   * @param <T> the concrete type of the translation.
   * @return a translation of the object in the given language. Can be never null.
   */
  <T extends Translation> T getTranslation(final String language);
}

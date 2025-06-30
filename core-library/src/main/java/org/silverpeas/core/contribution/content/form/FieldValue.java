/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.contribution.content.form;

/**
 * Value of a field in a form. The value is identifiable by a unique key among all the values the
 * field accept and by a l10n label.
 *
 * @author mmoquillon
 */
public class FieldValue {

  private final String key;
  private final String label;
  private final String language;

  public static FieldValue emptyFor(String language) {
    return new FieldValue("", "", language);
  }

  /**
   * Constructs a new field value to display to users.
   * @param key the unique identifier of the value among all the values of the field.
   * @param label the value localized for the country/language specified by the language parameter.
   * @param language the ISO 639-1 code of a localization.
   */
  public FieldValue(String key, String label, String language) {
    this.key = key;
    this.label = label;
    this.language = language;
  }

  /**
   * Gets the unique identifier of this value.
   * @return the value key.
   */
  public String getKey() {
    return key;
  }

  /**
   * Gets the l10n label of the value.
   * @return a textual and localized format of the value.
   */
  public String getLabel() {
    return label;
  }

  /**
   * The ISO 639-1 code of the language in which the value is expressed.
   * @return the language in which the value is written.
   */
  public String getLanguage() {
    return language;
  }
}
  
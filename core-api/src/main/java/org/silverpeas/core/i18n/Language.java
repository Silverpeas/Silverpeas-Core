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

/**
 * A language supported in Silverpeas for contents. Reification of the concept of the supported
 * language.
 *
 * @author mmoquillon
 */
public class Language {

  private final String name;
  private final String code;

  /**
   * Constructs a language for content.
   * @param code the ISO 639-1 code of the language.
   * @param name the user readable language name.
   */
  public Language(String code, String name) {
    this.name = name;
    this.code = code;
  }

  /**
   * Gets the user readable name of the language
   * @return the language name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the ISO 639-1 code of the language.
   * @return the ISO 639-1 code of the language.
   */
  public String getCode() {
    return code;
  }
}
  
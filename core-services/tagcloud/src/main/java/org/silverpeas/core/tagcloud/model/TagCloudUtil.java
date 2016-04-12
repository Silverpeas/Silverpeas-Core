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

package org.silverpeas.core.tagcloud.model;

/**
 * Utilities class. Used to generate the correct spelling for the tag of a tagcloud.
 */
public class TagCloudUtil {

  // List of characters used to generate a correct tag.
  // For each line, the characters contained in the first string has to be
  // replaced by the
  // character of the second string.
  private static final String[][] TAG_DATA = {
      { "(Ç)", "C" },
      { "(À|Á|Â|Ã|Ä|Å|Æ)", "A" },
      { "(È|É|Ê|Ë)", "E" },
      { "(Ì|Í|Î|Ï)", "I" },
      { "(Ò|Ó|Ô|Õ|Ö)", "O" },
      { "(Ù|Ú|Û|Ü)", "U" },
      { "(Ý)", "Y" }
      };

  /**
   * @param s The string to convert into a valid tag.
   * @return The tag corresponding to the string given as parameter.
   */
  public static String getTag(String s) {
    s = s.toUpperCase();
    for (int i = 0, n = TAG_DATA.length; i < n; i++) {
      s = s.replaceAll(TAG_DATA[i][0], TAG_DATA[i][1]);
    }
    return s;
  }

}
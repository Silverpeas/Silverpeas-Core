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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.util.WebEncodeHelper;

/**
 * Util class to encode special string or characters
 * @author lloiseau
 * @version 1.0
 * @deprecated Use {@link WebEncodeHelper} instead
 */
public class Encode {
  /**
   * Convert a java string to a javascript string Replace \,\n,\r and "
   * @param javastring Java string to encode
   * @return javascript string encoded
   */
  public static String javaStringToJsString(String javastring) {
    return WebEncodeHelper.javaStringToJsString(javastring);
  }

  /**
   * Convert a java string to a html string for textArea Replace ", <, >, & and \n
   * @param javastring Java string to encode
   * @return html string encoded
   */
  public static String javaStringToHtmlString(String javastring) {
    return WebEncodeHelper.javaStringToHtmlString(javastring);
  }

  public static String escapeXml(String javastring) {
    return WebEncodeHelper.escapeXml(javastring);
  }

  /**
   * Convert a java string to a html string for textfield... Replace ", <, >, & and \n
   * @param javastring Java string to encode
   * @return html string encoded
   */
  public static String javaStringToHtmlParagraphe(String javastring) {
    return WebEncodeHelper.javaStringToHtmlParagraphe(javastring);
  }

  public static String convertHTMLEntities(String text) {
    return WebEncodeHelper.convertHTMLEntities(text);
  }

}
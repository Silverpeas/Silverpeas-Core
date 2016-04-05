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

package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.util.EncodeHelper;

/**
 * Util class to encode special string or characters
 * @author lloiseau
 * @version 1.0
 * @deprecated Use {@link EncodeHelper} instead
 */
public class Encode {
  /**
   * Convert a java string to a javascript string Replace \,\n,\r and "
   * @param javastring Java string to encode
   * @return javascript string encoded
   */
  public static String javaStringToJsString(String javastring) {
    return EncodeHelper.javaStringToJsString(javastring);
  }

  /**
   * Convert a java string to a html string for textArea Replace ", <, >, & and \n
   * @param javastring Java string to encode
   * @return html string encoded
   */
  public static String javaStringToHtmlString(String javastring) {
    return EncodeHelper.javaStringToHtmlString(javastring);
  }

  public static String javaStringToXmlString(String javastring) {
    return EncodeHelper.javaStringToXmlString(javastring);
  }

  public static String escapeXml(String javastring) {
    return EncodeHelper.escapeXml(javastring);
  }

  /**
   * Convert a java string to a html string for textfield... Replace ", <, >, & and \n
   * @param javastring Java string to encode
   * @return html string encoded
   */
  public static String javaStringToHtmlParagraphe(String javastring) {
    return EncodeHelper.javaStringToHtmlParagraphe(javastring);
  }

  /**
   * Convert a html string to a java string Replace &quot
   * @param HTML string to encode
   * @return html string JAVA encoded
   */
  public static String htmlStringToJavaString(String htmlstring) {
    return EncodeHelper.htmlStringToJavaString(htmlstring);
  }

  /**
   * This method transforms a text with caracter specificly encoded for HTML by a text encoded in
   * according to the Java code.
   * @param text (String) a single text which contains a lot of forbidden caracters. This text must
   * not be null
   * @return Returns the transformed text without specific codes.
   */
  public static String transformHtmlCode(String text) {
    return EncodeHelper.transformHtmlCode(text);
  }

  /**
   * Convert a java string to a html string for textArea Replace euro symbol
   * @param javastring Java string to encode
   * @return html string encoded
   * @deprecated
   */
  public static String encodeSpecialChar(String javastring) {
    if (javastring == null)
      return "";

    return javastring;
  }

  /**
   * This method transforms a string to replace the 'special' caracters to store them correctly in
   * the database
   * @param text (String) a single text which may contains 'special' caracters
   * @return Returns the transformed text without specific codes.
   */
  public static String transformStringForBD(String sText) {
    return EncodeHelper.transformStringForBD(sText);
  }

  public static String convertHTMLEntities(String text) {
    return EncodeHelper.convertHTMLEntities(text);
  }

}
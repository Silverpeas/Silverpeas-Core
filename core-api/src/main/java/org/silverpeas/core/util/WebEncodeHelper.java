/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import org.apache.commons.lang3.StringEscapeUtils;

import javax.xml.bind.DatatypeConverter;

/**
 * Utility class to encode special string or characters to be compliant with the web (HTML and
 * Javascript). Useful to format text in HTML.
 *
 * @author lloiseau
 * @version 1.0
 * @deprecated please use instead {@link org.owasp.encoder.Encode}
 */
@Deprecated
public class WebEncodeHelper {

  /**
   * Convert a java string to a javascript string Replace \,\n,\r and "
   *
   * @param javastring Java string to encode
   * @return javascript string encoded
   */
  public static String javaStringToJsString(String javastring) {
    if (!isDefined(javastring)) {
      return "";
    }
    return StringEscapeUtils.escapeEcmaScript(javastring);
  }

  /**
   * Convert a java string to a html string for textArea Replace ", &gt;, &lt;, &amp; and \n
   *
   * @param javastring Java string to encode
   * @return html string encoded
   */
  public static String javaStringToHtmlString(String javastring) {
    if (!isDefined(javastring)) {
      return "";
    }
    return StringEscapeUtils.escapeHtml4(javastring).replace("œ", "&oelig;");
  }

  public static String escapeXml(String javastring) {
    if (isDefined(javastring)) {
      return StringEscapeUtils.escapeXml11(javastring);
    } else {
      return "";
    }
  }

  /**
   * Convert a java string to a html string for textfield... Replace ", &gt;, &lt;, &amp; and \n
   *
   * @param javastring Java string to encode
   * @return html string encoded
   */
  public static String convertWhiteSpacesForHTMLDisplay(String javastring) {
    if (!isDefined(javastring)) {
      return "";
    }
    StringBuilder resSB = new StringBuilder(javastring.length() + 10);
    for (int i = 0; i < javastring.length(); i++) {
      switch (javastring.charAt(i)) {
        case '\n':
          resSB.append("<br/>");
          break;
        case '\r':
          break;
        case '\t':
          resSB.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
          break;
        default:
          resSB.append(javastring.charAt(i));
      }
    }
    return resSB.toString();
  }

  /**
   * Convert a java string to a html string for textfield... Replace ", &gt;, &lt;, &amp; and \n
   *
   * @param javastring Java string to encode
   * @return html string encoded
   */
  public static String javaStringToHtmlParagraphe(String javastring) {
    String escapedString = javaStringToHtmlString(javastring);
    return convertWhiteSpacesForHTMLDisplay(escapedString);
  }

  /**
   * Convert a html string to a java string. Replace &quot;
   *
   * @param htmlstring HTML string to encode
   * @return html string JAVA encoded
   */
  public static String htmlStringToJavaString(String htmlstring) {
    if (!isDefined(htmlstring)) {
      return "";
    }
    return StringEscapeUtils.unescapeHtml4(htmlstring);
  }

  public static String convertHTMLEntities(String text) {
    return StringEscapeUtils.escapeHtml4(text);
  }

  /**
   * Encode an UTF-8 filename in Base64 for the content-disposition header according to
   * <a href="http://www.ietf.org/rfc/rfc2047.txt">RFC2047</a>.
   *
   * @param filename the UTF-8 filename to be encoded.
   * @return the filename to be inserted in the content-disposition header.
   */
  public static String encodeFilename(String filename) {
   return  "=?UTF-8?B?"
       + DatatypeConverter.printBase64Binary(filename.getBytes(Charsets.UTF_8))
       + "?=";
  }

  private WebEncodeHelper() {
  }

  private static boolean isDefined(String text) {
    return text != null && !text.isEmpty();
  }
}

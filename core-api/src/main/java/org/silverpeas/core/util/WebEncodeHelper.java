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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;


import org.apache.commons.text.StringEscapeUtils;

import javax.xml.bind.DatatypeConverter;

/**
 * Utility class to encode special string or characters to be compliant with the web (HTML and
 * Javascript). Useful to format text in HTML but not to encode unsafe text in HTML or Javascript.
 * To encode unsafe text, please use instead {@link org.owasp.encoder.Encode}.
 *
 * @author lloiseau
 * @version 1.0
 */
public class WebEncodeHelper {

  /**
   * Convert a java string to a javascript string. Replace \,\n,\r and "
   *
   * @param input Java string to encode
   * @return javascript string encoded
   */
  public static String javaStringToJsString(String input) {
    if (!isDefined(input)) {
      return "";
    }
    return StringEscapeUtils.escapeEcmaScript(input);
  }

  /**
   * Convert a java string to a html text for textArea value. Replace ", &gt;, &lt;, &amp; and \n
   *
   * @param input Java string to encode
   * @return html string encoded
   */
  public static String javaStringToHtmlString(String input) {
    if (!isDefined(input)) {
      return "";
    }
    return StringEscapeUtils.escapeHtml4(input).replace("œ", "&oelig;");
  }

  public static String escapeXml(String input) {
    if (isDefined(input)) {
      return StringEscapeUtils.escapeXml11(input);
    } else {
      return "";
    }
  }

  /**
   * Convert a text, possibly an HTML one, by replacing any blank tokens (tab and line-feeds) by
   * their counterpart in HTML.
   *
   * @param input a text in which blank tokens are converted in HTML.
   * @return an HTML text
   */
  public static String convertBlanksForHtml(String input) {
    if (!isDefined(input)) {
      return "";
    }
    StringBuilder resSB = new StringBuilder(input.length() + 10);
    for (int i = 0; i < input.length(); i++) {
      switch (input.charAt(i)) {
        case '\n':
          resSB.append("<br/>");
          break;
        case '\r':
          break;
        case '\t':
          resSB.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
          break;
        default:
          resSB.append(input.charAt(i));
      }
    }
    return resSB.toString();
  }

  /**
   * Convert a java string to a html string for HTML paragraph. Replace ", &gt;, &lt;, &amp; and \n
   *
   * @param input Java string to encode
   * @return html string encoded
   */
  public static String javaStringToHtmlParagraphe(String input) {
    String escapedString = javaStringToHtmlString(input);
    return convertBlanksForHtml(escapedString);
  }

  /**
   * Convert a html string to a java string. Replace &quot;
   *
   * @param input HTML text to encode
   * @return html string JAVA encoded
   */
  public static String htmlStringToJavaString(String input) {
    if (!isDefined(input)) {
      return "";
    }
    return StringEscapeUtils.unescapeHtml4(input);
  }

  public static String convertHTMLEntities(String text) {
    return StringEscapeUtils.escapeHtml4(text);
  }

  /**
   * Encode a UTF-8 filename in Base64 for the content-disposition header according to
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

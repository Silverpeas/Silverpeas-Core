/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.util;

import org.apache.commons.lang.StringEscapeUtils;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Util class to encode special string or characters
 * @author lloiseau
 * @version 1.0
 */
public class EncodeHelper extends Object {
  /**
   * Convert a java string to a javascript string Replace \,\n,\r and "
   * @param javastring Java string to encode
   * @return javascript string encoded
   */
  public static String javaStringToJsString(String javastring) {
    if (javastring == null)
      return "";

    return StringEscapeUtils.escapeJavaScript(javastring);
  }

  /**
   * Convert a java string to a html string for textArea Replace ", <, >, & and \n
   * @param javastring Java string to encode
   * @return html string encoded
   */
  public static String javaStringToHtmlString(String javastring) {
    if (javastring == null)
      return "";

    return StringEscapeUtils.escapeHtml(javastring);
  }

  public static String javaStringToXmlString(String javastring) {
    return escapeXml(javastring);
  }

  public static String escapeXml(String javastring) {
    if (javastring == null)
      return "";

    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < javastring.length(); ++i) {
      char ch = javastring.charAt(i);
      if (((int) ch) > 128) { // check if is an extended character
        if (((int) ch) == 158)
          sb.append("&#382;"); // zcaron
        else if (((int) ch) == 142)
          sb.append("&#381;"); // Zcaron
        else if (((int) ch) == 154)
          sb.append("&#353;"); // scaron
        else if (((int) ch) == 138)
          sb.append("&#352;"); // Scaron
        else if (((int) ch) == 156)
          sb.append("&#339;"); // oe
        else if (((int) ch) == 140)
          sb.append("&#338;"); // OE
        else if (((int) ch) == 146)
          sb.append("&#8217;"); // ’
        else
          sb.append("&#" + ((int) ch) + ";"); // convert extended character
      } else if (((int) ch) == 128) {
        sb.append("&#8364;");
      } else if (((int) ch) == 34) {
        sb.append("&quot;");
      } else if (((int) ch) == 38) {
        sb.append("&amp;");
      } else if (((int) ch) == 60) {
        sb.append("&lt;");
      } else if (((int) ch) == 62) {
        sb.append("&gt;");
      } else if (ch == 0x0009) {
        sb.append(""); // Horizontal Tab (9)
      } else if (ch == 0x000B) {
        sb.append(""); // Vertical Tab (0B)
      } else if (ch == 0x000C) {
        sb.append(""); // Form feed (0C)
      } else {
        sb.append(ch); // append the character as is
      }
    }

    return sb.toString();
  }

  /**
   * Convert a java string to a html string for textfield... Replace ", <, >, & and \n
   * @param javastring Java string to encode
   * @return html string encoded
   */
  public static String javaStringToHtmlParagraphe(String javastring) {
    if (javastring == null)
      return "";

    StringBuffer resSB = new StringBuffer(javastring.length() + 10);

    boolean cr = false;
    for (int i = 0; i < javastring.length(); i++) {
      switch (javastring.charAt(i)) {
        case '\n':
          if (!cr)
            resSB.append("<br/>");
          cr = false;
          break;
        case 0x000D:
          resSB.append("<br/>");
          cr = true;
          break;
        case '\t':
          resSB.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
          cr = false;
          break;
        default:
          resSB.append(javastring.charAt(i));
          cr = false;
      }
    }
    return resSB.toString();
  }

  /**
   * Convert a html string to a java string Replace &quot
   * @param HTML string to encode
   * @return html string JAVA encoded
   */
  public static String htmlStringToJavaString(String htmlstring) {
    if (htmlstring == null)
      return "";

    return StringEscapeUtils.unescapeHtml(htmlstring);
  }

  /**
   * This method transforms a text with caracter specificly encoded for HTML by a text encoded in
   * according to the Java code.
   * @param text (String) a single text which contains a lot of forbidden caracters. This text must
   * not be null
   * @return Returns the transformed text without specific codes.
   */
  public static String transformHtmlCode(String text) {
    SilverTrace.info("util", "Encode.transformHtmlCode()",
        "root.MSG_GEN_PARAM_VALUE", " text recu " + text);

    return StringEscapeUtils.unescapeHtml(text);
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
    if (sText == null)
      return "";

    SilverTrace.info("util", "Encode.transformStringForBD()",
        "root.MSG_GEN_ENTER_METHOD", " text = " + sText);

    int nStringLength = sText.length();
    StringBuffer resSB = new StringBuffer(nStringLength + 10);

    for (int i = 0; i < nStringLength; i++) {
      // SilverTrace.debug("util", "Encode.transformStringForBD",
      // "root_dsklflkjsdh",
      // "Char = '"+sText.charAt(i)+"' unicode ='"+escapeUnicodeString(Character.toString(sText.charAt(i)),
      // true)+"'");
      switch (sText.charAt(i)) {
        case '€':
          resSB.append('\u20ac'); // Euro Symbol
          break;
        // case '’':
        case '\u2019':
          resSB.append('\''); // ’ quote word
          break;
        default:
          resSB.append(sText.charAt(i));
      }
    }
    SilverTrace.info("util", "Encode.transformStringForBD()",
        "root.MSG_GEN_EXIT_METHOD", " new text = " + resSB.toString());

    return resSB.toString();
  }

  public static String convertHTMLEntities(String text) {
    SilverTrace.info("util", "Encode.convertHTMLEntities()",
        "root.MSG_GEN_PARAM_VALUE", " text recu " + text);

    text = StringEscapeUtils.escapeHtml(text);

    SilverTrace.info("util", "Encode.convertHTMLEntities()",
        "root.MSG_GEN_PARAM_VALUE", "text sortant = " + text);
    return text;
  }

}
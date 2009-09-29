package com.stratelia.webactiv.util.viewGenerator.html;

import com.silverpeas.util.EncodeHelper;

/**
 * Util class to encode special string or characters
 * 
 * @author lloiseau
 * @version 1.0
 * @deprecated Use {@link EncodeHelper} instead
 */
public class Encode extends Object {
  /**
   * Convert a java string to a javascript string Replace \,\n,\r and "
   * 
   * @param javastring
   *          Java string to encode
   * @return javascript string encoded
   */
  public static String javaStringToJsString(String javastring) {
    return EncodeHelper.javaStringToJsString(javastring);
  }

  /**
   * Convert a java string to a html string for textArea Replace ", <, >, & and
   * \n
   * 
   * @param javastring
   *          Java string to encode
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
   * Convert a java string to a html string for textfield... Replace ", <, >, &
   * and \n
   * 
   * @param javastring
   *          Java string to encode
   * @return html string encoded
   */
  public static String javaStringToHtmlParagraphe(String javastring) {
    return EncodeHelper.javaStringToHtmlParagraphe(javastring);
  }

  /**
   * Convert a html string to a java string Replace &quot
   * 
   * @param HTML
   *          string to encode
   * @return html string JAVA encoded
   */
  public static String htmlStringToJavaString(String htmlstring) {
    return EncodeHelper.htmlStringToJavaString(htmlstring);
  }

  /**
   * This method transforms a text with caracter specificly encoded for HTML by
   * a text encoded in according to the Java code.
   * 
   * @param text
   *          (String) a single text which contains a lot of forbidden
   *          caracters. This text must not be null
   * @return Returns the transformed text without specific codes.
   */
  public static String transformHtmlCode(String text) {
    return EncodeHelper.transformHtmlCode(text);
  }

  /**
   * Convert a java string to a html string for textArea Replace euro symbol
   * 
   * @param javastring
   *          Java string to encode
   * @return html string encoded
   * @deprecated
   */
  public static String encodeSpecialChar(String javastring) {
    if (javastring == null)
      return "";

    return javastring;
  }

  /**
   * This method transforms a string to replace the 'special' caracters to store
   * them correctly in the database
   * 
   * @param text
   *          (String) a single text which may contains 'special' caracters
   * @return Returns the transformed text without specific codes.
   */
  public static String transformStringForBD(String sText) {
    return EncodeHelper.transformStringForBD(sText);
  }

  public static String convertHTMLEntities(String text) {
    return EncodeHelper.convertHTMLEntities(text);
  }

}
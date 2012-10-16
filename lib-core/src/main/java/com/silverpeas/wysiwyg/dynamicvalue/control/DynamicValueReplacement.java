/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.wysiwyg.dynamicvalue.control;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.wysiwyg.dynamicvalue.dao.DynamicValueDAO;
import com.silverpeas.wysiwyg.dynamicvalue.model.DynamicValue;
import com.silverpeas.wysiwyg.dynamicvalue.pool.ConnectionPoolFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.ResourceLocator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 *
 */
public class DynamicValueReplacement {

  /**
   * regular expression use to search the following expression (%<key>%) in HTML code
   */
  final static String REGEX = "\\(%(.*?)%\\)";

  private String updatedString;

  /**
   * default constructor
   */
  public DynamicValueReplacement() {
  }

  /**
   * gets the list of valid DynamicValue object and build the HTML code to display the HTML select
   * @param language used to display information in correct language
   * @param fieldName name of the html field to allow using the generating code html with page
   * contained many HTML editor
   * @return String which contains HTML code
   */
  public static String buildHTMLSelect(String language, String fieldName) {
    // local variable initialization
    String HTMLCodeFramgment = "";
    Connection conn = null;
    try {
      // get connection
      conn = ConnectionPoolFactory.getConnection();
      // get the list of valid DynamicValue object
      List<DynamicValue> list = DynamicValueDAO.getAllValidDynamicValue(conn);

      // build the HTML select with the key list
      if (list != null) {
        ResourceLocator message = null;
        StringBuilder builder = new StringBuilder();
        // gets the words to display in the first select option
        String firstOption = " ------------------";
        try {
          message =
              new ResourceLocator("com.stratelia.silverpeas.wysiwyg.multilang.wysiwygBundle",
              language);
          if (message != null) {
            firstOption = message.getString("DynamicValues");
          }
        } catch (Exception ex) {
          SilverTrace.error("wysiwiy", DynamicValueReplacement.class.toString(),
              "root.EX_CANT_GET_LANGUAGE_RESOURCE ", ex);
        }

        // build the HTML select
        builder.append(
            " <select id=\"dynamicValues_").append(fieldName).append(
            "\" name=\"dynamicValues\" onchange=\"chooseDynamicValues").append(
            FileServerUtils.replaceAccentChars(fieldName.replace(' ', '_'))).append(
            "();this.selectedIndex=0;\">").append("<option value=\"\">").append(firstOption)
            .append(
            "</option>");
        for (DynamicValue dynamicValue : list) {
          builder.append("<option value=\"").append(dynamicValue.getKey()).append("\">").append(
              dynamicValue.getKey()).append("</option>");
        }
        builder.append(" </select>");

        HTMLCodeFramgment = builder.toString();
      }

    } catch (Exception e) {
      SilverTrace.error("wysiwiy", DynamicValueReplacement.class.toString(),
          "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      DBUtil.close(conn);
    }

    return HTMLCodeFramgment;

  }

  public String replaceKeyByValue(String wysiwygText) {
    // local variable initialization
    Connection conn = null;

    updatedString = wysiwygText;
    // Compile regular expression
    Pattern pattern = Pattern.compile(REGEX);
    Matcher matcher = pattern.matcher(updatedString);
    // if a key has been found in the HTML code, we try to replace him by his value.
    if (matcher.find()) {
      try {
        // get connection
        conn = ConnectionPoolFactory.getConnection();
        // search/replace all each key by his value
        searchReplaceKeys(updatedString, conn, matcher, null);
      } catch (SQLException e) {
        SilverTrace.error("wysiwyg", DynamicValueReplacement.class.toString(),
            "root.EX_SQL_QUERY_FAILED", e);
      } finally {
        DBUtil.close(conn);
      }
    }
    SilverTrace.debug("wysiwyg", DynamicValueReplacement.class.toString(),
        "content after  key replacement by value : " + updatedString);

    return updatedString;
  }

  /**
   * Recursive method which replaces the key by the correct value. The replacement take place as
   * follows : 1-retrieve the key from HTML code 2-search in database the dynamic value by his key
   * 3-if the value has been found, replaces all the key occurrences by his value otherwise it does
   * nothing. 4- realize again the third operation until all the different keys has been replaced
   * @param wysiwygText the wysiwyg content
   * @param conn the SQL connection
   * @param matcher Matcher Object to realize the search with regular expression
   * @return
   * @throws SQLException SQLException whether a SQl error occurred during the process
   */
  private String searchReplaceKeys(String wysiwygText, Connection conn, Matcher matcher,
      String oldMach)
      throws SQLException {

    String escapementStr = "";

    SilverTrace.debug("wysiwyg", DynamicValueReplacement.class.toString(),
        " character matching " + matcher.toString());
    SilverTrace.debug("wysiwyg", DynamicValueReplacement.class.toString(), " complete tag : " +
        matcher.group());
    // get the dynamic value corresponding to a key
    SilverTrace.debug("wysiwyg", DynamicValueReplacement.class.toString(),
        " key to use to get the dynamic value : " +
        EncodeHelper.htmlStringToJavaString(matcher.group(1)));
    DynamicValue value =
        DynamicValueDAO.getValidDynamicValue(conn, EncodeHelper.htmlStringToJavaString(matcher
        .group(1)));

    if (value != null) {
      SilverTrace.debug("wysiwyg", DynamicValueReplacement.class.toString(), "key : " +
          value.getKey() + "  value :" + value.getValue());
      // escape the first brace in the key string because it's a reserved character in regular
      // expression
      escapementStr = matcher.group().replaceAll("\\\\", "\\\\\\\\");
      escapementStr = escapementStr.replaceAll("\\(", "\\\\(");
      escapementStr = escapementStr.replaceAll("\\)", "\\\\)");
      SilverTrace.debug("wysiwyg", DynamicValueReplacement.class.toString(),
          " result after escaping special characters : " + escapementStr);
      // replace all the occurrences of current key in HTML code
      updatedString = wysiwygText.replaceAll(escapementStr, value.getValue());

    } else {
      SilverTrace.debug("wysiwyg", DynamicValueReplacement.class.toString(),
          " key not found in database : " + EncodeHelper.htmlStringToJavaString(matcher.group(1)));
    }

    // if value == null, we do nothing
    matcher.reset(updatedString);
    if (matcher.find() && !escapementStr.equalsIgnoreCase(oldMach)) {
      searchReplaceKeys(updatedString, conn, matcher, escapementStr);
    }

    return updatedString;
  }

  /**
   * checks whether the dynamic value functionality is active. This activation is realized by
   * writing "ON" in an properties file. if the property isn't found or if it's at OFF, the
   * functionality will be considered as inactive.
   * @return true whether the functionality is activated
   */
  public static boolean isActivate() {
    ResourceLocator resource =
        new ResourceLocator("com.stratelia.silverpeas.wysiwyg.settings.wysiwygSettings", "");
    return resource.getBoolean("activateDynamicValue", false);
  }

}

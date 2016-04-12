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

package org.silverpeas.core.contribution.content.wysiwyg.dynamicvalue.control;

import org.silverpeas.core.contribution.content.wysiwyg.dynamicvalue.dao.DynamicValueDAO;
import org.silverpeas.core.contribution.content.wysiwyg.dynamicvalue.model.DynamicValue;
import org.silverpeas.core.contribution.content.wysiwyg.dynamicvalue.pool.ConnectionFactory;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

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
   * @param editorName name of the wysiwyg editor
   * @return String which contains HTML code
   */
  public static String buildHTMLSelect(String language, String fieldName, String editorName) {
    // local variable initialization
    String HTMLCodeFramgment = "";
    try (Connection conn = ConnectionFactory.getConnection()) {
      // get the list of valid DynamicValue object
      List<DynamicValue> list = DynamicValueDAO.getAllValidDynamicValue(conn);

      // build the HTML select with the key list
      if (list != null) {
        LocalizationBundle message = null;
        StringBuilder builder = new StringBuilder();
        // gets the words to display in the first select option
        String firstOption = " ------------------";
        try {
          message =
              ResourceLocator.getLocalizationBundle(
                  "org.silverpeas.wysiwyg.multilang.wysiwygBundle", language);
          if (message != null) {
            firstOption = message.getString("DynamicValues");
          }
        } catch (Exception ex) {
          SilverTrace.error("wysiwyg", DynamicValueReplacement.class.toString(),
              "root.EX_CANT_GET_LANGUAGE_RESOURCE ", ex);
        }

        // build the HTML select
        builder.append(
            " <select id=\"dynamicValues_").append(fieldName).append(
            "\" name=\"dynamicValues\" onchange=\"chooseDynamicValues").append(
            FileServerUtils.replaceAccentChars(fieldName.replace(' ', '_'))).append(
            "('").append(editorName).append("');this.selectedIndex=0;\">").append("<option value=\"\">").append(firstOption)
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
      SilverTrace.error("wysiwyg", DynamicValueReplacement.class.toString(),
          "root.EX_SQL_QUERY_FAILED", e);
    }

    return HTMLCodeFramgment;

  }

  public String replaceKeyByValue(String wysiwygText) {
    // local variable initialization
    updatedString = wysiwygText;
    // Compile regular expression
    Pattern pattern = Pattern.compile(REGEX);
    Matcher matcher = pattern.matcher(updatedString);
    // if a key has been found in the HTML code, we try to replace him by his value.
    if (matcher.find()) {
      try (Connection conn = ConnectionFactory.getConnection()) {
        // search/replace all each key by his value
        searchReplaceKeys(updatedString, conn, matcher, null);
      } catch (SQLException e) {
        SilverTrace.error("wysiwyg", DynamicValueReplacement.class.toString(),
            "root.EX_SQL_QUERY_FAILED", e);
      }
    }
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

    // get the dynamic value corresponding to a key
    DynamicValue value =
        DynamicValueDAO.getValidDynamicValue(conn, EncodeHelper.htmlStringToJavaString(matcher
        .group(1)));

    if (value != null) {
      // escape the first brace in the key string because it's a reserved character in regular
      // expression
      escapementStr = matcher.group().replaceAll("\\\\", "\\\\\\\\");
      escapementStr = escapementStr.replaceAll("\\(", "\\\\(");
      escapementStr = escapementStr.replaceAll("\\)", "\\\\)");
      // replace all the occurrences of current key in HTML code
      updatedString = wysiwygText.replaceAll(escapementStr, value.getValue());

    } else {
      SilverTrace.warn("wysiwyg", DynamicValueReplacement.class.toString(),
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
   * Checks whether the dynamic value functionality is active. This activation is realized by
   * writing "ON" in an properties file. if the property isn't found or if it's at OFF, the
   * functionality will be considered as inactive.
   * @return true whether the functionality is activated
   */
  public static boolean isActivate() {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.wysiwyg.settings.wysiwygSettings");
    return settings.getBoolean("activateDynamicValue", false);
  }

}

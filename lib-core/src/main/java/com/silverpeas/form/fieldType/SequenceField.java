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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.form.fieldType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;

public class SequenceField extends TextField {
  
  private static final long serialVersionUID = -6526406111012877271L;
  
  public static final String TYPE = "sequence";
  
  private static final String VALUES_QUERY = "select distinct(f.fieldValue)"
    + " from sb_formtemplate_template t, sb_formtemplate_record r, sb_formtemplate_textfield f"
    + " where t.templateId = r.templateId"
    + " and r.recordId = f.recordId"
    + " and f.fieldName = ?"
    + " and t.externalId = ?";
  
  private static final int NUMBER_ERROR = -1;
  
  private String value = "";
  
  public SequenceField() {
  }
  
  public String getTypeName() {
    return TYPE;
  }
  
  public void setStringValue(String value) {
    this.value = value;
  }
  
  public String getStringValue() {
    return value;
  }
  
  public boolean isReadOnly() {
    return false;
  }
  
  public boolean acceptValue(String value, String language) {
    return !isReadOnly();
  }
  
  public boolean acceptValue(String value) {
    return !isReadOnly();
  }
  
  /**
   * @param fieldName The field's name.
   * @param templateName The template's name.
   * @param componentId The id of the component containing the field.
   * @param minLength The field's minimum length.
   * @param startValue The field's start value.
   * @param reuseAvailableValues Indicates whether previous values used by objects which were
   * removed can be used again.
   * @return The next sequence value to use.
   */
  public String getNextValue(String fieldName, String templateName, String componentId,
      int minLength, int startValue, boolean reuseAvailableValues) {
    List<Integer> values = getValues(fieldName, templateName, componentId);
    int newValue = 0;
    if (values.isEmpty()) {
      newValue = startValue;
    } else {
      if (reuseAvailableValues) {
        newValue = startValue;
        while (values.contains(Integer.valueOf(newValue))) {
          newValue++;
        }
      } else {
        newValue = values.get(values.size() - 1).intValue() + 1;
      }
    }
    return numberToString(newValue, minLength);
  }
  
  /**
   * 
   * @param fieldName The field's name.
   * @param templateName The template's name.
   * @param componentId The id of the component containing the field.
   * @return The list of values from the sequence which are already used.
   */
  private List<Integer> getValues(String fieldName, String templateName, String componentId) {
    List<Integer> values = new ArrayList<Integer>();
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      connection = DBUtil.makeConnection(JNDINames.FORMTEMPLATE_DATASOURCE);

      statement = connection.prepareStatement(VALUES_QUERY);
      statement.setString(1, fieldName);
      statement.setString(2, componentId + ":" + templateName);

      SilverTrace.debug("form", "SequenceField.getValues", "root.MSG_GEN_PARAM_VALUE",
        "fieldName = " + fieldName + ", componentId = " + componentId + ", templateName = "
        + templateName);

      rs = statement.executeQuery();

      int value;
      while (rs.next()) {
        value = numberToInt(rs.getString(1));
        if (value != NUMBER_ERROR) {
          values.add(Integer.valueOf(value));
        }
      }
    } catch (Exception e) {
      SilverTrace.error("form", "SequenceField.getValues", "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      DBUtil.close(rs, statement);
      try {
        if (connection != null && !connection.isClosed()) {
          connection.close();
        }
      } catch (SQLException e) {
        SilverTrace.error("form", "SequenceField.getValues", "root.EX_CONNECTION_CLOSE_FAILED", e);
      }
    }
    
    Collections.sort(values);
    return values;
  }
  
  /**
   * @param number The number to convert.
   * @param minLength The minimum length of the result.
   * @return The string representing the number.
   */
  private static String numberToString(int number, int minLength) {
    String result = String.valueOf(number);
    while (result.length() < minLength) {
      result = "0" + result;
    }
    return result;
  }
  
  /**
   * @param number A string corresponding to a number maybe starting with zeros
   * @return
   */
  private static int numberToInt(String number) {
    if (number != null && number.length() > 0) {
      while (number.startsWith("0")) {
        number = number.substring(1);
      }
      try {
        return Integer.parseInt(number);
      } catch (NumberFormatException e) {
        SilverTrace.error("form", "SequenceField.numberToInt", "form.EX_CANT_PARSE_NUMBER",
          "number=" + number, e);
      }
    }
    return NUMBER_ERROR;
  }
  
}

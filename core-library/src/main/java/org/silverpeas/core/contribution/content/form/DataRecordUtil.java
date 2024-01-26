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
package org.silverpeas.core.contribution.content.form;

import org.silverpeas.core.contribution.content.form.displayers.WysiwygFCKFieldDisplayer;
import org.silverpeas.core.contribution.content.form.field.DateField;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.ArrayList;
import java.util.List;

public class DataRecordUtil {

  private static final String VARIABLE_PREFIX = "${";
  private static final String VARIABLE_SUFFIX = "}";
  private static final String FOLDER_PREFIX = "folder.";

  private DataRecordUtil() {
    throw new IllegalAccessError("Utility class");
  }

  /**
   * Updates the specified fields.
   */
  public static void updateFields(String[] fieldNames, DataRecord updatedRecord,
      DataRecord copiedRecord, String language) throws FormException {
    for (String fieldName : fieldNames) {
      updateField(fieldName, updatedRecord, copiedRecord, language);
    }
  }

  private static void updateField(String fieldName,
    DataRecord updatedRecord, DataRecord copiedRecord, String language) throws FormException {
    Field updatedField = updatedRecord.getField(fieldName);
    try {
      Field copiedField = copiedRecord.getField(fieldName);
      if (copiedField != null) {
        if (updatedField.getTypeName().equals(copiedField.getTypeName())) {
          Object value = copiedField.getObjectValue();
          if (value instanceof String) {
            updatedField.setObjectValue(
                applySubstitution((String) value, updatedRecord, language));
          } else {
            updatedField.setObjectValue(copiedField.getObjectValue());
          }
        } else {
          updatedField.setValue(copiedField.getValue());
        }
      }
    } catch (FormException ignored) {
      SilverLogger.getLogger(DataRecordUtil.class)
          .warn("Field '" + fieldName + "' is unknown", ignored);
    }
  }

  /**
   * Returns : "But who is xoxox ?" for : "But who is ${foo} ?" when xoxox is the value of the foo
   * field, The resolvedVar is used to detect recursive call like : foo = "${foo}"
   */
  public static String applySubstitution(String text, DataRecord data, String lang) {
    String appliedText = applySubstitution(text, data, lang, new ArrayList<>());
    // applying twice to replace variables in substituted text
    appliedText = applySubstitution(appliedText, data, lang, new ArrayList<>());
    return appliedText;
  }

  private static String applySubstitution(String text, DataRecord data,
      String lang, List<String> resolvedVars) {
    if (text == null) {
      return "";
    }

    int varBegin = text.indexOf(VARIABLE_PREFIX);
    if (varBegin == -1) {
      return text;
    }

    int varEnd = text.indexOf(VARIABLE_SUFFIX, varBegin);
    if (varEnd == -1) {
      return text;
    }

    String var = text.substring(varBegin + VARIABLE_PREFIX.length(), varEnd);
    String prefix = text.substring(0, varBegin);
    String suffix = (varEnd + 1 < text.length()) ? text.substring(varEnd + 1) : null;

    String value;
    if (resolvedVars.contains(var)) {
      value = VARIABLE_PREFIX + var + VARIABLE_SUFFIX;
    } else {
      resolvedVars.add(var);
      value = getFieldValue(var, data, lang);
    }

    if (suffix != null) {
      suffix = applySubstitution(suffix, data, lang, resolvedVars);
      return prefix + value + suffix;
    } else {
      return prefix + value;
    }
  }

  private static String getFieldValue(String fieldName, DataRecord data, String lang) {
    String value = "";
    try {
      Field field = getField(fieldName, data);
      if (field != null) {
        value = field.getValue(lang);
        if (DateField.TYPE.equals(field.getTypeName())) {
          value = getOutputDate(field, lang);
        } else if (value != null && value.startsWith(WysiwygFCKFieldDisplayer.DB_KEY)) {
          String fileName = value.substring(WysiwygFCKFieldDisplayer.DB_KEY.length());
          value = WysiwygFCKFieldDisplayer
              .getContentFromFile(data.getResourceReference().getComponentInstanceId(), fileName);
          // replacing non HTML End-Of-Line
          value = value.replace("\r\n", "");
        }
      }
      if (value == null) {
        value = "";
      }
    } catch (FormException e) {
      SilverLogger.getLogger(DataRecordUtil.class).warn("Field '" + fieldName +
          "' is unknown. Error: " + e.getMessage());
      value = VARIABLE_PREFIX + fieldName + VARIABLE_SUFFIX;
    }
    return value;
  }

  private static Field getField(String fieldName, DataRecord data) throws FormException {
    Field field = data.getField(fieldName);
    if (field != null) {
      return field;
    } else if (fieldName.startsWith(FOLDER_PREFIX)) {
      return data.getField(fieldName.substring(FOLDER_PREFIX.length()));
    }
    return null;
  }

  private static String getOutputDate(Field field, String lang) {
    try {
      return DateUtil.getOutputDate(field.getValue(), lang);
    } catch (Exception e) {
      SilverLogger.getLogger(DataRecordUtil.class)
          .warn("Can't parse date '" + field.getValue() + "' from field '" + field.getName() + "'",
              e);
    }
    return field.getValue();
  }

}
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
package org.silverpeas.core.workflow.util;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.displayers.WysiwygFCKFieldDisplayer;
import org.silverpeas.core.contribution.content.form.field.DateField;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

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
      DataRecord copiedRecord, Item[] items, String language) throws FormException {
    for (String fieldName : fieldNames) {
      updateField(fieldName, updatedRecord, copiedRecord, items, language);
    }
  }

  private static void updateField(String fieldName,
      DataRecord updatedRecord, DataRecord copiedRecord, Item[] items, String language) throws FormException {
    Field updatedField = updatedRecord.getField(fieldName);
    try {
      Field copiedField = copiedRecord.getField(fieldName);
      if (copiedField != null) {
        if (updatedField.getTypeName().equals(copiedField.getTypeName())) {
          Object value = copiedField.getObjectValue();
          if (value instanceof String) {
            updatedField.setObjectValue(
                applySubstitution((String) value, updatedRecord, items, language));
          } else {
            updatedField.setObjectValue(copiedField.getObjectValue());
          }
        } else {
          updatedField.setValue(copiedField.getValue());
        }
      }
    } catch (FormException e) {
      SilverLogger.getLogger(DataRecordUtil.class)
          .warn("Field '" + fieldName + "' is unknown", e);
    }
  }

  /**
   * Applies any variable substitution in the specified text by their values as set in the given
   * data record. In case a variable is in fact a dictionary encoded in a text field, the value of
   * the field referred by the variable is the key to the actual value in the dictionary. So, the
   * workflow item defining the field is required to access to the underlying dictionary. Recursive
   * call like <code>foo = "${foo}"</code> are detected.
   * <p>
   * If the text is <code>"But who is ${foo}?"</code>, with the field <code>foo</code> valued with
   * <code>"xoxox"</code>, then the returned text will be <code>"But who is xoxox?"</code>. If the
   * text is <code>"Request of type ${type}</code>, with a text field valued with
   * <code>"0"</code> but mapped in fact to a dictionary <code>{0 → "Foo", 1 → "Boo", 2 → "Proo"
   * }</code>, then the returned text will be <code>"Request of type Foo</code>.
   * </p>
   *
   * @param text the text to which variables substitution have to be applied. The text can be a
   * variable itself.
   * @param data the data record with all the valued fields and with its template (template defining
   * each fields).
   * @param items all the implied  workflow items.
   * @param lang the ISO 639-1 code of a user language.
   * @return the expanded text.
   */
  public static String applySubstitution(String text, DataRecord data, Item[] items,
      String lang) {
    String appliedText = applySubstitution(text, data, items, lang, new ArrayList<>());
    // applying twice to replace variables in substituted text
    appliedText = applySubstitution(appliedText, data, items, lang, new ArrayList<>());
    return appliedText;
  }

  private static String applySubstitution(String text, DataRecord data, Item[] items,
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
      value = getFieldValue(var, data, items, lang);
    }

    if (suffix != null) {
      suffix = applySubstitution(suffix, data, items, lang, resolvedVars);
      return prefix + value + suffix;
    } else {
      return prefix + value;
    }
  }

  private static String getFieldValue(String fieldName, DataRecord data, Item[] items,
      String lang) {
    String value;
    try {
      FieldTemplate template =
          data.getTemplate() == null ? null : data.getTemplate().getFieldTemplate(fieldName);
      Field field = getField(fieldName, data);
      if (field != null) {
        value = getFieldValue(field, template, items, lang);
        if (DateField.TYPE.equals(field.getTypeName())) {
          value = getOutputDate(field, lang);
        } else if (value != null && value.startsWith(WysiwygFCKFieldDisplayer.DB_KEY)) {
          String fileName = value.substring(WysiwygFCKFieldDisplayer.DB_KEY.length());
          value = WysiwygFCKFieldDisplayer
              .getContentFromFile(data.getResourceReference().getComponentInstanceId(), fileName);
          // replacing non HTML End-Of-Line
          value = value.replace("\r\n", "");
        }
      } else {
        return "";
      }
    } catch (FormException e) {
      SilverLogger.getLogger(DataRecordUtil.class).warn("Field '" + fieldName +
          "' is unknown. Error: " + e.getMessage());
      value = VARIABLE_PREFIX + fieldName + VARIABLE_SUFFIX;
    }
    return value == null ? "" : value;
  }

  private static String getFieldValue(Field field, FieldTemplate template, Item[] items,
      String lang) throws FormException {
    String value;
    if (template != null) {
      value = WorkflowUtil.formatFieldValueAsString(items, template, field, lang);
      if (StringUtil.isNotDefined(value)) {
        value = field.getValue(lang);
      }
    } else {
      value = field.getValue(lang);
    }
    field.setValue(value, lang);
    return value;
  }

  private static Field getField(String fieldName, DataRecord data) throws FormException {
    String simpleFieldName = fieldName.startsWith(FOLDER_PREFIX) ?
        fieldName.substring(FOLDER_PREFIX.length()) : fieldName;
    Field field = data.getField(fieldName);
    if (field == null) {
      field = data.getField(simpleFieldName);
    }
    if (field != null) {
      field.setName(simpleFieldName);
    }
    return field;
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
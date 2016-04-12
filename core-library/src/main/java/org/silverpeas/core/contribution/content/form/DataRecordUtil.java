/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.contribution.content.form;

import org.silverpeas.core.contribution.content.form.field.DateField;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class DataRecordUtil {
  /**
   * Updates the specified fields.
   */
  static public void updateFields(String[] fieldNames,
      DataRecord updatedRecord, DataRecord copiedRecord) throws FormException {
    Field updatedField;
    Field copiedField;

    for (String fieldName : fieldNames) {
      updatedField = updatedRecord.getField(fieldName);
      try {
        copiedField = copiedRecord.getField(fieldName);
        if (copiedField == null) {
          continue;
        }
      } catch (FormException ignored) {
        continue;
      }

      if (updatedField.getTypeName().equals(copiedField.getTypeName())) {
        updatedField.setObjectValue(copiedField.getObjectValue());
      } else {
        updatedField.setValue(copiedField.getValue());
      }
    }
  }

  /**
   * Returns : "But who is xoxox ?" for : "But who is ${foo}" when xoxox is the value of the foo
   * field, The resolvedVar is used to detect recursive call like : foo = "${foo}"
   */
  static public String applySubstitution(String text, DataRecord data,
      String lang) {
    return applySubstitution(text, data, lang, new ArrayList<>());
  }

  static private String applySubstitution(String text, DataRecord data,
      String lang, List<String> resolvedVars) {
    if (text == null) {
      return "";
    }

    int varBegin = text.indexOf("${");
    if (varBegin == -1) {
      return text;
    }

    int varEnd = text.indexOf("}", varBegin);
    if (varEnd == -1) {
      return text;
    }

    String var = text.substring(varBegin + 2, varEnd);
    String prefix = text.substring(0, varBegin);
    String suffix = (varEnd + 1 < text.length()) ? text.substring(varEnd + 1) : null;

    String value;
    try {
      if (resolvedVars.contains(var)) {
        value = "${" + var + "}";
      } else {
        resolvedVars.add(var);
        Field field = data.getField(var);
        value = field.getValue(lang);
        if (DateField.TYPE.equals(field.getTypeName())) {
          try {
            value = DateUtil.getOutputDate(field.getValue(), lang);
          } catch (Exception e) {
            SilverTrace.error("form", "DataRecordUtil.applySubstitution",
                "form.INFO_NOT_CORRECT_TYPE", "value = " + field.getValue(), e);
          }
        }
        if (value == null) {
          value = "";
        }
      }
    } catch (FormException e) {
      value = "${" + var + "}";
    }

    if (suffix != null) {
      suffix = applySubstitution(suffix, data, lang, resolvedVars);
      return prefix + value + suffix;
    } else {
      return prefix + value;
    }
  }

}

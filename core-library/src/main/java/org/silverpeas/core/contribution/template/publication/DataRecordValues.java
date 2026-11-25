/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.contribution.template.publication;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

/**
 * The values of the fields of a data record at a given time, used to know whether the data of a
 * form have been modified.
 * @author mmoquillon
 */
final class DataRecordValues {

  private DataRecordValues() {
  }

  /**
   * Gets the current values of all the fields of the specified data record, each occurrence of a
   * repeatable field included. An undefined value is taken as an empty one.
   * @param record a data record.
   * @return the values of the fields of the record, keyed by the name and the occurrence of the
   * fields.
   * @throws FormException if the template of a field of the record cannot be got.
   */
  static Map<String, String> of(final DataRecord record) throws FormException {
    Map<String, String> values = new HashMap<>();
    RecordTemplate template = record.getTemplate();
    for (String fieldName : record.getFieldNames()) {
      int occurrences = template.getFieldTemplate(fieldName).getMaximumNumberOfOccurrences();
      for (int occurrence = 0; occurrence < occurrences; occurrence++) {
        Field field = record.getField(fieldName, occurrence);
        if (field != null) {
          values.put(fieldName + "#" + occurrence,
              defaultStringIfNotDefined(field.getStringValue()));
        }
      }
    }
    return values;
  }
}

/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.workflow.util;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.field.DateField;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.model.Item;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author silveryocha
 */
public class WorkflowUtil {

  /**
   * The name of the parameter holding the process file name in a workflow application descriptor.
   */
  public static final String PROCESS_XML_FILE_NAME = "XMLFileName";
  private static final String SEVERAL_VALUE_DELIMITER = "##";

  /**
   * Hidden constructor.
   */
  private WorkflowUtil() {
  }

  /**
   * Gets the comparable of the field value from given context.
   * @param items the items of a form.
   * @param fieldTemplate the field templates of a form.
   * @param field the aimed field.
   * @param language the language the formatting rules must take in charge.
   * @return the comparable of the field.
   */
  public static Comparable getFieldComparable(Item[] items, FieldTemplate fieldTemplate,
      Field field, String language) {
    final String formattedValue = formatFieldValueAsString(items, fieldTemplate, field, language);
    if (DateField.TYPE.equals(field.getTypeName())) {
      try {
        return isDefined(formattedValue) ?
            DateUtil.getDateInputFormat(language).parse(formattedValue) : null;
      } catch (ParseException e) {
        SilverLogger.getLogger(WorkflowUtil.class).error(e);
      }
    }
    return formattedValue;
  }

  /**
   * Formats the field value as string from given context.
   * @param items the items of a form.
   * @param fieldTemplate the field templates of a form.
   * @param field the aimed field.
   * @param language the language the formatting rules must take in charge.
   * @return the formatted value.
   */
  public static String formatFieldValueAsString(Item[] items, FieldTemplate fieldTemplate,
      Field field, String language) {
    String fieldValueAsString = defaultStringIfNotDefined(field.getValue(language));
    if (fieldValueAsString.isEmpty() || !DateField.TYPE.equals(field.getTypeName())) {
      final String fieldName = fieldTemplate.getFieldName();
      final Item item = getItemByName(items, fieldName);
      if (item != null) {
        final Map<String, String> keyValuePairs = item.getKeyValuePairs();
        if (keyValuePairs != null && keyValuePairs.size() > 0) {
          // Try to format a checkbox list
          fieldValueAsString = Arrays
              .stream(fieldValueAsString.split(SEVERAL_VALUE_DELIMITER))
              .map(keyValuePairs::get)
              .collect(Collectors.joining(", "));
        }
      }
    }
    return fieldValueAsString;
  }

  /**
   * Gets form given array the item which has the given name.
   * @param items the items array.
   * @param itemName the item name to get.
   * @return {@link Item} instance if any corresponding, null otherwise.
   */
  public static Item getItemByName(Item[] items, String itemName) {
    for (final Item item : items) {
      if (itemName.equals(item.getName())) {
        return item;
      }
    }
    return null;
  }
}

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
package com.silverpeas.form.filter;

import java.util.*;

import com.silverpeas.form.*;
import com.silverpeas.form.record.*;
import com.silverpeas.form.form.*;

/**
 * FilterManager
 */
public class FilterManager {
  /**
   * Builds a FilterManager for records built on the given template.
   */
  public FilterManager(RecordTemplate rowTemplate, String lang) {
    this.template = rowTemplate;
    this.lang = lang;
  }

  /**
   * Returns a Form which can be used to select criteria values.
   */
  public Form getCriteriaForm() throws FormException {
    if (criteriaForm == null) {
      criteriaForm = new XmlForm(getCriteriaTemplate());
    }

    return criteriaForm;
  }

  /**
   * Returns a RecordTemplate which can be used to select criteria values.
   */
  public RecordTemplate getCriteriaTemplate() throws FormException {
    if (criteriaTemplate == null) {
      criteriaTemplate = new GenericRecordTemplate();
      FieldTemplate[] filteredFields = template.getFieldTemplates();

      String filteredType;
      String filteredName;
      String filteredLabel;
      GenericFieldTemplate criteriumField;

      for (int i = 0; i < filteredFields.length; i++) {
        filteredType = filteredFields[i].getTypeName();
        filteredName = filteredFields[i].getFieldName();
        filteredLabel = filteredFields[i].getLabel(lang);

        if ("text".equals(filteredType)) {
          criteriumField = new GenericFieldTemplate(filteredName + "__like",
              "text");
          criteriumField.addLabel(filteredLabel + " "
              + Util.getString("eq", lang), lang);

          if (fieldsParameter.containsKey(filteredName)) {
            FieldTemplate field = (FieldTemplate) fieldsParameter
                .get(filteredName);
            criteriumField.setDisplayerName("listbox");

            Map<String, String> parameters = field.getParameters(lang);
            Set<String> keys = parameters.keySet();
            Iterator<String> it = keys.iterator();
            String key = null;
            while (it.hasNext()) {
              key = it.next();
              criteriumField.addParameter(key, parameters.get(key));
            }
          }

          criteriaTemplate.addFieldTemplate(criteriumField);
        } else if ("date".equals(filteredType)) {
          criteriumField = new GenericFieldTemplate(filteredName + "__lt",
              "date");
          criteriumField.addLabel(filteredLabel + " "
              + Util.getString("le", lang), lang);
          criteriaTemplate.addFieldTemplate(criteriumField);

          criteriumField = new GenericFieldTemplate(filteredName + "__gt",
              "date");
          criteriumField.addLabel(filteredLabel + " "
              + Util.getString("ge", lang), lang);
          criteriaTemplate.addFieldTemplate(criteriumField);
        } else {
          criteriumField = new GenericFieldTemplate(filteredName + "__equal",
              filteredType);
          criteriumField.addLabel(filteredLabel + " "
              + Util.getString("eq", lang), lang);
          criteriaTemplate.addFieldTemplate(criteriumField);
        }
      }
    }

    return criteriaTemplate;
  }

  /**
   * Returns an empty criteria record.
   */
  public DataRecord getEmptyCriteriaRecord() throws FormException {
    return getCriteriaTemplate().getEmptyRecord();
  }

  /**
   * Filters the given list of DataRecord using the specified criteria.
   */
  public List<DataRecord> filter(DataRecord criteria, List<DataRecord> filtered)
      throws FormException {
    ArrayList<DataRecord> result = new ArrayList<DataRecord>();
    RecordFilter filter = buildRecordFilter(criteria);
    Iterator<DataRecord> records = filtered.iterator();
    DataRecord record;

    while (records.hasNext()) {
      record = records.next();
      if (filter.match(record))
        result.add(record);
    }

    return result;
  }

  /**
   * Builds a RecordFilter from the criteria record (which must be built with the criterieTemplate)
   */
  public RecordFilter buildRecordFilter(DataRecord criteriaRecord)
      throws FormException {
    SimpleRecordFilter filter = new SimpleRecordFilter();
    FieldFilter fieldFilter;

    String[] criteriaNames = getCriteriaTemplate().getFieldNames();
    String criteriaName;
    Field criteria;
    String filteredName;
    String filterKind;
    for (int i = 0; i < criteriaNames.length; i++) {
      criteriaName = criteriaNames[i];
      criteria = criteriaRecord.getField(criteriaName);

      // skip null criterium
      if (criteria.isNull())
        continue;

      filteredName = getFilteredName(criteriaName);
      filterKind = getFilterKind(criteriaName);

      if ("like".equals(filterKind)) {
        fieldFilter = new LikeFilter(criteria);
      } else if ("lt".equals(filterKind)) {
        fieldFilter = new LessThenFilter(criteria);
      } else if ("gt".equals(filterKind)) {
        fieldFilter = new GreaterThenFilter(criteria);
      } else if ("equal".equals(filterKind)) {
        fieldFilter = new EqualityFilter(criteria);
      } else
        fieldFilter = null;

      if (fieldFilter != null) {
        filter.addFieldFilter(filteredName, fieldFilter);
      }
    }

    return filter;
  }

  public void addFieldParameter(String fieldName, FieldTemplate field) {
    fieldsParameter.put(fieldName, field);
  }

  private String getFilteredName(String criteriaName) {
    int sep = criteriaName.lastIndexOf("__");
    if (sep == -1)
      return criteriaName;
    else
      return criteriaName.substring(0, sep);
  }

  private String getFilterKind(String criteriaName) {
    int sep = criteriaName.lastIndexOf("__");
    if (sep == -1 || sep + 2 >= criteriaName.length())
      return "";
    else
      return criteriaName.substring(sep + 2);
  }

  /**
   * The template of the filtered records.
   */
  private final RecordTemplate template;

  /**
   * The template of the criteria records.
   */
  private GenericRecordTemplate criteriaTemplate = null;

  /**
   * The Form which can be used to fill the criteria.
   */
  private Form criteriaForm = null;

  /**
   * The lang used to display the several label.
   */
  private String lang = null;

  private Hashtable<String, FieldTemplate> fieldsParameter = new Hashtable<String, FieldTemplate>();
}

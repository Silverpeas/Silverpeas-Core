/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.contribution.content.form.filter;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.form.XmlForm;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * FilterManager
 */
public class FilterManager {

  /**
   * The template of the filtered records.
   */
  private final RecordTemplate template;

  /**
   * The template of the criteria records.
   */
  private GenericRecordTemplate criteriaTemplate;

  /**
   * The Form which can be used to fill the criteria.
   */
  private Form criteriaForm;

  /**
   * The lang used to display the several label.
   */
  private String lang;

  private Map<String, FieldTemplate> fieldsParameter = Collections.synchronizedMap(new HashMap<>());

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

      GenericFieldTemplate criteriumField;

      for (FieldTemplate filteredField : filteredFields) {
        String filteredType = filteredField.getTypeName();
        String filteredName = filteredField.getFieldName();
        String filteredLabel = filteredField.getLabel(lang);

        switch (filteredType) {
          case "text":
            criteriumField = processText(filteredName, filteredLabel);
            break;
          case "jdbc":
            criteriumField = processJDBC(filteredType, filteredName, filteredLabel);
            break;
          case "date":
            criteriumField = processDate(filteredName, filteredLabel);
            break;
          default:
            criteriumField = new GenericFieldTemplate(filteredName + "__equal", filteredType);
            criteriumField.addLabel(filteredLabel + " " + Util.getString("eq", lang), lang);
            break;
        }
        criteriaTemplate.addFieldTemplate(criteriumField);
      }
    }

    return criteriaTemplate;
  }

  private GenericFieldTemplate processDate(final String filteredName, final String filteredLabel)
      throws FormException {
    GenericFieldTemplate criteriumField = new GenericFieldTemplate(filteredName + "__lt", "date");
    criteriumField.addLabel(filteredLabel + " " + Util.getString("le", lang), lang);
    criteriaTemplate.addFieldTemplate(criteriumField);

    criteriumField = new GenericFieldTemplate(filteredName + "__gt", "date");
    criteriumField.addLabel(filteredLabel + " " + Util.getString("ge", lang), lang);
    return criteriumField;
  }

  private GenericFieldTemplate processJDBC(final String filteredType, final String filteredName,
      final String filteredLabel) throws FormException {
    final GenericFieldTemplate criteriumField;
    criteriumField = new GenericFieldTemplate(filteredName + "__equal", filteredType);
    criteriumField.addLabel(filteredLabel + " " + Util.getString("eq", lang), lang);

    if (fieldsParameter.containsKey(filteredName)) {
      FieldTemplate field = fieldsParameter.get(filteredName);
      Map<String, String> parameters = field.getParameters(lang);
      Set<String> keys = parameters.keySet();
      for (String key : keys) {
        criteriumField.addParameter(key, parameters.get(key));
      }
      criteriumField.addParameter("displayer", "listbox");
    }
    return criteriumField;
  }

  private GenericFieldTemplate processText(final String filteredName, final String filteredLabel)
      throws FormException {
    final GenericFieldTemplate criteriumField;
    criteriumField = new GenericFieldTemplate(filteredName + "__like", "text");
    criteriumField.addLabel(filteredLabel + " " + Util.getString("eq", lang), lang);

    if (fieldsParameter.containsKey(filteredName)) {
      FieldTemplate field = fieldsParameter.get(filteredName);
      criteriumField.setDisplayerName("listbox");

      Map<String, String> parameters = field.getParameters(lang);
      Set<String> keys = parameters.keySet();
      for (String key : keys) {
        criteriumField.addParameter(key, parameters.get(key));
      }
    }
    return criteriumField;
  }

  /**
   * Returns an empty criteria record.
   */
  public DataRecord getEmptyCriteriaRecord() throws FormException {
    return getCriteriaTemplate().getEmptyRecord();
  }

  public void addFieldParameter(String fieldName, FieldTemplate field) {
    fieldsParameter.put(fieldName, field);
  }

  /**
   * Gets a RecordFilter from the criteria record (which must be built with the criteriaRecord)
   */
  public RecordFilter getRecordFilter(DataRecord criteriaRecord) throws FormException {
    SimpleRecordFilter filter = new SimpleRecordFilter();
    FieldFilter fieldFilter;

    String[] criteriaNames = getCriteriaTemplate().getFieldNames();
    Field criteria;
    String filteredName;
    String filterKind;
    for (String criteriaName : criteriaNames) {
      criteria = criteriaRecord.getField(criteriaName);
      // skip null criterium
      if (criteria.isNull()) {
        continue;
      }
      filteredName = getFilteredName(criteriaName);
      filterKind = getFilterKind(criteriaName);

      switch (filterKind) {
        case "like":
          fieldFilter = new LikeFilter(criteria);
          break;
        case "lt":
          fieldFilter = new LessThenFilter(criteria);
          break;
        case "gt":
          fieldFilter = new GreaterThenFilter(criteria);
          break;
        case "equal":
          fieldFilter = new EqualityFilter(criteria);
          break;
        default:
          fieldFilter = null;
          break;
      }
      if (fieldFilter != null) {
        filter.addFieldFilter(filteredName, fieldFilter);
      }
    }
    return filter;
  }

  private String getFilteredName(String criteriaName) {
    int sep = criteriaName.lastIndexOf("__");
    if (sep == -1) {
      return criteriaName;
    } else {
      return criteriaName.substring(0, sep);
    }
  }

  private String getFilterKind(String criteriaName) {
    int sep = criteriaName.lastIndexOf("__");
    if (sep == -1 || sep + 2 >= criteriaName.length()) {
      return "";
    } else {
      return criteriaName.substring(sep + 2);
    }
  }
}

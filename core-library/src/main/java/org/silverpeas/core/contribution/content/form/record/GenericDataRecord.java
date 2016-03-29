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

package org.silverpeas.core.contribution.content.form.record;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.silvertrace.SilverTrace;

/**
 * A GenericDataRecord use a Field[] and a GenericRecordTemplate.
 */
public class GenericDataRecord implements DataRecord, Serializable {

  private static final long serialVersionUID = 1L;
  private int id = -1;
  private String externalId;
  private Field[] fields;
  private RecordTemplate template;
  private String language;
  private Map<String, List<Field>> fieldsByName;

  /**
   * A GenericDataRecord is built from a RecordTemplate.
   */
  public GenericDataRecord(RecordTemplate template) throws FormException {
    this.template = template;

    List<Field> allFields = new ArrayList<>();
    fieldsByName = new HashMap<>();

    FieldTemplate fieldTemplate;
    Field field;
    for (final String fieldName : template.getFieldNames()) {
      fieldTemplate = template.getFieldTemplate(fieldName);
      List<Field> occurrences = new ArrayList<>();
      int maximumNumberOfOccurrences = fieldTemplate.getMaximumNumberOfOccurrences();
      for (int o = 0; o < maximumNumberOfOccurrences; o++) {
        field = fieldTemplate.getEmptyField(o);
        occurrences.add(field);
      }
      fieldsByName.put(fieldName, occurrences);
      allFields.addAll(occurrences);
    }
    fields = allFields.toArray(new Field[allFields.size()]);
  }

  /**
   * Returns the data record id. The record is known by its external id.
   */
  @Override
  public String getId() {
    return externalId;
  }

  /**
   * Gives an id to the record. Caution ! the record is known by its external id.
   */
  @Override
  public void setId(String id) {
    // if (this.externalId == null)
    this.externalId = id;
  }

  /**
   * Returns all the fields
   */
  public Field[] getFields() {
    return fields;
  }

  /**
   * Returns the named field.
   * @throws FormException when the fieldName is unknown.
   */
  @Override
  public Field getField(String fieldName) throws FormException {
    return getField( fieldName,  0);
  }

  @Override
  public Field getField(String fieldName, int occurrence) {
    List<Field> occurrences = fieldsByName.get(fieldName);
    Field field = null;
    if (occurrences != null && occurrence < occurrences.size()) {
      field = occurrences.get(occurrence);
    }

    if (field == null) {
      SilverTrace.warn("form", "GenericDataRecord.getField",
          "form.EXP_UNKNOWN_FIELD", "fieldName '" + fieldName
          + "' in DB not found in XML descriptor");
    }

    return field;
  }

  /**
   * Returns the field at the index position in the record.
   * @throws FormException when the fieldIndex is unknown.
   */
  @Override
  public Field getField(int fieldIndex) throws FormException {
    if (fieldIndex >= 0 && fieldIndex < fields.length) {
      return fields[fieldIndex];
    } else {
      throw new FormException("DataRecord", "form.EXP_INDEX_OUT_OF_BOUNDS");
    }
  }

  /**
   * Return true if this record has not been inserted in a RecordSet.
   */
  @Override
  public boolean isNew() {
    return (id == -1);
  }

  /**
   * Gets the internal id. May be used only by a package class !
   */
  int getInternalId() {
    return id;
  }

  /**
   * Sets the internal id. May be used only by a package class !
   */
  void setInternalId(int id) {
    // if (this.id == -1)
    this.id = id;
  }

  @Override
  public String[] getFieldNames() {
    return template.getFieldNames();
  }

  @Override
  public String getLanguage() {
    return language;
  }

  @Override
  public void setLanguage(String language) {
    this.language = language;
  }

  @Override
  public Map<String, String> getValues(String language) {
    Map<String, String> formValues = new HashMap<>();
    String fieldNames[] = getFieldNames();
    PagesContext pageContext = new PagesContext();
    pageContext.setLanguage(language);
    for (String fieldName : fieldNames) {
      try {
        Field field = getField(fieldName);
        GenericFieldTemplate fieldTemplate =
            (GenericFieldTemplate) template.getFieldTemplate(fieldName);
        FieldDisplayer fieldDisplayer =
            TypeManager.getInstance().getDisplayer(fieldTemplate.getTypeName(), "simpletext");
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        //noinspection unchecked
        fieldDisplayer.display(out, field, fieldTemplate, pageContext);
        formValues.put(fieldName, sw.toString());
      } catch (Exception e) {
        SilverTrace.warn("form", "GenericDataRecord.getValues", "CANT_GET_FIELD_VALUE",
            "objectId = " + externalId + "fieldName = " + fieldName, e);
      }
    }
    return formValues;
  }
}
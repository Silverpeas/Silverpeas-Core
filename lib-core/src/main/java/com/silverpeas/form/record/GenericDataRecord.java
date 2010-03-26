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
package com.silverpeas.form.record;

import java.io.Serializable;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A GenericDataRecord use a Field[] and a GenericRecordTemplate.
 */
public class GenericDataRecord implements DataRecord, Serializable {

  private static final long serialVersionUID = 1L;
  private int id = -1;
  private String externalId = null;
  private Field[] fields = null;
  private RecordTemplate template = null;
  private String language = null;

  /**
   * A GenericDataRecord is built from a RecordTemplate.
   */
  public GenericDataRecord(RecordTemplate template) throws FormException {
    this.template = template;

    String[] fieldNames = template.getFieldNames();
    int size = fieldNames.length;
    fields = new Field[size];

    String fieldName;
    FieldTemplate fieldTemplate;
    Field field;
    for (int i = 0; i < size; i++) {
      fieldName = fieldNames[i];
      // SilverTrace.info("form", "GenericDataRecord.GenericDataRecord",
      // "root.MSG_GEN_PARAM_VALUE", "fieldName="+fieldName);
      fieldTemplate = template.getFieldTemplate(fieldName);
      field = fieldTemplate.getEmptyField();
      // SilverTrace.info("form", "GenericDataRecord.GenericDataRecord",
      // "root.MSG_GEN_PARAM_VALUE", "fieldType="+field.getTypeName());
      fields[i] = field;
    }

    for (int i = 0; i < fields.length; i++) {
      field = fields[i];
      // SilverTrace.info("form", "GenericDataRecord.GenericDataRecord",
      // "root.MSG_GEN_PARAM_VALUE", "fieldType="+field.getTypeName());
    }
  }

  /**
   * Returns the data record id. The record is known by its external id.
   */
  public String getId() {
    return externalId;
  }

  /**
   * Gives an id to the record. Caution ! the record is known by its external id.
   */
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
   * @throw FormException when the fieldName is unknown.
   */
  public Field getField(String fieldName) throws FormException {
    // SilverTrace.info("form", "GenericDataRecord.getField",
    // "root.MSG_GEN_PARAM_VALUE", "fieldName="+fieldName);
    // int index = template.getFieldIndex(fieldName);
    // return fields[index];

    String[] fieldNames = template.getFieldNames();
    int size = fieldNames.length;

    String theFieldName;

    for (int i = 0; i < size; i++) {
      theFieldName = fieldNames[i];
      if (theFieldName.equalsIgnoreCase(fieldName))
        return fields[i];
    }
    SilverTrace.warn("form", "GenericDataRecord.getField",
        "form.EXP_UNKNOWN_FIELD", "fieldName '" + fieldName
        + "' in DB not found in XML descriptor");
    return null;
    // throw new FormException("DataRecord", "form.EXP_UNKNOWN_FIELD");
  }

  /**
   * Returns the field at the index position in the record.
   * @throw FormException when the fieldIndex is unknown.
   */
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

  public String[] getFieldNames() {
    return template.getFieldNames();
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}
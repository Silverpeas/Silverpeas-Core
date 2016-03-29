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

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A GenericRecordTemplate builds GenericDataRecord. It use a map : Map (FieldName ->
 * (index,GenericFieldTemplate))
 */
@XmlRootElement(name = "recordTemplate")
@XmlAccessorType(XmlAccessType.NONE)
public class GenericRecordTemplate implements RecordTemplate, Serializable {
  private static final long serialVersionUID = 5454875955919676819L;

  private Map<String, IndexedFieldTemplate> fields = new LinkedHashMap<>();

  @XmlElement(name = "fieldTemplate", type=GenericFieldTemplate.class)
  private List<FieldTemplate> fieldList = new ArrayList<>();
  private String templateName;

  /**
   * A GenericRecordTemplate is built empty : use addFieldTemplate for each field.
   */
  public GenericRecordTemplate() {
  }

  public List<FieldTemplate> getFieldList() {
    return fieldList;
  }

  public void setFieldList(List<FieldTemplate> fieldList) {
    this.fieldList = new ArrayList<>(fieldList);
  }

  Map<String, IndexedFieldTemplate> getFields() {
    if (fields == null || fields.isEmpty()) {
      for (FieldTemplate aFieldList : fieldList) {
        GenericFieldTemplate field = (GenericFieldTemplate) aFieldList;
        field.setTemplateName(templateName);
        addFieldTemplate(field);
      }
    }
    return Collections.unmodifiableMap(fields);
  }

  /**
   * Adds a new field template at the end of this record template.
   */
  public void addFieldTemplate(FieldTemplate fieldTemplate) {
    IndexedFieldTemplate indexed = new IndexedFieldTemplate(fields.size(), fieldTemplate);
    fields.put(fieldTemplate.getFieldName(), indexed);
  }

  /**
   * Returns all the field names of the DataRecord built on this template.
   */
  @Override
  public String[] getFieldNames() {
    return getFields().keySet().toArray(new String[getFields().size()]);
  }

  /**
   * Returns all the field templates.
   */
  @Override
  public FieldTemplate[] getFieldTemplates() {
    FieldTemplate[] fieldsArray = new FieldTemplate[getFields().keySet().size()];
    for (IndexedFieldTemplate field : getFields().values()) {
      fieldsArray[field.index] = field.fieldTemplate;
    }
    return fieldsArray;
  }

  /**
   * Returns the FieldTemplate of the named field.
   * @throws FormException if the field name is unknown.
   */
  @Override
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    IndexedFieldTemplate indexed = getFields().get(fieldName);
    if (indexed == null) {
      throw new FormException("GenericRecordTemplate",
          "form.EXP_UNKNOWN_FIELD", fieldName);
    }

    return indexed.fieldTemplate;
  }

  /**
   * Returns the field index of the named field.
   * @throws FormException if the field name is unknown.
   */
  @Override
  public int getFieldIndex(String fieldName) throws FormException {
    IndexedFieldTemplate indexed = getFields().get(fieldName);
    if (indexed == null) {
      throw new FormException("GenericRecordTemplate",
          "form.EXP_UNKNOWN_FIELD", fieldName);
    }

    return indexed.index;
  }

  /**
   * Returns an empty DataRecord built on this template.
   */
  @Override
  public DataRecord getEmptyRecord() throws FormException {
    return new GenericDataRecord(this);
  }

  /**
   * Returns true if the data record is built on this template and all the constraints are ok.
   */
  @Override
  public boolean checkDataRecord(DataRecord record) {
    return true; // xoxox à implémenter
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

}

final class IndexedFieldTemplate implements Serializable {

  private static final long serialVersionUID = 7951905766676133561L;

  public final int index;
  public final FieldTemplate fieldTemplate;

  public IndexedFieldTemplate(int index, FieldTemplate fieldTemplate) {
    this.index = index;
    this.fieldTemplate = fieldTemplate;
  }
}
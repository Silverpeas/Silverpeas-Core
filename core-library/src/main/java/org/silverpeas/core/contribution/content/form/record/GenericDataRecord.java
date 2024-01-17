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
package org.silverpeas.core.contribution.content.form.record;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A GenericDataRecord is made up of an array of {@link Field}s and a {@link GenericRecordTemplate}
 * from which the fields are modelled.
 */
public class GenericDataRecord implements DataRecord, Serializable {

  private static final long serialVersionUID = 1L;
  private int id = -1;
  private String externalId;
  private final Field[] fields;
  private final transient RecordTemplate template;
  private String language;
  private final Map<String, List<Field>> fieldsByName;

  /**
   * A GenericDataRecord is built from a {@link RecordTemplate} instance.
   * @param template the template modelling the fields that make this data record.
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
    fields = allFields.toArray(new Field[0]);
  }

  @Override
  public String getId() {
    return externalId;
  }

  @Override
  public void setId(String id) {
    this.externalId = id;
  }

  /**
   * Gets all the fields set in this data record.
   * @return an array of {@link Field}s
   */
  public Field[] getFields() {
    return fields;
  }

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
      SilverLogger.getLogger(this)
          .warn("Unknown field {0}: not found in the XML descriptor", fieldName);
    }

    return field;
  }

  @Override
  public Field getField(int fieldIndex) throws FormException {
    if (fieldIndex >= 0 && fieldIndex < fields.length) {
      return fields[fieldIndex];
    } else {
      throw new FormException("DataRecord", "form.EXP_INDEX_OUT_OF_BOUNDS");
    }
  }

  @Override
  public int size() {
    return fields.length;
  }

  @Override
  public boolean isNew() {
    return (id == -1);
  }

  /**
   * Gets the internal id. For internal mechanism of record set.
   */
  int getInternalId() {
    return id;
  }

  /**
   * Sets the internal id. For internal mechanism or record set.
   */
  void setInternalId(int id) {
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
    String[] fieldNames = getFieldNames();
    PagesContext pageContext = new PagesContext();
    pageContext.setObjectId(externalId);
    if (template instanceof IdentifiedRecordTemplate) {
      pageContext.setComponentId(((IdentifiedRecordTemplate) template).getInstanceId());
    }
    pageContext.setLanguage(language);
    for (String fieldName : fieldNames) {
      try {
        Field field = getField(fieldName);
        GenericFieldTemplate fieldTemplate =
            (GenericFieldTemplate) template.getFieldTemplate(fieldName);
        FieldDisplayer<Field> fieldDisplayer =
            TypeManager.getInstance().getDisplayer(fieldTemplate.getTypeName(), "simpletext");
        if ("wysiwyg".equals(fieldTemplate.getDisplayerName())) {
          fieldTemplate.setReadOnly(true);
          fieldDisplayer =
              TypeManager.getInstance().getDisplayer(fieldTemplate.getTypeName(), "wysiwyg");
        }
        if (fieldTemplate.isRepeatable()) {
          // returns each value (of a repeatable field) separated by " / "
          formValues.put(fieldName,
              getRepeatableFieldDisplayableValues(field, fieldTemplate, fieldDisplayer,
                  pageContext));
        } else {
          String value = getFieldDisplayableValue(field, fieldTemplate, fieldDisplayer, pageContext);
          if (StringUtil.isDefined(value)) {
            formValues.put(fieldName, value);
          }
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("Cannot get value of field " + fieldName + " of the object " + externalId, e);
      }
    }
    return formValues;
  }

  private String getRepeatableFieldDisplayableValues(Field field, FieldTemplate fieldTemplate,
      FieldDisplayer<Field> fieldDisplayer, PagesContext pageContext) throws FormException {
    int maxOccurrences = fieldTemplate.getMaximumNumberOfOccurrences();
    StringBuilder fieldValues = new StringBuilder();
    for (int occ = 0; occ < maxOccurrences; occ++) {
      final Field fieldOcc = getField(field.getName(), occ);
      if (fieldOcc != null && !fieldOcc.isNull()) {
        String value =
            getFieldDisplayableValue(fieldOcc, fieldTemplate, fieldDisplayer, pageContext);
        if (StringUtil.isDefined(value)) {
          if (fieldValues.length() > 0) {
            fieldValues.append(" / ");
          }
          fieldValues.append(value);
        }
      }
    }
    return fieldValues.toString();
  }

  private <T extends Field> String getFieldDisplayableValue(T field, FieldTemplate fieldTemplate,
      FieldDisplayer<T> fieldDisplayer, PagesContext pageContext) throws FormException {
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw);
    fieldDisplayer.display(out, field, fieldTemplate, pageContext);
    return sw.toString();
  }

  @Override
  public ResourceReference getResourceReference() {
    String componentId = "unknown";
    if (template instanceof IdentifiedRecordTemplate) {
      componentId = ((IdentifiedRecordTemplate) template).getInstanceId();
    }
    return new ResourceReference(externalId, componentId);
  }
}
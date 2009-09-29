package com.silverpeas.workflow.engine.dataRecord;

import java.util.HashMap;
import java.util.Iterator;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.workflow.api.model.Column;
import com.silverpeas.workflow.api.model.Item;
import com.silverpeas.workflow.api.model.Presentation;
import com.silverpeas.workflow.api.model.ProcessModel;

/**
 * ProcessInstanceRowTemplate.
 */
public class ProcessInstanceRowTemplate implements RecordTemplate {
  /**
   * Builds the record template of the process instance rows.
   */
  public ProcessInstanceRowTemplate(ProcessModel processModel, String role,
      String lang) {
    this.processModel = processModel;
    this.role = role;
    this.lang = lang;
    init();
  }

  /**
   * Returns all the field names of the DataRecord built on this template.
   */
  public String[] getFieldNames() {
    if (fieldNames == null) {
      fieldNames = new String[fields.size()];
      Iterator names = fields.keySet().iterator();
      String name;
      while (names.hasNext()) {
        name = (String) names.next();
        try {
          fieldNames[getFieldIndex(name)] = name;
        } catch (Exception e) {
          // can't happen : the name is a key
        }
      }
    }
    return fieldNames;
  }

  /**
   * Returns all the field templates.
   */
  public FieldTemplate[] getFieldTemplates() throws FormException {
    if (fieldTemplates == null) {
      fieldTemplates = new FieldTemplate[fields.size()];
      Iterator names = fields.keySet().iterator();
      String name;
      while (names.hasNext()) {
        name = (String) names.next();
        try {
          fieldTemplates[getFieldIndex(name)] = getFieldTemplate(name);
        } catch (Exception e) {
          // can't happen : the name is a key
        }
      }
    }
    return fieldTemplates;
  }

  /**
   * Returns the FieldTemplate of the named field.
   * 
   * @throw FormException if the field name is unknown.
   */
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    IndexedFieldTemplate indexed = (IndexedFieldTemplate) fields.get(fieldName);

    if (indexed == null) {
      throw new FormException("ProcessInstanceRecordTemplate",
          "form.EXP_UNKNOWN_FIELD", fieldName);
    }

    return indexed.fieldTemplate;
  }

  /**
   * Returns the FieldTemplate at the given position
   * 
   * @throw FormException if the field index is out of bound.
   */
  public FieldTemplate getFieldTemplate(int fieldIndex) throws FormException {
    if (0 <= fieldIndex && fieldIndex < fields.size()) {
      return getFieldTemplates()[fieldIndex];
    } else {
      throw new FormException("ProcessInstanceRecordTemplate",
          "form.EXP_UNKNOWN_FIELD", "" + fieldIndex);
    }
  }

  /**
   * Returns the Field index of the named field.
   * 
   * @throw FormException if the field name is unknown.
   */
  public int getFieldIndex(String fieldName) throws FormException {
    IndexedFieldTemplate indexed = (IndexedFieldTemplate) fields.get(fieldName);

    if (indexed == null) {
      throw new FormException("ProcessInstanceRecordTemplate",
          "form.EXP_UNKNOWN_FIELD", fieldName);
    }

    return indexed.index;
  }

  /**
   * Throws an illegal call exception, since an empty DataRecord can't be built
   * from this template.
   */
  public DataRecord getEmptyRecord() throws FormException {
    throw new FormException("workflowEngine", "workflowEngine.EXP_ILLEGAL_CALL");
  }

  /**
   * Returns true if the data record is built on this template.
   */
  public boolean checkDataRecord(DataRecord record) {
    if (record instanceof ProcessInstanceRowRecord) {
      ProcessInstanceRowRecord rowRecord = (ProcessInstanceRowRecord) record;
      return this == rowRecord.template;
    } else
      return false;
  }

  /**
   * Builds a Field[] with the correct size().
   */
  public Field[] buildFieldsArray() {
    return new Field[fields.size()];
  }

  /**
   * The process model.
   */
  private final ProcessModel processModel;

  /**
   * The role giving this view of the process.
   */
  private final String role;

  /**
   * The lang used for this view of the process.
   */
  private final String lang;

  /**
   * The field names.
   */
  private String[] fieldNames = null;

  /**
   * The field templates.
   */
  private FieldTemplate[] fieldTemplates = null;

  /**
   * The map (fieldName -> IndexedFieldTemplate).
   */
  private HashMap fields = new HashMap();

  /**
   * Inits the fields
   */
  private void init() {
    addField(new TitleTemplate("title", processModel, role, lang));
    addField(new StateTemplate("instance.state", processModel, role, lang));

    Presentation presentation = processModel.getPresentation();
    if (presentation != null) {
      Column[] columns = presentation.getColumns(role);
      Item item;
      for (int i = 0; columns != null && i < columns.length; i++) {
        item = columns[i].getItem();
        if (item != null) {
          addField(new ItemTemplate(item.getName(), item, role, lang));
        }
      }
    }

  }

  /**
   * Adds the given fieldTemplate.
   */
  private void addField(FieldTemplate fieldTemplate) {
    fields.put(fieldTemplate.getFieldName(), new IndexedFieldTemplate(fields
        .size(), fieldTemplate));

  }
}

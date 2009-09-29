package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.ProcessInstance;

/**
 * A ProcessInstanceDataRecord groups in a single DataRecord all the data items
 * of a ProcessInstance.
 * 
 * The instance : instance instance.title instance.<columnName> The model :
 * model model.label model.peas-label The folder : <folderItem> The forms :
 * form.<formName> form.<formName>.title form.<formName>.<fieldItem> The actions
 * : action.<actionName> action.<actionName>.label action.<actionName>.date
 * action.<actionName>.actor The users : participant.<participantName>
 */
public class ProcessInstanceDataRecord implements DataRecord {
  /**
   * Builds the data record representation of a process instance.
   */
  public ProcessInstanceDataRecord(ProcessInstance instance, String role,
      String lang) throws WorkflowException {
    this.instance = instance;
    this.template = (ProcessInstanceRecordTemplate) instance.getProcessModel()
        .getAllDataTemplate(role, lang);
    this.fields = template.buildFieldsArray();
  }

  /**
   * Returns the data record id.
   */
  public String getId() {
    return instance.getInstanceId();
  }

  /**
   * The id of an instance is immutable.
   */
  public void setId(String externalId) {
    // do nothing
  }

  /**
   * An instance is always registred.
   */
  public boolean isNew() {
    return true;
  }

  /**
   * Returns the named field.
   * 
   * @throw FormException when the fieldName is unknown.
   */
  public Field getField(String fieldName) throws FormException {
    return getField(template.getFieldIndex(fieldName));
  }

  /**
   * Returns the field at the index position in the record.
   * 
   * @throw FormException when the fieldIndex is unknown.
   */
  public Field getField(int fieldIndex) throws FormException {
    Field field = fields[fieldIndex];
    if (field == null) {
      ProcessInstanceFieldTemplate fieldTemplate = (ProcessInstanceFieldTemplate) template
          .getFieldTemplate(fieldIndex);
      field = fieldTemplate.getField(instance);
      fields[fieldIndex] = field;
    }
    return field;
  }

  public String[] getFieldNames() {
    return template.getFieldNames();
  }

  public String getLanguage() {
    return null;
  }

  public void setLanguage(String lang) {
    // do nothing
  }

  /**
   * The process instance whose data are managed by this data record.
   */
  final ProcessInstance instance;

  /**
   * The record template associated to this data record.
   */
  final ProcessInstanceRecordTemplate template;

  /**
   * The fields.
   */
  final Field[] fields;

}

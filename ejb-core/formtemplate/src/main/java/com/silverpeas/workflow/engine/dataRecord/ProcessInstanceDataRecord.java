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
package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.ProcessInstance;

/**
 * A ProcessInstanceDataRecord groups in a single DataRecord all the data items of a
 * ProcessInstance. The instance : instance instance.title instance.<columnName> The model : model
 * model.label model.peas-label The folder : <folderItem> The forms : form.<formName>
 * form.<formName>.title form.<formName>.<fieldItem> The actions : action.<actionName>
 * action.<actionName>.label action.<actionName>.date action.<actionName>.actor The users :
 * participant.<participantName>
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
   * @throw FormException when the fieldName is unknown.
   */
  public Field getField(String fieldName) throws FormException {
    return getField(template.getFieldIndex(fieldName));
  }

  /**
   * Returns the field at the index position in the record.
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

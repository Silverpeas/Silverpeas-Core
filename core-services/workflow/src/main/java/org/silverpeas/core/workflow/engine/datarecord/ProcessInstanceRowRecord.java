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

package org.silverpeas.core.workflow.engine.datarecord;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.user.User;

/**
 * ProcessInstanceDataRecord
 */
public class ProcessInstanceRowRecord extends AbstractProcessInstanceDataRecord {

  private static final long serialVersionUID = 5258805262791365104L;

  /**
   * Builds the data record representation of a process instance.
   */
  public ProcessInstanceRowRecord(ProcessInstance instance, String role,
      String lang) throws WorkflowException {
    this.instance = instance;
    this.template = (ProcessInstanceRowTemplate) instance.getProcessModel()
        .getRowTemplate(role, lang);
    this.fields = template.buildFieldsArray();
  }

  /**
   * Returns the data record id.
   */
  public String getId() {
    return instance.getInstanceId();
  }

  /**
   * Returns true if this instance is locked by the workflow engine.
   */
  public boolean isLockedByAdmin() {
    return instance.isLockedByAdmin();
  }

  /**
   * Returns error status.
   */
  public boolean isInError() {
    return instance.getErrorStatus();
  }

  /**
   * Returns timeout status.
   */
  public boolean isInTimeout() {
    return instance.getTimeoutStatus();
  }

  /**
   * Returns true if the given user is a working on this instance.
   */
  public boolean isWorking(User user) {
    if (user == null)
      return false;

    Actor[] workers = null;
    try {
      workers = instance.getWorkingUsers();
    } catch (WorkflowException e) {
      return false;
    }

    for (int i = 0; i < workers.length; i++) {
      if (user.equals(workers[i].getUser()))
        return true;
    }
    return false;
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

  public ProcessInstance getFullProcessInstance() {
    return instance;
  }

  /**
   * The process instance whose data are managed by this data record.
   */
  final ProcessInstance instance;

  /**
   * The record template associated to this data record.
   */
  final ProcessInstanceRowTemplate template;

  /**
   * The fields.
   */
  final Field[] fields;

}

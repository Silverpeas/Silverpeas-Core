/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
    super(instance, role, lang);
  }

  @Override
  protected ProcessInstanceTemplate getTemplate(final String role, final String lang)
      throws WorkflowException {
    return (ProcessInstanceRowTemplate) instance.getProcessModel().getRowTemplate(role, lang);
  }

  /**
   * Returns the data record id.
   */
  @Override
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
  @Override
  public Field getField(String fieldName) throws FormException {
    return getField(template.getFieldIndex(fieldName));
  }

  @Override
  public String[] getFieldNames() {
    return template.getFieldNames();
  }

  public ProcessInstance getFullProcessInstance() {
    return instance;
  }



}

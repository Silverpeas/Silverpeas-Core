/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;

/**
 * A ProcessInstanceDataRecord groups in a single DataRecord all the data items of a
 * ProcessInstance. The instance : instance instance.title instance.<columnName> The model : model
 * model.label model.peas-label The folder : <folderItem> The forms : form.<formName>
 * form.<formName>.title form.<formName>.<fieldItem> The actions : action.<actionName>
 * action.<actionName>.label action.<actionName>.date action.<actionName>.actor The users :
 * participant.<participantName>
 */
public class ProcessInstanceDataRecord extends AbstractProcessInstanceDataRecord {

  private static final long serialVersionUID = 4538018078050395139L;

  /**
   * Builds the data record representation of a process instance.
   */
  public ProcessInstanceDataRecord(ProcessInstance instance, String role,
      String lang) throws WorkflowException {
    super(instance, role, lang);
  }

  @Override
  protected ProcessInstanceTemplate getTemplate(final String role, final String lang)
      throws WorkflowException {
    return (ProcessInstanceRecordTemplate) instance.getProcessModel()
        .getAllDataTemplate(role, lang);
  }

  /**
   * Returns the data record id.
   */
  @Override
  public String getId() {
    return instance.getInstanceId();
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
  public Field getField(String fieldName, int occurrence) {
    try {
      return getField(fieldName);
    } catch (FormException e) {
      SilverLogger.getLogger(this).error(failureOnGetting("fieldName", fieldName), e);
    }
    return null;
  }

  @Override
  public String[] getFieldNames() {
    return template.getFieldNames();
  }

  @Override
  public ResourceReference getResourceReference() {
    return new ResourceReference(instance.getInstanceId(), instance.getModelId());
  }

}

/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProcessInstanceDataRecord implements DataRecord {

  private static final long serialVersionUID = 1L;

  /**
   * The fields.
   */
  protected final Field[] fields;

  /**
   * The process instance whose data are managed by this data record.
   */
  protected final ProcessInstance instance;

  /**
   * The record template associated to this data record.
   */
  protected final ProcessInstanceTemplate template;

  /**
   * Builds the data record representation of a process instance.
   */
  public AbstractProcessInstanceDataRecord(ProcessInstance instance, String role, String lang)
      throws WorkflowException {
    this.instance = instance;
    this.template = getTemplate(role, lang);
    if (this.template != null) {
      this.fields = template.buildFieldsArray();
    } else {
      this.fields = new Field[0];
    }
  }

  protected abstract ProcessInstanceTemplate getTemplate(final String role, final String lang)
      throws WorkflowException;

  @Override
  public String getId() {
    return null;
  }

  /**
   * The id of an instance is immutable.
   */
  @Override
  public void setId(String externalId) {
    // do nothing
  }

  /**
   * An instance is always registred.
   */
  @Override
  public boolean isNew() {
    return true;
  }

  @Override
  public Field getField(String fieldName) throws FormException {
    return null;
  }

  @Override
  public Field getField(String fieldName, int occurrence) {
    return null;
  }

  @Override
  public Field getField(int fieldIndex) throws FormException {
    Field field = fields[fieldIndex];
    if (field == null) {
      ProcessInstanceFieldTemplate fieldTemplate =
          (ProcessInstanceFieldTemplate) template.getFieldTemplate(fieldIndex);
      field = fieldTemplate.getField(instance);
      fields[fieldIndex] = field;
    }
    return field;
  }

  @Override
  public String[] getFieldNames() {
    return new String[0];
  }

  @Override
  public String getLanguage() {
    return null;
  }

  @Override
  public void setLanguage(String lang) {
    // do nothing
  }

  @Override
  public Map<String, String> getValues(String language) {
    // no implemented yet !
    return new HashMap<>();
  }

  @Override
  public ResourceReference getResourceReference() {
    return null;
  }

}

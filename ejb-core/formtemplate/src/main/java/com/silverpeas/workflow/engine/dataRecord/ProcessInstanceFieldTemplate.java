/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import java.util.HashMap;
import java.util.Map;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.workflow.api.instance.ProcessInstance;

/**
 * A ProcessInstanceFieldTemplate describes a field of a process instance.
 */
public abstract class ProcessInstanceFieldTemplate implements FieldTemplate {
  public ProcessInstanceFieldTemplate(String fieldName, String typeName,
      String displayerName, String label) {
    this.fieldName = fieldName;
    this.typeName = typeName;
    this.displayerName = displayerName;
    this.label = label;
  }

  /**
   * Returns the field name of the Field built on this template.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Returns the type name of the described field.
   */
  public String getTypeName() {
    return typeName;
  }

  /**
   * Returns the name of the FieldDisplayer to display the described field.
   */
  public String getDisplayerName() {
    return displayerName;
  }

  /**
   * Returns the default label of the described field.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns the local label of the described field.
   */
  public String getLabel(String language) {
    return label;
  }

  /**
   * Returns an empty array : this implementation use only a default local.
   */
  public String[] getLanguages() {
    return new String[0];
  }

  /**
   * Returns false since a process instance field is read only.
   */
  public boolean isMandatory() {
    return false;
  }

  /**
   * Returns true since a process instance field is read only.
   */
  public boolean isReadOnly() {
    return true;
  }

  /**
   * Returns false.
   */
  public boolean isDisabled() {
    return false;
  }

  /**
   * Returns false.
   */
  public boolean isHidden() {
    return false;
  }

  /**
   * Returns a Map (String -> String) of named parameters which can be used by the displayer
   * (max-size, length ...).
   */
  public Map getParameters(String language) {
    return new HashMap();
  }

  /**
   * Throws an illegal call exception, since an empty field can't be built from this template.
   * @see getField
   */
  public Field getEmptyField() throws FormException {
    throw new FormException("workflowEngine", "workflowEngine.EXP_ILLEGAL_CALL");
  }

  public boolean isSearchable() {
    return false;
  }

  public String getTemplateName() {
    return "unknown";
  }

  /**
   * Returns a field built from this template and filled from the given process instance.
   */
  abstract public Field getField(ProcessInstance instance) throws FormException;

  /**
   * The field name.
   */
  private final String fieldName;

  /**
   * The field type name.
   */
  private final String typeName;

  /**
   * The final displayer name.
   */
  private final String displayerName;

  /**
   * The label
   */
  private final String label;

}

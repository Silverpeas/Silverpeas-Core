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
package org.silverpeas.core.workflow.engine.datarecord;

import java.util.*;

import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.record.Parameter;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;

/**
 * A ProcessInstanceFieldTemplate describes a field of a process instance.
 */
public abstract class ProcessInstanceFieldTemplate implements FieldTemplate {
  protected ProcessInstanceFieldTemplate(String fieldName, String typeName,
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
  public Map<String, String> getParameters(String language) {
    return Map.of();
  }

  @Override
  public List<Parameter> getParameters() {
    return List.of();
  }

  @Override
  public FieldValuesTemplate getFieldValuesTemplate(String language) {
    return new FieldValuesTemplate(language);
  }

  /**
   * Throws an illegal call exception, since an empty field can't be built from this template.
   */
  public Field getEmptyField() throws FormException {
    throw new FormException("Unsupported operation");
  }

  @Override
  public Field getEmptyField(int occurrence) throws FormException {
    throw new FormException("Unsupported operation");
  }

  public boolean isSearchable() {
    return false;
  }

  public String getTemplateName() {
    return "unknown";
  }

  public boolean isUsedAsFacet() {
    return false;
  }

  @Override
  public int getMaximumNumberOfOccurrences() {
    return 1;
  }

  @Override
  public boolean isRepeatable() {
    return false;
  }

  /**
   * Returns a field built from this template and filled from the given process instance.
   */
  public abstract Field getField(ProcessInstance instance) throws FormException;

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

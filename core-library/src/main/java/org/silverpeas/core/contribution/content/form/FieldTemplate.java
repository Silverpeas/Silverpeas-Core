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

package org.silverpeas.core.contribution.content.form;

import java.util.List;
import java.util.Map;

import org.silverpeas.core.contribution.content.form.record.Parameter;

/**
 * A FieldTemplate describes a specific field of a DataRecord. A FieldTemplate gives the field name,
 * type information and display information.
 * @see DataRecord
 * @see RecordTemplate
 */
public interface FieldTemplate {
  /**
   * Returns the field name of the Field built on this template.
   */
  public String getFieldName();

  /**
   * Returns the type name of the described field.
   */
  public String getTypeName();

  /**
   * Returns the name of the FieldDisplayer to display the described field.
   */
  public String getDisplayerName();

  /**
   * Returns the label of the described field (in the default locale).
   */
  public String getLabel();

  /**
   * Returns the local label of the described field.
   */
  public String getLabel(String lang);

  /**
   * Returns the locals
   */
  public String[] getLanguages();

  /**
   * Returns true when the described field must have a value.
   */
  public boolean isMandatory();

  /**
   * Returns true when the described field can't be updated.
   */
  public boolean isReadOnly();

  /**
   * Returns true when the described field must be disabled.
   */
  public boolean isDisabled();

  /**
   * Returns true when the described field must be hidden.
   */
  public boolean isHidden();

  /**
   * Returns a Map (String -> String) of named parameters which can be used by the displayer
   * (max-size, length ...).
   */
  public Map<String, String> getParameters(String language);

  public List<Parameter> getParametersObj();

  /**
   * Returns an empty Field built on this template.
   */
  public Field getEmptyField() throws FormException;
  public Field getEmptyField(int occurrence) throws FormException;

  public boolean isSearchable();

  public String getTemplateName();

  public boolean isUsedAsFacet();

  public int getMaximumNumberOfOccurrences();

  public boolean isRepeatable();

}
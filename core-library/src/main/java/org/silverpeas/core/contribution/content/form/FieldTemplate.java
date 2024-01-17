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
package org.silverpeas.core.contribution.content.form;

import org.silverpeas.core.contribution.content.form.record.Parameter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A FieldTemplate describes a specific field of a DataRecord. A FieldTemplate gives the field name,
 * type information and display information.
 * @see DataRecord
 * @see RecordTemplate
 */
public interface FieldTemplate extends Serializable {
  /**
   * Returns the field name of the Field built on this template.
   */
  String getFieldName();

  /**
   * Returns the type name of the described field.
   */
  String getTypeName();

  /**
   * Returns the name of the FieldDisplayer to display the described field.
   */
  String getDisplayerName();

  /**
   * Returns the label of the described field (in the default locale).
   */
  String getLabel();

  /**
   * Returns the local label of the described field.
   */
  String getLabel(String lang);

  /**
   * Returns the locals
   */
  String[] getLanguages();

  /**
   * Returns true when the described field must have a value.
   */
  boolean isMandatory();

  /**
   * Returns true when the described field can't be updated.
   */
  boolean isReadOnly();

  /**
   * Returns true when the described field must be disabled.
   */
  boolean isDisabled();

  /**
   * Returns true when the described field must be hidden.
   */
  boolean isHidden();

  /**
   * Returns a Map (String -> String) of named parameters which can be used by the displayer
   * (max-size, length ...).
   */
  Map<String, String> getParameters(String language);

  List<Parameter> getParametersObj();

  /**
   * Returns an empty Field built on this template.
   */
  Field getEmptyField() throws FormException;
  Field getEmptyField(int occurrence) throws FormException;

  boolean isSearchable();

  String getTemplateName();

  boolean isUsedAsFacet();

  int getMaximumNumberOfOccurrences();

  boolean isRepeatable();

}
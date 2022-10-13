/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.model;

import org.silverpeas.core.workflow.api.WorkflowException;

import java.util.Iterator;
import java.util.Map;

/**
 * Interface describing a representation of the &lt;item&gt; element of a Process Model.
 */
public interface Item {
  /**
   * Get the name of this item
   * @return item's name
   */
  String getName();

  /**
   * Set the name of this item
   * @param name item 's name
   */
  void setName(String name);

  /**
   * Get description in specific language for the given role
   * @param language description's language
   * @param role role for which the description is
   * @return wanted description as a String object. If description is not found, search description
   * with given role and default language, if not found again, return the default description in
   * given language, if not found again, return the default description in default language, if not
   * found again, return empty string.
   */
  String getDescription(String role, String language);

  /**
   * Get all the descriptions
   * @return an object containing the collection of the descriptions
   */
  ContextualDesignations getDescriptions();

  /**
   * Get label in specific language for the given role
   * @param language label's language
   * @param role role for which the label is
   * @return wanted label as a String object. If label is not found, search label with given role
   * and default language, if not found again, return the default label in given language, if not
   * found again, return the default label in default language, if not found again, return empty
   * string.
   */
  String getLabel(String role, String language);

  /**
   * Get all the labels
   * @return an object containing the collection of the labels
   */
  ContextualDesignations getLabels();

  /**
   * Get value of computed attribute
   * @return true if item must be computed
   */
  boolean isComputed();

  /**
   * Set value of computed attribute
   * @param computed true if item must be computed
   */
  void setComputed(boolean computed);

  /**
   * Get formula to use if item must be computed
   * @return formula of type 'action.Validation.actor'
   */
  String getFormula();

  /**
   * Set formula to use if item must be computed
   * @param formula formula of type 'action.Validation.actor'
   */
  void setFormula(String formula);

  /**
   * Get value of readOnly attribute
   * @return true if item must be readonly
   */
  boolean isReadonly();

  /**
   * Set value of readOnly attribute
   * @param readonly true if item must be readonly
   */
  void setReadonly(boolean readonly);

  /**
   * Get the type of this item
   * @return item's type (text for text field)
   */
  String getType();

  /**
   * Set the type of this item
   * @param type item 's type (text for text field)
   */
  void setType(String type);

  /**
   * Get the full user field name, to which this item is map
   * @return full user field name
   */
  String getMapTo();

  /**
   * Set the full user field name, to which this item is map
   * @param mapTo full user field name
   */
  void setMapTo(String mapTo);

  /**
   * Get the parameter specified by name
   * @param strName the parameter name
   * @return the parameters
   */
  Parameter getParameter(String strName);

  /**
   * Create an object implementing Parameter
   * @return
   */
  Parameter createParameter();

  /**
   * Add a Parameter to the collection
   * @param parameter
   */
  void addParameter(Parameter parameter);

  /**
   * Return an Iterator over the parameters collection
   * @return
   */
  Iterator<Parameter> iterateParameter();

  /**
   * Remove the parameter specified by its name
   * @param strName the name of the parameter
   * @throws WorkflowException when the parameter cannot be found
   */
  void removeParameter(String strName) throws WorkflowException;

  /**
   * @return
   */
  Map<String, String> getKeyValuePairs();
}
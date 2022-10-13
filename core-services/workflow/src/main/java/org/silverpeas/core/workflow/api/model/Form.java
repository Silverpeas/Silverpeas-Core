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

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.workflow.api.WorkflowException;

import java.util.Iterator;

/**
 * Interface describing a representation of the &lt;Form&gt; element of a Process Model.
 */
public interface Form {
  /**
   * Get the name of this form
   * @return form's name
   */
  String getName();

  /**
   * Set the name of this form
   * @param name form's name
   */
  void setName(String name);

  /**
   * Get the role
   * @return the role name
   */
  String getRole();

  /**
   * Set the role name
   * @param role name to set
   */
  void setRole(String role);

  /**
   * Get the name of HTML file to show this form if no HTML file is defined, XMLForm will be used to
   * display the form
   * @return form's name
   */
  String getHTMLFileName();

  /**
   * Set the name of HTML file to show this form if no HTML file is defined, XMLForm will be used to
   * display the form
   * @return form's name
   */
  void setHTMLFileName(String htmlFileName);

  /**
   * Get the inputs
   * @return the inputs as a Vector
   */
  Input[] getInputs();

  /**
   * Get the input specified by the index
   * @param idx the index
   * @return the items as a Vector
   */
  Input getInput(int idx);

  /**
   * Get the input specified by item and / or value
   * @param reference the reference object
   * @return the items as a Vector
   */
  Input getInput(Input reference);

  /**
   * Create and return and object implementing Input
   */
  Input createInput();

  /**
   * Iterate through the inputs
   * @return an iterator
   */
  Iterator<Input> iterateInput();

  /**
   * Add an input
   */
  void addInput(Input input);

  /**
   * Remove the input specified by the index
   * @param idx the index
   */
  void removeInput(int idx) throws WorkflowException;

  /**
   * Get all the titles
   * @return an object containing the collection of the tables
   */
  ContextualDesignations getTitles();

  /**
   * Get title in specific language for the given role
   * @param language title's language
   * @param role role for which the title is
   * @return wanted title as a String object. If title is not found, search title with given role
   * and default language, if not found again, return the default title in given language, if not
   * found again, return the default title in default language, if not found again, return empty
   * string.
   */
  String getTitle(String role, String language);

  /**
   * Converts this object in a DataRecord object
   * @return the resulting DataRecord object with the default values set
   */
  DataRecord getDefaultRecord(String role, String lang, DataRecord data)
      throws WorkflowException;

  /**
   * Converts this object in a RecordTemplate object
   * @return the resulting RecordTemplate
   */
  RecordTemplate toRecordTemplate(String roleName, String language)
      throws WorkflowException;

  /**
   * Converts this object in a RecordTemplate object
   * @return the resulting RecordTemplate
   */
  RecordTemplate toRecordTemplate(String roleName, String language,
      boolean readOnly) throws WorkflowException;
}
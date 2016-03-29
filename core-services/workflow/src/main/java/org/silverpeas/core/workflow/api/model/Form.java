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

package org.silverpeas.core.workflow.api.model;

import java.util.Iterator;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;Form&gt; element of a Process Model.
 */
public interface Form {
  /**
   * Get the name of this form
   * @return form's name
   */
  public String getName();

  /**
   * Set the name of this form
   * @param name form's name
   */
  public void setName(String name);

  /**
   * Get the role
   * @return the role name
   */
  public String getRole();

  /**
   * Set the role name
   * @param role name to set
   */
  public void setRole(String role);

  /**
   * Get the name of HTML file to show this form if no HTML file is defined, XMLForm will be used to
   * display the form
   * @return form's name
   */
  public String getHTMLFileName();

  /**
   * Set the name of HTML file to show this form if no HTML file is defined, XMLForm will be used to
   * display the form
   * @return form's name
   */
  public void setHTMLFileName(String HTMLFileName);

  /**
   * Get the inputs
   * @return the inputs as a Vector
   */
  public Input[] getInputs();

  /**
   * Get the input specified by the index
   * @param idx the index
   * @return the items as a Vector
   */
  public Input getInput(int idx);

  /**
   * Get the input specified by item and / or value
   * @param reference the reference object
   * @return the items as a Vector
   */
  public Input getInput(Input reference);

  /**
   * Create and return and object implementing Input
   */
  public Input createInput();

  /**
   * Iterate through the inputs
   * @return an iterator
   */
  public Iterator<Input> iterateInput();

  /**
   * Add an input Method needed primarily by Castor
   */
  public void addInput(Input input);

  /**
   * Remove the input specified by the index
   * @param idx the index
   */
  public void removeInput(int idx) throws WorkflowException;

  /**
   * Get all the titles
   * @return an object containing the collection of the tables
   */
  public ContextualDesignations getTitles();

  /**
   * Get title in specific language for the given role
   * @param lang title's language
   * @param role role for which the title is
   * @return wanted title as a String object. If title is not found, search title with given role
   * and default language, if not found again, return the default title in given language, if not
   * found again, return the default title in default language, if not found again, return empty
   * string.
   */
  public String getTitle(String role, String language);

  /**
   * Iterate through the titles
   * @return an iterator
   */
  public Iterator<ContextualDesignation> iterateTitle();

  /**
   * Add a title Method needed primarily by Castor
   */
  public void addTitle(ContextualDesignation description);

  /**
   * Create an object implementing ContextualDesignation Method needed primarily by Castor
   */
  public ContextualDesignation createDesignation();

  /**
   * Converts this object in a DataRecord object
   * @return the resulting DataRecord object with the default values set
   */
  public DataRecord getDefaultRecord(String role, String lang, DataRecord data)
      throws FormException, WorkflowException;

  /**
   * Converts this object in a RecordTemplate object
   * @return the resulting RecordTemplate
   */
  public RecordTemplate toRecordTemplate(String roleName, String language)
      throws WorkflowException;

  /**
   * Converts this object in a RecordTemplate object
   * @return the resulting RecordTemplate
   */
  public RecordTemplate toRecordTemplate(String roleName, String language,
      boolean readOnly) throws WorkflowException;
}

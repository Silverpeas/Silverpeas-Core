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

import org.silverpeas.core.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;forms&gt; element of a Process Model.
 */
public interface Forms {

  /**
   * Iterate through the Form objects
   * @return an iterator
   */
  public Iterator<Form> iterateForm();

  /**
   * Add an form to the collection
   * @param form to be added
   */
  public void addForm(Form form);

  /**
   * Create an Form
   * @return an object implementing Form
   */
  public Form createForm();

  /**
   * Get the form definition with given name. Works fine for forms other than 'presentationForm',
   * since they have unique names.
   * @param name action form
   * @return form definition
   */
  public Form getForm(String name);

  /**
   * Get the form definition with given name for the given role, will return the form dedicated to
   * that role or, if the former has not been found, a generic form with this name
   * @param name action form
   * @param role role name
   * @return wanted form definition
   */
  public Form getForm(String name, String role);

  /**
   * Remove the form identified by name and role
   * @param strName the form name
   * @param strRole the name of the role, may be <code>null</code>
   * @throws WorkflowException if the role cannot be found
   */
  public void removeForm(String strName, String strRole) throws WorkflowException;
}

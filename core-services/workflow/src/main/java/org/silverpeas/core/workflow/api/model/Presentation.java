/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.model;

import org.silverpeas.core.workflow.api.WorkflowException;

import java.util.Iterator;

/**
 * Interface describing a representation of the &lt;presentation&gt; element of a Process Model.
 */
public interface Presentation {
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
   * Get all the titles
   * @return an object containing the collection of the titles
   */
  ContextualDesignations getTitles();

  /**
   * Get the contents of the Columns object with the given role name, or of the 'Columns' for the
   * default role if nothing for the specified role can be found.
   * @param roleName the name of the role
   * @return the contents of 'Columns' as an array of 'Column'
   */
  Column[] getColumns(String roleName);

  /**
   * Get the Columns object referenced by the given role name
   * @param strRoleName the name of the Columns object
   * @return a Columns implementation
   */
  Columns getColumnsByRole(String strRoleName);

  /**
   * Iterate through the Column objects
   * @return an iterator
   */
  Iterator<Columns> iterateColumns();

  /**
   * Add an column to the collection
   * @param columns to be added
   */
  void addColumns(Columns columns);

  /**
   * Create an Column
   * @return an object implementing Column
   */
  Columns createColumns();

  /**
   * Delete the Columns object with the given name
   * @param strRoleName the name of the Columns object (a role name)
   * @throws WorkflowException when the Columns for the given name have not been found.
   */
  void deleteColumns(String strRoleName) throws WorkflowException;
}
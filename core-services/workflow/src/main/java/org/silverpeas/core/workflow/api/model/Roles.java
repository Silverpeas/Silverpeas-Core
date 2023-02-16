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

import java.io.Serializable;
import java.util.Iterator;

/**
 * Interface describing a representation of the &lt;roles&gt; element of a Process Model.
 */
public interface Roles extends Serializable {
  /**
   * Iterate through the Role objects
   * @return an iterator
   */
  Iterator<Role> iterateRole();

  /**
   * Create a Role
   * @return an object implementing Role
   */
  Role createRole();

  /**
   * Add an role to the collection
   * @param role to be added
   */
  void addRole(Role role);

  /**
   * Get the roles definition
   * @return roles definition
   */
  Role[] getRoles();

  /**
   * Get the role definition with given name
   * @param name role name
   * @return wanted role definition
   */
  Role getRole(String name);

  /**
   * Remove an role from the collection
   * @param strRoleName the name of the role to be removed.
   * @throws WorkflowException if the role cannot be found.
   */
  void removeRole(String strRoleName) throws WorkflowException;
}

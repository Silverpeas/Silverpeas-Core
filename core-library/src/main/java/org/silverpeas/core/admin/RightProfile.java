/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin;

import java.util.List;

/**
 * It represents a profile of right access for a resource in Silverpeas that can be a space,
 * a component instance, a node or whatever. A right profile defines the users and the user groups
 * that can access a resource with some well defined privileges.
 * @author mmoquillon
 */
public interface RightProfile {

  /**
   * Adds a user group to the right profile.
   * @param id the unique identifier of an existing user group.
   */
  void addGroup(final String id);

  /**
   * Adds a user to the right profile.
   * @param id the unique identifier of an existing user.
   */
  void addUser(final String id);

  /**
   * Removes from this right profile the specified user group.
   * @param id the unique identifier of an existing user group.
   */
  void removeGroup(final String id);

  /**
   * Removes from this right profile the specified user.
   * @param id the unique identifier of an existing user.
   */
  void removeUser(final String id);

  /**
   * Gets all the users set in this right profile. In others words, the users that have access
   * the resource to which this right profile refers.
   * @return a list of user identifiers.
   */
  List<String> getAllUsers();

  /**
   * Gets all the user groups set in this right profile. In others words, the groups that have
   * access the resource to which this right profile refers.
   * @return a list of user group identifiers.
   */
  List<String> getAllGroups();
}

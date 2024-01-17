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
package org.silverpeas.core.admin.user.service;

import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

/**
 * In charge of providing groups.
 * @author mmoquillon
 */
public interface GroupProvider {

  /**
   * Gets the instance of the implementation of the interface.
   * @return an implementation of {@link GroupProvider}.
   */
  static GroupProvider get() {
    return ServiceProvider.getSingleton(GroupProvider.class);
  }

  /**
   * Gets a group of users from the specified identifier.
   * @param groupId a group identifier as string.
   * @return a group instance of {@link org.silverpeas.core.admin.user.model.Group}.
   */
  Group getGroup(String groupId);

  /**
   * Gets all {@link GroupState#VALID} root groups in Silverpeas. A root group is the group of
   * users without any other parent group.
   * @return a list of root groups.
   */
  List<Group> getAllRootGroups();

  /**
   * Gets all {@link GroupState#VALID}  root groups of users that are defined in the specified
   * domain in Silverpeas.
   * @param domainId the unique identifier of a Silverpeas domain.
   * @return a list of root groups belonging to the specified domain.
   */
  List<Group> getAllRootGroupsInDomain(String domainId);
}

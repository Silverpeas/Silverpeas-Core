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
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.user.service.GroupProvider;

import java.io.Serializable;
import java.util.List;

/**
 * A group of users in Silverpeas.
 * @author mmoquillon
 */
public interface Group extends Serializable, Comparable<Group> {

  /**
   * Gets the detail about the specified group of users.
   * @param groupId the unique identifier of the group to get.
   * @return the detail about the group with the specified identifier or null if no such group
   * exists.
   */
  static Group getById(String groupId) {
    return GroupProvider.get().getGroup(groupId);
  }

  /**
   * Gets all root groups available in Silverpeas, whatever their domain.
   * @return a list with all the groups in the Silverpeas portal.
   */
  static List<Group> getAllRoots() {
    return GroupProvider.get().getAllRootGroups();
  }

  /**
   * Gets all root groups available in the specified domain in Silverpeas.
   * @param domainId the unique identifier of the domain to which the root groups belong.
   * @return a list with all the root user groups in the specified domain.
   */
  static List<Group> getAllRootsInDomain(String domainId) {
    return GroupProvider.get().getAllRootGroupsInDomain(domainId);
  }

  /**
   * Gets the unique identifier of this group.
   * @return the group unique identifier.
   */
  String getId();

  /**
   * Get the domain id where the group is stored
   * @return the user domain identifier.
   */
  String getDomainId();

  /**
   * Get the father group id
   * @return the identifier of the group parent of this group. Null the group has no parent.
   */
  String getSuperGroupId();

  /**
   * Get the group name
   * @return the group name.
   */
  String getName();

  /**
   * Get the group description
   * @return the group description.
   */
  String getDescription();

  /**
   * Get the list of users in the group
   * @return the identifiers of the users in this group.
   */
  String[] getUserIds();

  /**
   * Gets the synchronization rule that is applied to this group.
   * @return a synchronization rule or null if no such rule is defined for this group.
   */
  String getRule();

  /**
   * Is this group synchronized from a remote domain service?
   * @return true if this group is synchronized, false otherwise.
   */
  boolean isSynchronized();

  /**
   * Gets the number of direct users in this group; the users from its subgroups aren't counted. To
   * count also the users in its subgroups, please use the
   * {@code org.silverpeas.core.admin.user.model.Group#getTotalNbUsers} method instead.
   * @return the number of direct users.
   */
  int getNbUsers();

  /**
   * Gets the total number of users in this group and in its subgroups. Users that are in several
   * groups are counted only once.
   * <p>
   * Depending on the requester, the total number of users can omit some users by their state
   * (usually the users whose their account is deactivated). By default, all the users whose
   * the account is deleted aren't taken into account.
   * @return the total number of distinct users in its group and subgroups.
   */
  int getTotalNbUsers();

  /**
   * Is this group is a root one?
   * A root group is a group that has no father group.
   * @return true if this group is a root one, false otherwise.
   */
  boolean isRoot();

  /**
   * Gets the direct subgroups of this user group.
   * @return a list with its direct subgroups. If this group hasn't children group, then the
   * returned list is empty.
   */
  List<Group> getSubGroups();

  /**
   * Gets the detail about all the users that are in this group (and in the subgroups of this
   * group).
   * @return a list of all the user details in this group.
   */
  List<User> getAllUsers();
}

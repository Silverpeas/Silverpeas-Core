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

import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.service.GroupProvider;

import java.io.Serializable;
import java.util.Date;
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
   * Gets all {@link GroupState#VALID} root groups in Silverpeas. A root group is the group of
   * users without any other parent group.
   * @return a list with all the groups in the Silverpeas portal.
   */
  static List<Group> getAllRoots() {
    return GroupProvider.get().getAllRootGroups();
  }

  /**
   * Gets all {@link GroupState#VALID} root groups available in the specified domain in Silverpeas.
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
   * Get the list of the direct users in the group
   * @return the identifiers of the direct users in this group.
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
   * Gets the count of direct users in this group; the users from its subgroups aren't counted. To
   * count also the users in its subgroups, please use the
   * {@link Group#getTotalUsersCount()} method instead.
   * @return the number of direct users.
   */
  int getDirectUsersCount();

  /**
   * Gets the total count of users in this group and in its subgroups. Users that are in several
   * groups are counted only once.
   * <p>
   * Depending on the requester, the total count of users can omit some users by their state
   * (usually the users whose their account is deactivated). By default, all the users whose
   * the account is deleted aren't taken into account.
   * @return the total number of distinct users in its group and subgroups.
   */
  int getTotalUsersCount();

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
   * @param <T> the concrete type of the {@link Group}
   */
  <T extends Group> List<T> getSubGroups();

  /**
   * Gets the detail about all the direct users in this group. The users in the subgroups of this
   * group aren't taken into account; to get all the users, whatever they are in this group or in
   * one of its subgroup, please consider the {@link Group#getAllUsers()} method.
   * @return a list of all of the direct users in this group. An empty list if the group has no
   * direct users.
   * @param <T> the concrete type of the {@link User}
   */
  <T extends User> List<T> getUsers();

  /**
   * Gets the detail about all the users that are in this group (and in the subgroups of this
   * group).
   * @return a list of all the user details in this group.
   * @param <T> the concrete type of the {@link User}
   */
  <T extends User> List<T> getAllUsers();

  /**
   * Gets the date of the group creation.
   * @return creation date of the group as {@link Date}.
   */
  Date getCreationDate();

  /**
   * Gets the date of the last group save.
   * @return the date of the last group save as {@link Date}.
   */
  Date getSaveDate();

  /**
   * Please use {@link Group#isValidState()} to retrieve group validity information.
   * Please use {@link Group#isRemovedState()} to retrieve group removed information.
   * This method returns the stored state information but not the functional information.
   * @return the state of the group.
   */
  GroupState getState();

  /**
   * This method is the only one able to indicate the group validity state. Please do not use {@link
   * Group#getState()} to retrieve group validity information.
   * @return true if valid state, false otherwise.
   */
  boolean isValidState();

  /**
   * This method is the only one able to indicate the group removed state. Please do not use {@link
   * Group#getState()} to retrieve group removed information.
   * @return true if deleted state, false otherwise.
   */
  boolean isRemovedState();

  /**
   * Gets the last date of the last state save.
   * @return the date of last state save (when it changes) as {@link Date}.
   */
  Date getStateSaveDate();

  /**
   * Is this group of users is directly managed by an application in Silverpeas or is it a group
   * managed in a user domain?
   * @return true if this group is managed by an application. False if it is managed by a user
   * domain.
   */
  boolean isApplicationManaged();
}

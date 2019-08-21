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
 * FLOSS exception. You should have received a copy of the text describing
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmoquillon
 */
public abstract class BaseRightProfile implements RightProfile, Serializable {

  private String id = "";
  private String name = "";
  private String label = "";
  private String description = "";
  private boolean isInherited = false;
  private List<String> groups = new ArrayList<>();
  private List<String> users = new ArrayList<>();

  /**
   * Sets a unique identifier to this profile. Shouldn't be used. Reserved to the persistence
   * layer.
   * @param sId the unique identifier of the profile.
   */
  public void setId(String sId) {
    id = sId;
  }

  /**
   * Gets a unique identifier of this profile.
   * @return the profile identifier.
   */
  public String getId() {
    return id;
  }

  /**
   * Sets a name to this profile. The name of the profile defines by convention the privileges
   * that are granted to the users and groups defined in this profile. In Silverpeas, the privileges
   * are defined by convention in different predefined roles and it is the name of this role that
   * should be set here.
   * @param sName the name of the role referred by this profile.
   */
  public void setName(String sName) {
    name = sName;
  }

  /**
   * Gets the name of this profile. The name of the profile defines by convention the privileges
   * that are granted to the users and groups defined in this profile. In Silverpeas, the privileges
   * are defined by convention in different predefined roles and it is the name of this role that
   * is returned here.
   * @return the name of the role referred by this profile.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets a label to this profile. A label is a user-friendly name given to this profile.
   * @param sLabel a label to give to this profile.
   */
  public void setLabel(String sLabel) {
    label = sLabel;
  }

  /**
   * Gets a label of this profile. A label is a user-friendly name given to this profile.
   * @return the profile label.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets a short description to this profile
   * @param sDescription a textual description.
   */
  public void setDescription(String sDescription) {
    description = sDescription;
  }

  /**
   * Gets a short description of this profile.
   * @return a profile description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Is the right accesses defined by this profile are inherited by another right profile, a
   * parent right profile?
   * @return true if the right accesses are inherited. False otherwise.
   */
  public boolean isInherited() {
    return isInherited;
  }

  /**
   * Sets the inheritance in right accesses to this profile.
   * @param isInherited a boolean indicating if the right accesses are inherited from another
   * right profile.
   */
  public void setInherited(boolean isInherited) {
    this.isInherited = isInherited;
  }

  /**
   * Gets the number of groups that are concerned by this profile
   * @return
   */
  public int getNumGroup() {
    return groups.size();
  }

  /**
   * Adds among the groups covered by this profile the specified group.
   * @param sGroupId the unique identifier of a group.
   */
  @Override
  public void addGroup(String sGroupId) {
    if (!groups.contains(sGroupId)) {
      groups.add(sGroupId);
    }
  }

  /**
   * Removes from the groups concerned by this profile the specified group.
   * @param sGroupId the unique identifier of a group.
   */
  @Override
  public void removeGroup(String sGroupId) {
    groups.remove(sGroupId);
  }

  /**
   * Gets all the groups that are concerned by this profile.
   * @return a list of group identifiers.
   */
  public List<String> getAllGroups() {
    return groups;
  }

  /**
   * Removes all the groups concerned by this profile.
   */
  public void removeAllGroups() {
    groups.clear();
  }

  /**
   * Gets the number of users covered by this profile.
   * @return the number of users in this profile.
   */
  public int getNumUser() {
    return users.size();
  }

  /**
   * Adds a user as being covered by this profile.
   * @param sUserId a unique identifier of a user.
   */
  @Override
  public void addUser(String sUserId) {
    if (!users.contains(sUserId)) {
      users.add(sUserId);
    }
  }

  /**
   * Removes the specified user among those covered by this profile.
   * @param sUserId a unique identifier of a user.
   */
  @Override
  public void removeUser(String sUserId) {
    users.remove(sUserId);
  }

  /**
   * Gets all the users concerned by this profile.
   * @return a list of user identifiers.
   */
  public List<String> getAllUsers() {
    return users;
  }

  /**
   * Removes all the users concerned by this profile.
   */
  public void removeAllUsers() {
    users.clear();
  }

  /**
   * Adds all the specified users in this profile.
   * @param users a list of user identifiers.
   */
  public void addUsers(List<String> users) {
    ArrayList<String> a = new ArrayList<>(users);
    a.removeAll(this.users);
    this.users.addAll(a);
  }

  /**
   * Adds all the specified groups in this profile.
   * @param groups a list of group identifiers.
   */
  public void addGroups(List<String> groups) {
    ArrayList<String> a = new ArrayList<>(groups);
    a.removeAll(this.groups);
    this.groups.addAll(a);
  }

  public void setUsers(List<String> users) {
    this.users.clear();
    this.users.addAll(users);
  }

  public void setGroups(List<String> groups) {
    this.groups.clear();
    this.groups.addAll(groups);
  }

  /**
   * Is this right profile empty?
   * @return true if no users or groups are concerned by this right profile. False otherwise.
   */
  public boolean isEmpty() {
    return this.groups.isEmpty() && this.users.isEmpty();
  }
}
  
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
package org.silverpeas.core.admin.space;

import org.silverpeas.core.admin.RightProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * A right profile on a collaboration space in Silverpeas. Such a profile defines the users and the
 * user groups that can access a given space in Silverpeas with some well defined privileges. By
 * default all the right profiles of a space are inherited to the component instances that are
 * included in those spaces.
 */
public class SpaceProfileInst implements RightProfile {

  public static final String SPACE_MANAGER = "Manager";

  private static final long serialVersionUID = 1L;
  private String id;
  private String name;
  private String label;
  private String description;
  private String spaceFatherId;
  private List<String> groups;
  private List<String> users;

  private boolean isInherited = false;

  /** Creates new SpaceProfileInst */
  public SpaceProfileInst() {
    id = "";
    name = "";
    label = "";
    description = "";
    spaceFatherId = "";
    groups = new ArrayList<>();
    users = new ArrayList<>();
  }

  public void setId(String sId) {
    id = sId;
  }

  public String getId() {
    return id;
  }

  public void setName(String sName) {
    name = sName;
  }

  public String getName() {
    return name;
  }

  public void setLabel(String sLabel) {
    label = sLabel;
  }

  public String getLabel() {
    return label;
  }

  public void setDescription(String sDescription) {
    description = sDescription;
  }

  public String getDescription() {
    return description;
  }

  public void setSpaceFatherId(String sSpaceFatherId) {
    spaceFatherId = sSpaceFatherId;
  }

  public String getSpaceFatherId() {
    return spaceFatherId;
  }

  public int getNumGroup() {
    return groups.size();
  }

  public String getGroup(int nIndex) {
    return groups.get(nIndex);
  }

  @Override
  public void addGroup(String sGroupId) {
    if (!groups.contains(sGroupId)) {
      groups.add(sGroupId);
    }
  }

  @Override
  public void removeGroup(String sGroupId) {
    groups.remove(sGroupId);
  }

  public List<String> getAllGroups() {
    return groups;
  }

  public void removeAllGroups() {
    groups = new ArrayList<>();
  }

  public int getNumUser() {
    return users.size();
  }

  public String getUser(int nIndex) {
    return users.get(nIndex);
  }

  @Override
  public void addUser(String sUserId) {
    if (!users.contains(sUserId)) {
      users.add(sUserId);
    }
  }

  @Override
  public void removeUser(String sUserId) {
    users.remove(sUserId);
  }

  public void addUsers(List<String> users) {
    ArrayList<String> a = new ArrayList<>(users);
    a.removeAll(this.users);
    this.users.addAll(a);
  }

  public void setUsers(List<String> users) {
    this.users.clear();
    this.users.addAll(users);
  }

  public void addGroups(List<String> groups) {
    ArrayList<String> a = new ArrayList<>(groups);
    a.removeAll(this.groups);
    this.groups.addAll(a);
  }

  public void setGroups(List<String> groups) {
    this.groups.clear();
    this.groups.addAll(groups);
  }

  public List<String> getAllUsers() {
    return users;
  }

  public void removeAllUsers() {
    users = new ArrayList<>();
  }

  public boolean isInherited() {
    return isInherited;
  }

  public void setInherited(boolean isInherited) {
    this.isInherited = isInherited;
  }

  public boolean isManager() {
    return SPACE_MANAGER.equalsIgnoreCase(name);
  }

  /**
   * Is this profile empty?
   * @return true if there is no users and no groups in this profile. Otherwise false.
   */
  public boolean isEmpty() {
    return users.isEmpty() && groups.isEmpty();
  }

  @SuppressWarnings("unchecked")
  protected SpaceProfileInst copy() {
    SpaceProfileInst clone = new SpaceProfileInst();
    clone.setDescription(description);
    clone.setInherited(isInherited);
    clone.setLabel(label);
    clone.setName(name);
    clone.setSpaceFatherId(spaceFatherId);
    clone.addGroups((List<String>)((ArrayList<String>) groups).clone());
    clone.addUsers((List<String>)((ArrayList<String>) users).clone());
    return clone;
  }
}
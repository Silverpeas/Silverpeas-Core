/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroupProfileInst implements Serializable {

  private static final long serialVersionUID = -9158575581807428715L;
  private String id = "";
  private String name = "";
  private String groupId = "";
  private final List<String> groups = new ArrayList<>();
  private final List<String> users = new ArrayList<>();

  /** Creates new GroupProfileInst */
  public GroupProfileInst() {
    // Nothing to do
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

  public void setGroupId(String sGroupId) {
    groupId = sGroupId;
  }

  public String getGroupId() {
    return groupId;
  }

  public int getNumGroup() {
    return groups.size();
  }

  public String getGroup(int nIndex) {
    return groups.get(nIndex);
  }

  public void addGroup(String sGroupId) {
    groups.add(sGroupId);
  }

  public void removeGroup(String sGroupId) {
    groups.remove(sGroupId);
  }

  public List<String> getAllGroups() {
    return groups;
  }

  public void removeAllGroups() {
    groups.clear();
  }

  public int getNumUser() {
    return users.size();
  }

  public String getUser(int nIndex) {
    return users.get(nIndex);
  }

  public void addUser(String sUserId) {
    users.add(sUserId);
  }

  public void removeUser(String sUserId) {
    users.remove(sUserId);
  }

  public void addUsers(List<String> users) {
    this.users.addAll(users);
  }

  public void addGroups(List<String> groups) {
    this.groups.addAll(groups);
  }

  public List<String> getAllUsers() {
    return users;
  }

  public void removeAllUsers() {
    users.clear();
  }

  public void setUsers(List<String> users) {
    removeAllUsers();
    addUsers(users);
  }

  public void setGroups(List<String> groups) {
    removeAllGroups();
    addGroups(groups);
  }
}
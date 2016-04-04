/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

/**
 * @author  neysseri
 * @version 1.0
 */

package org.silverpeas.core.admin.user.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroupProfileInst implements Serializable {

  private static final long serialVersionUID = -9158575581807428715L;
  private String m_sId;
  private String m_sName;
  private String m_sGroupId;
  private ArrayList<String> m_alGroups;
  private ArrayList<String> m_alUsers;

  /** Creates new GroupProfileInst */
  public GroupProfileInst() {
    m_sId = "";
    m_sName = "";
    m_sGroupId = "";
    m_alGroups = new ArrayList<String>();
    m_alUsers = new ArrayList<String>();
  }

  public void setId(String sId) {
    m_sId = sId;
  }

  public String getId() {
    return m_sId;
  }

  public void setName(String sName) {
    m_sName = sName;
  }

  public String getName() {
    return m_sName;
  }

  public void setGroupId(String sGroupId) {
    m_sGroupId = sGroupId;
  }

  public String getGroupId() {
    return m_sGroupId;
  }

  public int getNumGroup() {
    return m_alGroups.size();
  }

  public String getGroup(int nIndex) {
    return m_alGroups.get(nIndex);
  }

  public void addGroup(String sGroupId) {
    m_alGroups.add(sGroupId);
  }

  public void removeGroup(String sGroupId) {
    m_alGroups.remove(sGroupId);
  }

  public ArrayList<String> getAllGroups() {
    return m_alGroups;
  }

  public void removeAllGroups() {
    m_alGroups = new ArrayList<String>();
  }

  public int getNumUser() {
    return m_alUsers.size();
  }

  public String getUser(int nIndex) {
    return m_alUsers.get(nIndex);
  }

  public void addUser(String sUserId) {
    m_alUsers.add(sUserId);
  }

  public void removeUser(String sUserId) {
    m_alUsers.remove(sUserId);
  }

  public void addUsers(List<String> users) {
    m_alUsers.addAll(users);
  }

  public void addGroups(List<String> groups) {
    m_alGroups.addAll(groups);
  }

  public ArrayList<String> getAllUsers() {
    return m_alUsers;
  }

  public void removeAllUsers() {
    m_alUsers = new ArrayList<String>();
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
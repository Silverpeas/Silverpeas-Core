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
 *
 * @author  nchaix
 * @version
 */

package org.silverpeas.core.admin.user.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProfileInst implements Serializable, Cloneable {

  private static final long serialVersionUID = 3092416162986110340L;
  private String m_sId;
  private String m_sName;
  private String m_sLabel;
  private String m_sDescription;
  private String m_sComponentFatherId;
  private ArrayList<String> m_alGroups;
  private ArrayList<String> m_alUsers;

  private boolean isInherited = false;
  private int objectId = -1;
  private int objectFatherId = -1;
  private String objectType = null;

  /** Creates new ProfileInst */
  public ProfileInst() {
    m_sId = "";
    m_sName = "";
    m_sLabel = "";
    m_sDescription = "";
    m_sComponentFatherId = "";
    m_alGroups = new ArrayList<String>();
    m_alUsers = new ArrayList<String>();
  }

  @Override
  @SuppressWarnings(
      {"unchecked", "CloneDoesntDeclareCloneNotSupportedException", "CloneDoesntCallSuperClone"})
  public Object clone() {
    ProfileInst pi = new ProfileInst();
    pi.m_sName = m_sName;
    pi.m_sLabel = m_sLabel;
    pi.m_sDescription = m_sDescription;
    pi.m_sComponentFatherId = m_sComponentFatherId;
    pi.isInherited = isInherited;
    pi.objectId = objectId;
    pi.objectType = objectType;
    pi.m_alGroups = (ArrayList<String>) m_alGroups.clone();
    pi.m_alUsers = (ArrayList<String>) m_alUsers.clone();
    return pi;
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

  public void setLabel(String sLabel) {
    m_sLabel = sLabel;
  }

  public String getLabel() {
    return m_sLabel;
  }

  public void setDescription(String sDescription) {
    m_sDescription = sDescription;
  }

  public String getDescription() {
    return m_sDescription;
  }

  public void setComponentFatherId(String sComponentFatherId) {
    m_sComponentFatherId = sComponentFatherId;
  }

  public String getComponentFatherId() {
    return m_sComponentFatherId;
  }

  public int getNumGroup() {
    return m_alGroups.size();
  }

  public String getGroup(int nIndex) {
    return m_alGroups.get(nIndex);
  }

  public void addGroup(String sGroupId) {
    if (!m_alGroups.contains(sGroupId)) {
      m_alGroups.add(sGroupId);
    }
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
    if (!m_alUsers.contains(sUserId)) {
      m_alUsers.add(sUserId);
    }
  }

  public void removeUser(String sUserId) {
    m_alUsers.remove(sUserId);
  }

  public ArrayList<String> getAllUsers() {
    return m_alUsers;
  }

  public void removeAllUsers() {
    m_alUsers = new ArrayList<String>();
  }

  public void addUsers(List<String> users) {
    ArrayList<String> a = new ArrayList<String>(users);
    a.removeAll(this.m_alUsers);
    m_alUsers.addAll(a);
  }

  public void addGroups(List<String> groups) {
    ArrayList<String> a = new ArrayList<String>(groups);
    a.removeAll(this.m_alGroups);
    m_alGroups.addAll(a);
  }

  public boolean isInherited() {
    return isInherited;
  }

  public void setInherited(boolean isInherited) {
    this.isInherited = isInherited;
  }

  public int getObjectId() {
    return objectId;
  }

  public void setObjectId(int objectId) {
    this.objectId = objectId;
  }

  public int getObjectFatherId() {
    return objectFatherId;
  }

  public void setObjectFatherId(int objectFatherId) {
    this.objectFatherId = objectFatherId;
  }

  public void setGroupsAndUsers(String[] groupIds, String[] userIds) {
    // groups
    for (int i = 0; groupIds != null && i < groupIds.length; i++) {
      if (groupIds[i] != null && groupIds[i].length() > 0) {
        addGroup(groupIds[i]);
      }
    }

    // users
    for (int i = 0; userIds != null && i < userIds.length; i++) {
      if (userIds[i] != null && userIds[i].length() > 0) {
        addUser(userIds[i]);
      }
    }
  }

  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public boolean isEmpty() {
    return getAllGroups().isEmpty() && getAllUsers().isEmpty();
  }
}
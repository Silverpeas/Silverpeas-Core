/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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
 * @author  lbertin
 * @version 1.0
 */

package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SpaceProfileInst extends Object implements Serializable {

  private static final long serialVersionUID = 1L;
  private String m_sId;
  private String m_sName;
  private String m_sLabel;
  private String m_sDescription;
  private String m_sSpaceFatherId;
  private ArrayList<String> m_alGroups;
  private ArrayList<String> m_alUsers;

  private boolean isInherited = false;

  /** Creates new SpaceProfileInst */
  public SpaceProfileInst() {
    m_sId = "";
    m_sName = "";
    m_sLabel = "";
    m_sDescription = "";
    m_sSpaceFatherId = "";
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

  public void setSpaceFatherId(String sSpaceFatherId) {
    m_sSpaceFatherId = sSpaceFatherId;
  }

  public String getSpaceFatherId() {
    return m_sSpaceFatherId;
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

  public boolean isInherited() {
    return isInherited;
  }

  public void setInherited(boolean isInherited) {
    this.isInherited = isInherited;
  }

  @Override
  protected SpaceProfileInst clone() {
    SpaceProfileInst clone = new SpaceProfileInst();
    
    clone.setDescription(m_sDescription);
    //clone.setId(m_sId);
    clone.setInherited(isInherited);
    clone.setLabel(m_sLabel);
    clone.setName(m_sName);
    clone.setSpaceFatherId(m_sSpaceFatherId);
    
    clone.addGroups((List<String>) m_alGroups.clone());
    clone.addUsers((List<String>) m_alUsers.clone());
    
    return clone;
  }
}
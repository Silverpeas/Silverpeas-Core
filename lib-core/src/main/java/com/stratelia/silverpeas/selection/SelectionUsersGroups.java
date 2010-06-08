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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.selection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class SelectionUsersGroups implements SelectionExtraParams {
  static OrganizationController m_oc = new OrganizationController();
  
  public final static int USER = 0;
  public final static int GROUP = 1;
  
  String m_domainId = null;
  String m_componentId = null;
  List<String> profileIds = null;
  ArrayList<String> profileNames = null;

  public String[] getProfileIds() {
    if (profileIds != null) {
      return (String[]) profileIds.toArray(new String[0]);
    } else {
      return null;
    }
  }

  public ArrayList<String> getProfileNames() {
    return profileNames;
  }

  public void setProfileNames(ArrayList<String> profileNames) {
    this.profileNames = profileNames;
    ComponentInst componentInst = m_oc.getComponentInst(m_componentId);
    int nbProfiles = componentInst.getNumProfileInst();
    ProfileInst profileInst = null;
    profileIds = new ArrayList<String>();
    for (int p = 0; p < nbProfiles; p++) {
      profileInst = componentInst.getProfileInst(p);
      if (profileNames.contains(profileInst.getName())) {
        profileIds.add(profileInst.getId());
      }
    }
  }

  public void setProfileIds(List<String> profileIds) {
    this.profileIds = profileIds;
  }

  public void addProfileId(String profileId) {
    if (profileIds == null)
      profileIds = new ArrayList<String>();

    profileIds.add(profileId);
  }

  public void addProfileIds(List<String> profileIds) {
    if (this.profileIds == null)
      this.profileIds = new ArrayList<String>();

    profileIds.addAll(profileIds);
  }

  public String getComponentId() {
    return m_componentId;
  }

  public void setComponentId(String componentId) {
    m_componentId = componentId;
  }

  public String getDomainId() {
    return m_domainId;
  }

  public void setDomainId(String domainId) {
    m_domainId = domainId;
  }

  public String getParameter(String name) {
    return null;
  }

  static public String[] getDistinctUserIds(String[] selectedUsers,
      String[] selectedGroups) {
    int g, u;
    HashSet<String> usersSet = new HashSet<String>();
    
    if (selectedUsers != null && selectedUsers.length > 0) {
      for (u = 0; u < selectedUsers.length; u++) {
        usersSet.add(selectedUsers[u]);
      }
    }
    if (selectedGroups != null && selectedGroups.length > 0) {
      for (g = 0; g < selectedGroups.length; g++) {
        UserDetail[] groupUsers = m_oc.getAllUsersOfGroup(selectedGroups[g]);
        for (u = 0; u < groupUsers.length; u++) {
          usersSet.add(groupUsers[u].getId());
        }
      }
    }
    return (String[]) usersSet.toArray(new String[0]);
  }

  static public UserDetail[] getUserDetails(String[] userIds) {
    return m_oc.getUserDetails(userIds);
  }

  static public Group[] getGroups(String[] groupIds) {
    if (groupIds != null && groupIds.length > 0) {
      Group[] valret = new Group[groupIds.length];
      for (int g = 0; g < groupIds.length; g++) {
        valret[g] = m_oc.getGroup(groupIds[g]);
      }
      return valret;
    } else {
      return new Group[0];
    }
  }

  static public String[] getUserIds(UserDetail[] users) {
    if (users == null) {
      return new String[0];
    }
    String[] valret = new String[users.length];
    for (int i = 0; i < users.length; i++) {
      valret[i] = users[i].getId();
    }
    return valret;
  }

  static public String[] getGroupIds(Group[] groups) {
    if (groups == null) {
      return new String[0];
    }
    String[] valret = new String[groups.length];
    for (int i = 0; i < groups.length; i++) {
      valret[i] = groups[i].getId();
    }
    return valret;
  }
}

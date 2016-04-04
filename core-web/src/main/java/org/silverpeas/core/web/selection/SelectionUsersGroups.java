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

package org.silverpeas.core.web.selection;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class SelectionUsersGroups implements SelectionExtraParams {
  static OrganizationController organizationController =  OrganizationControllerProvider
      .getOrganisationController();

  public final static int USER = 0;
  public final static int GROUP = 1;

  String domainId = null;
  String componentId = null;
  String objectId = null;
  List<String> profileIds = null;
  List<String> profileNames = null;

  @Deprecated
  public String[] getProfileIds() {
    if (profileIds != null) {
      return profileIds.toArray(new String[profileIds.size()]);
    }
    return null;
  }

  public String getJoinedProfileNames() {
    if (profileNames != null && !profileNames.isEmpty()) {
      StringBuilder names = new StringBuilder();
      for(int i = 0; i < profileNames.size() - 1; i++) {
        names.append(profileNames.get(i)).append(",");
      }
      names.append(profileNames.get(profileNames.size() - 1));
      return names.toString();
    }
    return null;
  }

  public List<String> getProfileNames() {
    return profileNames;
  }

  public void setProfileNames(List<String> profileNames) {
    this.profileNames = profileNames;
    ComponentInst componentInst = organizationController.getComponentInst(componentId);
    profileIds = new ArrayList<>();
    for (ProfileInst profileInst : componentInst.getAllProfilesInst()) {
      if (profileNames.contains(profileInst.getName())) {
        profileIds.add(profileInst.getId());
      }
    }
  }

  /**
   * Add the identifier of the role the users must play.
   * @param profileId the unique identifier of a user role.
   * @deprecated Use instead either both the setObjectId() and setProfileNames() methods to set the roles
   * for a given object in the component instance or the setProfileNames() method to set the roles
   * for the whole component instance.
   */
  @Deprecated
  public void addProfileId(String profileId) {
    if (profileIds == null) {
      profileIds = new ArrayList<>();
    }
    profileIds.add(profileId);
  }

  /**
   * Gets the identifier of the object in the component instance for which the users must have
   * enough right to access.
   * @return the unique identifier of the object, made up of its type followed by its identifier.
   */
  public String getObjectId() {
    return objectId;
  }

  /**
   * Sets the object in the component instance for which the users must have enough right to access.
   * @param objectId the unique identifier of the object in Silverpeas. It must be made up of its
   * type followed by its identifier.
   */
  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getDomainId() {
    return domainId;
  }

  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  @Override
  public String getParameter(String name) {
    return null;
  }

  static public String[] getDistinctUserIds(String[] selectedUsers,
      String[] selectedGroups) {
    HashSet<String> usersSet = new HashSet<String>();
    if (selectedUsers != null && selectedUsers.length > 0) {
      Collections.addAll(usersSet, selectedUsers);
    }
    if (selectedGroups != null && selectedGroups.length > 0) {
      for (String selectedGroup : selectedGroups) {
        UserDetail[] groupUsers = organizationController.getAllUsersOfGroup(selectedGroup);
        for (UserDetail groupUser : groupUsers) {
          usersSet.add(groupUser.getId());
        }
      }
    }
    return usersSet.toArray(new String[usersSet.size()]);
  }

  static public UserDetail[] getUserDetails(String[] userIds) {
    return organizationController.getUserDetails(userIds);
  }

  static public Group[] getGroups(String[] groupIds) {
    if (groupIds != null && groupIds.length > 0) {
      Group[] result = new Group[groupIds.length];
      for (int g = 0; g < groupIds.length; g++) {
        result[g] = organizationController.getGroup(groupIds[g]);
      }
      return result;
    }
    return new Group[0];
  }

  static public String[] getUserIds(UserDetail[] users) {
    if (users == null) {
      return new String[0];
    }
    String[] result = new String[users.length];
    for (int i = 0; i < users.length; i++) {
      result[i] = users[i].getId();
    }
    return result;
  }

  static public String[] getGroupIds(Group[] groups) {
    if (groups == null) {
      return new String[0];
    }
    String[] result = new String[groups.length];
    for (int i = 0; i < groups.length; i++) {
      result[i] = groups[i].getId();
    }
    return result;
  }
}

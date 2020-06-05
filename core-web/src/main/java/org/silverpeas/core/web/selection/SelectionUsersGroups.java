/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

public class SelectionUsersGroups {
  static OrganizationController organizationController =  OrganizationControllerProvider
      .getOrganisationController();

  public static final int USER = 0;
  public static final int GROUP = 1;

  private String domainId = null;
  private String componentId = null;
  private String objectId = null;
  private List<String> profileIds = null;
  private List<String> profileNames = null;

  public String getJoinedProfileNames() {
    if (profileNames != null && !profileNames.isEmpty()) {
      return String.join(",", profileNames);
    }
    return null;
  }

  public List<String> getProfileIds() {
    return this.profileIds;
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

  public static String[] getDistinctUserIds(String[] selectedUsers,
      String[] selectedGroups) {
    HashSet<String> usersSet = new HashSet<>();
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

  public static UserDetail[] getUserDetails(String[] userIds) {
    return organizationController.getUserDetails(userIds);
  }

  public static Group[] getGroups(String[] groupIds) {
    if (groupIds != null && groupIds.length > 0) {
      Group[] result = new Group[groupIds.length];
      for (int g = 0; g < groupIds.length; g++) {
        result[g] = organizationController.getGroup(groupIds[g]);
      }
      return result;
    }
    return new Group[0];
  }

  public static String[] getUserIds(UserDetail[] users) {
    if (users == null) {
      return new String[0];
    }
    String[] result = new String[users.length];
    for (int i = 0; i < users.length; i++) {
      result[i] = users[i].getId();
    }
    return result;
  }

  public static String[] getGroupIds(Group[] groups) {
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

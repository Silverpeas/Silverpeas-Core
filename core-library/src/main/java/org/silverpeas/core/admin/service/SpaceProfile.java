/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Getting user identifiers and group identifiers of behind a Silverpeas profiles linked to a
 * space.
 * @author Nicolas Eysseric
 */
public class SpaceProfile {

  private SpaceProfileInst profile;
  private Set<String> inheritedUserIds = new HashSet<>();
  private Set<String> inheritedGroupIds = new HashSet<>();

  public void setProfile(SpaceProfileInst profile) {
    this.profile = profile;
  }

  public List<String> getGroupIds() {
    if (profile == null) {
      return Collections.emptyList();
    }
    return profile.getAllGroups();
  }

  public List<String> getUserIds() {
    if (profile == null) {
      return Collections.emptyList();
    }
    return profile.getAllUsers();
  }

  public Set<String> getInheritedGroupIds() {
    return inheritedGroupIds;
  }

  public Set<String> getInheritedUserIds() {
    return inheritedUserIds;
  }

  public Set<String> getAllUserIds() {
    Set<String> ids = getInheritedUserIds();
    ids.addAll(getUserIds());
    return ids;
  }

  public Set<String> getAllGroupIds() {
    Set<String> ids = getInheritedGroupIds();
    ids.addAll(getGroupIds());
    return ids;
  }

  public Set<String> getAllUserIdsIncludingAllGroups() {
    Set<String> ids = getInheritedUserIds();
    for (String groupId : inheritedGroupIds) {
      ids.addAll(getAllUserIdsOfGroup(groupId));
    }

    ids.addAll(getUserIds());
    for (String groupId : getGroupIds()) {
      ids.addAll(getAllUserIdsOfGroup(groupId));
    }

    return ids;
  }

  @SuppressWarnings("unchecked")
  private List<String> getAllUserIdsOfGroup(String groupId) {
    List<String> userIds = new ArrayList<>();
    Group group = Group.getById(groupId);
    List<User> users = (List<User>) group.getAllUsers();
    for (User user : users) {
      userIds.add(user.getId());
    }
    return userIds;
  }

  void addInheritedProfile(SpaceProfileInst profile) {
    if (profile != null) {
      inheritedUserIds.addAll(profile.getAllUsers());
      inheritedGroupIds.addAll(profile.getAllGroups());
    }
  }

  public boolean isEmpty() {
    boolean emptyProfile =
        profile == null || (profile.getAllUsers().isEmpty() && profile.getAllGroups().isEmpty());
    return emptyProfile && inheritedUserIds.isEmpty() && inheritedGroupIds.isEmpty();
  }

}
package com.stratelia.webactiv.beans.admin;

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
  private Set<String> inheritedUserIds = new HashSet<String>();
  private Set<String> inheritedGroupIds = new HashSet<String>();

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
    List<String> userIds = new ArrayList<String>();
    Group group = Group.getById(groupId);
    List<UserDetail> users = (List<UserDetail>) group.getAllUsers();
    for (UserDetail user : users) {
      userIds.add(user.getId());
    }
    return userIds;
  }

  protected void addInheritedProfile(SpaceProfileInst profile) {
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
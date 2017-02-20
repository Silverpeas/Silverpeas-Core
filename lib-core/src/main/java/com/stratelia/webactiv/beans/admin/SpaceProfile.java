package com.stratelia.webactiv.beans.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Nicolas on 15/02/2017.
 */
public class SpaceProfile {

  private SpaceProfileInst profile;
  private List<String> inheritedUserIds = new ArrayList<String>();
  private List<String> inheritedGroupIds = new ArrayList<String>();

  public void setProfile(SpaceProfileInst profile) {
    this.profile = profile;
  }

  public List<String> getGroupIds() {
    if (profile == null) {
      return Collections.EMPTY_LIST;
    }
    return profile.getAllGroups();
  }

  public List<String> getUserIds() {
    if (profile == null) {
      return Collections.EMPTY_LIST;
    }
    return profile.getAllUsers();
  }

  public List<String> getInheritedGroupIds() {
    return inheritedGroupIds;
  }

  public List<String> getInheritedUserIds() {
    return inheritedUserIds;
  }

  public List<String> getAllUserIds() {
    List<String> ids = getInheritedUserIds();
    ids.addAll(getUserIds());
    return ids;
  }

  public List<String> getAllGroupIds() {
    List<String> ids = getInheritedGroupIds();
    ids.addAll(getGroupIds());
    return ids;
  }

  public List<String> getAllUserIdsIncludingAllGroups() {
    List<String> ids = getInheritedUserIds();
    for (String groupId : inheritedGroupIds) {
      ids.addAll(getAllUserIdsOfGroup(groupId));
    }

    ids.addAll(getUserIds());
    for (String groupId : getGroupIds()) {
      ids.addAll(getAllUserIdsOfGroup(groupId));
    }

    return ids;
  }

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
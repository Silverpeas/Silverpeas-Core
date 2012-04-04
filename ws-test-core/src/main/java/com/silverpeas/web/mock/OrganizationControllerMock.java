/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.web.mock;

import static com.silverpeas.util.StringUtil.isDefined;
import com.stratelia.webactiv.beans.admin.*;
import java.util.*;
import javax.inject.Named;

/**
 * A mock the OrganizationController objects for testing purpose.
 */
@Named("organizationController")
public class OrganizationControllerMock extends OrganizationController {

  private static final long serialVersionUID = -3271734262141821655L;
  private Map<String, UserDetail> users = new HashMap<String, UserDetail>();
  private Map<String, Group> groups = new HashMap<String, Group>();
  private Set<String> components = new HashSet<String>();

  @Override
  public UserDetail getUserDetail(String sUserId) {
    return users.get(sUserId);
  }

  /**
   * The behaviour matches the expected one from OrganizationController: if no users, null is
   * returned.
   */
  @Override
  public UserDetail[] getAllUsers() {
    UserDetail[] allUsers = new UserDetail[users.size()];
    int i = 0;
    for (UserDetail userDetail : users.values()) {
      allUsers[i++] = userDetail;
    }
    if (allUsers.length == 0) {
      return null;
    }
    return allUsers;
  }

  @Override
  public UserDetail[] getAllUsersInDomain(String domainId) {
    if (isDefined(domainId)) {
      List<UserDetail> allUsers = new ArrayList<UserDetail>();
      for (UserDetail userDetail : users.values()) {
        if (userDetail.getDomainId().equals(domainId)) {
          allUsers.add(userDetail);
        }
      }
      return allUsers.toArray(new UserDetail[allUsers.size()]);
    }
    return null;
  }

  /**
   * The behaviour matches the expected one from OrganizationController: if no g, null is returned.
   */
  @Override
  public Group[] getAllRootGroups() {
    List<Group> allGroups = new ArrayList<Group>();
    int i = 0;
    for (Group group : groups.values()) {
      if (group.isRoot()) {
        allGroups.add(group);
      }
    }
    if (allGroups.isEmpty()) {
      return null;
    }
    return allGroups.toArray(new Group[allGroups.size()]);
  }

  @Override
  public Group[] getAllRootGroupsInDomain(String domainId) {
    if (isDefined(domainId)) {
      List<Group> allGroups = new ArrayList<Group>();
      for (Group group : groups.values()) {
        if (group.getDomainId().equals(domainId) && group.isRoot()) {
          allGroups.add(group);
        }
      }
      return allGroups.toArray(new Group[allGroups.size()]);
    }
    return null;
  }

  @Override
  public int getAllSubUsersNumber(String sGroupId) {
    int count = 0;
    Group[] subgroups = getAllSubGroups(sGroupId);
    for (Group group : subgroups) {
      count += group.getNbUsers();
    }
    return count;
  }

  @Override
  public Group[] getAllSubGroups(String parentGroupId) {
    List<Group> allsubGroups = new ArrayList<Group>();
    for (Group group : groups.values()) {
      if (parentGroupId.equals(group.getSuperGroupId())) {
        allsubGroups.add(group);
      }
    }
    return allsubGroups.toArray(new Group[allsubGroups.size()]);
  }

  @Override
  public Group getGroup(String sGroupId) {
    return this.groups.get(sGroupId);
  }

  @Override
  public Group[] getAllGroups() {
    List<Group> allGroups = new ArrayList<Group>(groups.values());
    return allGroups.toArray(new Group[allGroups.size()]);
  }

  @Override
  public UserDetail[] getAllUsersOfGroup(String groupId) {
    List<UserDetail> theUsers = new ArrayList<UserDetail>();
    Group group = getGroup(groupId);
    Group[] subgroups = getAllSubGroups(groupId);
    for (String userId : group.getUserIds()) {
      theUsers.add(getUserDetail(userId));
    }
    for (Group aSubGroup : subgroups) {
      for (String userId : aSubGroup.getUserIds()) {
        theUsers.add(getUserDetail(userId));
      }
    }
    return theUsers.toArray(new UserDetail[theUsers.size()]);
  }

  /**
   * Adds a new user for tests.
   *
   * @param userDetail the detail about the user to add for tests.
   */
  public void addUserDetail(final UserDetail userDetail) {
    users.put(userDetail.getId(), userDetail);
  }

  /**
   * Adds a new user group for tests.
   *
   * @param group the user group to add for tests.
   */
  public void addGroup(final Group group) {
    groups.put(group.getId(), group);
  }

  /**
   * Clears all of the data used in tests.
   */
  public void clearAll() {
    users.clear();
    groups.clear();
  }

  @Override
  public String[] getUserProfiles(String userId, String componentId) {
    return ((UserDetailWithProfiles) users.get(userId)).getUserProfiles(componentId);
  }

  @Override
  public boolean isComponentExist(String componentId) {
    return components.contains(componentId);
  }

  @Override
  public Domain getDomain(String domainId) {
    Domain domain = new Domain();
    domain.setId(domainId);
    if ("0".equals(domainId)) {
      domain.setName("Silverpeas");
    } else if ("-1".equals(domainId)) {
      domain.setName("interne");
    } else {
      domain.setName("Domaine " + domainId);
    }
    return domain;
  }

  /**
   * Adds a component instance to use on tests. All component instances others than the added ones
   * are considered as non existing.
   *
   * @param componentId the unique identifier of the component instance to take into account in
   * tests.
   */
  public void addComponentInstance(String componentId) {
    components.add(componentId);
  }

  @Override
  public UserDetail[] searchUsers(UserDetail modelUser, boolean isAnd) {
    List<UserDetail> foundUsers = new ArrayList<UserDetail>();
    for (UserDetail user : users.values()) {
      boolean match = false;
      if (isDefined(modelUser.getId())) {
        match = user.getId().equals(modelUser.getId());
      } else if (isDefined(modelUser.getFirstName()) || isDefined(modelUser.getLastName())) {
        if (isDefined(modelUser.getFirstName())) {
          if (modelUser.getFirstName().endsWith("%")) {
            String name = modelUser.getFirstName().replace("%", "");
            match = user.getFirstName().startsWith(name);
          } else {
            match = user.getFirstName().equals(modelUser.getFirstName());
          }
        }
        if (!match && isDefined(modelUser.getLastName())) {
          if (modelUser.getLastName().endsWith("%")) {
            String name = modelUser.getLastName().replace("%", "");
            match = user.getLastName().startsWith(name);
          } else {
            match = user.getLastName().equals(modelUser.getLastName());
          }
        }
      } else {
        match = true;
      }
      if (match && (!isDefined(modelUser.getDomainId()) || modelUser.getDomainId().equals(
              user.getDomainId()))) {
        foundUsers.add(user);
      }
    }
    return foundUsers.toArray(new UserDetail[foundUsers.size()]);
  }

  @Override
  public UserDetail[] searchUsers(SearchCriteria criteria) {
    UserDetail model = new UserDetail();
    model.setFirstName(criteria.getCriterionOnName());
    model.setLastName(criteria.getCriterionOnName());
    model.setDomainId(criteria.getCriterionOnDomainId());
    return searchUsers(criteria.getCriterionOnComponentInstanceId(),
            criteria.getCriterionOnRoleIds(), criteria.getCriterionOnGroupId(), model);
  }

  private UserDetail[] searchUsers(String componentId, String[] roleIds, String groupId,
          UserDetail userFilter) {
    UserDetail[] theUsers = searchUsers(userFilter, false);
    if (isDefined(groupId)) {
      List<UserDetail> foundUsers = new ArrayList<UserDetail>();
      Group group = groups.get(groupId);
      List<? extends UserDetail> usersOfGroup = group.getAllUsers();
      for (UserDetail userDetail : theUsers) {
        if (usersOfGroup.contains(userDetail)) {
          foundUsers.add(userDetail);
        }
      }
      return foundUsers.toArray(new UserDetail[foundUsers.size()]);
    } else {
      return theUsers;
    }
  }

  @Override
  public Group[] getGroups(String[] groupsId) {
    List<Group> theGroups = new ArrayList<Group>(groupsId.length);
    for (String groupId : groupsId) {
      theGroups.add(this.groups.get(groupId));
    }
    return theGroups.toArray(new Group[theGroups.size()]);
  }

  @Override
  public String[] searchGroupsIds(boolean isRootGroup, String componentId, String[] profileId,
          Group modelGroup) {
    List<String> groupIds = new ArrayList<String>();
    for (Group aGroup : groups.values()) {
      if (aGroup.isRoot() == isRootGroup) {
        boolean match = true;
        if (isDefined(modelGroup.getDomainId())) {
          match = modelGroup.getDomainId().equals(aGroup.getDomainId());
        }
        if (match && isDefined(modelGroup.getSuperGroupId())) {
          match = modelGroup.getSuperGroupId().equals(aGroup.getSuperGroupId());
        }
        if (match && isDefined(modelGroup.getName())) {
          if (modelGroup.getName().endsWith("%")) {
            String name = modelGroup.getName().replace("%", "");
            match = aGroup.getName().startsWith(name);
          } else {
            match = aGroup.getName().equals(modelGroup.getName());
          }
        }
        if (match) {
          groupIds.add(aGroup.getId());
        }
      }
    }
    return groupIds.toArray(new String[groupIds.size()]);
  }
}

/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package com.silverpeas.profile.web;

import com.silverpeas.rest.TestResources;
import com.silverpeas.rest.mock.OrganizationControllerMock;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * The resources to use in the test on the UserProfileResource REST service. Theses objects manage
 * in a single place all the resources required to perform correctly unit tests on the
 * UserProfileResource published operations.
 */
@Named(TestResources.TEST_RESOURCES_NAME)
public class UserProfileTestResources extends TestResources {

  public static final String JAVA_PACKAGE = "com.silverpeas.profile.web";
  public static final String SPRING_CONTEXT = "spring-profile-webservice.xml";
  public static final String USER_PROFILE_PATH = "/profile/users";
  public static final String GROUP_PROFILE_PATH = "/profile/groups";
  @Inject
  private OrganizationControllerMock organization;
  private Set<String> domainIds = new HashSet<String>();

  /**
   * Allocates the resources required by the unit tests.
   */
  public void allocate() {
    prepareSeveralUsers();
    prepareSeveralGroups();
    putUsersInGroups();
  }
  
  public void deallocate() {
    organization.clearAll();
  }

  public UserDetail[] getAllExistingUsers() {
    return organization.getAllUsers();
  }

  public UserDetail[] getAllExistingUsersInDomain(String domainId) {
    return organization.getAllUsersInDomain(domainId);
  }

  public Group[] getAllExistingRootGroups() {
    return organization.getAllRootGroups();
  }

  public Group[] getAllExistingRootGroupsInDomain(String domainId) {
    return organization.getAllRootGroupsInDomain(domainId);
  }
  
  public Group[] getAllRootGroupsAccessibleFromDomain(String domainId) {
    List<Group> groups = new ArrayList<Group>();
    groups.addAll(Arrays.asList(organization.getAllRootGroupsInDomain(domainId)));
    groups.addAll(Arrays.asList(organization.getAllRootGroupsInDomain("-1")));
    return groups.toArray(new Group[groups.size()]);
  }
  
  public Group getGroupById(String groupId) {
    return organization.getGroup(groupId);
  }

  public UserDetail getWebServiceCaller() {
    UserDetail caller = null;
    for (UserDetail userDetail : getAllExistingUsers()) {
      if (userDetail.getId().equals(USER_ID_IN_TEST)) {
        caller = userDetail;
        break;
      }
    }
    return caller;
  }

  public List<String> getAllDomainIds() {
    return new ArrayList<String>(domainIds);
  }

  public List<String> getAllDomainIdsExceptedSilverpeasOne() {
    List<String> otherDomainIds = new ArrayList<String>(domainIds.size() - 1);
    for (String aDomainId : domainIds) {
      if (!aDomainId.equals("0")) {
        otherDomainIds.add(aDomainId);
      }
    }
    return otherDomainIds;
  }
  
  /**
   * Gets randomly an existing user detail among the available resources for tests.
   * @return a user detail.
   */
  public UserDetail anExistingUser() {
    UserDetail[] allUsers = organization.getAllUsers();
    return allUsers[new Random().nextInt(allUsers.length)];
  }
  
  public UserDetail anExistingUserNotInSilverpeasDomain() {
    UserDetail user;
    do {
      user = anExistingUser();
    } while("0".equals(user.getDomainId()));
    return user;
  }
  
  /**
   * Gets randomly an existing group among the available resources for tests.
   * @return a group.
   */
  public Group anExistingGroup() {
    Group[] allGroups = organization.getAllGroups();
    return allGroups[new Random().nextInt(allGroups.length)];
  }
  
  /**
   * Gets a group that isn't in an internal domain.
   * @return a group in a domain other than internal one.
   */
  public Group getAGroupNotInAnInternalDomain() {
    Group group = anExistingGroup();
    while(group.getDomainId().equals("-1")) {
      group = anExistingGroup();
    }
    return group;
  }

  private void prepareSeveralUsers() {
    UserDetail[] users = new UserDetail[5];
    for (int i = 0; i < 5; i++) {
      String suffix = String.valueOf(10 + i);
      String domainId = (i == 0 ? "0" : (i < 3 ? "1" : "2"));
      domainIds.add(domainId);
      users[i] = aUser("Toto" + suffix, "Foo" + suffix, suffix, domainId);
    }
    addSomeUsers(users);
  }

  private void addSomeUsers(final UserDetail... users) {
    for (UserDetail userDetail : users) {
      organization.addUserDetail(userDetail);
    }
  }

  private UserDetail aUser(String firstName, String lastName, String id, String domainId) {
    UserDetail user = new UserDetail();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setId(id);
    user.setDomainId(domainId);
    return user;
  }

  private void prepareSeveralGroups() {
    addSomeGroups(aGroup("Groupe 1", "1", null, "-1"),
            aGroup("Groupe 2", "2", null, "-1"),
            aGroup("Groupe 3", "3", null, "0"),
            aGroup("Groupe 4 - 3", "4", "3", "0"),
            aGroup("Groupe 5 - 4", "5", "4", "0"),
            aGroup("Groupe 6", "6", null, "1"),
            aGroup("Groupe 7 - 6", "7", "6", "1"),
            aGroup("Groupe 8 - 6", "8", "6", "1"),
            aGroup("Groupe 9 - 7", "9", "7", "1"));
  }
  
  private void putUsersInGroups() {
    Group internalGroup = organization.getGroup("1");
    UserDetail[] users = organization.getAllUsers();
    internalGroup.setUserIds(getUserIds(users));
    
    for (int i = 0; i <= 1; i++) {
      String domainId = String.valueOf(i);
      users = organization.getAllUsersInDomain(domainId);
      if (users != null) {
        Group[] groups = organization.getAllRootGroupsInDomain(domainId);
        groups[0].setUserIds(getUserIds(users));
      }
    }
  }

  private void addSomeGroups(final Group... groups) {
    for (Group group : groups) {
      organization.addGroup(group);
    }
  }

  private Group aGroup(String name, String id, String fatherId, String domainId) {
    Group group = new Group();
    group.setName(name);
    group.setDescription("This is the group " + name);
    group.setSuperGroupId(fatherId);
    group.setId(id);
    group.setDomainId(domainId);
    return group;
  }
  
  private String[] getUserIds(final UserDetail ... users) {
    String[] ids = new String[users.length];
    for(int i = 0; i < users.length; i++) {
      ids[i] = users[i].getId();
    }
    return ids;
  }
}

/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.profile.web;

import com.silverpeas.profile.web.mock.RelationShipServiceMock;
import org.silverpeas.core.socialnetwork.relationShip.RelationShip;
import org.silverpeas.core.socialnetwork.relationShip.RelationShipService;
import com.silverpeas.web.TestResources;
import com.stratelia.webactiv.beans.admin.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.util.ListSlice;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

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
  private RelationShipServiceMock relationShipServiceMock;
  private Set<String> domainIds = new HashSet<String>();

  /**
   * Allocates the resources required by the unit tests.
   */
  @PostConstruct
  public void allocate() {
    prepareSeveralUsers();
    prepareSeveralGroups();
    putUsersInGroups();
  }

  public UserDetail[] getAllExistingUsers() {
    UserDetail[] users = getOrganizationControllerMock().getAllUsers();
    if (users == null) {
      users = new UserDetail[0];
    }
    return users;
  }

  public void whenSearchGroupsByCriteriaThenReturn(final Group[] groups) {
    OrganizationController mock = getOrganizationControllerMock();
    doAnswer(new Answer<ListSlice<Group>>() {
      @Override
      public ListSlice<Group> answer(InvocationOnMock invocation) throws Throwable {
        return new ListSlice<Group>(Arrays.asList(groups));
      }
    }).when(mock).searchGroups(any(GroupsSearchCriteria.class));
  }

  public void whenSearchUsersByCriteriaThenReturn(final UserDetailsSearchCriteria criteria,
          final UserDetail[] users) {
    OrganizationController mock = getOrganizationControllerMock();
    doAnswer(new Answer<ListSlice<UserDetail>>() {

      @Override
      public ListSlice<UserDetail> answer(InvocationOnMock invocation) throws Throwable {
        ListSlice<UserDetail> emptyUsers = new ListSlice<UserDetail>(0, 0, 0);
        UserDetailsSearchCriteria passedCriteria = (UserDetailsSearchCriteria) invocation.getArguments()[0];
        if (criteria.isCriterionOnComponentInstanceIdSet() && passedCriteria.
                isCriterionOnComponentInstanceIdSet()) {
          if (!criteria.getCriterionOnComponentInstanceId().equals(passedCriteria.
                  getCriterionOnComponentInstanceId())) {
            return emptyUsers;
          }
        } else if ((!criteria.isCriterionOnComponentInstanceIdSet() && passedCriteria.
                isCriterionOnComponentInstanceIdSet()) || (criteria.
                isCriterionOnComponentInstanceIdSet() && !passedCriteria.
                isCriterionOnComponentInstanceIdSet())) {
          return emptyUsers;
        }
        if (criteria.isCriterionOnDomainIdSet() && passedCriteria.isCriterionOnDomainIdSet()) {
          if (!criteria.getCriterionOnDomainId().equals(passedCriteria.getCriterionOnDomainId())) {
            return emptyUsers;
          }
        } else if ((!criteria.isCriterionOnDomainIdSet()
                && passedCriteria.isCriterionOnDomainIdSet()) || (criteria.isCriterionOnDomainIdSet()
                && !passedCriteria.isCriterionOnDomainIdSet())) {
          return emptyUsers;
        }
        if (criteria.isCriterionOnGroupIdsSet() && passedCriteria.isCriterionOnGroupIdsSet()) {
          if (!Arrays.equals(criteria.getCriterionOnGroupIds(), passedCriteria.getCriterionOnGroupIds())) {
            return emptyUsers;
          }
        } else if ((!criteria.isCriterionOnGroupIdsSet()
                && passedCriteria.isCriterionOnGroupIdsSet()) || (criteria.isCriterionOnGroupIdsSet()
                && !passedCriteria.isCriterionOnGroupIdsSet())) {
          return emptyUsers;
        }
        if (criteria.isCriterionOnAccessLevelsSet() &&
            passedCriteria.isCriterionOnAccessLevelsSet()) {
          if (!Arrays.equals(criteria.getCriterionOnAccessLevels(),
              passedCriteria.getCriterionOnAccessLevels())) {
            return emptyUsers;
          }
        } else if ((!criteria.isCriterionOnAccessLevelsSet() &&
            passedCriteria.isCriterionOnAccessLevelsSet()) ||
            (criteria.isCriterionOnAccessLevelsSet() &&
                !passedCriteria.isCriterionOnAccessLevelsSet())) {
          return emptyUsers;
        }
        if (criteria.isCriterionOnNameSet() && passedCriteria.isCriterionOnNameSet()) {
          if (!criteria.getCriterionOnName().equals(passedCriteria.getCriterionOnName())) {
            return emptyUsers;
          }
        } else if ((!criteria.isCriterionOnNameSet()
                && passedCriteria.isCriterionOnNameSet()) || (criteria.isCriterionOnNameSet()
                && !passedCriteria.isCriterionOnNameSet())) {
          return emptyUsers;
        }
        if (criteria.isCriterionOnRoleNamesSet() && passedCriteria.isCriterionOnRoleNamesSet()) {
          List<String> roles = Arrays.asList(criteria.getCriterionOnRoleNames());
          List<String> passedRoles = Arrays.asList(passedCriteria.getCriterionOnRoleNames());
          if (roles.size() != passedRoles.size() || !roles.containsAll(passedRoles)) {
            return emptyUsers;
          }
        } else if ((!criteria.isCriterionOnRoleNamesSet()
                && passedCriteria.isCriterionOnRoleNamesSet()) || (criteria.isCriterionOnRoleNamesSet()
                && !passedCriteria.isCriterionOnRoleNamesSet())) {
          return emptyUsers;
        }
        if (criteria.isCriterionOnUserIdsSet() && passedCriteria.isCriterionOnUserIdsSet()) {
          List<String> userIds = Arrays.asList(criteria.getCriterionOnUserIds());
          List<String> passedUserIds = Arrays.asList(passedCriteria.getCriterionOnUserIds());
          if (userIds.size() != passedUserIds.size() || !userIds.containsAll(passedUserIds)) {
            return emptyUsers;
          }
        } else if ((!criteria.isCriterionOnUserIdsSet()
                && passedCriteria.isCriterionOnUserIdsSet()) || (criteria.isCriterionOnUserIdsSet()
                && !passedCriteria.isCriterionOnUserIdsSet())) {
          return emptyUsers;
        }

        return new ListSlice<UserDetail>(Arrays.asList(users));
      }
    }).when(mock).searchUsers(any(UserDetailsSearchCriteria.class));
  }

  public UserDetail[] getAllExistingUsersInDomain(String domainId) {
    List<UserDetail> usersInDomain = new ArrayList<UserDetail>();
    UserDetail[] existingUsers = getAllExistingUsers();
    for (UserDetail aUser : existingUsers) {
      if (aUser.getDomainId().equals(domainId)) {
        usersInDomain.add(aUser);
      }
    }
    return usersInDomain.toArray(new UserDetail[usersInDomain.size()]);
  }

  public Group[] getAllExistingRootGroups() {
    Group[] groups = getOrganizationControllerMock().getAllRootGroups();
    if (groups == null) {
      groups = new Group[0];
    }
    return groups;
  }

  public Group[] getAllExistingRootGroupsInDomain(String domainId) {
    List<Group> rootGroupsInDomain = new ArrayList<Group>();
    Group[] existingGroups = getAllExistingRootGroups();
    for (Group aGroup : existingGroups) {
      if (aGroup.getDomainId().equals(domainId)) {
        rootGroupsInDomain.add(aGroup);
      }
    }
    return rootGroupsInDomain.toArray(new Group[rootGroupsInDomain.size()]);
  }

  public Group[] getAllRootGroupsAccessibleFromDomain(String domainId) {
    List<Group> groups = new ArrayList<Group>();
    groups.addAll(Arrays.asList(getAllExistingRootGroupsInDomain(domainId)));
    groups.addAll(Arrays.asList(getAllExistingRootGroupsInDomain("-1")));
    return groups.toArray(new Group[groups.size()]);
  }

  public Group getGroupById(String groupId) {
    return getOrganizationControllerMock().getGroup(groupId);
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
   *
   * @return a user detail.
   */
  public UserDetail anExistingUser() {
    UserDetail[] allUsers = getOrganizationControllerMock().getAllUsers();
    return allUsers[new Random().nextInt(allUsers.length)];
  }

  public UserDetail anExistingUserNotInSilverpeasDomain() {
    UserDetail user;
    do {
      user = anExistingUser();
    } while ("0".equals(user.getDomainId()));
    return user;
  }

  /**
   * Gets randomly an existing group among the available resources for tests.
   *
   * @return a group.
   */
  public Group anExistingGroup() {
    Group[] allGroups = getOrganizationControllerMock().getAllGroups();
    return allGroups[new Random().nextInt(allGroups.length)];
  }

  public Group anExistingRootGroup() {
    Group group = null;
    do {
      group = anExistingGroup();
    } while (!group.isRoot());
    return group;
  }

  /**
   * Gets a group that isn't in an internal domain.
   *
   * @return a group in a domain other than internal one.
   */
  public Group getAGroupNotInAnInternalDomain() {
    Group group = anExistingGroup();
    while (group.getDomainId().equals("-1")) {
      group = anExistingGroup();
    }
    return group;
  }

  public UserDetail[] getRelationShipsOfUser(String userId) {
    UserDetail[] users = getAllExistingUsers();
    UserDetail[] contacts = new UserDetail[users.length - 1];
    List<RelationShip> relationships = new ArrayList<RelationShip>(users.length - 1);
    int currentUserId = Integer.valueOf(userId);
    int i = 0;
    for (UserDetail aUser : users) {
      if (!aUser.getId().equals(userId)) {
        contacts[i++] = aUser;
        relationships.add(new RelationShip(Integer.valueOf(aUser.getId()), currentUserId, 1,
                new Date(), currentUserId));
      }
    }
    RelationShipService mock = relationShipServiceMock.getMockedRelationShipService();
    try {
      when(mock.getAllMyRelationShips(currentUserId)).thenReturn(relationships);
    } catch (SQLException ex) {
      Logger.getLogger(UserProfileTestResources.class.getName()).log(Level.SEVERE, null, ex);
    }
    return contacts;
  }

  @Override
  public UserDetail registerUser(UserDetail user) {
    UserDetail[] existingUsers = getAllExistingUsers();
    UserDetail[] actualUsers = Arrays.copyOf(existingUsers, existingUsers.length + 1);
    actualUsers[actualUsers.length - 1] = user;
    OrganizationController mock = getOrganizationControllerMock();
    when(mock.getAllUsers()).thenReturn(actualUsers);
    return super.registerUser(user);
  }

  private void prepareSeveralUsers() {
    for (int i = 0; i < 5; i++) {
      String suffix = String.valueOf(10 + i);
      String domainId = (i == 0 ? "0" : (i < 3 ? "1" : "2"));
      domainIds.add(domainId);
      registerUser(aUser("Toto" + suffix, "Foo" + suffix, suffix, domainId));
    }
  }

  private UserDetail aUser(String firstName, String lastName, String id, String domainId) {
    UserDetail user = new UserDetail();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setId(id);
    user.setDomainId(domainId);
    user.setAccessLevel(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    return user;
  }

  private void prepareSeveralGroups() {
    registerSomeGroups(aGroup("Groupe 1", "1", null, "-1"),
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
    OrganizationController mock = getOrganizationControllerMock();
    Group[] groups = mock.getAllGroups();
    for (Group group : groups) {
      when(mock.getAllUsersOfGroup(group.getId())).thenReturn(new UserDetail[0]);
      //group.setTotalNbUsers(1);
    }
    Group internalGroup = mock.getGroup("1");
    UserDetail[] users = getAllExistingUsers();
    internalGroup.setUserIds(getUserIds(users));
    //internalGroup.setTotalNbUsers(users.length);

    for (int i = 0; i <= 1; i++) {
      String domainId = String.valueOf(i);
      users = getAllExistingUsersInDomain(domainId);
      if (users != null) {
        groups = getAllExistingRootGroupsInDomain(domainId);
        groups[0].setUserIds(getUserIds(users));
        when(mock.getAllUsersOfGroup(groups[0].getId())).thenReturn(users);
        for (int j = 1; j < groups.length; j++) {
          when(mock.getAllUsersOfGroup(groups[j].getId())).thenReturn(new UserDetail[0]);
        }
      }
    }
  }

  private void registerSomeGroups(final Group... someGroups) {
    OrganizationController mock = getOrganizationControllerMock();
    List<Group> rootGroups = new ArrayList<Group>();
    for (int i = 0; i < someGroups.length; i++) {
      when(mock.getGroup(someGroups[i].getId())).thenReturn(someGroups[i]);
      if (someGroups[i].isRoot()) {
        rootGroups.add(someGroups[i]);
      }
      Domain domain = mock.getDomain(someGroups[i].getDomainId());
      if (domain == null) {
        domain = new Domain();
        domain.setId(someGroups[i].getDomainId());
        domain.setName("Domaine " + someGroups[i].getDomainId());
        when(mock.getDomain(someGroups[i].getDomainId())).thenReturn(domain);
      }
    }
    when(mock.getAllRootGroups()).thenReturn(rootGroups.toArray(new Group[rootGroups.size()]));
    when(mock.getAllGroups()).thenReturn(someGroups);

    for (Group group : someGroups) {
      if (!group.isRoot()) {
        Group[] subgroups = mock.getAllSubGroups(group.getSuperGroupId());
        if (subgroups == null) {
          subgroups = new Group[1];
        } else {
          subgroups = Arrays.copyOf(subgroups, subgroups.length + 1);
        }
        subgroups[subgroups.length - 1] = group;
        when(mock.getAllSubGroups(group.getSuperGroupId())).thenReturn(subgroups);
      }
      Group[] subgroups = mock.getAllSubGroups(group.getId());
      if (subgroups == null) {
        when(mock.getAllSubGroups(group.getId())).thenReturn(new Group[0]);
      }
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

  public String[] getUserIds(final UserDetail... users) {
    String[] ids = new String[users.length];
    for (int i = 0; i < users.length; i++) {
      ids[i] = users[i].getId();
    }
    return ids;
  }

  public String[] getGroupIds(final Group... groups) {
    String[] ids = new String[groups.length];
    for (int i = 0; i < groups.length; i++) {
      ids[i] = groups[i].getId();
    }
    return ids;
  }
}

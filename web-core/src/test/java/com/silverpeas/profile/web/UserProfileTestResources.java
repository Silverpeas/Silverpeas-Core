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
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.aop.SpringProxy;
import org.springframework.context.ApplicationContext;

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
  
  @Inject
  private OrganizationControllerMock organization;
  private Set<String> domainIds = new HashSet<String>();
  
  /**
   * Allocates the resources required by the unit tests.
   */
  public void allocate() {
    prepareSeveralUsers();
  }
  
  public UserDetail[] getAllExistingUsers() {
    return organization.getAllUsers();
  }
  
  public UserDetail[] getAllExistingUsersInDomain(String domainId) {
    List<UserDetail> theUsers = new ArrayList<UserDetail>();
    for (UserDetail userDetail : getAllExistingUsers()) {
      if (userDetail.getDomainId().equals(domainId)) {
        theUsers.add(userDetail);
      }
    }
    return theUsers.toArray(new UserDetail[theUsers.size()]);
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
  
  private void prepareSeveralUsers() {
    UserDetail[] users = new UserDetail[5];
    for(int i = 0; i < 5; i++) {
      String suffix = String.valueOf(10 + i);
      String domainId = String.valueOf((i + 1) % 2) + 1;
      domainIds.add(domainId);
      users[i] = aUser("Toto" + suffix, "Foo" + suffix, suffix, domainId);
    }
    addSomeUsers(users);
  }
  
  private void addSomeUsers(final UserDetail ... users) {
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
}

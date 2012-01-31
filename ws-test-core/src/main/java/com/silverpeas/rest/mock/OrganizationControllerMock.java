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
package com.silverpeas.rest.mock;

import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.*;
import javax.inject.Named;
import static com.silverpeas.util.StringUtil.isDefined;
import com.stratelia.webactiv.beans.admin.Domain;

/**
 * A mock the OrganizationController objects for testing purpose.
 */
@Named("organizationController")
public class OrganizationControllerMock extends OrganizationController {

  private static final long serialVersionUID = -3271734262141821655L;
  private Map<String, UserDetail> users = new HashMap<String, UserDetail>();
  private Set<String> components = new HashSet<String>();

  @Override
  public UserDetail getUserDetail(String sUserId) {
    return users.get(sUserId);
  }

  @Override
  public UserDetail[] getAllUsers() {
    UserDetail[] allUsers = new UserDetail[users.size()];
    int i = 0;
    for (UserDetail userDetail : users.values()) {
      allUsers[i++] = userDetail;
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
   * Adds a new user for tests.
   *
   * @param userDetail the detail about the user to add for tests.
   */
  public void addUserDetail(final UserDetail userDetail) {
    users.put(userDetail.getId(), userDetail);
  }

  /**
   * Clears all of the data used in tests.
   */
  public void clearAll() {
    users.clear();
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
}

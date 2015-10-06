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

import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserDetailsSearchCriteria;
import com.stratelia.webactiv.util.GeneralPropertiesManagerHelper;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;

import static com.silverpeas.profile.web.UserProfileTestResources.*;
import static com.silverpeas.profile.web.matchers.UsersMatcher.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit tests on the operations published by the UserProfileResource REST service.
 */
public class UserProfileResourceTest extends ResourceGettingTest<UserProfileTestResources> {

  private String sessionKey;
  private UserDetail currentUser;

  public UserProfileResourceTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    currentUser = aUser();
    sessionKey = authenticate(currentUser);
  }

  /**
   * With no domain isolation, the users can see others ones whatever their user domain.
   */
  @Test
  public void gettingAllUsersWhateverTheDomainWithNoDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    UserDetail[] expectedUsers = getTestResources().getAllExistingUsers();
    getTestResources().whenSearchUsersByCriteriaThenReturn(new UserDetailsSearchCriteria(), expectedUsers);

    UserProfileEntity[] actualUsers = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.length));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * Whatever the domain isolation applied, the users can see the others ones in their own user
   * domain. In this test, we validate this is the case with no domain isolation.
   */
  @Test
  public void getAllUsersInItsOwnsDomainWithNoDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    String domainId = getTestResources().getAllDomainIdsExceptedSilverpeasOne().get((0));
    currentUser.setDomainId(domainId);
    UserDetail[] expectedUsers = getTestResources().getAllExistingUsersInDomain(domainId);
    getTestResources().whenSearchUsersByCriteriaThenReturn(new UserDetailsSearchCriteria(),
            expectedUsers);

    UserProfileEntity[] actualUsers = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.length));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * Whatever the domain isolation applied, the users can see the others ones in their own user
   * domain. In this test, we validate this is the case with a semi domain isolation.
   */
  @Test
  public void getAllUsersInItsOwnsDomainWithSemiDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
    String domainId = getTestResources().getAllDomainIdsExceptedSilverpeasOne().get((0));
    currentUser.setDomainId(domainId);
    UserDetail[] expectedUsers = getTestResources().getAllExistingUsersInDomain(domainId);
    getTestResources().whenSearchUsersByCriteriaThenReturn(new UserDetailsSearchCriteria().onDomainId(domainId),
            expectedUsers);

    UserProfileEntity[] actualUsers = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.length));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * Whatever the domain isolation applied, the users can see the others ones in their own user
   * domain. In this test, we validate this is the case with a full domain isolation.
   */
  @Test
  public void getAllUsersInItsOwnsDomainWithFullDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_EACH);
    String domainId = getTestResources().getAllDomainIdsExceptedSilverpeasOne().get((0));
    currentUser.setDomainId(domainId);
    UserDetail[] expectedUsers = getTestResources().getAllExistingUsersInDomain(domainId);
    getTestResources().whenSearchUsersByCriteriaThenReturn(new UserDetailsSearchCriteria().onDomainId(domainId),
            expectedUsers);

    UserProfileEntity[] actualUsers = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.length));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * With a semi domain isolation, the users in the Silverpeas domain can see the users whatever
   * their user domain.
   */
  @Test
  public void getAllUsersWhateverTheDomainWhenInSilverpeasDomainAndWithSemiDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
    UserDetail[] expectedUsers = getTestResources().getAllExistingUsers();
    getTestResources().whenSearchUsersByCriteriaThenReturn(new UserDetailsSearchCriteria(), expectedUsers);

    UserProfileEntity[] actualUsers = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.length));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * With a full domain isolation, the users in the Silverpeas domain cannot see the users of others
   * user domains.
   */
  @Test
  public void getAllUnaccessibleUsersWhateverTheDomainWhenInSilverpeasDomainAndWithFullDomainIsolation() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
      UserDetail[] expectedUsers = getTestResources().getAllExistingUsers();
      getTestResources().whenSearchUsersByCriteriaThenReturn(new UserDetailsSearchCriteria(), expectedUsers);
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  /**
   * With no domain isolation, a user can see another one whatever its user domain.
   */
  @Test
  public void getAGivenUserWhateverItsDomainWithNoDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
    UserProfileEntity actualUser = getAt(aResourceURI() + "/" + expectedUser.getId(),
            UserProfileEntity.class);
    assertThat(actualUser, notNullValue());
    assertThat(actualUser.getId(), is(expectedUser.getId()));
  }

  /**
   * Whatever the domain isolation applied, a users can see another one in its own user domain. In
   * this test, we validate this is the case with no domain isolation.
   */
  @Test
  public void getAGivenUserInItsOwnsDomainWithNoDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
    currentUser.setDomainId(expectedUser.getDomainId());
    UserProfileEntity actualUser = getAt(aResourceURI() + "/" + expectedUser.getId(),
            UserProfileEntity.class);
    assertThat(actualUser, notNullValue());
    assertThat(actualUser.getId(), is(expectedUser.getId()));
  }

  /**
   * Whatever the domain isolation applied, a users can see another one in its own user domain. In
   * this test, we validate this is the case with a semi domain isolation.
   */
  @Test
  public void getAGivenUserInItsOwnsDomainWithSemiDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
    UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
    currentUser.setDomainId(expectedUser.getDomainId());
    UserProfileEntity actualUser = getAt(aResourceURI() + "/" + expectedUser.getId(),
            UserProfileEntity.class);
    assertThat(actualUser, notNullValue());
    assertThat(actualUser.getId(), is(expectedUser.getId()));
  }

  /**
   * Whatever the domain isolation applied, a users can see another one in its own user domain. In
   * this test, we validate this is the case with full domain isolation.
   */
  @Test
  public void getAGivenUserInItsOwnsDomainWithFullDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
    currentUser.setDomainId(expectedUser.getDomainId());
    UserProfileEntity actualUser = getAt(aResourceURI() + "/" + expectedUser.getId(),
            UserProfileEntity.class);
    assertThat(actualUser, notNullValue());
    assertThat(actualUser.getId(), is(expectedUser.getId()));
  }

  /**
   * With a semi domain isolation, only a user in the silverpeas domain can see another one of
   * another domain.
   */
  @Test
  public void getAUserWhateverTheDomainWhenInSilverpeasDomainAndWhenInSemiIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
    UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
    UserProfileEntity actualUser = getAt(aResourceURI() + "/" + expectedUser.getId(),
            UserProfileEntity.class);
    assertThat(actualUser, notNullValue());
    assertThat(actualUser.getId(), is(expectedUser.getId()));
  }

  /**
   * With a full domain isolation, a user in the silverpeas domain cannot see another one of another
   * domain.
   */
  @Test
  public void getAnUnaccessibleUserWhateverTheDomainWhenInSilverpeasDomainAndWhenInFullIsolation() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_EACH);
      UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
      getAt(aResourceURI() + "/" + expectedUser.getId(),
              UserProfileEntity.class);
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  /**
   * With a semi domain isolation, a user whose the domain isn't the Silverpeas one cannot see a
   * user of another domain.
   */
  @Test
  public void getAnUnaccessibleUserInAnotherDomainWithSemiDomainIsolation() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
      UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
      currentUser.setDomainId(expectedUser.getDomainId() + "0");
      getAt(aResourceURI() + "/" + expectedUser.getId(), UserProfileEntity.class);
      fail("The user shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  /**
   * With a full domain isolation, a user whose the domain isn't the Silverpeas one cannot see a
   * user of another domain.
   */
  @Test
  public void getAnUnaccessibleUserInAnotherDomainWithFullDomainIsolation() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_EACH);
      UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
      currentUser.setDomainId(expectedUser.getDomainId() + "0");
      getAt(aResourceURI() + "/" + expectedUser.getId(), UserProfileEntity.class);
      fail("The user shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  /**
   * With no domain isolation, a user can see all users of a given group whatever their user domain.
   */
  @Test
  public void gettingAllUsersOfAGivenGroupWhateverTheDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
    List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
    UserDetailsSearchCriteria criteria = new UserDetailsSearchCriteria().onGroupIds(aGroup.getId());
    if (Integer.valueOf(aGroup.getDomainId()) >= 0) {
      criteria.onDomainId(aGroup.getDomainId());
    }
    getTestResources().whenSearchUsersByCriteriaThenReturn(criteria,
            expectedUsers.toArray(new UserDetail[expectedUsers.size()]));

    UserProfileEntity[] actualUsers = getAt(aResourceURI() + "?group=" + aGroup.getId(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.size()));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * Whavers the domain isolation applied, a user can see all the users of a group when they are in
   * its own domain. This test validates this rule with no user domain isolation.
   */
  @Test
  public void getAllUsersOfAGivenGroupInItsOwnsDomainWithNoIsolationDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
    currentUser.setDomainId(aGroup.getDomainId());
    List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
    getTestResources().whenSearchUsersByCriteriaThenReturn(
            new UserDetailsSearchCriteria().onGroupIds(aGroup.getId()).onDomainId(aGroup.getDomainId()),
            expectedUsers.toArray(new UserDetail[expectedUsers.size()]));

    UserProfileEntity[] actualUsers = getAt(aResourceURI() + "?group=" + aGroup.getId(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.size()));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * Whavers the domain isolation applied, a user can see all the users of a group when they are in
   * its own domain. This test validates this rule with a semi user domain isolation.
   */
  @Test
  public void getAllUsersOfAGivenGroupInItsOwnsDomainWithSemiDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
    Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
    currentUser.setDomainId(aGroup.getDomainId());
    List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
    getTestResources().whenSearchUsersByCriteriaThenReturn(
            new UserDetailsSearchCriteria().onGroupIds(aGroup.getId()).onDomainId(aGroup.getDomainId()),
            expectedUsers.toArray(new UserDetail[expectedUsers.size()]));

    UserProfileEntity[] actualUsers = getAt(aResourceURI() + "?group=" + aGroup.getId(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.size()));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * Whavers the domain isolation applied, a user can see all the users of a group when they are in
   * its own domain. This test validates this rule with a full user domain isolation.
   */
  @Test
  public void getAllUsersOfAGivenGroupInItsOwnsDomainWithFullDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_EACH);
    Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
    currentUser.setDomainId(aGroup.getDomainId());
    List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
    getTestResources().whenSearchUsersByCriteriaThenReturn(
            new UserDetailsSearchCriteria().onGroupIds(aGroup.getId()).onDomainId(aGroup.getDomainId()),
            expectedUsers.toArray(new UserDetail[expectedUsers.size()]));

    UserProfileEntity[] actualUsers = getAt(aResourceURI() + "?group=" + aGroup.getId(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.size()));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * With a semi domain isolation, a user, whose the domain is the Silverpeas one, can see all the
   * users of a group of another domain.
   */
  @Test
  public void getAllUsersOfAGivenGroupWhateverTheDomainWhenInSilverpeasDomainAndWithSemiDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
    Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
    List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
    UserDetailsSearchCriteria criteria = new UserDetailsSearchCriteria().onGroupIds(aGroup.getId());
    if (Integer.valueOf(aGroup.getDomainId()) >= 0) {
      criteria.onDomainId(aGroup.getDomainId());
    }
    getTestResources().whenSearchUsersByCriteriaThenReturn(criteria,
            expectedUsers.toArray(new UserDetail[expectedUsers.size()]));

    UserProfileEntity[] actualUsers = getAt(aResourceURI() + "?group=" + aGroup.getId(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.size()));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * With a full domain isolation, a user, whose the domain is the Silverpeas one, cannot see all
   * the users of a group of another domain.
   */
  @Test
  public void getAllUsersOfAnUnaccessibleGroupWhenInSilverpeasDomainAndWithFullDomainIsolation() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_EACH);
      Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
      List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
      UserDetailsSearchCriteria criteria = new UserDetailsSearchCriteria().onGroupIds(aGroup.getId());
      if (Integer.valueOf(aGroup.getDomainId()) >= 0) {
        criteria.onDomainId(aGroup.getDomainId());
      }
      getTestResources().whenSearchUsersByCriteriaThenReturn(criteria,
              expectedUsers.toArray(new UserDetail[expectedUsers.size()]));
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  /**
   * With a semi isolation level, a user cannot see all the users of a group when not in its own
   * user domain.
   */
  @Test
  public void getAllUsersOfAnUnaccessibleGivenGroupWithSemiDomainIsolation() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
      Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
      currentUser.setDomainId(aGroup.getDomainId() + "0");
      List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
      getAt(aResourceURI() + "?group=" + aGroup.getId(), getWebEntityClass());
      fail("The user shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  /**
   * With a full isolation level, a user cannot see all the users of a group when not in its own
   * user domain.
   */
  @Test
  public void getAllUsersOfAnUnaccessibleGivenGroupWithFullDomainIsolation() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_EACH);
      Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
      currentUser.setDomainId(aGroup.getDomainId() + "0");
      List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
      getAt(aResourceURI() + "?group=" + aGroup.getId(), getWebEntityClass());
      fail("The user shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  /**
   * A user can see another one by its first name.
   */
  @Test
  public void getAUserByItsFirstName() {
    UserDetail expectedUser = getTestResources().anExistingUser();
    getTestResources().whenSearchUsersByCriteriaThenReturn(
            new UserDetailsSearchCriteria().onName(expectedUser.getFirstName()),
            new UserDetail[]{expectedUser});

    UserDetail[] actualUsers = getAt(aResourceURI() + "?name=" + expectedUser.getFirstName(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(1));
    assertThat(actualUsers[0], is(expectedUser));
  }

  /**
   * A user can see another one by its last name.
   */
  @Test
  public void getAUserByItsLastName() {
    UserDetail expectedUser = getTestResources().anExistingUser();
    getTestResources().whenSearchUsersByCriteriaThenReturn(
            new UserDetailsSearchCriteria().onName(expectedUser.getLastName()),
            new UserDetail[]{expectedUser});

    UserProfileEntity[] actualUsers = getAt(aResourceURI() + "?name=" + expectedUser.getLastName(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(1));
    assertThat(actualUsers[0], is(expectedUser));
  }

  /**
   * A user can see another ones by a pattern on their name.
   */
  @Test
  public void getAUserByTheFirstCharactersOfItsName() {
    UserDetail[] expectedUsers = getTestResources().getAllExistingUsers();
    String name = expectedUsers[0].getFirstName().substring(0, 2) + "*";
    getTestResources().whenSearchUsersByCriteriaThenReturn(
            new UserDetailsSearchCriteria().onName(name.replaceAll("\\*", "%")),
            expectedUsers);

    UserProfileEntity[] actualUsers = getAt(aResourceURI() + "?name=" + name,
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.length));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * A user can see another ones in a given group by a pattern on their name.
   */
  @Test
  public void getAUserInAGivenGroupByTheFirstCharactersOfItsName() {
    Group group;
    List<? extends UserDetail> expectedUsers;
    do {
      group = getTestResources().getAGroupNotInAnInternalDomain();
      expectedUsers = group.getAllUsers();
    } while (expectedUsers.isEmpty());
    String name = expectedUsers.get(0).getFirstName().substring(0, 2) + "*";
    UserDetailsSearchCriteria criteria = new UserDetailsSearchCriteria().onGroupIds(group.getId()).onName(name.replaceAll(
            "\\*", "%"));
    if (Integer.valueOf(group.getDomainId()) >= 0) {
      criteria.onDomainId(group.getDomainId());
    }
    getTestResources().whenSearchUsersByCriteriaThenReturn(criteria,
            expectedUsers.toArray(new UserDetail[expectedUsers.size()]));

    UserProfileEntity[] actualUsers = getAt(aResourceURI() + "?group=" + group.getId() + "&name="
            + name,
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.size()));
    assertThat(actualUsers, contains(expectedUsers));
  }

  /**
   * A user can see its own profile without knowing its identifier.
   */
  @Test
  public void getTheCurrentUserInTheSession() {
    UserProfileEntity me = getAt(aResourceURI() + "/me", UserProfileEntity.class);
    assertThat(me, is(currentUser));
  }

  /**
   * A user can see the relationships of another one.
   */
  @Test
  public void getTheContactsOfAGivenUser() {
    UserDetail aUser = getTestResources().anExistingUser();
    UserDetail[] expectedContacts = getTestResources().getRelationShipsOfUser(aUser.getId());
    getTestResources().whenSearchUsersByCriteriaThenReturn(
            new UserDetailsSearchCriteria().onUserIds(getTestResources().getUserIds(expectedContacts)),
            expectedContacts);

    UserProfileEntity[] actualContacts = getAt(aResourceURI() + "/" + aUser.getId() + "/contacts",
            getWebEntityClass());
    assertThat(actualContacts.length, is(expectedContacts.length));
  }

  @Override
  @Ignore
  public void gettingAResourceByAnUnauthorizedUser() {
  }

  @Override
  @Ignore
  public void gettingAnUnexistingResource() {
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{};
  }

  @Override
  public String aResourceURI() {
    return USER_PROFILE_PATH;
  }

  @Override
  public String anUnexistingResourceURI() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public <T> T aResource() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<UserProfileEntity[]> getWebEntityClass() {
    return UserProfileEntity[].class;
  }
}

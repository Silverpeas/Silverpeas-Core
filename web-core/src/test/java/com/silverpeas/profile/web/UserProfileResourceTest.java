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

import static com.silverpeas.profile.web.UserProfileTestResources.*;
import static com.silverpeas.profile.web.matchers.UsersMatcher.contains;
import com.silverpeas.rest.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.GeneralPropertiesManagerHelper;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.List;
import javax.ws.rs.core.Response;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests on the operations published by the UserProfileResource REST service.
 */
public class UserProfileResourceTest extends ResourceGettingTest<UserProfileTestResources> {

  private String sessionKey;

  public UserProfileResourceTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ALL);
    sessionKey = authenticate(aUser());
    getTestResources().allocate();
  }

  @Test
  public void gettingAllUsersWhateverTheDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ALL);
    UserDetail[] expectedUsers = getTestResources().getAllExistingUsers();
    SelectableUser[] actualUsers = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.length));
    assertThat(actualUsers, contains(expectedUsers));
  }

  @Test
  public void getAllUsersInItsOwnsDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ONE);
    String domainId = getTestResources().getAllDomainIdsExceptedSilverpeasOne().get((0));
    getTestResources().getWebServiceCaller().setDomainId(domainId);
    UserDetail[] expectedUsers = getTestResources().getAllExistingUsersInDomain(domainId);
    SelectableUser[] actualUsers = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.length));
    assertThat(actualUsers, contains(expectedUsers));
  }

  @Test
  public void getAllUsersWhateverTheDomainWhenInSilverpeasDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_EACH);
    UserDetail[] expectedUsers = getTestResources().getAllExistingUsers();

    SelectableUser[] actualUsers = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.length));
    assertThat(actualUsers, contains(expectedUsers));
  }

  @Test
  public void getAllUsersInItsOwnDomainWhenNotInSilverpeasDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_EACH);
    String domainId = getTestResources().getAllDomainIdsExceptedSilverpeasOne().get((1));
    getTestResources().getWebServiceCaller().setDomainId(domainId);
    UserDetail[] expectedUsers = getTestResources().getAllExistingUsersInDomain(domainId);

    SelectableUser[] actualUsers = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.length));
    assertThat(actualUsers, contains(expectedUsers));
  }

  @Test
  public void getAGivenUserWhateverItsDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ALL);
    UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
    SelectableUser actualUser = getAt(aResourceURI() + "/" + expectedUser.getId(),
            SelectableUser.class);
    assertThat(actualUser, notNullValue());
    assertThat(actualUser.getId(), is(expectedUser.getId()));
  }

  @Test
  public void getAGivenUserInItsOwnsDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ONE);
    UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
    getTestResources().getWebServiceCaller().setDomainId(expectedUser.getDomainId());
    SelectableUser actualUser = getAt(aResourceURI() + "/" + expectedUser.getId(),
            SelectableUser.class);
    assertThat(actualUser, notNullValue());
    assertThat(actualUser.getId(), is(expectedUser.getId()));
  }

  @Test
  public void getAUserWhateverTheDomainWhenInSilverpeasDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_EACH);
    UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
    SelectableUser actualUser = getAt(aResourceURI() + "/" + expectedUser.getId(),
            SelectableUser.class);
    assertThat(actualUser, notNullValue());
    assertThat(actualUser.getId(), is(expectedUser.getId()));
  }

  @Test
  public void getAGivenUserInItsOwnDomainWhenNotInSilverpeasDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_EACH);
    UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
    getTestResources().getWebServiceCaller().setDomainId(expectedUser.getDomainId());
    SelectableUser actualUser = getAt(aResourceURI() + "/" + expectedUser.getId(),
            SelectableUser.class);
    assertThat(actualUser, notNullValue());
    assertThat(actualUser.getId(), is(expectedUser.getId()));
  }

  @Test
  public void getAnUnaccessibleUserInAnotherDomain() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ONE);
      UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
      getTestResources().getWebServiceCaller().setDomainId(expectedUser.getDomainId() + "0");
      getAt(aResourceURI() + "/" + expectedUser.getId(), SelectableUser.class);
      fail("The user shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void getAnUnaccessibleUserInAnotherDomainWhenOtherThanSilverpeasOne() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_EACH);
      UserDetail expectedUser = getTestResources().anExistingUserNotInSilverpeasDomain();
      getTestResources().getWebServiceCaller().setDomainId(expectedUser.getDomainId() + "0");
      getAt(aResourceURI() + "/" + expectedUser.getId(), SelectableUser.class);
      fail("The user shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void gettingAllUsersOfAGivenGroupWhateverTheDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ALL);
    Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
    List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
    SelectableUser[] actualUsers = getAt(aResourceURI() + "?group=" + aGroup.getId(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.size()));
    assertThat(actualUsers, contains(expectedUsers));
  }

  @Test
  public void getAllUsersOfAGivenGroupInItsOwnsDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ONE);
    Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
    getTestResources().getWebServiceCaller().setDomainId(aGroup.getDomainId());
    List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
    SelectableUser[] actualUsers = getAt(aResourceURI() + "?group=" + aGroup.getId(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.size()));
    assertThat(actualUsers, contains(expectedUsers));
  }

  @Test
  public void getAllUsersOfAGivenGroupWhateverTheDomainWhenInSilverpeasDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_EACH);
    Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
    List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
    SelectableUser[] actualUsers = getAt(aResourceURI() + "?group=" + aGroup.getId(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.size()));
    assertThat(actualUsers, contains(expectedUsers));
  }

  @Test
  public void getAllUsersOfAGivenGroupInItsOwnDomainWhenNotInSilverpeasDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_EACH);
    Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
    getTestResources().getWebServiceCaller().setDomainId(aGroup.getDomainId());
    List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
    SelectableUser[] actualUsers = getAt(aResourceURI() + "?group=" + aGroup.getId(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.size()));
    assertThat(actualUsers, contains(expectedUsers));
  }

  @Test
  public void getAllUsersOfAnUnaccessibleGivenGroup() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ONE);
      Group aGroup = getTestResources().getAGroupNotInAnInternalDomain();
      getTestResources().getWebServiceCaller().setDomainId(aGroup.getDomainId() + "0");
      List<? extends UserDetail> expectedUsers = aGroup.getAllUsers();
      getAt(aResourceURI() + "?group=" + aGroup.getId(), getWebEntityClass());
      fail("The user shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }
  
  @Test
  public void getAUserByItsFirstName() {
    UserDetail expectedUser =  getTestResources().anExistingUser();
    UserDetail[] actualUsers = getAt(aResourceURI() + "?name=" + expectedUser.getFirstName(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(1));
    assertThat(actualUsers[0], is(expectedUser));
  }
  
  @Test
  public void getAUserByItsLastName() {
    UserDetail expectedUser =  getTestResources().anExistingUser();
    SelectableUser[] actualUsers = getAt(aResourceURI() + "?name=" + expectedUser.getLastName(),
            getWebEntityClass());
    assertThat(actualUsers.length, is(1));
    assertThat(actualUsers[0], is(expectedUser));
  }
  
  @Test
  public void getAUserByTheFirstCharactersOfItsName() {
    UserDetail[] expectedUsers =  getTestResources().getAllExistingUsers();
    String name = expectedUsers[0].getFirstName().substring(0, 2) + "*";
    SelectableUser[] actualUsers = getAt(aResourceURI() + "?name=" + name,
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.length));
    assertThat(actualUsers, contains(expectedUsers));
  }
  
  @Test
  public void getAUserInAGivenGroupByTheFirstCharactersOfItsName() {
    Group group;
    List<? extends UserDetail> expectedUsers;
    do {
      group = getTestResources().getAGroupNotInAnInternalDomain();
      expectedUsers =  group.getAllUsers();
    } while(expectedUsers.isEmpty());
    String name = expectedUsers.get(0).getFirstName().substring(0, 2) + "*";
    SelectableUser[] actualUsers = getAt(aResourceURI() + "?group=" + group.getId() + "?name=" + name,
            getWebEntityClass());
    assertThat(actualUsers.length, is(expectedUsers.size()));
    assertThat(actualUsers, contains(expectedUsers));
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
  public Class<SelectableUser[]> getWebEntityClass() {
    return SelectableUser[].class;
  }
}

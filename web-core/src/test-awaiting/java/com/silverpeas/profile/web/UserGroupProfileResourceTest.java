/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
import com.stratelia.webactiv.beans.admin.DomainProperties;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManagerHelper;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;

import static com.silverpeas.profile.web.UserProfileTestResources.*;
import static com.silverpeas.profile.web.matchers.UserGroupsMatcher.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit tests on the operations published by the UserGroupProfileResource REST service.
 */
public class UserGroupProfileResourceTest extends ResourceGettingTest<UserProfileTestResources> {

  private String sessionKey;
  private UserDetail currentUser;

  public UserGroupProfileResourceTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    currentUser = aUser();
    sessionKey = authenticate(currentUser);
  }

  /**
   * With no domain isolation, a user can see the user groups of another domain.
   */
  @Test
  public void getAllRootGroupsWhateverTheDomainWithNoDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    Group[] expectedGroups = getTestResources().getAllExistingRootGroups();
    getTestResources().whenSearchGroupsByCriteriaThenReturn(expectedGroups);
    currentUser.setDomainId(currentUser.getDomainId() + "0");

    UserGroupProfileEntity[] actualGroups = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualGroups.length, is(expectedGroups.length));
    assertThat(actualGroups, contains(expectedGroups));
  }

  /**
   * With a semi domain isolation, only a user in the Silverpeas domain can see the user groups of
   * another domain.
   */
  @Test
  public void getAllRootGroupsWhateverTheDomainWhenInSilverpeasDomainAndWithSemiDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
    Group[] expectedGroups = getTestResources().getAllExistingRootGroups();
    getTestResources().whenSearchGroupsByCriteriaThenReturn(expectedGroups);

    UserGroupProfileEntity[] actualGroups = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualGroups.length, is(expectedGroups.length));
    assertThat(actualGroups, contains(expectedGroups));
  }

  /**
   * With a full domain isolation, a user in the Silverpeas domain can see only the user groups of
   * its own domain.
   */
  @Test
  public void getAllGroupsInItsOwnDomainWhenInSilverpeasDomainAndWithFullDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_EACH);
    Group[] expectedGroups = getTestResources().getAllExistingRootGroupsInDomain(currentUser.getDomainId());
    getTestResources().whenSearchGroupsByCriteriaThenReturn(expectedGroups);

    UserGroupProfileEntity[] actualGroups = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualGroups.length, is(expectedGroups.length));
    assertThat(actualGroups, contains(expectedGroups));
  }

  /**
   * With a semi domain isolation, a user can see only the user groups of its own domain.
   */
  @Test
  public void getAllRootGroupsInItsOwnsDomainWithSemiDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
    String domainId = getTestResources().getAllDomainIdsExceptedSilverpeasOne().get((0));
    currentUser.setDomainId(domainId);
    Group[] expectedGroups = getTestResources().getAllRootGroupsAccessibleFromDomain(domainId);
    getTestResources().whenSearchGroupsByCriteriaThenReturn(expectedGroups);

    UserGroupProfileEntity[] actualGroups = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualGroups.length, is(expectedGroups.length));
    assertThat(actualGroups, contains(expectedGroups));
  }

  /**
   * With a full domain isolation, a user can see only the user groups of its own domain.
   */
  @Test
  public void getAllRootGroupsInItsOwnDomainWithFullDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_EACH);
    String domainId = getTestResources().getAllDomainIdsExceptedSilverpeasOne().get((1));
    currentUser.setDomainId(domainId);
    Group[] expectedGroups = getTestResources().getAllRootGroupsAccessibleFromDomain(domainId);
    getTestResources().whenSearchGroupsByCriteriaThenReturn(expectedGroups);

    UserGroupProfileEntity[] actualGroups = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualGroups.length, is(expectedGroups.length));
    assertThat(actualGroups, contains(expectedGroups));
  }

  /**
   * With no domain isolation, a user can access a given user group, whatever the domain.
   */
  @Test
  public void getAGivenAccessibleGroupWhateverTheDomainWithNoDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    currentUser.setDomainId(currentUser.getDomainId() + "0");
    Group actualGroup = getTestResources().anExistingGroup();
    String path = buildURIPathOf(actualGroup);
    UserGroupProfileEntity expectedGroup = getAt(path, UserGroupProfileEntity.class);
    assertThat(expectedGroup, notNullValue());
    assertThat(expectedGroup.getId(), is(actualGroup.getId()));
  }

  /**
   * With a semi domain isolation, only a user in the Silverpeas domain can access a user group of
   * another domain.
   */
  @Test
  public void getAGivenAccessibleGroupWhateverTheDomainWhenInSilverpeasDomainAndWithSemiDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    Group actualGroup = getTestResources().anExistingGroup();
    String path = buildURIPathOf(actualGroup);
    UserGroupProfileEntity expectedGroup = getAt(path, UserGroupProfileEntity.class);
    assertThat(expectedGroup, notNullValue());
    assertThat(expectedGroup.getId(), is(actualGroup.getId()));
  }


  /**
   * With full domain isolation, a user can access only a user group of its own domain.
   */
  @Test
  public void getAGivenAccessibleGroupOnlyInItsOwnDomainWithFullDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
    Group actualGroup = getTestResources().getAGroupNotInAnInternalDomain();
    currentUser.setDomainId(actualGroup.getDomainId());
    String path = buildURIPathOf(actualGroup);
    UserGroupProfileEntity expectedGroup = getAt(path, UserGroupProfileEntity.class);
    assertThat(expectedGroup, notNullValue());
    assertThat(expectedGroup.getId(), is(actualGroup.getId()));
  }

  /**
   * With semi domain isolation, a user that is not in the Silverpeas domain cannot access a user
   * group of another domain.
   */
  @Test
  public void getAGivenUnaccessibleGroupInAnotherDomainWithSemiDomainIsolation() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
      Group actualGroup = getTestResources().getAGroupNotInAnInternalDomain();
      currentUser.setDomainId(actualGroup.getDomainId() + "0");
      String path = buildURIPathOf(actualGroup);
      getAt(path, UserGroupProfileEntity.class);
      fail("The group shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  /**
   * With full domain isolation, a user cannot access a user group of another domain.
   */
  @Test
  public void getAGivenUnaccessibleGroupInAnotherDomainWithFullDomainIsolation() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_EACH);
      Group actualGroup = getTestResources().getAGroupNotInAnInternalDomain();
      currentUser.setDomainId(actualGroup.getDomainId() + "0");
      String path = buildURIPathOf(actualGroup);
      getAt(path, UserGroupProfileEntity.class);
      fail("The group shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  /**
   * With no isolation domain, a user can access the subgroups of a user group whatever their
   * domain.
   */
  @Test
  public void getTheSubGroupsOfAGivenAccessibleGroupWhateverTheDomainWithNoDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    Group actualGroup = getTestResources().anExistingGroup();
    String path = buildURIPathOf(actualGroup) + "/groups";
    List<? extends Group> actualSubGroups = actualGroup.getSubGroups();
    getTestResources().whenSearchGroupsByCriteriaThenReturn(actualSubGroups.toArray(
            new Group[actualSubGroups.size()]));

    UserGroupProfileEntity[] expectedSubGroups = getAt(path, getWebEntityClass());
    assertThat(actualSubGroups.size(), is(expectedSubGroups.length));
    assertThat(expectedSubGroups, contains(actualSubGroups));
  }

  /**
   * With semi isolation domain, a user can access the subgroups of a user group of its own domain.
   */
  @Test
  public void getTheSubGroupsOfAGivenAccessibleGroupOnlyInItsOwnDomainWithSemiDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
    Group actualGroup = getTestResources().getAGroupNotInAnInternalDomain();
    currentUser.setDomainId(actualGroup.getDomainId());
    String path = buildURIPathOf(actualGroup) + "/groups";
    List<? extends Group> actualSubGroups = actualGroup.getSubGroups();
    getTestResources().whenSearchGroupsByCriteriaThenReturn(actualSubGroups.toArray(
            new Group[actualSubGroups.size()]));

    UserGroupProfileEntity[] expectedSubGroups = getAt(path, getWebEntityClass());
    assertThat(actualSubGroups.size(), is(expectedSubGroups.length));
    assertThat(expectedSubGroups, contains(actualSubGroups));
  }

  /**
   * With full isolation domain, a user can access the subgroups of a user group of its own domain.
   */
  @Test
  public void getTheSubGroupsOfAGivenAccessibleGroupOnlyInItsOwnDomainWithFullDomainIsolation() {
    GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ALL);
    Group actualGroup = getTestResources().getAGroupNotInAnInternalDomain();
    currentUser.setDomainId(actualGroup.getDomainId());
    String path = buildURIPathOf(actualGroup) + "/groups";
    List<? extends Group> actualSubGroups = actualGroup.getSubGroups();
    getTestResources().whenSearchGroupsByCriteriaThenReturn(actualSubGroups.toArray(
            new Group[actualSubGroups.size()]));

    UserGroupProfileEntity[] expectedSubGroups = getAt(path, getWebEntityClass());
    assertThat(actualSubGroups.size(), is(expectedSubGroups.length));
    assertThat(expectedSubGroups, contains(actualSubGroups));
  }

  /**
   * With semi domain isolation, a user, not in the Silverpeas domain, cannot access the subgroups
   * of a group in another domain.
   */
  @Test
  public void getTheSubGroupsOfAGivenUnaccessibleGroupInAnotherDomainWithSemiDomainIsolation() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_ONE);
      Group actualGroup = getTestResources().getAGroupNotInAnInternalDomain();
      currentUser.setDomainId(actualGroup.getDomainId() + "0");
      String path = buildURIPathOf(actualGroup) + "/groups";
      getAt(path, getWebEntityClass());
      fail("The group shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  /**
   * With full domain isolation, a user cannot access the subgroups of a group in another domain.
   */
  @Test
  public void getTheSubGroupsOfAGivenUnaccessibleGroupInAnotherDomain() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(DomainProperties.DVIS_EACH);
      Group actualGroup = getTestResources().getAGroupNotInAnInternalDomain();
      currentUser.setDomainId(actualGroup.getDomainId() + "0");
      String path = buildURIPathOf(actualGroup) + "/groups";
      getAt(path, getWebEntityClass());
      fail("The group shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  /**
   * A user can access a user group by its name.
   */
  @Test
  public void getAGroupByItsName() {
    Group expectedGroup = getTestResources().anExistingRootGroup();
    getTestResources().whenSearchGroupsByCriteriaThenReturn(new Group[]{expectedGroup});

    UserGroupProfileEntity[] actualGroups = getAt(aResourceURI() + "?name="
            + expectedGroup.getName(),
            getWebEntityClass());
    assertThat(actualGroups.length, is(1));
    assertThat(actualGroups[0].getId(), is(expectedGroup.getId()));
  }

  /**
   * A user can access a user group by a pattern on its name.
   */
  @Test
  public void getAGroupByTheFirstCharactersOfItsName() {
    Group[] expectedGroups = getTestResources().getAllExistingRootGroups();
    getTestResources().whenSearchGroupsByCriteriaThenReturn(expectedGroups);

    UserGroupProfileEntity[] actualGroups = getAt(aResourceURI() + "?name=" + expectedGroups[0].
            getName().substring(
            0, 2) + "*",
            getWebEntityClass());
    assertThat(actualGroups.length, is(expectedGroups.length));
    assertThat(actualGroups, contains(expectedGroups));
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
    return GROUP_PROFILE_PATH;
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
  public Class<UserGroupProfileEntity[]> getWebEntityClass() {
    return UserGroupProfileEntity[].class;
  }

  private String buildURIPathOf(Group group) {
    Group currentGroup = group;
    String path = "/" + currentGroup.getId();
    while (!currentGroup.isRoot()) {
      currentGroup = getTestResources().getGroupById(currentGroup.getSuperGroupId());
      path = "/" + currentGroup.getId() + "/groups" + path;
    }
    return aResourceURI() + path;
  }
}

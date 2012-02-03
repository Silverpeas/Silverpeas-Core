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
import static com.silverpeas.profile.web.matchers.UserGroupsMatcher.contains;
import com.silverpeas.rest.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.GeneralPropertiesManagerHelper;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.List;
import javax.ws.rs.core.Response;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.After;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests on the operations published by the UserGroupProfileResource REST service.
 */
public class UserGroupProfileResourceTest extends ResourceGettingTest<UserProfileTestResources> {

  private String sessionKey;

  public UserGroupProfileResourceTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    sessionKey = authenticate(aUser());
    getTestResources().allocate();
  }
  
  @After
  public void freeTestResources() {
    getTestResources().deallocate();
  }

  @Test
  public void gettingAllRootGroupsWhateverTheDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ALL);
    Group[] expectedGroups = getTestResources().getAllExistingRootGroups();
    SelectableUserGroup[] actualGroups = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualGroups.length, is(expectedGroups.length));
    assertThat(actualGroups, contains(expectedGroups));
  }

  @Test
  public void getAllRootGroupsInItsOwnsDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ONE);
    String domainId = getTestResources().getAllDomainIdsExceptedSilverpeasOne().get((0));
    getTestResources().getWebServiceCaller().setDomainId(domainId);
    Group[] expectedGroups = getTestResources().getAllRootGroupsAccessibleFromDomain(domainId);
    SelectableUserGroup[] actualGroups = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualGroups.length, is(expectedGroups.length));
    assertThat(actualGroups, contains(expectedGroups));
  }

  @Test
  public void getAllRootGroupsWhateverTheDomainWhenInSilverpeasDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_EACH);
    Group[] expectedGroups = getTestResources().getAllExistingRootGroups();

    SelectableUserGroup[] actualGroups = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualGroups.length, is(expectedGroups.length));
    assertThat(actualGroups, contains(expectedGroups));
  }

  @Test
  public void getAllRootGroupsInItsOwnDomainWhenNotInSilverpeasDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_EACH);
    String domainId = getTestResources().getAllDomainIdsExceptedSilverpeasOne().get((1));
    getTestResources().getWebServiceCaller().setDomainId(domainId);
    Group[] expectedGroups = getTestResources().getAllRootGroupsAccessibleFromDomain(domainId);

    SelectableUserGroup[] actualGroups = getAt(aResourceURI(), getWebEntityClass());
    assertThat(actualGroups.length, is(expectedGroups.length));
    assertThat(actualGroups, contains(expectedGroups));
  }

  @Test
  public void getAGivenAccessibleGroupWhateverTheDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ALL);
    Group actualGroup = getTestResources().anExistingGroup();
    String path = buildURIPathOf(actualGroup);
    SelectableUserGroup expectedGroup = getAt(path, SelectableUserGroup.class);
    assertThat(expectedGroup, notNullValue());
    assertThat(expectedGroup.getId(), is(actualGroup.getId()));
  }

  @Test
  public void getAGivenAccessibleGroupOnlyInItsOwnDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ONE);
    Group actualGroup = getTestResources().getAGroupNotInAnInternalDomain();
    getTestResources().getWebServiceCaller().setDomainId(actualGroup.getDomainId());
    String path = buildURIPathOf(actualGroup);
    SelectableUserGroup expectedGroup = getAt(path, SelectableUserGroup.class);
    assertThat(expectedGroup, notNullValue());
    assertThat(expectedGroup.getId(), is(actualGroup.getId()));
  }

  @Test
  public void getAGivenUnaccessibleGroupInAnotherDomain() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ONE);
      Group actualGroup = getTestResources().getAGroupNotInAnInternalDomain();
      getTestResources().getWebServiceCaller().setDomainId(actualGroup.getDomainId() + "0");
      String path = buildURIPathOf(actualGroup);
      getAt(path, SelectableUserGroup.class);
      fail("The group shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }
  
  @Test
  public void getTheSubGroupsOfAGivenAccessibleGroupWhateverTheDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ALL);
    Group actualGroup = getTestResources().anExistingGroup();
    String path = buildURIPathOf(actualGroup) + "/groups";
    List<Group> actualSubGroups = actualGroup.getSubGroups();
    SelectableUserGroup[] expectedSubGroups = getAt(path, getWebEntityClass());
    assertThat(actualSubGroups.size(), is(expectedSubGroups.length));
    assertThat(expectedSubGroups, contains(actualSubGroups));
  }

  @Test
  public void getTheSubGroupsOfAGivenAccessibleGroupOnlyInItsOwnDomain() {
    GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_ONE);
    Group actualGroup = getTestResources().getAGroupNotInAnInternalDomain();
    getTestResources().getWebServiceCaller().setDomainId(actualGroup.getDomainId());
    String path = buildURIPathOf(actualGroup) + "/groups";
    List<Group> actualSubGroups = actualGroup.getSubGroups();
    SelectableUserGroup[] expectedSubGroups = getAt(path, getWebEntityClass());
    assertThat(actualSubGroups.size(), is(expectedSubGroups.length));
    assertThat(expectedSubGroups, contains(actualSubGroups));
  }

  @Test
  public void getTheSubGroupsOfAGivenUnaccessibleGroupInAnotherDomain() {
    try {
      GeneralPropertiesManagerHelper.setDomainVisibility(GeneralPropertiesManager.DVIS_EACH);
      Group actualGroup = getTestResources().getAGroupNotInAnInternalDomain();
      getTestResources().getWebServiceCaller().setDomainId(actualGroup.getDomainId() + "0");
      String path = buildURIPathOf(actualGroup) + "/groups";
      getAt(path, getWebEntityClass());
      fail("The group shouldn't be get as it is unaccessible");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
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
  public Class<SelectableUserGroup[]> getWebEntityClass() {
    return SelectableUserGroup[].class;
  }

  private String buildURIPathOf(Group group) {
    Group currentGroup = group;
    String path = "/" + currentGroup.getId();
    while(!currentGroup.isRoot()) {
      currentGroup = getTestResources().getGroupById(currentGroup.getSuperGroupId());
      path = "/" + currentGroup.getId() + "/groups" + path;
    }
    return aResourceURI() + path;
  }
  
}

/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.web;

import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.web.mock.*;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.silverpeas.web.mock.SpaceAccessControllerMock;
import com.stratelia.webactiv.beans.admin.UserDetail;
import javax.inject.Inject;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * It is a wrapper of the resources required in the RESTWebServiceTest test cases.
 * It defines also some default parameters to use in tests such as the language of the user or the
 * unique identifier of the user.
 * 
 * The life-cycle of test resources is managed by the IoC container in which are bootstrapped the
 * test cases.
 * 
 * The class is dedicated to be extending by the test classes in order to set additional resources
 * required by the test cases they represent. All data or behaviour to prepare for the tests have
 * their place in a TestResources instance.
 */
public abstract class TestResources implements ApplicationContextAware {
  
  /**
   * The name under which a test resources must be deployed.
   */
  public static final String TEST_RESOURCES_NAME = "testRESTResources";

  /**
   * Identifier of the default user to use in the unit tests on the REST-based web services.
   */
  public static final String USER_ID_IN_TEST = "2";
  /**
   * Default language to use in the unit tests on the REST-based web services.
   */
  public static final String DEFAULT_LANGUAGE = "fr";
  /**
   * The default domain the default user belongs to.
   */
  public static final String DEFAULT_DOMAIN = "0";
  @Inject
  private SessionManagerMock sessionManagerMock;
  @Inject
  private AccessControllerMock accessControllerMock;
  @Inject
  private OrganizationControllerMockWrapper organizationControllerMockWrapper;
  @Inject
  private SpaceAccessControllerMock spaceAccessController;
  @Inject
  private PersonalizationServiceMockWrapper personalizationServiceMockWrapper;
  private static ApplicationContext context;
  
  private int maxUserId = Integer.valueOf(USER_ID_IN_TEST);

  /**
   * Gets a TestResources instance managed by the IoC container within which is running the test
   * case.
   * @return a bean managed TestResources. 
   */
  public static TestResources getTestResources() {
    assertNotNull(context);
    TestResources resources = context.getBean(TEST_RESOURCES_NAME, TestResources.class);
    assertNotNull(resources.getAccessControllerMock());
    assertNotNull(resources.getSpaceAccessControllerMock());
    assertNotNull(resources.getOrganizationControllerMock());
    assertNotNull(resources.getPersonalizationServiceMock());
    assertNotNull(resources.getSessionManagerMock());
    return resources;
  }

  /**
   * Gets the application context within which the current test case is actually running.
   * @return the test case execution context (from the underlying IoC container).
   */
  public ApplicationContext getApplicationContext() {
    return context;
  }

  /**
   * Gets a mock of the AccessController. This mock is used to handle authorization capabilities
   * according to the test fixture.
   * @return mock of the access controller used in the test case.
   */
  public AccessControllerMock getAccessControllerMock() {
    return accessControllerMock;
  }

  /**
   * Gets a mock of the SpaceAccessController. This mock is used to handle space authorization
   * capabilities
   * according to the test fixture.
   * @return mock of the access controller used in the test case.
   */
  public SpaceAccessControllerMock getSpaceAccessControllerMock() {
    return spaceAccessController;
  }

  /**
   * Gets the OrganizationController mock used in the tests.
   * With this mock, you can register expected behaviours for the OrganizationController instances.
   *
   * @return the OrganizationController mock used in the tests.
   */
  public OrganizationController getOrganizationControllerMock() {
    return organizationControllerMockWrapper.getOrganizationControllerMock();
  }

  /**
   * Gets a mock of the personalization service. This mock is to be used in tests.
   * This mock is created with Mockito, so you can use it for adding some behaviour to the returned
   * mocked service. By default, the mock is configured to returns a UserPreferences object for any
   * user with as prefered language the french (fr).
   * @return a mock of the PersonalizationService.
   */
  public PersonalizationService getPersonalizationServiceMock() {
    return personalizationServiceMockWrapper.getPersonalizationServiceMock();
  }

  /**
   * Gets a mock of the session management service. This mock is to be used in tests.
   * This mock is used to manage the sessions of the user(s) used in tests.
   * @return a mock of the SessionManagement.
   */
  public SessionManagerMock getSessionManagerMock() {
    return sessionManagerMock;
  }

  /**
   * Gets a user to use in the tests as the one in the current HTTP session.
   * This method is just for tests requiring only one user and whatever he's.
   * The user in defined in the default domain (see DEFAULT_DOMAIN).
   * The user has no profiles for any Silverpeas component instances.
   * @return the current user in the underlying HTTP session used in the tests.
   */
  public UserDetailWithProfiles aUser() {
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Toto");
    user.setLastName("Chez-les-papoos");
    user.setDomainId(DEFAULT_DOMAIN);
    return user;
  }
  
  /**
   * Computes a user named with the specified first and last name.
   * The user in defined in the default domain (see DEFAULT_DOMAIN).
   * The user has no profiles for any Silverpeas component instances.
   * @param firstName the user first name.
   * @param lastName the user last name.
   * @return a user.
   */
  public UserDetailWithProfiles aUserNamed(String firstName, String lastName) {
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setDomainId(DEFAULT_DOMAIN);
    return user;
  }
  
  /**
   * Registers the specified user among the organization controller so that when it is asked it is
   * returned by the controller. The unique identifier of the user is set by this method.
   * If the user is one with profiles, then the method getUserProfile of the OrganizationController
   * is prepared to return the expected profiles.
   * @param user the user to registers in the test context.
   * @return the user itself with its identifier set.
   */
  public UserDetail registerUser(final UserDetail user) {
    OrganizationController mock = getOrganizationControllerMock();
    user.setId(String.valueOf(maxUserId++));
    if (user.getDomainId() == null) {
      user.setDomainId(DEFAULT_DOMAIN);
    }
    Domain domain = new Domain();
    domain.setId(user.getDomainId());
    domain.setName("Domaine " + user.getDomainId());
    if (user instanceof UserDetailWithProfiles) {
      UserDetailWithProfiles userWithProfiles = (UserDetailWithProfiles) user;
      for (String componentId : userWithProfiles.getAccessibleComponentIds()) {
        String[] profiles = userWithProfiles.getUserProfiles(componentId);
        when(mock.getUserProfiles(user.getId(), componentId)).thenReturn(profiles);
      }
    }
    when(mock.getUserDetail(user.getId())).thenReturn(user);
    when(mock.getDomain(user.getDomainId())).thenReturn(domain);
    
    return user;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }
}

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
import com.silverpeas.web.mock.AccessControllerMock;
import com.silverpeas.web.mock.MockablePersonalizationService;
import com.silverpeas.web.mock.OrganizationControllerMock;
import com.silverpeas.web.mock.SessionManagerMock;
import com.stratelia.webactiv.beans.admin.UserDetail;
import javax.inject.Inject;
import static org.junit.Assert.assertNotNull;
import org.mockito.Mock;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * It is a wrapper of the resources that have to be used in the RESTWebServiceTest test cases.
 * It defines also some default parameters to use in tests such as the language of the user or the
 * unique identifier of the user.
 * 
 * The life-cycle of test resources is managed by the IoC container in which are bootstrapped the
 * test cases.
 * 
 * The class is dedicated to be extending by the test classes in order to set additional resources
 * required by the test cases they represent.
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
  private SessionManagerMock sessionManager;
  @Inject
  private AccessControllerMock accessController;
  @Inject
  private OrganizationControllerMock organizationController;
  @Inject
  private PersonalizationService personalizationService;
  private static ApplicationContext context;

  /**
   * Gets a TestResources instance managed by the IoC container within which is running the test
   * case.
   * @return a bean managed TestResources. 
   */
  public static TestResources getTestResources() {
    assertNotNull(context);
    TestResources resources = context.getBean(TEST_RESOURCES_NAME, TestResources.class);
    assertNotNull(resources.getAccessControllerMock());
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
    return accessController;
  }

  /**
   * Gets a mock of the organization controller. This mock is to be used in tests.
   * Currently, the mock is used to specify the detail of the authenticated users to return.
   * If the business service used by the web service requires some of the OrganizationController
   * operations, then mocks theses operations by using the mocked OrganizationController instance
   * wrapped by the mock.
   * This method should be called if the organization controller isn't injected by the IoC container
   * in the objects that requires it.
   * Actually the mock is managed by the IoC container under the name 'organizationController'. If
   * the spring context is well configured for tests, the mock should be injected instead of an
   * OrganizationController managed instance.
   *
   * @return a mock of the OrganizationController.
   */
  public OrganizationControllerMock getOrganizationControllerMock() {
    return organizationController;
  }

  /**
   * Gets a mock of the personalization service. This mock is to be used in tests.
   * This mock is created with Mockito, so you can use it for adding some behaviour to the returned
   * mocked service. By default, the mock is configured to returns a UserPreferences object for any
   * user with as prefered language the french (fr).
   * @return a mock of the PersonalizationService.
   */
  public PersonalizationService getPersonalizationServiceMock() {
    return personalizationService;
  }

  /**
   * Gets a mock of the session management service. This mock is to be used in tests.
   * This mock is used to manage the sessions of the user(s) used in tests.
   * @return a mock of the SessionManagement.
   */
  public SessionManagerMock getSessionManagerMock() {
    return sessionManager;
  }

  /**
   * Creates a new user to use in tests.
   * @return a new user.
   */
  public UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setFirstName("Toto");
    user.setLastName("Chez-les-papoos");
    user.setId(USER_ID_IN_TEST);
    user.setDomainId(DEFAULT_DOMAIN);
    return user;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }
}

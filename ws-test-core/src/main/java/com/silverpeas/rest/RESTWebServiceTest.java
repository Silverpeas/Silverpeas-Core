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

package com.silverpeas.rest;

import javax.inject.Inject;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.rest.mock.OrganizationControllerMock;
import com.silverpeas.session.SessionInfo;
import java.util.UUID;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.riffpie.common.testing.AbstractSpringAwareJerseyTest;
import com.silverpeas.personalization.service.MockablePersonalizationService;
import com.silverpeas.rest.mock.AccessControllerMock;
import com.silverpeas.session.SessionManagement;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.junit.Before;
import org.springframework.web.context.ContextLoaderListener;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * The base class for testing REST web services in Silverpeas.
 * This base class wraps all of the mechanismes required to prepare the environment for testing
 * web services with Jersey and Spring in the context of Silverpeas.
 */
public abstract class RESTWebServiceTest extends AbstractSpringAwareJerseyTest {

  /**
   * Identifier of the default user to use in the unit tests on the REST-based web service.
   */
  public static final String USER_ID_IN_TEST = "2";
  
  protected static final String CONTEXT_NAME = "silverpeas";
  protected static final String DEFAULT_LANGUAGE = "fr";

  @Inject
  private SessionManagement sessionManager;
  @Inject
  private AccessControllerMock accessController;
  @Inject
  private OrganizationControllerMock organizationController;
  @Inject
  private MockablePersonalizationService personalizationService;
  private Client webClient;

  /**
   * Constructs a new test case on the REST web service testing.
   * It bootstraps the runtime context into which the REST web service to test will run.
   * @param webServicePackage the Java package in which is defined the web service to test.
   * @param springContext the Spring context configuration file that accompanies the web service to
   * test.
   */
  public RESTWebServiceTest(String webServicePackage, String springContext) {
    super(new WebAppDescriptor.Builder(webServicePackage).
        contextPath(CONTEXT_NAME).
        contextParam("contextConfigLocation", "classpath:/" + springContext).
        initParam(JSONConfiguration.FEATURE_POJO_MAPPING, "true").
        requestListenerClass(org.springframework.web.context.request.RequestContextListener.class).
        servletClass(SpringServlet.class).
        contextListenerClass(ContextLoaderListener.class).
        build());
    ClientConfig config = new DefaultClientConfig();
    config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
    webClient = Client.create(config);
  }
  
  /**
   * Gets the component instances to take into account in tests. Theses component instances will be
   * considered as existing. Others than thoses will be rejected with an HTTP error 404 (NOT FOUND).
   * @return an array with the identifier of the component instances to take into account in tests.
   * The array cannot be null but it can be empty.
   */
  abstract public String[] getExistingComponentInstances();

  @Override
  public WebResource resource() {
    return webClient.resource(getBaseURI() + CONTEXT_NAME + "/");
  }

  @Before
  public void checkDependencyInjectionAndMockResources() {
    assertNotNull(sessionManager);
    assertNotNull(accessController);
    assertNotNull(personalizationService);
    PersonalizationService mockedPersonalizationService = mock(PersonalizationService.class);
    UserPreferences settings = new UserPreferences();
    settings.setLanguage(DEFAULT_LANGUAGE);
    when(mockedPersonalizationService.getUserSettings(anyString())).thenReturn(settings);
    personalizationService.setPersonalizationService(mockedPersonalizationService);
    for (String componentId : getExistingComponentInstances()) {
      organizationController.addComponentInstance(componentId);
    }
  }

  /**
   * Gets a mock of the organization controller. This mock is to be used in tests.
   * Currently, the mock is used to specify the detail of the authenticated users to return.
   * If the business service used by the web service requires some of the OrganizationController
   * operations,then mocks or stubs the business service so that it calls the operations to
   * the mock of the OrganizationController instead of a real OrganizationController instance.
   * This method should be called if the organization controller isn't injected by the IoC container
   * in the objects that requires it.
   * Actually the mock is managed by the IoC container under the name 'organizationController'. If
   * the spring context is well configured for tests, the mock should be injected instead of an
   * OrganizationController managed instance.
   *
   * @return a mock of the OrganizationController.
   */
  public OrganizationController getMockedOrganizationController() {
    return organizationController;
  }
  
  /**
   * Gets a mock of the personalization service. This mock is to be used in tests.
   * This mock is created with Mockito, so you can use it for adding some behaviour to the returned
   * mocked service. By default, the mock is configured to returns a UserPreferences object for any
   * user with as prefered language the french (fr).
   * @return a mock of the PersonalizationService.
   */
  public PersonalizationService getMockedPersonalizationService() {
    return personalizationService.getPersonalizationService();
  }

  /**
   * Authenticates the user to use in the tests.
   * The user will be added in the context of the mocked organization controller.
   * @param theUser the user to authenticate.
   * @return the key of the opened session.
   */
  public String authenticate(final UserDetail theUser) {
    organizationController.addUserDetail(theUser);
    SessionInfo session = new SessionInfo(UUID.randomUUID().toString(), theUser);
    return sessionManager.openSession(session);
  }

  /**
   * Denies the access to the silverpeas resources to all users.
   */
  public void denieAuthorizationToUsers() {
    accessController.setAuthorization(false);
  }
  
  /**
   * Adds the specified component instance among the existing ones and that will be used in tests.
   * @param componentId the unique identifier of the component instance to use in tests.
   */
  public void addComponentInstance(String componentId) {
    organizationController.addComponentInstance(componentId);
  }

  /**
   * Creates a new user.
   * @return a new user.
   */
  public UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setFirstName("Toto");
    user.setLastName("Chez-les-papoos");
    user.setId("2");
    return user;
  }
}

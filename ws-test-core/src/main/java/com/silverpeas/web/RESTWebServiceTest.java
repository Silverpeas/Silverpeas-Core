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
* FLOSS exception. You should have recieved a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/legal/licensing"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.silverpeas.web;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Before;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

import com.silverpeas.personalization.UserMenuDisplay;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.session.SessionInfo;
import com.silverpeas.web.mock.AccessControllerMock;
import com.silverpeas.web.mock.SpaceAccessControllerMock;
import com.silverpeas.web.mock.UserDetailWithProfiles;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
* The base class for testing REST web services in Silverpeas.
* This base class wraps all of the mechanismes required to prepare the environment for testing
* web services with Jersey and Spring in the context of Silverpeas.
* @param <T> the test resources wrapper in use in the test cases.
*/
public abstract class RESTWebServiceTest<T extends TestResources> extends JerseyTest {

  protected static final String CONTEXT_NAME = "silverpeas";
  private T testResources;
  private Client webClient;

  /**
* Constructs a new test case on the REST web service testing.
* It bootstraps the runtime context into which the REST web service to test will run.
* @param webServicePackage the Java package in which is defined the web service to test.
* @param springContext the Spring context configuration file that accompanies the web service to
* test.
*/
  public RESTWebServiceTest(String webServicePackage, String springContext) {
    super(new WebAppDescriptor.Builder(webServicePackage).contextPath(CONTEXT_NAME).
            contextParam("contextConfigLocation", "classpath:/" + springContext).
            initParam(JSONConfiguration.FEATURE_POJO_MAPPING, "true").
            initParam("com.sun.jersey.config.property.packages", "org.codehaus.jackson.jaxrs;com.silverpeas.web.mappers").
            requestListenerClass(RequestContextListener.class).
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

  /**
   * Gets tools to take into account in tests. Theses tools will be considered as existing. Others
   * than thoses will be rejected with an HTTP error 404 (NOT FOUND).
   * @return an array with the identifier of tools to take into account in tests. The array cannot
   * be null but it can be empty.
   */
  public String[] getExistingTools() {
    return new String[]{};
  }

  @Override
  public WebResource resource() {
    return webClient.resource(getBaseURI() + CONTEXT_NAME + "/");
  }

  @SuppressWarnings("unchecked")
  @Before
  public void prepareMockedResources() {
    testResources = (T) TestResources.getTestResources();
    PersonalizationService mock = testResources.getPersonalizationServiceMock();
    UserPreferences preferences = new UserPreferences(TestResources.DEFAULT_LANGUAGE, "", "", false,
            true, true, UserMenuDisplay.DISABLE);
    when(mock.getUserSettings(anyString())).thenReturn(preferences);
    for (String componentId : getExistingComponentInstances()) {
      addComponentInstance(componentId);
    }
    for (String toolId : getExistingTools()) {
      addTool(toolId);
    }
  }

  /**
* Authenticates the user to use in the tests.
* The user will be added in the context of the mocked organization controller.
* @param theUser the user to authenticate.
* @return the key of the opened session.
*/
  public String authenticate(final UserDetail theUser) {
    getTestResources().registerUser(theUser);
    for (String componentId : getExistingComponentInstances()) {
      setComponentAccessibilityToUser(componentId, theUser.getId());
    }
    SessionInfo session = new SessionInfo(UUID.randomUUID().toString(), theUser);
    return getTestResources().getSessionManagerMock().openSession(session);
  }

  /**
   * Gets a user to use in the test case. The user is managed by the test resources. So, to override
   * it, please override the TestResources class.
   * @return the detail about the user in use in the current test case.
   */
  public UserDetailWithProfiles aUser() {
    return getTestResources().aUser();
  }

  /**
* Denies the access to the silverpeas resources to all users.
*/
  public void denieAuthorizationToUsers() {
    getAccessControllerMock().setAuthorization(false);
  }

  /**
   * Denies the access to the silverpeas spaces to all users.
   */
  public void denieSpaceAuthorizationToUsers() {
    getMockedSpaceAccessController().setAuthorization(false);
  }

  /**
   * Gets the resources in use in the current test case.
   * @return a TestResources instance.
   */
  public T getTestResources() {
    return testResources;
  }

  /**
* Adds the specified component instance among the existing ones and that will be used in tests.
* @param componentId the unique identifier of the component instance to use in tests.
*/
  public void addComponentInstance(String componentId) {
    OrganizationController mock = getOrganizationControllerMock();
    when(mock.isComponentExist(componentId)).thenReturn(true);
  }
  
  public void addTool(String toolId) {
    OrganizationController mock = getOrganizationControllerMock();
    when(mock.isToolAvailable(toolId)).thenReturn(true);
  }
  
  public void setComponentAccessibilityToUser(String componentId, String userId) {
    OrganizationController mock = getOrganizationControllerMock();
    when(mock.isComponentAvailable(componentId, userId)).thenReturn(true);
  }

  protected OrganizationController getOrganizationControllerMock() {
    return getTestResources().getOrganizationControllerMock();
  }

  protected AccessControllerMock getAccessControllerMock() {
    return getTestResources().getAccessControllerMock();
  }

  protected SpaceAccessControllerMock getMockedSpaceAccessController() {
    return getTestResources().getSpaceAccessControllerMock();
  }

  protected PersonalizationService getPersonalizationServiceMock() {
    return getTestResources().getPersonalizationServiceMock();
  }
  
  protected MultivaluedMap<String, String> buildQueryParametersFrom(String query) {
    MultivaluedMap<String, String> parameters = new MultivaluedMapImpl();
    String[] queryParameters = query.split("&");
    for (String aQueryParameter : queryParameters) {
      String[] parameterParts = aQueryParameter.split("=");
      parameters.add(parameterParts[0], parameterParts.length > 1 ? parameterParts[1] : "");
    }
    return parameters;
  }
}

/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package com.stratelia.silverpeas.peasCore.servlets;

import com.silverpeas.web.mock.OrganizationControllerMockWrapper;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import com.stratelia.silverpeas.peasCore.servlets.control.AbstractTestWebComponentGenericController;
import com.stratelia.silverpeas.silverstatistics.control.SilverStatisticsManager;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.mockito.internal.stubbing.answers.Returns;
import org.silverpeas.core.cache.service.InMemoryCacheService;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.servlet.HttpRequest;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.util.StringTokenizer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author: Yohann Chastagnier
 */
public class WebComponentRequestRouterTest {

  // Spring context
  private ClassPathXmlApplicationContext context;

  @Before
  public void setUp() throws Exception {
    OrganizationControllerProvider.getFactory().clearFactory();

    // Spring
    context = new ClassPathXmlApplicationContext("spring-webComponentManager.xml");
    SilverStatisticsManager.setInstanceForTest(mock(SilverStatisticsManager.class));

    WebComponentManager.managedWebComponentRouters.clear();
    CacheServiceProvider.getRequestCacheService().clear();
    reset(getOrganisationController(),
        AbstractTestWebComponentGenericController.getResourceLocatorMock());
  }

  @After
  public void tearDown() {
    OrganizationControllerProvider.getFactory().clearFactory();
    SilverStatisticsManager.setInstanceForTest(null);
    reset(getOrganisationController(),
        AbstractTestWebComponentGenericController.getResourceLocatorMock());
    context.close();
  }

  private OrganizationController getOrganisationController() {
    return context.getBean(OrganizationControllerMockWrapper.class).getOrganizationControllerMock();
  }

  protected void verifyDestination(WebComponentRequestRouter routerInstance,
      String expectedDestination) {
    ServletContext servletContextMock = routerInstance.getServletContext();
    verify(servletContextMock, times(1)).getRequestDispatcher(expectedDestination);
  }

  private HttpRequest mockRequest(String path, SilverpeasRole greaterUserRole) {
    HttpRequest request = mock(HttpRequest.class);
    HttpSession session = mock(HttpSession.class);
    MainSessionController mainSessionController = mock(MainSessionController.class);
    OrganizationController organisationController = getOrganisationController();

    String uriPart = path;
    int indexOfUriParamSplit = path.indexOf('?');
    if (indexOfUriParamSplit >= 0) {
      // URI part
      uriPart = path.substring(0, indexOfUriParamSplit);
      // Params part
      String paramPart = path.substring(indexOfUriParamSplit + 1);
      StringTokenizer paramPartTokenizer = new StringTokenizer(paramPart, "&");
      while (paramPartTokenizer.hasMoreTokens()) {
        String param = paramPartTokenizer.nextToken();
        int indexOfEqual = param.indexOf('=');
        if (indexOfEqual > 0) {
          String paramName = param.substring(0, indexOfEqual);
          String paramValue = param.substring(indexOfEqual + 1);
          when(request.getParameter(paramName)).thenReturn(paramValue);
        }
      }
    }

    when(request.getPathInfo()).thenReturn(uriPart);
    when(request.getRequestURI()).thenReturn(
        UriBuilder.fromPath(URLUtil.getApplicationURL()).path(uriPart).build().toString());
    when(organisationController.isComponentAvailable(anyString(), anyString()))
        .then(new Returns(true));
    when(organisationController.getComponentInstLight(anyString()))
        .then(new Returns(new ComponentInstLight()));
    when(mainSessionController.getCurrentUserDetail()).thenReturn(new UserDetail());
    when(mainSessionController.getOrganisationController()).thenReturn(organisationController);
    ComponentContext componentContext = mock(ComponentContext.class);
    when(componentContext.getCurrentProfile())
        .thenReturn(greaterUserRole != null ? new String[]{greaterUserRole.name()} : null);
    when(componentContext.getCurrentComponentName()).thenReturn("componentName");
    when(componentContext.getCurrentSpaceName()).thenReturn("spaceName");
    when(componentContext.getCurrentComponentLabel()).thenReturn("componentLabel");
    when(mainSessionController.createComponentContext(anyString(), anyString()))
        .then(new Returns(componentContext));
    when(session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT))
        .thenReturn(mainSessionController);
    when(session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT))
        .thenReturn(mock(GraphicElementFactory.class));
    when(request.getSession()).thenReturn(session);
    when(request.getSession(anyBoolean())).then(new Returns(session));
    return request;
  }

  /**
   * Initialization of a WebComponentController.
   * @param controller
   */
  private WebComponentRequestRouter initRequestRouterWith(
      Class<? extends WebComponentController> controller) {
    WebComponentRequestRouter routerInstance = new WebComponentRequestRouter();
    ServletConfig servletConfig = mock(ServletConfig.class);
    when(servletConfig.getInitParameter(
        org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController.class
            .getSimpleName())).thenReturn(controller.getName());
    ServletContext servletContext = mock(ServletContext.class);
    when(servletConfig.getServletContext()).thenReturn(servletContext);

    try {
      routerInstance.init(servletConfig);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
    return routerInstance;
  }

  @SuppressWarnings("unchecked")
  protected <
      CONTROLLER extends WebComponentController<WEB_COMPONENT_REQUEST_CONTEXT>,
      WEB_COMPONENT_REQUEST_CONTEXT extends WebComponentRequestContext<? extends
      WebComponentController>> ControllerTest<CONTROLLER, WEB_COMPONENT_REQUEST_CONTEXT>
  onController(
      Class<CONTROLLER> controllerClass) {
    return new ControllerTest(controllerClass);
  }

  protected class TestResult<
      WEB_COMPONENT_REQUEST_CONTEXT extends WebComponentRequestContext<? extends
      WebComponentController>> {
    public WebComponentRequestRouter router = null;
    public WEB_COMPONENT_REQUEST_CONTEXT requestContext = null;
  }

  protected class ControllerTest<
      CONTROLLER extends WebComponentController<WEB_COMPONENT_REQUEST_CONTEXT>,
      WEB_COMPONENT_REQUEST_CONTEXT extends WebComponentRequestContext<? extends
      WebComponentController>> {
    private Class<CONTROLLER> controllerClass = null;
    private SilverpeasRole greaterUserRole = null;

    protected ControllerTest(Class<CONTROLLER> controllerClass) {
      this.controllerClass = controllerClass;
    }

    public ControllerTest setGreaterUserRole(SilverpeasRole greaterUserRole) {
      this.greaterUserRole = greaterUserRole;
      return this;
    }

    @SuppressWarnings("unchecked")
    public RequestTest<CONTROLLER, WEB_COMPONENT_REQUEST_CONTEXT> defaultRequest()
        throws Exception {
      return new RequestTest(this);
    }
  }

  protected class RequestTest<
      CONTROLLER extends WebComponentController<WEB_COMPONENT_REQUEST_CONTEXT>,
      WEB_COMPONENT_REQUEST_CONTEXT extends WebComponentRequestContext<? extends
      WebComponentController>> {
    private ControllerTest<CONTROLLER, WEB_COMPONENT_REQUEST_CONTEXT> controller;
    private String httpMethod = HttpMethod.GET;
    private String suffixPath = "Main";

    protected RequestTest(ControllerTest<CONTROLLER, WEB_COMPONENT_REQUEST_CONTEXT> controller) {
      this.controller = controller;
    }

    public RequestTest changeHttpMethodWith(final String httpMethod) {
      this.httpMethod = httpMethod;
      return this;
    }

    public RequestTest changeSuffixPathWith(final String suffixPath) {
      this.suffixPath = suffixPath;
      return this;
    }

    @SuppressWarnings("unchecked")
    public TestResult<WEB_COMPONENT_REQUEST_CONTEXT> perform() throws Exception {
      InMemoryCacheService sessionCache = CacheServiceProvider.getRequestCacheService()
          .get("@SessionCache@", InMemoryCacheService.class);
      CacheServiceProvider.getRequestCacheService().clear();
      CacheServiceProvider.getRequestCacheService().put("@SessionCache@",
          (sessionCache != null ? sessionCache : new InMemoryCacheService()));
      WebComponentRequestRouter routerInstance = initRequestRouterWith(controller.controllerClass);
      HttpServletResponse response = mock(HttpServletResponse.class);
      if (HttpMethod.GET.equals(httpMethod)) {
        routerInstance
            .doGet(mockRequest("/componentName26/" + suffixPath, controller.greaterUserRole),
                response);
      } else if (HttpMethod.POST.equals(httpMethod)) {
        routerInstance
            .doPost(mockRequest("/componentName26/" + suffixPath, controller.greaterUserRole),
                response);
      } else if (HttpMethod.PUT.equals(httpMethod)) {
        routerInstance
            .doPut(mockRequest("/componentName26/" + suffixPath, controller.greaterUserRole),
                response);
      } else if (HttpMethod.DELETE.equals(httpMethod)) {
        routerInstance
            .doDelete(mockRequest("/componentName26/" + suffixPath, controller.greaterUserRole),
                response);
      }
      WEB_COMPONENT_REQUEST_CONTEXT requestContext =
          (WEB_COMPONENT_REQUEST_CONTEXT) CacheServiceProvider.getRequestCacheService()
              .get(WebComponentRequestContext.class.getName());
      assertThat(requestContext, notNullValue());
      assertThat(requestContext.getHttpMethodClass().getName(), Matchers.endsWith(httpMethod));
      assertThat(requestContext.getController(), instanceOf(controller.controllerClass));
      TestResult<WEB_COMPONENT_REQUEST_CONTEXT> testResult =
          new TestResult<WEB_COMPONENT_REQUEST_CONTEXT>();
      testResult.router = routerInstance;
      testResult.requestContext = requestContext;
      return testResult;
    }
  }
}

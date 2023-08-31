/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.mvc.webcomponent;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.internal.stubbing.answers.Returns;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.silverstatistics.volume.service.SilverStatisticsManager;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedBeans;
import org.silverpeas.core.test.unit.extention.TestManagedMock;
import org.silverpeas.core.test.unit.extention.TestManagedMocks;
import org.silverpeas.core.test.unit.extention.TestedBean;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;
import org.silverpeas.core.web.session.SessionManager;
import org.silverpeas.core.web.token.SynchronizerTokenService;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
@TestManagedMocks({Administration.class, SessionManager.class, SilverStatisticsManager.class})
@TestManagedBeans({SynchronizerTokenService.class})
public abstract class WebComponentRequestRouterTest {

  @TestManagedMock
  private OrganizationController mockedOrganizationController;

  @TestedBean
  private WebComponentRequestRouter<?, ?> requestRouter;

  @TestedBean
  private SilverpeasWebUtil silverpeasWebUtil;

  private UserDetail user;

  @BeforeEach
  public void setUp(@TestManagedMock SilverpeasComponentInstanceProvider provider) {
    when(provider.getComponentName(any())).thenReturn("componentName");
    user = new UserDetail();
    user.setId("400");
    WebComponentManager.managedWebComponentRouters.clear();
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  private OrganizationController getOrganisationController() {
    return mockedOrganizationController;
  }

  protected void verifyNoNavigation(TestResult<?> testResult) throws Exception {
    ServletContext servletContextMock = testResult.router.getServletContext();
    verify(servletContextMock, times(0)).getRequestDispatcher(anyString());
    verify(testResult.requestContext.getResponse(), times(1)).getWriter();
  }

  protected void verifyDestination(WebComponentRequestRouter<?, ?> routerInstance,
      String expectedDestination) {
    ServletContext servletContextMock = routerInstance.getServletContext();
    verify(servletContextMock, times(1)).getRequestDispatcher(expectedDestination);
  }

  private HttpRequest mockRequest(String path, SilverpeasRole highestUserRole) {
    HttpRequest request = mock(HttpRequest.class);
    HttpSession session = mock(HttpSession.class);
    MainSessionController mainSessionController = mock(MainSessionController.class);
    when(mainSessionController.getUserId()).thenReturn(user.getId());
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
    when(organisationController.isComponentAvailableToUser(anyString(), anyString())).then(
        new Returns(true));
    when(organisationController.getComponentInstLight(anyString())).then(
        new Returns(new ComponentInstLight()));
    when(mainSessionController.getCurrentUserDetail()).thenReturn(new UserDetail());
    ComponentContext componentContext = mock(ComponentContext.class);
    when(componentContext.getCurrentProfile()).thenReturn(
        highestUserRole != null ? new String[]{highestUserRole.getName()} : null);
    when(componentContext.getCurrentComponentName()).thenReturn("componentName");
    when(componentContext.getCurrentSpaceName()).thenReturn("spaceName");
    when(componentContext.getCurrentComponentId()).thenReturn("componentName26");
    when(mainSessionController.createComponentContext(any(), anyString())).then(
        new Returns(componentContext));
    when(session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).thenReturn(
        mainSessionController);
    when(session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT)).thenAnswer(i ->
        mock(GraphicElementFactory.class));
    when(request.getSession()).thenReturn(session);
    when(request.getSession(anyBoolean())).then(new Returns(session));
    return request;
  }

  /**
   * Initialization of a WebComponentController.
   */
  private WebComponentRequestRouter<?, ?> initRequestRouterWith(WebComponentRequestRouter<?, ?> routerInstance,
      Class<? extends WebComponentController<?>> controller) {
    ServletConfig servletConfig = mock(ServletConfig.class);
    when(servletConfig.getInitParameter(
        org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController.class.getSimpleName()))
        .thenReturn(controller.getName());
    ServletContext servletContext = mock(ServletContext.class);
    when(servletConfig.getServletContext()).thenReturn(servletContext);

    try {
      routerInstance.init(servletConfig);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
    return routerInstance;
  }

  protected <C extends WebComponentController<R>, R extends WebComponentRequestContext<?
      extends WebComponentController<R>>> ControllerTest<C, R> onController(
      Class<C> controllerClass) {
    return new ControllerTest<>(controllerClass);
  }

  protected static class TestResult<R extends WebComponentRequestContext<?
      extends WebComponentController<R>>> {
    public WebComponentRequestRouter<?, R> router = null;
    public R requestContext = null;
  }

  protected class ControllerTest<C extends WebComponentController<R>,
      R extends WebComponentRequestContext<? extends WebComponentController<R>>> {
    private final Class<C> controllerClass;
    private SilverpeasRole highestUserRole = null;

    protected ControllerTest(Class<C> controllerClass) {
      this.controllerClass = controllerClass;
    }

    public ControllerTest<C, R> setHighestUserRole(SilverpeasRole highestUserRole) {
      this.highestUserRole = highestUserRole;
      return this;
    }

    public RequestTest<C, R> defaultRequest() {
      return new RequestTest<>(this);
    }
  }

  protected class RequestTest<C extends WebComponentController<R>,
      R extends WebComponentRequestContext<? extends WebComponentController<R>>> {
    private final ControllerTest<C, R> controller;
    private String httpMethod = HttpMethod.GET;
    private String suffixPath = "Main";

    protected RequestTest(ControllerTest<C, R> controller) {
      this.controller = controller;
    }

    public RequestTest<C, R> changeHttpMethodWith(final String httpMethod) {
      this.httpMethod = httpMethod;
      return this;
    }

    public RequestTest<C, R> changeSuffixPathWith(final String suffixPath) {
      this.suffixPath = suffixPath;
      return this;
    }

    @SuppressWarnings("unchecked")
    public TestResult<R> perform() throws Exception {
      SimpleCache sessionCache = CacheAccessorProvider.getSessionCacheAccessor().getCache();
      CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
      if (sessionCache != null) {
        // Session cache is not trashed
        ((SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor()).setCurrentSessionCache(
            sessionCache);
      } else {
        // Putting a current requester for the next actions of this test.
        ((SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor()).newSessionCache(user);
      }

      WebComponentRequestRouter<?, ?> routerInstance =
          initRequestRouterWith(requestRouter, controller.controllerClass);
      HttpServletResponse response = mock(HttpServletResponse.class);
      when(response.getWriter()).thenReturn(new PrintWriter4Test(new ByteArrayOutputStream()));
      if (HttpMethod.GET.equals(httpMethod)) {
        routerInstance.doGet(
            mockRequest("/componentName26/" + suffixPath, controller.highestUserRole), response);
      } else if (HttpMethod.POST.equals(httpMethod)) {
        routerInstance.doPost(
            mockRequest("/componentName26/" + suffixPath, controller.highestUserRole), response);
      } else if (HttpMethod.PUT.equals(httpMethod)) {
        routerInstance.doPut(
            mockRequest("/componentName26/" + suffixPath, controller.highestUserRole), response);
      } else if (HttpMethod.DELETE.equals(httpMethod)) {
        routerInstance.doDelete(
            mockRequest("/componentName26/" + suffixPath, controller.highestUserRole), response);
      }
      R requestContext = (R) CacheAccessorProvider.getThreadCacheAccessor()
          .getCache()
          .get(WebComponentRequestContext.class.getName());
      assertThat(requestContext, notNullValue());
      assertThat(requestContext.getHttpMethodClass().getName(), Matchers.endsWith(httpMethod));
      assertThat(requestContext.getController(), instanceOf(controller.controllerClass));
      TestResult<R> testResult = new TestResult<>();
      testResult.router = (WebComponentRequestRouter<?, R>) routerInstance;
      testResult.requestContext = requestContext;
      return testResult;
    }
  }

  private static class PrintWriter4Test extends PrintWriter {
    ByteArrayOutputStream baos;

    PrintWriter4Test(final ByteArrayOutputStream out) {
      super(out);
      baos = out;
    }

    @Override
    public String toString() {
      return baos.toString();
    }
  }
}

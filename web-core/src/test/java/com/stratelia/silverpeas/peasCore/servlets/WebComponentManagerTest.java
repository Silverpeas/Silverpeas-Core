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

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.control.BadTestWebComponentController;
import com.stratelia.silverpeas.peasCore.servlets.control.HomePageIsNotSpecifiedController;
import com.stratelia.silverpeas.peasCore.servlets.control.TestWebComponentController;
import com.stratelia.silverpeas.peasCore.servlets.control.TestWebComponentRequestContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.servlet.HttpRequest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author: Yohann Chastagnier
 */
public class WebComponentManagerTest {

  @Before
  public void setUp() throws ServletException {
    CacheServiceFactory.getRequestCacheService().clear();
  }

  @Test(expected = IllegalArgumentException.class)
  public void webComponentControllerIsNotAnnoted() throws ServletException {
    initRequestRouterWith(BadTestWebComponentController.class);
  }

  @Test
  public void veryfingWebComponentControllerInitialize() {
    WebComponentRequestRouter routerInstance =
        initRequestRouterWith(TestWebComponentController.class);
    assertThat(routerInstance.getSessionControlBeanName(),
        is("TestWebComponentControllerIdentifier"));
    assertThat(routerInstance.createComponentSessionController(mock(MainSessionController.class),
        mock(ComponentContext.class)), instanceOf(TestWebComponentController.class));
  }

  @Test
  @Ignore
  public void doGetOnRequestRouter() throws ServletException {
    WebComponentRequestRouter routerInstance =
        initRequestRouterWith(TestWebComponentController.class);
    routerInstance.doGet(mockRequest(), null);
    TestWebComponentRequestContext requestContext = CacheServiceFactory.getRequestCacheService()
        .get(WebComponentRequestContext.class.getName(), TestWebComponentRequestContext.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void homepageIsNotSpecified() throws ServletException {
    WebComponentRequestRouter routerInstance =
        initRequestRouterWith(HomePageIsNotSpecifiedController.class);
    routerInstance.doGet(null, null);
  }

  private HttpRequest mockRequest() {
    HttpRequest request = mock(HttpRequest.class);
    HttpSession session = mock(HttpSession.class);
    MainSessionController mainSessionController = mock(MainSessionController.class);
    OrganisationController organisationController = mock(OrganisationController.class);
    when(organisationController.isComponentAvailable(anyString(), anyString()))
        .then(new Returns(true));
    when(mainSessionController.getOrganisationController()).thenReturn(organisationController);
    when(session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT))
        .thenReturn(mainSessionController);
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
        com.stratelia.silverpeas.peasCore.servlets.annotation.WebComponentController.class
            .getSimpleName())).thenReturn(controller.getName());
    try {
      routerInstance.init(servletConfig);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
    return routerInstance;
  }
}

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
package org.silverpeas.core.web.mvc.webcomponent;

import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.io.IOException;

/**
 * This request router is an extension of the historical one. It provides a new way to perform the
 * requests on the server, especially by annoting methods that must be invoked.
 * @param <T> the type of the Component Session Controller that provides a lot of stuff around
 * the component, the user, etc.
 * @param <WEB_COMPONENT_REQUEST_CONTEXT> the type of the web component context.
 */
public final class WebComponentRequestRouter<
    T extends WebComponentController<WEB_COMPONENT_REQUEST_CONTEXT>,
    WEB_COMPONENT_REQUEST_CONTEXT extends WebComponentRequestContext<? extends
        WebComponentController>>
    extends ComponentRequestRouter<T> {
  private static final long serialVersionUID = -3344222078427488724L;

  private final static String WEB_COMPONENT_CONTROLLER_CLASS_NAME_PARAM =
      org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController.class
          .getSimpleName();

  private Class<T> webComponentControllerClass;
  private String webComponentControllerBeanName;

  @SuppressWarnings("unchecked")
  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    String webComponentClassName =
        servletConfig.getInitParameter(WEB_COMPONENT_CONTROLLER_CLASS_NAME_PARAM);
    try {
      webComponentControllerClass = (Class) Class.forName(webComponentClassName);
      org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController
          webComponentControllerAnnotation = webComponentControllerClass.getAnnotation(
          org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController.class);
      if (webComponentControllerAnnotation == null) {
        throw new IllegalArgumentException(webComponentClassName +
            " must specify one, and only one, @WebComponentController annotation");
      }
      webComponentControllerBeanName = webComponentControllerAnnotation.value();
    } catch (ClassNotFoundException e) {
      throw new ServletException(e);
    }
  }

  @Override
  public String getSessionControlBeanName() {
    return webComponentControllerBeanName;
  }

  @Override
  public T createComponentSessionController(final MainSessionController mainSessionCtrl,
      final ComponentContext componentContext) {
    try {
      return webComponentControllerClass
          .getConstructor(MainSessionController.class, ComponentContext.class)
          .newInstance(mainSessionCtrl, componentContext);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          webComponentControllerClass + " does not expose the required constructor.", e);
    }
  }

  @Override
  protected void doPut(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    WebComponentManager
        .manageRequestFor(webComponentControllerClass, PUT.class, (HttpRequest) request, response);
    super.doPost(request, response);
  }

  @Override
  protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    WebComponentManager
        .manageRequestFor(webComponentControllerClass, DELETE.class, (HttpRequest) request,
            response);
    super.doPost(request, response);
  }

  @Override
  public void doPost(final HttpServletRequest request, final HttpServletResponse response) {
    WebComponentManager
        .manageRequestFor(webComponentControllerClass, POST.class, (HttpRequest) request, response);
    super.doPost(request, response);
  }

  @Override
  public void doGet(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException {
    WebComponentManager
        .manageRequestFor(webComponentControllerClass, GET.class, (HttpRequest) request, response);
    super.doGet(request, response);
  }

  @Override
  public final String getDestination(final String path, final T componentSC,
      final HttpRequest request) {
    String destination;
    try {

      // Performing the request.
      destination = WebComponentManager.perform(componentSC, path).getDestination();

    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }
}

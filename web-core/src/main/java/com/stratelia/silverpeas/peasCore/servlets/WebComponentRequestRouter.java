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

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.servlet.HttpRequest;

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
 * @param <WEB_COMPONENT_CONTEXT> the type of the web component context.
 */
public abstract class WebComponentRequestRouter<T extends ComponentSessionController,
    WEB_COMPONENT_CONTEXT extends WebComponentContext<T>>
    extends ComponentRequestRouter<T> {
  private static final long serialVersionUID = -3344222078427488724L;

  @Override
  protected void doPut(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    WebRouteManager.manageRequestOf(this, PUT.class, (HttpRequest) request, response);
    super.doPost(request, response);
  }

  @Override
  protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    WebRouteManager.manageRequestOf(this, DELETE.class, (HttpRequest) request, response);
    super.doPost(request, response);
  }

  @Override
  public void doPost(final HttpServletRequest request, final HttpServletResponse response) {
    WebRouteManager.manageRequestOf(this, POST.class, (HttpRequest) request, response);
    super.doPost(request, response);
  }

  @Override
  public void doGet(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException {
    WebRouteManager.manageRequestOf(this, GET.class, (HttpRequest) request, response);
    super.doGet(request, response);
  }

  /**
   * Permits to perform some common initializations. The method is called just before the method
   * behing the identified path is invoked.
   * @param context the context of the request in relation with the web controller
   */
  protected void commonContextInitialization(WEB_COMPONENT_CONTEXT context) {
    context.getRequest().setAttribute("greaterUserRole", context.getGreaterUserRole());
  }

  @Override
  public final String getDestination(final String path, final T componentSC,
      final HttpRequest request) {
    String destination;
    SilverTrace
        .debug("peasCore", "WebComponentRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
            "User=" + componentSC.getUserId() + " Path=" + path);

    try {

      // Performing the request.
      destination = WebRouteManager.perform(this, componentSC, path).getDestination();

    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace
        .debug("peasCore", "WebComponentRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
            "Destination=" + destination);
    return destination;
  }
}

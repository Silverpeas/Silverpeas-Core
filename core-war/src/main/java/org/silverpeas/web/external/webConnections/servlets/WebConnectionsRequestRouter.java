/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.external.webconnections.servlets;

import org.silverpeas.web.external.webconnections.control.WebConnectionsSessionController;
import org.silverpeas.core.web.external.webconnections.model.ConnectionDetail;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;
import java.util.List;

public class WebConnectionsRequestRouter extends
    ComponentRequestRouter<WebConnectionsSessionController> {
  private static final long serialVersionUID = 1L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "webConnections";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public WebConnectionsSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new WebConnectionsSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param webConnectionsSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, WebConnectionsSessionController webConnectionsSC,
      HttpRequest request) {
    String destination = "";
    String rootDest = "/webConnections/jsp/";

    try {
      if (function.startsWith("Main")) {
        destination = getDestination("ViewConnections", webConnectionsSC, request);
      } else if (function.equals("Connection")) {
        String componentId = (String) request.getAttribute("ComponentId");
        String methodType = (String) request.getAttribute("Method");
        if (!StringUtil.isDefined(componentId)) {
          componentId = request.getParameter("ComponentId");
          methodType = request.getParameter("Method");
        }
        // rechercher si la connexion existe déjà
        ConnectionDetail connection = webConnectionsSC.getConnection(componentId);
        if (connection != null) {
          // se connecter directement avec les données
          request.setAttribute("Connection", connection);
          request.setAttribute("Method", methodType);
          destination = getDestination("Redirect", webConnectionsSC, request);
        } else {
          // demander les paramètres de connexion
          request.setAttribute("Action", "CreateConnection");
          addParam(request, webConnectionsSC);
          destination = rootDest + "connectionManager.jsp";
        }
      } else if (function.equals("CreateConnection")) {
        // créé la connexion
        String componentId = request.getParameter("ComponentId");
        ConnectionDetail connection = new ConnectionDetail(componentId);
        addParamToconnection(connection, request, webConnectionsSC);
        if (!isAnonymousAccess(request)) {
          // As anonymous user, connection parameters are not stored
          webConnectionsSC.createConnection(connection);
        }
        request.setAttribute("Connection", connection);
        destination = getDestination("Redirect", webConnectionsSC, request);
      } else if (function.equals("Redirect")) {
        ConnectionDetail connection = (ConnectionDetail) request.getAttribute("Connection");
        String methodType = (String) request.getAttribute("Method");
        request.setAttribute("Connection", connection);
        request.setAttribute("Method", methodType);
        destination = rootDest + "connection.jsp";
      } else if (function.equals("ExitRedirect")) {
        String componentId = request.getParameter("ComponentId");
        ConnectionDetail connection = new ConnectionDetail(componentId);
        addParamToconnection(connection, request, webConnectionsSC);
        request.setAttribute("Connection", connection);
        destination = getDestination("Redirect", webConnectionsSC, request);
      } else if (function.equals("ViewConnections")) {
        // liste des connexions de l'utilisateur
        List<ConnectionDetail> connections = (List<ConnectionDetail>) webConnectionsSC
            .getConnectionsByUser();
        request.setAttribute("Connections", connections);
        destination = rootDest + "viewConnections.jsp";
      } else if (function.equals("EditConnection")) {
        addParam(request, webConnectionsSC);
        request.setAttribute("Action", "UpdateConnection");
        destination = rootDest + "connectionManager.jsp";
      } else if (function.equals("UpdateConnection")) {
        String connectionId = request.getParameter("ConnectionId");
        String login = request.getParameter("Login");
        String password = request.getParameter("Password");
        webConnectionsSC.updateConnection(connectionId, login, password);
        destination = getDestination("ViewConnections", webConnectionsSC, request);
      } else if (function.equals("DeleteConnection")) {
        String connectionId = request.getParameter("ConnectionId");
        webConnectionsSC.deleteConnection(connectionId);
        destination = getDestination("ViewConnections", webConnectionsSC, request);
      } else {
        destination = rootDest + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }


    return destination;
  }

  private void addParamToconnection(ConnectionDetail connection, HttpServletRequest request,
      WebConnectionsSessionController webConnectionsSC) {
    String login = request.getParameter("Login");
    if (!StringUtil.isDefined(login)) {
      login = "";
    }
    String password = request.getParameter("Password");
    if (!StringUtil.isDefined(password)) {
      password = "";
    }
    ComponentInst inst = getOrganizationController().getComponentInst(connection.getComponentId());
    String componentName = inst.getLabel();
    String url = inst.getParameterValue("URL");
    Hashtable<String, String> param = new Hashtable<String, String>();
    String nameLogin = inst.getParameterValue("login");
    param.put(nameLogin, login);
    String namePassword = inst.getParameterValue("password");
    param.put(namePassword, password);
    int i = 1;
    String nameParam = inst.getParameterValue("nameParam" + i);
    while (StringUtil.isDefined(nameParam)) {
      String value = inst.getParameterValue("valueParam" + i);
      param.put(nameParam, value);
      i = i + 1;
      nameParam = inst.getParameterValue("nameParam" + i);
    }
    connection.setUrl(url);
    connection.setParam(param);
    connection.setUserId(webConnectionsSC.getUserId());
    connection.setComponentName(componentName);
  }

  private void addParam(HttpServletRequest request, WebConnectionsSessionController webConnectionsSC) {
    String connectionId = request.getParameter("ConnectionId");
    ConnectionDetail connection;
    if (StringUtil.isDefined(connectionId)) {
      connection = webConnectionsSC.getConnectionById(connectionId);
    } else {
      String componentId = (String) request.getAttribute("ComponentId");
      connection = new ConnectionDetail(componentId);
      connection.setUserId(webConnectionsSC.getUserId());
    }
    request.setAttribute("Connection", connection);
    ComponentInst inst = getOrganizationController().getComponentInst(connection.getComponentId());
    request.setAttribute("ComponentInst", inst);
    request.setAttribute("IsAnonymousAccess", isAnonymousAccess(request));
  }

  private boolean isAnonymousAccess(HttpServletRequest request) {
    LookHelper lookHelper = LookHelper.getLookHelper(request.getSession());
    if (lookHelper != null) {
      return lookHelper.isAnonymousAccess();
    }
    return false;
  }

}
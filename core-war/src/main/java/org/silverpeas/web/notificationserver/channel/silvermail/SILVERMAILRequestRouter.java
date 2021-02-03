/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.notificationserver.channel.silvermail;

/**
 * Titre : SILVERMAILRequestRouter.java
 * @author eDurand
 */

import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILException;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

import java.util.HashMap;
import java.util.Map;

/**
 * Class declaration
 */
public class SILVERMAILRequestRouter extends ComponentRequestRouter<SILVERMAILSessionController> {
  private static final long serialVersionUID = -1666867964822716456L;

  /**
   * Package from which to attempt to load RequestHandler objects
   */
  private static final String REQUEST_HANDLER_PACKAGE =
      "org.silverpeas.web.notificationserver.channel.silvermail.requesthandlers";

  /**
   * Name of the session bean that will be used for this application. This must be matched by the
   * useBean actions in the JSPs.
   */
  private static final String SESSION_BEAN_NAME = "SILVERMAIL";

  /**
   * Hash table of RequestHandler instances, keyed by class name. This is used for performance
   * optimization, to avoid the need to load a class by name to process each request.
   */
  private static final Map<String, SILVERMAILRequestHandler> handlerHash = new HashMap<>();

  public SILVERMAILSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new SILVERMAILSessionController(mainSessionCtrl, context);
  }

  /**
   * This method has to be implemented in the component request router class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return SESSION_BEAN_NAME;
  }

  public String getDestination(String action, SILVERMAILSessionController componentSC,
      HttpRequest request) {
    String destination = "/SILVERMAIL/jsp/" + action;
    String function = extractFunctionName(action);

    // If we have an action parameter, obtain the RequestHandler that implements
    // the action. This method will throw a exception if no handler is associated with the action.
    SILVERMAILRequestHandler requestHandler;

    try {
      requestHandler = getHandlerInstance(function);
      componentSC.setCurrentFunction(function);

      // Return the URL of the view the RequestHandler instance
      // chooses after doing the work of processing the request
      destination = requestHandler.handleRequest(componentSC, request);
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  /**
   * Locate and return the RequestHandler instance for this action. Request handlers are managed
   * by the {@link ServiceProvider} subsystem.
   */
  private static SILVERMAILRequestHandler getHandlerInstance(String action)
      throws SILVERMAILException {
    try {
      return ServiceProvider.getService(action);
    } catch (IllegalStateException e) {
      final String handlerName = REQUEST_HANDLER_PACKAGE + "." + action;
      throw new SILVERMAILException("No such request handler " + handlerName, e);
    }
  }

  private static String extractFunctionName(String action) {
    String result = action;

    if (action.endsWith(".jsp")) {
      result = action.substring(0, action.length() - 4);
    }
    return result;
  }
}

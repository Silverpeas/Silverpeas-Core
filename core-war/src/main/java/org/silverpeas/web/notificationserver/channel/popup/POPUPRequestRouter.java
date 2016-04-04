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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.web.notificationserver.channel.popup;

/**
 * Titre : PopupRequestRouter.java
 * @author dlesimple
 * @version 1.0
 */

import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.http.HttpRequest;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class POPUPRequestRouter extends ComponentRequestRouter<POPUPSessionController> {

  private static final long serialVersionUID = -240502612963231503L;
  /**
   * Name of the session bean that will be used for this application. This must be matched by the
   * useBean actions in the JSPs.
   */
  private static final String SESSION_BEAN_NAME = "POPUP";

  public POPUPRequestRouter() {
  }

  public POPUPSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new POPUPSessionController(mainSessionCtrl, context);
  }

  /**
   * This method has to be implemented in the component request router class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return SESSION_BEAN_NAME;
  }

  public String getDestination(String function, POPUPSessionController popupSC,
      HttpRequest request) {
    String destination = "/POPUP/jsp/" + function;
    if (function.startsWith("Main")) {
      destination = "/POPUP/jsp/main.jsp";
    } else if (function.startsWith("ReadMessage")) {
      long messageId = Long.parseLong(request.getParameter("MessageID"));
      popupSC.setCurrentMessageId(messageId);
      destination = "/POPUP/jsp/readMessage.jsp";
    } else if (function.startsWith("ToAlert")) {
      popupSC.notifySession(request.getParameter("theUserId"), request
          .getParameter("messageAux"));
      request.setAttribute("action", "Close");
      destination = "/POPUP/jsp/readMessage.jsp";
    }
    return destination;
  }
}

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
---*/

package com.silverpeas.notificationserver.channel.popup;

/**
 * Titre : PopupRequestRouter.java
 * @author dlesimple
 * @version 1.0
 */

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.notificationserver.channel.popup.POPUPSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class POPUPRequestRouter extends ComponentRequestRouter {
  /**
   * Name of the session bean that will be used for this application. This must
   * be matched by the useBean actions in the JSPs.
   */
  private static final String SESSION_BEAN_NAME = "POPUP";

  public POPUPRequestRouter() {
  }

  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    ComponentSessionController component = new POPUPSessionController(
        mainSessionCtrl, context);
    return component;
  }

  /**
   * This method has to be implemented in the component request router class.
   * returns the session control bean name to be put in the request object ex :
   * for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return SESSION_BEAN_NAME;
  }

  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = "/POPUP/jsp/" + function;
    SilverTrace.info("popup", "POPUPRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "function=" + function);

    POPUPSessionController popupSC = (POPUPSessionController) componentSC;

    if (function.startsWith("Main")) {
      destination = "/POPUP/jsp/main.jsp";
    } else if (function.startsWith("ReadMessage")) {
      long messageId = Long.parseLong(request.getParameter("MessageID"));
      popupSC.setCurrentMessageId(messageId);
      destination = "/POPUP/jsp/readMessage.jsp";
    } else if (function.startsWith("ToAlert")) {
      SilverTrace.info("popup", "POPUPRequestRouter.getDestination()",
          "root.MSG_GEN_PARAM_VALUE", "request.getParameter(theUserId)="
              + request.getParameter("theUserId"));
      popupSC.notifySession(request.getParameter("theUserId"), request
          .getParameter("messageAux"));
      request.setAttribute("action", "Close");
      destination = "/POPUP/jsp/readMessage.jsp";
    }
    return destination;
  }
}

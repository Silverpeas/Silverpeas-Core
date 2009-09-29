/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.silverpeas.interestCenterPeas.servlets;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.interestCenterPeas.control.InterestCenterSessionController;
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
 */
public class InterestCenterPeasRequestRouter extends ComponentRequestRouter {

  private InterestCenterSessionController icSC = null;

  /**
   * Method declaration
   * 
   * 
   * @param mainSessionCtrl
   * @param componentContext
   * 
   * @return
   * 
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new InterestCenterSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for notificationUser, returns "notificationUser"
   */
  public String getSessionControlBeanName() {
    return "interestCenterPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @param request
   *          The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   *         "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = "";
    icSC = (InterestCenterSessionController) componentSC;

    try {
      if (function.startsWith("newICenter")) {
        String newICName = (String) request.getParameter("icName");
        destination = "newICenter.jsp";
        String action = (String) request.getParameter("action");
        request.setAttribute("icName", newICName);
        if ("check".equals(action)) {
          if (icSC.isICExists(newICName)) {
            request.setAttribute("action", "needConfirm");
          } else {
            request.setAttribute("action", "save");
          }
          destination = "newICenter.jsp";
        }
      } else if (function.startsWith("usedICenter.jsp")) {
        String newICName = (String) request.getParameter("icName");
        request.setAttribute("icName", newICName);
        destination = "usedICenter.jsp";
      } else if (function.startsWith("iCenterList")) {
        String mode = (String) request.getParameter("mode");
        if ("delete".equals(mode)) {
          Object o = request.getParameterValues("icCheck");
          if (o != null) {
            String[] iDs = (String[]) o;
            icSC.removeICByPKs(iDs);
          }
        }
        ArrayList icList = icSC.getICByUserId();
        request.setAttribute("icList", icList);
        destination = "iCenterList.jsp";
      }
    } catch (Exception e) {
      SilverTrace.error("interestCenterPeas",
          "InterestCenterPeasRequestRouter.getDestination",
          "pdcPeas.EX_GET_DESTINATION_ERROR", "", e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }

    return "/interestCenterPeas/jsp/" + destination;
  }

}

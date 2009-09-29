/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
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

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

package org.silverpeas.web.interests.servlets;

import org.silverpeas.core.pdc.interests.model.Interests;
import org.silverpeas.web.interests.control.InterestCenterSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.web.http.HttpRequest;

import java.util.List;

/**
 * MVC router to manage user interest center
 */
public class InterestCenterPeasRequestRouter
    extends ComponentRequestRouter<InterestCenterSessionController> {

  private static final long serialVersionUID = -6581146192028464533L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return an InterestCenterSessionController
   */
  public InterestCenterSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new InterestCenterSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for notificationUser, returns
   * "notificationUser"
   */
  public String getSessionControlBeanName() {
    return "interestCenterPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param icSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  public String getDestination(String function, InterestCenterSessionController icSC,
      HttpRequest request) {
    String destination = "";

    try {
      if (function.startsWith("newICenter")) {
        String newICName = request.getParameter("icName");
        destination = "newICenter.jsp";
        String action = request.getParameter("action");
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
        String newICName = request.getParameter("icName");
        request.setAttribute("icName", newICName);
        destination = "usedICenter.jsp";
      } else if (function.startsWith("iCenterList")) {
        String mode = request.getParameter("mode");
        if ("delete".equals(mode)) {
          Object o = request.getParameterValues("icCheck");
          if (o != null) {
            String[] iDs = (String[]) o;
            icSC.removeICByPKs(iDs);
          }
        }
        List<Interests> icList = icSC.getICByUserId();
        request.setAttribute("icList", icList);
        destination = "iCenterList.jsp";
      }
    } catch (Exception e) {
      SilverTrace.error("interestCenterPeas", "InterestCenterPeasRequestRouter.getDestination",
          "pdcPeas.EX_GET_DESTINATION_ERROR", "", e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }

    return "/interestCenterPeas/jsp/" + destination;
  }

}

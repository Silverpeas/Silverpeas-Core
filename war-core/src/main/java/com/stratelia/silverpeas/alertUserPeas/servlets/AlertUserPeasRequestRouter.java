/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.alertUserPeas.servlets;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.alertUserPeas.control.AlertUserPeasSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;

import javax.servlet.http.HttpServletRequest;

/**
 * Class declaration
 * @author
 */
public class AlertUserPeasRequestRouter extends
    ComponentRequestRouter<AlertUserPeasSessionController> {

  private static final long serialVersionUID = 5335551355656715989L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public AlertUserPeasSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new AlertUserPeasSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "alertUserPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param scc The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, AlertUserPeasSessionController scc,
      HttpServletRequest request) {
    String destination = "";
    SilverTrace.info("alertUserPeas", "getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Function=" + function);
    try {
      if (!StringUtil.isDefined(request.getCharacterEncoding())) {
        request.setCharacterEncoding("UTF-8");
      }
      if (function.equals("Main")) {
        scc.init();
        destination = getDestination("ToSelection", scc, request);
      } else if (function.startsWith("ToSelection")) // nav vers selectionPeas
      // pour choix users et
      // groupes
      {
        destination = scc.initSelection();
      } else if (function.startsWith("FromSelection")) // récupère les users
      // et
      // groupes selectionnés
      // au travers de
      // selectionPeas et les
      // place en session
      {
        scc.computeSelection();
        UserDetail[] userDetails = scc.getUserRecipients();
        Group[] groups = scc.getGroupRecipients();
        if ((userDetails.length > 0) || (groups.length > 0)) {
          request.setAttribute("UserR", userDetails);
          request.setAttribute("GroupR", groups);
          request.setAttribute("HostComponentName", scc.getHostComponentName());
          request.setAttribute("HostSpaceName", scc.getHostSpaceName());
          destination = "/alertUserPeas/jsp/writeMessage.jsp";
        } else {
          // No users or groups => clsoing the popup
          destination = getDestination("Close", scc, request);
        }
      } else if (function.startsWith("Close")) // fermeture de la fenêtre
      {
        destination = "/alertUserPeas/jsp/close.jsp";
      } else if (function.startsWith("ToAlert")) {
        request.setAttribute("HostComponentName", scc.getHostComponentName());
        request.setAttribute("HostSpaceName", scc.getHostSpaceName());
        String message = request.getParameter("messageAux");
        scc.prepareNotification(message);
        destination = "/alertUserPeas/jsp/sendMessage.jsp";
      } else if (function.startsWith("Notify")) // Notification
      {
        request.setAttribute("HostComponentName", scc.getHostComponentName());
        request.setAttribute("HostSpaceName", scc.getHostSpaceName());
        scc.sendNotification();
        destination = "/alertUserPeas/jsp/messageOk.jsp";
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("alertUserPeas", "getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }
}

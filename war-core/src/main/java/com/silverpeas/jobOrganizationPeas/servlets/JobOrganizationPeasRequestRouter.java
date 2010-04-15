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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.jobOrganizationPeas.servlets;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.jobOrganizationPeas.control.JobOrganizationPeasSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 * @author Thierry Leroi
 */
public class JobOrganizationPeasRequestRouter extends ComponentRequestRouter {

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new JobOrganizationPeasSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "jobOrganizationPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = "";
    JobOrganizationPeasSessionController jobOrganizationSC =
        (JobOrganizationPeasSessionController) componentSC;
    SilverTrace.info("jobOrganizationPeas",
        "JobOrganizationPeasRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + jobOrganizationSC.getUserId()
        + " Function=" + function);

    try {
      // 1) Performs the action
      // ----------------------
      if (function.startsWith("ViewUserOrGroup")) {
        // get user panel data
        jobOrganizationSC.retourSelectionPeas();
        destination = "/jobOrganizationPeas/jsp/jopUserView.jsp";
      } else {
        destination = jobOrganizationSC.initSelectionPeas();
      }

      // 2) Prepare the pages
      // --------------------
      if (destination.endsWith("jopUserView.jsp")) {
        request.setAttribute("infos", jobOrganizationSC.getCurrentInfos());
        request
            .setAttribute("groups", jobOrganizationSC.getCurrentUserGroups());
        request.setAttribute("spaces", jobOrganizationSC.getCurrentSpaces());
        request
            .setAttribute("profiles", jobOrganizationSC.getCurrentProfiles());
        request.setAttribute("userid", jobOrganizationSC.getCurrentUserId());
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("jobOrganizationPeas",
        "JobOrganizationPeasRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

}

/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.web.joborganization.servlets;

import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.web.joborganization.JobOrganizationPeasException;
import org.silverpeas.web.joborganization.control.JobOrganizationPeasSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.web.http.HttpRequest;

/**
 * Class declaration
 * @author Thierry Leroi
 */
public class JobOrganizationPeasRequestRouter extends
    ComponentRequestRouter<JobOrganizationPeasSessionController> {
  private static final long serialVersionUID = -3952939609496239407L;

  @Override
  public JobOrganizationPeasSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new JobOrganizationPeasSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "jobOrganizationPeas";
  }

  @Override
  public String getDestination(String function,
      JobOrganizationPeasSessionController jobOrganizationSC, HttpRequest request) {
    String destination = "";

    request.setAttribute("isRightCopyReplaceActivated",
        jobOrganizationSC.isRightCopyReplaceActivated());

    try {
      if ("Main".equals(function)) {
        destination = jobOrganizationSC.initSelectionUserOrGroup();
      } else if ("ViewUserOrGroup".equals(function)) {
        // get user panel data
        jobOrganizationSC.backSelectionUserOrGroup();
        destination = "/jobOrganizationPeas/jsp/jopUserView.jsp";
      } else if ("SelectRightsUserOrGroup".equals(function)) {
        destination = jobOrganizationSC.initSelectionRightsUserOrGroup();
      } else if (function.startsWith("AssignRights")) {
        if (!jobOrganizationSC.isRightCopyReplaceActivated()) {
          throwHttpForbiddenError();
        }

        assignRights(jobOrganizationSC, request);

        destination = "/jobOrganizationPeas/jsp/jopUserView.jsp";
      }

      if (destination.endsWith("jopUserView.jsp")) {
        // a user has been selected
        if (jobOrganizationSC.getCurrentUserId() != null) {
          request.setAttribute("userid", jobOrganizationSC.getCurrentUserId());
          request.setAttribute("user", jobOrganizationSC.getCurrentUser());
          request.setAttribute("userGroups", jobOrganizationSC.getCurrentUserGroups());
        } else if (jobOrganizationSC.getCurrentGroupId() != null) {
          // a group has been selected
          request.setAttribute("group", jobOrganizationSC.getCurrentGroup());
          request.setAttribute("superGroupName", jobOrganizationSC.getCurrentSuperGroupName());
        }
        request.setAttribute("isAdmin", UserAccessLevel.ADMINISTRATOR.equals(
            jobOrganizationSC.getUserAccessLevel()));
        request.setAttribute("spaces", jobOrganizationSC.getCurrentSpaces());
        request.setAttribute("profiles", jobOrganizationSC.getCurrentProfiles());
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private void assignRights(JobOrganizationPeasSessionController jobOrganizationSC,
      HttpRequest request) {
    //1 = replace rights | 2 = add rights
    String choiceAssignRights = request.getParameter("choiceAssignRights");
    String sourceRightsId = request.getParameter("sourceRightsId");
    //Set | Element
    String sourceRightsType = request.getParameter("sourceRightsType");
    //true | false
    boolean nodeAssignRights = request.getParameterAsBoolean("nodeAssignRights");

    try {
      jobOrganizationSC
          .assignRights(choiceAssignRights, sourceRightsId, sourceRightsType, nodeAssignRights);
      request.setAttribute("message", jobOrganizationSC.getString("JOP.assignRightsMessageOk"));
    } catch (JobOrganizationPeasException e) {
      SilverLogger.getLogger(this).error(e);
      request.setAttribute("message", jobOrganizationSC.getString("JOP.assignRightsMessageNOk"));
    }
  }

}
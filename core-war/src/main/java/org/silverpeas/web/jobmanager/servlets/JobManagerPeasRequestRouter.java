/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.jobmanager.servlets;

import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.AdminComponentRequestRouter;
import org.silverpeas.web.jobmanager.JobManagerService;
import org.silverpeas.web.jobmanager.control.JobManagerPeasSessionController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * Class declaration
 * @author
 */
public class JobManagerPeasRequestRouter extends
    AdminComponentRequestRouter<JobManagerPeasSessionController> {

  private static final long serialVersionUID = -2003485584890163789L;
  private static final String OPERATION_ACTION = "Operation";
  private static final String SERVICE_ACTION = "Service";
  private static final String ID_PARAMETER = "Id";

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   *
   */
  public JobManagerPeasSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new JobManagerPeasSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "jobManagerPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param jobManagerSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getAdminDestination(String function, JobManagerPeasSessionController jobManagerSC,
      HttpRequest request) {
    String destination;
    try {
      if (function.startsWith("Main")) {
        destination = homePage(jobManagerSC, request);
      } else if (function.startsWith("TopBarManager") || function.startsWith("Change")) {
        destination = headerPart(request, jobManagerSC, function);
      } else if (function.startsWith("SetMaintenanceMode")) {
        jobManagerSC.setAppModeMaintenance(Boolean.parseBoolean(request.getParameter("mode")));
        destination = getDestination("ManageMaintenanceMode", jobManagerSC,
            request);
      } else if (function.startsWith("ManageMaintenanceMode")) {
        boolean mode = jobManagerSC.isAppInMaintenance();
        request.setAttribute("mode", Boolean.toString(mode));
        destination = "/jobManagerPeas/jsp/manageMaintenance.jsp";
      } else {
        destination = "/jobManagerPeas/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }

  private String homePage(final JobManagerPeasSessionController jobManagerSC,
      final HttpRequest request) {
    String spaceId = request.getParameter("SpaceId");
    if (isDefined(spaceId)) {
      jobManagerSC.setDirectAccessToSpaceId(spaceId);
      jobManagerSC.changeServiceActif("1");
      jobManagerSC.changeOperationActif("12");
    } else {
      String service = request.getParameter(SERVICE_ACTION);
      if (isDefined(service)) {
        String operation = request.getParameter(OPERATION_ACTION);
        jobManagerSC.changeServiceActif(service);
        jobManagerSC.changeOperationActif(operation);
      }
    }
    this.setAttributes(request, jobManagerSC);
    return "/jobManagerPeas/jsp/jobManager.jsp";
  }

  private String headerPart(final HttpRequest request,
      final JobManagerPeasSessionController jmSC, final String function) {
    final String id = request.getParameter(ID_PARAMETER);
    if (isDefined(id)) {
      if (function.endsWith(SERVICE_ACTION)) {
        jmSC.changeServiceActif(id);
      } else if (function.endsWith(OPERATION_ACTION)) {
        String idOperation = request.getParameter(ID_PARAMETER);
        if ("15".equals(idOperation)) {
          boolean mode = jmSC.isAppInMaintenance();
          request.setAttribute("mode", Boolean.toString(mode));
        }
        jmSC.changeOperationActif(idOperation);
      }
    }
    setAttributes(request, jmSC);
    return "/jobManagerPeas/jsp/topBarManager.jsp";
  }

  private void setAttributes(HttpServletRequest request,
      JobManagerPeasSessionController jmpSC) {
    if (!isDefined(jmpSC.getIdServiceActif())) {
      // Setting default service if none defined
      jmpSC.changeServiceActif(jmpSC.getIdDefaultService());
    }
    // l'objet "Services" est la liste des services disponibles pour l'administrateur
    request.setAttribute("Services", jmpSC
        .getServices(JobManagerService.LEVEL_SERVICE));

    // l'objet "Operation" est la liste des opérations disponibles pour le service actif
    request.setAttribute(OPERATION_ACTION, jmpSC.getSubServices(jmpSC
        .getIdServiceActif()));

    // l'objet URL est un string representatnt l'operation active pour le service actif
    final UriBuilder url = UriBuilder
        .fromUri(jmpSC.getService(jmpSC.getIdOperationActif()).getUrl());
    if (isDefined(jmpSC.getDirectAccessToSpaceId())) {
      url.queryParam("SpaceId", jmpSC.getDirectAccessToSpaceId());
      jmpSC.setDirectAccessToSpaceId(null);
    }
    request.setAttribute("adminBodyUrl", url.build().toString());
  }
}

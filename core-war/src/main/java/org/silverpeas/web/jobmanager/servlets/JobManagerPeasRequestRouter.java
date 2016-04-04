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

package org.silverpeas.web.jobmanager.servlets;

import org.silverpeas.web.jobmanager.JobManagerService;
import org.silverpeas.web.jobmanager.control.JobManagerPeasSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * Class declaration
 * @author
 */
public class JobManagerPeasRequestRouter extends
    ComponentRequestRouter<JobManagerPeasSessionController> {

  private static final long serialVersionUID = -2003485584890163789L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
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
  public String getDestination(String function, JobManagerPeasSessionController jobManagerSC,
      HttpRequest request) {
    String destination = "";


    try {
      if (function.startsWith("Main")) {
        destination = "/jobManagerPeas/jsp/jobManager.jsp";
      } else if (function.startsWith("TopBarManager"))// lors du permier
      // accès=> via jobManager.jsp
      {
        // set le service actif par le service par defaut; active aussi une
        // opération par défaut pour ce service
        jobManagerSC.changeServiceActif(jobManagerSC.getIdDefaultService());
        destination = this.setAttributes(request, jobManagerSC);
      } else if (function.startsWith("ChangeService")) {
        String idService = request.getParameter("Id");
        // changer l'id représentant le service actif
        // set aussi l' idCurrentOperationActif pour ce service (à la vaeleur
        // par defaut ou la valeur précdente si existe
        jobManagerSC.changeServiceActif(idService);
        destination = this.setAttributes(request, jobManagerSC);
      } else if (function.startsWith("ChangeOperation")) {
        String idOperation = request.getParameter("Id");
        if ("15".equals(idOperation)) {
          Boolean mode = jobManagerSC.isAppInMaintenance();
          request.setAttribute("mode", mode.toString());
        }
        // changer l'id représentant l'opération active
        // set idCurrentOperationActif avec cette id
        jobManagerSC.changeOperationActif(idOperation);
        destination = this.setAttributes(request, jobManagerSC);
      }

      else if (function.startsWith("SetMaintenanceMode")) {
        jobManagerSC.setAppModeMaintenance(Boolean.parseBoolean(request.getParameter("mode")));
        destination = getDestination("ManageMaintenanceMode", jobManagerSC,
            request);
      } else if (function.startsWith("ManageMaintenanceMode")) {
        Boolean mode = jobManagerSC.isAppInMaintenance();
        request.setAttribute("mode", mode.toString());
        destination = "/jobManagerPeas/jsp/manageMaintenance.jsp";
      }

      else {
        destination = "/jobManagerPeas/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private String setAttributes(HttpServletRequest request,
      JobManagerPeasSessionController jmpSC) {
    // l'objet "Services" est la liste des services disponibles pour l'administrateur
    request.setAttribute("Services", jmpSC
        .getServices(JobManagerService.LEVEL_SERVICE));

    // l'objet "Operation" est la liste des opérations disponibles pour le service actif
    request.setAttribute("Operation", jmpSC.getSubServices(jmpSC
        .getIdServiceActif()));

    // l'objet URL est un string representatnt l'operation actice pour le service actif
    request.setAttribute("URL", (jmpSC.getService(jmpSC.getIdOperationActif())).getUrl());
    return "/jobManagerPeas/jsp/topBarManager.jsp";
  }
}

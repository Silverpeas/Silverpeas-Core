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

package com.silverpeas.jobDomainPeas.servlets;

import java.io.IOException;


import java.io.Writer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.jobOrganizationPeas.control.JobOrganizationPeasSessionController;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

public class JobDomainPeasComponentServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
	  HttpSession session = req.getSession(true);

	  JobOrganizationPeasSessionController sc = (JobOrganizationPeasSessionController) session.getAttribute("Silverpeas_" + "jobOrganizationPeas");
	  
	  String componentId = getComponentId(req);
	  String result = getComponentPath(sc, componentId);
    
	  Writer writer = resp.getWriter();
	  writer.write(result);
  }
  
  private String getComponentId(HttpServletRequest req) {
	  return req.getParameter("ComponentId");
  }

  private String getComponentPath(JobOrganizationPeasSessionController sc, String componentId) {
	  String componentPath = "";
	  
	  //Espace > Sous-espaces
	  AdminController adminController = sc.getAdminController();
	  List<SpaceInstLight> spaceList = adminController.getPathToComponent(componentId);
	  for (SpaceInstLight space : spaceList) {
		  componentPath += space.getName(sc.getLanguage()) + " > ";
	  }
	  //Composant
	  componentPath += adminController.getComponentInst(componentId).getLabel(sc.getLanguage());
	  return componentPath;
  }
}
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
package org.silverpeas.web.jobdomain.servlets;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.web.jobdomain.control.JobDomainPeasSessionController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class JobDomainPeasGroupPathServlet extends HttpServlet {

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

    JobDomainPeasSessionController sc =
        (JobDomainPeasSessionController) session.getAttribute("Silverpeas_" + "jobDomainPeas");

    String groupId = getGroupId(req);
    String result = getGroupPath(sc, groupId);

    Writer writer = resp.getWriter();
    writer.write(result);
  }

  private String getGroupId(HttpServletRequest req) {
    return req.getParameter("GroupId");
  }

  private String getGroupPath(JobDomainPeasSessionController sc, String groupId) {
    StringBuilder groupPath = new StringBuilder();

    OrganizationController orgaController = sc.getOrganisationController();
    Group group = orgaController.getGroup(groupId);
    String domainId = group.getDomainId();
    if (domainId == null) {
      domainId = "-1";
    }
    Domain domain = orgaController.getDomain(domainId);

    // nom du domaine
    if ("-1".equals(domainId)) {// domaine mixte
      groupPath.append(sc.getString("JDP.domainMixt"));
    } else {
      groupPath.append(domain.getName());
    }

    // nom du(des) groupe(s) pères
    List<String> groupList = orgaController.getPathToGroup(groupId);
    for (String elementGroupId : groupList) {
      groupPath.append(" > ").append(orgaController.getGroup(elementGroupId).getName());
    }

    // nom du groupe
    groupPath.append(" > ").append(group.getName());
    return groupPath.toString();
  }
}

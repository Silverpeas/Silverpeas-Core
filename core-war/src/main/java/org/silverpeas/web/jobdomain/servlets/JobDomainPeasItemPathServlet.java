/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.web.jobdomain.servlets;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.web.jobdomain.control.JobDomainPeasSessionController;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class JobDomainPeasItemPathServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final String SEPARATOR = " > ";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {

    HttpSession session = req.getSession(true);

    JobDomainPeasSessionController sc =
        (JobDomainPeasSessionController) session.getAttribute("Silverpeas_" + "jobDomainPeas");

    String groupId = req.getParameter("GroupId");
    String componentId = req.getParameter("ComponentId");
    String spaceId = req.getParameter("SpaceId");
    String result = "";
    if (StringUtil.isDefined(groupId)) {
      result = getGroupPath(sc, groupId);
    } else if (StringUtil.isDefined(spaceId)) {
      result = getSpacePath(sc, spaceId);
    } else if (StringUtil.isDefined(componentId)) {
      result = getComponentPath(sc, componentId);
    }
    try {
      Writer writer = resp.getWriter();
      writer.write(result);
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private String getGroupPath(JobDomainPeasSessionController sc, String groupId) {
    StringBuilder groupPath = new StringBuilder();

    OrganizationController oc = OrganizationController.get();
    Group group = Group.getById(groupId);
    Domain domain = oc.getDomain(group.getDomainId());

    // nom du domaine
    if (domain.isMixedOne()) {
      groupPath.append(sc.getString("JDP.domainMixt"));
    } else {
      groupPath.append(domain.getName());
    }

    // nom du(des) groupe(s) pères
    List<String> groupList = oc.getPathToGroup(groupId);
    for (String elementGroupId : groupList) {
      groupPath.append(SEPARATOR).append(Group.getById(elementGroupId).getName());
    }

    // nom du groupe
    groupPath.append(SEPARATOR).append(group.getName());
    return groupPath.toString();
  }

  private String getComponentPath(JobDomainPeasSessionController sc, String componentId) {
    StringBuilder componentPath = new StringBuilder();

    OrganizationController oc = OrganizationController.get();

    List<SpaceInstLight> spaceList = oc.getPathToComponent(componentId);
    for (SpaceInstLight space : spaceList) {
      componentPath.append(space.getName(sc.getLanguage())).append(SEPARATOR);
    }

    componentPath.append(oc.getComponentInstLight(componentId).getLabel(sc.getLanguage()));
    return componentPath.toString();
  }

  private String getSpacePath(JobDomainPeasSessionController sc, String spaceId) {
    StringBuilder path = new StringBuilder();

    OrganizationController oc = OrganizationController.get();

    // Espace > Sous-espaces
    List<SpaceInstLight> spaceList = oc.getPathToSpace(spaceId);
    for (SpaceInstLight space : spaceList) {
      if (path.length() > 0) {
        path.append(SEPARATOR);
      }
      path.append(space.getName(sc.getLanguage()));
    }

    return path.toString();
  }

}
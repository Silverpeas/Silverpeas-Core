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

package org.silverpeas.web.jobdomain.servlets;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.silverpeas.web.jobdomain.control.JobDomainPeasSessionController;
import org.silverpeas.core.admin.user.model.UserDetail;

public class JobDomainPeasAjaxServlet extends HttpServlet {

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

    String action = getAction(req);
    String result = "";

    if ("CheckUser".equals(action)) {
      result = checkUser(req, sc);
    }

    Writer writer = resp.getWriter();
    writer.write(result);
  }

  private String getAction(HttpServletRequest req) {
    return req.getParameter("Action");
  }

  private String checkUser(HttpServletRequest req, JobDomainPeasSessionController sc) {
    UserDetail userToCheck = new UserDetail();
    userToCheck.setFirstName(req.getParameter("FirstName"));
    userToCheck.setLastName(req.getParameter("LastName"));
    userToCheck.seteMail(req.getParameter("Email"));
    UserDetail user = sc.checkUser(userToCheck);

    if (user == null) {
      return "ok";
    } else {
      return "nok";
    }
  }
}
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

package com.silverpeas.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;

public class MailExistsServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    processRequest(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html");
    PrintWriter out = resp.getWriter();

    String email = req.getParameter("email");
    if (StringUtil.isDefined(email)) {
      Admin admin = AdminReference.getAdminService();
      try {
        if ( admin.isEmailExisting(email) ) {
          out.print("MailExists");
        }
      } catch (AdminException e) {
        SilverTrace.error("admin", "MailExistsServlet", "root.CANT_CHECK_EMAIL", "email : "+email, e );
      }
    }
  }

}

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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.agenda.servlets;

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.calendar.ical.ExportIcalManager;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SubscribeAgenda extends HttpServlet {

  private static final long serialVersionUID = -7864790793422182001L;

  @Inject
  private AdminController adminController;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    String userId = getUserId(req);
    String login = getLogin(req);
    String password = getPassword(req);
    try {
      // Check login/pwd must be a identified user
      UserFull user = adminController.getUserFull(userId);
      if (user != null && login.equals(user.getLogin())
          && password.equals(user.getPassword())) {
        // Get calendar user
        ExportIcalManager exportManager = new ExportIcalManager(userId);
        String filePath = exportManager.exportIcalAgendaForSynchro();
        res.setContentType("text/calendar");
        res.setHeader("Content-Disposition", "attachment;filename=calendar"
            + userId + ".ics");
        try (OutputStream os = res.getOutputStream();
             FileInputStream fs = new FileInputStream(filePath)) {
          // Stream data back to the client
          int i;
          while (((i = fs.read()) != -1)) {
            os.write(i);
          }
          os.flush();
          res.getOutputStream();
        }
      } else {
        objectNotFound(res);
      }
    } catch (Exception e) {
      objectNotFound(res);
    }

  }

  private String getUserId(HttpServletRequest request) {
    return request.getParameter("userId");
  }

  private String getLogin(HttpServletRequest request) {
    return request.getParameter("login");
  }

  private String getPassword(HttpServletRequest request) {
    return request.getParameter("password");
  }

  private boolean isUserLoggedIn() {
    return (UserDetail.getCurrentRequester() != null);
  }

  private void objectNotFound(HttpServletResponse res) {
    boolean isLoggedIn = isUserLoggedIn();
    try {
      if (!isLoggedIn) {
        res.sendRedirect("/weblib/notFound.html");
      } else {
        res.sendRedirect(URLUtil.getApplicationURL() + "/admin/jsp/documentNotFound.jsp");
      }
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

}

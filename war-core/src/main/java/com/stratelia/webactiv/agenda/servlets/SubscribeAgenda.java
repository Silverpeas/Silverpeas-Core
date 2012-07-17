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

package com.stratelia.webactiv.agenda.servlets;

import com.silverpeas.ical.ExportIcalManager;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.UserFull;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class SubscribeAgenda extends HttpServlet {

  private static final long serialVersionUID = -7864790793422182001L;
  HttpSession session;
  PrintWriter out;

  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    SilverTrace.info("agenda", "SubscribeAgenda.doPost",
        "root.MSG_GEN_ENTER_METHOD");
    String userId = getUserId(req);
    String login = getLogin(req);
    String password = getPassword(req);
    FileInputStream fs = null;
    try {
      SilverTrace.info("agenda", "SubscribeAgenda.doPost",
          "root.MSG_GEN_PARAM_VALUE", "userId = " + userId);

      // Check login/pwd must be a identified user
      AdminController adminController = new AdminController(null);
      UserFull user = adminController.getUserFull(userId);
      if (user != null && login.equals(user.getLogin())
          && password.equals(user.getPassword())) {
        // Get calendar user
        ExportIcalManager exportManager = new ExportIcalManager(userId);
        String filePath = exportManager.exportIcalAgendaForSynchro();
        res.setContentType("text/calendar");
        res.setHeader("Content-Disposition", "attachment;filename=calendar"
            + userId + ".ics");
        OutputStream os = res.getOutputStream();
        fs = new FileInputStream(filePath);
        // Stream data back to the client
        int i;
        while (((i = fs.read()) != -1)) {
          os.write(i);
        }
        os.flush();
        os.close();
        res.getOutputStream();
      } else {
        objectNotFound(req, res);
      }
    } catch (Exception e) {
      objectNotFound(req, res);
    } finally {
      IOUtils.closeQuietly(fs);
    }
    SilverTrace.info("agenda", "SubscribeAgenda.doPost",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public String getServerURL(AdminController admin, String domainId) {
    Domain defaultDomain = admin.getDomain(domainId);
    return defaultDomain.getSilverpeasServerURL();
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

  private MainSessionController getMainSessionController(HttpServletRequest req) {
    HttpSession session = req.getSession(true);
    return (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  }

  private boolean isUserLogin(HttpServletRequest req) {
    return (getMainSessionController(req) != null);
  }

  private void objectNotFound(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    boolean isLoggedIn = isUserLogin(req);
    if (!isLoggedIn) {
      res.sendRedirect("/weblib/notFound.html");
    } else {
      res.sendRedirect(URLManager.getApplicationURL() + "/admin/jsp/documentNotFound.jsp");
    }
  }

}

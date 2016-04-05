/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.directory.servlets;

import org.silverpeas.web.directory.control.DirectorySessionController;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.owasp.encoder.Encode;
import org.silverpeas.core.util.JSONCodec;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;

public class DirectoryJSONServlet extends HttpServlet {

  private static final long serialVersionUID = 3351692741655439410L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    HttpSession session = req.getSession(true);
    DirectorySessionController dsc = (DirectorySessionController) session.getAttribute(
        "Silverpeas_directory");
    if (dsc == null) {
      MainSessionController msc = (MainSessionController) session.getAttribute(
          MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      if (msc != null) {
        dsc = new DirectorySessionController(msc, msc.createComponentContext(null, null));
      }
    }
    res.setContentType("application/json");
    String action = req.getParameter("Action");
    Writer writer = res.getWriter();
    String jsonStatus = "";
    if ("SendMessage".equals(action) && dsc != null) {
      try {
        UserRecipient[] selectedUsers = new UserRecipient[1];
        selectedUsers[0] = new UserRecipient(req.getParameter("TargetUserId"));
        String title = req.getParameter("Title");
        String message = Encode.forHtml(req.getParameter("Message"));
        dsc.sendMessage(null, title, message, selectedUsers);
        jsonStatus = JSONCodec.encodeObject(o -> o.put("success", true));
      } catch (NotificationManagerException ex) {
        SilverTrace.error("directory", "DirectoryRequestRouter.sendMessage", "ERROR", ex);
        jsonStatus =
            JSONCodec.encodeObject(o -> o.put("success", false).put("error", ex.toString()));
      }
    }
    writer.write(jsonStatus);
  }
}

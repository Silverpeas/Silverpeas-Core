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
package org.silverpeas.web.socialnetwork.myprofil.servlets;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasHttpServlet;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.web.socialnetwork.myprofil.control.SocialNetworkService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class JSONServlet extends SilverpeasHttpServlet {

  private static final long serialVersionUID = -843491398398079951L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      HttpSession session = request.getSession();
      MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(
          MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      StatusEntity status = new StatusEntity();
      if (mainSessionCtrl == null) {
        PrintWriter out = response.getWriter();
        status.setStatus("silverpeastimeout");
        out.println(JSONCodec.encode(status));
        return;
      }
      if (User.getCurrentRequester().isAnonymous() || User.getCurrentRequester().isAccessGuest()) {
        throwHttpForbiddenError("anonymous or guest user cannot access my profil features");
      }
      String userId = mainSessionCtrl.getUserId();
      StatusEntity newStatus = JSONCodec.decode(request.getInputStream(), StatusEntity.class);
      SocialNetworkService socialNetworkService = new SocialNetworkService(userId);
      // if status is empty or set with a text, update it (an empty status means no status)
      if (newStatus.isDefined()) {
        status.setStatus(socialNetworkService.changeStatus(newStatus.getStatus()));
      } else {
        // if status equal null don't do update status and do get Last status
        String lastStatus = Encode.forHtml(socialNetworkService.getLastStatus());
        status.setStatus(lastStatus);
      }
      PrintWriter out = response.getWriter();
      out.println(JSONCodec.encode(status));
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  public static class StatusEntity {
    private String status;

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public boolean isDefined() {
      return status != null;
    }
  }
}

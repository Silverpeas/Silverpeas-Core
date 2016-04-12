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

package org.silverpeas.web.communicationuser.servlets;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;

import org.silverpeas.web.communicationuser.CommunicationUserException;
import org.silverpeas.web.communicationuser.control.CommunicationUserSessionController;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.DateUtil;

public class AjaxCommunicationUserServlet extends HttpServlet {
  private static final long serialVersionUID = 5706201637719833762L;

  @Override
  public void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    HttpSession session = req.getSession(true);

    CommunicationUserSessionController commUserSC = (CommunicationUserSessionController) session
        .getAttribute("Silverpeas_" + "communicationUser");
    if (commUserSC != null) {
      String currentUserId = commUserSC.getUserId();

      String action = req.getParameter("Action");
      String userId = req.getParameter("UserIdDest");
      String userId1, userId2;

      Collection<File> listCurrentDiscussion = commUserSC.getListCurrentDiscussion();
      Iterator<File> it = listCurrentDiscussion.iterator();
      File fileDiscussion = null;
      String fileName;
      boolean trouve = false;
      while (it.hasNext() && !trouve) {
        fileDiscussion = it.next();
        fileName = fileDiscussion.getName(); // userId1.userId2.txt
        userId1 = fileName.substring(0, fileName.indexOf('.'));
        userId2 = fileName.substring(fileName.indexOf('.') + 1, fileName.lastIndexOf('.'));
        trouve = ((userId.equals(userId1) && currentUserId.equals(userId2))
            || (userId.equals(userId2) && currentUserId.equals(userId1)));
      }

      if (!trouve) {
        throw new IOException("Fichier de discussion non trouvé !!");
      }
      // Post
      if ("Post".equals(action)) {
        String message = req.getParameter("Msg");
        message = URLDecoder.decode(message, "ISO-8859-1");
        // on client side, javascript function espace do not escape + character.
        // It is manually encoded.
        // So, on server side, it must be decoded manually too !
        message = message.replaceAll("%2B", "+");

        Date now = new Date();
        try {
          commUserSC.addMessageDiscussion(fileDiscussion, "["
              + DateUtil.dateToString(now, commUserSC.getLanguage()) + " "
              + DateUtil.getFormattedTime(now) + "] <"
              + commUserSC.getUserDetail().getDisplayedName() + "> " + message);
        } catch (CommunicationUserException e) {
          throw new IOException(e.getMessage());
        }
        commUserSC.notifySession(userId, message);
      }

      // Get
      else if ("Get".equals(action)) {
        res.setContentType("text/xml");
        res.setHeader("charset", "UTF-8");

        Writer writer = res.getWriter();
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.write("<ajax-response>");
        writer.write("<response type=\"element\" id=\"message\">");
        writer.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");

        /* lecture du contenu du fichier */
        List<String> lines = FileUtils.readLines(fileDiscussion, "UTF-8");
        userId1 = "<" + commUserSC.getUserDetail().getDisplayedName() + ">"; // <currentUser>
        userId2 = "<" + commUserSC.getUserDetail(userId).getDisplayedName() + ">"; // <User dest>
        String color = "#009900";

        for (String line : lines) {
          writer.write("<tr>");
          if (line.indexOf(userId1) > -1) {
            color = "#009900";
          } else if (line.indexOf(userId2) > -1) {
            color = "#cc6600";
          }
          writer.write("<td valign=\"top\"><font color=\"" + color + "\">"
              + EncodeHelper.escapeXml(line) + "</font></td>");
          writer.write("</tr>");
        }
        writer.write("</table>");
        writer.write("</response>");
        writer.write("</ajax-response>");
      }
      // Clear
      else if ("Clear".equals(action)) {
        try {
          commUserSC.clearDiscussion(fileDiscussion);
        } catch (CommunicationUserException e) {
          throw new IOException(e.getMessage());
        }
        res.setContentType("text/xml");
        res.setHeader("charset", "UTF-8");
        Writer writer = res.getWriter();
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.write("<ajax-response>");
        writer.write("<response type=\"element\" id=\"message\">");
        writer.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
        writer.write("</table>");
        writer.write("</response>");
        writer.write("</ajax-response>");
      }
    }
  }
}
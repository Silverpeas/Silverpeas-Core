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
package org.silverpeas.web.templatedesigner.servlets;

import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.web.templatedesigner.control.TemplateDesignerSessionController;
import org.silverpeas.web.templatedesigner.model.TemplateDesignerException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;

public class TemplateDesignerAJAXServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) {
    HttpSession session = req.getSession(true);

    TemplateDesignerSessionController designer =
      (TemplateDesignerSessionController) session.getAttribute("Silverpeas_TemplateDesigner");

    String result = "nok";

    if (designer != null) {
      String[] values = req.getParameterValues("form-row[]");
      try {
        designer.sortFields(values);
        result = "ok";
      } catch (TemplateDesignerException e) {
        result = e.getMessage();
      }
    }

    try {
      Writer writer = res.getWriter();
      writer.write(result);
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
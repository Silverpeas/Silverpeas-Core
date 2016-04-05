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

package org.silverpeas.web.pdc.servlets;

import org.silverpeas.core.pdc.form.displayers.PdcFieldDisplayer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.silverpeas.core.util.MimeTypes.SERVLET_HTML_CONTENT_TYPE;

/**
 * Servlet used in Ajax mode to update the positions of a PDC field.
 * @author ahedin
 */
public class PdcAjaxServlet extends HttpServlet {

  private static final long serialVersionUID = 2363812622945071639L;

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request The HTPP request.
   * @param response The HTTP response.
   * @throws ServletException if a servlet-specific error occurs.
   * @throws IOException if an I/O error occurs.
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String fieldName = request.getParameter("fieldName");
    String positions = request.getParameter("positions");
    String language = request.getParameter("language");
    response.setContentType(SERVLET_HTML_CONTENT_TYPE);
    PrintWriter out = response.getWriter();

    PdcFieldDisplayer displayer = new PdcFieldDisplayer();
    String content = displayer.getPositionsDivContent(fieldName, positions, language);

    try {
      out.println(content);
    } finally {
      out.close();
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

}

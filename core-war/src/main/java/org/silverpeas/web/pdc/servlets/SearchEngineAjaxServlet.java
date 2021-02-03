/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.web.pdc.servlets;

import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.web.pdc.control.PdcSearchSessionController;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;

/**
 * Servlet used in Ajax mode to update the positions of a PDC field.
 * @author ebonnet
 */
public class SearchEngineAjaxServlet extends HttpServlet {

  private static final long serialVersionUID = 6192145328904954694L;

  private static final String PDC_SESSION_CONTROLLER_ATTRIBUTE_NAME = "Silverpeas_pdcSearch";
  private static final String ACTION_MARK_AS_READ = "markAsRead";

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param req The HTTP request.
   * @param resp The HTTP response.
   */
  protected void processRequest(HttpServletRequest req, HttpServletResponse resp) {

    String action = getAction(req);

    String result;
    if (ACTION_MARK_AS_READ.equals(action)) {
      result = markAsRead(req);
    } else {
      result = "{success:false, message:'Unknown action servlet'}";
    }

    resp.setContentType("text");
    resp.setHeader("charset", "UTF-8");

    // Send response
    try {
      Writer writer = resp.getWriter();
      writer.write(result);
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    processRequest(request, response);
  }

  /**
   * @param req the HttpServletRequest request
   * @return "Action" request parameter
   */
  private String getAction(HttpServletRequest req) {
    return req.getParameter("Action");
  }

  private String markAsRead(HttpServletRequest req) {
    HttpSession session = req.getSession(true);
    PdcSearchSessionController pdcSC =
        (PdcSearchSessionController) session.getAttribute(PDC_SESSION_CONTROLLER_ATTRIBUTE_NAME);
    PdcSearchRequestRouterHelper.markResultAsRead(pdcSC, req);
    // prepare JSON response
    String sId = req.getParameter("id");
    return JSONCodec.encodeObject(jsonResult -> jsonResult.put("success", true).put("id", sId));
  }
}

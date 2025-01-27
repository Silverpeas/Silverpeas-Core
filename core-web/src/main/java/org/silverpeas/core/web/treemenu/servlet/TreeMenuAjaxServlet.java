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
package org.silverpeas.core.web.treemenu.servlet;

import org.silverpeas.core.web.treemenu.process.TreeHandler;
import org.silverpeas.kernel.util.StringUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.silverpeas.core.web.treemenu.model.MenuConstants.DEFAULT_MENU_TYPE;
import static org.silverpeas.core.web.treemenu.model.MenuConstants.REQUEST_KEY_MENU_TYPE;

/**
 * It builds the requested menu made up of a tree of hierachical resources. Those resources can
 * be kinds: both Silverpeas spaces and component instances, hierachical topics in a component
 * instance, and so one.
 */
public class TreeMenuAjaxServlet extends HttpServlet {

  private static final long serialVersionUID = 208151209879380329L;

  /**
   * Processes requests to build the tree of Silverpeas resources that will be sent back in JSON.
   * @param request servlet request
   * @param response servlet response
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json");
    try (PrintWriter out = response.getWriter()) {
      // building of the father of menu items to display
      // get information from request
      String menuType = request.getParameter(REQUEST_KEY_MENU_TYPE);
      if (!StringUtil.isDefined(menuType)) {
        menuType = (String) request.getAttribute(REQUEST_KEY_MENU_TYPE);
        if (!StringUtil.isDefined(menuType)) {
          menuType = DEFAULT_MENU_TYPE;
        }
      }
      out.write(TreeHandler.processMenu(request, menuType, true));
    }
  }

  /**
   * Handles the HTTP <code>GET</code> method.
   * @param request servlet request
   * @param response servlet response
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      processRequest(request, response);
    } catch (Exception e) {
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      } catch (IOException ex) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  /**
   * Returns a short description of the servlet.
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "return a tree of resources in JSON";
  }

}

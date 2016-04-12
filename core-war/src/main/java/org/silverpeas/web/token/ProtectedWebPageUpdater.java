/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.web.token;

import org.silverpeas.core.web.token.SynchronizerTokenService;
import org.silverpeas.core.web.token.TokenSettingTemplate;
import org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationsOfCreationAreaTag;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.web.token.TokenSettingTemplate.Parameter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.web.token.SynchronizerTokenService.NAVIGATION_TOKEN_KEY;
import static org.silverpeas.core.web.token.SynchronizerTokenService.SESSION_TOKEN_KEY;

/**
 * A setter of security tokens in the web pages, so any requests emitted from them will be validated
 * and trusted.
 *
 * @author mmoquillon
 */
public class ProtectedWebPageUpdater extends HttpServlet {

  private static final long serialVersionUID = -607735276296383075L;

  @Inject
  private SynchronizerTokenService tokenService;

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
    response.setContentType("application/javascript");
    PrintWriter out = response.getWriter();
    try {
      String script = applyTemplate(new TokenSettingTemplate(), request);
      out.println(script);
    } finally {
      out.close();
    }
  }

  protected String applyTemplate(TokenSettingTemplate template, HttpServletRequest request) {
    String result = "";
    List<Parameter> parameters = new ArrayList<Parameter>(4);
    Token token = tokenService.getSessionToken(request);
    if (token.isDefined()) {
      parameters.add(new TokenSettingTemplate.Parameter(
          TokenSettingTemplate.CREATION_MENU_CONTAINER_ID,
          OperationsOfCreationAreaTag.CREATION_AREA_ID));
      parameters.add(new TokenSettingTemplate.Parameter(
          TokenSettingTemplate.SESSION_TOKEN_NAME_PARAMETER, SESSION_TOKEN_KEY));
      parameters.add(
          new TokenSettingTemplate.Parameter(TokenSettingTemplate.SESSION_TOKEN_VALUE_PARAMETER, token.getValue()));
    }
    token = tokenService.getNavigationToken(request);
    if (token.isDefined()) {
      parameters.add(new TokenSettingTemplate.Parameter(
          TokenSettingTemplate.CREATION_MENU_CONTAINER_ID,
          OperationsOfCreationAreaTag.CREATION_AREA_ID));
      parameters.add(new TokenSettingTemplate.Parameter(
          TokenSettingTemplate.NAVIGATION_TOKEN_NAME_PARAMETER, NAVIGATION_TOKEN_KEY));
      parameters.add(new TokenSettingTemplate.Parameter(
          TokenSettingTemplate.NAVIGATION_TOKEN_VALUE_PARAMETER, token.getValue()));
    }
    if (!parameters.isEmpty()) {
      result = template.apply(parameters);
    }
    return result;
  }

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Javascript generator for setting the security tokens into the web pages";
  }

}

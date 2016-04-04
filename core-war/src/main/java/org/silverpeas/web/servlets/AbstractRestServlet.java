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

package org.silverpeas.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractRestServlet extends HttpServlet {

  private static final long serialVersionUID = -8272551004360184500L;

  /**
   * The path element in the request used to call the servlet.This method might be overriden for
   * each instance - otherwise it returns the initialization parameter "request_path".
   * @return the path element in the request used to call the servlet.
   */
  protected String getServletRequestPath() {
    return getInitParameter("request_path");
  }

  protected void service(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    RestRequest restRequest = new RestRequest(request, getServletRequestPath());
    switch (restRequest.getAction()) {
      case CREATE:
        create(restRequest, response);
        break;
      case UPDATE:
        update(restRequest, response);
        break;
      case DELETE:
        delete(restRequest, response);
        break;
      case FIND:
      default:
        find(restRequest, response);
        break;
    }
  }

  /**
   * Dispatches client requests to the protected delete method if it is a deletion. This method must
   * be to overriden.
   * @param request the rest request.
   * @param response - the HttpServletResponse object that contains the response the servlet returns
   * to the client
   * @throws ServletException - if the HTTP request cannot be handled.
   * @throws IOException - if an input or output error occurs while the servlet is handling the HTTP
   * request .
   */
  protected abstract void delete(RestRequest request,
      HttpServletResponse response) throws ServletException, IOException;

  /**
   * Dispatches client requests to the protected create method if it is a creation. This method must
   * be to overriden.
   * @param request the rest request.
   * @param response - the HttpServletResponse object that contains the response the servlet returns
   * to the client
   * @throws ServletException - if the HTTP request cannot be handled.
   * @throws IOException - if an input or output error occurs while the servlet is handling the HTTP
   * request .
   */
  protected abstract void create(RestRequest request,
      HttpServletResponse response) throws ServletException, IOException;

  /**
   * Dispatches client requests to the protected update method if it is an update. This method must
   * be to overriden.
   * @param request the rest request.
   * @param response - the HttpServletResponse object that contains the response the servlet returns
   * to the client
   * @throws ServletException - if the HTTP request cannot be handled.
   * @throws IOException - if an input or output error occurs while the servlet is handling the HTTP
   * request .
   */
  protected abstract void update(RestRequest request,
      HttpServletResponse response) throws ServletException, IOException;

  /**
   * Dispatches client requests to the protected find method if it is a search. This method must be
   * to overriden.
   * @param request the rest request.
   * @param response - the HttpServletResponse object that contains the response the servlet returns
   * to the client
   * @throws ServletException - if the HTTP request cannot be handled.
   * @throws IOException - if an input or output error occurs while the servlet is handling the HTTP
   * request .
   */
  protected abstract void find(RestRequest request, HttpServletResponse response)
      throws ServletException, IOException;
}

package com.silverpeas.util.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractRestServlet extends HttpServlet {

  /**
   * The path element in the request used to call the servlet.This method might
   * be overriden for each instance - otherwise it returns the initialization
   * parameter "request_path".
   * 
   * @return the path element in the request used to call the servlet.
   */
  protected String getServletRequestPath() {
    return getInitParameter("request_path");
  }

  protected void service(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    RestRequest restRequest = new RestRequest(request, getServletRequestPath());
    switch (restRequest.getAction()) {
    case RestRequest.CREATE:
      create(restRequest, response);
      break;
    case RestRequest.UPDATE:
      update(restRequest, response);
      break;
    case RestRequest.DELETE:
      delete(restRequest, response);
      break;
    case RestRequest.FIND:
    default:
      find(restRequest, response);
      break;
    }
  }

  /**
   * Dispatches client requests to the protected delete method if it is a
   * deletion. This method must be to overriden.
   * 
   * @param request
   *          the rest request.
   * @param response
   *          - the HttpServletResponse object that contains the response the
   *          servlet returns to the client
   * @throws ServletException
   *           - if the HTTP request cannot be handled.
   * @throws IOException
   *           - if an input or output error occurs while the servlet is
   *           handling the HTTP request .
   */
  protected abstract void delete(RestRequest request,
      HttpServletResponse response) throws ServletException, IOException;

  /**
   * Dispatches client requests to the protected create method if it is a
   * creation. This method must be to overriden.
   * 
   * @param request
   *          the rest request.
   * @param response
   *          - the HttpServletResponse object that contains the response the
   *          servlet returns to the client
   * @throws ServletException
   *           - if the HTTP request cannot be handled.
   * @throws IOException
   *           - if an input or output error occurs while the servlet is
   *           handling the HTTP request .
   */
  protected abstract void create(RestRequest request,
      HttpServletResponse response) throws ServletException, IOException;

  /**
   * Dispatches client requests to the protected update method if it is an
   * update. This method must be to overriden.
   * 
   * @param request
   *          the rest request.
   * @param response
   *          - the HttpServletResponse object that contains the response the
   *          servlet returns to the client
   * @throws ServletException
   *           - if the HTTP request cannot be handled.
   * @throws IOException
   *           - if an input or output error occurs while the servlet is
   *           handling the HTTP request .
   */
  protected abstract void update(RestRequest request,
      HttpServletResponse response) throws ServletException, IOException;

  /**
   * Dispatches client requests to the protected find method if it is a search.
   * This method must be to overriden.
   * 
   * @param request
   *          the rest request.
   * @param response
   *          - the HttpServletResponse object that contains the response the
   *          servlet returns to the client
   * @throws ServletException
   *           - if the HTTP request cannot be handled.
   * @throws IOException
   *           - if an input or output error occurs while the servlet is
   *           handling the HTTP request .
   */
  protected abstract void find(RestRequest request, HttpServletResponse response)
      throws ServletException, IOException;
}

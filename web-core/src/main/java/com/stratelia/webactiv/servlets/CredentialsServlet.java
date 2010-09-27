/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.servlets;

import com.stratelia.webactiv.servlets.credentials.ChangePasswordHandler;
import com.stratelia.webactiv.servlets.credentials.ChangeQuestionHandler;
import com.stratelia.webactiv.servlets.credentials.EffectiveChangePasswordHandler;
import com.stratelia.webactiv.servlets.credentials.ForcePasswordChangeHandler;
import com.stratelia.webactiv.servlets.credentials.ForgotPasswordHandler;
import com.stratelia.webactiv.servlets.credentials.FunctionHandler;
import com.stratelia.webactiv.servlets.credentials.LoginQuestionHandler;
import com.stratelia.webactiv.servlets.credentials.ResetLoginPasswordHandler;
import com.stratelia.webactiv.servlets.credentials.ResetPasswordHandler;
import com.stratelia.webactiv.servlets.credentials.SendMessageHandler;
import com.stratelia.webactiv.servlets.credentials.ValidationAnswerHandler;
import com.stratelia.webactiv.servlets.credentials.ValidationQuestionHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller tier for credential management (Used by LoginFilter)
 * @author Ludovic Bertin
 */
public class CredentialsServlet extends HttpServlet {

  private static final long serialVersionUID = -7586840606648226466L;
  private Map<String, FunctionHandler> handlers = new HashMap<String, FunctionHandler>();

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    initHandlers();
  }



  /**
   * Load mapping between functions and associated handlers
   */
  private synchronized void initHandlers() {
    //Password change management
    handlers.put("ForcePasswordChange", new ForcePasswordChangeHandler());
    handlers.put("EffectiveChangePassword", new EffectiveChangePasswordHandler());
    handlers.put("ChangeQuestion", new ChangeQuestionHandler());
    handlers.put("ValidateQuestion", new ValidationQuestionHandler());
    handlers.put("LoginQuestion", new LoginQuestionHandler());
    handlers.put("ValidateAnswer", new ValidationAnswerHandler());
    handlers.put("ChangePassword", new ChangePasswordHandler());
    // Password reset management
    handlers.put("ForgotPassword", new ForgotPasswordHandler());
    handlers.put("ResetPassword", new ResetPasswordHandler());
    handlers.put("ResetLoginPassword", new ResetLoginPasswordHandler());
    handlers.put("SendMessage", new SendMessageHandler());
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String function = getFunction(request);
    FunctionHandler handler = handlers.get(function);
    if (handler != null) {
      String destinationPage = handler.doAction(request);
      RequestDispatcher dispatcher = request.getRequestDispatcher(destinationPage);
      dispatcher.forward(request, response);
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "command not found : " + function);
    }
  }

  /**
   * Retrieves function from path info.
   * @param request HTTP request
   * @return the function as a String
   */
  private String getFunction(HttpServletRequest request) {
    String function = "Error";
    String pathInfo = request.getPathInfo();
    if (pathInfo != null) {
      pathInfo = pathInfo.substring(1); // remove first '/'
      function = pathInfo.substring(pathInfo.indexOf("/") + 1);
    }
    return function;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }
}

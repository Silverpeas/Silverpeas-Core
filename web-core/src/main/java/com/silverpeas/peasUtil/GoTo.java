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
package com.silverpeas.peasUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public abstract class GoTo extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    SilverTrace.info("peasUtil", "GoTo.doPost", "root.MSG_GEN_ENTER_METHOD");
    String id = getObjectId(req);

    try {
      SilverTrace.info("peasUtil", "GoTo.doPost", "root.MSG_GEN_PARAM_VALUE",
          "id = " + id);

      String redirect = getDestination(id, req, res);
      if (redirect == null || "".equals(redirect)) {
        objectNotFound(req, res);
      } else {
        if (res.isCommitted()) {
          // La réponse a déjà été envoyée
        } else {
          if (redirect == null || !redirect.startsWith("http")) {
            redirect = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")
                + "/autoRedirect.jsp?" + redirect;
          }
          res.sendRedirect(redirect);
        }
      }
    } catch (AccessForbiddenException afe) {
      accessForbidden(req, res);
    } catch (Exception e) {
      objectNotFound(req, res);
    }
  }

  public abstract String getDestination(String objectId,
      HttpServletRequest req, HttpServletResponse res) throws Exception;

  private void objectNotFound(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    boolean isLoggedIn = isUserLogin(req);
    if (!isLoggedIn) {
      res.sendRedirect("/weblib/notFound.html");
    } else {
      res.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")
          + "/admin/jsp/documentNotFound.jsp");
    }
  }

  private void accessForbidden(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    res.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")
        + "/admin/jsp/accessForbidden.jsp");
  }

  public String getObjectId(HttpServletRequest request) {
    String pathInfo = request.getPathInfo();
    if (pathInfo != null) {
      return pathInfo.substring(1);
    }
    return null;
  }

  public boolean isUserLogin(HttpServletRequest req) {
    return (getMainSessionController(req) != null);
  }

  // check if the user is allowed to access the required component
  public boolean isUserAllowed(HttpServletRequest req, String componentId) {
    MainSessionController mainSessionCtrl = getMainSessionController(req);
    boolean isAllowed = false;
    if (componentId == null) { // Personal space
      isAllowed = true;
    } else {
      return mainSessionCtrl.getOrganizationController().isComponentAvailable(componentId,
          mainSessionCtrl.getUserId());
    }
    return isAllowed;
  }

  private MainSessionController getMainSessionController(HttpServletRequest req) {
    HttpSession session = req.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
    return mainSessionCtrl;
  }

  public String getUserId(HttpServletRequest req) {
    return getMainSessionController(req).getUserId();
  }

  public void displayError(HttpServletResponse res) {
    SilverTrace.info("peasUtil", "GoToFile.displayError()",
        "root.MSG_GEN_ENTER_METHOD");

    res.setContentType("text/html");
    OutputStream out2 = null;
    int read;

    StringBuilder message = new StringBuilder(255);
    message.append("<HTML>");
    message.append("<BODY>");
    message.append("</BODY>");
    message.append("</HTML>");

    StringReader reader = new StringReader(message.toString());

    try {
      out2 = res.getOutputStream();
      read = reader.read();
      while (read != -1) {
        out2.write(read); // writes bytes into the response
        read = reader.read();
      }
    } catch (Exception e) {
      SilverTrace.warn("peasUtil", "GoToFile.displayError",
          "root.EX_CANT_READ_FILE");
    } finally {
      // we must close the in and out streams
      try {
        out2.close();
      } catch (Exception e) {
        SilverTrace.warn("peasUtil", "GoToFile.displayError",
            "root.EX_CANT_READ_FILE", "close failed");
      }
    }
  }
}

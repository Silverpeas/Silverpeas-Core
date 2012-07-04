/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.peasUtil;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.silverpeas.look.LookHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SilverpeasWebUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.OutputStream;

public abstract class GoTo extends HttpServlet {

  private static final long serialVersionUID = -8381001443484846645L;
  private static SilverpeasWebUtil util = new SilverpeasWebUtil();

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
      SilverTrace.info("peasUtil", "GoTo.doPost", "root.MSG_GEN_PARAM_VALUE", "id = " + id);

      String redirect = getDestination(id, req, res);
      if (!StringUtil.isDefined(redirect)) {
        objectNotFound(req, res);
      } else {
        if (!res.isCommitted()) { // The response was not previously sent
          if (redirect == null || !redirect.startsWith("http")) {
            redirect = URLManager.getApplicationURL() + "/autoRedirect.jsp?" + redirect;
          }
          res.sendRedirect(res.encodeRedirectURL(redirect));
        }
      }
    } catch (AccessForbiddenException afe) {
      accessForbidden(req, res);
    } catch (Exception e) {
      objectNotFound(req, res);
    }
  }

  public abstract String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception;

  private void objectNotFound(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    boolean isLoggedIn = isUserLogin(req);
    if (!isLoggedIn) {
      res.sendRedirect("/weblib/notFound.html");
    } else {
      res.sendRedirect(URLManager.getApplicationURL() + "/admin/jsp/documentNotFound.jsp");
    }
  }

  private void accessForbidden(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    res.sendRedirect(URLManager.getApplicationURL() + "/admin/jsp/accessForbidden.jsp");
  }

  public String getObjectId(HttpServletRequest request) {
    String pathInfo = request.getPathInfo();
    if (pathInfo != null) {
      return pathInfo.substring(1);
    }
    return null;
  }

  public boolean isUserLogin(HttpServletRequest req) {
    return util.getMainSessionController(req) != null;
  }

  // check if the user is allowed to access the required component
  public boolean isUserAllowed(HttpServletRequest req, String componentId) {
    MainSessionController mainSessionCtrl = util.getMainSessionController(req);
    if (componentId == null) { // Personal space
      return true;
    }
    return mainSessionCtrl.getOrganizationController().isComponentAvailable(componentId,
        mainSessionCtrl.getUserId());
  }

  public String getUserId(HttpServletRequest req) {
    return util.getMainSessionController(req).getUserId();
  }

  public void displayError(HttpServletResponse res) {
    SilverTrace.info("peasUtil", "GoToFile.displayError()", "root.MSG_GEN_ENTER_METHOD");

    res.setContentType("text/html");
    OutputStream out = null;
    StringBuilder message = new StringBuilder(255);
    message.append("<HTML>").append("<BODY>").append("</BODY>").append("</HTML>");
    try {
      out = res.getOutputStream();
      out.write(message.toString().getBytes(Charsets.UTF_8)); // writes bytes into the response
    } catch (IOException e) {
      SilverTrace.warn("peasUtil", "GoToFile.displayError", "root.EX_CANT_READ_FILE");
    } finally {
      Closeables.closeQuietly(out);
    }
  }

  /**
   * Set GEF and look helper space identifier
   * @param req current HttpServletRequest
   * @param componentId the component identifier
   */
  protected void setGefSpaceId(HttpServletRequest req, String componentId) {
    if (StringUtil.isDefined(componentId)) {
      HttpSession session = req.getSession(true);
      GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
          GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      LookHelper helper = (LookHelper) session.getAttribute(LookHelper.SESSION_ATT);
      if (gef != null && helper != null) {
        helper.setComponentIdAndSpaceIds(null, null, componentId);
        String helperSpaceId = helper.getSubSpaceId();
        if (!StringUtil.isDefined(helperSpaceId)) {
          helperSpaceId = helper.getSpaceId();
        }
        gef.setSpaceId(helperSpaceId);
      }
    }
  }
}

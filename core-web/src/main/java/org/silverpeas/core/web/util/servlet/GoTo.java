/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.servlet;

import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.util.AccessForbiddenException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;

public abstract class GoTo extends HttpServlet {

  private static final long serialVersionUID = -8381001443484846645L;

  @Inject
  private OrganizationController organizationController;

  @Inject
  protected SilverpeasWebUtil util;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    String id = getObjectId(req);

    try {


      String redirect = getDestination(id, req, res);
      if (!StringUtil.isDefined(redirect)) {
        objectNotFound(req, res);
      } else {
        if (!res.isCommitted()) {
          // The response was not previously sent
          if (redirect == null || !redirect.startsWith("http")) {
            redirect = URLUtil.getApplicationURL() + "/autoRedirect.jsp?" + redirect;
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
      res.sendRedirect(URLUtil.getApplicationURL() + "/admin/jsp/documentNotFound.jsp");
    }
  }

  private void accessForbidden(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    res.sendRedirect(URLUtil.getApplicationURL() + "/admin/jsp/accessForbidden.jsp");
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
    if (componentId == null) {
      // Personal space
      return true;
    }
    return organizationController.isComponentAvailable(componentId,
        mainSessionCtrl.getUserId());
  }

  public String getUserId(HttpServletRequest req) {
    return util.getMainSessionController(req).getUserId();
  }

  public void displayError(HttpServletResponse res) {


    res.setContentType("text/html");
    OutputStream out = null;
    StringBuilder message = new StringBuilder(255);
    message.append("<html>").append("<body>").append("</body>").append("</html>");
    try {
      out = res.getOutputStream();
      // writes bytes into the response
      out.write(message.toString().getBytes(Charsets.UTF_8));
    } catch (IOException e) {
      SilverTrace.warn("util", "GoToFile.displayError", "root.EX_CANT_READ_FILE");
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  /**
   * Set GEF and look helper space identifier
   *
   * @param req current HttpServletRequest
   * @param componentId the component identifier
   */
  protected void setGefSpaceId(HttpServletRequest req, String componentId) {
    if (StringUtil.isDefined(componentId)) {
      HttpSession session = req.getSession(true);
      GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
          GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      LookHelper helper = LookHelper.getLookHelper(session);
      if (gef != null && helper != null) {
        helper.setComponentIdAndSpaceIds(null, null, componentId);
        String helperSpaceId = helper.getSubSpaceId();
        if (!StringUtil.isDefined(helperSpaceId)) {
          helperSpaceId = helper.getSpaceId();
        }
        gef.setSpaceIdForCurrentRequest(helperSpaceId);
        gef.setComponentIdForCurrentRequest(componentId);
      }
    }
  }

  /**
   * Gets the content language specified into the request.
   * @param request
   * @return
   */
  protected String getContentLanguage(HttpServletRequest request) {
    return util.getContentLanguage(request);
  }
}

/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.web.util.servlet;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;
import org.silverpeas.core.web.mvc.util.AccessForbiddenException;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

import static org.silverpeas.core.util.ResourceLocator.getGeneralLocalizationBundle;

public abstract class GoTo extends HttpServlet {

  private static final long serialVersionUID = -8381001443484846645L;

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
    final HttpRequest httpRequest = HttpRequest.decorate(req);
    final Context context = new Context(httpRequest, res).init();
    String id = getObjectId(httpRequest);
    try {
      String redirect = getDestination(id, httpRequest, res);
      if (!StringUtil.isDefined(redirect)) {
        objectNotFound(context);
      } else {
        if (!res.isCommitted()) {
          // The response was not previously sent
          if (!redirect.startsWith("http")) {
            redirect = URLUtil.getFullApplicationURL(httpRequest) + "/autoRedirect.jsp?" + redirect;
          }
          res.sendRedirect(res.encodeRedirectURL(UriBuilder.fromUri(redirect)
              .queryParam("fromResponsiveWindow", context.isFromResponsiveWindow()).build()
              .toString()));
        }
      }
    } catch (AccessForbiddenException afe) {
      accessForbidden(context);
    } catch (Exception e) {
      objectNotFound(context);
    }
  }

  public abstract String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception;

  private void objectNotFound(final Context context) throws IOException {
    if (context.isFromResponsiveWindow()) {
      sendJsonResponse(context, JSONCodec.encodeObject(o -> o.put("errorMessage",
          getGeneralLocalizationBundle(context.getLanguage()).getString("GML.DocumentNotFound"))));
    } else {
      boolean isLoggedIn = isUserLogin(context.getRequest());
      if (!isLoggedIn) {
        context.getResponse().sendRedirect("/weblib/notFound.html");
      } else {
        context.getResponse().sendRedirect(URLUtil.getApplicationURL() + "/admin/jsp/documentNotFound.jsp");
      }
    }
  }

  private void accessForbidden(final Context context) throws IOException {
    if (context.isFromResponsiveWindow()) {
      sendJsonResponse(context, JSONCodec.encodeObject(o -> o.put("errorMessage",
          getGeneralLocalizationBundle(context.getLanguage()).getString("GML.ForbiddenAccessContent"))));
    } else {
      context.getResponse().sendRedirect(URLUtil.getApplicationURL() + "/admin/jsp/accessForbidden.jsp");
    }
  }

  private void sendJsonResponse(final Context context, final String json) throws IOException {
    context.getResponse().setContentType(MediaType.APPLICATION_JSON);
    context.getResponse().getWriter().print(json);
  }

  public String getObjectId(HttpServletRequest request) {
    String pathInfo = request.getPathInfo();
    if (pathInfo != null) {
      return pathInfo.substring(1);
    }
    return null;
  }

  protected boolean isUserLogin(HttpServletRequest req) {
    return util.getMainSessionController(req) != null;
  }

  // check if the user is allowed to access the required component
  protected boolean isUserAllowed(HttpServletRequest req, String componentId) {
    MainSessionController mainSessionCtrl = util.getMainSessionController(req);
    if (componentId == null) {
      // Personal space
      return true;
    }
    return OrganizationController.get().isComponentAvailableToUser(componentId, mainSessionCtrl.getUserId());
  }

  public String getUserId(HttpServletRequest req) {
    return util.getMainSessionController(req).getUserId();
  }

  /**
   * Set GEF and look helper space identifier
   * @param req current HttpServletRequest
   * @param componentId the component identifier
   */
  protected void setGefSpaceId(HttpServletRequest req, String componentId) {
    if (StringUtil.isDefined(componentId)) {
      HttpSession session = req.getSession(true);
      GraphicElementFactory gef = (GraphicElementFactory) session
          .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
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
   */
  protected String getContentLanguage(HttpServletRequest request) {
    return util.getContentLanguage(request);
  }

  private static class Context {
    private final HttpRequest request;
    private final HttpServletResponse response;
    private final boolean fromResponsiveWindow;
    private final String language;

    private Context(HttpRequest request, final HttpServletResponse response) {
      this.request = request;
      this.response = response;
      this.fromResponsiveWindow = request.getParameterAsBoolean("fromResponsiveWindow");
      this.language = User.getCurrentRequester() != null
          ? User.getCurrentRequester().getUserPreferences().getLanguage()
          : DisplayI18NHelper.getDefaultLanguage();
    }

    public Context init() {
      return this;
    }

    HttpServletRequest getRequest() {
      return request;
    }

    HttpServletResponse getResponse() {
      return response;
    }

    boolean isFromResponsiveWindow() {
      return fromResponsiveWindow;
    }

    String getLanguage() {
      return language;
    }
  }
}

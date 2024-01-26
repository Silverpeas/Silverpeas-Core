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
package org.silverpeas.core.web.util.servlet;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;
import org.silverpeas.core.web.mvc.util.AccessForbiddenException;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Optional;

import static org.silverpeas.kernel.bundle.ResourceLocator.getGeneralLocalizationBundle;

public abstract class GoTo extends HttpServlet {

  private static final long serialVersionUID = -8381001443484846645L;
  private static final String MANUAL_JSON_RESPONSE_PREFIX = "MANUAL_JSON_RESPONSE_";

  @Inject
  protected SilverpeasWebUtil util;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    final HttpRequest httpRequest = HttpRequest.decorate(req);
    final Context context = new Context(httpRequest, res).init();
    String id = getObjectId(httpRequest);
    try {
      String redirect = getDestination(id, context);
      if (!StringUtil.isDefined(redirect)) {
        objectNotFound(context);
      } else {
        if (!res.isCommitted()) {
          // The response was not previously sent
          if (redirect.startsWith(MANUAL_JSON_RESPONSE_PREFIX)) {
            res.setContentType(MediaType.APPLICATION_JSON);
            res.setCharacterEncoding(Charsets.UTF_8.name());
            res.getWriter().append(redirect.substring(MANUAL_JSON_RESPONSE_PREFIX.length()));
            res.flushBuffer();
            return;
          }
          if (!redirect.startsWith("http")) {
            redirect = URLUtil.getFullApplicationURL(httpRequest) + "/autoRedirect.jsp?" + redirect;
          }
          final UriBuilder uriBuilder = UriBuilder.fromUri(redirect)
              .queryParam("fromResponsiveWindow", context.isFromResponsiveWindow());
          if (context.isForceToLogin()) {
            uriBuilder.queryParam("forceToLogin", true);
          }
          res.sendRedirect(res.encodeRedirectURL(uriBuilder.build().toString()));
        }
      }
    } catch (AccessForbiddenException afe) {
      accessForbidden(context);
    } catch (Exception e) {
      objectNotFound(context);
    }
  }

  public String getDestination(String objectId, Context context) throws Exception {
    return getDestination(objectId, context.getRequest(), context.getResponse());
  }

  public abstract String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception;

  protected String sendJson(String jsonContent) {
    return MANUAL_JSON_RESPONSE_PREFIX + jsonContent;
  }

  private void objectNotFound(final Context context) {
    try {
      if (context.isFromResponsiveWindow()) {
        sendJsonResponse(context, JSONCodec.encodeObject(o -> o.put("errorMessage",
            getGeneralLocalizationBundle(context.getLanguage()).getString(
                "GML.DocumentNotFound"))));
      } else {
        boolean isLoggedIn = isUserLogin(context.getRequest());
        if (!isLoggedIn) {
          context.getResponse().sendRedirect("/weblib/notFound.html");
        } else {
          context.getResponse()
              .sendRedirect(URLUtil.getApplicationURL() + "/admin/jsp/documentNotFound.jsp");
        }
      }
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      context.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void accessForbidden(final Context context) {
    try {
      if (context.isFromResponsiveWindow()) {
        sendJsonResponse(context, JSONCodec.encodeObject(o -> o.put("errorMessage",
            getGeneralLocalizationBundle(context.getLanguage()).getString(
                "GML.ForbiddenAccessContent"))));
      } else {
        context.getResponse()
            .sendRedirect(URLUtil.getApplicationURL() + "/admin/jsp/accessForbidden.jsp");
      }
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      context.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
      Optional.ofNullable(LookHelper.getLookHelper(session))
          .ifPresent(h -> h.setComponentIdAndSpaceIds(null, null, componentId));
    }
  }

  /**
   * Gets the content language specified into the request.
   */
  protected String getContentLanguage(HttpServletRequest request) {
    return util.getContentLanguage(request);
  }

  public static class Context {
    private final HttpRequest request;
    private final HttpServletResponse response;
    private final boolean fromResponsiveWindow;
    private final boolean forceToLogin;
    private final String language;

    private Context(HttpRequest request, final HttpServletResponse response) {
      this.request = request;
      this.response = response;
      this.fromResponsiveWindow = request.getParameterAsBoolean("fromResponsiveWindow");
      this.forceToLogin = request.getParameterAsBoolean("forceToLogin");
      this.language = User.getCurrentRequester() != null
          ? User.getCurrentRequester().getUserPreferences().getLanguage()
          : DisplayI18NHelper.getDefaultLanguage();
    }

    Context init() {
      return this;
    }

    public HttpServletRequest getRequest() {
      return request;
    }

    public HttpServletResponse getResponse() {
      return response;
    }

    public boolean isFromResponsiveWindow() {
      return fromResponsiveWindow;
    }

    public boolean isForceToLogin() {
      return forceToLogin;
    }

    public String getLanguage() {
      return language;
    }
  }
}

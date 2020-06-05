/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.web;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.Serializable;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static org.silverpeas.core.util.MimeTypes.SERVLET_HTML_CONTENT_TYPE;
import static org.silverpeas.core.util.ResourceLocator.getGeneralLocalizationBundle;
import static org.silverpeas.core.util.StringUtil.*;
import static org.silverpeas.core.web.mvc.controller.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;
import static org.silverpeas.core.web.mvc.controller.MainSessionController.isAppInMaintenance;
import static org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory.GE_FACTORY_SESSION_ATT;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.scriptContent;

/**
 * @author ehugonnet
 */
public class AutoRedirectServlet extends HttpServlet {
  private static final long serialVersionUID = -8962464286320797737L;
  private static final String REDIRECT_TO_COMPONENT_ID_ATTR = "RedirectToComponentId";
  private static final String REDIRECT_TO_SPACE_ID_ATTR = "RedirectToSpaceId";
  private static final String GOTO_NEW_ATTR = "gotoNew";

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws IOException if an I/O error occurs
   */
  private void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    prepareResponseHeader(response);
    final HttpRequest httpRequest = HttpRequest.decorate(request);
    final Context context = new Context(httpRequest, response).init();
    final MainSessionController mainController = context.getMainSessionController();
    final GraphicElementFactory gef = context.getGraphicElementFactory();

    // The user is either not connected or as the anonymous user. He comes back to the login page.
    if (mainController == null ||
        (gef != null && UserDetail.isAnonymousUser(mainController.getUserId()) && context.notExistsGoto())) {
      loginPageRedirection(context);
    } else {
      if (isAccessComponentForbidden(context.getComponentId(), mainController) ||
          isAccessSpaceForbidden(context.getSpaceId(), mainController)) {
        if (httpRequest.isWithinAnonymousUserSession()) {
          loginPageRedirection(context);
        } else {
          forbiddenPageRedirection(context);
        }
      } else if (isAppInMaintenance() && !mainController.getCurrentUserDetail().isAccessAdmin()) {
        appInMaintenancePageRedirection(context);
      } else if (gef != null) {
        mainPageRedirection(context);
      } else {
        loginPageRedirection(context);
      }
    }
  }

  private void loginPageRedirection(final Context context) throws IOException {
    final Integer domainId = context.getRequest().getParameterAsInteger("domainId");
    final Mutable<String> loginUrl = Mutable.of(URLUtil.getApplicationURL() + "/Login.jsp");
    if (domainId != null) {
      loginUrl.set(loginUrl.get() + "?DomainId=" + domainId);
    }
    if (context.isFromResponsiveWindow()) {
      context.getSession().setAttribute(GOTO_NEW_ATTR, context.getGotoUrl());
      sendJsonResponse(context, JSONCodec.encodeObject(o -> o.put("mainUrl", loginUrl.get())));
    } else {
      sendHtmlRedirectResponse(context, loginUrl.get());
    }
  }

  private void forbiddenPageRedirection(final Context context) throws IOException {
    if (context.isFromResponsiveWindow()) {
      sendJsonResponse(context, JSONCodec.encodeObject(o -> o.put("errorMessage",
          getGeneralLocalizationBundle(context.getLanguage()).getString("GML.ForbiddenAccessContent"))));
    } else {
      context.getResponse().sendRedirect(URLUtil.getApplicationURL() + "/admin/jsp/accessForbidden.jsp");
    }
  }

  private void appInMaintenancePageRedirection(final Context context) throws IOException {
    final String url = URLUtil.getApplicationURL() + "/admin/jsp/appInMaintenance.jsp";
    if (context.isFromResponsiveWindow()) {
      sendJsonResponse(context, JSONCodec.encodeObject(o -> o.put("mainUrl", url)));
    } else {
      sendHtmlRedirectResponse(context, url);
    }
  }

  private void mainPageRedirection(final Context context) throws IOException {
    String mainFrame = context.getGraphicElementFactory().getLookFrame();
    if (!mainFrame.startsWith("/")) {
      mainFrame = "admin/jsp/" + mainFrame;
    } else if (!mainFrame.startsWith(URLUtil.getApplicationURL())) {
      mainFrame = URLUtil.getApplicationURL() + mainFrame;
    }
    final String componentId = defaultStringIfNotDefined(context.getComponentId());
    final String spaceId = defaultStringIfNotDefined(context.getSpaceId());
    final String url = mainFrame + "?ComponentIdFromRedirect=" + componentId;
    if (context.isFromResponsiveWindow()) {
      final Mutable<Boolean> isPersonalComponent = Mutable.of(false);
      if (isDefined(componentId)) {
        SilverpeasComponentInstance.getById(componentId)
            .ifPresent(i -> isPersonalComponent.set(i.isPersonal()));
      }
      sendJsonResponse(context, JSONCodec.encodeObject(o ->
          o.put("contentUrl", context.getGotoUrl())
           .put(isPersonalComponent.get() ? "RedirectToPersonalComponentId" : REDIRECT_TO_COMPONENT_ID_ATTR, componentId)
           .put(REDIRECT_TO_SPACE_ID_ATTR, spaceId)));
    } else {
      sendHtmlRedirectResponse(context, url);
    }
  }

  private void sendHtmlRedirectResponse(final Context context, final String url) throws IOException {
    context.getResponse().getWriter().print(
        scriptContent("top.location=\"" + WebEncodeHelper.javaStringToJsString(url) + "\";"));
  }

  private void sendJsonResponse(final Context context, final String json) throws IOException {
    context.getResponse().setContentType(MediaType.APPLICATION_JSON);
    context.getResponse().getWriter().print(json);
  }

  private void prepareResponseHeader(HttpServletResponse response) {
    response.setContentType(SERVLET_HTML_CONTENT_TYPE);
    response.setHeader("Expires", "Tue, 21 Dec 1993 23:59:59 GMT");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-control", "no-cache");
    response.setHeader("Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT");
    response.setStatus(SC_CREATED);
  }

  private boolean isAccessSpaceForbidden(String spaceId, MainSessionController mainController) {
    return isDefined(spaceId) &&
        !OrganizationController.get().isSpaceAvailable(spaceId, mainController.getUserId());
  }

  private boolean isAccessComponentForbidden(String componentId,
      MainSessionController mainController) {
    return isDefined(componentId) && !StringUtil.isAlpha(componentId) &&
        !ComponentAccessControl.get().isUserAuthorized(mainController.getUserId(), componentId);
  }

  /**
   * Handles the HTTP <code>GET</code> method.
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
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }

  private static class Context {
    private final HttpRequest request;
    private final HttpServletResponse response;
    private final HttpSession session;
    private final MainSessionController mainSessionController;
    private final GraphicElementFactory graphicElementFactory;
    private final String componentIdGoTo;
    private final String spaceIdGoTo;
    private final String attachmentIdGoTo;
    private final boolean fromResponsiveWindow;
    private String gotoUrl;
    private String componentId = null;
    private String spaceId = null;
    private String language;

    private Context(HttpRequest request, final HttpServletResponse response) {
      this.request = request;
      this.session = request.getSession();
      this.response = response;
      this.mainSessionController = (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
      this.graphicElementFactory = (GraphicElementFactory) session.getAttribute(GE_FACTORY_SESSION_ATT);
      this.componentIdGoTo = request.getParameter("ComponentId");
      this.spaceIdGoTo = request.getParameter("SpaceId");
      this.attachmentIdGoTo = request.getParameter("AttachmentId");
      this.gotoUrl = request.getParameter("goto");
      this.fromResponsiveWindow = request.getParameterAsBoolean("fromResponsiveWindow");
      this.language = this.mainSessionController != null
          ? this.mainSessionController.getFavoriteLanguage()
          : DisplayI18NHelper.getDefaultLanguage();
    }

    private static boolean isSilverpeasIdValid(String silverpeasId) {
      return isDefined(silverpeasId)
          && !StringUtil.isAlpha(silverpeasId)
          && StringUtil.isAlphanumeric(silverpeasId.replaceAll("[-_]",""));
    }

    private void setIntoSession(String name, Serializable value) {
      if (!fromResponsiveWindow) {
        session.setAttribute(name, value);
      }
    }

    public Context init() {
      session.removeAttribute(GOTO_NEW_ATTR);
      session.removeAttribute(REDIRECT_TO_COMPONENT_ID_ATTR);
      session.removeAttribute(REDIRECT_TO_SPACE_ID_ATTR);
      session.removeAttribute("RedirectToAttachmentId");
      session.removeAttribute("RedirectToMapping");
      if (isDefined(gotoUrl)) {
        setIntoSession(GOTO_NEW_ATTR, gotoUrl);
        String urlToParse = gotoUrl;
        final String extractedComponentId;
        if (gotoUrl.startsWith("/RpdcSearch/")) {
          int indexOf = urlToParse.indexOf("&componentId=");
          extractedComponentId = urlToParse.substring(indexOf + 13, urlToParse.length());
        } else {
          urlToParse = urlToParse.substring(1);
          int indexBegin = urlToParse.indexOf('/') + 1;
          int indexEnd = urlToParse.indexOf('/', indexBegin);
          extractedComponentId = urlToParse.substring(indexBegin, indexEnd);
        }
        if (isSilverpeasIdValid(extractedComponentId)) {
          componentId = extractedComponentId;
          setIntoSession(REDIRECT_TO_COMPONENT_ID_ATTR, componentId);
        }
      } else if (isSilverpeasIdValid(componentIdGoTo)) {
        componentId = componentIdGoTo;
        setIntoSession(REDIRECT_TO_COMPONENT_ID_ATTR, componentId);
        final String foreignId = request.getParameter("ForeignId");
        if (isDefined(attachmentIdGoTo) && isSilverpeasIdValid(foreignId)) {
          final String type = request.getParameter("Mapping");
          // Contruit l'url vers l'objet du composant contenant le fichier
          gotoUrl = URLUtil.getURL(null, componentId) + "searchResult?Type=Publication&Id=" + foreignId;
          setIntoSession(GOTO_NEW_ATTR, gotoUrl);
          // Ajoute l'id de l'attachment pour ouverture automatique
          setIntoSession("RedirectToAttachmentId", attachmentIdGoTo);
          setIntoSession("RedirectToMapping", type);
        }
      } else if (isSilverpeasIdValid(spaceIdGoTo)) {
        spaceId = spaceIdGoTo;
        setIntoSession(REDIRECT_TO_SPACE_ID_ATTR, spaceId);
      }

      return this;
    }

    HttpSession getSession() {
      return session;
    }

    HttpRequest getRequest() {
      return request;
    }

    HttpServletResponse getResponse() {
      return response;
    }

    MainSessionController getMainSessionController() {
      return mainSessionController;
    }

    GraphicElementFactory getGraphicElementFactory() {
      return graphicElementFactory;
    }

    boolean notExistsGoto() {
      return isNotDefined(gotoUrl) && isNotDefined(componentIdGoTo) && isNotDefined(spaceIdGoTo);
    }

    public String getComponentId() {
      return componentId;
    }

    public String getSpaceId() {
      return spaceId;
    }

    boolean isFromResponsiveWindow() {
      return fromResponsiveWindow;
    }

    String getGotoUrl() {
      return gotoUrl;
    }

    String getLanguage() {
      return language;
    }
  }
}

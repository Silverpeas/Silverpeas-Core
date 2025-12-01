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
package org.silverpeas.web;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.security.authorization.SimpleDocumentAccessControl;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.Mutable;
import org.silverpeas.kernel.util.StringUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.*;
import static org.silverpeas.core.util.MimeTypes.SERVLET_HTML_CONTENT_TYPE;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;
import static org.silverpeas.core.web.mvc.controller.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;
import static org.silverpeas.core.web.mvc.controller.MainSessionController.isAppInMaintenance;
import static org.silverpeas.core.web.util.WebRedirection.*;
import static org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory.GE_FACTORY_SESSION_ATT;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.scriptContent;
import static org.silverpeas.kernel.bundle.ResourceLocator.getGeneralLocalizationBundle;
import static org.silverpeas.kernel.util.StringUtil.*;

/**
 * This servlet aims to process the Web redirection requests by taking into account the access
 * rights of the user on the resource targeted by the redirection.
 *
 * @author ehugonnet
 */
public class AutoRedirectServlet extends HttpServlet {
  private static final long serialVersionUID = -8962464286320797737L;

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   */
  private void processRequest(HttpServletRequest request, HttpServletResponse response) {
    prepareResponseHeader(response);
    final HttpRequest httpRequest = HttpRequest.decorate(request);
    final Context context = new Context(httpRequest, response).init();
    final MainSessionController mainController = context.getMainSessionController();
    final GraphicElementFactory gef = context.getGraphicElementFactory();

    try {
      // The user is either not connected or as the anonymous user. He comes back to the login page.
      UserDetail user = UserDetail.getCurrentRequester();
      if (mainController == null || context.isForceToLogin() ||
          ((gef != null && user.isAnonymous()) && context.notExistsGoto())) {
        loginPageRedirection(context);
      } else {
        redirect(httpRequest, context, mainController, gef);
      }
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      response.setStatus(SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void redirect(final HttpRequest httpRequest, final Context context,
      final MainSessionController mainController, final GraphicElementFactory gef)
      throws IOException {
    final List<Status> errorStatuses = Stream.of(
            getAttachmentAccessStatus(context, mainController),
            getPublicationAccessStatus(context, mainController),
            getComponentAccessStatus(context, mainController),
            getSpaceAccessStatus(context, mainController))
        .filter(s -> s.getStatusCode() >= 400)
        .collect(Collectors.toList());
    if (!errorStatuses.isEmpty()) {
      if (httpRequest.isWithinAnonymousUserSession()) {
        loginPageRedirection(context);
      } else if (errorStatuses.stream().anyMatch(NOT_FOUND::equals)) {
        notFoundRedirection(context);
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

  private void loginPageRedirection(final Context context) throws IOException {
    final Integer domainId = context.getRequest().getParameterAsInteger("domainId");
    final Mutable<String> loginUrl = Mutable.of(getApplicationURL() + "/Login");
    if (domainId != null) {
      loginUrl.set(loginUrl.get() + "?DomainId=" + domainId);
    }
    if (context.isFromResponsiveWindow()) {
      context.getSession().setAttribute(REDIRECT_URL, context.getGotoUrl());
      sendJsonResponse(context, JSONCodec.encodeObject(o -> o.put("mainUrl", loginUrl.get())));
    } else {
      sendHtmlRedirectResponse(context, loginUrl.get());
    }
  }

  private void forbiddenPageRedirection(final Context context) throws IOException {
    if (context.isFromResponsiveWindow()) {
      sendJsonResponse(context, JSONCodec.encodeObject(o -> o.put("errorMessage",
          getGeneralLocalizationBundle(context.getLanguage()).getString("GML" +
              ".ForbiddenAccessContent"))));
    } else {
      context.getResponse().sendRedirect(getApplicationURL() + "/admin/jsp/accessForbidden.jsp");
    }
  }

  private void notFoundRedirection(final Context context) throws IOException {
    if (context.isFromResponsiveWindow()) {
      sendJsonResponse(context, JSONCodec.encodeObject(o -> o.put("errorMessage",
          getGeneralLocalizationBundle(context.getLanguage()).getString("GML.DocumentNotFound"))));
    } else {
      context.getResponse().sendRedirect(getApplicationURL() + "/admin/jsp/documentNotFound.jsp");
    }
  }

  private void appInMaintenancePageRedirection(final Context context) throws IOException {
    final String url = getApplicationURL() + "/admin/jsp/appInMaintenance.jsp";
    if (context.isFromResponsiveWindow()) {
      sendJsonResponse(context, JSONCodec.encodeObject(o -> o.put("mainUrl", url)));
    } else {
      sendHtmlRedirectResponse(context, url);
    }
  }

  private void mainPageRedirection(final Context context) throws IOException {
    final String componentId = defaultStringIfNotDefined(context.getComponentId());
    final String spaceId = defaultStringIfNotDefined(context.getSpaceId());
    if (context.isFromResponsiveWindow()) {
      final Mutable<Boolean> isPersonalComponent = Mutable.of(false);
      if (isDefined(componentId)) {
        SilverpeasComponentInstance.getById(componentId)
            .ifPresent(i -> isPersonalComponent.set(i.isPersonal()));
      }
      boolean isPersoComp = isPersonalComponent.get();
      sendJsonResponse(context, JSONCodec.encodeObject(o ->
          o.put("contentUrl", context.getGotoUrl())
              .put(isPersoComp ? "RedirectToPersonalComponentId" : REDIRECT_TO_COMPONENT,
                  componentId)
              .put(REDIRECT_TO_SPACE, spaceId)));
    } else {
      String mainFrame = context.getGraphicElementFactory().getLookFrame();
      if (!mainFrame.startsWith("/")) {
        mainFrame = "admin/jsp/" + mainFrame;
      } else if (!mainFrame.startsWith(getApplicationURL())) {
        mainFrame = getApplicationURL() + mainFrame;
      }
      final String url = mainFrame + "?" + REDIRECT_TO_COMPONENT + "=" + componentId;
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

  private Status getSpaceAccessStatus(Context context, MainSessionController mainController) {
    String spaceId = context.getSpaceId();
    return isDefined(spaceId) ?
        of(spaceId)
            .map(i -> OrganizationController.get().getSpaceInstLightById(i))
            .filter(s -> isAtLeastOneRemovedSpaceFrom(s.getId()))
            .map(s -> {
              if (s.canBeAccessedBy(mainController.getCurrentUserDetail())) {
                return OK;
              }
              return FORBIDDEN;
            })
            .orElse(NOT_FOUND) :
        SEE_OTHER;
  }

  private Status getComponentAccessStatus(Context context, MainSessionController mainController) {
    String componentId = context.getComponentId();
    return isDefined(componentId) ?
        of(componentId)
            .filter(not(StringUtils::isAlpha))
            .flatMap(i -> OrganizationController.get().getComponentInstance(i))
            .filter(not(SilverpeasComponentInstance::isRemoved))
            .filter(i -> isAtLeastOneRemovedSpaceFrom(i.getSpaceId()))
            .map(i -> {
              if (ComponentAccessControl.get().isUserAuthorized(mainController.getUserId(),
                  i.getId())) {
                return OK;
              }
              return FORBIDDEN;
            })
            .orElse(NOT_FOUND) :
        SEE_OTHER;
  }

  private Status getPublicationAccessStatus(Context context, MainSessionController mainController) {
    String componentId = context.getComponentId();
    String publicationId = context.getPublicationId();
    return isDefined(publicationId) && isDefined(componentId) ?
        of(publicationId)
            .filter(StringUtils::isNumeric)
            .map(i -> {
              if (PublicationAccessControl.get().isUserAuthorized(mainController.getUserId(),
                  new PublicationPK(publicationId, componentId))) {
                return OK;
              }
              return FORBIDDEN;
            })
            .orElse(NOT_FOUND) :
        SEE_OTHER;
  }

  private Status getAttachmentAccessStatus(Context context, MainSessionController mainController) {
    String componentId = context.getComponentId();
    String attachmentId = context.getAttachmentId();
    return isDefined(attachmentId) && isDefined(componentId) ?
        of(attachmentId)
            .filter(StringUtils::isNumeric)
            .map(i -> {
              if (SimpleDocumentAccessControl.get().isUserAuthorized(mainController.getUserId(),
                  new PublicationPK(attachmentId, componentId))) {
                return OK;
              }
              return FORBIDDEN;
            })
            .orElse(NOT_FOUND) :
        SEE_OTHER;
  }

  private boolean isAtLeastOneRemovedSpaceFrom(final String spaceId) {
    return ofNullable(spaceId)
        .map(s -> OrganizationController.get().getPathToSpace(s))
        .stream()
        .flatMap(Collection::stream)
        .noneMatch(SpaceInstLight::isRemoved);
  }

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
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
    private final boolean forceToLogin;
    private String gotoUrl;
    private String componentId = null;
    private String publicationId = null;
    private String spaceId = null;
    private final String language;

    private Context(HttpRequest request, final HttpServletResponse response) {
      this.request = request;
      this.session = request.getSession();
      this.response = response;
      this.mainSessionController =
          (MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT);
      this.graphicElementFactory =
          (GraphicElementFactory) session.getAttribute(GE_FACTORY_SESSION_ATT);
      this.componentIdGoTo = encodeURL(request.getParameter("ComponentId"));
      this.spaceIdGoTo = encodeURL(request.getParameter("SpaceId"));
      this.attachmentIdGoTo = encodeURL(request.getParameter("AttachmentId"));
      this.gotoUrl = encodeURL(request.getParameter("goto"));
      this.fromResponsiveWindow = request.getParameterAsBoolean("fromResponsiveWindow");
      this.forceToLogin = request.getParameterAsBoolean("forceToLogin");
      this.language = this.mainSessionController != null
          ? this.mainSessionController.getFavoriteLanguage()
          : DisplayI18NHelper.getDefaultLanguage();
    }

    private String encodeURL(String url) {
      return StringUtil.isDefined(url) ? URLUtil.decode(url) : url;
    }

    private static boolean isSilverpeasIdValid(String silverpeasId) {
      return isDefined(silverpeasId)
          && !StringUtils.isAlpha(silverpeasId)
          && StringUtils.isAlphanumeric(silverpeasId.replaceAll("[-_]", ""));
    }

    private void setIntoSession(String name, Serializable value) {
      if (!fromResponsiveWindow) {
        session.setAttribute(name, value);
      }
    }

    public Context init() {
      session.removeAttribute(REDIRECT_URL);
      session.removeAttribute(REDIRECT_TO_COMPONENT);
      session.removeAttribute(REDIRECT_TO_SPACE);
      session.removeAttribute(REDIRECT_TO_ATTACHMENT);
      session.removeAttribute(REDIRECT_RESOURCE_TYPE);
      if (isDefined(gotoUrl)) {
        setIntoSession(REDIRECT_URL, gotoUrl);
        final String extractedComponentId = extractComponentId();
        publicationId = extractPublicationId();
        if (isSilverpeasIdValid(extractedComponentId)) {
          componentId = extractedComponentId;
          setIntoSession(REDIRECT_TO_COMPONENT, componentId);
        }
      } else if (isSilverpeasIdValid(componentIdGoTo)) {
        componentId = componentIdGoTo;
        setIntoSession(REDIRECT_TO_COMPONENT, componentId);
        final String foreignId = request.getParameter("ForeignId");
        if (isDefined(attachmentIdGoTo) && isSilverpeasIdValid(foreignId)) {
          final String type = request.getParameter("Mapping");
          // Contruit l'url vers l'objet du composant contenant le fichier
          gotoUrl =
              URLUtil.getURL(null, componentId) + "searchResult?Type=Publication&Id=" + foreignId;
          setIntoSession(REDIRECT_URL, gotoUrl);
          // Ajoute l'id de l'attachment pour ouverture automatique
          setIntoSession(REDIRECT_TO_ATTACHMENT, attachmentIdGoTo);
          setIntoSession(REDIRECT_RESOURCE_TYPE, type);
        }
      } else if (isSilverpeasIdValid(spaceIdGoTo)) {
        spaceId = spaceIdGoTo;
        setIntoSession(REDIRECT_TO_SPACE, spaceId);
      }

      return this;
    }

    private String extractComponentId() {
      String urlToParse = gotoUrl;
      final String extractedComponentId;
      if (gotoUrl.startsWith("/RpdcSearch/")) {
        int indexOf = urlToParse.indexOf("&componentId=");
        extractedComponentId = urlToParse.substring(indexOf + 13);
      } else {
        urlToParse = urlToParse.substring(1);
        int indexBegin = urlToParse.indexOf('/') + 1;
        int indexEnd = urlToParse.indexOf('/', indexBegin);
        extractedComponentId = urlToParse.substring(indexBegin, indexEnd);
      }
      return extractedComponentId;
    }

    private String extractPublicationId() {
      String extractedPublicationId = null;
      if (gotoUrl.contains("Type=Publication")) {
        var p = Pattern.compile(".+Id=(\\d{1,10}).*");
        Matcher m = p.matcher(gotoUrl);
        if (m.matches()) {
          extractedPublicationId = m.group(1);
        }
      }
      return extractedPublicationId;
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

    public String getAttachmentId() {
      return attachmentIdGoTo;
    }

    public String getPublicationId() {
      return publicationId;
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

    boolean isForceToLogin() {
      return forceToLogin;
    }

    String getGotoUrl() {
      return gotoUrl;
    }

    String getLanguage() {
      return language;
    }
  }
}

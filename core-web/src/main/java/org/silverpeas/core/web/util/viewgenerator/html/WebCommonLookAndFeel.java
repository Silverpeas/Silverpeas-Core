/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.ecs.Element;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.look.SilverpeasLook;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.silverpeas.core.util.JSONCodec.encodeObject;
import static org.silverpeas.core.util.Mutable.empty;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.URLUtil.getMinifiedWebResourceUrl;
import static org.silverpeas.core.web.look.SilverpeasLook.getSilverpeasLook;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.*;

class WebCommonLookAndFeel {

  public static final String LOOK_CONTEXT_MANAGER_CALLBACK_ONLY_ATTR = "lookContextManagerCallbackOnly";

  private static final String SILVERPEAS_JS = "silverpeas.js";
  private static final String STANDARD_CSS = "/util/styleSheets/silverpeas-main.css";
  private static final String STR_NEW_LINE = "\n";

  private static final WebCommonLookAndFeel instance = new WebCommonLookAndFeel();
  private static final String LOOK_CONTEXT_MANAGER_SPACE_ID = "LookContextManager.spaceId";
  private static final String LOOK_CONTEXT_MANAGER_COMPONENT_ID = "LookContextManager.componentId";

  private WebCommonLookAndFeel() {
  }

  public static WebCommonLookAndFeel getInstance() {
    return instance;
  }

  String getCommonHeader(HttpServletRequest req) {
    final HttpRequest request = HttpRequest.decorate(req);
    HttpSession session = request.getSession();
    MainSessionController controller = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
        GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    // Retrieving from the request, or from GraphicElementFactory instance the current space and
    // the current component of the user
    final String spaceId;
    final String componentId;
    final String[] context = (String[]) request.getAttribute("browseContext");
    final String lookContextManagerSpaceId = request.getParameter(LOOK_CONTEXT_MANAGER_SPACE_ID);
    if (StringUtil.isDefined(lookContextManagerSpaceId)) {
      // page of a regular component
      spaceId = lookContextManagerSpaceId;
      componentId = request.getParameter(LOOK_CONTEXT_MANAGER_COMPONENT_ID);
    } else if (isDefined(context)) {
      // page of a regular component
      spaceId = context[2];
      componentId = context[3];
    } else if (context != null) {
      // page of a "personal" component (context is not defined, but the associated request
      // attribute is set)
      spaceId = null;
      componentId = null;
    } else {
      // Not context set into the request attributes (so no context is defined)
      spaceId = gef.getSpaceIdOfCurrentRequest();
      componentId = gef.getComponentIdOfCurrentRequest();
    }
    if (request.getAttributeAsBoolean(LOOK_CONTEXT_MANAGER_CALLBACK_ONLY_ATTR)) {
      // cas of simple look context management
      return Optional.of(generateSpWindowLookContextUpdate(controller, getLookSettings(controller, spaceId), spaceId, componentId))
          .filter(StringUtil::isDefined)
          .map(JavascriptPluginInclusion::scriptContent)
          .map(Element::toString)
          .orElse(StringUtil.EMPTY);
    }
    return getCommonHeader(request, controller, spaceId, componentId);
  }

  private boolean isDefined(String[] context) {
    return StringUtil.isDefined(StringUtil.join(context));
  }

  private String getCommonHeader(HttpRequest request, MainSessionController controller,
      String spaceId, String componentId) {

    String language = controller.getFavoriteLanguage();
    final SettingBundle lookSettings = getLookSettings(controller, spaceId);

    String silverpeasUrl = URLUtil.getFullApplicationURL(request);
    String contextPath = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
    String charset =
        ResourceLocator.getGeneralSettingBundle().getString("charset", Charsets.UTF_8.name());
    final StringBuilder code = new StringBuilder();
    code.append("<link rel=\"icon\" href=\"").append(
        lookSettings.getString("favicon", request.getContextPath() + "/util/icons/favicon.ico"))
        .append("\"/>");
    code.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=");
    code.append(charset);
    code.append("\"/>\n");

    code.append(includeJQueryCss(new ElementContainer()).toString());

    // append default global CSS
    code.append(getCSSLinkTag(contextPath + STANDARD_CSS));

    // define CSS(default and specific) and JS (specific) dedicated to current component
    final Mutable<String> specificJS = empty();
    final Mutable<String> defaultComponentCSS = empty();
    final Mutable<String> specificComponentCSS = empty();
    computeComponentStuffs(lookSettings, contextPath, componentId, specificJS, defaultComponentCSS,
        specificComponentCSS);

    // append default CSS of current component
    defaultComponentCSS.ifPresent(code::append);

    // append specific look CSS
    String css = lookSettings.getString("StyleSheet", "");
    if (StringUtil.isDefined(css)) {
      code.append(getCSSLinkTag(css));
    }

    if (StringUtil.isDefined(spaceId)) {
      // load CSS file manually uploaded
      String cssUploadedOnSpace = getSilverpeasLook().getCSSOfSpace(spaceId);
      if (StringUtil.isDefined(cssUploadedOnSpace)) {
        code.append(getCSSLinkTag(cssUploadedOnSpace));
      }
    }

    // append specific CSS of current component*
    specificComponentCSS.ifPresent(code::append);
    code.append(includePolyfills(new ElementContainer()).toString()).append(STR_NEW_LINE);
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/mousetrap.min.js"));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/mousetrap-global-bind.min.js"));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/mousetrap-pause.min.js"));

    code.append(getJavaScriptTag(contextPath + "/util/javaScript/" +
        GraphicElementFactory.MOMENT_JS));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/" +
        GraphicElementFactory.MOMENT_TIMEZONE_JS));

    // append javascript
    code.append("<script type=\"text/javascript\">var webContext='")
        .append(contextPath)
        .append("';")
        .append(STR_NEW_LINE)
        .append("var silverpeasUrl = '")
        .append(silverpeasUrl)
        .append("';")
        .append(STR_NEW_LINE);
    if (request.getRequestURI().endsWith(lookSettings.getString("FrameJSP"))) {
      code.append("var __spWindow_main_frame = true;")
          .append(STR_NEW_LINE);
    }
    code.append(addGlobalJSVariable(controller))
        .append("</script>\n");

    code.append(getJavaScriptTagWithVersion(contextPath + "/util/javaScript/" + SILVERPEAS_JS));
    code.append(includeJQuery(new ElementContainer()).toString()).append(STR_NEW_LINE);
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/silverpeas-i18n.js"));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/jquery/jquery.cookie.js"));

    code.append(includeChat(new ElementContainer()).toString()).append(STR_NEW_LINE);

    code.append(includeLayout(new ElementContainer(),
        LookHelper.getLookHelper(controller.getHttpSession())).toString()).append(STR_NEW_LINE);
    Optional.of(generateSpWindowLookContextUpdate(controller, lookSettings, spaceId, componentId))
        .filter(StringUtil::isDefined)
        .ifPresent(c -> code.append(scriptContent(c)));

    code.append(includeAngular(new ElementContainer(), language).toString()).append(STR_NEW_LINE);
    code.append(includeVueJs(new ElementContainer()).toString()).append(STR_NEW_LINE);
    code.append(includeSecurityTokenizing(new ElementContainer()).toString()).append(STR_NEW_LINE);
    code.append(includeNotifier(new ElementContainer()).toString()).append(STR_NEW_LINE);
    code.append(includeSelectize(new ElementContainer()).toString()).append(STR_NEW_LINE);
    code.append(includePopup(new ElementContainer()).toString()).append(STR_NEW_LINE);
    code.append(includeUserZoom(new ElementContainer(), language).toString()).append(STR_NEW_LINE);
    code.append(includeCkeditorAddOns(new ElementContainer()).toString()).append(
        STR_NEW_LINE);
    code.append(includeMessager(new ElementContainer(), language).toString()).append(STR_NEW_LINE);

    specificJS.ifPresent(code::append);

    if (lookSettings.getString("OperationPane").toLowerCase().endsWith("web20")) {
      code.append(getYahooElements());
      code.append(includeResponsibles(new ElementContainer(), language).toString())
          .append(STR_NEW_LINE);
      code.append(includeMylinks(new ElementContainer()).toString()).append(STR_NEW_LINE);
    }

    code.append(includeVirtualKeyboard(new ElementContainer(), language).toString()).append(STR_NEW_LINE);

    return code.toString();
  }

  private String generateSpWindowLookContextUpdate(final MainSessionController controller,
      final SettingBundle lookSettings, final String spaceId, final String componentId) {
    final StringBuilder code = new StringBuilder();
    final String mainFrameUrl = Optional.of(lookSettings.getString("FrameJSP"))
        .map(f -> {
          String url = f;
          if (StringUtil.isDefined(spaceId)) {
            url = fromUri(url).queryParam(LOOK_CONTEXT_MANAGER_SPACE_ID, spaceId).build().toString();
          }
          if (StringUtil.isDefined(componentId)) {
            url = fromUri(url).queryParam(LOOK_CONTEXT_MANAGER_COMPONENT_ID, componentId).build().toString();
          }
          if (!url.startsWith(URLUtil.getApplicationURL())) {
            url = URLUtil.getApplicationURL() + url;
          }
          return url;
        })
        .orElse(null);
    final String look;
    final String defaultLook = controller.getFavoriteLook();
    final String css;
    final String wallpaper;
    final List<String> spacePathIds;
    if (StringUtil.isDefined(spaceId)) {
      spacePathIds = OrganizationController.get().getPathToSpace(spaceId).stream()
          .map(SpaceInstLight::getId)
          .collect(Collectors.toList());
      final SilverpeasLook silverpeasLook = getSilverpeasLook();
      look = defaultStringIfNotDefined(silverpeasLook.getSpaceLook(spaceId), defaultLook);
      css = ofNullable(silverpeasLook.getCSSOfSpace(spaceId)).map(JavascriptPluginInclusion::normalizeWebResourceUrl).orElse(null);
      wallpaper = ofNullable(silverpeasLook.getWallpaperOfSpace(spaceId)).map(JavascriptPluginInclusion::normalizeWebResourceUrl).orElse(null);
    } else {
      spacePathIds = emptyList();
      look = defaultLook;
      css = null;
      wallpaper = null;
    }
    code.append("if(top.spWindow){");
    code.append("top.spWindow.updateLookContext(");
    code.append(encodeObject(o -> o
        .put("mainFrameUrl", mainFrameUrl)
        .putJSONArray("currentSpacePathIds", a -> {
          spacePathIds.forEach(a::add);
          return a;
        })
        .put("currentComponentId", defaultStringIfNotDefined(componentId))
        .put("defaultLook", defaultLook)
        .put("look", look)
        .put("bannerHeight", lookSettings.getString("banner.height", "115") + "px")
        .put("footerHeight", lookSettings.getString("footer.height", "26") + "px")
        .put("css", css)
        .put("wallpaper", wallpaper)
        .put("defaultWallpaper", ofNullable(lookSettings.getString("banner.wallPaper", null))
            .filter(StringUtil::isDefined)
            .orElse("imgDesign/bandeau.jpg"))));
    code.append(");}");
    code.append(STR_NEW_LINE);
    return code.toString();
  }

  private void computeComponentStuffs(final SettingBundle lookSettings, final String contextPath,
      final String componentId, final Mutable<String> specificJS,
      final Mutable<String> defaultComponentCSS, final Mutable<String> specificComponentCSS) {
    if (StringUtil.isDefined(componentId)) {
      SilverpeasComponentInstance component =
          OrganizationControllerProvider.getOrganisationController()
              .getComponentInstance(componentId).orElse(null);
      if (component != null) {
        String componentName = component.getName();
        String genericComponentName = getGenericComponentName(componentName);
        if (component.isWorkflow()) {
          genericComponentName = "processManager";
        }
        defaultComponentCSS.set(getCSSLinkTag(contextPath + "/" + genericComponentName
            + "/jsp/styleSheets/" + genericComponentName + ".css"));
        String specificStyle = lookSettings.getString("StyleSheet." + componentName, "");
        if (StringUtil.isDefined(specificStyle)) {
          specificComponentCSS.set(getCSSLinkTag(specificStyle));
        }
        final String specificJs = lookSettings.getString("JavaScript." + componentName, "");
        if (StringUtil.isDefined(specificJs)) {
          specificJS.set(getJavaScriptTagWithVersion(specificJs));
        }
      }
    }
  }

  /**
   * Gets look settings accoring to the context.
   * @param controller the main controller.
   * @param spaceId the identifier of the current space.
   * @return the right settings.
   */
  private SettingBundle getLookSettings(final MainSessionController controller,
      final String spaceId) {
    String userLookName = controller.getFavoriteLook();
    SettingBundle lookSettings = GraphicElementFactory.getLookSettings(userLookName);
    if (StringUtil.isDefined(spaceId)) {
      String spaceLook = getSilverpeasLook().getSpaceLook(spaceId);
      if (StringUtil.isDefined(spaceLook)) {
        lookSettings = GraphicElementFactory.getLookSettings(spaceLook);
      }
    }
    return lookSettings;
  }

  private String getCSSLinkTag(String href) {
    return link(href).toString();
  }

  private String getJavaScriptTag(String src) {
    String normalizedUrl = getMinifiedWebResourceUrl(src);
    return new script().setType("text/javascript").setSrc(normalizedUrl).toString() + STR_NEW_LINE;
  }

  private String getJavaScriptTagWithVersion(String src) {
    String normalizedUrl = getMinifiedWebResourceUrl(src);
    return getJavaScriptTag(URLUtil.addFingerprintVersionOn(normalizedUrl));
  }

  /**
   * Some logical components have got the same technical component. For example, "toolbox" component
   * is technically "kmelia"
   * @return the "implementation" name of the given component
   */
  private String getGenericComponentName(String componentName) {
    if ("toolbox".equalsIgnoreCase(componentName) || "kmax".equalsIgnoreCase(componentName)) {
      return "kmelia";
    }
    if ("pollingstation".equalsIgnoreCase(componentName)) {
      return "survey";
    }
    return componentName;
  }

  @SuppressWarnings("StringBufferReplaceableByString")
  private String addGlobalJSVariable(MainSessionController controller) {
    final String language = controller.getFavoriteLanguage();
    final ZoneId zoneId = controller.getFavoriteZoneId();
    StringBuilder globalJSVariableBuilder = new StringBuilder();
    globalJSVariableBuilder.append("moment.locale('").append(language).append("');")
        .append(STR_NEW_LINE);
    globalJSVariableBuilder.append("var userLanguage = '").append(language)
        .append("';").append(STR_NEW_LINE);
    globalJSVariableBuilder.append("function getUserLanguage() { return userLanguage;")
        .append(" }").append(STR_NEW_LINE);
    globalJSVariableBuilder.append("function getString(key) { return sp.i18n.get(key); }")
        .append(STR_NEW_LINE);
    globalJSVariableBuilder.append("var currentUserId = '").append(controller.getUserId())
        .append("';").append(STR_NEW_LINE);
    globalJSVariableBuilder.append("var currentUser = ").append(encodeObject(j -> {
      final UserDetail currentUserDetail = controller.getCurrentUserDetail();
      if (currentUserDetail != null) {
        j.put("id", currentUserDetail.getId())
         .put("domainId", currentUserDetail.getDomainId())
         .put("language", language)
         .put("zoneId", zoneId.getId());
      }
      return j;
    })).append(";").append(STR_NEW_LINE);
    return globalJSVariableBuilder.toString();
  }

  @SuppressWarnings("StringBufferReplaceableByString")
  private String getYahooElements() {
    String contextPath = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
    StringBuilder code = new StringBuilder();

    code.append("<!-- CSS for Menu -->\n");
    code.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
    code.append(GraphicElementFactory.getSettings().getString("YUIMenuCss",
        contextPath + "/util/yui/menu/assets/menu.css"));
    code.append("\"/>\n");
    code.append("<!-- Page-specific styles -->\n");
    code.append("<style type=\"text/css\">\n");
    code.append("    div.yuimenu {\n");
    code.append("    position:dynamic;\n");
    code.append("    visibility:hidden;\n");
    code.append("    }\n");
    code.append("</style>\n");
    code.append(getJavaScriptTag(contextPath + "/util/yui/yahoo-dom-event/yahoo-dom-event.js"));
    code.append(getJavaScriptTag(contextPath + "/util/yui/container/container_core-min.js"));
    code.append(getJavaScriptTag(contextPath + "/util/yui/menu/menu-min.js"));
    return code.toString();
  }

}

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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.look.SilverpeasLook;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.time.ZoneId;

import static org.silverpeas.core.util.URLUtil.getMinifiedWebResourceUrl;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.*;

class WebCommonLookAndFeel {

  private static final String SILVERPEAS_JS = "silverpeas.js";
  private static final String STANDARD_CSS = "/util/styleSheets/silverpeas-main.css";
  private static final String STR_NEW_LINE = "\n";

  private static final WebCommonLookAndFeel instance = new WebCommonLookAndFeel();

  private WebCommonLookAndFeel() {
  }

  public static WebCommonLookAndFeel getInstance() {
    return instance;
  }

  String getCommonHeader(HttpServletRequest request) {
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
    if (isDefined(context)) {
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

    return getCommonHeader(request, controller, spaceId, componentId);
  }

  private boolean isDefined(String[] context) {
    return StringUtil.isDefined(StringUtil.join(context));
  }

  @SuppressWarnings("StringBufferReplaceableByString")
  private String getCommonHeader(HttpServletRequest request, MainSessionController controller,
      String spaceId, String componentId) {

    String language = controller.getFavoriteLanguage();
    SettingBundle lookSettings = getLookSettings(controller, spaceId);

    String silverpeasUrl = URLUtil.getFullApplicationURL(request);
    String contextPath = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
    String charset =
        ResourceLocator.getGeneralSettingBundle().getString("charset", Charsets.UTF_8.name());
    StringBuilder code = new StringBuilder();
    code.append("<link rel=\"icon\" href=\"")
        .append(getLookSettings(controller, spaceId).getString("favicon",
            request.getContextPath() + "/util/icons/favicon.ico"))
        .append("\"/>");
    code.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=");
    code.append(charset);
    code.append("\"/>\n");

    String specificJS = null;

    code.append(includeJQueryCss(new ElementContainer()).toString());

    // append default global CSS
    code.append(getCSSLinkTag(contextPath + STANDARD_CSS));

    // define CSS(default and specific) and JS (specific) dedicated to current component
    String defaultComponentCSS = null;
    String specificComponentCSS = null;
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
        defaultComponentCSS = getCSSLinkTag(contextPath + "/" + genericComponentName
            + "/jsp/styleSheets/" + genericComponentName + ".css");

        String specificStyle = lookSettings.getString("StyleSheet." + componentName, "");
        if (StringUtil.isDefined(specificStyle)) {
          specificComponentCSS = getCSSLinkTag(specificStyle);
        }

        specificJS = lookSettings.getString("JavaScript." + componentName, "");
      }
    }

    // append default CSS of current component
    if (defaultComponentCSS != null) {
      code.append(defaultComponentCSS);
    }

    // append specific look CSS
    String css = lookSettings.getString("StyleSheet", "");
    if (StringUtil.isDefined(css)) {
      code.append(getCSSLinkTag(css));
    }

    if (StringUtil.isDefined(spaceId)) {
      // load CSS file manually uploaded
      String cssUploadedOnSpace = SilverpeasLook.getSilverpeasLook().getCSSOfSpace(spaceId);
      if (StringUtil.isDefined(cssUploadedOnSpace)) {
        code.append(getCSSLinkTag(cssUploadedOnSpace));
      }
    }

    // append specific CSS of current component
    if (specificComponentCSS != null) {
      code.append(specificComponentCSS);
    }
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

    if (StringUtil.isDefined(specificJS)) {
      code.append(getJavaScriptTag(specificJS));
    }

    if (lookSettings.getString("OperationPane").toLowerCase().endsWith("web20")) {
      code.append(getYahooElements());
      code.append(includeResponsibles(new ElementContainer(), language).toString())
          .append(STR_NEW_LINE);
      code.append(includeMylinks(new ElementContainer()).toString()).append(STR_NEW_LINE);
    }


    return code.toString();
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
      String spaceLook = SilverpeasLook.getSilverpeasLook().getSpaceLook(spaceId);
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
    return getJavaScriptTag(URLUtil.appendVersion(normalizedUrl));
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
    globalJSVariableBuilder.append("var currentUser = ").append(JSONCodec.encodeObject(j -> {
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

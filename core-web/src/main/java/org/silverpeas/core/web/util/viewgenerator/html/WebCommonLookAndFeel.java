/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.silverpeas.core.web.look.SilverpeasLook;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.apache.commons.lang3.CharEncoding;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.*;

public class WebCommonLookAndFeel {

  private static final String SILVERPEAS_JS = "silverpeas.js";
  private static final String STANDARD_CSS = "/util/styleSheets/globalSP_SilverpeasV5.css";
  private static final String STR_NEW_LINE = "\n";

  private static final WebCommonLookAndFeel instance = new WebCommonLookAndFeel();

  private WebCommonLookAndFeel() {
  }

  public static WebCommonLookAndFeel getInstance() {
    return instance;
  }

  public String getCommonHeader(HttpServletRequest request) {
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

    return getCommonHeader(controller, spaceId, componentId);
  }

  private boolean isDefined(String[] context) {
    return StringUtil.isDefined(StringUtil.join(context));
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getCommonHeader(MainSessionController controller, String spaceId, String componentId) {

    String language = controller.getFavoriteLanguage();
    String userLookName = controller.getFavoriteLook();

    SettingBundle lookSettings = GraphicElementFactory.getLookSettings(userLookName);
    if (StringUtil.isDefined(spaceId)) {
      String spaceLook = SilverpeasLook.getSilverpeasLook().getSpaceLook(spaceId);
      if (StringUtil.isDefined(spaceLook)) {
        lookSettings = GraphicElementFactory.getLookSettings(spaceLook);
      }
    }

    String contextPath = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
    String charset =
        ResourceLocator.getGeneralSettingBundle().getString("charset", CharEncoding.UTF_8);
    StringBuilder code = new StringBuilder();
    code.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=");
    code.append(charset);
    code.append("\"/>\n");

    String specificJS = null;

    code.append(getCSSLinkTag(contextPath + "/util/styleSheets/jquery/" +
        GraphicElementFactory.JQUERYUI_CSS));

    // append default global CSS
    code.append(getCSSLinkTagWithVersion(contextPath + STANDARD_CSS));

    // define CSS(default and specific) and JS (specific) dedicated to current component
    String defaultComponentCSS = null;
    String specificComponentCSS = null;
    if (StringUtil.isDefined(componentId)) {
      ComponentInstLight component =
          OrganizationControllerProvider.getOrganisationController().getComponentInstLight(
              componentId);
      if (component != null) {
        String componentName = component.getName();
        String genericComponentName = getGenericComponentName(componentName);
        if (component.isWorkflow()) {
          genericComponentName = "processManager";
        }
        defaultComponentCSS = getCSSLinkTagWithVersion(contextPath + "/" + genericComponentName
            + "/jsp/styleSheets/" + genericComponentName + ".css");

        String specificStyle = lookSettings.getString("StyleSheet." + componentName, "");
        if (StringUtil.isDefined(specificStyle)) {
          specificComponentCSS = getCSSLinkTagWithVersion(specificStyle);
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
      code.append(getCSSLinkTagWithVersion(css));
    }

    if (StringUtil.isDefined(spaceId)) {
      // load CSS file manually uploaded
      String cssUploadedOnSpace = SilverpeasLook.getSilverpeasLook().getCSSOfSpace(spaceId);
      if (StringUtil.isDefined(cssUploadedOnSpace)) {
        code.append(getCSSLinkTagWithVersion(cssUploadedOnSpace));
      }
    }

    // append specific CSS of current component
    if (specificComponentCSS != null) {
      code.append(specificComponentCSS);
    }
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/polyfill/array.generics.min.js"));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/polyfill/es6-promise.min.js"));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/polyfill/classList.min.js"));
    code.append(
        getJavaScriptTag(contextPath + "/util/javaScript/polyfill/eventListenerIEPolyfill.min.js"));
    code.append(
        getJavaScriptTag(contextPath + "/util/javaScript/polyfill/silverpeas-polyfills.js"));

    // append javascript
    code.append("<script type=\"text/javascript\">var webContext='").append(contextPath).append
        ("';").append(STR_NEW_LINE).append(addGlobalJSVariable(
        language)).append("</script>\n");

    code.append(getJavaScriptTagWithVersion(contextPath + "/util/javaScript/" + SILVERPEAS_JS));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/jquery/" +
        GraphicElementFactory.JQUERY_JS));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/jquery/" +
        GraphicElementFactory.JQUERYJSON_JS));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/jquery/" +
        GraphicElementFactory.JQUERYJSON_JS));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/jquery/" +
        GraphicElementFactory.JQUERYUI_JS));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/jquery/" +
        GraphicElementFactory.JQUERY_i18N_JS));
    code.append(getJavaScriptTag(contextPath + "/util/javaScript/jquery/jquery.cookie.js"));

    code.append(includeAngular(new ElementContainer(), language).toString()).append(STR_NEW_LINE);
    code.append(includeSecurityTokenizing(new ElementContainer()).toString()).append(STR_NEW_LINE);
    code.append(includeNotifier(new ElementContainer()).toString()).append(STR_NEW_LINE);
    code.append(includeUserZoom(new ElementContainer()).toString()).append(STR_NEW_LINE);
    code.append(includeCkeditorAddOns(new ElementContainer(), language).toString()).append(
        STR_NEW_LINE);

    if (StringUtil.isDefined(specificJS)) {
      code.append(getJavaScriptTag(specificJS));
    }

    if (lookSettings != null
        && lookSettings.getString("OperationPane").toLowerCase().endsWith("web20")) {
      code.append(getYahooElements());
      code.append(
          JavascriptPluginInclusion.includeResponsibles(new ElementContainer(), language)
              .toString()).append(STR_NEW_LINE);
      code.append(JavascriptPluginInclusion.includeMylinks(new ElementContainer()).toString())
          .append(STR_NEW_LINE);
    }


    return code.toString();
  }

  private String getCSSLinkTag(String href) {
    return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + href + "\"/>\n";
  }

  private String getCSSLinkTagWithVersion(String href) {
    return getCSSLinkTag(URLUtil.appendVersion(href));
  }

  private String getJavaScriptTag(String src) {
    return new script().setType("text/javascript").setSrc(src).toString() + STR_NEW_LINE;
  }

  private String getJavaScriptTagWithVersion(String src) {
    return getJavaScriptTag(URLUtil.appendVersion(src));
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

  private String addGlobalJSVariable(String language) {
    StringBuilder globalJSVariableBuilder = new StringBuilder();
    globalJSVariableBuilder.append("var userLanguage = '").append(language).append("';")
        .append(STR_NEW_LINE);
    globalJSVariableBuilder.append("function getUserLanguage() { return userLanguage;")
        .append(" }").append(STR_NEW_LINE);
    globalJSVariableBuilder.append("function getString(key) { return $.i18n.prop(key); }").append(
        STR_NEW_LINE);
    return globalJSVariableBuilder.toString();
  }

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

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

package org.silverpeas.core.web.util.viewgenerator.html.window;

import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar;
import org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane;
import org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPaneType;

/**
 * @author neysseri
 */
public abstract class AbstractWindow implements Window {

  private BrowseBar browseBar = null;
  private OperationPane operationPane = null;
  private GraphicElementFactory gef = null;
  private String body = null;
  private String width = null;
  private boolean browserBarDisplayable = true;
  private boolean popup = false;
  String contextualDiv = null;

  /**
   * Constructor declaration
   * @see
   */
  public AbstractWindow() {
  }

  /**
   * Method declaration
   * @param gef
   * @see
   */
  @Override
  public void init(GraphicElementFactory gef) {
    this.gef = gef;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getBody() {
    return this.body;
  }

  /**
   * Method declaration
   * @param body
   * @see
   */
  @Override
  public void addBody(String body) {
    this.body = body;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public GraphicElementFactory getGEF() {
    return this.gef;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  /**
   * Method declaration
   * @param width
   * @see
   */
  @Override
  public void setWidth(String width) {
    this.width = width;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getWidth() {
    if (this.width == null) {
      this.width = "100%";
    }
    return this.width;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public OperationPane getOperationPane() {
    if (this.operationPane == null) {
      this.operationPane = getGEF().getOperationPane();
      if (!isPopup() &&
          ResourceLocator.getGeneralSettingBundle().getBoolean("AdminFromComponentEnable", true) &&
          StringUtil.isDefined(getGEF().getComponentIdOfCurrentRequest())) {
        addOperationToSetupComponent();
      }
    }
    return this.operationPane;
  }

  private void addOperationToSetupComponent() {
    MainSessionController msc = getGEF().getMainSessionController();
    OrganizationController organizationController =
        OrganizationControllerProvider.getOrganisationController();
    if (organizationController.isComponentManageable(getGEF().getComponentIdOfCurrentRequest(),
        msc.getUserId()) && getGEF().isComponentMainPage()) {
      String label = ResourceLocator.getGeneralLocalizationBundle(
          getGEF().getMultilang().getLocale().getLanguage())
              .getString("GML.operations.setupComponent");
      String url = URLUtil.getApplicationURL() + "/R" + URLUtil.CMP_JOBSTARTPAGEPEAS +
          "/jsp/SetupComponent?ComponentId=" + getGEF().getComponentIdOfCurrentRequest();
      this.operationPane.addOperation("useless", label, url);
      this.operationPane.addLine();
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public BrowseBar getBrowseBar() {
    if (this.browseBar == null) {
      this.browseBar = getGEF().getBrowseBar();
    }
    return this.browseBar;
  }

  public abstract String getContextualDiv();

  @Override
  public boolean isBrowseBarVisible() {
    return this.browserBarDisplayable;
  }

  @Override
  public void setBrowseBarVisibility(boolean browseBarVisible) {
    this.browserBarDisplayable = browseBarVisible;
  }

  @Override
  public boolean isPopup() {
    return popup;
  }

  @Override
  public void setPopup(boolean popup) {
    this.popup = popup;
  }

  private String getWelcomeMessage(ComponentInstLight component, String language) {
    String message = null;
    String fileName = null;
    try {
      fileName = "welcome_" + language;
      message = getSilverpeasTemplate().applyFileTemplateOnComponent(component.getName(), fileName);
    } catch (Exception e) {

    }
    if (!StringUtil.isDefined(message)) {
      return null;
    }
    StringBuilder sb = new StringBuilder(100);
    String title = getGEF().getMultilang().getString("GEF.welcome.title");
    sb.append("<div id=\"welcome-message\" title=\"").append(title)
        .append("\" style=\"display: none;\">\n");
    sb.append("<p>\n");
    sb.append(message);
    sb.append("</p>\n");
    sb.append("</div>\n");
    return sb.toString();
  }

  private SilverpeasTemplate getSilverpeasTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents();
  }

  private String getWelcomeMessageScript(ComponentInstLight component) {
    StringBuilder sb = new StringBuilder(100);
    sb.append("<script type=\"text/javascript\">\n");
    sb.append("var welcomeMessageAlreadyShown = false;\n");
    sb.append("$(function() {\n");
    sb.append("var welcomeMessageCookieName = \"Silverpeas_").append(component.getName())
        .append("_WelcomeMessage\";\n");
    sb
        .append("if (!welcomeMessageAlreadyShown && \"IKnowIt\" != $.cookie(welcomeMessageCookieName)) {\n");
    sb.append("if (!welcomeMessageAlreadyShown) {\n");
    sb.append("welcomeMessageAlreadyShown = true;\n");
    sb.append("$('#welcome-message').dialog({\n");
    sb.append("modal: true,\n");
    sb.append("resizable: false,\n");
    sb.append("width: 400,\n");
    sb.append("dialogClass: 'help-modal-message',\n");
    sb.append("buttons: {\n");
    sb.append("\"").append(getGEF().getMultilang().getString("GEF.welcome.button.ok"))
        .append("\": function() {\n");
    sb.append("$.cookie(welcomeMessageCookieName, \"IKnowIt\", { expires: 3650, path: '/' });\n");
    sb.append("$(this).dialog(\"close\");\n");
    sb.append(" }, \n");
    sb.append("\"").append(getGEF().getMultilang().getString("GEF.welcome.button.reminder"))
        .append("\": function() {\n");
    sb.append(" $(this).dialog(\"close\");\n");
    sb.append(" }\n");
    sb.append("}\n");
    sb.append("});\n");
    sb.append("}\n");
    sb.append("}\n");
    sb.append("}\n");
    sb.append(");\n");
    sb.append("</script>");
    return sb.toString();
  }

  public String displayWelcomeMessage() {
    if (getGEF().isComponentMainPage()) {
      String componentId = getGEF().getComponentIdOfCurrentRequest();
      if (StringUtil.isDefined(componentId)) {
        StringBuilder sb = new StringBuilder(300);
        ComponentInstLight component = OrganizationControllerProvider.getOrganisationController()
                .getComponentInstLight(componentId);
        String language = getGEF().getMainSessionController().getFavoriteLanguage();
        if (component != null) {
          String message = getWelcomeMessage(component, language);
          if (message == null) {
            // Welcome message is not yet defined for this application
            // So, display nothing at all !
            return "";
          }
          sb.append(message);
          sb.append(getWelcomeMessageScript(component));
        }
        return sb.toString();
      }
    }
    return "";
  }

  /**
   * Add an operation that permits to
   * <ul>
   * <li>display space or application manager</li>
   * <li>add space or application to user bookmarks</li>
   * </ul>
   */
  protected void addSpaceOrComponentOperations() {
    if (!isPopup() && !getGEF().getMainSessionController().getCurrentUserDetail().isAnonymous() &&
        !OperationPaneType.personalSpace.equals(getOperationPane().getType())) {
      if ((OperationPaneType.space.equals(getOperationPane().getType()) &&
          StringUtil.isDefined(getGEF().getSpaceIdOfCurrentRequest())) ||
          StringUtil.isDefined(getGEF().getComponentIdOfCurrentRequest())) {
        if (getOperationPane().nbOperations() > 0) {
          getOperationPane().addLine();
        }
        final String viewMgrLabel, addFavLabel;
        final String viewMgrAction, addFavAction;
        boolean addFavOperation = true;
        LocalizationBundle bundle = ResourceLocator.getGeneralLocalizationBundle(
            getGEF().getMultilang().getLocale().getLanguage());
        if (OperationPaneType.space.equals(getOperationPane().getType())) {
          viewMgrLabel = bundle.getString("GML.space.responsibles").replaceAll("''", "'");
          viewMgrAction =
              "displaySpaceResponsibles('" + getGEF().getMainSessionController().getUserId() +
                  "','" + getGEF().getSpaceIdOfCurrentRequest() + "')";
          addFavLabel = bundle.getString("GML.favorite.space.add");
          addFavAction = "addFavoriteSpace('" + getGEF().getSpaceIdOfCurrentRequest() + "')";
        } else {
          viewMgrLabel = bundle.getString("GML.component.responsibles").replaceAll("''", "'");
          viewMgrAction =
              "displayComponentResponsibles('" + getGEF().getMainSessionController().getUserId() +
                  "','" + getGEF().getComponentIdOfCurrentRequest() + "')";
          addFavLabel = bundle.getString("GML.favorite.application.add");
          addFavAction = "addFavoriteApp('" + getGEF().getComponentIdOfCurrentRequest() + "')";
          addFavOperation = getGEF().isComponentMainPage();
        }
        if (addFavOperation) {
          getOperationPane().addOperation("", addFavLabel, "javascript:" + addFavAction + ";",
              "space-or-application-favorites-operation");
        }
        getOperationPane().addOperation("", viewMgrLabel, "javascript:" + viewMgrAction + ";",
            "space-or-component-responsibles-operation");
      }
    }
  }

  @Override
  public String printBefore() {
    StringBuilder result = new StringBuilder(200);
    String width = getWidth();
    int nbCols = 1;
    addSpaceOrComponentOperations();
    if (getOperationPane().nbOperations() > 0) {
      nbCols = 2;
    }

    contextualDiv = getContextualDiv();
    if (StringUtil.isDefined(contextualDiv)) {
      result.append(contextualDiv);
    }

    result.append("<table width=\"").append(width).append(
        "\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" id=\"topPage\">");
    if (isBrowseBarVisible()) {
      if (isPopup()) {
        getBrowseBar().setClickable(false);
      }
      result.append("<tr><td class=\"cellBrowseBar\" width=\"100%\">");
      result.append(getBrowseBar().print());
      result.append("</td>");
      if (nbCols == 2) {
        result.append("<td align=\"right\" class=\"cellOperation\" nowrap=\"nowrap\">");
        result.append(getOperationPane().print());
        result.append("</td>");
      } else {
        result.append("<td align=\"right\" class=\"cellOperation\" nowrap=\"nowrap\">");
        result.append("&nbsp;");
        result.append("</td>");
      }
      result.append("</tr>");
    }
    result.append("<tr><td width=\"100%\" valign=\"top\" colspan=\"2\" class=\"cellBodyWindows\">");
    result
        .append(
        "<table border=\"0\" width=\"100%\" cellpadding=\"5\" cellspacing=\"5\"><tr><td valign=\"top\">");
    return result.toString();
  }

  @Override
  public String printAfter() {
    StringBuilder result = new StringBuilder(200);
    String iconsPath = getIconsPath();
    result.append("</td></tr></table></td></tr></table>");

    if (!isPopup()) {
      result.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
      result.append("<tr><td class=\"basGaucheWindow\">");
      result.append("<img src=\"").append(iconsPath).append("/1px.gif\" width=\"1\" alt=\"\"/>\n");
      result.append("</td><td class=\"basMilieuWindow\">");
      result.append("<img src=\"").append(iconsPath).append("/1px.gif\" width=\"1\" alt=\"\"/>\n");
      result.append("</td><td class=\"basDroiteWindow\">");
      result.append("<img src=\"").append(iconsPath).append("/1px.gif\" width=\"1\" alt=\"\"/>\n");
      result.append("</td></tr></table>");

      result.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
      result.append("<tr><td>");
      result.append("<div align=\"left\"><a href=\"#topPage\"><img src=\"").append(iconsPath).append(
          "/goTop.gif\" border=\"0\" alt=\"\"/></a></div>");
      result.append("</td><td width=\"100%\">");
      result.append("&nbsp;");
      result.append("</td><td>");
      result.append("<div align=\"right\"><a href=\"#topPage\"><img src=\"")
          .append(iconsPath).append("/goTop.gif\" border=\"0\" alt=\"\"/></a></div>");
      result.append("</td></tr></table>");
    } else {
      Object includePopupResizeJsDone =
          CacheServiceProvider.getRequestCacheService().get("@includePopupResizeJsDone@");
      if (includePopupResizeJsDone == null) {
        StringBuilder popupResizeJs = new StringBuilder();
        popupResizeJs.append("jQuery(document.body).ready(");
        popupResizeJs.append("function(){");
        popupResizeJs.append("currentPopupResize();");
        popupResizeJs.append("});");
        result.append(new script().setType("text/javascript").addElement(popupResizeJs.toString())
            .toString());
        CacheServiceProvider.getRequestCacheService().put("@includePopupResizeJsDone@", true);
      }
    }
    if (StringUtil.isDefined(contextualDiv)) {
      result.append("</div>");
    }

    result.append(displayWelcomeMessage());

    return result.toString();
  }

  @Override
  public String print() {
    StringBuilder result = new StringBuilder(500);
    result.append(printBefore());
    result.append(getBody());
    result.append(printAfter());
    return result.toString();
  }
}
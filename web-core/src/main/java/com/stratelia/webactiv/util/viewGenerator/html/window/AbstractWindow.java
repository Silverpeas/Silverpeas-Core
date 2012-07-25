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

package com.stratelia.webactiv.util.viewGenerator.html.window;

import java.util.List;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar;
import com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane;

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
  public OperationPane getOperationPane() {
    if (this.operationPane == null) {
      this.operationPane = getGEF().getOperationPane();
      if (GeneralPropertiesManager.getGeneralResourceLocator().getBoolean(
          "AdminFromComponentEnable", true) &&
          StringUtil.isDefined(getGEF().getComponentId())) {
        addOperationToSetupComponent();
      }
    }
    return this.operationPane;
  }

  private void addOperationToSetupComponent() {
    MainSessionController msc = getGEF().getMainSessionController();
    if (msc.getOrganizationController().isComponentManageable(getGEF().getComponentId(),
        msc.getUserId()) && getGEF().isComponentMainPage()) {
      String label =
          GeneralPropertiesManager.getGeneralMultilang(getGEF().getMultilang().getLanguage())
          .getString("GML.operations.setupComponent");
      String url =
          URLManager.getApplicationURL() + "/R" + URLManager.CMP_JOBSTARTPAGEPEAS +
          "/jsp/SetupComponent?ComponentId=" + getGEF().getComponentId();
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

  public String getContextualDiv() {
    String spaceIds = "";
    String componentId = gef.getComponentId();
    OrganizationController oc = gef.getMainSessionController().getOrganizationController();
    if (StringUtil.isDefined(componentId)) {
      List<SpaceInst> spaces = oc.getSpacePathToComponent(componentId);

      for (SpaceInst spaceInst : spaces) {
        String spaceId = spaceInst.getId();
        if (!spaceId.startsWith("WA")) {
          spaceId = "WA" + spaceId;
        }
        spaceIds += spaceId + " ";
      }
    }

    if (StringUtil.isDefined(spaceIds)) {
      ComponentInstLight component = oc.getComponentInstLight(componentId);
      return "<div class=\"" + spaceIds + component.getName() + " " + componentId + "\">";
    }
    return null;
  }

  @Override
  public boolean isBrowseBarVisible() {
    return this.browserBarDisplayable;
  }

  @Override
  public void setBrowseBarVisibility(boolean browseBarVisible) {
    this.browserBarDisplayable = browseBarVisible;
  }

  private String getWelcomeMessage(ComponentInstLight component, String language) {
    String message = null;
    String fileName = null;
    try {
      fileName = "welcome_" + language;
      message = getSilverpeasTemplate().applyFileTemplateOnComponent(component.getName(), fileName);
    } catch (Exception e) {
      SilverTrace.info("viewgenerator", "AbstractWindow.getWelcomeMessage", "CANT_FIND_TEMPLATE",
          "fileName = " + fileName + ", component = " + component.getName());
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
      String componentId = getGEF().getComponentId();
      if (StringUtil.isDefined(componentId)) {
        StringBuilder sb = new StringBuilder(300);
        ComponentInstLight component =
            getGEF().getMainSessionController().getOrganizationController()
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
}
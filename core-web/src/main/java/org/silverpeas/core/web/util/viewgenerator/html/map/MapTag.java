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

package org.silverpeas.core.web.util.viewgenerator.html.map;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class MapTag extends TagSupport {

  private static final long serialVersionUID = 1425756234498404463L;
  private String spaceId;
  private boolean displayAppIcon;
  private boolean displayAppsFirst;
  private String callbackJSForMainSpace;
  private String callbackJSForApps;
  private String callbackJSForSubspaces;

  public String getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public boolean isDisplayAppIcon() {
    return displayAppIcon;
  }

  public void setDisplayAppIcon(final boolean displayAppIcon) {
    this.displayAppIcon = displayAppIcon;
  }

  public boolean isDisplayAppsFirst() {
    return displayAppsFirst;
  }

  public void setDisplayAppsFirst(final boolean displayAppsFirst) {
    this.displayAppsFirst = displayAppsFirst;
  }

  public String getCallbackJSForMainSpace() {
    return callbackJSForMainSpace;
  }

  public void setCallbackJSForMainSpace(final String callbackJSForMainSpace) {
    this.callbackJSForMainSpace = callbackJSForMainSpace;
  }

  public String getCallbackJSForApps() {
    return callbackJSForApps;
  }

  public void setCallbackJSForApps(final String callbackJSForApps) {
    this.callbackJSForApps = callbackJSForApps;
  }

  public String getCallbackJSForSubspaces() {
    return callbackJSForSubspaces;
  }

  public void setCallbackJSForSubspaces(final String callbackJSForSubspaces) {
    this.callbackJSForSubspaces = callbackJSForSubspaces;
  }

  @Override
  public int doStartTag() throws JspException {
    try {
      LookHelper helper = LookHelper.getLookHelper(pageContext.getSession());
      boolean showHiddenComponents = helper.getSettings("display.all.components", false);
      pageContext.getOut().print(printSpaceAndSubSpaces(spaceId, showHiddenComponents));
    } catch (IOException e) {
      throw new JspException("Can't display the site map", e);
    }
    return SKIP_BODY;
  }

  private String printSpaceAndSubSpaces(String spaceId, boolean showHiddenComponents) {
    MainSessionController sessionController = getMainSessionController();
    OrganizationController organisationController =
        OrganizationControllerProvider.getOrganisationController();
    SpaceInst spaceInst = organisationController.getSpaceInstById(spaceId);
    StringBuilder result = new StringBuilder(500);
    if (spaceInst != null) {
      String language = sessionController.getFavoriteLanguage();

      String spaceHref =
          "<a href=\"" + URLUtil.getSimpleURL(URLUtil.URL_SPACE, spaceInst.getId()) +
              "\" target=\"_top\">";
      if (spaceInst.getLevel() == 0 && StringUtil.isDefined(getCallbackJSForMainSpace())) {
        spaceHref =
            "<a href=\"javascript:" + getCallbackJSForMainSpace() + "('" + spaceInst.getId() +
                "');\">";
      } else if (spaceInst.getLevel() > 0 && StringUtil.isDefined(getCallbackJSForSubspaces())) {
        spaceHref =
            "<a href=\"javascript:" + getCallbackJSForSubspaces() + "('" + spaceInst.getId() +
                "');\">";
      }

      result.append("<li class=\"space\" id=\"space-").append(spaceInst.getId()).append("\">");
      result.append(spaceHref).append(spaceInst.getName(language));
      result.append("</a>\n");

      result.append("<ul>");

      if (displayAppsFirst) {
        // Get apps
        result.append(printApps(spaceId, showHiddenComponents));
      }

      // Get sub spaces
      result.append(printSubspaces(spaceId, showHiddenComponents));

      if (!displayAppsFirst) {
        // Get apps
        result.append(printApps(spaceId, showHiddenComponents));
      }

      result.append("</ul>\n");
    }
    return result.toString();
  }

  private String printSubspaces(String spaceId, boolean showHiddenComponents) {
    StringBuilder result = new StringBuilder(500);
    OrganizationController organisationController =
        OrganizationControllerProvider.getOrganisationController();
    MainSessionController sessionController = getMainSessionController();
    String userId = sessionController.getUserId();
    String[] subSpaceIds = organisationController.getAllowedSubSpaceIds(userId, spaceId);
    for (String subSpaceId : subSpaceIds) {
      result.append(printSpaceAndSubSpaces(subSpaceId, showHiddenComponents));
    }
    return result.toString();
  }

  private String printApps(String spaceId, boolean showHiddenComponents) {
    StringBuilder result = new StringBuilder(500);
    MainSessionController sessionController = getMainSessionController();
    String userId = sessionController.getUserId();
    String language = sessionController.getFavoriteLanguage();
    OrganizationController organisationController =
        OrganizationControllerProvider.getOrganisationController();
    String[] appIds = organisationController.getAvailCompoIdsAtRoot(spaceId, userId);
    for (String appId : appIds) {
      ComponentInstLight componentInst = organisationController.getComponentInstLight(appId);
      if (!componentInst.isHidden() || showHiddenComponents) {
        String label = componentInst.getLabel(language);

        result.append("<li class=\"app\" id=\"app-").append(componentInst.getId()).append("\">");

        if (displayAppIcon) {
          // display component icon
          result.append("<img src=\"").append(componentInst.getIcon(false)).append("\"");
          result.append(" border=\"0\" alt=\"\"/>");
        }

        String href =
            "<a href=\"" + URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, componentInst.getId()) +
                "\" target=\"_top\">";
        if (StringUtil.isDefined(getCallbackJSForApps())) {
          href = "<a href=\"javascript:" + getCallbackJSForApps() + "('" + componentInst.getId() +
              "');\">";
        }

        // display component link
        result.append(href).append(label).append("</a>\n");
        result.append("</li>");
      }
    }
    return result.toString();
  }

  private MainSessionController getMainSessionController() {
    return (MainSessionController) pageContext.getSession().
        getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  }
}
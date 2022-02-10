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
package org.silverpeas.core.web.util.viewgenerator.html.map;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.service.SpaceWithSubSpacesAndComponents;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import static org.silverpeas.core.admin.component.model.ComponentInstLight.getIcon;

public class MapTag extends TagSupport {

  private static final long serialVersionUID = 1425756234498404463L;
  private String spaceId;
  private boolean displayAppIcon;
  private boolean displayAppsFirst;
  private String callbackJSForMainSpace;
  private String callbackJSForApps;
  private String callbackJSForSubspaces;
  private boolean megaMenu = false;
  private Boolean forceHidingComponents;

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

  public boolean isMegaMenu() {
    return megaMenu;
  }

  public void setMegaMenu(final boolean megaMenu) {
    this.megaMenu = megaMenu;
  }

  public void setForceHidingComponents(final Boolean forceHidingComponents) {
    this.forceHidingComponents = forceHidingComponents;
  }

  @Override
  public int doStartTag() throws JspException {
    try {
      LookHelper helper = LookHelper.getLookHelper(pageContext.getSession());
      boolean showHiddenComponents = helper.getSettings("display.all.components", false);
      if (forceHidingComponents != null) {
        showHiddenComponents = !forceHidingComponents;
      }
      if (StringUtil.isNotDefined(spaceId)) {
        SpaceWithSubSpacesAndComponents root =
            OrganizationControllerProvider.getOrganisationController()
                .getFullTreeview(helper.getUserId());
        for (SpaceWithSubSpacesAndComponents space : root.getSubSpaces()) {
          pageContext.getOut().print(printSpaceAndSubSpaces(space, showHiddenComponents));
        }
      } else {
        SpaceWithSubSpacesAndComponents space =
            OrganizationControllerProvider.getOrganisationController()
                .getFullTreeview(helper.getUserId(), spaceId);
        pageContext.getOut().print(printSpaceAndSubSpaces(space, showHiddenComponents));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      throw new JspException("Can't display the site map", e);
    }
    return SKIP_BODY;
  }

  private String printSpaceAndSubSpaces(SpaceWithSubSpacesAndComponents space,
      boolean showHiddenComponents) {
    MainSessionController sessionController = getMainSessionController();
    SpaceInstLight spaceInst = space.getSpace();
    StringBuilder result = new StringBuilder(500);
    if (spaceInst != null) {
      String language = sessionController.getFavoriteLanguage();

      String spaceHref = getSpaceHREF(spaceInst);

      String hasMegaMenuLI = "";
      String megaMenuUL="";
      if (isMegaMenu() && spaceInst.isRoot()) {
        hasMegaMenuLI = "has-mega-menu";
        megaMenuUL = "mega-menu";
      }

      result.append("<li class=\"space "+hasMegaMenuLI+"\" id=\"space-").append(spaceInst.getId()).append("\">");
      result.append(spaceHref).append(spaceInst.getName(language));
      result.append("</a>\n");

      result.append(getSpaceDescription(spaceInst, language));

      String apps = printApps(space, showHiddenComponents);
      String subspaces = printSubspaces(space, showHiddenComponents);

      if (StringUtil.isDefined(apps) || StringUtil.isDefined(subspaces)) {
        result.append("<ul class=\"" + megaMenuUL + "\">");

        if (displayAppsFirst) {
          // Get apps
          result.append(apps);
          result.append("<li class=\"clear\"></li>");
        }

        // Get sub spaces
        result.append(subspaces);

        if (!displayAppsFirst) {
          // Get apps
          result.append(apps);
        }

        result.append("</ul>\n");
      }
    }
    return result.toString();
  }

  private String getSpaceDescription(SpaceInstLight spaceInst, String language) {
    if (isMegaMenu() && !spaceInst.isRoot() &&
        StringUtil.isDefined(spaceInst.getDescription(language))) {
      return "<p class=\"megaMenu-spaceDescription\">"+spaceInst.getDescription(language)+"</p>";
    }
    return "";
  }

  private String getSpaceHREF(SpaceInstLight spaceInst) {
    String spaceHref =
        "<a class=\"sp-link\" href=\"" + URLUtil.getSimpleURL(URLUtil.URL_SPACE, spaceInst.getId()) +
            "\" target=\"_top\">";
    if (spaceInst.isRoot() && StringUtil.isDefined(getCallbackJSForMainSpace())) {
      spaceHref =
          "<a href=\"javascript:" + getCallbackJSForMainSpace() + "('" + spaceInst.getId() +
              "');\">";
    } else if (!spaceInst.isRoot() && StringUtil.isDefined(getCallbackJSForSubspaces())) {
      spaceHref =
          "<a href=\"javascript:" + getCallbackJSForSubspaces() + "('" + spaceInst.getId() +
              "');\">";
    }
    return spaceHref;
  }

  private String printSubspaces(SpaceWithSubSpacesAndComponents space, boolean showHiddenComponents) {
    StringBuilder result = new StringBuilder(500);
    for (SpaceWithSubSpacesAndComponents subSpace : space.getSubSpaces()) {
      result.append(printSpaceAndSubSpaces(subSpace, showHiddenComponents));
    }
    return result.toString();
  }

  private String printApps(SpaceWithSubSpacesAndComponents space, boolean showHiddenComponents) {
    StringBuilder result = new StringBuilder(500);
    MainSessionController sessionController = getMainSessionController();
    String language = sessionController.getFavoriteLanguage();
    int nbApp = 0;
    for (final SilverpeasComponentInstance componentInst : space.getComponents()) {
      nbApp++;
      if (!componentInst.isHidden() || showHiddenComponents) {
        String label = componentInst.getLabel(language);

        result.append("<li class=\"app num").append(nbApp).append("\" id=\"app-")
            .append(componentInst.getId()).append("\">");

        if (displayAppIcon) {
          // display component icon
          result.append("<img src=\"").append(getIcon(componentInst, false)).append("\"");
          result.append(" border=\"0\" alt=\"\"/>");
        }

        String href =
            "<a class=\"sp-link\" href=\"" + URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, componentInst.getId()) +
                "\" target=\"_top\">";
        if (StringUtil.isDefined(getCallbackJSForApps())) {
          href = "<a href=\"javascript:" + getCallbackJSForApps() + "('" + componentInst.getId() +
              "');\">";
        }

        // display component link
        result.append(href).append(label).append("</a>\n");

        if (isMegaMenu() && StringUtil.isDefined(componentInst.getDescription(language))) {
          result.append(
              "<p class=\"megaMenu-appDescription\">" + componentInst.getDescription(language) +
                  "</p>");
        }

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
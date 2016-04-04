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

package org.silverpeas.core.web.util.viewgenerator.html.browsebars;

import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.webcomponent.NavigationContext;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.web.util.viewgenerator.html.NeedWindowTag;
import org.silverpeas.core.web.util.viewgenerator.html.window.Window;

import javax.servlet.jsp.JspException;

public class BrowseBarTag extends NeedWindowTag {

  private static final long serialVersionUID = 2496136938371562945L;
  private BrowseBar browseBar;
  private String extraInformations;
  private Object path;
  private String spaceId;
  private String componentId;
  private boolean ignoreComponentLink = true;
  private boolean clickable = true;

  public void setExtraInformations(String extraInformations) {
    this.extraInformations = extraInformations;
  }

  public void setPath(Object path) {
    this.path = path;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public void setIgnoreComponentLink(boolean ignoreComponentLink) {
    this.ignoreComponentLink = ignoreComponentLink;
  }

  public void setClickable(boolean clickable) {
    this.clickable = clickable;
  }

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  public void addElement(BrowseBarElement element) {
    browseBar.addElement(element);
  }

  @Override
  public int doStartTag() throws JspException {
    Window window = getWindow();
    browseBar = window.getBrowseBar();
    browseBar.setLook((LookHelper) pageContext.getSession().getAttribute(LookHelper.SESSION_ATT));
    if (extraInformations != null) {
      browseBar.setExtraInformation(extraInformations);
    }
    if (path instanceof String && StringUtil.isDefined(path.toString())) {
      browseBar.setPath(path.toString());
    }
    if (StringUtil.isDefined(componentId)) {
      ComponentInstLight component = OrganizationControllerProvider.getOrganisationController()
          .getComponentInstLight(componentId);
      if (component == null) {
        browseBar.setComponentName(componentId);
      } else {
        browseBar.setComponentId(componentId);
      }
    }
    if (StringUtil.isDefined(spaceId)) {
      browseBar.setSpaceId(spaceId);
    }
    browseBar.setIgnoreComponentLink(ignoreComponentLink);
    browseBar.setClickable(clickable);

    if (path instanceof NavigationContext) {
      NavigationContext.NavigationStep currentNavigationStep =
          ((NavigationContext) path).getBaseNavigationStep();
      while (currentNavigationStep != null) {
        if (StringUtil.isDefined(currentNavigationStep.getLabel())) {
          String link = "#";
          if (currentNavigationStep.isUriMustBeUsedByBrowseBar()) {
            link = URLUtil.getApplicationURL() +
                currentNavigationStep.getUri().toString().replaceAll("[&]ArrayPaneAction.*", "");
          }
          BrowseBarElement element =
              new BrowseBarElement(currentNavigationStep.getLabel(), link, null);
          browseBar.addElement(element);
        }
        currentNavigationStep = currentNavigationStep.getNext();
      }
    }

    return EVAL_BODY_INCLUDE;
  }
}

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

package com.stratelia.webactiv.util.viewGenerator.html.map;

import com.silverpeas.look.LookHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.List;

public class MapTag extends TagSupport {

  private static final long serialVersionUID = 1425756234498404463L;
  private String spaceId;

  public String getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  @Override
  public int doStartTag() throws JspException {
    try {
      LookHelper helper =
          (LookHelper) pageContext.getSession().getAttribute(LookHelper.SESSION_ATT);
      boolean showHiddenComponents = helper.getSettings("display.all.components", false);
      pageContext.getOut().print(printSpaceAndSubSpaces(spaceId, 0, showHiddenComponents));
    } catch (IOException e) {
      throw new JspException("Can't display the site map", e);
    }
    return SKIP_BODY;
  }

  private String printSpaceAndSubSpaces(String spaceId, int depth, boolean showHiddenComponents) {
    String contextPath = ((HttpServletRequest) pageContext.getRequest()).getContextPath();
    MainSessionController sessionController = (MainSessionController) pageContext.getSession().
        getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    OrganizationController organisationController = sessionController.getOrganizationController();
    SpaceInst spaceInst = organisationController.getSpaceInstById(spaceId);
    StringBuilder result = new StringBuilder(500);
    if (spaceInst != null) {
      String language = sessionController.getFavoriteLanguage();
      result.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\">\n");
      if (depth == 0) {
        result.append("<tr><td>&nbsp;</td></tr>\n");
      }
      result.append("<tr>\n");
      if (URLManager.displayUniversalLinks()) {
        result.append("<td class=\"txttitrecol\">&#8226; <a href=\"");
        result.append(URLManager.getSimpleURL(URLManager.URL_SPACE, spaceInst.getId()));
        result.append("\" target=\"_top\">").append(spaceInst.getName(language));
        result.append("</a></td></tr>\n");
      } else {
        result.append("<td class=\"txttitrecol\">&#8226; ").append(spaceInst.getName(language));
        result.append("</td></tr>\n");
      }
      result.append("<tr><td>\n");
      List<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
      for (ComponentInst componentInst : alCompoInst) {
        if (!componentInst.isHidden() || showHiddenComponents) {
          boolean bAllowed = organisationController.isComponentAvailable(componentInst.getId(),
              sessionController.getUserId());
          if (bAllowed) {
            String label = componentInst.getLabel(language);
            if (!StringUtil.isDefined(label)) {
              label = componentInst.getName();
            }

            if (URLManager.displayUniversalLinks()) {
              result.append("&nbsp;<img src=\"").append(contextPath)
                  .append("/util/icons/component/");
              result.append(componentInst.getName());
              result.append(
                  "Small.gif\" border=\"0\" width=\"15\" align=\"top\" alt=\"\"/>&nbsp;<a href=\"");
              result
                  .append(URLManager.getSimpleURL(URLManager.URL_COMPONENT, componentInst.getId()));
              result.append("\" target=\"_top\">").append(label).append("</a>\n");
            } else {
              result.append("&nbsp;<img src=\"").append(contextPath).append(
                  "/util/icons/component/");
              result.append(componentInst.getName());
              result.append(
                  "Small.gif\" border=\"0\" width=\"15\" align=\"top\" alt=\"\"/>&nbsp;<a href=\"");
              result.append(contextPath).append(
                  URLManager.getURL(componentInst.getName(), spaceId, componentInst.getId()));
              result.append("Main\" target=\"MyMain\" title=\"").append(
                  componentInst.getDescription()).append("\">").append(label).append("</a>\n");
            }
          }
        }
      }

      // Get all sub spaces
      String[] subSpaceIds = organisationController.getAllowedSubSpaceIds(sessionController.
          getUserId(), spaceId);
      for (String subSpaceId : subSpaceIds) {
        String subSpaceContent =
            printSpaceAndSubSpaces(subSpaceId, depth + 1, showHiddenComponents);
        if (StringUtil.isDefined(subSpaceContent)) {
          result.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\">\n");
          result.append("<tr><td>&nbsp;&nbsp;</td>\n");
          result.append("<td>\n");
          result.append(subSpaceContent);
          result.append("</td></tr></table>\n");
        }
      }

      result.append("</td>\n");
      result.append("</tr>\n");
      result.append("</table>\n");
    }
    return result.toString();
  }
}

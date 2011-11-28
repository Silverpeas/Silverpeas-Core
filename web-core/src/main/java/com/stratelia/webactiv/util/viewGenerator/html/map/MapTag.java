package com.stratelia.webactiv.util.viewGenerator.html.map;

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
      pageContext.getOut().print(printSpaceAndSubSpaces(spaceId, 0));
    } catch (IOException e) {
      throw new JspException("Can't display the site map", e);
    }
    return SKIP_BODY;
  }

  private String printSpaceAndSubSpaces(String spaceId, int depth) {
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
        if (!componentInst.isHidden()) {
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
              result.append("&nbsp;<img src=\"").append(contextPath)
                  .append("/util/icons/component/");
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
        String subSpaceContent = printSpaceAndSubSpaces(subSpaceId, depth + 1);
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

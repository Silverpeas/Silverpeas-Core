/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.browsebars;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.html.HtmlCleaner;

import java.io.IOException;
import java.util.List;

/**
 * The default implementation of ArrayPane interface
 *
 * @author squere
 * @version 1.0
 */
public class BrowseBarComplete extends AbstractBrowseBar {

  private static final String CONNECTOR = "<span class=\"connector\">&nbsp;>&nbsp;</span>";

  /**
   * Constructor declaration
   *
   * @see
   */
  public BrowseBarComplete() {
    super();
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  @Override
  public String print() {
    StringBuilder result = new StringBuilder();
    result.append("<div id=\"browseBar\">");
    result.append(printBreadCrumb());
    if (isI18N()) {
      result.append("<div id=\"i18n\">");
      result.append(getI18NHTMLLinks());
      result.append("&nbsp;|&nbsp;");
      result.append("</div>");
    }
    result.append("</div>");
    return result.toString();
  }

  private String printBreadCrumb() {
    StringBuilder result = new StringBuilder();
    String information = getExtraInformation();
    String path = getPath();
    // print javascript to go to spaces in displayed path
    result.append(printScript());
    if (!StringUtil.isDefined(getSpaceJavascriptCallback())) {
      setSpaceJavascriptCallback("goSpace");
    }
    result.append("<div id=\"breadCrumb\">");

    boolean emptyBreadCrumb = true;

    // Display spaces path from root to component
    String language = (getMainSessionController() == null) ? "" : getMainSessionController()
        .getFavoriteLanguage();
    if (StringUtil.isDefined(getComponentId()) || StringUtil.isDefined(getSpaceId())) {
      List<SpaceInst> spaces;

      OrganizationController organizationController =
          OrganizationControllerProvider.getOrganisationController();
      if (StringUtil.isDefined(getComponentId())) {
        spaces = organizationController.getSpacePathToComponent(getComponentId());
      } else {
        spaces = organizationController.getSpacePath(getSpaceId());
      }
      boolean firstSpace = true;
      for (SpaceInst spaceInst : spaces) {
        String spaceId = spaceInst.getId();
        if (!spaceId.startsWith("WA")) {
          spaceId = "WA" + spaceId;
        }
        String href = "javascript:" + getSpaceJavascriptCallback() + "('" + spaceId + "')";
        if (!isClickable()) {
          href = "#";
        }

        if (!firstSpace) {
          result.append(CONNECTOR);
        }
        result.append("<a href=\"").append(href).append("\"");
        result.append(" class=\"space\"");
        result.append(" id=\"space").append(spaceId).append("\"");
        result.append(">");
        result.append(Encode.forHtml(spaceInst.getName(language)));
        result.append("</a>");

        firstSpace = false;
        emptyBreadCrumb = false;
      }

      if (StringUtil.isDefined(getComponentId())) {
        // Display component's label
        ComponentInstLight componentInstLight = organizationController.getComponentInstLight(
                getComponentId());
        if (componentInstLight != null) {
          result.append(CONNECTOR);
          result.append("<a href=\"");
          if (!isClickable()) {
            result.append("#");
          } else if (StringUtil.isDefined(getComponentJavascriptCallback())) {
            result.append("javascript:").append(getComponentJavascriptCallback()).append("('")
                .append(getComponentId()).append("')");
          } else {
            result.append(URLUtil.getApplicationURL()).append(URLUtil.getURL(getSpaceId(),
                getComponentId()));
            if (ignoreComponentLink()) {
              result.append("Main");
            } else {
              result.append(getComponentLink());
            }
          }
          result.append("\"");
          result.append(" class=\"component\"");
          result.append(" id=\"bc_").append(componentInstLight.getId()).append("\"");
          result.append(">");
          result.append(Encode.forHtml(componentInstLight.getLabel(language)));
          result.append("</a>");
          emptyBreadCrumb = false;
        }
      }
    } else {
      if (getDomainName() != null) {
        result.append(getDomainName());
        emptyBreadCrumb = false;
      }
      if (getComponentName() != null) {
        if (getDomainName() != null) {
          result.append(CONNECTOR);
        }
        if (getComponentLink() != null) {
          result.append("<a href=\"").append(getComponentLink()).append("\">").append(
              getComponentName()).append("</a>");
        } else {
          result.append(getComponentName());
        }
        emptyBreadCrumb = false;
      }
    }

    // Display path
    List<BrowseBarElement> elements = getElements();
    if (!elements.isEmpty()) {
      for (BrowseBarElement element : elements) {
        if (!emptyBreadCrumb) {
          result.append(CONNECTOR);
        }
        result.append("<a href=\"").append(element.getLink()).append("\"");
        result.append(" class=\"element\"");
        if (StringUtil.isDefined(element.getId())) {
          result.append(" id=\"").append(element.getId()).append("\"");
        }
        result.append(">");
        result.append(EncodeHelper.javaStringToHtmlString(element.getLabel()));
        result.append("</a>");
        emptyBreadCrumb = false;
      }
    } else if (StringUtil.isDefined(path)) {
      if (!emptyBreadCrumb) {
        result.append(CONNECTOR);
      }
      result.append("<span class=\"path\">");
      result.append(path);
      result.append("</span>");
    }

    // Display extra information
    if (StringUtil.isDefined(information)) {
      if (!emptyBreadCrumb) {
        result.append(CONNECTOR);
      }
      result.append("<span class=\"information\">");
      result.append(information);
      result.append("</span>");
    }

    result.append("</div>");

    return result.toString();
  }

  private String printScript() {
    String context = URLUtil.getApplicationURL();
    StringBuilder script = new StringBuilder();
    script.append("<script type=\"text/javascript\">");
    script.append("function goSpace(spaceId) {");
    String mainFrame = "/admin/jsp/MainFrameSilverpeasV5.jsp";
    if (look != null && StringUtil.isDefined(look.getMainFrame())) {
      mainFrame = look.getMainFrame();
      if (!mainFrame.startsWith("/")) {
        mainFrame = "/admin/jsp/" + mainFrame;
      }
    }
    script.append(" top.location = \"").append(context).append(mainFrame)
        .append("?RedirectToSpaceId=\"+spaceId;");
    script.append("}");
    script.append("</script>");
    return script.toString();
  }

  @Override
  public String getBreadCrumb() {
    try {
      return getTextBreadCrumb(printBreadCrumb());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "";
  }

  String getTextBreadCrumb(String html) throws IOException {
    HtmlCleaner cleaner = new HtmlCleaner();
    return cleaner.cleanHtmlFragment(html);
  }
}

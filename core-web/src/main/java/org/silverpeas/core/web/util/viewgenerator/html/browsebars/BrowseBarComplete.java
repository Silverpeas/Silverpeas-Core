/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.browsebars;

import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.span;
import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.html.HtmlCleaner;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion
    .scriptContent;

/**
 * The default implementation of ArrayPane interface
 *
 * @author squere
 * @version 1.0
 */
public class BrowseBarComplete extends AbstractBrowseBar {

  private static final String CONNECTOR = "<span class=\"connector\"> > </span>";
  private String userLanguage = null;

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
      div div = new div(getI18NHTMLLinks() + "&nbsp;|&nbsp;");
      div.setID("i18n");
      result.append(div.toString());
    }
    result.append("</div>");
    return result.toString();
  }

  private String printBreadCrumb() {
    OrganizationController organizationController =
        OrganizationControllerProvider.getOrganisationController();
    StringBuilder result = new StringBuilder();
    // print javascript to go to spaces in displayed path
    result.append(printScript());
    if (!StringUtil.isDefined(getSpaceJavascriptCallback())) {
      setSpaceJavascriptCallback("goSpace");
    }
    result.append("<div id=\"breadCrumb\">");

    StringBuilder breadcrumb = new StringBuilder();

    SilverpeasComponentInstance componentInst = null;
    if (StringUtil.isDefined(getComponentId())) {
      componentInst = organizationController.getComponentInstance(getComponentId()).orElse(null);
    }

    // Display spaces path from root to component
    if (componentInst != null || StringUtil.isDefined(getSpaceId())) {
      breadcrumb.append(getSpacePath(componentInst));
      breadcrumb.append(getComponent(componentInst));
    } else {
      appendExplicitSpaceAndComponent(breadcrumb);
    }

    // Display path
    appendPath(breadcrumb);

    // Display extra information
    appendExtraInformation(breadcrumb);

    result.append(breadcrumb);

    result.append("</div>");

    return result.toString();
  }

  private void appendExplicitSpaceAndComponent(StringBuilder breadcrumb) {
    if (getDomainName() != null) {
      breadcrumb.append(getDomainName());
    }
    if (getComponentName() != null) {
      if (getDomainName() != null) {
        breadcrumb.append(CONNECTOR);
      }
      if (getComponentLink() != null) {
        a href = new a(getComponentLink());
        href.addElement(getComponentName());
        breadcrumb.append(href.toString());
      } else {
        breadcrumb.append(getComponentName());
      }
    }
  }

  private String getUserLanguage() {
    if (userLanguage == null) {
      userLanguage = (getMainSessionController() == null) ? "" : getMainSessionController()
          .getFavoriteLanguage();
    }
    return userLanguage;
  }

  private String getSpacePath(SilverpeasComponentInstance componentInst) {
    List<SpaceInstLight> spaces = Collections.emptyList();
    OrganizationController organizationController =
        OrganizationControllerProvider.getOrganisationController();
    if (componentInst != null && !componentInst.isPersonal()) {
      spaces = organizationController.getPathToComponent(getComponentId());
    } else if (componentInst == null) {
      spaces = organizationController.getPathToSpace(getSpaceId());
    }
    StringBuilder breadcrumb = new StringBuilder();
    for (SpaceInstLight spaceInst : spaces) {
      String spaceId = spaceInst.getId();
      String href = "javascript:" + getSpaceJavascriptCallback() + "('" + spaceId + "')";
      if (!isClickable()) {
        href = "#";
      }

      if (breadcrumb.length() > 0) {
        breadcrumb.append(CONNECTOR);
      }
      a link = new a(href);
      link.addElement(Encode.forHtml(spaceInst.getName(getUserLanguage())));
      link.setClass("space");
      link.setID("space"+spaceId);
      breadcrumb.append(link.toString());
    }
    return breadcrumb.toString();
  }

  private String getComponent(SilverpeasComponentInstance componentInst) {
    StringBuilder breadcrumb = new StringBuilder();
    if (componentInst != null) {
      // Display component's label
      if (!componentInst.isPersonal()) {
        breadcrumb.append(CONNECTOR);
      }
      breadcrumb.append("<a href=\"");
      if (!isClickable()) {
        breadcrumb.append("#");
      } else if (StringUtil.isDefined(getComponentJavascriptCallback())) {
        breadcrumb.append("javascript:").append(getComponentJavascriptCallback()).append("('")
            .append(getComponentId()).append("')");
      } else {
        breadcrumb.append(URLUtil.getApplicationURL()).append(URLUtil.getURL(getSpaceId(),
            getComponentId()));
        if (ignoreComponentLink()) {
          breadcrumb.append("Main");
        } else {
          breadcrumb.append(getComponentLink());
        }
      }
      breadcrumb.append("\"");
      breadcrumb.append(" class=\"component\"");
      breadcrumb.append(" id=\"bc_").append(componentInst.getId()).append("\"");
      breadcrumb.append(">");
      breadcrumb.append(Encode.forHtml(componentInst.getLabel(getUserLanguage())));
      breadcrumb.append("</a>");
    }
    return breadcrumb.toString();
  }

  private void appendPath(StringBuilder breadcrumb) {
    List<BrowseBarElement> elements = getElements();
    if (!elements.isEmpty()) {
      for (BrowseBarElement element : elements) {
        appendElement(breadcrumb, element);
      }
    } else if (StringUtil.isDefined(getPath())) {
      if (breadcrumb.length() > 0) {
        breadcrumb.append(CONNECTOR);
      }
      span span = new span(getPath());
      span.setClass("path");

      breadcrumb.append(span.toString());
    }
  }

  private void appendElement(StringBuilder breadcrumb, BrowseBarElement element) {
    if (breadcrumb.length() > 0) {
      breadcrumb.append(CONNECTOR);
    }
    if (StringUtil.isDefined(element.getLink())) {
      breadcrumb.append("<a href=\"").append(element.getLink()).append("\"");
    } else {
      breadcrumb.append("<span");
    }
    breadcrumb.append(" class=\"element\"");
    if (StringUtil.isDefined(element.getId())) {
      breadcrumb.append(" id=\"").append(element.getId()).append("\"");
    }
    breadcrumb.append(">");
    breadcrumb.append(element.getLabel());
    if (StringUtil.isDefined(element.getLink())) {
      breadcrumb.append("</a>");
    } else {
      breadcrumb.append("</span>");
    }
  }

  private void appendExtraInformation(StringBuilder breadcrumb) {
    if (StringUtil.isDefined(getExtraInformation())) {
      if (breadcrumb.length() > 0) {
        breadcrumb.append(CONNECTOR);
      }
      breadcrumb.append("<span class=\"information\">");
      breadcrumb.append(getExtraInformation());
      breadcrumb.append("</span>");
    }
  }

  private String printScript() {
    return scriptContent("function goSpace(spaceId) {spWindow.loadSpace(spaceId);}").toString();
  }

  @Override
  public String getBreadCrumb() {
    try {
      return getTextBreadCrumb(printBreadCrumb());
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return "";
  }

  String getTextBreadCrumb(String html) throws IOException {
    HtmlCleaner cleaner = new HtmlCleaner();
    return cleaner.cleanHtmlFragment(html);
  }
}

/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * ArrayPaneWA.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.browseBars;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.HTMLTagBalancer;
import org.cyberneko.html.filters.ElementRemover;
import org.cyberneko.html.filters.Writer;

import com.silverpeas.HtmlCleaner;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * The default implementation of ArrayPane interface
 * @author squere
 * @version 1.0
 */
public class BrowseBarComplete extends AbstractBrowseBar {

  /**
   * Constructor declaration
   * @see
   */
  public BrowseBarComplete() {
    super();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    StringBuffer result = new StringBuffer();

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
    StringBuffer result = new StringBuffer();
    String information = getExtraInformation();
    String path = getPath();

    String connector = " > ";

    // print javascript to go to spaces in displayed path
    result.append(printScript());

    result.append("<div id=\"breadCrumb\">");

    // Display spaces path from root to component
    String language = getMainSessionController().getFavoriteLanguage();
    if (StringUtil.isDefined(getComponentId()) || StringUtil.isDefined(getSpaceId())) {
      List<SpaceInst> spaces;

      if (StringUtil.isDefined(getComponentId())) {
        spaces =
            getMainSessionController().getOrganizationController().getSpacePathToComponent(
            getComponentId());
      } else {
        spaces = getMainSessionController().getOrganizationController().getSpacePath(getSpaceId());
      }
      for (SpaceInst spaceInst : spaces) {
        String spaceId = spaceInst.getId();
        if (!spaceId.startsWith("WA")) {
          spaceId = "WA" + spaceId;
        }
        result.append("<a href=\"javascript:goSpace('").append(spaceId).append("')\"");
        result.append(" class=\"space\"");
        result.append(" id=\"space").append(spaceId).append("\"");
        result.append(">");
        result.append(spaceInst.getName(language));
        result.append("</a>");
        result.append(connector);
      }

      if (StringUtil.isDefined(getComponentId())) {
        // Display component's label
        ComponentInstLight componentInstLight =
            getMainSessionController().getOrganizationController().getComponentInstLight(
            getComponentId());
        if (componentInstLight != null) {
          result.append("<a href=").append(URLManager.getApplicationURL()).append(URLManager.getURL(getSpaceId(), getComponentId())).append("Main");
          result.append(" class=\"component\"");
          result.append(" id=\"").append(componentInstLight.getId()).append("\"");
          result.append(">");
          result.append(componentInstLight.getLabel(language));
          result.append("</a>");
        }
      }
    } else {
      if (getDomainName() != null) {
        result.append(getDomainName());
      }
      if (getComponentName() != null) {
        if (getDomainName() != null) {
          result.append(connector);
        }
        if (getComponentLink() != null) {
          result.append("<a href=\"").append(getComponentLink()).append("\">").append(
              getComponentName()).append("</a>");
        } else {
          result.append(getComponentName());
        }
      }
    }

    // Display path
    List<BrowseBarElement> elements = getElements();
    if (!elements.isEmpty()) {
      for (BrowseBarElement element : elements) {
        result.append(connector);
        result.append("<a href=\"").append(element.getLink()).append("\"");
        result.append(" class=\"element\"");
        if (StringUtil.isDefined(element.getId())) {
          result.append(" id=\"").append(element.getId()).append("\"");
        }
        result.append(">");
        result.append(EncodeHelper.javaStringToHtmlString(element.getLabel()));
        result.append("</a>");
      }
    } else if (StringUtil.isDefined(path)) {
      result.append(connector).append(path);
    }

    // Display extra information
    if (StringUtil.isDefined(information)) {
      result.append(connector);
      result.append("<span class=\"information\">");
      result.append(information);
      result.append("</span>");
    }

    result.append("</div>");

    return result.toString();
  }

  private String printScript() {
    String context = GraphicElementFactory.getGeneralSettings().getString("ApplicationURL");
    StringBuilder script = new StringBuilder();
    script.append("<script type=\"text/javascript\">");
    script.append("function goSpace(spaceId) {");
    script.append(" top.location = \"").append(context).append(
        "/admin/jsp/MainFrameSilverpeasV5.jsp?RedirectToSpaceId=\"+spaceId;");
    script.append("}");
    script.append("</script>");
    return script.toString();
  }

  @Override
  public String getBreadCrumb() {
    try {
      return getTextBreadCrumb(printBreadCrumb());
    } catch (XNIException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
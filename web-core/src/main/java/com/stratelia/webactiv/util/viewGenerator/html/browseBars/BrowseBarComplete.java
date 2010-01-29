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

import java.util.List;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInst;

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
    String iconsPath = getIconsPath();
    String information = getExtraInformation();
    String path = getPath();

    result
        .append("<table class=\"" + BrowseBarComplete.class.getName() + "\">\n");
    result.append("<tr>\n");
    result.append("<td>");

    // Display spaces path from root to component
    String language = getMainSessionController().getFavoriteLanguage();
    if (StringUtil.isDefined(getComponentId())) {
      List<SpaceInst> spaces =
          getMainSessionController().getOrganizationController().getSpacePathToComponent(
              getComponentId());
      for (SpaceInst spaceInst : spaces) {
        result.append(spaceInst.getName(language));
        result.append(" > ");
      }

      // Display component's label
      ComponentInstLight componentInstLight =
          getMainSessionController().getOrganizationController().getComponentInstLight(
              getComponentId());
      if (componentInstLight != null) {
        result.append("<a href=\"Main\">");
        result.append(componentInstLight.getLabel(language));
        result.append("</a>");
        result.append(" > ");
      }
    } else {
      if (getDomainName() != null) {
        result.append(getDomainName()).append(" > ");
      }
      if (getComponentName() != null) {
        if (getComponentLink() != null) {
          result.append("<a href=\"").append(getComponentLink()).append("\">").append(
              getComponentName()).append("</a>");
        } else {
          result.append(getComponentName());
        }
      }
    }

    // Display path and/or extra information
    if ((path != null) || (information != null)) {
      if (path != null) {
        if (information == null) {
          result.append("&nbsp;&gt;&nbsp;").append(path).append("<img src=\"")
              .append(iconsPath).append("/1px.gif\" width=\"5\">\n");
        } else {
          result.append("&nbsp;&gt;&nbsp;").append(path).append("<img src=\"")
              .append(iconsPath).append("/1px.gif\" width=\"5\"> &gt;&gt; ")
              .append(information).append("\n");
        }
      } else {
        if (information != null) {
          result.append("&nbsp;&gt;&nbsp;").append(information).append("\n");
        }
      }
    }
    result.append("</td>\n");
    if (isI18N()) {
      result.append("<td align=\"right\" nowrap=\"nowrap\">");
      result.append(getI18NHTMLLinks());
      result.append("&nbsp;|&nbsp;");
      result.append("</td>");
    }
    result.append("</tr>\n");
    result.append("</table>\n");
    return result.toString();
  }

}
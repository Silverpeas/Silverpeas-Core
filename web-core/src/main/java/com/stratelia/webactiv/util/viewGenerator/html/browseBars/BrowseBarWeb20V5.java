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

/**
 * The default implementation of ArrayPane interface
 * 
 * @author squere
 * @version 1.0
 */
public class BrowseBarWeb20V5 extends AbstractBrowseBar {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public BrowseBarWeb20V5() {
    super();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String print() {
    StringBuffer result = new StringBuffer();
    String iconsPath = getIconsPath();
    String domainName = getDomainName();
    String componentName = getComponentName();
    String componentLink = getComponentLink();
    String information = getExtraInformation();
    String path = getPath();

    result
        .append("<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">\n");
    result.append("<tr>\n");
    result.append("<td width=\"0%\" align=\"left\" class=\"browsebar\">");
    if (domainName != null) {
      result.append(domainName).append("&nbsp;&gt;&nbsp;");
    }
    if (componentName != null) {
      if (componentLink != null) {
        result.append("<a href=\"").append(componentLink).append("\">").append(
            componentName).append("</a>");
      } else {
        result.append(componentName);
      }
    }
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
    // result.append(displayLine());
    result.append("</table>\n");
    return result.toString();
  }

}
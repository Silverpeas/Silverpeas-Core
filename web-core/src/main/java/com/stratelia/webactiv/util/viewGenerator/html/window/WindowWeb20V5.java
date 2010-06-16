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
 * FLOSS exception.  You should have received a copy of the text describing
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
 * WindowSogreah.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.window;

/**
 * The default implementation of Window interface
 * @author neysseri
 * @version 1.0
 */
public class WindowWeb20V5 extends AbstractWindow {

  /**
   * Constructor declaration
   * @see
   */
  public WindowWeb20V5() {
    super();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printBefore() {
    StringBuffer result = new StringBuffer();
    String width = getWidth();

    int nbCols = 1;

    if (getOperationPane().nbOperations() > 0) {
      nbCols = 2;
    }

    result
        .append("<table width=\"")
        .append(width)
        .append(
        "\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" id=\"topPage\">");
    result.append("<tr><td class=\"cellBrowseBar\" width=\"100%\">");
    result.append(getBrowseBar().print());
    result.append("</td>");
    if (nbCols == 2) {
      result
          .append("<td align=\"right\" class=\"cellOperation\" nowrap=\"nowrap\">");
      result.append(getOperationPane().print());
      result.append("</td>");
    } else {
      result
          .append("<td align=\"right\" class=\"cellOperation\" nowrap=\"nowrap\">");
      result.append("&nbsp;");
      result.append("</td>");
    }
    result.append("</tr>");
    result
        .append("<tr><td width=\"100%\" valign=\"top\" colspan=\"2\" class=\"cellBodyWindows\">");
    result
        .append("<table border=\"0\" width=\"100%\" cellpadding=\"5\" cellspacing=\"5\"><tr><td valign=\"top\">");
    return result.toString();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printAfter() {
    StringBuffer result = new StringBuffer();
    String iconsPath = getIconsPath();

    result.append("</td></tr></table>");
    result.append("</td>");
    result.append("</tr>");
    result.append("</table>");

    result
        .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
    result.append("<tr><td class=\"basGaucheWindow\">");
    result.append("<img src=\"").append(iconsPath).append(
        "/1px.gif\" width=\"1\" alt=\"\"/>\n");
    result.append("</td><td class=\"basMilieuWindow\">");
    result.append("<img src=\"").append(iconsPath).append(
        "/1px.gif\" width=\"1\" alt=\"\"/>\n");
    result.append("</td><td class=\"basDroiteWindow\">");
    result.append("<img src=\"").append(iconsPath).append(
        "/1px.gif\" width=\"1\" alt=\"\"/>\n");
    result.append("</td></tr></table>");

    result
        .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
    result.append("<tr><td>");
    result.append("<div align=\"left\"><a href=\"#topPage\"><img src=\"")
        .append(iconsPath).append("/goTop.gif\" border=\"0\" alt=\"\"/></a></div>");
    result.append("</td><td width=\"100%\">");
    result.append("&nbsp;");
    result.append("</td><td>");
    result.append("<div align=\"right\"><a href=\"#topPage\"><img src=\"")
        .append(iconsPath).append("/goTop.gif\" border=\"0\" alt=\"\"/></a></div>");
    result.append("</td></tr></table>");

    return result.toString();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    StringBuffer result = new StringBuffer();

    result.append(printBefore());
    result.append(getBody());
    result.append(printAfter());

    return result.toString();
  }

}

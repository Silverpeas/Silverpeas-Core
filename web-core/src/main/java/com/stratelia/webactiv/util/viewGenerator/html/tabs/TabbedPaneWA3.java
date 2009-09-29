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
 * TabbedPane.java
 *
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.tabs;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * 
 * @author squere
 * @version
 */
public class TabbedPaneWA3 extends AbstractTabbedPane {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public TabbedPaneWA3() {
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
    Vector tabLines = getTabLines();
    Collection tabs = null;
    int nbLines = tabLines.size();
    int incr = nbLines - 1;

    for (int j = 0; j < nbLines; j++) {
      tabs = (Collection) tabLines.get(j);
      result
          .append("<table cellpadding=\"0\" cellspacing=\"0\""
              + " border=\"0\" width=\"100%\">\r\n<tr><td align=\"right\" width=\"100%\">");
      result.append(printTabLine(tabs));
      result.append("</td><td><img src=\"").append(iconsPath).append(
          "/tabs/1px.gif\" width=\"").append(incr * 17).append(
          "\" height=\"1\"></td></tr></table>");
      incr--;
    }

    return result.toString();
  }

  /**
   * Method declaration
   * 
   * 
   * @param tabs
   * 
   * @return
   * 
   * @see
   */
  private String printTabLine(Collection tabs) {

    StringBuffer result = new StringBuffer();
    String iconsPath = getIconsPath();

    result
        .append("<table id=\"tabbedPane\" cellpadding=\"0\" cellspacing=\"0\""
            + " border=\"0\" width=\"100%\">\r\n");
    result.append("<tr>\r\n");
    result.append("<td colspan=\"2\"><img src=\"").append(iconsPath).append(
        "/tabs/1px.gif\" width=\"1\" height=\"2\"></td>\r\n");
    result.append("<td><img src=\"").append(iconsPath).append(
        "/tabs/left1.gif\" width=\"15\" height=\"2\"></td>\r\n");

    /*
     * if (indentation == RIGHT)
     * result.append("<td width=\"100%\">&nbsp;</td>\n");
     */

    Iterator i = tabs.iterator();
    while (i.hasNext()) {
      Tab tab = (Tab) i.next();
      String style = null;

      if (tab.getSelected()) {
        style = "styleOngletOn";
        result.append("<td rowspan=3>\n");
        result
            .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
        result.append("<tr>\n");
        result.append("<td><img src=\"").append(iconsPath).append(
            "/tabs/left1-1.gif\" width=\"14\" height=\"2\"></td>\n");
        result.append("<td rowspan=\"2\" nowrap class=").append(style).append(
            " valign=\"top\" align=\"center\">\n");
        result
            .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
        result.append("<tr bgcolor=\"#000000\">\n");
        result.append("<td><img src=\"").append(iconsPath).append(
            "/tabs/1px.gif\" width=\"1\" height=\"1\"></td>\n");
        result.append("</tr>\n");
        result.append("<tr align=\"center\">\n");

        if (tab.getEnabled()) {
          result.append("<td class=").append(style)
              .append(" nowrap><a href=\"").append(tab.getAction()).append(
                  "\" class=").append(style).append(">").append(tab.getLabel())
              .append("</td>\n");
        } else {
          result.append("<td class=").append(style).append(" nowrap>").append(
              tab.getLabel()).append("</td>\n");
        }

        result.append("</tr>\n");
        result.append("</table>\n");
        result.append("</td>\n");
        result.append("<td><img src=\"").append(iconsPath).append(
            "/tabs/right1-1.gif\" width=\"14\" height=\"2\"></td>\n");
        result.append("</tr>\n");
        result.append("<tr>\n");
        result.append("<td class=").append(style).append("><img src=\"")
            .append(iconsPath).append(
                "/tabs/ong_left.gif\" width=\"14\" height=\"14\"></td>\n");
        result.append("<td class=").append(style).append("><img src=\"")
            .append(iconsPath).append(
                "/tabs/ong_on-off.gif\" width=\"14\" height=\"14\"></td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");

      } else {
        style = "styleOngletOff";
        result.append("<td rowspan=3>\n");
        result
            .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
        result.append("<tr>\n");
        result.append("<td rowspan=\"2\" nowrap class=").append(style).append(
            " valign=\"top\" align=\"center\">\n");
        result
            .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
        result.append("<tr bgcolor=\"#000000\">\n");
        result.append("<td><img src=\"").append(iconsPath).append(
            "/tabs/1px.gif\" width=\"1\" height=\"1\"></td>\n");
        result.append("</tr>\n");
        result.append("<tr align=\"center\">\n");

        if (tab.getEnabled()) {
          result.append("<td class=").append(style)
              .append(" nowrap><a href=\"").append(tab.getAction()).append(
                  "\" class=").append(style).append(">").append(tab.getLabel())
              .append("</td>\n");
        } else {
          result.append("<td class=").append(style).append(" nowrap>").append(
              tab.getLabel()).append("</td>\n");
        }

        result.append("</tr>\n");
        result.append("</table>\n");
        result.append("</td>\n");
        result.append("<td><img src=\"").append(iconsPath).append(
            "/tabs/right1-1.gif\" width=\"14\" height=\"2\"></td>\n");
        result.append("</tr>\n");
        result.append("<tr>\n");
        result.append("<td class=").append(style).append("><img src=\"")
            .append(iconsPath).append(
                "/tabs/ong_on-off.gif\" width=\"14\" height=\"14\"></td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");
      }
      result.append("</td>\n");
    }
    // fin while

    /*
     * if (indentation == LEFT) {
     * result.append("<td width=\"100%\">&nbsp;</td>\n"); }
     */

    result.append("<td><img src=\"").append(iconsPath).append(
        "/tabs/right1.gif\" width=\"15\" height=\"2\"></td>\n");
    result.append("<td rowspan=\"3\"><img src=\"").append(iconsPath).append(
        "/tabs/1px.gif\" width=\"5\" height=\"1\"></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td colspan=\"2\"><img src=\"").append(iconsPath).append(
        "/tabs/1px.gif\" width=\"1\" height=\"13\"></td>\n");
    result.append("<td rowspan=\"2\"><img src=\"").append(iconsPath).append(
        "/tabs/left.gif\" width=\"15\" height=\"14\"></td>\n");
    result.append("<td rowspan=\"2\"><img src=\"").append(iconsPath).append(
        "/tabs/right.gif\" width=\"15\" height=\"14\"></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td colspan=\"2\" width=100%><img src=\"").append(iconsPath)
        .append("/tabs/1px.gif\" width=\"1\" height=\"1\"></td>\n");
    result.append("</tr>\n");
    result.append("</table>\n");

    return result.toString();
  }

}
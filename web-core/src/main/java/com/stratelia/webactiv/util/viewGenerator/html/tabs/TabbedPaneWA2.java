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
 * TabbedPane.java
 *
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.tabs;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author squere
 * @version
 */
public class TabbedPaneWA2 extends AbstractTabbedPane {

  /**
   * Constructor declaration
   * @see
   */
  public TabbedPaneWA2() {
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
    Vector tabLines = getTabLines();
    Collection tabs = null;
    int nbLines = tabLines.size();
    int incr = nbLines - 1;

    for (int j = 0; j < nbLines; j++) {
      tabs = (Collection) tabLines.get(j);
      result.append("<table id=\"tabbedPane\" cellpadding=\"0\" ").append(
          "cellspacing=\"0\" border=\"0\" width=\"100%\">\r\n").append(
          "<tr><td align=\"right\" width=\"100%\" >");
      result.append(printTabLine(tabs));
      result.append("</td><td><img src=\"").append(iconsPath).append(
          "/tabs/1px.gif\" width=").append(incr * 17).append(
          " height=1></td></tr></table>");
      incr--;
    }
    return result.toString();
  }

  /**
   * Method declaration
   * @param tabs
   * @return
   * @see
   */
  private String printTabLine(Collection tabs) {

    StringBuffer result = new StringBuffer();
    String iconsPath = getIconsPath();
    int indentation = getIndentation();

    result
        .append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
    result.append("<tr align=\"right\">");
    if (indentation == RIGHT) {
      result.append("<td width=\"100%\">&nbsp;</td>\n");
    }

    Iterator i = tabs.iterator();
    while (i.hasNext()) {
      Tab tab = (Tab) i.next();
      String style = "ongletOff";

      if (tab.getSelected()) {
        style = "ongletOn";
      }

      result.append(TabbedPaneWA2StringFactory.getPrintBeforeString());

      /*
       * result.append("<td>\n"); result.append("<table border=0 cellspacing=0 cellpadding=0>\n");
       * result.append("<tr>\n"); result.append("<td colspan=3 rowspan=3><img
       * src=\"").append(iconsPath).append("/tabs/bt2_hg.gif\"></td>\n"); result.append("<td
       * bgcolor=#999999><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
       * result.append("<td colspan=3 rowspan=3><img
       * src=\"").append(iconsPath).append("/tabs/bt2_hd.gif\"></td>\n"); result.append("</tr>\n");
       * result.append("<tr>\n"); result.append("<td bgcolor=#FFFFFF width=1><img
       * src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n"); result.append("</tr>\n");
       * result.append("<tr>\n"); result.append("<td class=ongletColorLight><img
       * src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n"); result.append("</tr>\n");
       * result.append("<tr>\n"); result.append("<td bgcolor=#666666><img
       * src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n"); result.append("<td
       * bgcolor=#CCCCCC><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
       * result.append("<td class=ongletColorLight><img
       * src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
       */

      if (tab.getEnabled()) {
        result.append("<td align=\"center\" nowrap class=").append(style)
            .append("><a href=\"").append(tab.getAction())
            .append("\" class=\"").append(style).append("\">&nbsp;").append(
            tab.getLabel()).append("&nbsp;</td>\n");
      } else {
        result.append("<td align=\"center\" nowrap class=\"").append(style)
            .append("\">").append(tab.getLabel()).append("</td>\n");
      }

      result.append(TabbedPaneWA2StringFactory.getPrintAfterString());

      /*
       * result.append("<td class=ongletColorDark><img
       * src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n"); result.append("<td
       * bgcolor=#666666><img src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n");
       * result.append("<td bgcolor=#000000><img
       * src=\"").append(iconsPath).append("/tabs/1px.gif\"></td>\n"); result.append("</tr>\n");
       * result.append("</table>\n"); result.append("</td>\n");
       */
    }

    if (indentation == LEFT) {
      result.append("<td width=\"100%\">&nbsp;</td>\n");

    }
    result.append("<td><img src=\"").append(iconsPath).append(
        "/tabs/1px.gif\" width=13></td></tr>\n");
    result.append("</table>\n");
    return result.toString();
  }

}
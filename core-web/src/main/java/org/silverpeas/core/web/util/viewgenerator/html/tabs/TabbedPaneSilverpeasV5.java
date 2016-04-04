/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html.tabs;

import java.util.Collection;
import java.util.Vector;

/**
 * @author squere
 * @version
 */
public class TabbedPaneSilverpeasV5 extends AbstractTabbedPane {

  /**
   * Constructor declaration
   * @see
   */
  public TabbedPaneSilverpeasV5() {
    super();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    StringBuilder result = new StringBuilder();
    String iconsPath = getIconsPath();
    Vector<Collection<Tab>> tabLines = getTabLines();
    Collection<Tab> tabs = null;

    int nbLines = tabLines.size();
    int incr = nbLines - 1;

    result.append("<div id=\"gef-tabs\">");
    for (int j = 0; j < nbLines; j++) {
      tabs = tabLines.get(j);
      result.append("<table id=\"tabbedPane");
      if (nbLines > 1) {
        result.append(j);
      }
      result.append("\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
      result.append("<tr><td align=\"right\" width=\"100%\">");
      result.append(printTabLine(tabs));
      result.append("</td><td><img src=\"").append(iconsPath).append(
          "/tabs/1px.gif\" width=\"").append(incr * 17).append(
          "\" height=\"1\" alt=\"\"/></td></tr></table>");
      incr--;
    }
    result
        .append("<table id=\"sousTabbedPane\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td width=\"100%\" class=\"sousOnglets\">");
    result.append("<img src=\"").append(iconsPath).append(
        "/tabs/1px.gif\" width=\"1\" height=\"1\" alt=\"\"/></td></tr></table>");
    result.append("</div>");
    return result.toString();
  }

  /**
   * Method declaration
   * @param tabs
   * @return
   * @see
   */
  private String printTabLine(Collection<Tab> tabs) {

    StringBuilder result = new StringBuilder();
    String iconsPath = getIconsPath();
    int indentation = getIndentation();

    result.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
    result.append("<tr align=\"right\">");
    if (indentation == RIGHT) {
      result.append("<td width=\"100%\">&nbsp;</td>\n");
    }

    int i = 0;
    for (Tab tab : tabs) {
      String style = null;
      String styleGauche = null;
      String styleDroite = null;

      if (tab.getSelected()) {
        style = "milieuOngletOn";
        styleGauche = "gaucheOngletOn";
        styleDroite = "droiteOngletOn";
      } else {
        style = "milieuOngletOff";
        styleGauche = "gaucheOngletOff";
        styleDroite = "droiteOngletOff";
      }

      result.append("<td id=\"tableft").append(i)
          .append("\" align=\"center\" nowrap=\"nowrap\" class=\"")
          .append(styleGauche).append("\">");
      result.append("<img src=\"").append(iconsPath).append("/tabs/1px.gif\" alt=\"\"/></td>\n");
      result.append("<td id=\"tab").append(i)
          .append("\" align=\"center\" nowrap=\"nowrap\" class=\"")
          .append(style).append("\">");
      if (tab.getEnabled()) {
        result.append("<a href=\"").append(tab.getAction()).append("\">");
        result.append(tab.getLabel());
        result.append("</a>");
      } else {
        result.append("<span>");
        result.append(tab.getLabel());
        result.append("</span>");
      }
      result.append("</td>\n");
      result.append("<td id=\"tabright").append(i)
          .append("\" align=\"center\" nowrap=\"nowrap\" class=\"").append(styleDroite)
          .append("\"><img src=\"").append(iconsPath).append(
              "/tabs/1px.gif\" alt=\"\"/></td>\n");
      i++;
    }

    if (indentation == LEFT) {
      result.append("<td width=\"100%\">&nbsp;</td>\n");
    }
    result.append("<td><img src=\"").append(iconsPath).append(
        "/tabs/1px.gif\" width=\"13\" alt=\"\"/></td></tr>\n");
    result.append("</table>\n");
    return result.toString();
  }

}

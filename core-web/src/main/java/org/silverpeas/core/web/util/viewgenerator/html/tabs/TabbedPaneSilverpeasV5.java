/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.tabs;

import org.silverpeas.core.web.util.viewgenerator.html.TagUtil;

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
      result.append("<div class =\"tabbedPane\" id=\"tabbedPane");
      if (nbLines > 1) {
        result.append(j);
      }
      result.append("\" >");
      result.append(printTabLine(tabs));
      result.append("</div>");
      incr--;
    }
    result
        .append("<div id=\"sousTabbedPane\"><div class=\"sousOnglets\"></div></div></div>");
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

 
    int i = 0;
    for (Tab tab : tabs) {
      String style = null;
      String styleGauche = null;
      String styleDroite = null;

      if (tab.getSelected()) {
        style = "sp_tabOn";
      } else {
        style = "sp_tabOff";
      }

      
      result.append("<div id=\"tab").append(i)
          .append("\" class=\"")
          .append(style).append("\">");
      if (tab.getEnabled()) {
        String href = TagUtil.formatHrefFromAction(tab.getAction());
        result.append("<a ").append(href).append(" >");
        result.append(tab.getLabel());
        result.append("</a>");
      } else {
        result.append("<span>");
        result.append(tab.getLabel());
        result.append("</span>");
      }
      result.append("</div>\n");
      i++;
    }

    return result.toString();
  }

}

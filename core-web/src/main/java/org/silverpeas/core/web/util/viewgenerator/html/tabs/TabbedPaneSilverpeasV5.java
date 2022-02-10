/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.TagUtil;

/**
 * @author squere
 * @version
 */
public class TabbedPaneSilverpeasV5 extends AbstractTabbedPane {

  /**
   * Constructor declaration
   *
   */
  public TabbedPaneSilverpeasV5() {
    super();
  }

  /**
   * Method declaration
   * @return
   *
   */
  public String print() {
    StringBuilder result = new StringBuilder();

    result.append("<div id=\"gef-tabs\">");
    result.append("<div class=\"tabbedPane\" id=\"tabbedPane\">");
    result.append(printTabLine());
    result.append("</div>");
    result.append("<div id=\"sousTabbedPane\"><div class=\"sousOnglets\"></div></div></div>");
    return result.toString();
  }

  /**
   * Method declaration
   * @return
   *
   */
  private String printTabLine() {
    StringBuilder result = new StringBuilder();
    for (Tab tab : getTabs()) {
      String style = "sp_tabOff";
      if (tab.getSelected()) {
        style = "sp_tabOn";
      }

      result.append("<div");
      if (StringUtil.isDefined(tab.getName())) {
        result.append(" id=\"tab_").append(tab.getName()).append("\"");
      }
      result.append(" class=\"").append(style).append("\">");
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
    }

    return result.toString();
  }

}
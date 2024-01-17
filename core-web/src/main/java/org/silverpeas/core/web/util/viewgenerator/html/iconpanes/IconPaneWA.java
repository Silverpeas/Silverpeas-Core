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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * IconPaneWA.java
 *
 * Created on 12 decembre 2000, 11:47
 */

package org.silverpeas.core.web.util.viewgenerator.html.iconpanes;

import org.silverpeas.core.web.util.viewgenerator.html.icons.Icon;

import java.util.Vector;

/**
 * The default implementation of IconPane interface
 * @author neysseric
 * @version 1.0
 */
public class IconPaneWA extends AbstractIconPane {

  /**
   * Constructor declaration
   *
   */
  public IconPaneWA() {
    super();
  }

  /**
   * Method declaration
   * @return
   *
   */
  public String horizontalPrint() {
    StringBuilder result = new StringBuilder();
    Vector icons = getIcons();
    String spacing = getSpacing();

    result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
    if (icons.size() > 0) {
      result.append("<td>");
      result.append(((Icon) icons.elementAt(0)).print());
      result.append("</td>");
    }
    for (int i = 1; i < icons.size(); i++) {
      result.append("<td width=\"").append(spacing).append("\">&nbsp;</td>");
      result.append("<td>");
      result.append(((Icon) icons.elementAt(i)).print());
      result.append("</td>");
    }
    result.append("</tr></table>");

    return result.toString();
  }

  /**
   * Method declaration
   * @return
   *
   */
  public String verticalPrint() {
    StringBuilder result = new StringBuilder();
    Vector icons = getIcons();
    String verticalWidth = getVerticalWidth();
    String spacing = getSpacing();

    result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"")
        .append(verticalWidth)
        .append("\">");
    if (icons.size() > 0) {
      result.append("<tr><td>");
      result.append(((Icon) icons.elementAt(0)).print());
      result.append("</td></tr>");
    }
    for (int i = 1; i < icons.size(); i++) {
      result.append("<tr><td height=\"" + spacing + "\">&nbsp;</td></tr>");
      result.append("<tr><td>");
      result.append(((Icon) icons.elementAt(i)).print());
      result.append("</td></tr>");
    }
    result.append("</table>");

    return result.toString();
  }

  /**
   * Method declaration
   * @return
   *
   */
  public String print() {
    int viewType = getViewType();

    if (viewType == VERTICAL_PANE) {
      return verticalPrint();
    } else {
      return horizontalPrint();
    }
  }

}

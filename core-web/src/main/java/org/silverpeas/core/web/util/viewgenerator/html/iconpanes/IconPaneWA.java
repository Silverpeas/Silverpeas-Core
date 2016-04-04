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
   * @see
   */
  public IconPaneWA() {
    super();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String horizontalPrint() {
    String result = "";
    Vector icons = getIcons();
    String spacing = getSpacing();

    result += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>";
    if (icons.size() > 0) {
      result += "<td>";
      result += ((Icon) icons.elementAt(0)).print();
      result += "</td>";
    }
    for (int i = 1; i < icons.size(); i++) {
      result += "<td width=\"" + spacing + "\">&nbsp;</td>";
      result += "<td>";
      result += ((Icon) icons.elementAt(i)).print();
      result += "</td>";
    }
    result += "</tr></table>";

    return result;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String verticalPrint() {
    String result = "";
    Vector icons = getIcons();
    String verticalWidth = getVerticalWidth();
    String spacing = getSpacing();

    result += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\""
        + verticalWidth + "\">";
    if (icons.size() > 0) {
      result += "<tr><td>";
      result += ((Icon) icons.elementAt(0)).print();
      result += "</td></tr>";
    }
    for (int i = 1; i < icons.size(); i++) {
      result += "<tr><td height=\"" + spacing + "\">&nbsp;</td></tr>";
      result += "<tr><td>";
      result += ((Icon) icons.elementAt(i)).print();
      result += "</td></tr>";
    }
    result += "</table>";

    return result;
  }

  /**
   * Method declaration
   * @return
   * @see
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

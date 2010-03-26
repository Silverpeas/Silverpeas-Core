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

package com.stratelia.webactiv.util.viewGenerator.html.buttonPanes;

import java.util.Vector;

import com.stratelia.webactiv.util.viewGenerator.html.buttons.Button;

/**
 * The default implementation of ArrayPane interface
 * @author squere
 * @version 1.0
 */
public class ButtonPaneWA extends AbstractButtonPane {

  /**
   * Constructor declaration
   * @see
   */
  public ButtonPaneWA() {
    super();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String horizontalPrint() {
    StringBuffer result = new StringBuffer();
    Vector buttons = getButtons();

    result
        .append("<TABLE border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><TR>");
    result.append("<TD width=\"100\">&nbsp;</TD>");
    if (buttons.size() > 0) {
      result.append("<TD class=\"buttonStyle\">");
      result.append(((Button) buttons.elementAt(0)).print());
      result.append("</TD>");
    }
    for (int i = 1; i < buttons.size(); i++) {
      result.append("<TD width=\"50\">&nbsp;</TD>");
      result.append("<TD class=\"buttonStyle\">");
      result.append(((Button) buttons.elementAt(i)).print());
      result.append("</TD>");
    }
    result.append("<TD width=\"100\">&nbsp;</TD>");
    result.append("</TR></TABLE>");

    return result.toString();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String verticalPrint() {
    StringBuffer result = new StringBuffer();
    Vector buttons = getButtons();
    String verticalWidth = getVerticalWidth();

    result.append(
        "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"")
        .append(verticalWidth).append("\">");
    result.append("<tr>");
    result.append("<td class=\"buttonStyle\" width=\"").append(verticalWidth)
        .append("\">");
    for (int i = 0; i < buttons.size(); i++) {
      result.append(((Button) buttons.elementAt(i)).print());
    }
    result.append("</td>");
    result.append("</tr>");
    result.append("</table>");

    return result.toString();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    if (getViewType() == VERTICAL_PANE) {
      return verticalPrint();
    } else {
      return horizontalPrint();
    }
  }

}
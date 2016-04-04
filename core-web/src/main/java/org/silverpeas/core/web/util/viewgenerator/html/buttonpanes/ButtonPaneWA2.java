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

/*
 * ArrayPaneWA.java
 *
 * Created on 10 octobre 2000, 16:11
 */
package org.silverpeas.core.web.util.viewgenerator.html.buttonpanes;

import org.silverpeas.core.web.util.viewgenerator.html.buttons.Button;
import java.util.List;

/**
 * The default implementation of ArrayPane interface
 * @author squere
 * @version 1.0
 */
public class ButtonPaneWA2 extends AbstractButtonPane {

  /**
   * Constructor declaration
   * @see
   */
  public ButtonPaneWA2() {
    super();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public String horizontalPrint() {
    StringBuilder result = new StringBuilder();
    List<Button> buttons = getButtons();

    result.append("<div class=\"buttonPane\">");
    result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
    result.append("<td width=\"100\">&nbsp;</td>");
    if (buttons.size() > 0) {
      result.append("<td>");
      result.append(buttons.get(0).print());
      result.append("</td>");
    }
    for (int i = 1; i < buttons.size(); i++) {
      result.append("<td>&nbsp;</td>");
      result.append("<td>");
      result.append(buttons.get(i).print());
      result.append("</td>");
    }
    result.append("<td width=\"100\">&nbsp;</td>");
    result.append("</tr></table>");
    result.append("</div>");

    return result.toString();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public String verticalPrint() {
    StringBuilder result = new StringBuilder();
    List<Button> buttons = getButtons();
    String verticalWidth = getVerticalWidth();
    result.append(
        "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"").append(verticalWidth).
        append("\">");
    result.append("<tr>");
    result.append("<td width=\"").append(verticalWidth).append("\">");
    for (Button button : buttons) {
      result.append(button.print());
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
  @Override
  public String print() {
    if (getViewType() == VERTICAL_PANE) {
      return verticalPrint();
    }
    return horizontalPrint();
  }
}

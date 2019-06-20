/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
   *
   */
  public ButtonPaneWA2() {
    super();
  }

  /**
   * Method declaration
   * @return
   *
   */
  @Override
  public String horizontalPrint() {
    StringBuilder result = new StringBuilder();
    List<Button> buttons = getButtons();

    result.append("<div class=\"sp_buttonPane\">");

    if (buttons.size() > 0) {
      result.append(buttons.get(0).print());
    }
    for (int i = 1; i < buttons.size(); i++) {
      result.append(buttons.get(i).print());
    }
  
    result.append("</div>");

    return result.toString();
  }

  /**
   * Method declaration
   * @return
   *
   */
  @Override
  public String verticalPrint() {
    StringBuilder result = new StringBuilder();
    List<Button> buttons = getButtons();
    String verticalWidth = getVerticalWidth();
    result.append(
        "<div class=\"sp_buttonPane verticalPane\" style=\"width:").append(verticalWidth).
        append(";\">");
    for (Button button : buttons) {
      result.append(button.print());
    }
    result.append("</div>");
    return result.toString();
  }

  /**
   * Method declaration
   * @return
   *
   */
  @Override
  public String print() {
    if (getViewType() == VERTICAL_PANE) {
      return verticalPrint();
    }
    return horizontalPrint();
  }
}

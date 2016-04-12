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

package org.silverpeas.core.web.util.viewgenerator.html.buttonpanes;

import org.silverpeas.core.web.util.viewgenerator.html.buttons.Button;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The default implementation of ArrayPane interface
 * @author squere
 * @version 1.0
 */
public abstract class AbstractButtonPane implements ButtonPane {

  private List<Button> buttons = null;
  private String verticalWidth = "50px";
  public final static int VERTICAL_PANE = 1;
  public final static int HORIZONTAL_PANE = 2;

  private int viewType = HORIZONTAL_PANE;

  /**
   * Constructor declaration
   * @see
   */
  public AbstractButtonPane() {
    buttons = new ArrayList<Button>();
  }

  /**
   * Method declaration
   * @param button
   * @see
   */
  @Override
  public void addButton(Button button) {
    buttons.add(button);
  }

  /**
   * Method declaration
   * @see
   */
  @Override
  public void setVerticalPosition() {
    viewType = VERTICAL_PANE;
  }

  /**
   * Method declaration
   * @param width
   * @see
   */
  @Override
  public void setVerticalWidth(String width) {
    verticalWidth = width;
  }

  /**
   * Method declaration
   * @see
   */
  @Override
  public void setHorizontalPosition() {
    viewType = HORIZONTAL_PANE;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public List<Button> getButtons() {
    return Collections.unmodifiableList(this.buttons);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getViewType() {
    return this.viewType;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getVerticalWidth() {
    return this.verticalWidth;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String horizontalPrint();

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String verticalPrint();

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public abstract String print();
}

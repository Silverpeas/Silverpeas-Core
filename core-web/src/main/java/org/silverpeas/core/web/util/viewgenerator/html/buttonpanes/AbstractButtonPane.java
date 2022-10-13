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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.buttonpanes;

import org.silverpeas.core.util.StringUtil;
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

  public static final int VERTICAL_PANE = 1;
  public static final int HORIZONTAL_PANE = 2;
  private String cssClass = StringUtil.EMPTY;
  private List<Button> buttons = null;
  private String verticalWidth = "50px";
  private int viewType = HORIZONTAL_PANE;

  /**
   * Constructor declaration
   *
   */
  public AbstractButtonPane() {
    buttons = new ArrayList<>();
  }

  @Override
  public void setCssClass(final String cssClass) {
    this.cssClass = cssClass != null && cssClass.length() > 0 ? " " + cssClass : StringUtil.EMPTY;
  }

  protected String getCssClass() {
    return cssClass;
  }

  /**
   * Method declaration
   * @param button
   *
   */
  @Override
  public void addButton(Button button) {
    buttons.add(button);
  }

  /**
   * Method declaration
   *
   */
  @Override
  public void setVerticalPosition() {
    viewType = VERTICAL_PANE;
  }

  /**
   * Method declaration
   *
   */
  @Override
  public void setHorizontalPosition() {
    viewType = HORIZONTAL_PANE;
  }

  /**
   * Method declaration
   * @return
   *
   */
  public List<Button> getButtons() {
    return Collections.unmodifiableList(this.buttons);
  }

  /**
   * Method declaration
   * @return
   *
   */
  public int getViewType() {
    return this.viewType;
  }

  /**
   * Method declaration
   * @return
   *
   */
  public String getVerticalWidth() {
    return this.verticalWidth;
  }

  /**
   * Method declaration
   * @param width
   *
   */
  @Override
  public void setVerticalWidth(String width) {
    verticalWidth = width;
  }

  /**
   * Method declaration
   * @return
   *
   */
  public abstract String horizontalPrint();

  /**
   * Method declaration
   * @return
   *
   */
  public abstract String verticalPrint();
}

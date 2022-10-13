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
/*
 * IconPaneWA.java
 *
 * Created on 12 decembre 2000, 11:47
 */

package org.silverpeas.core.web.util.viewgenerator.html.iconpanes;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.icons.Icon;
import org.silverpeas.core.web.util.viewgenerator.html.icons.IconWA;

import java.util.Vector;

/**
 * The default implementation of IconPane interface
 * @author neysseric
 * @version 1.0
 */
public abstract class AbstractIconPane implements IconPane {

  private Vector icons = null;
  private String verticalWidth = "50";
  private String spacing = "20";
  public final static int VERTICAL_PANE = 1;
  public final static int HORIZONTAL_PANE = 2;

  private int viewType = HORIZONTAL_PANE;

  /**
   * Constructor declaration
   *
   */
  public AbstractIconPane() {
    icons = new Vector();
  }

  private String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  /**
   * Method declaration
   * @return
   *
   */
  @Override
  public Icon addIcon() {
    Icon icon = new IconWA();

    icons.add(icon);
    return icon;
  }

  public Icon addEmptyIcon() {
    Icon icon = new IconWA(getIconsPath() + "/15px.gif", "");

    icons.add(icon);
    return icon;
  }

  /**
   * Method declaration
   *
   */
  public void setVerticalPosition() {
    viewType = VERTICAL_PANE;
  }

  /**
   * Method declaration
   * @param width
   *
   */
  public void setVerticalWidth(String width) {
    verticalWidth = width;
  }

  /**
   * Method declaration
   *
   */
  public void setHorizontalPosition() {
    viewType = HORIZONTAL_PANE;
  }

  /**
   * Method declaration
   * @param space
   *
   */
  public void setSpacing(String space) {
    this.spacing = space;
  }

  /**
   * Method declaration
   * @return
   *
   */
  public Vector getIcons() {
    return this.icons;
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
  public String getSpacing() {
    return this.spacing;
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

  /**
   * Method declaration
   * @return
   *
   */
  public abstract String print();
}

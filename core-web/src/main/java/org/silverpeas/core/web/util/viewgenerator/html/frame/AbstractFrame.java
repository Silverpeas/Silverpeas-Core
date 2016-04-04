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

package org.silverpeas.core.web.util.viewgenerator.html.frame;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

/**
 * @author mraverdy&lloiseau
 * @version 1.0
 */
public abstract class AbstractFrame implements Frame {

  private String titleFrame = null;
  private String top = null;
  private String bottom = null;
  private boolean hasMiddle = false;

  /**
   * Constructor declaration
   * @see
   */
  public AbstractFrame() {
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  /**
   * add a string on the top of the frame.
   */
  @Override
  public void addTop(String top) {
    this.top = top;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getTop() {
    return this.top;
  }

  /**
   * add a string on the bottom of the frame.
   */
  @Override
  public void addBottom(String bottom) {
    this.bottom = bottom;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getBottom() {
    return this.bottom;
  }

  /**
   * Method declaration
   * @param title
   * @see
   */
  @Override
  public void addTitle(String title) {
    this.titleFrame = title;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getTitle() {
    return this.titleFrame;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean hasMiddle() {
    return this.hasMiddle;
  }

  /**
   * Method declaration
   * @see
   */
  public void setMiddle() {
    this.hasMiddle = true;
  }

}

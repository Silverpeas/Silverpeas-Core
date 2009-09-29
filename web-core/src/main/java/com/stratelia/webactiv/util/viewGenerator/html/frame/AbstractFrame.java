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
 * Frame.java
 * 
 * Created on 27 march 2001, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.frame;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * 
 * @author mraverdy&lloiseau
 * @version 1.0
 */
public abstract class AbstractFrame implements Frame {

  private String titleFrame = null;
  // private String iconsPath = null;
  private String top = null;
  private String bottom = null;
  private boolean hasMiddle = false;

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public AbstractFrame() {
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getIconsPath() {
    /*
     * if (iconsPath == null) { ResourceLocator generalSettings = new
     * ResourceLocator("com.stratelia.webactiv.general", "fr");
     * 
     * iconsPath = generalSettings.getString("ApplicationURL") +
     * GraphicElementFactory.getSettings().getString("IconsPath"); } return
     * iconsPath;
     */
    return GraphicElementFactory.getIconsPath();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public abstract String print();

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public abstract String printBefore();

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public abstract String printMiddle();

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public abstract String printAfter();

  /**
   * add a string on the top of the frame.
   */
  public void addTop(String top) {
    this.top = top;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getTop() {
    return this.top;
  }

  /**
   * add a string on the bottom of the frame.
   */
  public void addBottom(String bottom) {
    this.bottom = bottom;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getBottom() {
    return this.bottom;
  }

  /**
   * Method declaration
   * 
   * 
   * @param title
   * 
   * @see
   */
  public void addTitle(String title) {
    this.titleFrame = title;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getTitle() {
    return this.titleFrame;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public boolean hasMiddle() {
    return this.hasMiddle;
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void setMiddle() {
    this.hasMiddle = true;
  }

}

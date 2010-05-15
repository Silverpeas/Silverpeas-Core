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
 * FLOSS exception.  You should have received a copy of the text describing
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
 * AbstractWindow.java
 * 
 * Created on 07 fevrier 2001, 09:35
 */

package com.stratelia.webactiv.util.viewGenerator.html.window;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar;
import com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane;

/**
 * @author neysseri
 * @version 1.0
 */
public abstract class AbstractWindow implements Window {

  private BrowseBar browseBar = null;
  private OperationPane operationPane = null;
  private GraphicElementFactory gef = null;
  private String body = null;
  private String width = null;

  // private String iconsPath = null;

  /**
   * Constructor declaration
   * @see
   */
  public AbstractWindow() {
  }

  /**
   * Method declaration
   * @param gef
   * @see
   */
  public void init(GraphicElementFactory gef) {
    this.gef = gef;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getBody() {
    return this.body;
  }

  /**
   * Method declaration
   * @param body
   * @see
   */
  public void addBody(String body) {
    this.body = body;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public GraphicElementFactory getGEF() {
    return this.gef;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getIconsPath() {
    /*
     * if (iconsPath == null) { ResourceLocator generalSettings = new
     * ResourceLocator("com.stratelia.webactiv.general", "fr"); iconsPath =
     * generalSettings.getString("ApplicationURL") +
     * GraphicElementFactory.getSettings().getString("IconsPath"); } return iconsPath;
     */
    return GraphicElementFactory.getIconsPath();
  }

  /**
   * Method declaration
   * @param width
   * @see
   */
  public void setWidth(String width) {
    this.width = width;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getWidth() {
    if (this.width == null) {
      this.width = "100%";
    }
    return this.width;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String printBefore();

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String printAfter();

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String print();

  /**
   * Method declaration
   * @return
   * @see
   */
  public OperationPane getOperationPane() {
    if (this.operationPane == null) {
      this.operationPane = getGEF().getOperationPane();
    }
    return this.operationPane;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public BrowseBar getBrowseBar() {
    if (this.browseBar == null) {
      this.browseBar = getGEF().getBrowseBar();
    }
    return this.browseBar;
  }

}

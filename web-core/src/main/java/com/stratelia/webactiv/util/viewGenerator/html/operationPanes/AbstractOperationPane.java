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

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import java.util.Vector;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * The default implementation of ArrayPane interface
 * @author squere
 * @version 1.0
 */
public abstract class AbstractOperationPane implements OperationPane {

  private Vector<String> stack = null;

  // private String iconsPath = null;

  /**
   * Constructor declaration
   * @see
   */
  public AbstractOperationPane() {
    stack = new Vector<String>();
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
   * @return
   * @see
   */
  public Vector<String> getStack() {
    return this.stack;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int nbOperations() {
    int nbOperations = 0;

    if (getStack() != null) {
      nbOperations = getStack().size();
    }
    return nbOperations;
  }

  /**
   * Method declaration
   * @param iconPath
   * @param altText
   * @param action
   * @see
   */
  public abstract void addOperation(String iconPath, String altText,
      String action);

  /**
   * Method declaration
   * @see
   */
  public abstract void addLine();

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String print();
}

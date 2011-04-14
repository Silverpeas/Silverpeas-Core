/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * ButtonWA.java
 * 
 * Created on 10 octobre 2000, 16:18
 */

package com.stratelia.webactiv.util.viewGenerator.html.buttons;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * @author neysseri
 * @version
 */
public abstract class AbstractButton implements Button {

  public String label;
  public String action;
  public boolean disabled;

  // private String iconsPath = null;

  /**
   * Creates new ButtonWA
   */
  public AbstractButton() {
  }

  /**
   * Method declaration
   * @param label
   * @param action
   * @param disabled
   * @see
   */
  public void init(String label, String action, boolean disabled) {
    this.label = label;
    this.action = action;
    this.disabled = disabled;
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
   * @param s
   * @see
   */
  public void setRootImagePath(String s) {
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String print();
}

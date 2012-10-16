/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * SimpleGraphicElement.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.tabs;

/**
 * @author squere
 * @version 1.0
 */
public class Tab {
  private String label;
  private String action;
  private boolean selected;
  private boolean enabled;

  /**
   * Constructor declaration
   * @param label
   * @param action
   * @param selected
   * @see
   */
  public Tab(String label, String action, boolean selected) {
    this.label = label;
    this.action = action;
    this.selected = selected;
    this.enabled = true;
  }

  /**
   * Constructor declaration
   * @param label
   * @param action
   * @param selected
   * @param enabled
   * @see
   */
  public Tab(String label, String action, boolean selected, boolean enabled) {
    this.label = label;
    this.action = action;
    this.selected = selected;
    this.enabled = enabled;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getLabel() {
    return label;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getAction() {
    return action;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean getSelected() {
    return selected;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean getEnabled() {
    return this.enabled;
  }

}

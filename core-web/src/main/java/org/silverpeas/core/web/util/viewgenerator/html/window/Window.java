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

package org.silverpeas.core.web.util.viewgenerator.html.window;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;
import org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar;
import org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane;

/**
 * The Window interface gives us the skeleton for all funtionnalities we need to display typical WA
 * window
 * @author neysseri
 * @version 1.0
 */
public interface Window extends SimpleGraphicElement {

  /**
   * Method declaration
   * @param gef
   * @see
   */
  public void init(GraphicElementFactory gef);

  /**
   * Method declaration
   * @param body
   * @see
   */
  public void addBody(String body);

  /**
   * Method declaration
   * @return
   * @see
   */
  public BrowseBar getBrowseBar();

  /**
   * Method declaration
   * @return
   * @see
   */
  public OperationPane getOperationPane();

  /**
   * Method declaration
   * @param width
   * @see
   */
  public void setWidth(String width);

  /**
   * Print the beginning of the window in an html format.
   * @return The html based line code
   */
  public String printBefore();

  /**
   * Print the end of the window in an html format.
   * @return The html based line code
   */
  public String printAfter();

  /**
   * Indicates if this Window should display a Browsebar.
   * @return true if this Window should display a Browsebar - false otherwise.
   */
  public boolean isBrowseBarVisible();

  /**
   * Defines if this Window should display a Browsebar.
   * @param browseBarVisible set to true if this Window should display a Browsebar - false
   * otherwise.
   */
  public void setBrowseBarVisibility(boolean browseBarVisible);

  public boolean isPopup();

  public void setPopup(boolean popup);
}

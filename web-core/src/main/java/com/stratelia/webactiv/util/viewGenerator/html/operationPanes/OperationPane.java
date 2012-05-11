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
 * "http://www.silverpeas.org/legal/licensing"
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
 * BrowseBar.java
 * 
 * Created on 07 decembre 2000, 11:26
 */

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * The Browse interface gives us the skeleton for all funtionnalities we need to display typical WA
 * browse bar
 * @author neysseri
 * @version 1.0
 */
public interface OperationPane extends SimpleGraphicElement {

  /**
   * Method declaration
   * @return
   * @see
   */
  public int nbOperations();

  /**
   * Method declaration
   * @param iconPath
   * @param altText
   * @param action
   * @see
   */
  public void addOperation(String iconPath, String altText, String action);

  /**
   * Method declaration
   * @see
   */
  public void addLine();

  /**
   * Print the browseBar in an html format.
   * @return The html based line code
   */
  public String print();

  public void setMultilang(ResourceLocator multilang);

}

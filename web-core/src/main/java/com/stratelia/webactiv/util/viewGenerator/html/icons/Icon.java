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
 * Button.java
 * 
 * Created on 10 octobre 2000, 16:16
 */

package com.stratelia.webactiv.util.viewGenerator.html.icons;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * @author neysseri
 * @version
 */
public interface Icon extends SimpleGraphicElement {

  /**
   * Method declaration
   * @param iconName
   * @param altText
   * @see
   */
  public void setProperties(String iconName, String altText);

  /**
   * Method declaration
   * @param iconName
   * @param altText
   * @param action
   * @see
   */
  public void setProperties(String iconName, String altText, String action);

  /**
   * Method declaration
   * @param iconName
   * @param altText
   * @param action
   * @param imagePath
   * @see
   */
  public void setProperties(String iconName, String altText, String action,
      String imagePath);

  /**
   * Method declaration
   * @param s
   * @see
   */
  public void setRootImagePath(String s);

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print();
}

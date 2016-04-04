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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * IconPane.java
 *
 * Created on 12 decembre 2000, 11:47
 */

package org.silverpeas.core.web.util.viewgenerator.html.iconpanes;

import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;
import org.silverpeas.core.web.util.viewgenerator.html.icons.Icon;

/**
 * @author neysseri
 * @version 1.0
 */
public interface IconPane extends SimpleGraphicElement {

  /**
   * Method declaration
   * @return
   * @see
   */
  public Icon addIcon();

  /**
   * Return an icon using the 1px image
   * @return an Icon
   * @see
   */
  public Icon addEmptyIcon();

  /**
   * Method declaration
   * @see
   */
  public void setVerticalPosition();

  /**
   * Method declaration
   * @param width
   * @see
   */
  public void setVerticalWidth(String width);

  /**
   * Method declaration
   * @see
   */
  public void setHorizontalPosition();

  /**
   * Method declaration
   * @param space
   * @see
   */
  public void setSpacing(String space);

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print();

}

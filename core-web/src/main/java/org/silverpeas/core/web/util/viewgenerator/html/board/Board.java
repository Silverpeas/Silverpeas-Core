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
 * Board.java
 *
 * Created on 27 march 2001, 16:11
 */

package org.silverpeas.core.web.util.viewgenerator.html.board;

import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

/**
 * Board is an interface to be implemented by a graphic element to print a frame in an html format.
 * @author lloiseau
 * @version 1.0
 */
public interface Board extends SimpleGraphicElement {
  /**
   * set CSS classes on the board.
   */
  public void setClasses(String classes);
  /**
   * add a string on the board.
   */
  public void addBody(String body);

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printBefore();

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printAfter();
}

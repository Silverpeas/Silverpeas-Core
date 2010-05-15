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
 * FrameWA.java
 * 
 * Created on 27 mars 2001, 15:22
 */

package com.stratelia.webactiv.util.viewGenerator.html.board;

/**
 * @author mraverdy&lloiseau
 * @version 1.0
 */
public class BoardSilverpeasV5 extends AbstractBoard {

  /**
   * Creates new FrameWA
   */
  public BoardSilverpeasV5() {
    super();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printBefore() {
    StringBuffer result = new StringBuffer();

    result.append("<CENTER>");
    result
        .append("<TABLE CELLPADDING=\"5\" CELLSPACING=\"0\" BORDER=\"0\" WIDTH=\"98%\" CLASS=\"tableBoard\"><TR><TD nowrap=\"nowrap\">");

    return result.toString();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printAfter() {
    StringBuffer result = new StringBuffer();

    result.append("</TD></TR></TABLE>");
    result.append("</CENTER>");

    return result.toString();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    StringBuffer result = new StringBuffer();

    result.append(printBefore());
    result.append(getBody());
    result.append(printAfter());

    return result.toString();
  }
}
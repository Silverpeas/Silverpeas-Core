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
 * Row.java
 * this object represent the row in week.
 * the event of week is represented by different row if the event have the same date.
 * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar
 * Created on 18 juin 2001, 10:26
 * @author Jean-Claude GROCCIA
 * jgroccia@silverpeas.com
 */

package com.stratelia.webactiv.util.viewGenerator.html.monthCalendar;

import java.util.Vector;

/**
 * Class declaration
 * @author
 */
class Row {

  private Vector<Event> listEventRow = null;

  /**
   * Creates the new row
   */
  public Row(Event evt) {
    listEventRow = new Vector<Event>();
    listEventRow.addElement(evt);
  }

  /**
   * Constructor declaration
   * @see
   */
  public Row() {
    listEventRow = new Vector<Event>();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Vector<Event> getListEvent() {
    return listEventRow;
  }

  /**
   * Method declaration
   * @param evt
   * @see
   */
  public void addEventIntRow(Event evt) {
    listEventRow.addElement(evt);
  }

}

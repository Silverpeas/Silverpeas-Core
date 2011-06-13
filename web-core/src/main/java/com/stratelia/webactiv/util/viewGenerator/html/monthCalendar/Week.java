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
 * Week.java
 * this object represent the week in the monthCalendar viewGenerator
 * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar
 * Created on 18 juin 2001, 10:26
 * @author Jean-Claude GROCCIA
 * jgroccia@silverpeas.com
 */
package com.stratelia.webactiv.util.viewGenerator.html.monthCalendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 * @author
 */
class Week extends Object {

  private Date startDate = null;
  private Date endDate = null;
  private Date[] dateDayOfWeek = null;
  private Day[] dayOfWeek = null;
  private List<Event> listEventWeek = null;
  private List<Row> listRow = null;

  /**
   * Creates the new Week
   * @param Day [], an array of Day
   * @param Vector , a list of object Event. the event of month
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Day
   * @see java.util.Vector
   * @return object Week
   */
  public Week(Day[] day, List<Event> listEventMonth) {
    SilverTrace.info("viewgenerator", "Week.Constructor",
        "root.MSG_GEN_ENTER_METHOD");
    listEventWeek = new ArrayList<Event>();
    listRow = new ArrayList<Row>();

    this.dayOfWeek = day;
    int lg = day.length;

    // this.numbersDayOfWeek = numbersDayOfWeek;
    this.startDate = day[0].getDate();
    this.endDate = day[lg - 1].getDate();

    // to initialise the list event for this week with the list event for the
    // current month
    listEventWeek = initListEventWeek(listEventMonth);

    this.listRow = initListRow(listEventWeek);
    SilverTrace.info("viewgenerator", "Week.Constructor",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * *****************
   */
  /**
   * private method
   */
  /**
   * *****************
   */
  /**
   * to initialise the event of the week
   * @param Vector , the list of object Event. The event of current month
   * @return Vector, the list of objectEvent. The event of this week
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event
   * @see java.util.Vector
   */
  private List<Event> initListEventWeek(List<Event> listEventMonth) {
    List<Event> v = new ArrayList<Event>();
    if (listEventMonth.isEmpty()) {
      return v;
    }
    Iterator<Event> itEvent = listEventMonth.iterator();

    while (itEvent.hasNext()) {
      Event currentEvt = itEvent.next();

      if (currentEvt.isInWeek(startDate, endDate)) {
        Date stDateEvt = null;
        Date edDateEvt = null;

        // Date.comparTo(Date argument)=> the value 0 if the argument Date is
        // equal to this Date;
        // a value less than 0 if this Date is before the Date argument;
        // and a value greater than 0 if this Date is after the Date argument

        // ramène le date du nouvel evenement (issue de l'evenement en cours)
        // au
        // limite de date de la semaine
        if (currentEvt.getStartDate().compareTo(startDate) <= 0) {
          stDateEvt = startDate;
        } else {
          stDateEvt = currentEvt.getStartDate();
        }

        if (currentEvt.getEndDate().compareTo(endDate) >= 0) {
          edDateEvt = endDate;
        } else {
          edDateEvt = currentEvt.getEndDate();
        }

        // Event evt = new Event(currentEvt.getId(), currentEvt.getName(),
        // stDateEvt, edDateEvt, currentEvt.getUrl(), currentEvt.getPriority());
        Event evt = new Event(currentEvt);
        evt.setStartDate(stDateEvt);
        evt.setEndDate(edDateEvt);
        v.add(evt);
      }
    }
    SilverTrace.info("viewgenerator", "Week.initListEventWeek()",
        "root.MSG_GEN_EXIT_METHOD");
    return v;
  }

  /**
   * to initialise the object Row in this week. if isn't possible to insert the event in the row,
   * the new row is created
   * @param Vector , the list of object Event of current week
   * @return Vector, the list of object Row of current week
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Row
   * @see java.util.Vector
   */
  private List<Row> initListRow(List<Event> listEventWeek) {

    List<Row> listRow = new ArrayList<Row>();
    // il y a au moins une row dans une semaine
    Row firstRow = new Row();
    listRow.add(firstRow);
    int index = 0;

    // contrôle s'il existe au moins un événement dans la semaine
    if (listEventWeek.isEmpty()) {
      return listRow;

    }

    for (Event evt : listEventWeek) {
      boolean resultAddEventInRow = false;
      Iterator<Row> itListRow = listRow.iterator();

      index = -1;

      while (!resultAddEventInRow) {
        // récupération du row courrant
        Row nextRow = itListRow.next();
        index++;
        // esaie l'ajout de l'event dans le row courrant
        resultAddEventInRow = addEventInRow(nextRow, evt);
        if (resultAddEventInRow) {
          // l'événement à été rajouter au row courrant, on remplace
          // l'ancien
          // row par le row courrant
          // listRow.removeElementAt(index);
          // listRow.add(index, tmpRow);
          break;
        } else {
          // il est impossible de rajouter l'evt dans le row courrant, on passe
          // au row suivant s'il existe sinon on le crée
          if (!(itListRow.hasNext())) {
            Row newRow = new Row();

            resultAddEventInRow = addEventInRow(newRow, evt); // tmp sera
            // toujours != de
            // null
            // listRow.removeElementAt(index);
            // ajout en dernier; index augmente de 1
            index++;
            listRow.add(newRow);
            break;
          }
        }
      }
    }
    return listRow;
  }

  /**
   * to add a object Event int the current row
   * @param Row , Event, the event who is add in the row
   * @return boolean, true if the operation "add" is succesfull, false else if
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Row
   * @see java.util.Vector
   */
  private boolean addEventInRow(Row currentRow, Event evt) {

    List<Event> listEvent = currentRow.getListEvent();

    if (listEvent.isEmpty()) {
      currentRow.addEventIntRow(evt);
      return true;
    }
    for (Event currentEvent : listEvent) {
      if (!(evt.compareTo(currentEvent))) {
        return false;
      }
    }
    currentRow.addEventIntRow(evt);
    return true;
  }

  /**
   * **************************
   */
  /**
   * getter an setter methods
   */
  /**
   * **************************
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public List<Event> getListEventWeek() {
    return listEventWeek;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Date[] getDateDayOfWeek() {
    return dateDayOfWeek;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Day[] getDayOfWeek() {
    return dayOfWeek;
  }

  /**
   * Method declaration
   * @param index
   * @return
   * @see
   */
  public Day getDayOfWeek(int index) {
    try {
      return dayOfWeek[index];
    } catch (java.lang.ArrayIndexOutOfBoundsException ae) {
      SilverTrace.warn("viewgenerator", "Week.getDayOfWeek()",
          "viewgenerator.EX_CANT_GET_DAY_OF_WEEK", "", ae);
      return dayOfWeek[0];
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public List<Row> getListRow() {
    return listRow;
  }
}

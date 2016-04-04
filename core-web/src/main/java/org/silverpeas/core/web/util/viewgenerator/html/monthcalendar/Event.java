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
 * Event.java
 * this object represent the day in the monthCalendar viewgenerator
 * @see org.silverpeas.core.web.util.viewgenerator.html.monthcalendar
 * Created on 18 juin 2001, 10:26
 * @author Jean-Claude GROCCIA
 * jgroccia@silverpeas.com
 */
package org.silverpeas.core.web.util.viewgenerator.html.monthcalendar;

import java.util.Calendar;
import java.util.Date;

/**
 * this class allows to convert objects into object "Event" usable by the monthCalendar
 */
public class Event {

  private String id = null;
  private String name = null;
  private Date startDate = null;
  private Date endDate = null;
  private String url = null;
  private int priority = 0;
  private String startHour = null;
  private String endHour = null;
  private String place = null;
  private String color = null;
  private String instanceId = null;
  private String tooltip = null;

  /**
   * Creates new Event
   */
  public Event(String id, String name, Date sDate, Date eDate, String url,
      int priority) {
    init(id, name, sDate, eDate, url, priority);
  }

  public Event(Event event) {
    init(event.getId(), event.getName(), event.getStartDate(), event.getEndDate(), event.getUrl(),
        event.
        getPriority());
    this.startHour = event.getStartHour();
    this.endHour = event.getEndHour();
    this.place = event.getPlace();
    this.color = event.getColor();
    this.instanceId = event.getInstanceId();
    this.tooltip = event.getTooltip();
  }

  private void init(String id, String name, Date sDate, Date eDate, String url,
      int priority) {
    this.id = id;
    this.name = name;

    Calendar cal = Calendar.getInstance();

    cal.setTime(sDate);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    this.startDate = cal.getTime();

    if (eDate == null) {
      this.endDate = startDate;
    } else {
      cal.setTime(eDate);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);

      this.endDate = cal.getTime();
    }

    this.url = url;

    this.priority = priority;
  }

  /**
   * ************************
   */
  /* getter and setter ******* */
  /**
   * ************************
   */
  public String getId() {
    return id;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Date getStartDate() {
    return new Date(startDate.getTime());
  }

  /**
   * Method declaration
   * @param date
   * @see
   */
  public void setStartDate(Date date) {
    startDate = new Date(date.getTime());
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Date getEndDate() {
    return new Date(endDate.getTime());
  }

  /**
   * Method declaration
   * @param date
   * @see
   */
  public void setEndDate(Date date) {
    endDate = new Date(date.getTime());
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getUrl() {
    return url;
  }

  /**
   * Method declaration
   * @param p
   * @see
   */
  public void setPriority(int p) {
    priority = p;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getPriority() {
    return priority;
  }

  /**
   * to compare the event with an other event this method return true if the event parameter span
   * the same day, false else if
   * @return boolean
   */
  public boolean compareTo(Event evt) {
    boolean val = true;

    Date startDateEvt = evt.getStartDate();
    Date endDateEvt = evt.getEndDate();

    int St_StEvt = startDate.compareTo(startDateEvt);
    int Ed_EdEvt = endDate.compareTo(endDateEvt);
    int Ed_StEvt = endDate.compareTo(startDateEvt);
    int St_EdEvt = startDate.compareTo(endDateEvt);

    if ((St_StEvt == 0) || (Ed_StEvt == 0) || (St_EdEvt == 0)
        || (Ed_EdEvt == 0)) {
      val = false;
    } else if ((St_StEvt > 0) && (St_EdEvt < 0)) {
      val = false;
    } else if ((Ed_StEvt > 0) && (Ed_EdEvt < 0)) {
      val = false;
    } else if ((St_StEvt < 0) && (Ed_EdEvt > 0)) // ok
    {
      val = false;
    } else if ((St_StEvt > 0) && (Ed_EdEvt < 0)) {
      val = false;
    }
    return val;
  }

  /**
   * @return
   */
  public String getEndHour() {
    return endHour;
  }

  /**
   * @return
   */
  public String getPlace() {
    return place;
  }

  /**
   * @return
   */
  public String getStartHour() {
    return startHour;
  }

  /**
   * @param string
   */
  public void setEndHour(String string) {
    endHour = string;
  }

  /**
   * @param string
   */
  public void setPlace(String string) {
    place = string;
  }

  /**
   * @param string
   */
  public void setStartHour(String string) {
    startHour = string;
  }

  /**
   * @author dlesimple
   * @return
   */
  public String getColor() {
    return color;
  }

  /**
   * @author dlesimple
   * @return
   */
  public void setColor(String color) {
    this.color = color;
  }

  /**
   * @author dlesimple
   * @return instanceId
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Get the InstanceId of the Event
   * @author dlesimple
   * @param instanceId
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getTooltip() {
    return tooltip;
  }

  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }
}
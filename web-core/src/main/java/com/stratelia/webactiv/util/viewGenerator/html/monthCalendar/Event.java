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
 * Event.java
 * this object represent the day in the monthCalendar viewGenerator
 * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar
 * Created on 18 juin 2001, 10:26
 * @author Jean-Claude GROCCIA
 * jgroccia@silverpeas.com
 */
package com.stratelia.webactiv.util.viewGenerator.html.monthCalendar;

import java.util.Calendar;
import java.util.Date;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * this class allows to convert objects into object "Event" usable by the monthCalendar
 */
public class Event {

  private String id = null;
  private String name = null;
  private Date startDate = null;
  private Date endDate = null;
  private String url = null;
  private int spanDay = 1;
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
    init(event.getId(), event.getName(), event.getStartDate(), event.getEndDate(), event.getUrl(), event.
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
    // this.style = initStyle();
    this.spanDay = initSpanDay();
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
   * Method declaration
   * @return
   * @see
   */
  public int getSpanDay() {
    return spanDay;
  }

  public int getSpanDay(Date beginPeriod) {
    return initSpanDay(beginPeriod);
  }

  /*
   * to control if the event is in Day
   * @param Day: the day to control if the current event is into this day
   * @return boolean
   */
  public boolean isInDay(Day day) {
    Date dateDay = day.getDate();

    SilverTrace.info("viewgenerator", "Event.isInDay()",
        "root.MSG_GEN_PARAM_VALUE", "dateDay = " + dateDay.toString()
        + ", startDate = " + startDate.toString());

    if (dateDay.equals(startDate)) {
      return true;
    }
    return false;
  }

  /*
   * to control if the date of event is in the week if the start date of event is before the satrat
   * day of week, the start day of event equals the start day of week if the end date of event is
   * over the end day of week, the end day of event equals end day of week
   * @param Date: start date and end date of week
   * @return boolean catch java.text.ParseException, write this exception int the log file
   * @see com.stratelia.webactiv.util.DateUtil;
   */
  public boolean isInWeek(Date sDate, Date eDate) {
    boolean val = false;

    Date sD = sDate;
    Date eD = eDate;

    if ((startDate.compareTo(eD) <= 0) && (endDate.compareTo(eD) >= 0)) {
      val = true;
    } else if ((startDate.compareTo(sD) >= 0) && (endDate.compareTo(eD) <= 0)) {
      val = true;

    } else if ((startDate.compareTo(sD) <= 0) && (endDate.compareTo(sD) >= 0)) {
      val = true;
    }
    return val;
  }

  /*
   * to initialize the parametar spanDay. This parameter represent le nombre de jour couvert par
   * l'evenement it's use by the print method of the class of extend AbstractMonthCalendar default
   * value for spanDay is 0
   * @return int
   * @catch java.text.ParseException, write this exception int the log file and return default value
   * @see com.stratelia.webactiv.util.DateUtil;
   */
  private int initSpanDay(Date date) {
    Date sD = date;
    Date eD = this.endDate;

    int span = ((int) ((eD.getTime() - sD.getTime()) / (3600 * 24 * 1000)));

    if (span == 1) {
      SilverTrace.info("viewgenerator", "Event.initSpanDay()",
          "root.MSG_GEN_PARAM_VALUE", "return 2");
      return 2;
    } else {
      SilverTrace.info("viewgenerator", "Event.initSpanDay()",
          "root.MSG_GEN_PARAM_VALUE", "return " + span + 1);
      return span + 1;
    }
  }

  private int initSpanDay() {
    Date sD = this.startDate;
    Date eD = this.endDate;

    int span = ((int) ((eD.getTime() - sD.getTime()) / (3600 * 24 * 1000)));

    if (span == 1) {
      return 2;
    } else {
      return span + 1;
    }
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

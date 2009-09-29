/*
 * AbstractCalendar.java
 * this class implements Calendar interface
 * this class implements the functionalities of a calendar.
 * for the use, you must necessarily create a new class inheriting this one
 * This class must implement the method String print().
 * for thue use:
 * 1 creates a new class extend AbstractCalendar and implements String print()
 * 2 uses method addEvents(Collection events), in order to initialize the list of the events 
 * 
 * @version
 */

package com.stratelia.webactiv.util.viewGenerator.html.calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public abstract class AbstractCalendar implements
    com.stratelia.webactiv.util.viewGenerator.html.calendar.Calendar {
  protected boolean navigationBar = true;
  protected boolean shortName = true;
  protected boolean monthVisible = true;
  protected String weekDayStyle = "class=\"txtnav\"";
  protected String monthDayStyle = "class=\"txtnav3\"";
  protected String monthDayStyleEvent = "class=\"intfdcolor3\"";
  private String context = "";
  protected ResourceLocator settings = null;
  protected String language = null;
  private List events = null;
  private List nonSelectableDays = null;
  private Date currentDate = null;
  private boolean emptyDayNonSelectable = false; // true => les jours sans

  // evenements ne sont pas
  // selectionnables

  public AbstractCalendar(String context, String language, Date date) {
    // this.context = context+URLManager.getURL(URLManager.CMP_AGENDA);
    this.context = context;
    this.language = language;
    this.currentDate = date;
    this.settings = new ResourceLocator(
        "com.stratelia.webactiv.multilang.generalMultilang", language);
  }

  public void setEvents(List events) {
    this.events = events;
  }

  public void addEvent(Event event) {
    if (events == null)
      events = new ArrayList();
    events.add(event);
  }

  public boolean isEmptyDayNonSelectable() {
    return emptyDayNonSelectable;
  }

  public void setEmptyDayNonSelectable(boolean nonSelectable) {
    this.emptyDayNonSelectable = nonSelectable;
  }

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setWeekDayStyle(String value) {
    weekDayStyle = value;
  }

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setMonthDayStyle(String value) {
    monthDayStyle = value;
  }

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setMonthVisible(boolean value) {
    monthVisible = value;
  }

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setNavigationBar(boolean value) {
    navigationBar = value;
  }

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @see
   */
  public void setShortName(boolean value) {
    shortName = value;
  }

  /**
   * @return
   */
  public String getContext() {
    return context;
  }

  public Collection getEvents() {
    return events;
  }

  public List getNonSelectableDays() {
    return nonSelectableDays;
  }

  public void setNonSelectableDays(List nonSelectableDays) {
    this.nonSelectableDays = nonSelectableDays;
  }

  public Date getCurrentDate() {
    return currentDate;
  }

  public void setCurrentDate(Date currentDate) {
    this.currentDate = currentDate;
  }
}
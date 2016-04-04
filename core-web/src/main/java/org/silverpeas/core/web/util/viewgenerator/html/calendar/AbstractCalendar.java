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

/*
 * AbstractCalendar.java
 * this class implements SilverpeasCalendar interface
 * this class implements the functionalities of a calendar.
 * for the use, you must necessarily create a new class inheriting this one
 * This class must implement the method String print().
 * for thue use:
 * 1 creates a new class extend AbstractCalendar and implements String print()
 * 2 uses method addEvents(Collection events), in order to initialize the list of the events
 *
 * @version
 */

package org.silverpeas.core.web.util.viewgenerator.html.calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.web.util.viewgenerator.html.monthcalendar.Event;

/**
 * Class declaration
 * @author
 */
public abstract class AbstractCalendar implements Calendar {
  protected boolean navigationBar = true;
  protected boolean shortName = true;
  protected boolean monthVisible = true;
  protected String weekDayStyle = "class=\"txtnav\"";
  protected String monthDayStyle = "class=\"txtnav3\"";
  protected String monthDayStyleEvent = "class=\"intfdcolor3\"";
  private String context = "";
  protected LocalizationBundle messages = null;
  protected String language = null;
  private List<Event> events = null;
  private List<Date> nonSelectableDays = null;
  private Date currentDate = null;
  private boolean emptyDayNonSelectable = false; // true => les jours sans

  // evenements ne sont pas
  // selectionnables

  public AbstractCalendar(String context, String language, Date date) {
    // this.context = context+URLUtil.getURL(URLUtil.CMP_AGENDA);
    this.context = context;
    this.language = language;
    this.currentDate = date;
    this.messages = ResourceLocator.getGeneralLocalizationBundle(language);
  }

  @Override
  public void setEvents(List<Event> events) {
    if (events != null) {
      this.events = new ArrayList<Event>(events);
    }
  }

  @Override
  public void addEvent(Event event) {
    if (events == null) {
      events = new ArrayList<Event>();
    }
    events.add(event);
  }

  public boolean isEmptyDayNonSelectable() {
    return emptyDayNonSelectable;
  }

  @Override
  public void setEmptyDayNonSelectable(boolean nonSelectable) {
    this.emptyDayNonSelectable = nonSelectable;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  @Override
  public void setWeekDayStyle(String value) {
    weekDayStyle = value;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  @Override
  public void setMonthDayStyle(String value) {
    monthDayStyle = value;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  @Override
  public void setMonthVisible(boolean value) {
    monthVisible = value;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  @Override
  public void setNavigationBar(boolean value) {
    navigationBar = value;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  @Override
  public void setShortName(boolean value) {
    shortName = value;
  }

  /**
   * @return
   */
  public String getContext() {
    return context;
  }

  public Collection<Event> getEvents() {
    return events;
  }

  public List<Date> getNonSelectableDays() {
    return nonSelectableDays;
  }

  public void setNonSelectableDays(List<Date> nonSelectableDays) {
    this.nonSelectableDays = nonSelectableDays;
  }

  public Date getCurrentDate() {
    return currentDate;
  }

  public void setCurrentDate(Date currentDate) {
    this.currentDate = currentDate;
  }
}
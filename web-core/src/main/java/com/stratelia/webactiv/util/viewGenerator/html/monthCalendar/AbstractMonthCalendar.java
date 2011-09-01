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

/*
 * AbstractSilverpeasCalendar.java
 * this class implements MonthCalendar interface
 * this class implements the functionalities of a monthly almanac.
 * for the use, you must necessarily create a new class inheriting this one
 * This class must implement the method String print().
 * for thue use:
 * 1 creates a new class extend AbstractMonthCalendar and implements String print()
 * 2 uses method addEvent(Event eventMonth), in order to initialize the list of the events of the month
 * 3 uses method setCurrentMonth(Date currentDate), in order to initialise monthCalendar for the current month
 *
 * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendar
 * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Week
 * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event
 * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Row
 * @author  Jean-Claude Groccia
 * @version
 */

package com.stratelia.webactiv.util.viewGenerator.html.monthCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 * @author
 */
public abstract class AbstractMonthCalendar implements MonthCalendar {

  /**
   * custumer constants
   */
  protected ResourceLocator messages = null;
  protected String language = null;

  /**
   * abstracts classes
   */
  @Override
  abstract public String print();

  protected Calendar cal = null;
  private List<Event> listEventMonth = null;
  private List<Week> listWeek = null;

  // name of first day of week
  private int firstDayOfWeek = Calendar.MONTH;
  private int lastDayOfWeek = Calendar.SUNDAY;
  private int numbersDayOfWeek = 7;
  // number of week in month
  private int numbersWeekInMonth = 0;
  private String propertieJour = "jour";

  /**
   * Creates new AbstractMonthCalendar: constructor
   * @param: language: the language of use of the monthCalendar.
   */
  public AbstractMonthCalendar(String language) {
    init(language);
  }

  public AbstractMonthCalendar(String language, int numbersDays) {
    init(language);
    if (numbersDays > 0) {
      numbersDayOfWeek = numbersDays;
    }
  }

  private void init(String language) {
    SilverTrace.info("viewgenerator", "AbstractMonthCalendar.Constructor",
        "root.MSG_GEN_ENTER_METHOD", " Language = " + language);
    listEventMonth = new ArrayList<Event>();
    listWeek = new ArrayList<Week>();
    cal = Calendar.getInstance();
    cal.clear(Calendar.HOUR);
    cal.clear(Calendar.HOUR_OF_DAY);
    cal.clear(Calendar.MINUTE);
    cal.clear(Calendar.SECOND);
    cal.clear(Calendar.MILLISECOND);
    this.language = language;
    this.messages = DateUtil.getMultilangProperties(language);
    initDayOfWeek();
    SilverTrace.info("viewgenerator", "AbstractMonthCalendar.Constructor",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * to add event in the month calendar
   * @param eventMonth : type com.stratelia.webactiv.util.viewGenerator.html.monthCalendar .Event:
   * the evenet of the current month
   * @return: void
   */
  @Override
  public void addEvent(Event eventMonth) {
    listEventMonth.add(eventMonth);
  }

  /**
   * Method declaration
   * @param listEventMonth
   * @see
   */
  @Override
  public void addEvent(List<Event> listEventMonth) {
    this.listEventMonth = new ArrayList<Event>(listEventMonth);
  }

  /**
   * to initialise the monthcalendar to current date
   * @param: currentDate: type java.util.Date: current date
   */
  @Override
  public void setCurrentMonth(Date currentDate) {
    SilverTrace.debug("viewgenerator", "MonthCalendarWA1.setCurrentMonth()",
        "root.MSG_GEN_PARAM_VALUE", "currentDate = " + currentDate);
    // to inititialse cal with currentDate
    cal.setTime(currentDate);
    cal.clear(Calendar.HOUR_OF_DAY);
    cal.clear(Calendar.HOUR);
    cal.clear(Calendar.MINUTE);
    cal.clear(Calendar.SECOND);
    cal.clear(Calendar.MILLISECOND);
    cal.setFirstDayOfWeek(this.firstDayOfWeek);
    numbersWeekInMonth = weeksIn(cal.get(Calendar.MONTH), this.firstDayOfWeek, 
        cal.get(Calendar.YEAR));
    SilverTrace.debug("viewgenerator", "MonthCalendarWA1.setCurrentMonth()",
        "root.MSG_GEN_PARAM_VALUE", "numbersWeekInMonth = "
        + numbersWeekInMonth);

    this.listWeek = initListWeek();
  }

  /**
   * to initialise the list week of this current month and create the object Day
   * @see: com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Week
   * @see: com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Day
   * @return: java.util.Vector: the vector of object Week
   */
  private List<Week> initListWeek() {
    // tri des évenements pas dates et horaires croissants
    EventBeginDateComparatorAsc comparator = new EventBeginDateComparatorAsc();
    Collections.sort(listEventMonth, comparator);

    List<Week> v = new ArrayList<Week>();
    Calendar calY = Calendar.getInstance();
    calY.setMinimalDaysInFirstWeek(1);
    calY.setTime(cal.getTime());
    calY.set(Calendar.HOUR_OF_DAY, 0);
    calY.set(Calendar.MINUTE, 0);
    calY.set(Calendar.SECOND, 0);
    calY.set(Calendar.MILLISECOND, 0);
    calY.setFirstDayOfWeek(cal.getFirstDayOfWeek());
    SilverTrace.debug("viewgenerator", "MonthCalendarWA1.setCurrentMonth()",
        "root.MSG_GEN_PARAM_VALUE", "calY.getTime() = " + calY.getTime());
    int tmpNbWeek = numbersWeekInMonth;

    calY.set(Calendar.WEEK_OF_MONTH, 1);
    calY.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

    SilverTrace.debug("viewgenerator", "MonthCalendarWA1.setCurrentMonth()",
        "root.MSG_GEN_PARAM_VALUE", "calY.getTime() = " + calY.getTime());

    for (int k = 1; k <= numbersWeekInMonth; k++) {
      boolean weekIsInThisMonth = false;
      Day[] day = new Day[numbersDayOfWeek];

      calY.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
      SilverTrace.debug("viewgenerator", "MonthCalendarWA1.setCurrentMonth()",
          "root.MSG_GEN_PARAM_VALUE", "for k=" + k + " calY.getTime() = "
          + calY.getTime());

      for (int z = 0; z < numbersDayOfWeek; z++) {
        Date dateDay = calY.getTime();
        boolean dayIsInThisMonth = getIsInThisMonthDay(dateDay);

        Day d = new Day(dateDay, getNameDay(dateDay), getNumbersDay(dateDay),
            dayIsInThisMonth);

        day[z] = d;

        if (!weekIsInThisMonth) {
          weekIsInThisMonth = dayIsInThisMonth;
        }
        calY.add(Calendar.DAY_OF_MONTH, 1);
      }
      // ajout de la nouvelle semaine seulement s'il y a au mois 1 jour qui
      // appartient au mois courrant

      if (weekIsInThisMonth) {
        Week newWeek = new Week(day, listEventMonth);
        v.add(newWeek);
      } else {
        tmpNbWeek -= 1;
      }

      calY.add(Calendar.DAY_OF_MONTH, 7 - numbersDayOfWeek);
    }
    numbersWeekInMonth = tmpNbWeek;
    return v;
  }

  /**
   * to obtain the name of day
   * @param: date: type java.util.Date: the date of day
   * @return: java.lang.String: the name of day
   */
  private String getNameDay(Date date) {
    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);
    return messages.getString(propertieJour
        + calendar.get(Calendar.DAY_OF_WEEK));

  }

  /**
   * to obtain the numbers of day
   * @param date : type java.util.Date: the date of day
   * @return java.lang.String: the name of day
   */
  private String getNumbersDay(Date date) {
    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);
    return String.valueOf(calendar.get(Calendar.DATE));
  }

  /**
   * to control if the param date is in the current month
   * @param: date: type java.util.Date: the date of day
   * @return: boolean: true if is in the month
   */
  private boolean getIsInThisMonthDay(Date date) {
    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);
    return calendar.get(Calendar.MONTH) == this.cal.get(Calendar.MONTH);
  }

  /**
   * to initialize the private parameters of the week, this method read in the file properties
   * @param: date: type java.util.Date: the date of day
   * @return: void
   * @catch: java.lang.Exception: write this exception in the log file
   */
  private void initDayOfWeek() {
    try {
      firstDayOfWeek = Integer.parseInt(messages.getString("weekFirstDay"));
      lastDayOfWeek = Integer.parseInt(messages.getString("weekLastDay"));
      numbersDayOfWeek = Integer.parseInt(messages
          .getString("numbersDayOfWeek"));

      cal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
    } catch (Exception e) {
      SilverTrace.warn("viewgenerator",
          "AbstractMonthCalendar.initDayOfWeek()",
          "viewgenerator.EX_CANT_LOAD_DATE_UTIL",
          "DateUtil propertie: firstDayOfWeek = " + firstDayOfWeek
          + "; lastDayOfWeek = " + lastDayOfWeek + "; numbersDayOfWeek = "
          + numbersDayOfWeek + "; numbersWeekInMonth = "
          + numbersWeekInMonth + ". ", e);
    }
  }

  /**
   * to obtain the numbers of week in current month
   * @param int: the current month
   * @param int: the firstDayOf Week
   * @see: java.util.Calendar
   * @return int: the number of week in the given month and firstDayOfWeek
   */
  private int weeksIn(int month, int firstDayOfWeek, int year) {
    SilverTrace.debug("viewgenerator", "MonthCalendarWA1.weeksIn()",
        "root.MSG_GEN_PARAM_VALUE", "month = " + month + ", firstDayOfWeek = "
        + firstDayOfWeek + ", year = " + year);

    // month--;
    Calendar calendar = Calendar.getInstance();

    calendar.setMinimalDaysInFirstWeek(1);

    SilverTrace.debug("viewgenerator", "MonthCalendarWA1.weeksIn()",
        "root.MSG_GEN_PARAM_VALUE", "minimal day in first week = "
        + calendar.getMinimalDaysInFirstWeek());

    calendar.setFirstDayOfWeek(firstDayOfWeek);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.DAY_OF_MONTH, calendar
        .getMaximum(Calendar.DAY_OF_MONTH));

    while (calendar.get(Calendar.MONTH) != month) {
      calendar.add(Calendar.DAY_OF_MONTH, -1);
    }
    SilverTrace.debug("viewgenerator", "MonthCalendarWA1.weeksIn()",
        "root.MSG_GEN_PARAM_VALUE", "calendar.getTime() = " + calendar.getTime());

    return calendar.get(Calendar.WEEK_OF_MONTH);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  protected String[] getHeaderNameDay() {
    String[] nameDay = new String[numbersDayOfWeek];
    Week firstWeek = listWeek.get(0);

    for (int i = 0; i < numbersDayOfWeek; i++) {
      nameDay[i] = firstWeek.getDayOfWeek(i).getName();
    }
    return nameDay;
  }

  /**
   * this method is use by the class who extend AbstractMonthCalendar the get method to obtain
   * numbers week in the current month
   * @return: int
   */
  protected int getNumbersWeekOfMonth() {
    return numbersWeekInMonth;
  }

  /**
   * this method is use by the class who extend AbstractMonthCalendar the get method to obtain
   * numbers day int the week of current month
   * @return: int
   */
  protected int getNumbersDayOfWeek() {
    return numbersDayOfWeek;
  }

  /**
   * this method is use by the class who extend AbstractMonthCalendar the get method to obtain the
   * list of Day
   * @param: int: the number of week int the current month
   * @return: Day[]: the array of Day
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Day
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Week
   * @see java.util.Calendar
   */
  protected Day[] getDayOfWeek(int week) {
    Week wk = listWeek.get(week - 1);
    Day[] d = wk.getDayOfWeek();

    return d;
  }

  /**
   * this method is use by the class who extend AbstractMonthCalendar the get method to obtain the
   * numbers of row int the week
   * @param: int: the week
   * @return: int:
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Week
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Row
   * @see java.util.Calendar
   */
  protected int getNumbersOfRow(int week) {
    Week wk = listWeek.get(week - 1);

    return wk.getListRow().size();
  }

  /**
   * this method is use by the class who extend AbstractMonthCalendar the get method to obtain an
   * array of object Event
   * @param week : the week.
   * @param row : the specific row in the week.
   * @return Event[]: an array of Event object
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Week
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event
   * @see com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Row
   * @see java.util.Calendar
   */
  protected Event[] getEventOfRow(int week, int row) {
    Week wk = listWeek.get(week - 1);
    Row currentRow = wk.getListRow().get(row);

    if (currentRow.getListEvent().isEmpty()) {
      return null;
    }
    int numbersEvent = currentRow.getListEvent().size();

    Event evt[] = new Event[numbersEvent];

    for (int i = 0; i < numbersEvent; i++) {
      evt[i] = currentRow.getListEvent().elementAt(i);
    }

    return evt;
  }
}
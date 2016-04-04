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

package org.silverpeas.core.web.tools.agenda.view;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.tools.agenda.control.AgendaRuntimeException;
import org.silverpeas.core.web.tools.agenda.control.AgendaSessionController;
import org.silverpeas.core.calendar.model.JournalHeader;
import org.silverpeas.core.calendar.model.SchedulableCount;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.exception.SilverpeasException;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

/**
 * Class declaration
 * @author
 */
public class CalendarHtmlView {

  private Vector<SchedulableCount> scheduleCounts = new Vector<SchedulableCount>();
  private boolean navigationBar = true;
  private boolean shortName = true;
  private boolean monthVisible = true;
  private String weekDayStyle = "class=\"txtnav\"";
  private String dayOffStyle = "class=\"txtdayoff1\"";
  private String dayOffStyleDayView = "class=\"txtdayoff3\"";
  private String weekDayOffStyle = "class=\"txtdayoff2\"";
  private String monthDayStyle = "class=\"txtnav3\"";
  private String monthDayStyleEvent = "class=\"intfdcolor6\"";
  private String context = "";

  public CalendarHtmlView() {
  }

  public CalendarHtmlView(String context) {
    this.context = context + URLUtil.getURL(URLUtil.CMP_AGENDA);
  }

  /**
   * Method declaration
   * @param scheduleCount
   * @see
   */
  public void add(SchedulableCount scheduleCount) {
    scheduleCounts.add(scheduleCount);
  }

  /**
   * Method declaration
   * @param day
   * @return
   * @see
   */
  public SchedulableCount getSchedulableCount(int day) {
    String d = String.valueOf(day);

    if (d.length() == 1) {
      d = "0" + d;

    }
    for (int i = 0; i < scheduleCounts.size(); i++) {
      SchedulableCount count = scheduleCounts.elementAt(i);

      if (count.getDay().endsWith(d)) {
        return count;
      }
    }
    return null;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  public void setWeekDayStyle(String value) {
    weekDayStyle = value;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  public void setMonthDayStyle(String value) {
    monthDayStyle = value;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  public void setMonthSelectedDayStyle(String value) {
    // monthSelectedDayStyle = value;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  public void setMonthVisible(boolean value) {
    monthVisible = value;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  public void setNavigationBar(boolean value) {
    navigationBar = value;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  public void setShortName(boolean value) {
    shortName = value;
  }

  /**
   * Method declaration
   * @param date
   * @param agendaSessionController
   * @return
   * @see
   */

  public String getHtmlView(Date date,
      AgendaSessionController agendaSessionController) {
    return getHtmlView(date, agendaSessionController, false);
  }

  /**
   * Method declaration
   * @param date
   * @param agendaSessionController
   * @return
   * @see
   */
  public String getPDAView(Date date,
      AgendaSessionController agendaSessionController) {
    return getHtmlView(date, agendaSessionController, true);
  }

  /**
   * Fonction ajoutée pour génerer le calendar soit pour un PDA (sans onmouseover) soit pour un web
   * classique.
   * @param date
   * @param agendaSessionController
   * @param forPDA
   * @return
   * @see
   */
  public String getHtmlView(Date date,
      AgendaSessionController agendaSessionController, boolean forPda) {
    boolean viewByDay = (AgendaHtmlView.BYDAY == agendaSessionController
        .getCurrentDisplayType());

    StringBuilder result = new StringBuilder(255);
    List<Date> nonSelectableDays = agendaSessionController.getNonSelectableDays();
    List<String> hiddenDays = null;
    try {
      hiddenDays = agendaSessionController.getHolidaysDates();
    } catch (RemoteException e) {
      throw new AgendaRuntimeException("CalendarView.getHtmlView()",
          SilverpeasException.ERROR, "agenda.MSG_GET_DAYS_OFF_FAILED", e);
    }

    int firstDayOfWeek = Integer.parseInt(agendaSessionController
        .getString("weekFirstDay"));

    if (!shortName) {
      result
          .append("<table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"2\">");
    } else {
      result
          .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\">");
    }

    Calendar calendar = Calendar.getInstance();

    calendar.setTime(date);

    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    int month = calendar.get(Calendar.MONTH);
    int year = calendar.get(Calendar.YEAR);

    // calcul du nombre de jour dans le mois
    calendar.add(Calendar.MONTH, 1);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.add(Calendar.DATE, -1);
    int numDays = calendar.get(Calendar.DAY_OF_MONTH);

    // calcul du jour de depart
    calendar.setTime(date);
    int startDay = 1;

    calendar.set(Calendar.DAY_OF_MONTH, 1);
    while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
      calendar.add(Calendar.DATE, -1);
      startDay++;
    }

    // Display Months name
    if (monthVisible) {
      result.append("<tr class=\"txtnav2\"><td colspan=\"7\">\n");
      result
          .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");
      if (navigationBar) {
        result
            .append("<td class=\"intfdcolor3\" align=\"right\"><a href=\"javascript:onClick=gotoPreviousMonth()\"");

        if (forPda)
          result
              .append(
              " onmouseout=\"MM_swapImgRestore()\" onmouseover=\"MM_swapImage('fle-2','','")
              .append(getContext()).append("icons/cal_fle-gon.gif',1)\"");

        result.append("><img name=\"fle-2\" border=\"0\" src=\"").append(
            getContext()).append(
            "icons/cal_fle-goff.gif\" width=\"8\" height=\"14\" alt=\"\"/></a></td> \n");
      }
      result.append(
          "<td class=\"intfdcolor3\" align=\"center\"><span class=\"txtNav4\">")
          .append(agendaSessionController.getString("mois" + month))
          .append(" ").append(year).append("</span></td>");
      if (navigationBar) {
        result
            .append("<td class=\"intfdcolor3\" align=\"left\"><a href=\"javascript:onClick=gotoNextMonth()\"");
        if (forPda)
          result
              .append(
              " onmouseout=\"MM_swapImgRestore()\" onmouseover=\"MM_swapImage('fle-1','','")
              .append(getContext()).append("icons/cal_fle-don.gif',1)\"");
        result.append("><img name=\"fle-1\" border=\"0\" src=\"").append(
            getContext()).append(
            "icons/cal_fle-doff.gif\" width=\"8\" height=\"14\" alt=\"\"/></a></td>\n");
      }
      result.append("</tr></table>\n");
      result.append("</td></tr>");
    }
    result.append("<tr class=\"intfdcolor2\">\n");

    // Display Months days name
    do {
      if (agendaSessionController.isSameDaysAreHolidays(calendar, month))
        result.append("<th ").append(weekDayOffStyle).append(">");
      else
        result.append("<th ").append(weekDayStyle).append(">");

      if (shortName) {
        result.append(agendaSessionController.getString("shortJour"
            + calendar.get(Calendar.DAY_OF_WEEK)));
      } else {
        result.append(agendaSessionController.getString("jour"
            + calendar.get(Calendar.DAY_OF_WEEK)));
      }
      result.append("</th>");
      calendar.add(Calendar.DATE, 1);
    } while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek);

    result.append("</tr>\n");

    // put blank table entries for days of week before beginning of the month
    result.append("<tr>\n");
    int column = 0;

    for (int i = 0; i < startDay - 1; i++) {
      result.append("<td ").append(monthDayStyle).append(
          " width=\"14%\">&nbsp;</td>");
      column++;
    }

    // Record in HashSet all the days of the month with an event
    calendar.setTime(date);
    String dayStyle = monthDayStyle;
    HashSet<Integer> dayWithEvents = new HashSet<Integer>();
    try {
      Collection<JournalHeader> events = agendaSessionController.getMonthSchedulables(date);
      for (JournalHeader event : events) {
        Calendar calendarEvents = Calendar.getInstance();
        calendarEvents.setTime(event.getStartDate());
        int currentMonth = calendar.get(Calendar.MONTH);

        while (calendarEvents.getTime().compareTo(event.getEndDate()) <= 0) {
          if (calendarEvents.get(Calendar.MONTH) == currentMonth) {
            int dayNumber = calendarEvents.get(Calendar.DAY_OF_MONTH);
            dayWithEvents.add(dayNumber);
          }
          calendarEvents.add(Calendar.DATE, 1);
        }
      }
      dayStyle = monthDayStyleEvent;
    } catch (RemoteException e) {
      throw new AgendaRuntimeException("CalendarView.getHtmlView()",
          SilverpeasException.ERROR, "agenda.MSG_GET_USER_EVENT_BYDAY_FAILED",
          e);
    }

    boolean isSelectableDate = true;

    Date currentDate = null;
    String d = null;
    SchedulableCount count = null;
    for (int i = 1; i <= numDays; i++) {
      boolean isVisibleDate = true;
      calendar.set(Calendar.DAY_OF_MONTH, i);

      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      // Write the day
      currentDate = calendar.getTime();
      d = DateUtil.getInputDate(currentDate, agendaSessionController
          .getLanguage());
      count = getSchedulableCount(i);

      if (hiddenDays != null) {
        if (hiddenDays.contains(DateUtil.date2SQLDate(currentDate)))
          isVisibleDate = false;
      }

      isSelectableDate = !nonSelectableDays.contains(currentDate);

      // If day has events
      dayStyle = monthDayStyle;
      if (dayWithEvents.contains(new Integer(i)))
        dayStyle = monthDayStyleEvent;

      if (count != null) {
        if (count.getCount() > 0) {
          if (isVisibleDate) {
            if (isSelectableDate)
              result.append("<td width=\"14%\" ").append(dayStyle).append(
                  " align=\"center\"><a ").append(dayStyle).append(
                  " href=\"javascript:selectDay('").append(d).append("')\">")
                  .append(i).append("</a></td>\n");
            else
              result.append(
                  "<td width=\"14%\" class=\"intfdcolor3\" align=\"center\">")
                  .append(i).append("</td>\n");
          } else {
            // Day off
            if (viewByDay) {
              result
                  .append("<td width=\"14%\" class=\"intfdcolor3\" align=\"center\">");
              dayOffStyle = dayOffStyleDayView;
            } else
              result
                  .append("<td width=\"14%\" class=\"intfdcolor4\" align=\"center\">");
            result.append("<span ").append(dayOffStyle).append(">").append(i)
                .append("</span></td>\n");
          }
        } else {
          if (isVisibleDate) {
            if (isSelectableDate)
              result.append("<td width=\"14%\" ").append(dayStyle).append(
                  " align=\"center\"><a ").append(dayStyle).append(
                  " href=\"javascript:selectDay('").append(d).append("')\">")
                  .append(i).append("</a></td>\n");
            else
              result.append(
                  "<td width=\"14%\" class=\"intfdcolor3\" align=\"center\">")
                  .append(i).append("</td>\n");
          } else {
            // Day off
            if (viewByDay) {
              result
                  .append("<td width=\"14%\" class=\"intfdcolor3\" align=\"center\">");
              dayOffStyle = dayOffStyleDayView;
            } else
              result
                  .append("<td width=\"14%\" class=\"intfdcolor4\" align=\"center\">");
            result.append("<span ").append(dayOffStyle).append(">").append(i)
                .append("</span></td>\n");
          }
        }
      } else {
        if (isVisibleDate) {
          if (isSelectableDate)
            result.append("<td width=\"14%\" ").append(dayStyle).append(
                " align=\"center\"><a ").append(dayStyle).append(
                " href=\"javascript:selectDay('").append(d).append("')\">")
                .append(i).append("</a></td>\n");
          else
            result.append(
                "<td width=\"14%\" class=\"intfdcolor3\" align=\"center\">")
                .append(i).append("</td>\n");
        } else {
          // Day off
          if (viewByDay) {
            result
                .append("<td width=\"14%\" class=\"intfdcolor3\" align=\"center\">");
            dayOffStyle = dayOffStyleDayView;
          } else
            result
                .append("<td width=\"14%\" class=\"intfdcolor4\" align=\"center\">");
          result.append("<span ").append(dayOffStyle).append(">").append(i)
              .append("</span></td>\n");
        }
      }

      // Check for end of week/row
      if ((++column == 7) && (numDays > i)) {
        result.append("</tr>\n<tr>");
        column = 0;
      }
    }
    for (int i = column; i <= 6; i++) {
      result.append("<td ").append(monthDayStyle).append(">&nbsp;</td>\n");
    }
    result.append("</tr></table>\n");

    return result.toString();
  }

  /**
   * @return
   */
  public String getContext() {
    return context;
  }

}
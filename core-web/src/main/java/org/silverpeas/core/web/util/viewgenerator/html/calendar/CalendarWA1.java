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

package org.silverpeas.core.web.util.viewgenerator.html.calendar;

import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.web.util.viewgenerator.html.monthcalendar.Event;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Class declaration
 * @author
 */
public class CalendarWA1 extends AbstractCalendar {
  public CalendarWA1(String context, String language, Date date) {
    super(context, language, date);
  }

  public String print() {
    StringBuffer result = new StringBuffer(255);
    List<Date> nonSelectableDays = getNonSelectableDays();
    boolean nonSelectable = isEmptyDayNonSelectable();

    int firstDayOfWeek = Integer.parseInt(messages.getString("GML.weekFirstDay"));

    if (!shortName) {
      result
          .append("<table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"2\">");
    } else {
      result
          .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\">");
    }

    Calendar calendar = Calendar.getInstance();

    calendar.setTime(getCurrentDate());

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
    calendar.setTime(getCurrentDate());
    int startDay = 1;

    calendar.set(Calendar.DAY_OF_MONTH, 1);
    while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
      calendar.add(Calendar.DATE, -1);
      startDay++;
    }

    if (monthVisible) {
      result.append("<tr class=\"txtnav2\"><td colspan=\"7\">\n");
      result
          .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");
      if (navigationBar) {
        result
            .append(
                "<td class=\"intfdcolor3\" align=\"right\"><a href=\"javascript:onClick=gotoPreviousMonth()\" onmouseout=\"MM_swapImgRestore()\" onmouseover=\"MM_swapImage('fle-2','','")
            .append(getContext())
            .append(
                "icons/cal_fle-gon.gif',1)\"><img name=\"fle-2\" border=\"0\" src=\"")
            .append(getContext())
            .append(
                "icons/cal_fle-goff.gif\" width=\"8\" height=\"14\" alt=\"\"/></a></td> \n");
      }
      result.append(
          "<td class=\"intfdcolor3\" align=\"center\"><span class=\"txtNav4\">")
          .append(messages.getString("GML.mois" + month)).append(" ").append(
          year).append("</span></td>");
      if (navigationBar) {
        result
            .append(
                "<td class=\"intfdcolor3\" align=\"left\"><a href=\"javascript:onClick=gotoNextMonth()\" onmouseout=\"MM_swapImgRestore()\" onmouseover=\"MM_swapImage('fle-1','','")
            .append(getContext())
            .append(
                "icons/cal_fle-don.gif',1)\"><img name=\"fle-1\" border=\"0\" src=\"")
            .append(getContext())
            .append(
                "icons/cal_fle-doff.gif\" width=\"8\" height=\"14\" alt=\"\"/></a></td>\n");
      }
      result.append("</tr></table>\n");
      result.append("</td></tr>");
    }
    result.append("<tr class=\"intfdcolor2\">\n");

    do {
      if (shortName) {
        result.append("<th ").append(weekDayStyle).append(">").append(
            messages.getString("GML.shortJour"
            + calendar.get(Calendar.DAY_OF_WEEK))).append("</th>");
      } else {
        result.append("<th ").append(weekDayStyle).append(">")
            .append(
            messages.getString("GML.jour"
            + calendar.get(Calendar.DAY_OF_WEEK))).append("</th>");
      }
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
    calendar.setTime(getCurrentDate());
    String dayStyle = monthDayStyle;
    HashSet<Integer> dayWithEvents = new HashSet<Integer>();

    Collection<Event> events = getEvents();
    if (events != null) {
      for (Event event : events) {
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
    }
    dayStyle = monthDayStyleEvent;

    boolean isSelectableDate = true;
    Date currentDate = null;
    String d = null;
    for (int i = 1; i <= numDays; i++) {
      calendar.set(Calendar.DAY_OF_MONTH, i);

      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      // Write the day
      currentDate = calendar.getTime();
      d = DateUtil.getInputDate(currentDate, language);

      isSelectableDate = !(nonSelectableDays != null && nonSelectableDays.contains(currentDate));

      // If day has events
      dayStyle = monthDayStyle;
      boolean isSelectable = true;
      // si "nonSelectable = true" on fait la diférence entre les jours avec
      // évènements et ceux sans évènements
      if (nonSelectable)
        isSelectable = false;
      if (dayWithEvents.contains(new Integer(i))) {
        dayStyle = monthDayStyleEvent;
        isSelectable = true;
      }

      if (isSelectableDate) {
        if (isSelectable)
          result.append(
              "<td width=\"14%\" class=\"intfdcolor3\" align=\"center\"><a ")
              .append(dayStyle).append(" href=\"javascript:selectDay('")
              .append(d).append("')\">").append(i).append("</a></td>\n");
        else
          result.append("<td width=\"14%\" ").append(monthDayStyle).append(
              " align=\"center\">").append(i).append("</td>\n");
      } else
        result.append("<td width=\"14%\" ").append(monthDayStyle).append(
            " align=\"center\">").append(i).append("</td>\n");

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

}
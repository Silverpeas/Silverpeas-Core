/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.core.personalorganizer.model.Category;
import org.silverpeas.core.personalorganizer.model.JournalHeader;
import org.silverpeas.core.personalorganizer.model.Schedulable;
import org.silverpeas.core.personalorganizer.model.SchedulableCount;
import org.silverpeas.core.personalorganizer.model.SchedulableGroup;
import org.silverpeas.core.personalorganizer.model.SchedulableList;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.tools.agenda.control.AgendaException;
import org.silverpeas.core.web.tools.agenda.control.AgendaRuntimeException;
import org.silverpeas.core.web.tools.agenda.control.AgendaSessionController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

@Deprecated(forRemoval = true)
public class AgendaHtmlView {

  public static final int BYDAY = 1;
  public static final int BYWEEK = 2;
  public static final int BYMONTH = 3;
  public static final int BYYEAR = 4;
  public static final int CHOOSE_DAYS = 5;
  private int WEEKDAYNUMBER = 7;
  private int BEGINHOUR = 8;
  private int ENDHOUR = 18;
  private String startDate;
  private Vector<Schedulable> schedules = new Vector<Schedulable>();
  private CalendarHtmlView calendarHtmlView = null;
  private int viewType = 0;
  private AgendaSessionController agendaSessionController;
  private boolean calendarVisible = true;
  private boolean isOtherAgenda = false;
  private String dayOffStyle = "class=\"txtdayoff1\"";
  private String weekDayOffStyle = "class=\"txtdayoff2\"";

  /**
   * Constructor declaration
   *
   * @param viewType
   *
   */
  public AgendaHtmlView(int viewType) {
    this.viewType = viewType;
    setDate(null);
  }

  /**
   * Constructor declaration
   *
   * @param viewType
   * @param date
   * @param agendaSessionController
   * @param settings
   *
   */
  public AgendaHtmlView(int viewType, Date date,
      AgendaSessionController agendaSessionController, SettingBundle settings) {
    this.viewType = viewType;
    setDate(date);
    this.agendaSessionController = agendaSessionController;
    isOtherAgenda = agendaSessionController.isOtherAgendaMode();
    WEEKDAYNUMBER = settings.getInteger("weekDayNumber");
    BEGINHOUR = settings.getInteger("beginHour");
    ENDHOUR = settings.getInteger("endHour");

  }

  /**
   * Method declaration
   *
   * @param on
   *
   */
  public void setCalendarVisible(boolean on) {
    this.calendarVisible = on;
  }

  /**
   * Method declaration
   *
   * @param date
   *
   */
  public final void setDate(Date date) {
    if (date == null) {
      date = new Date();
    }
    if (viewType == BYDAY) {
      startDate = DateUtil.date2SQLDate(date);
    }
    if (viewType == BYWEEK) {
      startDate = DateUtil.date2SQLDate(date);
    }
    if (viewType == BYMONTH) {
      startDate = DateUtil.date2SQLDate(date);
    }
    if (viewType == BYYEAR) {
      startDate = DateUtil.date2SQLDate(date);
    }
  }

  /**
   * Method declaration
   *
   * @param scheduleCount
   *
   */
  public void add(SchedulableCount scheduleCount) {
    if (calendarHtmlView == null) {
      calendarHtmlView = new CalendarHtmlView();
    }
    calendarHtmlView.add(scheduleCount);
  }

  /**
   * Method declaration
   *
   * @param schedule
   *
   */
  public void add(Schedulable schedule) {
    if (schedule.getStartDay() == null) {
      return;
    }
    try {
      if (schedule.getStartHour() != null) {
        if (schedule.getStartHour().compareTo(
            Schedulable.hourMinuteToString(BEGINHOUR, 0)) < 0) {
          schedule.setStartHour(Schedulable.hourMinuteToString(BEGINHOUR, 0));
        }
      }
      if (schedule.getEndHour() != null) {
        if (schedule.getEndHour().compareTo(
            Schedulable.hourMinuteToString(ENDHOUR, 0)) > 0) {
          schedule.setEndHour(Schedulable.hourMinuteToString(ENDHOUR, 0));
        }
      }
      if (schedule.getStartDay().equals(schedule.getEndDay())) {
        schedules.add(schedule);
      } else {
        // if the schedule start and end day are different, let's cut in smaller
        // schedules
        Calendar start = Calendar.getInstance();

        start.setTime(schedule.getStartDate());
        while (DateUtil.date2SQLDate(start.getTime()).compareTo(
            schedule.getEndDay()) <= 0) {
          Schedulable copy = schedule.getCopy();

          copy.setStartDay(DateUtil.date2SQLDate(start.getTime()));
          if (DateUtil.date2SQLDate(start.getTime()).compareTo(
              schedule.getStartDay()) != 0) {
            if (schedule.getStartHour() != null) {
              copy.setStartHour(Schedulable.hourMinuteToString(BEGINHOUR, 0));
            }
          }
          copy.setEndDay(null);
          if (DateUtil.date2SQLDate(start.getTime()).compareTo(
              schedule.getEndDay()) != 0) {
            if (schedule.getEndHour() != null) {
              copy.setEndHour(Schedulable.hourMinuteToString(ENDHOUR, 0));
            }
          }
          schedules.add(copy);
          start.add(Calendar.DATE, 1);
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  /**
   * Method declaration
   *
   * @return
   *
   */
  public String getHtmlView() throws AgendaException {
    if (viewType == BYDAY) {
      return getHtmlViewByDay(startDate);
    }
    if (viewType == BYWEEK) {
      return getHtmlViewByWeek(startDate);
    }
    if (viewType == BYMONTH) {
      return getHtmlViewByMonth(startDate);
    }
    if (viewType == BYYEAR) {
      return getHtmlViewByYear(startDate);
    }
    return "";
  }

  /**
   * Method declaration
   *
   * @param startDate
   * @return
   *
   */
  public String getHtmlViewByMonth(String startDate) {
    if (calendarHtmlView == null) {
      calendarHtmlView = new CalendarHtmlView();
    }
    try {
      calendarHtmlView.setShortName(false);
      calendarHtmlView.setNavigationBar(false);
      calendarHtmlView.setMonthVisible(false);
      calendarHtmlView.setWeekDayStyle("class=\"txtnav\"");
      calendarHtmlView.setMonthDayStyle("class=\"intfdcolor4\"");
      calendarHtmlView.setMonthSelectedDayStyle("class=\"intfdcolor6\"");
      StringBuffer result = new StringBuffer();

      result
          .append("\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"98%\">\n");
      result.append("\t\t\t\t<tr> \n");
      result.append("\t\t\t\t\t<td> \n");
      result
          .append(
              "\t\t\t\t\t\t<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\">\n");
      result.append("\t\t\t\t\t\t\t<tr>\n");
      result.append("\t\t\t\t\t\t\t\t<td class=\"grille\">\n");
      result
          .append(
              "\t\t\t\t\t\t\t\t\t<table border=\"0\" cellpadding=\"0\" cellspacing=\"1\" class=\"intfdcolor\" width=\"100%\">\n");
      result.append("\t\t\t\t\t\t\t\t\t\t<tr>\n");
      result.append("\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" class=\"grille\"> ");
      result.append(calendarHtmlView
          .getHtmlView(DateUtil.parse(startDate), agendaSessionController));
      result.append("\t\t\t\t\t\t\t\t\t\t\t</td>");
      result.append("                    </tr>");
      result.append("                  </table>");
      result.append("                </td>");
      result.append("              </tr>");
      result.append("            </table>");
      result.append("          </td>");
      result.append("        </tr>");
      result.append("      </table>");

      return result.toString();
    } catch (java.text.ParseException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return "";
    }
  }

  /**
   * Method declaration
   *
   * @param startDate
   * @return
   *
   */
  public String getHtmlViewByYear(String startDate) {
    CalendarHtmlView calendarHtmlView = new CalendarHtmlView();

    calendarHtmlView.setNavigationBar(false);
    calendarHtmlView.setWeekDayStyle("class=\"txtnav\"");
    calendarHtmlView.setMonthDayStyle("class=\"intfdcolor4\"");
    calendarHtmlView.setMonthSelectedDayStyle("class=\"intfdcolor6\"");
    StringBuilder result = new StringBuilder();

    result.append("      <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"98%\">\n");
    result.append("        <tr> \n");
    result.append("          <td> \n");

    result.append(
        "            <table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\">\n");
    result.append("              <tr> \n");
    result.append("                <td class=\"grille\"> \n");
    result.append(
        "                  <table border=\"0\" cellpadding=\"1\" cellspacing=\"1\" " +
            "width=\"100%\">\n");
    String year = startDate.substring(0, 4);
    int month = 1;

    for (int i = 0; i < 3; i++) {
      result.append("<tr>");
      for (int j = 0; j < 4; j++) {
        result.append("<td bgcolor=\"#ffffff\" align=\"left\" valign=\"top\">");
        try {
          String m = String.valueOf(month);

          if (m.length() == 1) {
            m = "0" + m;
          }
          result.append(calendarHtmlView.getHtmlView(DateUtil.parse(year + "/" + m + "/01"),
              agendaSessionController));
          month++;
        } catch (java.text.ParseException e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
          return "";
        }
        result.append("</td>\n");
      }
      result.append("</tr>");
    }
    result.append("                  </table>");
    result.append("                </td>");
    result.append("              </tr>");
    result.append("            </table>");
    result.append("          </td>");
    result.append("        </tr>");
    result.append("      </table>");
    return result.toString();
  }

  /**
   * Method declaration
   *
   * @param today
   * @return
   *
   */
  public String getHtmlViewByDay(String today) throws AgendaException {

    StringBuilder result = new StringBuilder();
    Calendar day = Calendar.getInstance();

    try {
      day.setTime(DateUtil.parse(today));
    } catch (java.text.ParseException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return "";
    }
    SchedulableList dayList = new SchedulableList(DateUtil.date2SQLDate(day.getTime()), schedules);

    result.append(
        "<table border=\"0\" width=\"98%\" cellspacing=\"2\" cellpadding=\"0\" " +
            "class=\"grille\">\n");
    result.append("<tr valign=\"top\">");
    result.append("<td>");
    result.append(
        "<table border=\"0\" align=\"center\" width=\"100%\" cellspacing=\"0\" " +
            "cellpadding=\"0\">\n");
    result.append("  <tr>\n");
    result.append("    <td width=\"100%\">");
    Vector<Schedulable> all = dayList.getWithoutHourSchedules();

    result.append(
        "      <table class=\"grille\" border=\"0\" align=\"center\" width=\"100%\" cellspacing=\"1\" cellpadding=\"1\">");

    if (all.size() > 0) {

      for (int i = 0; i < all.size(); i++) {
        Schedulable schedule = all.elementAt(i);
        result.append("  <tr>");
        if (i == 0) {
          result.append("    <td width=\"50\" rowspan=\"")
              .append(all.size())
              .append("\" align=\"right\" bgcolor=\"#FFFFFF\" nowrap=\"nowrap\" valign=\"top\">");
          result.append("&nbsp;</td>");
        }

        if (isOtherAgenda) {
          if (schedule.getClassification().isPrivate()) {
            result.append("    <td class=\"privateEvent\" width=\"600\">");
          } else {
            result.append("    <td class=\"publicEvent\" width=\"600\">");
          }
        } else {
          result.append("    <td class=\"intfdcolor4\" width=\"600\">");
        }

        if (schedule.getClassification().isPublic() || !isOtherAgenda) {
          result.append("      <a href=\"" + "javascript:onClick=viewJournal('")
              .append(schedule.getId())
              .append("')\"");
          result.append(getInfoBulle(schedule));
          result.append(WebEncodeHelper.javaStringToHtmlString(schedule.getName())).append("</a>");
        }

        result.append(" &nbsp;(" + agendaSessionController.getString("allDay"))
            .append(")    </td>");
        result.append("</tr>\n");
      }
    } else {
      result.append("  <tr>");
      result.append(
          "    <td width=\"50\" align=\"right\" bgcolor=\"#FFFFFF\" nowrap=\"nowrap\" " +
              "valign=\"top\">");
      result.append("&nbsp;</td>");
      result.append("    <td class=\"intfdcolor4\" width=\"600\">");
      result.append("&nbsp;");
      result.append("    </td>");
      result.append("  </tr>\n");
    }
    result.append("      </table>\n");
    result.append("    </td>");
    result.append("  </tr>\n");

    result.append("<tr> ");
    result.append(
        "<td class=\"intfdcolor3\"><img src=\"icons/1px.gif\" height=\"2\" width=\"1\" alt=\"\"/></td>");
    result.append("</tr>\n");

    int i = BEGINHOUR;
    int maxColumns = 0;
    List<Schedulable> lastGoOn = null;
    List<Schedulable> goOn;

    while (i < ENDHOUR) {
      String hour = Schedulable.quarterCountToHourString(i * 4);
      String nextHour = Schedulable.quarterCountToHourString(i * 4 + 4);
      Schedulable thisHour = new JournalHeader("", "");

      thisHour.setStartDate(day.getTime());
      try {
        thisHour.setStartHour(hour);
        thisHour.setEndHour(nextHour);
      } catch (Exception e) {
        throw new AgendaRuntimeException(e);
      }

      dayList.getStartingSchedules(hour, nextHour);
      goOn = new ArrayList<Schedulable>();
      for (int dayListIterator = 0; dayListIterator < schedules.size(); dayListIterator++) {
        Schedulable sched = schedules.elementAt(dayListIterator);

        if (sched.isOver(thisHour)) {
          goOn.add(sched);
        }
      }

      if ((!goOn.isEmpty()) && (maxColumns == 0)) {
        // compute the number of columns to display
        maxColumns = 1;
        int maxTime = 0;
        Schedulable tmpThisHour = new JournalHeader("", "");

        tmpThisHour.setStartDate(day.getTime());
        int countColumns;
        do {
          String tmpHour = Schedulable.quarterCountToHourString((i + maxTime) * 4);
          String tmpNextHour = Schedulable.quarterCountToHourString((i + maxTime + 1) * 4);
          try {
            tmpThisHour.setStartHour(tmpHour);
            tmpThisHour.setEndHour(tmpNextHour);
          } catch (Exception e) {
            throw new AgendaRuntimeException(e);
          }
          countColumns = 0;
          for (int iterator = 0; iterator < schedules.size(); iterator++) {
            Schedulable sched = schedules.elementAt(iterator);

            if (sched.isOver(tmpThisHour)) {
              countColumns++;
            }
          }
          if (countColumns > maxColumns) {
            maxColumns = countColumns;
          }
          maxTime++;
        } while (countColumns != 0);
      }

      if (lastGoOn == null) {
        // ouverture de la table
        result.append("  <tr>");
        result.append("    <td>");
        result.append(
            "      <table class=\"grille\" border=\"0\" align=\"center\" width=\"100%\" cellspacing=\"1\" cellpadding=\"1\">\n");
      } else {
        if (((goOn.size() != 0) && (lastGoOn.size() == 0))
            || ((goOn.size() == 0) && (lastGoOn.size() != 0))) {
          result.append("      </table>\n");
          result.append("    </td>");
          result.append("  </tr>\n");
          result.append("  <tr>");
          result.append("    <td>");
          result.append(
              "      <table class=\"grille\" border=\"0\" align=\"center\" width=\"100%\" cellspacing=\"1\" cellpadding=\"1\">\n");
        }
      }

      result.append("       <tr>");
      result.append(
          "        <td width=\"50\" align=\"right\" bgcolor=\"#FFFFFF\" nowrap=\"nowrap\" " +
              "valign=\"top\">");
      result.append("<a href=\"javascript:onClick=selectHour('").append(i).append("')\">");
      result.append(String.valueOf(i)).append("H</a>");
      result.append("        </td>");

      if (goOn.isEmpty()) {
        result.append("        <td class=\"intfdcolor4\" width=\"600\">&nbsp;</td>");
        maxColumns = 0;
      } else {
        for (Schedulable schedule : goOn) {
          boolean start = true;

          if (lastGoOn != null) {
            if (lastGoOn.contains(schedule)) {
              start = false;
            }
          }
          if (start) {
            int length = 0;
            Schedulable tmpThisHour = new JournalHeader("", "");

            tmpThisHour.setStartDate(day.getTime());
            do {
              length++;
              String tmpHour = Schedulable.quarterCountToHourString((i + length) * 4);
              String tmpNextHour = Schedulable.quarterCountToHourString((i
                  + length + 1) * 4);

              try {
                tmpThisHour.setStartHour(tmpHour);
                tmpThisHour.setEndHour(tmpNextHour);
              } catch (Exception e) {
                throw new AgendaRuntimeException(e);
              }
            } while (tmpThisHour.isOver(schedule));

            String color = "intfdcolor2";
            for (int iterator = 0; iterator < schedules.size(); iterator++) {
              Schedulable sched = schedules.elementAt(iterator);

              if (!sched.getId().equals(schedule.getId())) {
                if (sched.isOver(schedule)) {
                  color = "intfdcolor2";
                }
              }
            }
            if (isOtherAgenda) {
              color = "publicEvent";
              if (schedule.getClassification().isPrivate()) {
                color = "privateEvent";
              }
            }

            result.append("<td width=\"")
                .append((600 / maxColumns == 0 ? 1:maxColumns))
                .append("\" class=\"")
                .append(color)
                .append("\" rowspan=\"")
                .append(length)
                .append("\">");
            if (isOtherAgenda) {
              if (schedule.getClassification().isPrivate()) {
                if (!schedule.getEndHour().equals(schedule.getStartHour())) {
                  result.append(schedule.getStartHour())
                      .append(" - ")
                      .append(schedule.getEndHour())
                      .append("<br>");
                } else {
                  result.append(schedule.getStartHour()).append("<br>");
                }
              } else {
                result.append("      <a href=\"javascript:onClick=viewJournal('")
                    .append(schedule.getId())
                    .append("')\"");
                result.append(getInfoBulle(schedule));
                if (!schedule.getEndHour().equals(schedule.getStartHour())) {
                  result.append(schedule.getStartHour())
                      .append(" - ")
                      .append(schedule.getEndHour())
                      .append("<br>");
                } else {
                  result.append(schedule.getStartHour()).append("<br>");
                }
                result.append(WebEncodeHelper.javaStringToHtmlString(schedule.getName()))
                    .append("</a>");
              }
            } else {
              result.append("      <a href=\"javascript:onClick=viewJournal('")
                  .append(schedule.getId())
                  .append("')\"");
              result.append(getInfoBulle(schedule));
              if (!schedule.getEndHour().equals(schedule.getStartHour())) {
                result.append(schedule.getStartHour())
                    .append(" - ")
                    .append(schedule.getEndHour())
                    .append("<br>");
              } else {
                result.append(schedule.getStartHour()).append("<br>");
              }
              result.append(WebEncodeHelper.javaStringToHtmlString(schedule.getName())).append("</a>");
            }
            result.append("</td>");
          }
        }
        for (int maxColumnsIterator = goOn.size(); maxColumnsIterator < maxColumns;
            maxColumnsIterator++) {
          result.append("        <td class=\"intfdcolor4\" width=\"")
              .append((600 / maxColumns))
              .append("\">&nbsp;</td>");
        }
      }
      result.append("       </tr>\n");
      lastGoOn = goOn;
      i++;

    }
    result.append(" </table></td></tr>\n");
    result.append("</table>\n");
    result.append("</td>");
    if (calendarVisible) {
      result.append("<td width=\"100\" class=\"intfdcolor2\">\n");
    } else {
      result.append("<td width=\"10\" class=\"intfdcolor2\">\n");
    }

    // display the calendar
    result.append("<!-- [ CALENDRIER ] --> \n");
    result.append(" <table width=\"100%\" border=\"0\" cellpadding=\"1\" cellspacing=\"0\">\n");
    result.append(" <tr> \n");
    result.append("  <td>\n");
    result.append(
        "   <table class=\"intfdcolor2\" border=\"0\" cellpadding=\"1\" cellspacing=\"0\">");
    result.append("    <tr> ");
    result.append("     <td align=\"right\" class=\"intfdcolor2\">");
    if (!calendarVisible) {
      result.append(
          "        <a href=\"javascript:onClick=openCalendar()\"><img src=\"icons/cal_open.gif\" " +
              "width=\"16\" height=\"14\" border=\"0\" alt=\"Afficher le calendrier\" " +
              "title=\"Afficher le calendrier\"/></a> \n");
    } else {
      result.append(
          "        <a href=\"javascript:onClick=closeCalendar()\"><img src=\"icons/croix3.gif\" " +
              "width=\"16\" height=\"14\" border=\"0\" alt=\"Fermer le calendrier\" " +
              "title=\"Fermer le calendrier\"/></a> \n");
    }
    result.append("     </td>");
    result.append("    </tr> ");
    if (calendarVisible) {
      result.append("    <tr> ");
      result.append("     <td>");
      result.append(
          "       <table border=\"0\" cellpadding=\"0\" cellspacing=\"1\" class=\"intfdcolor\"> " +
              "\n");
      result.append("        <tr><td><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
      result.append("         <tr>\n");
      result.append("            <td align=\"center\" class=\"txtbigdate\">")
          .append(day.get(Calendar.DAY_OF_MONTH));
      result.append("            </td></tr>");
      result.append("            <tr><td align=\"center\" class=\"txtnav3\">")
          .append(agendaSessionController.getString("mois" + day.get(Calendar.MONTH)));
      result.append("            </td></tr>");
      result.append("            <tr><td align=\"center\" class=\"txtnav3\">")
          .append(DateUtil.getInputDate(day.getTime(), agendaSessionController.getLanguage()));
      result.append("            </td></tr>\n");
      result.append("            <tr><td>\n");

      try {
        result.append(
            (new CalendarHtmlView()).getHtmlView(DateUtil.parse(today), agendaSessionController));
      } catch (Exception e) {
      }

      result.append("             </td></tr>\n");
      result.append("           </table></td></tr>\n");
      result.append("         </table>\n");
      result.append("     </td></tr>\n");
    }
    result.append("    </table>\n");
    result.append(" </td></tr>\n");
    result.append("</table>\n");
    // end calendar
    result.append("</td>");
    result.append("</tr>\n");
    result.append("</table>");
    return result.toString();

  }

  /**
   * Method declaration
   *
   * @param firstDay
   * @return
   *
   */
  public String getHtmlViewByWeek(String firstDay) throws AgendaException {

    StringBuilder result = new StringBuilder();
    SchedulableList[] dayList = new SchedulableList[WEEKDAYNUMBER];

    result
        .append(
            "<table border=\"0\" width=\"98%\" cellspacing=\"0\" cellpadding=\"2\" class=\"grille\">\n");
    result.append("<tr>");
    result.append("<td>");
    result
        .append(
            "<table border=\"0\" align=\"center\" width=\"100%\" cellspacing=\"1\" cellpadding=\"1\">\n");
    result.append("  <tr bgcolor=\"#ffffff\">\n");
    result
        .append(
            "    <td rowspan=\"2\" align=\"right\" valign=\"bottom\"> <img src=\"icons/1px.gif\" alt=\"\"/><br>");
    result.append("</td>\n");
    Calendar day = Calendar.getInstance();

    try {
      day.setTime(DateUtil.parse(firstDay));
    } catch (java.text.ParseException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return "";
    }

    for (int i = 0; i < WEEKDAYNUMBER; i++) {
      if (agendaSessionController.isHolidayDate(day.getTime())) {
        result
            .append("<td class=\"intfdcolor4\" valign=\"bottom\" width=\"14%\" align=\"center\">");
        result.append("<span ");
        result.append(weekDayOffStyle).append(">").append(
            agendaSessionController.getString("jour" + day.get(Calendar.DAY_OF_WEEK)).substring(0,
                3));
        result.append(" ").append(day.get(Calendar.DAY_OF_MONTH)).append("</span>");
      } else {
        result
            .append(
                "    <td class=\"intfdcolor2\" valign=\"bottom\" width=\"14%\" align=\"center\">");
        result.append("<a href=\"javascript:onClick=selectDay('").append(
            DateUtil.getInputDate(day.getTime(), agendaSessionController.getLanguage())).append(
                "')\" class=\"txtnav\">");
        result.append(agendaSessionController.getString(
            "jour" + day.get(Calendar.DAY_OF_WEEK)).substring(0, 3));
        result.append(" ").append(day.get(Calendar.DAY_OF_MONTH));
        result.append("</a>");
        dayList[i] = new SchedulableList(DateUtil.date2SQLDate(day.getTime()),
            schedules);
      }
      result.append("</td>\n");
      day.add(Calendar.DATE, 1);
    }
    result.append("  </tr>\n");

    result.append("  <tr>");

    for (int j = 0; j < WEEKDAYNUMBER; j++) {
      result.append("    <td class=\"intfdcolor4\">");
      List<Schedulable> all = new ArrayList<Schedulable>();
      if (dayList[j] != null) {
        all = dayList[j].getWithoutHourSchedules();
      }
      if (all.size() > 0) {
        result.append("      <table>");
        for (Schedulable schedule : all) {
          result.append("  <tr>");
          if (isOtherAgenda && schedule.getClassification().isPrivate()) {
            result.append("    <td class=\"privateEvent\">").append(
                agendaSessionController.getString("privateEvent"));
          } else {
            if (isOtherAgenda) {
              result.append("    <td class=\"publicEvent\">");
            } else {
              result.append("    <td>");
            }

            result.append("      <a href=\"javascript:onClick=viewJournal('").append(
                schedule.getId()).append("')\"");
            result.append(getInfoBulle(schedule));
            result.append(WebEncodeHelper.javaStringToHtmlString(schedule.getName()));
            result.append("      </a>");
          }
          result.append("  </td></tr>\n");
        }
        result.append("      </table>");
      } else {
        result.append("&nbsp;");
      }
      result.append("    </td>");
    }
    result.append("  </tr>\n");

    result.append("<tr> ");
    result
        .append(
            "<td colspan=\"8\" class=\"intfdcolor3\"><img src=\"icons/1px.gif\" height=\"2\" width=\"1\" alt=\"\"/></td>");
    result.append("</tr>\n");

    for (int i = BEGINHOUR; i < ENDHOUR; i++) {
      result.append(" <tr>");
      result.append("<td align=\"right\" bgcolor=\"#FFFFFF\" nowrap=\"nowrap\" valign=\"top\">");
      result.append("<span class=\"intfdcolor4\">");
      result.append(String.valueOf(i)).append("H");
      result.append("</span></td>");
      // }
      String hour = Schedulable.quarterCountToHourString(i * 4);
      String nextHour = Schedulable.quarterCountToHourString(i * 4 + 4);

      for (int j = 0; j < WEEKDAYNUMBER; j++) {
        Vector starting = new Vector();
        if (dayList[j] != null) {
          starting = dayList[j].getStartingSchedules(hour, nextHour);
        }

        if (starting.isEmpty()) {
          if (dayList[j] != null) {
            Vector goOn = dayList[j].getGoOnSchedules(hour, nextHour);
            if (goOn.isEmpty()) {
              result.append("<td class=\"intfdcolor4\">&nbsp;</td>");
            }
          } else {
            result.append("<td class=\"intfdcolor51\">&nbsp;</td>");
          }
        } else {
          String color = "intfdcolor2";
          int maxRowSpan = 0;
          StringBuilder tmpResult = new StringBuilder();

          for (int m = 0; m < starting.size(); m++) {
            Object startObj = starting.elementAt(m);

            if (startObj instanceof Schedulable) {
              Schedulable schedule = (Schedulable) startObj;
              int rowSpan = getDuration(schedule);

              if (rowSpan > maxRowSpan) {
                maxRowSpan = rowSpan;
              }
              if (isOtherAgenda && schedule.getClassification().isPrivate()) {
                if (starting.size() == 1) {
                  tmpResult.append(schedule.getStartHour()).append("<br>");
                }
                color = "privateEvent";
              } else {
                if (isOtherAgenda) {
                  color = "publicEvent";
                }
                tmpResult.append("<a href=\"javascript:onClick=viewJournal('").append(
                    schedule.getId()).append("')\"");
                tmpResult.append(getInfoBulle(schedule));
                if (starting.size() == 1) {
                  tmpResult.append(schedule.getStartHour()).append("<br>");
                }
                tmpResult.append(WebEncodeHelper.javaStringToHtmlString(schedule.getName())).append(
                    "</a>");
              }

            } else if (startObj instanceof SchedulableGroup) {
              SchedulableGroup group = (SchedulableGroup) startObj;
              color = "intfdcolor2";
              int rowSpan = getDuration(group);
              if (rowSpan > maxRowSpan) {
                maxRowSpan = rowSpan;
              }
              for (int k = 0; k < group.getContent().size(); k++) {
                Schedulable schedule = (Schedulable) group.getContent().elementAt(k);
                if (isOtherAgenda && schedule.getClassification().isPrivate()) {
                  color = "privateEvent";
                } else {
                  if (isOtherAgenda) {
                    color = "publicEvent";
                  }

                  tmpResult.append("<a href=\"javascript:onClick=viewJournal('").append(
                      schedule.getId()).append("')\"");
                  tmpResult.append(getInfoBulle(schedule));
                  tmpResult.append(WebEncodeHelper.javaStringToHtmlString(schedule.getName())).append(
                      "</a>\n");
                }

                if (k + 1 < group.getContent().size()) {
                  tmpResult.append("<br>");
                }
              }
            }

            if (m + 1 < starting.size()) {
              tmpResult.append("<br>");
            }
          }
          maxRowSpan = ((maxRowSpan + 3) >> 2);
          String nexts = Schedulable.quarterCountToHourString((i + maxRowSpan - 1) * 4);
          String nexte = Schedulable.quarterCountToHourString((i + maxRowSpan) * 4);
          Vector nextStarting = dayList[j].getStartingSchedules(nexts, nexte);

          if ((nextStarting.size() > 0) && (maxRowSpan > 1)) {
            maxRowSpan--;
          }
          result.append("<td class=\"").append(color).append("\" rowspan=\"").append(maxRowSpan)
              .append("\">");
          result.append(tmpResult);
          result.append("</td>");
        }
      }
      result.append("  </tr>\n");
    }

    // redisplay the date
    result.append("  <tr bgcolor=\"#ffffff\">\n");
    result
        .append(
            "    <td rowspan=\"2\" align=\"right\" valign=\"bottom\"> <img src=\"icons/1px.gif\" alt=\"\"/><br>");
    result.append("</td>\n");

    try {
      day.setTime(DateUtil.parse(firstDay));
    } catch (java.text.ParseException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return "";
    }

    for (int i = 0; i < WEEKDAYNUMBER; i++) {
      result.append("    <td valign=\"bottom\" width=\"14%\" align=\"center\">");
      if (agendaSessionController.isHolidayDate(day.getTime())) {
        result.append("<span ");
        result.append(dayOffStyle).append(">").append(
            agendaSessionController.getString("jour" + day.get(Calendar.DAY_OF_WEEK)).substring(0,
                3));
        result.append(" ").append(day.get(Calendar.DAY_OF_MONTH));
        result.append("</span>");
      } else {
        result.append("<a href=\"javascript:onClick=selectDay('").append(
            DateUtil.getInputDate(day.getTime(), agendaSessionController.getLanguage())).append(
                "')\">");
        result.append(agendaSessionController.getString(
            "jour" + day.get(Calendar.DAY_OF_WEEK)).substring(0, 3));
        result.append(" ").append(day.get(Calendar.DAY_OF_MONTH));
        result.append("</a>");
        dayList[i] = new SchedulableList(DateUtil.date2SQLDate(day.getTime()),
            schedules);
      }
      result.append("</td>\n");
      day.add(Calendar.DATE, 1);
    }
    result.append("  </tr>\n");
    result.append("</table>");
    result.append("</td>");
    result.append("</tr>\n");
    result.append("</table>");
    return result.toString();
  }

  /**
   * Method declaration
   *
   * @param schedule
   * @return
   *
   */
  private int getDuration(Schedulable schedule) {
    try {
      String sHour = schedule.getStartHour().substring(0, 3) + "00";
      long startTime = DateUtil.parseDateTime(schedule.getStartDay() + " " + sHour).getTime();
      String eHour = schedule.getEndHour();
      if (!eHour.substring(3, 5).equals("00")) {
        eHour = eHour.substring(0, 3) + "45";
      }
      long endTime = DateUtil.parseDateTime(schedule.getEndDay() + " " + eHour).getTime();
      long ms = endTime - startTime;
      return (int) (ms / (60000 * 15));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return 0;
    }
  }

  /**
   * Method declaration
   *
   * @param group
   * @return
   *
   */
  private int getDuration(SchedulableGroup group) {
    try {
      String sHour = group.getStartHour().substring(0, 3) + "00";
      long startTime = DateUtil.parseTime(sHour).getTime();
      String eHour = group.getEndHour();
      if (!eHour.substring(3, 5).equals("00")) {
        eHour = eHour.substring(0, 3) + "45";
      }
      long endTime = DateUtil.parseTime(eHour).getTime();
      long ms = endTime - startTime;
      return (int) (ms / (60000 * 15));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      return 0;
    }
  }

  /**
   * Get infobulle detail of the event
   *
   * @param schedule
   * @return
   * @throws AgendaException
   */
  private String getInfoBulle(Schedulable schedule) throws AgendaException {
    Collection<Category> categories = agendaSessionController.getJournalCategories(schedule.getId());
    if (!StringUtil.isDefined(schedule.getDescription())
        && categories.isEmpty()) {
      return ">";
    }

    StringBuilder cat = new StringBuilder();
    for (Category category : categories) {
      cat.append(category.getName()).append("&nbsp;");
    }

    StringBuilder result = new StringBuilder("onmouseover=\"return overlib('");
    result.append(WebEncodeHelper.javaStringToJsString(WebEncodeHelper
        .javaStringToHtmlParagraphe(WebEncodeHelper.javaStringToHtmlString(schedule.getDescription()))));
    result.append("',CAPTION,'").append(WebEncodeHelper.javaStringToJsString(cat.toString())).append(
        "');\" onmouseout=\"return nd();\">");
    return result.toString();
  }
}

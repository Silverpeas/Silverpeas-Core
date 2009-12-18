/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

package com.stratelia.webactiv.agenda.view;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.agenda.control.AgendaException;
import com.stratelia.webactiv.agenda.control.AgendaRuntimeException;
import com.stratelia.webactiv.agenda.control.AgendaSessionController;
import com.stratelia.webactiv.calendar.model.Category;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.calendar.model.Schedulable;
import com.stratelia.webactiv.calendar.model.SchedulableCount;
import com.stratelia.webactiv.calendar.model.SchedulableGroup;
import com.stratelia.webactiv.calendar.model.SchedulableList;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.viewGenerator.html.Encode;

/*
 * CVS Informations
 *
 * $Id: AgendaHtmlView.java,v 1.7 2009/01/09 14:45:27 neysseri Exp $
 *
 * $Log: AgendaHtmlView.java,v $
 * Revision 1.7  2009/01/09 14:45:27  neysseri
 * Modifications des CSS utilisées pour look V5 + refonte page ajout/modification d'événement
 *
 * Revision 1.6  2008/07/11 11:29:49  dlesimple
 * Correction bug visu évènement public journée complète d'un agenda tiers: on ne pouvait pas voir le nom et desc
 *
 * Revision 1.5  2008/04/16 07:23:19  neysseri
 * no message
 *
 * Revision 1.4.6.6  2008/04/08 15:14:45  dlesimple
 * Correction look jours ouvrés
 *
 * Revision 1.4.6.5  2008/03/26 16:39:17  dlesimple
 * Gestion visibilité jours non ouvrés
 *
 * Revision 1.4.6.4  2008/03/25 15:21:37  dlesimple
 * Gestion des jours non ouvrés
 *
 * Revision 1.4.6.3  2008/03/19 16:08:14  dlesimple
 * Gestion Jours ouvrés
 *
 * Revision 1.4.6.2  2008/03/06 15:13:36  dlesimple
 * Import export iCal
 *
 * Revision 1.4.6.1  2008/02/29 16:28:11  dlesimple
 * Infobulle evénèment (rubrique + description)
 *
 * Revision 1.4  2006/02/23 18:28:07  dlesimple
 * Agenda partagé
 *
 * Revision 1.3  2005/09/30 14:15:59  neysseri
 * Centralisation de la gestion des dates
 *
 * Revision 1.2  2004/12/22 15:18:31  neysseri
 * Possibilité d'indiquer les jours non sélectionnables
 * + nettoyage sources
 * + précompilation jsp
 *
 * Revision 1.1.1.1  2002/08/06 14:47:40  nchaix
 * no message
 *
 * Revision 1.6  2002/05/29 09:22:16  groccia
 * portage netscape
 *
 * Revision 1.5.12.1  2002/05/07 15:12:19  fsauvand
 * no message
 *
 * Revision 1.5  2002/01/18 15:43:18  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 *
 */

/**
 * Class declaration
 * @author
 */
public class AgendaHtmlView {
  public static final int BYDAY = 1;
  public static final int BYWEEK = 2;
  public static final int BYMONTH = 3;
  public static final int BYYEAR = 4;
  public static final int CHOOSE_DAYS = 5;

  private int WEEKDAYNUMBER = 7;
  private int BEGINHOUR = 8;
  private int ENDHOUR = 18;

  private static final java.text.SimpleDateFormat completeFormat = new java.text.SimpleDateFormat(
      "yyyy/MM/dd HH:mm");
  private static final java.text.SimpleDateFormat hourFormat = new java.text.SimpleDateFormat(
      "HH:mm");

  private String startDate;

  private Vector schedules = new Vector();
  private CalendarHtmlView calendarHtmlView = null;

  private int viewType = 0;
  private AgendaSessionController agendaSessionController;

  private boolean calendarVisible = true;

  private boolean isOtherAgenda = false;

  private String dayOffStyle = "class=\"txtdayoff1\"";
  private String weekDayOffStyle = "class=\"txtdayoff2\"";

  /**
   * Constructor declaration
   * @param viewType
   * @see
   */
  public AgendaHtmlView(int viewType) {
    this.viewType = viewType;
    setDate(null);
  }

  /**
   * Constructor declaration
   * @param viewType
   * @param date
   * @param message
   * @param settings
   * @see
   */
  public AgendaHtmlView(int viewType, Date date,
      AgendaSessionController agendaSessionController, ResourceLocator settings) {
    this.viewType = viewType;
    setDate(date);
    this.agendaSessionController = agendaSessionController;
    isOtherAgenda = agendaSessionController.isOtherAgendaMode();
    WEEKDAYNUMBER = new Integer(settings.getString("weekDayNumber")).intValue();
    BEGINHOUR = new Integer(settings.getString("beginHour")).intValue();
    ENDHOUR = new Integer(settings.getString("endHour")).intValue();

  }

  /**
   * Method declaration
   * @param on
   * @see
   */
  public void setCalendarVisible(boolean on) {
    this.calendarVisible = on;
  }

  /**
   * Method declaration
   * @param date
   * @see
   */
  public void setDate(Date date) {
    if (date == null) {
      date = new java.util.Date();

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
   * @param scheduleCount
   * @see
   */
  public void add(SchedulableCount scheduleCount) {
    if (calendarHtmlView == null) {
      calendarHtmlView = new CalendarHtmlView();
    }
    calendarHtmlView.add(scheduleCount);
  }

  /**
   * Method declaration
   * @param schedule
   * @see
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
      SilverTrace.warn("agenda", "AgendaHtmView.add(Schedulable schedule)",
          "agenda.MSG_ADD_SCHEDULE_FAILED", "id=" + schedule.getId()
          + ", name=" + schedule.getName(), e);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
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
   * @param startDate
   * @return
   * @see
   */
  public String getHtmlViewByMonth(String startDate) {
    SilverTrace.debug("agenda",
        "AgendaHtmView.getHtmlViewByMonth(String startDate)", "schedules"
        + schedules.size());

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
      String result = "";

      result += "      <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"98%\">\n";
      result += "        <tr> \n";
      result += "          <td> \n";
      result +=
          "            <table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\">\n";
      result += "              <tr> \n";
      result += "                <td class=\"grille\"> \n";
      result +=
          "                  <table border=\"0\" cellpadding=\"0\" cellspacing=\"1\" class=\"intfdcolor\" width=\"100%\">\n";
      result += "                    <tr> \n";
      result += "                      <td align=center class=\"grille\"> ";

      result += calendarHtmlView.getHtmlView(DateUtil.parse(startDate),
          agendaSessionController);

      result += "                      </td>";
      result += "                    </tr>";
      result += "                  </table>";
      result += "                </td>";
      result += "              </tr>";
      result += "            </table>";
      result += "          </td>";
      result += "        </tr>";
      result += "      </table>";

      return result;
    } catch (java.text.ParseException e) {

      SilverTrace.warn("agenda",
          "AgendaHtmView.getHtmlViewByMonth(String startDate)",
          "agenda.MSG_CANT_GET_VIEW_MONTH", "return= null", e);
      return "";
    }
  }

  /**
   * Method declaration
   * @param startDate
   * @return
   * @see
   */
  public String getHtmlViewByYear(String startDate) {
    CalendarHtmlView calendarHtmlView = new CalendarHtmlView();

    calendarHtmlView.setNavigationBar(false);
    calendarHtmlView.setWeekDayStyle("class=\"txtnav\"");
    calendarHtmlView.setMonthDayStyle("class=\"intfdcolor4\"");
    calendarHtmlView.setMonthSelectedDayStyle("class=\"intfdcolor6\"");
    String result = "";

    result += "      <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"98%\">\n";
    result += "        <tr> \n";
    result += "          <td> \n";

    result +=
        "            <table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\">\n";
    result += "              <tr> \n";
    result += "                <td class=\"grille\"> \n";
    result +=
        "                  <table border=\"0\" cellpadding=\"1\" cellspacing=\"1\" width=\"100%\">\n";
    String year = startDate.substring(0, 4);
    int month = 1;

    for (int i = 0; i < 3; i++) {
      result += "<TR>";
      for (int j = 0; j < 4; j++) {
        result += "<TD bgcolor=\"#ffffff\" ALIGN=LEFT VALIGN=TOP>";
        try {
          String m = String.valueOf(month);

          if (m.length() == 1) {
            m = "0" + m;
          }
          result += calendarHtmlView.getHtmlView(DateUtil.parse(year + "/" + m
              + "/01"), agendaSessionController);
          month++;
        } catch (java.text.ParseException e) {
          SilverTrace.warn("agenda",
              "AgendaHtmView.getHtmlViewByMonth(String startDate)",
              "agenda.MSG_CANT_GET_VIEW_YEAR", "return= null", e);
          return "";
        }
        result += "</TD>\n";
      }
      result += "</TR>";
    }
    result += "                  </table>";
    result += "                </td>";
    result += "              </tr>";
    result += "            </table>";
    result += "          </td>";
    result += "        </tr>";
    result += "      </table>";
    return result;
  }

  /**
   * Method declaration
   * @param today
   * @return
   * @see
   */
  public String getHtmlViewByDay(String today) throws AgendaException {

    String result = "";
    Calendar day = Calendar.getInstance();

    try {
      day.setTime(DateUtil.parse(today));
    } catch (java.text.ParseException e) {
      SilverTrace.warn("agenda",
          "AgendaHtmView.getHtmlViewByDay(String today)",
          "agenda.MSG_CANT_GET_VIEW_DAY", "return= null", e);
      return "";
    }
    SchedulableList dayList = new SchedulableList(DateUtil.date2SQLDate(day
        .getTime()), schedules);

    result +=
        "<TABLE border=\"0\" align=\"center\" width=\"98%\" cellspacing=\"2\" cellpadding=\"0\" class=\"grille\">\n";
    result += "<TR valign=\"top\">";
    result += "<TD>";
    result +=
        "<TABLE border=\"0\" align=\"center\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n";
    result += "  <TR nowrap>\n";
    result += "    <TD width=\"100%\">";
    Vector all = dayList.getWithoutHourSchedules();

    result +=
        "      <TABLE class=\"grille\" border=\"0\" align=\"center\" width=\"100%\" cellspacing=\"1\" cellpadding=\"1\">";

    if (all.size() > 0) {

      for (int i = 0; i < all.size(); i++) {
        Schedulable schedule = (Schedulable) all.elementAt(i);
        result += "  <TR><span class=\"txtnote\">";
        if (i == 0) {
          result += "    <TD width=\"50\" rowspan=\"" + all.size()
              + "\" align=\"right\" bgcolor=\"#FFFFFF\" nowrap valign=\"top\">";
          result += "&nbsp;</TD>";
        }

        if (isOtherAgenda) {
          if (schedule.getClassification().isPrivate())
            result += "    <TD class=\"privateEvent\" width=\"600\">";
          else
            result += "    <TD class=\"publicEvent\" width=\"600\">";
        } else
          result += "    <TD class=\"intfdcolor4\" width=\"600\">";

        if (schedule.getClassification().isPublic() || !isOtherAgenda) {
          result += "      <A HREF=\"" + "javascript:onClick=viewJournal('"
              + schedule.getId() + "')\"";
          result += getInfoBulle(schedule);
          result += Encode.javaStringToHtmlString(schedule.getName()) + "</A>";
        }

        result += " &nbsp;(" + agendaSessionController.getString("allDay")
            + ")    </TD>";
        result += "  </span></TR>\n";
      }
    } else {
      result += "  <TR>";
      result += "    <TD width=\"50\" align=\"right\" bgcolor=\"#FFFFFF\" nowrap valign=\"top\">";
      result += "&nbsp;</TD>";
      result += "    <TD class=\"intfdcolor4\" width=\"600\">";
      result += "&nbsp;";
      result += "    </TD>";
      result += "  </TR>\n";
    }
    result += "      </TABLE>\n";
    result += "    </TD>";
    result += "  </TR>\n";

    result += "<tr> ";
    result += "<td class=\"intfdcolor3\"><img src=\"icons/1px.gif\" height=\"2\" width=\"1\"></td>";
    result += "</tr>\n";

    int i = BEGINHOUR;
    int maxColumns = 0;
    Vector lastGoOn = null;
    Vector goOn;

    while (i < ENDHOUR) {
      String hour = Schedulable.quaterCountToHourString(i * 4);
      String nextHour = Schedulable.quaterCountToHourString(i * 4 + 4);
      Schedulable thisHour = new JournalHeader("", "");

      thisHour.setStartDate(day.getTime());
      try {
        thisHour.setStartHour(hour);
        thisHour.setEndHour(nextHour);
      } catch (Exception e) {
        throw new AgendaRuntimeException(
            "AgendaHtmView.getHtmlViewByDay(String today)",
            SilverpeasException.ERROR, "agenda.EX_CANT_GET_VIEW_DAY", e);
      }

      dayList.getStartingSchedules(hour, nextHour);
      goOn = new Vector();
      for (int dayListIterator = 0; dayListIterator < schedules.size(); dayListIterator++) {
        Schedulable sched = (Schedulable) schedules.elementAt(dayListIterator);

        if (sched.isOver(thisHour)) {
          goOn.add(sched);
        }
      }

      if ((goOn.size() != 0) && (maxColumns == 0)) {
        // compute the number of columns to display
        maxColumns = 1;
        int maxTime = 0;
        Schedulable tmpThisHour = new JournalHeader("", "");

        tmpThisHour.setStartDate(day.getTime());
        int countColumns;

        do {
          String tmpHour = Schedulable
              .quaterCountToHourString((i + maxTime) * 4);
          String tmpNextHour = Schedulable
              .quaterCountToHourString((i + maxTime + 1) * 4);

          try {
            tmpThisHour.setStartHour(tmpHour);
            tmpThisHour.setEndHour(tmpNextHour);
          } catch (Exception e) {
            throw new AgendaRuntimeException(
                "AgendaHtmView.getHtmlViewByDay(String today)",
                SilverpeasException.ERROR, "agenda.EX_CANT_GET_VIEW_DAY", e);
          }
          countColumns = 0;
          for (int iterator = 0; iterator < schedules.size(); iterator++) {
            Schedulable sched = (Schedulable) schedules.elementAt(iterator);

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
        result += "  <TR>";
        result += "    <TD>";
        result +=
            "      <TABLE class=\"grille\" border=\"0\" align=\"center\" width=\"100%\" cellspacing=\"1\" cellpadding=\"1\">\n";
      } else {
        if (((goOn.size() != 0) && (lastGoOn.size() == 0))
            || ((goOn.size() == 0) && (lastGoOn.size() != 0))) {
          result += "      </TABLE>\n";
          result += "    </TD>";
          result += "  </TR>\n";
          result += "  <TR>";
          result += "    <TD>";
          result +=
              "      <TABLE class=\"grille\" border=\"0\" align=\"center\" width=\"100%\" cellspacing=\"1\" cellpadding=\"1\">\n";
        }
      }

      result += "       <TR>";
      result +=
          "        <TD width=\"50\" align=\"right\" bgcolor=\"#FFFFFF\" nowrap valign=\"top\">";
      result += "<A HREF=\"javascript:onClick=selectHour('" + i + "')\">";
      result += String.valueOf(i) + "H</A>";
      result += "        </TD>";

      if (goOn.size() == 0) {
        result += "        <TD class=\"intfdcolor4\" width=\"600\">&nbsp;</TD>";
        maxColumns = 0;
      } else {
        for (int goOnIterator = 0; goOnIterator < goOn.size(); goOnIterator++) {
          Schedulable schedule = (Schedulable) goOn.elementAt(goOnIterator);
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
              String tmpHour = Schedulable
                  .quaterCountToHourString((i + length) * 4);
              String tmpNextHour = Schedulable.quaterCountToHourString((i
                  + length + 1) * 4);

              try {
                tmpThisHour.setStartHour(tmpHour);
                tmpThisHour.setEndHour(tmpNextHour);
              } catch (Exception e) {
                throw new AgendaRuntimeException(
                    "AgendaHtmView.getHtmlViewByDay(String today)",
                    SilverpeasException.ERROR, "agenda.EX_CANT_GET_VIEW_DAY", e);
              }
            } while (tmpThisHour.isOver(schedule));

            String color = "intfdcolor2";
            for (int iterator = 0; iterator < schedules.size(); iterator++) {
              Schedulable sched = (Schedulable) schedules.elementAt(iterator);

              if (!sched.getId().equals(schedule.getId())) {
                if (sched.isOver(schedule)) {
                  color = "intfdcolor2";
                }
              }
            }
            if (isOtherAgenda) {
              color = "publicEvent";
              if (schedule.getClassification().isPrivate())
                color = "privateEvent";
            }

            result += "<TD width=\"" + ((int) (600 / maxColumns))
                + "\" class=\"" + color + "\" rowspan=\"" + length + "\">";
            if (isOtherAgenda) {
              if (schedule.getClassification().isPrivate()) {
                if (!schedule.getEndHour().equals(schedule.getStartHour())) {
                  result += schedule.getStartHour() + " - "
                      + schedule.getEndHour() + "<BR>";
                } else
                  result += schedule.getStartHour() + "<BR>";
              } else {
                result += "      <A HREF=\"javascript:onClick=viewJournal('"
                    + schedule.getId() + "')\"";
                result += getInfoBulle(schedule);
                if (!schedule.getEndHour().equals(schedule.getStartHour())) {
                  result += schedule.getStartHour() + " - "
                      + schedule.getEndHour() + "<BR>";
                } else
                  result += schedule.getStartHour() + "<BR>";
                result += Encode.javaStringToHtmlString(schedule.getName())
                    + "</A>";
              }
            } else {
              result += "      <A HREF=\"javascript:onClick=viewJournal('"
                  + schedule.getId() + "')\"";
              result += getInfoBulle(schedule);
              if (!schedule.getEndHour().equals(schedule.getStartHour())) {
                result += schedule.getStartHour() + " - "
                    + schedule.getEndHour() + "<BR>";
              } else
                result += schedule.getStartHour() + "<BR>";
              result += Encode.javaStringToHtmlString(schedule.getName())
                  + "</A>";
            }
            result += "</TD>";
          }
        }
        for (int maxColumnsIterator = goOn.size(); maxColumnsIterator < maxColumns; maxColumnsIterator++) {
          result += "        <TD class=\"intfdcolor4\" width=\""
              + ((int) (600 / maxColumns)) + "\">&nbsp;</TD>";
        }
      }

      result += "       </TR>\n";

      lastGoOn = goOn;
      i++;

    }
    result += " </table></td></tr>\n";
    result += "</TABLE>\n";
    result += "</TD>";
    if (calendarVisible) {
      result += "<TD width=\"100\" class=\"intfdcolor2\">\n";
    } else {
      result += "<TD width=\"10\" class=\"intfdcolor2\">\n";
    }

    // display the calendar
    result += "<!-- [ CALENDRIER ] --> \n";
    result += " <table width=\"100%\" border=\"0\" cellpadding=\"1\" cellspacing=\"0\">\n";
    result += " <tr> \n";
    result += "  <td>\n";
    result += "   <table class=\"intfdcolor2\" border=\"0\" cellpadding=\"1\" cellspacing=\"0\">";
    result += "    <tr> ";
    result += "     <td align=\"right\" class=\"intfdcolor2\">";
    if (!calendarVisible) {
      result +=
          "        <a href=\"javascript:onClick=openCalendar()\"><img src=\"icons/cal_open.gif\" width=\"16\" height=\"14\" border=\"0\" alt=\"Afficher le calendrier\" title=\"Afficher le calendrier\"></a> \n";
    } else {
      result +=
          "        <a href=\"javascript:onClick=closeCalendar()\"><img src=\"icons/croix3.gif\" width=\"16\" height=\"14\" border=\"0\" alt=\"Fermer le calendrier\" title=\"Fermer le calendrier\"></a> \n";
    }
    result += "     </td>";
    result += "    </tr> ";
    if (calendarVisible) {
      result += "    <tr> ";
      result += "     <td>";
      result +=
          "       <table border=\"0\" cellpadding=\"0\" cellspacing=\"1\" class=\"intfdcolor\"> \n";
      result += "        <tr><td><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n";
      result += "         <tr>\n";
      result += "            <td align=center class=\"txtbigdate\">"
          + day.get(Calendar.DAY_OF_MONTH);
      result += "            </td></tr>";
      result += "            <tr><td align=center class=\"txtnav3\">"
          + agendaSessionController.getString("mois" + day.get(Calendar.MONTH));
      result += "            </td></tr>";
      result += "            <tr><td align=center class=\"txtnav3\">"
          + DateUtil.getInputDate(day.getTime(), agendaSessionController
          .getLanguage());
      result += "            </TD></TR>\n";
      result += "            <TR><TD>\n";

      try {
        result += (new CalendarHtmlView()).getHtmlView(DateUtil.parse(today),
            agendaSessionController);
      } catch (Exception e) {
      }

      result += "             </TD></TR>\n";
      result += "           </table></td></tr>\n";
      result += "         </table>\n";
      result += "     </TD></TR>\n";
    }
    result += "    </table>\n";
    result += " </TD></TR>\n";
    result += "</table>\n";
    // end calendar
    result += "</TD>";
    result += "</TR>\n";
    result += "</TABLE>";
    return result;

  }

  /**
   * Method declaration
   * @param firstDay
   * @return
   * @see
   */
  public String getHtmlViewByWeek(String firstDay) throws AgendaException {

    StringBuffer result = new StringBuffer();
    SchedulableList[] dayList = new SchedulableList[WEEKDAYNUMBER];

    result
        .append("<TABLE border=\"0\" align=\"center\" width=\"98%\" cellspacing=\"0\" cellpadding=\"2\" class=\"grille\">\n");
    result.append("<TR>");
    result.append("<TD>");
    result
        .append("<TABLE border=\"0\" align=\"center\" width=\"100%\" cellspacing=\"1\" cellpadding=\"1\">\n");
    result.append("  <TR bgcolor=\"#ffffff\" nowrap>\n");
    result
        .append("    <td rowspan=\"2\" align=\"right\" valign=\"bottom\"> <img src=\"icons/1px.gif\"><br>");
    result.append("</TD>\n");
    Calendar day = Calendar.getInstance();

    try {
      day.setTime(DateUtil.parse(firstDay));
    } catch (java.text.ParseException e) {
      SilverTrace.warn("agenda",
          "AgendaHtmView.getHtmlViewByWeek(String firstDay)",
          "agenda.MSG_CANT_GET_VIEW_WEKK", "return= null", e);
      return "";
    }

    for (int i = 0; i < WEEKDAYNUMBER; i++) {
      if (agendaSessionController.isHolidayDate(day.getTime())) {
        result
            .append("<TD class=\"intfdcolor4\" valign=\"bottom\" width=\"14%\" align=\"center\">");
        result.append("<span ");
        result.append(weekDayOffStyle
            + ">"
            + agendaSessionController.getString(
            "jour" + day.get(Calendar.DAY_OF_WEEK)).substring(0, 3));
        result.append(" " + day.get(Calendar.DAY_OF_MONTH) + "</span>");
      } else {
        result
            .append("    <TD class=\"intfdcolor2\" valign=\"bottom\" width=\"14%\" align=\"center\">");
        result.append("<A HREF=\"javascript:onClick=selectDay('"
            + DateUtil.getInputDate(day.getTime(), agendaSessionController
            .getLanguage()) + "')\" class=\"txtnav\">");
        result.append(agendaSessionController.getString(
            "jour" + day.get(Calendar.DAY_OF_WEEK)).substring(0, 3));
        result.append(" " + day.get(Calendar.DAY_OF_MONTH));
        result.append("</A>");
        dayList[i] = new SchedulableList(DateUtil.date2SQLDate(day.getTime()),
            schedules);
      }
      result.append("</TD>\n");
      day.add(Calendar.DATE, 1);
    }
    result.append("  </TR>\n");

    result.append("  <TR>");

    for (int j = 0; j < WEEKDAYNUMBER; j++) {
      result.append("    <TD class=\"intfdcolor4\"><span class=\"txtnote\">");
      Vector all = new Vector();
      if (dayList[j] != null)
        all = dayList[j].getWithoutHourSchedules();

      if (all.size() > 0) {
        result.append("      <TABLE>");
        for (int i = 0; i < all.size(); i++) {
          Schedulable schedule = (Schedulable) all.elementAt(i);

          result.append("  <TR>");
          if (isOtherAgenda && schedule.getClassification().isPrivate()) {
            result.append("    <TD class=privateEvent>"
                + agendaSessionController.getString("privateEvent"));
          } else {
            if (isOtherAgenda)
              result.append("    <TD class=publicEvent>");
            else
              result.append("    <TD>");

            result.append("      <A HREF=\"javascript:onClick=viewJournal('"
                + schedule.getId() + "')\"");
            result.append(getInfoBulle(schedule));
            result.append(Encode.javaStringToHtmlString(schedule.getName()));
            result.append("      </A>");
          }
          result.append("  </TD></TR>\n");
        }
        result.append("      </TABLE>");
      } else {
        result.append("&nbsp;");
      }
      result.append("    </span></TD>");
    }
    result.append("  </TR>\n");

    result.append("<tr> ");
    result
        .append("<td colspan=\"8\" class=\"intfdcolor3\"><img src=\"icons/1px.gif\" height=\"2\" width=\"1\"></td>");
    result.append("</tr>\n");

    for (int i = BEGINHOUR; i < ENDHOUR; i++) {
      result.append(" <TR>");
      result
          .append("<TD align=\"right\" bgcolor=\"#FFFFFF\" nowrap valign=\"top\">");
      result.append("<span class=\"intfdcolor4\">");
      result.append(String.valueOf(i) + "H");
      result.append("</span></TD>");
      // }
      String hour = Schedulable.quaterCountToHourString(i * 4);
      String nextHour = Schedulable.quaterCountToHourString(i * 4 + 4);

      for (int j = 0; j < WEEKDAYNUMBER; j++) {
        Vector starting = new Vector();
        if (dayList[j] != null)
          starting = dayList[j].getStartingSchedules(hour, nextHour);

        if (starting.size() == 0) {
          if (dayList[j] != null) {
            Vector goOn = dayList[j].getGoOnSchedules(hour, nextHour);
            if (goOn.size() == 0)
              result.append("<TD class=\"intfdcolor4\">&nbsp;</TD>");
          } else
            result.append("<TD class=\"intfdcolor51\">&nbsp;</TD>");
        } else {
          String color = "intfdcolor2";
          int maxRowSpan = 0;
          StringBuffer tmpResult = new StringBuffer();

          for (int m = 0; m < starting.size(); m++) {
            Object startObj = starting.elementAt(m);

            if (startObj instanceof Schedulable) {
              Schedulable schedule = (Schedulable) startObj;
              int rowSpan = getDuration(schedule);

              if (rowSpan > maxRowSpan) {
                maxRowSpan = rowSpan;
              }
              if (isOtherAgenda && schedule.getClassification().isPrivate()) {
                if (starting.size() == 1)
                  tmpResult.append(schedule.getStartHour() + "<BR>");
                color = "privateEvent";
              } else {
                if (isOtherAgenda)
                  color = "publicEvent";
                tmpResult.append("<A HREF=\"javascript:onClick=viewJournal('"
                    + schedule.getId() + "')\"");
                tmpResult.append(getInfoBulle(schedule));
                if (starting.size() == 1)
                  tmpResult.append(schedule.getStartHour() + "<BR>");
                tmpResult.append(Encode.javaStringToHtmlString(schedule
                    .getName())
                    + "</A>");
              }

            } else if (startObj instanceof SchedulableGroup) {
              SchedulableGroup group = (SchedulableGroup) startObj;
              color = "intfdcolor2";
              int rowSpan = getDuration(group);
              if (rowSpan > maxRowSpan) {
                maxRowSpan = rowSpan;
              }
              for (int k = 0; k < group.getContent().size(); k++) {
                Schedulable schedule = (Schedulable) group.getContent()
                    .elementAt(k);
                if (isOtherAgenda && schedule.getClassification().isPrivate()) {
                  color = "privateEvent";
                } else {
                  if (isOtherAgenda)
                    color = "publicEvent";

                  tmpResult.append("<A HREF=\"javascript:onClick=viewJournal('"
                      + schedule.getId() + "')\"");
                  tmpResult.append(getInfoBulle(schedule));
                  tmpResult.append(Encode.javaStringToHtmlString(schedule
                      .getName())
                      + "</A>\n");
                }

                if (k + 1 < group.getContent().size())
                  tmpResult.append("<BR>");
              }
            }

            if (m + 1 < starting.size())
              tmpResult.append("<BR>");
          }
          maxRowSpan = ((maxRowSpan + 3) >> 2);
          String nexts = Schedulable
              .quaterCountToHourString((i + maxRowSpan - 1) * 4);
          String nexte = Schedulable
              .quaterCountToHourString((i + maxRowSpan) * 4);
          Vector nextStarting = dayList[j].getStartingSchedules(nexts, nexte);

          if ((nextStarting.size() > 0) && (maxRowSpan > 1)) {
            maxRowSpan--;
          }
          result.append("<TD class=\"" + color + "\" rowspan=\"" + maxRowSpan
              + "\">");
          result.append(tmpResult);
          result.append("</TD>");
        }
      }
      result.append("  </TR>\n");
    }

    // redisplay the date
    result.append("  <TR bgcolor=\"#ffffff\" nowrap>\n");
    result
        .append("    <td rowspan=\"2\" align=\"right\" valign=\"bottom\"> <img src=\"icons/1px.gif\"><br>");
    result.append("</TD>\n");

    try {
      day.setTime(DateUtil.parse(firstDay));
    } catch (java.text.ParseException e) {
      SilverTrace.warn("agenda",
          "AgendaHtmView.getHtmlViewByWeek(String firstDay)",
          "agenda.MSG_CANT_GET_VIEW_WEEK", "return= null", e);

      return "";
    }

    for (int i = 0; i < WEEKDAYNUMBER; i++) {
      result
          .append("    <TD valign=\"bottom\" width=\"14%\" align=\"center\">");
      if (agendaSessionController.isHolidayDate(day.getTime())) {
        result.append("<span ");
        result.append(dayOffStyle
            + ">"
            + agendaSessionController.getString(
            "jour" + day.get(Calendar.DAY_OF_WEEK)).substring(0, 3));
        result.append(" " + day.get(Calendar.DAY_OF_MONTH));
        result.append("</span>");
      } else {
        result.append("<A HREF=\"javascript:onClick=selectDay('"
            + DateUtil.getInputDate(day.getTime(), agendaSessionController
            .getLanguage()) + "')\">");
        result.append(agendaSessionController.getString(
            "jour" + day.get(Calendar.DAY_OF_WEEK)).substring(0, 3));
        result.append(" " + day.get(Calendar.DAY_OF_MONTH));
        result.append("</A>");
        dayList[i] = new SchedulableList(DateUtil.date2SQLDate(day.getTime()),
            schedules);
      }
      result.append("</TD>\n");
      day.add(Calendar.DATE, 1);
    }
    result.append("  </TR>\n");
    result.append("</TABLE>");
    result.append("</TD>");
    result.append("</TR>\n");
    result.append("</TABLE>");
    return result.toString();
  }

  /**
   * Method declaration
   * @param schedule
   * @return
   * @see
   */
  private int getDuration(Schedulable schedule) {
    try {
      String sHour = schedule.getStartHour().substring(0, 3) + "00";
      java.util.Date startDate = completeFormat.parse(schedule.getStartDay()
          + " " + sHour);

      String eHour = schedule.getEndHour();

      if (!eHour.substring(3, 5).equals("00")) {
        eHour = eHour.substring(0, 3) + "45";
      }
      java.util.Date endDate = completeFormat.parse(schedule.getEndDay() + " "
          + eHour);
      long ms = endDate.getTime() - startDate.getTime();

      return (int) (ms / (60000 * 15));
    } catch (Exception e) {
      SilverTrace
          .warn("agenda", "AgendaHtmView.getDuration(Schedulable schedule)",
          "agenda.MSG_CANT_DURATION", "id=" + schedule.getId()
          + " return=0", e);
      return 0;
    }
  }

  /**
   * Method declaration
   * @param group
   * @return
   * @see
   */
  private int getDuration(SchedulableGroup group) {
    try {
      String sHour = group.getStartHour().substring(0, 3) + "00";
      java.util.Date startDate = hourFormat.parse(sHour);

      String eHour = group.getEndHour();

      if (!eHour.substring(3, 5).equals("00")) {
        eHour = eHour.substring(0, 3) + "45";
      }
      java.util.Date endDate = hourFormat.parse(eHour);
      long ms = endDate.getTime() - startDate.getTime();

      return (int) (ms / (60000 * 15));
    } catch (Exception e) {
      SilverTrace.warn("agenda",
          "AgendaHtmView.getDuration(SchedulableGroup group)",
          "agenda.MSG_CANT_DURATION", "return=0", e);
      return 0;
    }
  }

  /**
   * Get infobulle detail of the event
   * @param schedule
   * @return
   * @throws AgendaException
   */
  private String getInfoBulle(Schedulable schedule) throws AgendaException {
    Collection categories = agendaSessionController
        .getJournalCategories(schedule.getId());
    if (!StringUtil.isDefined(schedule.getDescription())
        && categories.isEmpty())
      return ">";

    String categs = "";
    Iterator categoriesIt = categories.iterator();
    while (categoriesIt.hasNext()) {
      Category categorie = (Category) categoriesIt.next();
      categs += categorie.getName() + "&nbsp;";
    }

    StringBuffer result = new StringBuffer("onmouseover=\"return overlib('");
    result.append(Encode.javaStringToHtmlParagraphe(schedule.getDescription()));
    result.append("',CAPTION,'" + Encode.javaStringToJsString(categs)
        + "');\" onmouseout=\"return nd();\">");
    return result.toString();
  }
}
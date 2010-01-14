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
package com.stratelia.webactiv.util.viewGenerator.html.monthCalendar;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 * @author
 */
public class MonthCalendarWA1 extends AbstractMonthCalendar {

  public MonthCalendarWA1(String language) {
    super(language);
  }

  public MonthCalendarWA1(String language, int numbersDays) {
    super(language, numbersDays);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    ResourceLocator message = new ResourceLocator(
        "com.stratelia.webactiv.almanach.multilang.almanach", this.language);
    try {
      StringBuffer html = new StringBuffer();

      html
          .append("<TABLE cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"98%\" bgcolor=\"000000\"><TR><TD><TABLE cellpadding=\"0\" cellspacing=\"1\" border=\"0\" width=\"100%\">");

      html.append(printDayOfWeek());

      int k = super.getNumbersWeekOfMonth();

      SilverTrace.info("viewgenerator", "MonthCalendarWA1.print()",
          "root.MSG_GEN_PARAM_VALUE", " Numbers week = " + k + ". ");
      for (int i = 1; i <= k; i++) {
        html.append(printNumberDayOfWeek(i));
        html.append(printWeek(i, message));
      }
      html.append("</TABLE></TD></TR></TABLE>");

      return html.toString();
    } catch (Exception e) {
      return e.getMessage();
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private String printDayOfWeek() {
    String[] nameDay = super.getHeaderNameDay();
    int numbersDayOfWeek = super.getNumbersDayOfWeek();

    StringBuffer html = new StringBuffer("<tr class=\"intfdcolor51\">");

    for (int i = 0; i < numbersDayOfWeek; i++) {
      html
          .append(" <td width=\"14%\" align=\"center\"><span class=\"txtnav\">");
      html.append(nameDay[i]);
      html.append("</span></td>");
    }
    html.append("</tr>");
    return html.toString();
  }

  /**
   * Method declaration
   * @param week
   * @return
   * @throws Exception
   * @see
   */
  private String printNumberDayOfWeek(int week) throws Exception {
    StringBuffer html = new StringBuffer();
    html.append("<tr>");
    int numbersDayOfWeek = super.getNumbersDayOfWeek();
    Day[] day = super.getDayOfWeek(week);

    SilverTrace.info("viewgenerator",
        "MonthCalendarWA1.printNumberDayOfWeek()", "root.MSG_GEN_PARAM_VALUE",
        " Week = " + week + ". ");
    for (int k = 0; k < numbersDayOfWeek; k++) {
      if (day[k].getIsInThisMonth()) {
        if (day[k].isCurrentDay()) {
          html.append("<td class=\"intfdcolor52\">");
        } else {
          html.append("<td class=\"intfdcolor\">");
        }

        html.append("&nbsp;")
            .append("<a href=\"javascript: onClick=clickDay('").append(
            DateUtil.getInputDate(day[k].getDate(), super.language))
            .append("')\" class=\"almanachDay\" ").append(
            "onFocus=\"this.blur()\">").append(day[k].getNumbers()).append(
            "</a> </td>");
      } else {
        html.append("<td class=\"intfdcolor51\">&nbsp;").append(
            day[k].getNumbers()).append("</td>");
      }
    }
    html.append("</tr>");
    SilverTrace.info("viewgenerator",
        "MonthCalendarWA1.printNumberDayOfWeek()", "root.MSG_GEN_EXIT_METHOD");
    return html.toString();
  }

  /**
   * Method declaration
   * @param week
   * @return
   * @throws Exception
   * @see
   */
  private String printWeek(int week, ResourceLocator message) throws Exception {
    StringBuffer html = new StringBuffer();

    int numbersRowOfWeek = super.getNumbersOfRow(week);

    SilverTrace.info("viewgenerator", "MonthCalendarWA1.printWeek()",
        "root.MSG_GEN_PARAM_VALUE", " Week = " + (week - 1)
        + "); numbersRowOfWeek=" + numbersRowOfWeek + ". ");
    // pour chaque row de la semaine

    for (int i = 0; i < numbersRowOfWeek; i++) {
      html.append("<tr>");
      html.append(printRow(week, i, numbersRowOfWeek, message));
      html.append("</tr>");
    }
    SilverTrace.info("viewgenerator", "MonthCalendarWA1.printWeek()",
        "root.MSG_GEN_EXIT_METHOD");
    return html.toString();
  }

  /**
   * Method declaration
   * @param week
   * @param row
   * @param numbersRowOfWeek
   * @return
   * @see
   */
  private String printRow(int week, int row, int numbersRowOfWeek,
      ResourceLocator message) {
    SilverTrace.info("viewgenerator", "MonthCalendarWA1.printRow()",
        "root.MSG_GEN_PARAM_VALUE", " Week = " + week + "; numbersRowOfWeek="
        + numbersRowOfWeek + ". ");
    StringBuffer html = new StringBuffer();

    int numbersDayOfWeek = super.getNumbersDayOfWeek();
    Day[] days = super.getDayOfWeek(week);

    // récupération des événements contenu dans la "row"
    Event[] evt = super.getEventOfRow(week, row);

    String height = String.valueOf(70 / numbersRowOfWeek);

    if (evt == null) {
      for (int k = 0; k < numbersDayOfWeek; k++) {
        if (days[k].getIsInThisMonth()) {
          html.append("<td class=\"eventCells\"");
        } else {
          html.append("<td class=\"intfdcolor51\"");
        }
        html.append(" height=\"70\">&nbsp;</td>");
      }
      return html.toString();
    } else {
      SilverTrace.info("viewgenerator", "MonthCalendarWA1.printRow()",
          "root.MSG_GEN_PARAM_VALUE", " # of events = " + evt.length);

      int nbEvt = evt.length;
      Day day;
      for (int k = 0; k < numbersDayOfWeek; k++) {
        day = days[k];
        if (day.getIsInThisMonth()) {
          html.append("<td class=\"eventCells\"");
        } else {
          html.append("<td class=\"intfdcolor51\"");
        }

        html.append(" height=\"").append(height).append("\"");

        boolean tdIsCreate = false;

        // contrôle pour chaque événement, s'il débute ou pas ce jour
        // courant
        // afin d'avoir le html approprié
        for (int z = 0; z < nbEvt; z++) {
          if (evt[z].isInDay(days[k])) {
            int colspan = evt[z].getSpanDay(days[k].getDate());

            if (colspan > 1) {
              html.append(" colspan=\"" + String.valueOf(colspan) + "\">");
              k += (colspan - 1);
            } else {
              html.append(">");
            }

            String title = evt[z].getName();

            if (title.length() > 30) {
              title = title.substring(0, 30) + "...";
            }

            title = EncodeHelper.javaStringToHtmlString(title);

            if (evt[z].getColor() != null)
              title = "<span style=\"color :" + evt[z].getColor() + "\">"
                  + title + "</span>";

            if (evt[z].getPriority() == 1) {
              html.append(
                  "<img src=\"icons/urgent.gif\" align=\"absmiddle\" alt=\"")
                  .append(message.getString("important")).append("\" title=\"")
                  .append(message.getString("important")).append("\"> ");
            }
            html.append("<a href=\"javascript:onClick=clickEvent(").append(
                evt[z].getId()).append(", '");
            html.append(DateUtil.date2SQLDate(day.getDate())).append("', '");
            html.append(evt[z].getInstanceId()).append("')\"");
            if (StringUtil.isDefined(evt[z].getTooltip())) {
              html.append(" onmouseover=\"return overlib('").append(
                  EncodeHelper.javaStringToJsString(evt[z].getTooltip())).append("', CAPTION, '")
                  .append(
                  EncodeHelper.javaStringToJsString(evt[z].getName())).append(
                  "');\" onmouseout=\"return nd();\">");
            } else {
              html.append(" title=\"")
                  .append(EncodeHelper.javaStringToHtmlString(evt[z].getName()))
                  .append("\">");
            }
            html.append(title).append("</a>");
            if (evt[z].getStartHour() != null
                && evt[z].getStartHour().length() > 0) {
              html.append("&nbsp;(").append(evt[z].getStartHour());
              if (evt[z].getEndHour() != null
                  && evt[z].getEndHour().length() > 0)
                html.append("-").append(evt[z].getEndHour());
              html.append(")");
            }
            html.append("</td>");
            tdIsCreate = true;
            break;
          }
        }
        if (!tdIsCreate) {
          html.append(">&nbsp;</td>");
        }
      }
      return html.toString();
    }
  }
}
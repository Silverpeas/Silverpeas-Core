<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkAgenda.jsp" %>
<%!

String getHTMLMonthCalendar(Date date, MultiSilverpeasBundle resource, List holidays, AgendaSessionController agendaSc) {

		String  weekDayStyle 		= "class=\"txtnav\"";
	String  selectedDayStyle	= "class=\"intfdcolor5\"";
    String  dayOffStyle = "class=\"txtdayoff1\"";

	StringBuffer result = new StringBuffer(255);

	result.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\">");

        Calendar calendar = Calendar.getInstance();

        int firstDayOfWeek = Calendar.MONDAY;

        calendar.setTime(date);
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
        while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek)
        {
            calendar.add(Calendar.DATE, -1);
            startDay++;
        }

        result.append("<tr class=\"txtnav2\"><td colspan=\"7\">\n");
        result.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");
        result.append("<td class=\"intfdcolor3\" align=\"center\"><span class=\"txtNav4\">").append(resource.getString("GML.mois" + month)).append(" ").append(year).append("</span></td>");
        result.append("</tr></table>\n");
        result.append("</td></tr>");

        result.append("<tr class=\"intfdcolor2\">\n");

        do
        {
	        result.append("<th ").append(weekDayStyle).append("><a href=\"javaScript:changeDayStatus('").append(year).append("', '").append(month).append("', '").append(calendar.get(Calendar.DAY_OF_WEEK)).append("');\">").append("<span class=\"txtnav\">").append(resource.getString("GML.shortJour" + calendar.get(Calendar.DAY_OF_WEEK))).append("</span></a></th>");
          calendar.add(Calendar.DATE, 1);
        }
        while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek);

        result.append("</tr>\n");

        // put blank table entries for days of week before beginning of the month
        result.append("<tr>\n");
        int column = 0;

        for (int i = 0; i < startDay - 1; i++)
        {
            result.append("<td width=\"14%\">&nbsp;</td>");
            column++;
        }

        calendar.setTime(date);

        Date 	currentDate 		= null;
        String 	sCurrentDate		= null;
        String 	currentDateStyle	= null;
        int		nextStatus			= 0;
        for (int i = 1; i <= numDays; i++)
        {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            currentDate 	= calendar.getTime();
            sCurrentDate 	= DateUtil.date2SQLDate(currentDate);
            if (holidays.contains(sCurrentDate))
            {
							currentDateStyle 	= dayOffStyle;
		nextStatus			= 0;
            }
            else
            {
							currentDateStyle 	= "";
							nextStatus			= 1;
						}

            result.append("<td width=\"14%\" align=\"center\" ").append(">").append("<a href=\"javascript:changeDateStatus('").append(sCurrentDate).append("','").append(nextStatus).append("');\">").append("<span ").append(currentDateStyle).append(">"+i).append("</span></a></td>\n");

            // Check for end of week/row
            if ((++column == 7) && (numDays > i))
            {
                result.append("</tr>\n<tr>");
                column = 0;
            }
        }
        for (int i = column; i <= 6; i++)
        {
            result.append("<td>&nbsp;</td>\n");
        }
        result.append("</tr></table>\n");

        return result.toString();
    }

%>
<%
Date 				beginDate 	= (Date) request.getAttribute("BeginDate");
Date 				endDate		= (Date) request.getAttribute("EndDate");
List			 	holidays	= (List) request.getAttribute("HolidayDates");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript">
function changeDateStatus(day, status)
{
	document.calendarForm.Date.value = day;
	document.calendarForm.Status.value = status;
	document.calendarForm.submit();
}
function changeDayStatus(year, month, day)
{
	document.calendarDayForm.DayOfWeek.value = day;
	document.calendarDayForm.Month.value = month;
	document.calendarDayForm.Year.value = year;
	document.calendarDayForm.submit();
}

function viewByYear()
{
    document.calendarForm.action = "ViewByYear";
    document.calendarForm.submit();
}

function viewByMonth()
{
    document.calendarForm.action = "ViewByMonth";
    document.calendarForm.submit();
}

function viewByWeek()
{
    document.calendarForm.action = "ViewByWeek";
    document.calendarForm.submit();
}

function viewByDay()
{
    document.calendarForm.action = "ViewByDay";
    document.calendarForm.submit();
}
function gotoNext()
{
    document.calendarForm.action = "NextYear";
    document.calendarForm.submit();
}

function gotoPrevious()
{
    document.calendarForm.action = "PreviousYear";
    document.calendarForm.submit();
}
</script>
</head>
<body id="agenda">
<%
		Window window = graphicFactory.getWindow();
		BrowseBar browseBar = window.getBrowseBar();
		browseBar.setComponentName(resources.getString("agenda"), "Main");

    out.println(window.printBefore());

		TabbedPane tabbedPane = graphicFactory.getTabbedPane();
		tabbedPane.addTab(resources.getString("GML.day"), "javascript:onClick=viewByDay()", (agenda.getCurrentDisplayType() == AgendaHtmlView.BYDAY) );
		tabbedPane.addTab(resources.getString("GML.week"), "javascript:onClick=viewByWeek()", (agenda.getCurrentDisplayType() == AgendaHtmlView.BYWEEK) );
		tabbedPane.addTab(resources.getString("GML.month"), "javascript:onClick=viewByMonth()", (agenda.getCurrentDisplayType() == AgendaHtmlView.BYMONTH) );
		tabbedPane.addTab(resources.getString("GML.year"), "javascript:onClick=viewByYear()", (agenda.getCurrentDisplayType() == AgendaHtmlView.BYYEAR) );
		tabbedPane.addTab(resources.getString("GML.calendar"), "ToChooseWorkingDays", (agenda.getCurrentDisplayType() == AgendaHtmlView.CHOOSE_DAYS) );
    out.println(tabbedPane.print());

		Frame frame=graphicFactory.getFrame();
    out.println(frame.printBefore());

    Calendar calendar = Calendar.getInstance();

    %>
<center>
<table cellpadding="0" cellspacing="0" width="98%" border="0">
    <tr>
		<td bgcolor="#000000">
			<table cellpadding="2" cellspacing="1" border="0">
				<tr>
					<td class="intfdcolor" align="center" nowrap="nowrap">
					<table cellpadding="0" cellspacing="0" border="0" width="12"><tr><td width="12" align="right"><a href="javascript:onClick=gotoPrevious()"><img src="<%=arrLeft%>" border="0" alt=""/></a></td>
		        <td align="center" nowrap="nowrap"><span class="txtnav"><%=agenda.getStartYear()%></span></td>
          <td width="12"><a href="javascript:onClick=gotoNext()"><img src="<%=arrRight%>" border="0" alt=""/></a></td>
          <td align="right"></td></tr></table>
					</td>
				</tr>
			</table>
		 </td>
		 <td><img src="<%=noColorPix%>" width="2" alt=""/></td>
		 <td width="95%" align="left" class="txt1"><span class="txtdayoff2">&nbsp;<%=resources.getString("agenda.ChooseDaysOff")%> <%=resources.getString("agenda.DaysOffSilver")%></span></td>
		</tr>
</table>
<br/>
<table cellpadding="0" cellspacing="0" width="98%" border="0">
	<tr>
		<td>
			<table cellpadding="2" cellspacing="0" width="100%" border="0">
			<tr>
			<td class="intfdcolor">
					<table border="0" cellpadding="1" cellspacing="1" width="100%">
						<tr>
						<%
						    int i = 1;
						    while (beginDate.before(endDate))
						    {
							if (i-4 > 0) {
								out.println("</tr><tr>");
								i = 1;
							}

							out.println("<td valign=\"top\" class=\"contourintfdcolor\">");
							out.println(getHTMLMonthCalendar(beginDate, resources, holidays, agenda));
							out.println("</td>");

							calendar.setTime(beginDate);
							calendar.add(Calendar.MONTH, 1);
							beginDate = calendar.getTime();

							i++;
						    }
						    %>
						  </tr>
					 </table>
				 </td>
			 </tr>
		 </table>
		</td>
	</tr>
</table>
</center>
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>

<form name="calendarForm" action="ChangeDateStatus" method="post">
	<input type="hidden" name="Date"/>
	<input type="hidden" name="Status"/>
</form>
<form name="calendarDayForm" action="ChangeDayOfWeekStatus" method="post">
	<input type="hidden" name="DayOfWeek"/>
	<input type="hidden" name="Month"/>
	<input type="hidden" name="Year"/>
</form>
</body>
</html>
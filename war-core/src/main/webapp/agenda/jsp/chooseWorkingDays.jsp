<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="checkAgenda.jsp.inc" %>
<%!

String getHTMLMonthCalendar(Date date, ResourcesWrapper resource, List holidays, AgendaSessionController agendaSc) {

		String  weekDayStyle 		= "class=\"txtnav\"";
    	String  selectedDayStyle	= "class=\"intfdcolor5\"";
    String  dayOffStyle = "class=\"txtdayoff1\""; 

     	StringBuffer result = new StringBuffer(255);

     	result.append("<TABLE width=\"100%\" BORDER=0 CELLSPACING=\"0\" CELLPADDING=\"1\">");

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

        result.append("<TR class=\"txtnav2\"><TD COLSPAN=7>\n");
        result.append("<TABLE width=\"100%\" BORDER=0 CELLSPACING=\"0\" CELLPADDING=\"0\"><TR>");
        result.append("<TD class=\"intfdcolor3\" ALIGN=\"center\"><span class=txtNav4>").append(resource.getString("GML.mois" + month)).append(" ").append(year).append("</span></TD>");
        result.append("</TR></TABLE>\n");
        result.append("</TD></tr>");

        result.append("<TR class=\"intfdcolor2\">\n");

        do
        {
 	        result.append("<TH ").append(weekDayStyle).append("><a href=\"javaScript:changeDayStatus('").append(year).append("', '").append(month).append("', '").append(calendar.get(Calendar.DAY_OF_WEEK)).append("');\">").append("<span class=\"txtnav\">").append(resource.getString("GML.shortJour" + calendar.get(Calendar.DAY_OF_WEEK))).append("</a></TH>");
          calendar.add(Calendar.DATE, 1);
        }
        while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek);

        result.append("</TR>\n");

        // put blank table entries for days of week before beginning of the month
        result.append("<TR>\n");
        int column = 0;

        for (int i = 0; i < startDay - 1; i++)
        {
            result.append("<TD width=\"14%\">&nbsp;</TD>");
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
				
            result.append("<TD width=\"14%\" align=\"center\" ").append(">").append("<A HREF=\"javascript:changeDateStatus('").append(sCurrentDate).append("','").append(nextStatus).append("');\">").append("<span ").append(currentDateStyle).append(">"+i).append("</span></A></TD>\n");

            // Check for end of week/row
            if ((++column == 7) && (numDays > i))
            {
                result.append("</TR>\n<TR>");
                column = 0;
            }
        }
        for (int i = column; i <= 6; i++)
        {
            result.append("<TD>&nbsp;</TD>\n");
        }
        result.append("</TR></TABLE>\n");

        return result.toString();
    }

%>
<%
Date 				beginDate 	= (Date) request.getAttribute("BeginDate");
Date 				endDate		= (Date) request.getAttribute("EndDate");
List			 	holidays	= (List) request.getAttribute("HolidayDates");
%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
    out.println(graphicFactory.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
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

</HEAD>
<BODY id="agenda">
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
<TABLE CELLPADDING=0 CELLSPACING=0 width="98%" border=0>
    <TR>
		<TD bgcolor="#000000">
			<table cellpadding=2 cellspacing=1 border=0 height=28>
				<tr>
					<td class=intfdcolor align=center nowrap nowrap>
					<table cellpadding=0 cellspacing=0 border=0 width=200><tr><td width="12" align="right"><a href="javascript:onClick=gotoPrevious()"><img src="<%=arrLeft%>" border="0"></a></td>
		        <td align="center" nowrap><span class="txtnav"><%=agenda.getStartYear()%></span></td>
          <td width="12"><a href="javascript:onClick=gotoNext()"><img src="<%=arrRight%>" border="0"></a></td>
          <td align="right"></td></tr></table>
					</td>
				</tr>
			</table>
		 </TD>
		 <TD><img src="<%=noColorPix%>" width=2></TD>
		 <td width=95% align="left" class="txt1">&nbsp;<%=resources.getString("agenda.ChooseDaysOff")%><span class="txtdayoff2"><%=resources.getString("agenda.DaysOffSilver")%></span></td>
		</tr>
</table>		
<br>

<table CELLPADDING=0 CELLSPACING=0 width="98%" border=0>	
	<tr>
		<td>
			<table CELLPADDING=2 CELLSPACING=0 width="100%" border=0>	
        <tr>
        	<td class="intfdcolor">
						<table border="0" cellpadding="1" cellspacing="1" width="100%">
						<%
						    int i = 1;
						    while (beginDate.before(endDate))
						    {
						    	if (i==1)
						    		out.println("<tr>");
						    		
						    	if (i-4 > 0)
						    	{
						    		out.println("</tr>");
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
					 </table>
				 </td>
			 </tr>
		 </table>
		</tr>
	</td>
</table>
</center>
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>

<form name="calendarForm" action="ChangeDateStatus" method="POST">
	<input type="hidden" name="Date">
	<input type="hidden" name="Status">
</form>
<form name="calendarDayForm" action="ChangeDayOfWeekStatus" method="POST">
	<input type="hidden" name="DayOfWeek">
	<input type="hidden" name="Month">
	<input type="hidden" name="Year">
</form>
</BODY>
</HTML>
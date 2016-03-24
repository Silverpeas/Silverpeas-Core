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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>
<%@ page import="org.silverpeas.core.calendar.model.Schedulable"%>
<%@ page import="org.silverpeas.core.util.LocalizationBundle"%>
<%@ page import="org.silverpeas.core.calendar.model.JournalHeader" %>
<%@ page import="org.silverpeas.core.calendar.model.Attendee" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.SettingBundle" %>

<%@ include file="checkAgenda.jsp" %>
<%
LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(agenda.getLanguage());

  agenda.viewByDay();

  SettingBundle settings = agenda.getSettings();
  String action = (String) request.getParameter("Action");
  int formIndex = new Integer((String) request.getParameter("Form")).intValue();

  if (action == null) action = "View";
  if (action.equals("Next")) {
    agenda.nextDay();
  } else
  if (action.equals("Previous")) {
    agenda.previousDay();
  }
  String day = request.getParameter("Date");
  if (day != null)
	agenda.selectDay(day);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<title><%=agenda.getString("trouverCreneau")%></title>
<script type="text/javascript">

function selectHour(hour,minute,date)
{
  window.opener.setBeginDateAndHour(date, hour, minute);
  window.close();
}

function gotoNext()
{
    document.busyTimeForm.Action.value = "Next";
    document.busyTimeForm.submit();
}

function gotoPrevious()
{
    document.busyTimeForm.Action.value = "Previous";
    document.busyTimeForm.submit();
}
</script>
</head>
<body id="agenda">

<%
	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName("Agenda");
	browseBar.setPath(agenda.getString("trouverCreneau"));

	String navigation =  "<table><tr><td width=\"12\" align=\"right\"><a href=\"javascript:onClick=gotoPrevious()\">" +
			"<img src=\"icons/topnav_l.gif\" width=\"6\" height=\"11\" border=\"0\" alt=\"\"/></a></td>" +
		        "<td align=\"center\" width=\"340\"><span class=\"txtnav\">";

	navigation += agenda.getString("jour"+agenda.getStartDayInWeek()) + " " +
		    agenda.getStartDayInMonth() + " " +
		    agenda.getString("mois"+agenda.getStartMonth()) + " " +
		    agenda.getStartYear();

	navigation += "</span></td>" +
          "<td width=\"12\"><a href=\"javascript:onClick=gotoNext()\"><img src=\"icons/topnav_r.gif\" width=\"6\" height=\"11\" border=\"0\" alt=\"\"/></a></td>" +
          "<td align=\"right\">&nbsp;</td></tr></table>";

	out.println(window.printBefore());

	Frame frame=graphicFactory.getFrame();
    out.println(frame.printBefore());
%>

  <table border="0" cellpadding="0" cellspacing="0"><tr><td>&nbsp;&nbsp;</td><td>
	 <table cellpadding="2" cellspacing="1" border="0" width="300" class="line">
			  <tr>
			    <td class="intfdcolor" align="center" nowrap="nowrap" width="100%" height="24"><%out.println(navigation);%></td>
			  </tr>
			</table>
			</td></tr></table>
  <%= separator %>
  <center>
  <table border="0" cellpadding="0" cellspacing="0"><tr><td>&nbsp;&nbsp;</td><td>
     <table width="98%" class="intfdcolor" border="0" cellpadding="0" cellspacing="2">
      <tr>
       <td>
       <table border="1" class="intfdcolor4" cellpadding="2" cellspacing="1" width="100%">
        <tr>
         <td>
          <table border="0" cellpadding="0" cellspacing="1" class="intfdcolor" width="100%">
           <tr>
            <td align="center" class="intfdcolor2" rowspan="2">&nbsp;</td>
          <%
            int beginHour = new Integer(settings.getString("beginHour")).intValue();
            int endHour = new Integer(settings.getString("endHour")).intValue();
            for (int i = beginHour; i < endHour; i++) {
              out.println("<td align=\"center\" class=\"intfdcolor2\" colspan=\"4\">" +i + "H" + "</td>");
            }
          %>
        </tr>
        <tr>
          <%
            for (int i = beginHour * 4; i < endHour * 4; i++) {
              out.print("<td class=\"intfdcolor2\">");

              String minute = String.valueOf((i & 3)*15);
              if (minute.length() < 2) minute = "0" + minute;
              String hour = String.valueOf(i >> 2);
              if (hour.length() < 2) hour = "0" + hour;
              String date = resources.getInputDate(agenda.getCurrentDay());
              out.print("<a href=\"javascript:onClick=selectHour('"+ hour
                +"','"+minute+"','"+date+"')\">" + minute);
              out.print("</a>");
              out.println("</td>");
            }
          %>
        </tr>
      <%
        Collection attendeesOld = agenda.getCurrentAttendees();
        JournalHeader journal = agenda.getCurrentJournalHeader();
        Collection attendees = new ArrayList();
        attendees.add(new Attendee(journal.getDelegatorId()));
        if (attendeesOld != null) {
          Iterator i = attendeesOld.iterator();
          while (i.hasNext()) {
            attendees.add(i.next());
          }
        }
        if (attendees != null) {
          Iterator i = attendees.iterator();
          while (i.hasNext()) {
            Attendee attendee = (Attendee) i.next();
            UserDetail user = agenda.getUserDetail(attendee.getUserId());
            out.println("<tr class=\"intfdcolor2\">");
            out.println("<td class=\"txtnav\" nowrap=\"nowrap\">");
            out.println(user.getLastName() + " " + user.getFirstName());
            out.println("</td>");

            Collection busyTime = agenda.getBusyTime(attendee.getUserId(), agenda.getCurrentDay());

            int h = beginHour * 4;
            int nbFree = 0;
            int nbBusy = 0;

            //If day in agenda's diary is a day off
            boolean dayOff = false;
            if (agenda.isHolidayDate(user.getId(), agenda.getCurrentDay()))
            {
	            dayOff = true;
	            nbBusy = endHour * 4;
            }

            JournalHeader hour = new JournalHeader("","");
            hour.setStartDate(agenda.getCurrentDay());
            while (h < endHour * 4 && !dayOff) {
              hour.setStartHour(Schedulable.quaterCountToHourString(h));
              hour.setEndHour(Schedulable.quaterCountToHourString(h+1));
              boolean isOver = false;
              Iterator b = busyTime.iterator();
              while (b.hasNext()) {
                Schedulable sched = (Schedulable) b.next();
                if (sched.isOver(hour)) isOver = true;
                //Complete day case
								if (sched.getStartHour() == null || sched.getEndHour() == null) isOver = true;
		              }
		              if (isOver && (nbFree == 0)) {
		                nbBusy++;
		              }
		              else if ( (!isOver) && (nbBusy == 0)) {
		                nbFree++;
		              }
		              else {
		                if (nbBusy != 0) {
		                  out.println("<td colspan=\""+nbBusy+"\" class=\"intfdcolor6\">&nbsp;</td>");
		                  nbBusy = 0;
		                  nbFree++;
		                }
		                else {
		                  out.println("<td colspan=\""+nbFree+"\" class=\"intfdcolor4\">&nbsp;</td>");
		                  nbFree = 0;
		                  nbBusy++;
		                }
		              }

              h++;
            } //end hour

            if (nbBusy != 0) {
                out.println("<td colspan=\""+nbBusy+"\" class=\"intfdcolor6\">&nbsp;</td>");
                nbBusy = 0;
            } else
            if (nbFree != 0) {
                out.println("<td colspan=\""+nbFree+"\" class=\"intfdcolor4\">&nbsp;</td>");
                nbFree = 0;
            }

            out.println("</tr>");
          }
        }
      %>
        </table>
       </td>
      </tr>
      <tr>
		<td>
		<table align="center">
				<tr><td class="intfdcolor6outline" width="12">&nbsp;</td><td>&nbsp;<%=agenda.getString("busy")%></td></tr>
			</table>
		</td>
		</tr>
     </table>
    </td>
   </tr>
  </table>
  </td><td>&nbsp;&nbsp;</td></tr>
  </table>
</center>

<%
	out.println(frame.printMiddle());
    out.println(separator);
    out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<form name="busyTimeForm" action="busyTime.jsp" method="post">
  <input type="hidden" name="Action"/>
  <input type="hidden" name="Form" value="<%=String.valueOf(formIndex)%>"/>
</form>
</body>
</html>
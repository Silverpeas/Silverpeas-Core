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

<%@ page import="org.silverpeas.core.calendar.model.Schedulable"%>

<%@ include file="checkAgenda.jsp" %>

<%

  SettingBundle settings = agenda.getSettings();
  String action = null;
  String journalId = null;

  action = (String) request.getParameter("Action");
  if (action == null) {
    action="View";
  }

  if (action.length() == 0) {
    action="View";
  }

  String updateAgendaDay = null;

  if (action.equals("SetParticipationStatus")) {
    String userId = (String) request.getParameter("UserId");
    String status = (String) request.getParameter("Status");
    journalId = (String) request.getParameter("JournalId");
    updateAgendaDay = (String) request.getParameter("Day");
    agenda.setJournalParticipationStatus(journalId, userId, status);
    action = "View";
  }

  Collection tentatives = agenda.getTentativeSchedulables();

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript">

function viewJournal(journalId)
{
  window.opener.location.replace('journal.jsp?Action=Update&JournalId=' + journalId);
}

function viewAgenda(day)
{
  window.opener.location.replace('agenda.jsp?Action=SelectDay&Day=' + day);
}

function setParticipationStatus(journalId, userId, status, day)
{
  document.journalForm.Action.value = "SetParticipationStatus";
  document.journalForm.JournalId.value = journalId;
  document.journalForm.UserId.value = userId;
  document.journalForm.Status.value = status;
  document.journalForm.Day.value = day;
  document.journalForm.submit();
}

function updateAgendaDay(day)
{
  viewAgenda(day);
  <%
  if (tentatives.size() == 0) {
  %>
  window.close();
  <%
  }
  %>
}

</script>
</head>
<%
  if (updateAgendaDay != null)
    out.println("<body id=\"agenda\" onload=\"updateAgendaDay('"+updateAgendaDay+"')\">");
  else
    out.println("<body id=\"agenda\">");

 String acceptIcon = m_context + "/util/icons/ok.gif";
 String refuseIcon =  m_context + "/util/icons/refus.gif";

	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setPath(agenda.getString("listeInvitations"));
	out.println(window.printBefore());
	Frame frame = graphicFactory.getFrame();
  out.println(frame.printBefore());

%>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td valign="top">
      <table border="0" align="center" width="98%" cellspacing="1" cellpadding="1" class="intfdcolor">
        <tr valign="top">
          <td width="100%" align="center" class="intfdcolor4">
            <table border="0" width="100%" cellspacing="1" cellpadding="1">
              <tr><td>&nbsp;</td></tr>
            <%
            Iterator i = tentatives.iterator();
            while (i.hasNext()) {
              out.print("<tr>");
              Schedulable schedule = (Schedulable) i.next();
              out.print("<td>");
              out.print("<a href=\"javascript:onClick=viewJournal('"+schedule.getId()+"')\">");
              out.print(schedule.getName());
              out.println("</a>");
              out.print("</td>");

              if (schedule.getStartDay().equals(schedule.getEndDay())) {
                out.print("<td nowrap=\"nowrap\">");
                String startDay = resources.getInputDate(schedule.getStartDate());
                out.print("<a href=\"javascript:onClick=viewAgenda('"+startDay+"')\">");
                out.print(resources.getOutputDate(schedule.getStartDate()));
                out.print("</a>");
                out.print("</td>");
                out.print("<td class=\"txtnote\" nowrap=\"nowrap\">");
                if (schedule.getStartHour() != null) {
                  out.print(schedule.getStartHour());
                  if (schedule.getEndHour() != null) {
                    out.print(" - " +schedule.getEndHour());
                  }
                }
                out.print("</td>");
              }
              else {
                out.print("<td class=\"txtnote\" nowrap=\"nowrap\">");
                String startDay = resources.getInputDate(schedule.getStartDate());
                out.print("<a href=\"javascript:onClick=viewAgenda('"+startDay+"')\">");
                out.print(resources.getOutputDate(schedule.getStartDate()));
                out.print("</a>");
                if (schedule.getStartHour() != null) {
                  out.print(" - " + schedule.getStartHour());
                }
                out.print("</td>");
                out.print("<td class=\"txtnote\" nowrap=\"nowrap\">");
                out.print(resources.getOutputDate(schedule.getEndDate()));
                if (schedule.getEndHour() != null) {
                  out.print(" - " +schedule.getEndHour());
                }
                out.println("</td>");
              }
              out.print("<td><a href=\"javascript:onClick=setParticipationStatus('"+
                schedule.getId()+"','"+agenda.getUserId()+"','"+
                ParticipationStatus.ACCEPTED+"','"+resources.getInputDate(schedule.getStartDate())+"')\">");
              out.print("<img title=\"" + agenda.getString("accepterInvitation") + "\" src=\"" + acceptIcon + "\" border=\"0\"/>");
              out.print("</a></td>");
              out.print("<td><a href=\"javascript:onClick=setParticipationStatus('"+
                schedule.getId()+"','"+agenda.getUserId()+"','"+
                ParticipationStatus.DECLINED+"','"+resources.getInputDate(schedule.getStartDate())+"')\">");
              out.print("<img title=\"" + agenda.getString("refuserInvitation") + "\" src=\"" + refuseIcon + "\" border=\"0\"/>");
              out.println("</a></td>");
              out.println("</tr>");
            }
            %>
            <tr><td>&nbsp;</td></tr>
												</table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
<br/>
<center>
<%
				Button button = graphicFactory.getFormButton(agenda.getString("fermer"), "javascript:onClick=window.close()", false);
				out.print(button.print());
%>
</center>
<%
out.println(frame.printMiddle());
out.println(frame.printAfter());
out.println(window.printAfter());
%>

<form name="journalForm" action="tentative.jsp" method="post">
  <input type="hidden" name="Action"/>
  <input type="hidden" name="UserId"/>
  <input type="hidden" name="Status"/>
  <input type="hidden" name="JournalId"/>
  <input type="hidden" name="Day"/>
</form>

</body>
</html>
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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.agenda.view.*"%>
<%@ page import="com.stratelia.webactiv.calendar.model.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.*"%>
<%@ include file="checkAgenda.jsp.inc" %>

<%

  ResourceLocator settings = agenda.getSettings();
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

<HTML>
<HEAD>
<% out.println(graphicFactory.getLookStyleSheet()); %>
<Script language="JavaScript">

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
</HEAD>
<%
  if (updateAgendaDay != null)
    out.println("<BODY id=\"agenda\" onLoad=\"updateAgendaDay('"+updateAgendaDay+"')\">");
  else
    out.println("<BODY id=\"agenda\">");

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
    <td valign=top> 
      <table border="0" align="center" width="98%" cellspacing="1" cellpadding="1" class="intfdcolor">
        <tr valign="top">
          <td width="100%" align="center" class="intfdcolor4">
            <table border="0" width="100%" cellspacing="1" cellpadding="1">
              <tr><td>&nbsp;</td></tr>
            <%
            Iterator i = tentatives.iterator();
            while (i.hasNext()) {
              out.print("<TR>");
              Schedulable schedule = (Schedulable) i.next();
              out.print("<TD>");
              out.print("<a href=\"javascript:onClick=viewJournal('"+schedule.getId()+"')\">");
              out.print(schedule.getName());
              out.println("</a>");
              out.print("</TD>");
              
              if (schedule.getStartDay().equals(schedule.getEndDay())) {
                out.print("<TD nowrap>");
                String startDay = resources.getInputDate(schedule.getStartDate());
                out.print("<A HREF=\"javascript:onClick=viewAgenda('"+startDay+"')\">");
                out.print(resources.getOutputDate(schedule.getStartDate()));
                out.print("</A>");
                out.print("</TD>");
                out.print("<TD class=\"txtnote\" nowrap>");
                if (schedule.getStartHour() != null) {
                  out.print(schedule.getStartHour());
                  if (schedule.getEndHour() != null) {
                    out.print(" - " +schedule.getEndHour());
                  }
                }
                out.print("</TD>");
              }
              else {
                out.print("<TD class=\"txtnote\" nowrap>");
                String startDay = resources.getInputDate(schedule.getStartDate());
                out.print("<A HREF=\"javascript:onClick=viewAgenda('"+startDay+"')\">");
                out.print(resources.getOutputDate(schedule.getStartDate()));
                out.print("</A>");
                if (schedule.getStartHour() != null) {
                  out.print(" - " + schedule.getStartHour());
                }
                out.print("</TD>");
                out.print("<TD class=\"txtnote\" nowrap>");
                out.print(resources.getOutputDate(schedule.getEndDate()));
                if (schedule.getEndHour() != null) {
                  out.print(" - " +schedule.getEndHour());
                }
                out.println("</TD>");
              }
              out.print("<TD><a href=\"javascript:onClick=setParticipationStatus('"+
                schedule.getId()+"','"+agenda.getUserId()+"','"+
                ParticipationStatus.ACCEPTED+"','"+resources.getInputDate(schedule.getStartDate())+"')\">");
              out.print("<img title=\"" + agenda.getString("accepterInvitation") + "\" src=\"" + acceptIcon + "\" border=0");
              out.print("</img></a></td>");
              out.print("<TD><a href=\"javascript:onClick=setParticipationStatus('"+
                schedule.getId()+"','"+agenda.getUserId()+"','"+
                ParticipationStatus.DECLINED+"','"+resources.getInputDate(schedule.getStartDate())+"')\">");
              out.print("<img title=\"" + agenda.getString("refuserInvitation") + "\" src=\"" + refuseIcon + "\" border=0");
              out.println("</img></a></td>");
              out.println("</TR>");
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
<br>
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

<FORM NAME="journalForm" ACTION="tentative.jsp" METHOD=POST >
  <input type="hidden" name="Action">
  <input type="hidden" name="UserId">
  <input type="hidden" name="Status">
  <input type="hidden" name="JournalId">
  <input type="hidden" name="Day">
</FORM>

</BODY>
</HTML>
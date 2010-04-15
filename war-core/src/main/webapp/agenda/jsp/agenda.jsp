<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.*"%>

<%@ include file="checkAgenda.jsp.inc" %>

<%
  ResourceLocator settings = agenda.getSettings();
  ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(agenda.getLanguage());
				
  String action = (String) request.getParameter("Action");

  if (action == null) {
    action = "View";
  }
  else if (action.equals("OpenCalendar")) {
    agenda.setCalendarVisible(true);
  }
  else if (action.equals("CloseCalendar")) {
    agenda.setCalendarVisible(false);
  }
  else if (action.equals("Next")) {
    agenda.next();
  }
  else if (action.equals("Previous")) {
    agenda.previous();
  }
  else if (action.equals("NextMonth")) {
    agenda.nextMonth();
  }
  else if (action.equals("PreviousMonth")) {
    agenda.previousMonth();
  }
  else if (action.equals("ViewCategory")) {
    String categoryId = (String) request.getParameter("Category");
    if (categoryId.equals("0")) 
      agenda.setCategory(null);
    else
      agenda.setCategory(agenda.getCategory(categoryId));
    action = "View";
  }
  else if (action.equals("ViewParticipation")) {
    String participation = (String) request.getParameter("Participation");
    agenda.getParticipationStatus().setString(participation);
    action = "View";
  }
  String		rssURL		= (String) request.getAttribute("RSSUrl");
  String iconLink = m_context + "/util/icons/link.gif";
  String link = URLManager.getApplicationURL() + "/Agenda/" + agenda.getAgendaUserDetail().getId();
  String subscribeAgendaUrl = (String) request.getAttribute("MyAgendaUrl");
%>

<HTML>
<HEAD>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>

<% if (StringUtil.isDefined(rssURL)) { %>
	<link rel="alternate" type="application/rss+xml" title="<%=resources.getString("agenda.agenda")%> : <%=resources.getString("agenda.rssNext")%>" href="<%=m_context+rssURL%>"/>
<% } %>
<% out.println(graphicFactory.getLookStyleSheet()); %>
<SCRIPT LANGUAGE="JAVASCRIPT" SRC="<%=javaScriptSrc%>"></SCRIPT>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/overlib.js"></script>
<div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>
<Script language="JavaScript">

function viewAgenda()
{
    document.agendaForm.Action.value = "View";
    document.agendaForm.submit();
}

function viewByYear()
{
    document.agendaForm.action = "ViewByYear";
    document.agendaForm.submit();
}

function viewByMonth()
{
    document.agendaForm.action = "ViewByMonth";
    document.agendaForm.submit();
}

function viewByWeek()
{
    document.agendaForm.action = "ViewByWeek";
    document.agendaForm.submit();
}

function viewByDay()
{
    document.agendaForm.action = "ViewByDay";
    document.agendaForm.submit();
}

function openCalendar()
{
    document.agendaForm.Action.value = "OpenCalendar";
    document.agendaForm.submit();
}

function closeCalendar()
{
    document.agendaForm.Action.value = "CloseCalendar";
    document.agendaForm.submit();
}

function addJournal()
{
    document.journalForm.JournalId.value = "";
    document.journalForm.Action.value = "Add";
    document.journalForm.action = "journal.jsp";
    document.journalForm.submit();
}

function viewJournal(journalId)
{
    document.journalForm.JournalId.value = journalId;
    document.journalForm.action = "UpdateEvent";
    document.journalForm.submit();
}

function gotoNext()
{
    document.agendaForm.Action.value = "Next";
    document.agendaForm.submit();
}

function gotoPrevious()
{
    document.agendaForm.Action.value = "Previous";
    document.agendaForm.submit();
}

function gotoNextMonth()
{
    document.agendaForm.Action.value = "NextMonth";
    document.agendaForm.submit();
}

function gotoPreviousMonth()
{
    document.agendaForm.Action.value = "PreviousMonth";
    document.agendaForm.submit();
}

function selectDay(day)
{
    document.agendaForm.Day.value = day;
    document.agendaForm.action = "SelectDay";
    document.agendaForm.submit();
}

function selectHour(hour)
{
    document.journalForm.Hour.value = hour;
    addJournal();
}

function viewTentative() 
{
	if (window.TentativeWin != null)
		window.TentativeWin.close();
	TentativeWin = window.open('tentative.jsp','Invitation','width=450,height=300,alwaysRaised,scrollbars=yes,resizable');
}

function viewOtherAgenda()
{
	SP_openWindow('ChooseOtherAgenda','ChooseOtherAgenda','750','550','scrollbars=yes, resizable, alwaysRaised');
}

function exportIcal()
{
	SP_openWindow('ToExportIcal','ToExportIcal','345','200','scrollbars=no, noresize, alwaysRaised');
}
function importIcal()
{
	SP_openWindow('ToImportIcal','ToImportIcal','500','230','scrollbars=yes, noresize, alwaysRaised');
}
function synchroIcal()
{
	SP_openWindow('ToSynchroIcal','ToSynchroIcal','750','270','scrollbars=yes, noresize, alwaysRaised');
}

function viewCurrentAgenda()
{
    document.mainForm.submit();
}
</script>
</HEAD>
<BODY id="agenda">
<%
	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"));

	OperationPane operationPane = window.getOperationPane();

	if (!agenda.isOtherAgendaMode())
		operationPane.addOperation(agendaAddSrc, agenda.getString("nouvelleNote"), "javascript:onClick=addJournal()");
	if ("yes".equals(settings.getString("sharingModeAvailable")))
	{
		operationPane.addOperation(viewOtherAgenda, agenda.getString("viewOtherAgenda"), "javascript:onClick=viewOtherAgenda()");
		if (agenda.isOtherAgendaMode())
			operationPane.addOperation(viewCurrentAgenda, agenda.getString("viewCurrentAgenda"), "javascript:onClick=viewCurrentAgenda()");
	}

	if ("yes".equals(settings.getString("importCalendarAvailable")) && !agenda.isOtherAgendaMode())
	{
					operationPane.addLine();
					operationPane.addOperation(importSettingsSrc, agenda.getString("agenda.importSettings"), "ImportSettings");						
	}

	if (!agenda.isOtherAgendaMode())
	{
		operationPane.addLine();
		operationPane.addOperation(exportIcalSrc, resources.getString("agenda.ExportIcalCalendar"), "javascript:exportIcal();");						
		operationPane.addOperation(importIcalSrc, resources.getString("agenda.ImportIcalCalendar"), "javascript:importIcal();");
		operationPane.addOperation(synchroIcalSrc, resources.getString("agenda.SynchroIcalCalendar"), "javascript:synchroIcal();");									
	}
				
	out.println(window.printBefore());
  
		TabbedPane tabbedPane = graphicFactory.getTabbedPane();
		tabbedPane.addTab(resources.getString("GML.day"), "javascript:onClick=viewByDay()", (agenda.getCurrentDisplayType() == AgendaHtmlView.BYDAY) );
		tabbedPane.addTab(resources.getString("GML.week"), "javascript:onClick=viewByWeek()", (agenda.getCurrentDisplayType() == AgendaHtmlView.BYWEEK) );
		tabbedPane.addTab(resources.getString("GML.month"), "javascript:onClick=viewByMonth()", (agenda.getCurrentDisplayType() == AgendaHtmlView.BYMONTH) );
		tabbedPane.addTab(resources.getString("GML.year"), "javascript:onClick=viewByYear()", (agenda.getCurrentDisplayType() == AgendaHtmlView.BYYEAR) );
		if (!agenda.isOtherAgendaMode())
			tabbedPane.addTab(resources.getString("GML.calendar"), "ToChooseWorkingDays", (agenda.getCurrentDisplayType() == AgendaHtmlView.CHOOSE_DAYS) );
		out.println(tabbedPane.print());
	      
		Frame frame=graphicFactory.getFrame();
    out.println(frame.printBefore());

//Navigation de la browsbar

	String navigation = "<table cellpadding=0 cellspacing=0 border=0 width=200><tr><td width=\"12\" align=\"right\"><a href=\"javascript:onClick=gotoPrevious()\"><img src="+ arrLeft +" border=\"0\"></a></td>" +
		        "<td align=\"center\" nowrap><span class=\"txtnav\">";
	if (agenda.getCurrentDisplayType() == AgendaHtmlView.BYDAY) {
  	  navigation += agenda.getString("jour"+agenda.getStartDayInWeek()) + " " + 
		    agenda.getStartDayInMonth() + " " +
		    agenda.getString("mois"+agenda.getStartMonth()) + " " +
		    agenda.getStartYear();
	}
	else if (agenda.getCurrentDisplayType() == AgendaHtmlView.BYWEEK) {
	  navigation += agenda.getStartDayInMonth();
	  if ( agenda.getStartMonth() != agenda.getEndMonth() ) {
	     navigation += " " +  agenda.getString("mois"+agenda.getStartMonth());
	     if ( agenda.getStartYear() != agenda.getEndYear() )
		 navigation += " " +  agenda.getStartYear();
	  }
	  navigation += " - " +
		    agenda.getEndDayInMonth() + " " +
		    agenda.getString("mois"+agenda.getEndMonth()) + " " +
		    agenda.getEndYear();
	} 
	else if (agenda.getCurrentDisplayType() == AgendaHtmlView.BYMONTH) {
	  navigation += agenda.getString("mois"+agenda.getStartMonth()) + " " + agenda.getStartYear();
	}
	else if (agenda.getCurrentDisplayType() == AgendaHtmlView.BYYEAR) {
	  navigation += agenda.getStartYear();
	}

	navigation += "</span></td>" +
          "<td width=\"12\"><a href=\"javascript:onClick=gotoNext()\"><img src="+ arrRight +" border=\"0\"></a></td>" +
          "<td align=\"right\"></td></tr></table>";
		  %>

<CENTER>

<TABLE CELLPADDING=0 CELLSPACING=0 width="98%" border=0>
    <TR>
		<TD bgcolor=000000>
			<table cellpadding=2 cellspacing=1 border=0 height=28>
				<tr>
					<td class=intfdcolor align=center nowrap nowrap><%out.println(navigation);%></td>
				</tr>
			</table>
		 </TD>
		 <TD><img src="<%=noColorPix%>" width=2></TD>
		 <TD bgcolor=000000>
		 	<table cellpadding=2 cellspacing=1 width="90%" height=28>
		 		<form method="post" name="gotoDateForm" action="#">
		 			<tr>
		 				<td class=intfdcolor align=center nowrap width="120">
		 					<input type="text" name="Date" size="12" maxlength="10" align="middle" value="<%=DateUtil.getInputDate(new Date(), agenda.getLanguage())%>" onClick="this.value='';">
		 					<a href="javascript:onClick=selectDay(document.gotoDateForm.Date.value)">
		 					<img src="<%=btOk%>" border="0" align="top"></a>
		 				</td>
		 			</tr>
		 		</form>
		 	</table>
		 </TD>
		 <TD><img src="<%=noColorPix%>" width=2></TD>
		 <TD bgcolor=000000>
		 	<table cellpadding=2 cellspacing=1 width="100" height=28>
		 		<tr>
		 			<td class=intfdcolor align=center nowrap width="100%">
		 				<a class="hrefComponentName" href="javascript:onClick=selectDay('<%=resources.getInputDate(new java.util.Date())%>')"><%=agenda.getString("aujourdhui")%></a>
		 			</td>
		 		</tr>
		 	</table>
		 </TD>
		 <%
		 //Other agenda ?
		 if (agenda.isOtherAgendaMode())
		 {		
		 %>
		 <td width=95%>
		 	<table align="center">
		 		<tr>
		 			<td>
		 				&nbsp;<%=resources.getStringWithParam("userAgenda", agenda.getAgendaUserDetail().getDisplayedName())%>
		 				&nbsp;<a href=<%=link%> ><img src=<%=iconLink%> border="0" alt="<%=resources.getString("agenda.CopyAgendaLink")%>" title="<%=resources.getString("agenda.CopyAgendaLink")%>" ></a></td>

		 			</td>
		 		</tr>
		 	</table>
		 </td>
		 <%
		 }
		 else
		 { %>
		 <td width=95%>&nbsp;</td>
		 <%
		 }
	%>
	</tr>
  </TABLE>

 <%
      out.println(separator);
      AgendaHtmlView view = agenda.getCurrentHtmlView();
      String html =  view.getHtmlView();
      out.println(html);
	  out.println(separator);
 %>    

<TABLE CELLPADDING=0 CELLSPACING=0 width="98%" border=0>
        <TR>
				<FORM NAME="categoryForm" ACTION="agenda.jsp" METHOD="POST">
				<input type="hidden" name="Action" value="ViewCategory">
          <TD nowrap bgcolor=000000>
            <table cellpadding=1 cellspacing=1 border=0 width="100%">
							<tr>
								<td class=intfdcolor align=center nowrap width="100%" height="24">
	              <span class="txtnav"><%=agenda.getString("categories")%> : </span>
                  <SELECT name="Category" onChange="document.categoryForm.submit();">
                  <OPTION VALUE="0"><%=agenda.getString("toutesCategories")%></option>
<%
                Collection categories = agenda.getAllCategories();
                Iterator i = categories.iterator();
                while (i.hasNext()) {
                  Category category = (Category) i.next();
                  boolean selected = false;
                  if (agenda.getCategory() != null)
                    if (agenda.getCategory().getId().equals(category.getId()))
                      selected = true;
                  if (selected)
                    out.println("<OPTION SELECTED VALUE=\""+category.getId()+"\">" + category.getName() + "</option>");
                  else
                    out.println("<OPTION VALUE=\""+category.getId()+"\">" + category.getName() + "</option>");
                }
%>
                  </SELECT>

								</td>
							</tr>

						</table>
          </TD>
          </FORM>
					<TD><img src="<%=noColorPix%>" width=2></TD>
				<FORM NAME="participationForm" ACTION="agenda.jsp" METHOD="POST">
				<input type="hidden" name="Action" value="ViewParticipation">
      <TD bgcolor=000000>
										<table cellpadding=1 cellspacing=1 border=0 width="100%">
														<tr>
																		<td class=intfdcolor align=center nowrap width="100%" height="24" >
					              <span class="txtnav"><%=agenda.getString("participations")%> : </span>
				                  <SELECT name="Participation" onChange="document.participationForm.submit();">
<%
                String[] participations = ParticipationStatus.getJournalParticipationStatus();
                
                for (int pi = 0; pi < participations.length; pi++) {
                  boolean selected = false;
                  if (agenda.getParticipationStatus().getString().equals(participations[pi]) )
                    selected = true;
                  if (selected)
                    out.println("<OPTION SELECTED VALUE=\""+participations[pi]+"\">" + agenda.getString("participations" + participations[pi]) + "</option>");
                  else
                    out.println("<OPTION VALUE=\""+participations[pi]+"\">" + agenda.getString("participations" + participations[pi]) + "</option>");
                }
%>
                  </SELECT>
																</td>
											</tr>
						</table>
    </TD>	 
				<%
				if (agenda.hasTentativeSchedulables()) { %>
						<TD><img src="<%=noColorPix%>" width=1>
											<a href="javascript:onClick=viewTentative()"><img name="addnote" border="0" src="icons/alarm_bell.gif" alt="Voir les invitations" title="Voir les invitations"></a>
						</TD>
		 	<% } 
				//Other agenda ?
				if (agenda.isOtherAgendaMode() && (agenda.getCurrentDisplayType()==AgendaHtmlView.BYDAY || agenda.getCurrentDisplayType()==AgendaHtmlView.BYWEEK))
				{		%>
								<td width=50%>
												<table align="center">
																<tr>
																				<td width=20>&nbsp;</td>
																				<td class="publicEventOutline" width=12>&nbsp;</td>
																				<td>
																								&nbsp;<%=agenda.getString("publicEventLabel")%>
																				</td>
																				<td width=12>&nbsp;</td>
																				<td class="privateEventOutline" width=12>&nbsp;</td>
																				<td>
																								&nbsp;<%=agenda.getString("privateEventLabel")%>
																				</td>
																</tr>
												</table>
									</td>
			<% }	else { %>
								<td width=50%>&nbsp;</td>
			<% } %>
        </TR>
      </TABLE>
		</FORM>
		<% if (StringUtil.isDefined(rssURL) || !agenda.isOtherAgendaMode()) { %>
			<table>
			<tr>
				<% if (StringUtil.isDefined(rssURL)) { %>
					<td><a href="<%=m_context+rssURL%>"><img src="icons/rss.gif" border="0"></a><link rel="alternate" type="application/rss+xml" title="<%=resources.getString("agenda.agenda")%> : <%=resources.getString("agenda.rssNext")%>" href="<%=m_context+rssURL%>"></td>
				<% } %>
				<% if (!agenda.isOtherAgendaMode()) { %>
					<td><a href="<%=m_context+subscribeAgendaUrl%>" title="<%=agenda.getString("agenda.Subscribe")%>"><img valign="middle" src="icons/ical.gif" border="0"></a></td>
				<% } %>
			</tr>
			</table>
		<% } %>	
</CENTER>
<%		
      out.println(frame.printAfter());
      out.println(window.printAfter());
%>

<FORM NAME="agendaForm" METHOD="POST">
  <input type="hidden" name="Action">
  <input type="hidden" name="Day">
</FORM>

<FORM NAME="journalForm" METHOD="POST">
  <input type="hidden" name="JournalId">
  <input type="hidden" name="Hour">
  <input type="hidden" name="Action">
</FORM>

<form NAME="mainForm" ACTION="ViewCurrentAgenda" METHOD="POST">
</form>

</BODY>
</HTML>
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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.calendar.model.ParticipationStatus" %>
<%@ page import="org.silverpeas.core.calendar.model.Category" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="org.silverpeas.core.util.*" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkAgenda.jsp" %>

<%
  SettingBundle settings = agenda.getSettings();
  LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(agenda.getLanguage());

  String action = request.getParameter("Action");

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
    String categoryId = request.getParameter("Category");
    if (categoryId.equals("0")) {
      agenda.setCategory(null);
    } else {
      agenda.setCategory(agenda.getCategory(categoryId));
    }
    action = "View";
  }
  else if (action.equals("ViewParticipation")) {
    String participation = request.getParameter("Participation");
    agenda.getParticipationStatus().setString(participation);
    action = "View";
  }
  String		rssURL		= (String) request.getAttribute("RSSUrl");
  String iconLink = m_context + "/util/icons/link.gif";
  String link = URLUtil.getApplicationURL() + "/Agenda/" + agenda.getAgendaUserDetail().getId();
  String subscribeAgendaUrl = (String) request.getAttribute("MyAgendaUrl");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=generalMessage.getString("GML.popupTitle")%></title>
<% if (StringUtil.isDefined(rssURL)) { %>
	<link rel="alternate" type="application/rss+xml" title="<%=resources.getString("agenda.agenda")%> : <%=resources.getString("agenda.rssNext")%>" href="<%=m_context+rssURL%>"/>
<% } %>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/overlib.js"></script>
<script type="text/javascript">
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
	SP_openWindow('ToExportIcal','ToExportIcal','500','230','scrollbars=no, noresize, alwaysRaised');
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
</head>
<body id="agenda">
<div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>
<%
	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"));

	OperationPane operationPane = window.getOperationPane();

	if (!agenda.isOtherAgendaMode())
		operationPane.addOperationOfCreation(agendaAddSrc, agenda.getString("nouvelleNote"), "javascript:onClick=addJournal()");
	if ("yes".equals(settings.getString("sharingModeAvailable")))
	{
		operationPane.addOperation(viewOtherAgenda, agenda.getString("viewOtherAgenda"), "javascript:onClick=viewOtherAgenda()");
		if (agenda.isOtherAgendaMode()) {
			operationPane.addOperation(viewCurrentAgenda, agenda.getString("viewCurrentAgenda"), "javascript:onClick=viewCurrentAgenda()");
		}
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
		operationPane.addOperationOfCreation(importIcalSrc, resources.getString("agenda.ImportIcalCalendar"), "javascript:importIcal();");
		operationPane.addOperation(synchroIcalSrc, resources.getString("agenda.SynchroIcalCalendar"), "javascript:synchroIcal();");
	}

	out.println(window.printBefore());
%>
	<view:areaOfOperationOfCreation/>
<%
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
	String navigationLabel = "";
	if (agenda.getCurrentDisplayType() == AgendaHtmlView.BYDAY) {
	  navigationLabel += agenda.getString("jour"+agenda.getStartDayInWeek()) + " " +
		    agenda.getStartDayInMonth() + " " +
		    agenda.getString("mois"+agenda.getStartMonth()) + " " +
		    agenda.getStartYear();
	}
	else if (agenda.getCurrentDisplayType() == AgendaHtmlView.BYWEEK) {
	  navigationLabel += agenda.getStartDayInMonth();
	  if ( agenda.getStartMonth() != agenda.getEndMonth() ) {
	    navigationLabel += " " +  agenda.getString("mois"+agenda.getStartMonth());
	     if ( agenda.getStartYear() != agenda.getEndYear() )
	       navigationLabel += " " +  agenda.getStartYear();
	  }
	  navigationLabel += " - " +
		    agenda.getEndDayInMonth() + " " +
		    agenda.getString("mois"+agenda.getEndMonth()) + " " +
		    agenda.getEndYear();
	}
	else if (agenda.getCurrentDisplayType() == AgendaHtmlView.BYMONTH) {
	  navigationLabel += agenda.getString("mois"+agenda.getStartMonth()) + " " + agenda.getStartYear();
	}
	else if (agenda.getCurrentDisplayType() == AgendaHtmlView.BYYEAR) {
	  navigationLabel += agenda.getStartYear();
	}
  %>
<div id="navigation">
	<div id="currentScope">
		<a href="javascript:onClick=gotoPrevious()"><img src="<%=arrLeft %>" border="0" alt="" align="top"/></a>
		<span class="txtnav"><%=navigationLabel %></span>
		<a href="javascript:onClick=gotoNext()"><img src="<%=arrRight %>" border="0" alt="" align="top"/></a>
	</div>
	<div id="jump">
		<form method="post" name="gotoDateForm" action="#">
			<input type="text" name="Date" size="12" maxlength="10" align="middle" value="<%=DateUtil.getInputDate(new Date(), agenda.getLanguage())%>" onclick="this.value='';"/>
			<a href="javascript:onClick=selectDay(document.gotoDateForm.Date.value)"><img src="<%=btOk%>" border="0" align="top" alt="OK"/></a>
		 </form>
	</div>
	<div id="today">
		<a href="javascript:onClick=selectDay('<%=resources.getInputDate(new java.util.Date())%>')"><%=agenda.getString("aujourdhui")%></a>
	</div>
	<% if (agenda.isOtherAgendaMode()) { %>
		<div id="others">
			<%=resources.getStringWithParams("userAgenda", agenda.getAgendaUserDetail().getDisplayedName())%>
			&nbsp;<a href="<%=link%>"><img src="<%=iconLink%>" border="0" alt="<%=resources.getString("agenda.CopyAgendaLink")%>" title="<%=resources.getString("agenda.CopyAgendaLink")%>"/></a>
		</div>
	<% } %>
</div>
<div id="agendaView">
 <%
      AgendaHtmlView view = agenda.getCurrentHtmlView();
      String html =  view.getHtmlView();
      out.println(html);
 %>
 </div>
	<div id="footer">
		<div id="categories">
			<form name="categoryForm" action="agenda.jsp" method="post">
				<input type="hidden" name="Action" value="ViewCategory"/>
				<span class="txtnav"><%=agenda.getString("categories")%> : </span>
                <select name="Category" onchange="document.categoryForm.submit();">
                  <option value="0"><%=agenda.getString("toutesCategories")%></option>
<%
                Collection categories = agenda.getAllCategories();
                Iterator i = categories.iterator();
                while (i.hasNext()) {
                  Category category = (Category) i.next();
                  boolean selected = false;
                  if (agenda.getCategory() != null) {
                    if (agenda.getCategory().getId().equals(category.getId())) {
                      selected = true;
                    }
                  }
                  if (selected) {
                    out.println("<option selected=\"selected\" value=\""+category.getId()+"\">" + category.getName() + "</option>");
                  } else {
                    out.println("<option value=\""+category.getId()+"\">" + category.getName() + "</option>");
                  }
                }
%>
                  </select>
             </form>
		</div>
		<div id="invitations">
			<form name="participationForm" action="agenda.jsp" method="post">
				<input type="hidden" name="Action" value="ViewParticipation"/>
				<span class="txtnav"><%=agenda.getString("participations")%> : </span>
				<select name="Participation" onchange="document.participationForm.submit();">
<%
                String[] participations = ParticipationStatus.getJournalParticipationStatus();

                for (int pi = 0; pi < participations.length; pi++) {
                  boolean selected = false;
                  if (agenda.getParticipationStatus().getString().equals(participations[pi]) )
                    selected = true;
                  if (selected)
                    out.println("<option selected=\"selected\" value=\""+participations[pi]+"\">" + agenda.getString("participations" + participations[pi]) + "</option>");
                  else
                    out.println("<option value=\""+participations[pi]+"\">" + agenda.getString("participations" + participations[pi]) + "</option>");
                }
%>
                  </select>
            </form>
		</div>
		<% if (agenda.hasTentativeSchedulables()) { %>
			<div id="alert">
				<a href="javascript:onClick=viewTentative()"><img name="addnote" border="0" src="icons/alarm_bell.gif" alt="Voir les invitations" title="Voir les invitations"/></a>
			</div>
		<% } %>
		<% if (agenda.isOtherAgendaMode() && (agenda.getCurrentDisplayType()==AgendaHtmlView.BYDAY || agenda.getCurrentDisplayType()==AgendaHtmlView.BYWEEK)) {	%>
			<div id="caption">
				<table align="center" cellpadding="0">
					<tr>
						<td width="12">&nbsp;</td>
						<td class="publicEventOutline" width="12">&nbsp;</td>
						<td>
							&nbsp;<%=agenda.getString("publicEventLabel")%>
						</td>
						<td width="12">&nbsp;</td>
						<td class="privateEventOutline" width="12">&nbsp;</td>
						<td>
							&nbsp;<%=agenda.getString("privateEventLabel")%>
						</td>
					</tr>
				</table>
			</div>
		<% } %>
		<div id="rss">
			<% if (StringUtil.isDefined(rssURL)) { %>
				<a href="<%=m_context+rssURL%>"><img src="<%= m_context+"/util/icons/rss.gif"%>" alt="rss"/></a>
			<% } %>
			<% if (!agenda.isOtherAgendaMode()) { %>
				<a href="<%=m_context+subscribeAgendaUrl%>" title="<%=agenda.getString("agenda.Subscribe")%>"><img align="top" src="icons/ical.gif" alt="iCal"/></a>
			<% } %>
		</div>
	</div>

<%
      out.println(frame.printAfter());
      out.println(window.printAfter());
%>

<form name="agendaForm" method="post" action="">
  <input type="hidden" name="Action"/>
  <input type="hidden" name="Day"/>
</form>

<form name="journalForm" method="post" action="">
  <input type="hidden" name="JournalId"/>
  <input type="hidden" name="Hour"/>
  <input type="hidden" name="Action"/>
</form>

<form name="mainForm" action="ViewCurrentAgenda" method="post">
</form>

</body>
</html>
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

<%@ page import="org.silverpeas.core.web.tools.agenda.model.CalendarImportSettings"%>

<%@ include file="checkAgenda.jsp" %>

<%
	CalendarImportSettings importSettings = (CalendarImportSettings) request.getAttribute("ImportSettings");
	String action = "updateImportSettings";
	if (importSettings == null) {
		action = "saveImportSettings";
		importSettings = new CalendarImportSettings();
		importSettings.setHostName( request.getRemoteHost() );
	}

    SettingBundle settings = agenda.getSettings();
	Window window = graphicFactory.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"));
	browseBar.setPath(agenda.getString("agenda.outlookImportOptions"));
	Frame frame = graphicFactory.getFrame();

    ButtonPane buttonPane = graphicFactory.getButtonPane();
    buttonPane.addButton((Button) graphicFactory.getFormButton(agenda.getString("valider"), "javascript:onClick=document.importSettingsForm.submit();", false));
    buttonPane.addButton((Button) graphicFactory.getFormButton(agenda.getString("annuler"), "Main", false));

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript">
function updateHostName()
{
	document.importSettingsForm.hostName.value = '<%=request.getRemoteHost()%>';
}
</script>
</head>
<body id="agenda">
<%= window.printBefore() %>
<%= frame.printBefore() %>
<%= board.printBefore() %>

<form method="post" name="importSettingsForm" action="<%=action%>">
	<table border="0" cellspacing="0" cellpadding="5" width="100%">

	<%-- Synchro type --%>
    <tr>
        <td align="left" valign="baseline" class="txtlibform" nowrap="nowrap"><%=agenda.getString("agenda.synchroType")%> :</td>
        <td align="left" valign="middle" width="100%">
        <%
		if ("yes".equals(settings.getString("importOutlookCalendarAvailable")))
		{  %>
			<input name="synchroType" type="radio" value="<%=CalendarImportSettings.TYPE_OUTLOOK_IMPORT%>" <%= ( importSettings.getSynchroType() == CalendarImportSettings.TYPE_OUTLOOK_IMPORT ) ? "checked=\"checked\"" : ""%>/>&nbsp;<%=agenda.getString("agenda.outlookSynchro")%><br/>
		<%
		}

		if ("yes".equals(settings.getString("importNotesCalendarAvailable")))
		{  %>
			<input name="synchroType" type="radio" value="<%=CalendarImportSettings.TYPE_NOTES_IMPORT%>" <%= ( importSettings.getSynchroType() == CalendarImportSettings.TYPE_NOTES_IMPORT ) ? "checked=\"checked\"" : ""%>/>&nbsp;<%=agenda.getString("agenda.notesSynchro")%><br/>
		<%
		} %>
			<input name="synchroType" type="radio" value="<%=CalendarImportSettings.TYPE_NO_IMPORT%>" <%= ( importSettings.getSynchroType() == CalendarImportSettings.TYPE_NO_IMPORT ) ? "checked=\"checked\"" : ""%>/>&nbsp;<%=agenda.getString("agenda.noSynchro")%>
        </td>
    </tr>

	<%-- Synchro delay --%>
    <tr>
        <td align="left" valign="baseline" class="txtlibform" nowrap="nowrap"><%=agenda.getString("agenda.synchroDelay")%> :</td>
        <td align="left" valign="baseline">
		<input name="synchroDelay" type="text" size="3" maxlength="3" value="<%=importSettings.getSynchroDelay()%>"/>&nbsp;&nbsp; <%=agenda.getString("agenda.minutes")%>
        </td>
    </tr>

	<%-- Synchro hostName --%>
    <tr>
        <td align="left" valign="baseline" class="txtlibform" nowrap="nowrap"><%=agenda.getString("agenda.hostName")%> :</td>
        <td align="left" valign="baseline">
		<input name="hostName" type="text" size="20" maxlength="50" value="<%=importSettings.getHostName()%>"/>&nbsp;&nbsp; <input type="button" value="<%=request.getRemoteHost()%>" onclick="updateHostName()"/>
        </td>
    </tr>

  </table>
</form>
<%= board.printAfter() %>
<br/><center>
<%= buttonPane.print() %>
</center>
<%= frame.printAfter() %>
<%= window.printAfter() %>
</body>
</html>
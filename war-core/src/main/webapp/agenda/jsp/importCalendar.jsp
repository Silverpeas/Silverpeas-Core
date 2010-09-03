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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.agenda.model.CalendarImportSettings"%>

<%@ include file="checkAgenda.jsp.inc" %>

<%
	CalendarImportSettings importSettings = (CalendarImportSettings) request.getAttribute("ImportSettings");
	boolean doSynchro = ( (importSettings!=null) && (importSettings.getHostName().equals( request.getRemoteHost() ) ) );
	boolean doSynchroNotes = ( doSynchro && (importSettings.getSynchroType() == CalendarImportSettings.TYPE_NOTES_IMPORT) );
	boolean doSynchroOutlook = ( doSynchro && (importSettings.getSynchroType() == CalendarImportSettings.TYPE_OUTLOOK_IMPORT) );
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<% if ( doSynchro ) {%>
	<meta http-equiv="refresh" content="<%=importSettings.getSynchroDelay() * 60%>; URL=<%=m_context + "/Ragenda/jsp/importCalendar"%>"/>
<% } %>
<title></title>
</head>

<body id="agenda">
<%
	if ( doSynchroOutlook ) {%>

    	<applet code="com.silverpeas.importCalendar.importOutlook.AppletImportEvents.class" archive="<%=request.getScheme()%>://<%=request.getServerName()%>:<%=request.getServerPort()%>/weblib/applets/importOutlook.jar" width="1" height="1">
    		<param name="SESSIONID" value="<%=session.getId()%>"/>
    		<param name="SERVLETURL" value="<%=request.getScheme()%>://<%=request.getServerName()%>:<%=request.getServerPort()+m_context%>/ImportCalendar/"/>
			Votre navigateur ne supporte pas les applets.
        </applet>
<% } %>
</body>
</html>
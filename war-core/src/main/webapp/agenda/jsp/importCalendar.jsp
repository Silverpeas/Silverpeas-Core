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
<HTML>
<HEAD>
<% if ( doSynchro ) {%>
	<meta http-equiv="refresh" content="<%=importSettings.getSynchroDelay() * 60%>; URL=<%=m_context + "/Ragenda/jsp/importCalendar"%>">
<% } %>
<TITLE></TITLE>
</HEAD>

<BODY id="agenda">

<%
	if ( doSynchroOutlook ) {%>

    	<APPLET Code="com.silverpeas.importCalendar.importOutlook.AppletImportEvents.class" Archive="<%=request.getScheme()%>://<%=request.getServerName()%>:<%=request.getServerPort()%>/weblib/applets/importOutlook.jar" Width=1 Height=1>
    		<param name="SESSIONID" value="<%=session.getId()%>">
    		<param name="SERVLETURL" value="<%=request.getScheme()%>://<%=request.getServerName()%>:<%=request.getServerPort()+m_context%>/ImportCalendar/">
			Votre navigateur ne supporte pas les applets.
        </APPLET>
<% } %>
</BODY>
</HTML>

<%@ include file="checkAgenda.jsp.inc" %>

<%

  ResourceLocator settings = agenda.getSettings();
	String statusMessage = "";
	boolean exportDone = false;
	String calendarIcsFileName = "";
	String urlFileCalendar = "";
	
	if (StringUtil.isDefined((String) request.getAttribute("ExportReturnCode")))
	{
		exportDone = true;
		String returnCode = (String) request.getAttribute("ExportReturnCode");
		if (AgendaSessionController.EXPORT_SUCCEEDED.equals(returnCode))
		{
			 statusMessage = resources.getString("agenda.ExportSucceeded");
			 calendarIcsFileName = AgendaSessionController.AGENDA_FILENAME_PREFIX + agenda.getUserId() + ".ics";
			 urlFileCalendar = FileServerUtils.getUrlToTempDir(calendarIcsFileName, calendarIcsFileName, "text/calendar");
		 }
		else
			 statusMessage = resources.getString("agenda.ExportEmpty");
	}
%>


<%@page import="com.stratelia.webactiv.servlets.TempFileServer"%><HTML>
<HEAD>
<%
out.println(graphicFactory.getLookStyleSheet());
%>
<TITLE></TITLE>
<link href="<%=m_context%>/util/styleSheets/modal-message.css" rel="stylesheet"  type="text/css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/modal-message.js"></script>
<Script language="JavaScript">
	function calendar(indexForm, nameElement) {
		SP_openWindow('<%=m_context+URLManager.getURL(URLManager.CMP_AGENDA)%>calendar.jsp?indiceForm='+indexForm+'&nameElem='+nameElement,'Calendrier',200,200,'');
	}
		messageObj = new DHTML_modalMessage();	// We only create one object of this class
		messageObj.setShadowOffset(5);	// Large shadow

		function displayStaticMessage()
		{
			messageObj.setHtmlContent("<center><table border=0><tr><td align=\"center\"><br><b><%=resources.getString("agenda.ExportInProgress")%></b></td></tr><tr><td><br/></td></tr><tr><td align=\"center\"><img src=\"<%=m_context%>/util/icons/inProgress.gif\"/></td></tr></table></center>");
			messageObj.setSize(200,150);
			messageObj.setCssClassMessageBox(false);
			messageObj.setShadowDivVisible(true);	// Disable shadow for these boxes
			messageObj.display();
		}

		function closeMessage()
		{
			messageObj.close();
		}
		
		function exportIcal()
		{
			displayStaticMessage();
			setTimeout("document.exportIcalForm.submit();", 500);
		}
	
</script>
</HEAD>
	  
<BODY id="agenda">
<%
	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"));
	browseBar.setPath(resources.getString("agenda.ExportIcalCalendar"));
	out.println(window.printBefore());
  Frame frame = graphicFactory.getFrame();
  
  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
<CENTER>
<form name="exportIcalForm" action="ExportIcal" METHOD=POST>
<% if (exportDone) { %>
	<table width="100%" cellpadding="2" cellspacing="2" border="0">
		<tr>
			<td align="center"><span class="txtlibform"><%=statusMessage%></span></td>
	    </tr>
	    <tr>
	    	<td align="center"><a href="<%=urlFileCalendar%>"><%=calendarIcsFileName%></a>
	    </tr>
	 </table>
<% } else { %>
     <table width="100%" cellpadding="2" cellspacing="2" border="0">
		    <tr>
		    	<td align="left">
		    		<span class="txtlibform"><%=resources.getString("dateDebutNote")%></span> :
					    <input type="text" name="StartDate" size="14" maxlength="<%=DBUtil.DateFieldLength%>">
					      &nbsp;<a href="javascript:onClick=calendar('0','StartDate')"><img src="icons/calendrier.gif" width="13" height="15" border="0" alt="<%=resources.getString("afficherCalendrier")%>" align=absmiddle title="<%=resources.getString("afficherCalendrier")%>"></a>
				      <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
				  </td>
		    	<td align="left">
		    	<span class="txtlibform"><%=resources.getString("dateFinNote")%></span> :
						    <input type="text" name="EndDate" size="14" maxlength="<%=DBUtil.DateFieldLength%>">
					      &nbsp;<a href="javascript:onClick=calendar('0','EndDate')"><img src="icons/calendrier.gif" width="13" height="15" border="0" alt="<%=resources.getString("afficherCalendrier")%>" align=absmiddle title="<%=resources.getString("afficherCalendrier")%>"></a>
				      <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
				  </td>
		    </tr>
	  </table>
	<% } %>
</form>
<%
	out.println(board.printAfter());

	Button button = null;
	if (exportDone)
		button = graphicFactory.getFormButton(resources.getString("GML.close"), "javascript:window.close()", false);
	else
		button = graphicFactory.getFormButton(resources.getString("GML.validate"), "javascript:exportIcal()", false);
	out.print("<br/><center>"+button.print()+"</center>");
%>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>
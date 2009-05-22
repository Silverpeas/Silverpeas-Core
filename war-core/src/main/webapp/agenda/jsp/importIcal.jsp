<%@ include file="checkAgenda.jsp.inc" %>

<%

  ResourceLocator settings = agenda.getSettings();
	String statusMessage = "";
	boolean importDone = false;
	if (StringUtil.isDefined((String) request.getAttribute("ImportReturnCode")))
	{
		importDone = true;
		%>
		<script language="javascript">
				window.opener.location.href = "<%=agenda.getCurrentViewType()%>";
		</script>
		<%
		String returnCode = (String) request.getAttribute("ImportReturnCode");
		if (AgendaSessionController.IMPORT_SUCCEEDED.equals(returnCode))
		{
			 statusMessage = resources.getString("agenda.ImportSucceeded");
		 }
		else if (AgendaSessionController.IMPORT_FAILED.equals(returnCode))
			 statusMessage = resources.getString("agenda.ImportFailed");
	}
%>

<HTML>
<HEAD>
<%
out.println(graphicFactory.getLookStyleSheet());
%>
<TITLE></TITLE>
<link href="<%=m_context%>/util/styleSheets/modal-message.css" rel="stylesheet"  type="text/css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/modal-message.js"></script>
<Script language="JavaScript">
	
		messageObj = new DHTML_modalMessage();	// We only create one object of this class
		messageObj.setShadowOffset(5);	// Large shadow

		function displayStaticMessage()
		{
			messageObj.setHtmlContent("<center><table border=0><tr><td align=\"center\"><br><b><%=resources.getString("agenda.ImportInProgress")%></b></td></tr><tr><td><br/></td></tr><tr><td align=\"center\"><img src=\"<%=m_context%>/util/icons/inProgress.gif\"/></td></tr></table></center>");
			messageObj.setSize(200,150);
			messageObj.setCssClassMessageBox(false);
			messageObj.setShadowDivVisible(true);	// Disable shadow for these boxes
			messageObj.display();
		}

		function closeMessage()
		{
			messageObj.close();
		}
		
		function importIcal()
		{
			if (document.importIcalForm.fileCalendar.value != "")
			{
				displayStaticMessage();
				setTimeout("document.importIcalForm.submit();", 200);
			}
		}	

</script>
</HEAD>
	  
<BODY id="agenda">
<%
	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"));
	browseBar.setPath(resources.getString("agenda.ImportIcalCalendar"));
	out.println(window.printBefore());
  Frame frame = graphicFactory.getFrame();
  
  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
<CENTER>
<form name="importIcalForm" action="ImportIcal" METHOD="POST" ENCTYPE="multipart/form-data">
<% if (importDone) { %>
             <table width="100%" cellpadding="5" cellspacing="2" border="0">
					      <tr>
						      <td align="center" colspan="2" class="txtlibform">
						      	<%=statusMessage%>
						      </td>
						    </tr>
						 </table>
	<% } else { %>
             <table width="100%" cellpadding="5" cellspacing="2" border="0">
					      <tr>
						      <td align="left" colspan="2">
						      	<span class="txtlibform">
						      		<%=resources.getString("agenda.ImportFileCalendar")%>
						      			<br><br>
												<input type="file" name="fileCalendar" size="50" value="">
												<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5" align=absmiddle>
						      		<br>
						      	</span>
						      </td>
						    </tr>
								<tr>
			            <td colspan="2" nowrap>
								    <span class="txtlnote">(<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5">&nbsp;:&nbsp;<%=resources.getString("GML.requiredField")%>) <img src="icons/1px.gif" width="20" height="1"></span> 
            			</td>
			         </tr>
					  </table>
	<% } %>
</form>
<%
	out.println(board.printAfter());

	Button button = null;
	if (importDone)
		button = graphicFactory.getFormButton(resources.getString("GML.close"), "javascript:window.close()", false);
	else
		button = graphicFactory.getFormButton(resources.getString("GML.validate"), "javascript:importIcal()", false);
	
	out.print("<br/><center>"+button.print()+"</center>");
%>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>
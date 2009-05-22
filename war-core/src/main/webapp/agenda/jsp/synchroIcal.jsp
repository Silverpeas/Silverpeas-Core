<%@ include file="checkAgenda.jsp.inc" %>

<%

  ResourceLocator settings = agenda.getSettings();
	String statusMessage = "";
	boolean synchroDone = false;
	String urlIcalendar = "";
	String loginIcalendar = "";
	String pwdIcalendar = "";
	String charset = settings.getString("defautCharset");
		
	if (StringUtil.isDefined((String) request.getAttribute("UrlIcalendar")))
		urlIcalendar = (String) request.getAttribute("UrlIcalendar");
	if (StringUtil.isDefined((String) request.getAttribute("LoginIcalendar")))
		loginIcalendar = (String) request.getAttribute("LoginIcalendar");
	if (StringUtil.isDefined((String) request.getAttribute("PwdIcalendar")))
		pwdIcalendar = (String) request.getAttribute("PwdIcalendar");
	if (StringUtil.isDefined((String) request.getAttribute("DefautCharset")))
		charset = (String) request.getAttribute("DefautCharset");
		
	if (StringUtil.isDefined((String) request.getAttribute("SynchroReturnCode")))
	{
		synchroDone = true;
		%>
		<script language="javascript">
				window.opener.location.href = "<%=agenda.getCurrentViewType()%>";
		</script>
		<%
		
		String returnCode = (String) request.getAttribute("SynchroReturnCode");
		if (agenda.SYNCHRO_SUCCEEDED.equals(returnCode))
		{
			 statusMessage = resources.getString("agenda.SynchroSucceeded");
		 }
		else
			 statusMessage = resources.getString("agenda.SynchroFailed");
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
			messageObj.setHtmlContent("<center><table border=0><tr><td align=\"center\"><br><b><%=resources.getString("agenda.SynchroInProgress")%></b></td></tr><tr><td><br/></td></tr><tr><td align=\"center\"><img src=\"<%=m_context%>/util/icons/inProgress.gif\"/></td></tr></table></center>");
			messageObj.setSize(200,150);
			messageObj.setCssClassMessageBox(false);
			messageObj.setShadowDivVisible(true);	// Disable shadow for these boxes
			messageObj.display();
		}

		function closeMessage()
		{
			messageObj.close();
		}
		
		function synchroIcal()
		{
			if (document.synchroIcalForm.UrlIcalendar.value.indexOf("http") == 0) 
			{
				displayStaticMessage();
				setTimeout("document.synchroIcalForm.submit();", 200);
			}
		}
		
</script>
</HEAD>
	  
<BODY id="agenda">
<%
	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"));
	browseBar.setPath(resources.getString("agenda.SynchroIcalCalendar"));
	out.println(window.printBefore());
  Frame frame = graphicFactory.getFrame();
  
  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
<CENTER>
<form name="synchroIcalForm" action="SynchroIcal" METHOD="POST">
<% if (synchroDone) { %>
             <table width="100%" cellpadding="5" cellspacing="2" border="0">
					      <tr>
						      <td align="center" colspan="2">
						      	<span class="txtlibform"><%=statusMessage%></span>
						      </td>
						    </tr>
						 </table>
	<% } else { %>
             <table width="100%" cellpadding="5" cellspacing="2" border="0">
					      <tr>
						      <td align="left">
						      	<span class="txtlibform"><%=resources.getString("agenda.SynchroRemoteUrl")%></span>
						      </td>
						      <td>
						      	<input maxlength="200" size="80" type="text" name="UrlIcalendar" value="<%=urlIcalendar%>">
										<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5" align=absmiddle>
						      </td>
						    </tr>
					      <tr>
						      <td align="left">
						      	<span class="txtlibform"><%=resources.getString("agenda.SynchroRemoteLogin")%></span>
						      </td>
						      <td>
						      	<input maxlength="50" size="50" type="text" name="LoginIcalendar" value="<%=loginIcalendar%>">
						      </td>
						    </tr>
					      <tr>
						      <td align="left">
						      	<span class="txtlibform"><%=resources.getString("agenda.SynchroRemotePwd")%></span>
						      </td>
						      <td>
						      	<input maxlength="50" size="50" type="password" name="PwdIcalendar" value="<%=pwdIcalendar%>">
						      </td>
						    </tr>
					      <tr>
						      <td align="left">
						      	<span class="txtlibform"><%=resources.getString("agenda.SynchroCharset")%></span>
						      </td>
						      <td>
						      	<select name="Charset">
						      		<option value="ISO-8859-1">ISO-8859-1
						      		<option value="UTF-8">UTF-8
						      		<option value="US-ASCII">US-ASCII
						      	</select>
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
	out.print(board.printAfter());
	ButtonPane buttonPane = graphicFactory.getButtonPane();
	if (!synchroDone)
	{
		Button buttonValidate = graphicFactory.getFormButton(resources.getString("GML.validate"), "javascript:synchroIcal();", false);
		buttonPane.addButton(buttonValidate);
	}
	Button buttonClose = graphicFactory.getFormButton(resources.getString("GML.close"), "javascript:window.close();", false);
	buttonPane.addButton(buttonClose);
	out.print("<br/><center>"+buttonPane.print()+"</center>");

%>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>
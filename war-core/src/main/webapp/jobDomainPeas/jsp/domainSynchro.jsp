<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    Domain domObject = (Domain)request.getAttribute("domainObject");

    browseBar.setDomainName(resource.getString("JDP.jobDomain"));
    browseBar.setComponentName(Encode.javaStringToHtmlString((String)request.getAttribute("domainName")), (String)request.getAttribute("domainURL"));
    browseBar.setPath(resource.getString("JDP.domainSynchro") + "...");
%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
</head>
<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<script language='JavaScript'>
function ValidForm(){
	top.scriptFrame.SP_openWindow('<%=m_context %>/RjobDomainPeas/jsp/displayDynamicSynchroReport?IdTraceLevel=' + document.domainForm.IdTraceLevel.value, 'SynchroReport', '750', '550', 'menubar=yes,scrollbars=yes,statusbar=yes,resizable=yes');
	document.domainForm.submit()
}
</script>
<form name="domainForm" action="domainSynchro" method="POST">
    <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
                    <tr>			
                        <td valign="baseline" align=left  class="txtlibform">
                            <%=resource.getString("GML.name")%> :
                        </td>
                        <td align=left valign="baseline">
                            <input type="text" name="domainName" size="50" maxlength="20" VALUE="<%=Encode.javaStringToHtmlString(domObject.getName())%>">
                        </td>
                    </tr>
                    <tr>			
                        <td valign="baseline" align=left  class="txtlibform">
                            <%=resource.getString("GML.description")%> :
                        </td>
                        <td align=left valign="baseline">
                            <input type="text" name="domainDescription" size="50" maxlength="20" VALUE="<%=Encode.javaStringToHtmlString(domObject.getDescription())%>"> 
                        </td>
                    </tr>
			<tr>
				<td valign="baseline" align=left  class="txtlibform">
                          <%=resource.getString("JDP.traceLevel")%> :
		        </td>
				<td valign="baseline" align=left>
				 <select name="IdTraceLevel" size="1">
					<option value=<%=Integer.toString(SynchroReport.TRACE_LEVEL_DEBUG)%>>Debug</option>
					<option value=<%=Integer.toString(SynchroReport.TRACE_LEVEL_INFO)%>>Info</option>
					<option value=<%=Integer.toString(SynchroReport.TRACE_LEVEL_WARN)%> selected>Warning</option>
					<option value=<%=Integer.toString(SynchroReport.TRACE_LEVEL_ERROR)%>>Error</option>
	             </select>
				</td>
			</tr>
    </table>
<%
out.println(board.printAfter());
%>
</form>
		<%
		  ButtonPane bouton = gef.getButtonPane();
		  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:ValidForm()", false));
          bouton.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "domainContent", false));
		  out.println(bouton.print());
		%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
	%>

</body>
</html>
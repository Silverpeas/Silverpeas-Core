<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ include file="check.jsp" %>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>

<%
	PairObject hostComponentNameObject = (PairObject) request.getAttribute("HostComponentName");
	String hostSpaceName = (String) request.getAttribute("HostSpaceName");
	String hostComponentName = (String) hostComponentNameObject.getFirst(); 
%>

<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>

<%
	browseBar.setDomainName(hostSpaceName);
	browseBar.setComponentName(hostComponentName);

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>



<%
Button closeButton = (Button) gef.getFormButton(resource.getString("GML.close"), "javascript:onClick=window.close();", false);
%>

<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr><td>
		<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
			<tr><td>
				<TABLE border=0 cellPadding=1 cellSpacing=1 width="389" align="center">
					<TR>
						<TD width="369" align="center"><%=Encode.javaStringToHtmlString(resource.getString("AlertsConfirmation"))%></TD>
					</TR>
				</TABLE>
			</td></tr>
		</table>
	</td></tr>
</table>
<%		
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(closeButton);
	buttonPane.setHorizontalPosition();
	out.println(frame.printMiddle());
	out.println("<BR><center>"+buttonPane.print()+"<br></center>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</BODY>
</HTML>

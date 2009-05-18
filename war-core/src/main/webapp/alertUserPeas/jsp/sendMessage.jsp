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
	String componentURL = (String)request.getAttribute("myComponentURL");
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
<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr><td>
		<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
			<tr><td>
				<center><%=resource.getString("AlertInProgress")%></center>
			</td></tr>
		</table>
	</td></tr>
</table>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>

<script>location.replace("<%=componentURL%>Notify");</script>
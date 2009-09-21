<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	String	 	url		= (String) request.getAttribute("Url");

	String sURI = request.getRequestURI();
	String sRequestURL = HttpUtils.getRequestURL(request).toString();
	String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
	
	// déclaration des boutons
    Button exitButton = (Button) gef.getFormButton(resource.getString("GML.ok"), "javascript:window.close()", false);
	
%>

<html>
<head>

<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>		
<script language="javascript">
</script>
		
</head>
<body>
<%
	browseBar.setComponentName(resource.getString("fileSharing.tickets") + " > " + resource.getString("fileSharing.confirmTicket"));
		
	Board board	= gef.getBoard();
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<table CELLPADDING=5 WIDTH="100%">
	<tr>
		<td class="txtlibform" nowrap><%=resource.getString("fileSharing.url")%></td>
		<td><a href="<%=m_sAbsolute+url%>" target="_blank"><%=m_sAbsolute+url%></a></td>
	</tr>
</table>
<% 
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(exitButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
 	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>
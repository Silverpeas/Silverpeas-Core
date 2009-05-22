<%@ include file="check.jsp" %>

<% 
	// déclaration des boutons
	Button validateButton = (Button) gef.getFormButton(resource.getString("GML.close"), "javascript:window.close()", false);
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>		
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	browseBar.setComponentName(resource.getString("myLinks.links") + resource.getString("myLinks.createLink"));
		
	Board board	= gef.getBoard();
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<table CELLPADDING=5 WIDTH="100%">
	<tr>
		<td class="txtlibform"> <%=resource.getString("myLinks.messageConfirm")%> </td>
	</tr>
</table>
<% 
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
 	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>
<%@ include file="check.jsp" %>
<%
Boolean displayOperations = (Boolean) request.getAttribute("DisplayOperations");

Board board = gef.getBoard(); 
%>

<HTML>
<HEAD>
<%
	out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%

if (displayOperations.booleanValue())
{
	operationPane.addOperation(resource.getIcon("JDP.domainAdd"), resource.getString("JDP.domainAdd"), "displayDomainCreate");
	operationPane.addOperation(resource.getIcon("JDP.domainSqlAdd"), resource.getString("JDP.domainSQLAdd"), "displayDomainSQLCreate");
}

out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<div align="center" class="txtNav"><%=resource.getString("JDP.welcome") %></div>
<%
out.println(board.printAfter());
%>
</center>
<% 
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>
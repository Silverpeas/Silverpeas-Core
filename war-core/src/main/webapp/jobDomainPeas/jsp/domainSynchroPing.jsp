<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    browseBar.setDomainName(resource.getString("JDP.jobDomain"));
    browseBar.setComponentName(Encode.javaStringToHtmlString((String)request.getAttribute("domainName")));
    browseBar.setPath(resource.getString("JDP.domainSynchroReport") + "...");
%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<Script language="JavaScript">
ID = window.setTimeout ("DoIdle(10);", 10000);
function DoIdle()
{
    self.location.href = "domainPingSynchro";
}
</script>
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
<%=resource.getString("JDP.synchroEnCours")%>
<%
out.println(board.printAfter());
%>
<br>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
	%>

</body>
</html>
<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    browseBar.setDomainName(resource.getString("JDP.jobDomain"));
    browseBar.setComponentName(Encode.javaStringToHtmlString((String)request.getAttribute("domainName")), (String)request.getAttribute("domainURL"));
    browseBar.setPath(resource.getString("JDP.domainSynchroReport") + "...");
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
<TEXTAREA NAME="Report" ROWS="20" COLS="110">
<%=(String)request.getAttribute("SynchroReport")%>
</TEXTAREA>
<%
out.println(board.printAfter());
%>
<br>
		<%
		  ButtonPane bouton = gef.getButtonPane();
          bouton.addButton((Button) gef.getFormButton(resource.getString("GML.close"), "domainRefresh", false));
		  out.println(bouton.print());
		%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
	%>

</body>
</html>
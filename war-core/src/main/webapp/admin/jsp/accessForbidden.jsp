<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ include file="import.jsp" %>

<HTML>
<HEAD>
<TITLE><%= generalMessage.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>

<%
	Window window = gef.getWindow();
	Frame frame = gef.getFrame();
	Board board = gef.getBoard();
	
	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
	<br/>
	<center>
		<h3><%=generalMessage.getString("GML.ForbiddenAccessContent")%></h3>
	</center>
<%
	out.println(board.printAfter());
	
	Button back = (Button) gef.getFormButton(generalMessage.getString("GML.back"), "javascript:onClick=history.go(-1);", false);
	out.println("<br/><center>"+back.print()+"</center><br/>");
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY> 
</html>
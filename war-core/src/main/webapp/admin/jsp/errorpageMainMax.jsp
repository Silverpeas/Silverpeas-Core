<%--
 % This page is invoked when an error happens at the server.  The
 % error details are available in the implicit 'exception' object.
 % We set the error page to this file in each of our screens.
 % (via the template.jsp)
--%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.stratelia.webactiv.util.*"%>

<%@ include file="import.jsp" %>

<%
//Récupération des paramètres
String message = (String) request.getParameter("message");
String detailedMessage = (String) request.getParameter("detailedMessage");
String pile = (String) request.getParameter("pile");
%>
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
					out.println(window.printBefore());
					Frame frame = gef.getFrame();
					out.println(frame.printBefore());
%>
<CENTER>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<form name="formError" action="<%=m_context%>/admin/jsp/errorpageMainMin.jsp" method="post">
	<input type="Hidden" name="message" value="<%=message %>">
  <input type="Hidden" name="detailedMessage" value="<%=detailedMessage %>">
	<input type="Hidden" name="pile" value="<%=pile %>">
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
			<center>
				<br>
				<span class="txtnav">
				<%=detailedMessage %>
				</span>
				<br><br>
				<textarea rows="12" cols="90" wrap="virtual" name="pile"><%=pile %></textarea>
			</center>
			<br>
		</td>
	</tr>
	</form>
</table>
<br>
<%
		  ButtonPane buttonPane = gef.getButtonPane();
			buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.minimize"), "javascript:document.formError.submit();", false));
		  out.println(buttonPane.print());
%>
</CENTER>
<%
				out.println(frame.printAfter());
				out.println(window.printAfter());
%>
</BODY>
</HTML>
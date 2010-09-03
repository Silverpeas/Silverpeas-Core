<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>

<% 
	// r�cup�ration des param�tres :
	String	 	url		= (String) request.getAttribute("Url");

	String sURI = request.getRequestURI();
	String sRequestURL = HttpUtils.getRequestURL(request).toString();
	String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
	
	// d�claration des boutons
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
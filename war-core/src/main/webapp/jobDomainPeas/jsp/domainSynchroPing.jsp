<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/legal/licensing"

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
	Domain domObject = (Domain)request.getAttribute("domainObject");

    Board board = gef.getBoard();

    browseBar.setComponentName(getDomainLabel(domObject, resource));
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
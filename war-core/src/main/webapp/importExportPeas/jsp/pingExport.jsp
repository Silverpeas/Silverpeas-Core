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
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<Script language="JavaScript">
function ping()
{
	window.setTimeout("doIdle();", 5000);	
}

function doIdle()
{
    self.location.href = "/silverpeas/RimportExportPeas/jsp/ExportItemsPing";
}
</script>
</head>
<body onload="ping()">
<%
browseBar.setComponentName(resource.getString("importExportPeas.Export"));

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<center>
<table>
<tr><td class="txtlibform"><blink><%=resource.getString("importExportPeas.InProgress")%></blink></td></tr>
</table>
</center>
<%
out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
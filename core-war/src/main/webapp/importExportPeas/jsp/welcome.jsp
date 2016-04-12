<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<html>
<head>
<view:looknfeel/>
<script language="javascript">
function launchImport()
{
	document.imgProgress.src = "<%=resource.getIcon("importExportPeas.progress")%>";
	setTimeout("submitForm()", 500);
}
function submitForm()
{
	document.xmlForm.submit();
}
</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
browseBar.setComponentName(resource.getString("importExportPeas.Importation"));

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<table>
<form name="xmlForm" method="POST" action="Import" enctype="multipart/form-data">
<tr><td class="txtlibform"><%=resource.getString("importExportPeas.ImportDescriptor")%> :</td><td><input type="file" name="xmlFile" size="30"></td></tr>
</form>
</table>
<center><img src="<%=resource.getIcon("importExportPeas.1px")%>" border="0" name="imgProgress"></center>
<%
out.println(board.printAfter());
out.println(frame.printMiddle());
ButtonPane buttonPane = gef.getButtonPane();
Button button = gef.getFormButton(resource.getString("importExportPeas.Import"), "javaScript:launchImport();", false);
buttonPane.addButton(button);
out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
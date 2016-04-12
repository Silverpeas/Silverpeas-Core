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
<%@ include file="checkPdc.jsp"%>
<%
	String infoMessage = (String)request.getAttribute("infoMessage");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title><%=resource.getString("GML.popupTitle")%></title>
	<view:looknfeel/>
	<script type="text/javascript">
		function refresh() {
			try {<%

	if ("true".equals(request.getParameter("pdcFieldMode"))) {
		String fieldName = (String)request.getAttribute("pdcFieldName");
		String positions = (String)request.getAttribute("pdcFieldPositions");
%>
				window.opener.updatePositions_<%=fieldName%>("<%=positions%>");<%

	} else {
%>

				window.opener.document.toComponent.submit();<%

	}
%>
			} catch (e) {
				//opening window does not contain toComponent form
			}<%

	if (infoMessage != null) {
%>
			window.setTimeout("window.close();", 1500);<%

	} else {
%>
			window.close();<%

	}
%>
		}
	</script>
</head>

<body onload="refresh()"><%

	if (infoMessage != null) {
		browseBar.setDomainName(spaceLabel);
		browseBar.setComponentName(componentLabel);

	    out.println(window.printBefore());
	    out.println(frame.printBefore());
	    out.println(board.printBefore());
%>
	<center>
		<table width="100%" border="0" cellspacing="0" cellpadding="4">
			<tr>
				<td><%=infoMessage%></td>
			</tr>
		</table>
	</center><%

		out.println(board.printAfter());
		out.println(frame.printAfter());
		out.println(window.printAfter());
	}
%>
</body>
</html>
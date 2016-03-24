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

<%@page import="org.silverpeas.core.admin.user.model.UserDetail"%>

<%
UserDetail user = (UserDetail) request.getAttribute("UserDetail");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<script language="JavaScript">

function refresh()
{
	<% if (user != null) { %>
		/*var listUsers = window.opener.document.getElementById("listUsers");
		var option = window.opener.document.createElement("option");
		option.setAttribute("value", "<%=user.getId()%>");
		option.setAttribute("selected", "selected");
		option.innerHTML = "<%=user.getDisplayedName()%>";
		listUsers.appendChild(option);

		var callUserPanel = window.opener.document.getElementById("callUserPanel");

		var userIcon = window.opener.document.getElementById("userIcon");
		callUserPanel.removeChild(userIcon);

		var img = window.opener.document.createElement("img");
		img.setAttribute("src", "<%=m_context + "/util/icons/user.gif"%>");
		img.setAttribute("id", "userIcon");
		img.setAttribute("alt", "<%=resource.getString("pdcPeas.openUserPanelPeas")%>");
		img.setAttribute("align", "absmiddle");
		img.setAttribute("border", "0");
		callUserPanel.appendChild(img);*/

		var userName = window.opener.document.getElementById("userName");
		userName.innerHTML = "<%=user.getDisplayedName()%>";

		var userId = window.opener.document.getElementById("userId");
		userId.setAttribute("value", "<%=user.getId()%>");

		window.opener.document.getElementById("deleteURL").style.visibility = "visible";

	<% } %>

	window.close();
}
</script>
</HEAD>
<BODY onLoad="refresh()">
</BODY>
</HTML>
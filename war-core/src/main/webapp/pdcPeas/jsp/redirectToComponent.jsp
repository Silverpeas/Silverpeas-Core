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

<%@ include file="checkPdc.jsp"%>
<%
	String				toURL				= (String) request.getAttribute("ToURL");
	String				windowLocation		= m_context+toURL;

	ContainerContext	containerContext	= (ContainerContext) request.getAttribute("ContainerContext");
	String				returnURL			= "";
	if (containerContext != null) {
		returnURL = containerContext.getReturnURL();
	}
	if (returnURL != null && returnURL.length() > 0)
		windowLocation += "&ReturnURL="+returnURL;
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">
function redirect() {
	window.location = "<%=windowLocation%>";
}
</script>
</HEAD>
<BODY onLoad=redirect()>
</BODY>
</HTML>
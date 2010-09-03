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

<%@ page isELIgnored="false"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@page import="com.silverpeas.external.webConnections.model.ConnectionDetail"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%>

<%
	// récupération des paramètres
	ConnectionDetail connection = (ConnectionDetail) request.getAttribute("Connection");	
	String methodType			= (String) request.getAttribute("Method");
%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="javascript">
function sendForm() {
	document.connectionForm.submit();
}
</script>		
</head>
<body onload="javascript:sendForm()">
<form name="connectionForm" action="<%=connection.getUrl()%>" method="<%=methodType%>">
	<%
	Map param = connection.getParam();
	Set keys = param.keySet();
	Iterator iKeys = keys.iterator();
	while (iKeys.hasNext()) {
		String name = (String) iKeys.next();
		String value = (String) param.get(name);
		%>
		<input type="hidden" name="<%=name%>" value="<%=value%>"/>
	<% } %>
</form>
</body>
</html>
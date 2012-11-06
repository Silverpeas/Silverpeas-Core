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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="includeParamsField.jsp.inc" %>
<script language="javascript">
	function isCorrectForm() 
	{
     	checkFieldName();
     	return checkErrors();
	}
</script>
</head>
<body>
<%
	String driverName = "";
	String url = "";
	String login = "";
	String password = "";
	String query = "";
	String valueFieldType = "";
	
	if (field != null)
	{
		if (parameters.containsKey("driverName")) {
			driverName = (String) parameters.get("driverName");
		}
		
		if (parameters.containsKey("url")) {
			url = (String) parameters.get("url");
		}

		if (parameters.containsKey("login")) {
			login = (String) parameters.get("login");
		}

		if (parameters.containsKey("password")) {
			password = (String) parameters.get("password");
		}

		if (parameters.containsKey("query")) {
			query = (String) parameters.get("query");
		}

		if (parameters.containsKey("valueFieldType")) {
			valueFieldType = (String) parameters.get("valueFieldType");
		}
	}
%>
<%@ include file="includeTopField.jsp.inc" %>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.driverName")%> :</td><td><input type="text" name="Param_driverName" value="<%=driverName%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.url")%> :</td><td><input type="text" name="Param_url" value="<%=url%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.login")%> :</td><td><input type="text" name="Param_login" value="<%=login%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.passwordDB")%> :</td><td><input type="text" name="Param_password" value="<%=password%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.query")%> :</td><td><input type="text" name="Param_query" value="<%=query%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.valueFieldType")%> :</td><td><input type="text" name="Param_valueFieldType" value="<%=valueFieldType%>" size="30"/></td>
</tr>
<%@ include file="includeBottomField.jsp.inc" %>
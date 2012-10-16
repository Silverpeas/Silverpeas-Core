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

<%@page import="com.silverpeas.util.StringUtil"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="includeParamsField.jsp.inc" %>

<%
	String roles = "";
	String usersOfInstanceOnlyChecked = "";
	String rows = "";
	String cols = "";
	
	if (field != null) {
	  	if (parameters.containsKey("rows")) {
			rows = (String) parameters.get("rows");
		}
		
		if (parameters.containsKey("cols")) {
			cols = (String) parameters.get("cols");
		}
		
	  	roles = (String) parameters.get("roles");
		if (!StringUtil.isDefined(roles)) {
			roles = "";
		}
		
	  	String usersOfInstanceOnly = (String) parameters.get("usersOfInstanceOnly");
	  	if (StringUtil.getBooleanValue(usersOfInstanceOnly)) {
	  		usersOfInstanceOnlyChecked = "checked";
	  	}
	}
%>

<script type="text/javascript">
	function isCorrectForm() 
	{
     	checkFieldName();
     	return checkErrors();
	}
</script>
</head>
<body>
<%@ include file="includeTopField.jsp.inc" %>
<tr>
<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.usedInstanceOnly")%> :</td><td><input type="checkbox" name="Param_usersOfInstanceOnly" value="true" <%=usersOfInstanceOnlyChecked%>/></td>
</tr>
<tr>
<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.roles")%> :</td><td><input type="text" name="Param_roles" value="<%=roles%>" /></td>
</tr>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.rows")%> :</td><td><input type="text" name="Param_rows" value="<%=rows%>" size="5" maxLength="3"/></td>
</tr>
<tr>
<td class="txtlibform"><%=resource.getString("templateDesigner.cols")%> :</td><td><input type="text" name="Param_cols" value="<%=cols%>" size="5" maxLength="3"/></td>
</tr>
<%@ include file="includeBottomField.jsp.inc" %>
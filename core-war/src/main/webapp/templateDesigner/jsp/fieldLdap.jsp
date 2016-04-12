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

<%@ include file="includeParamsField.jsp" %>
<script language="javascript">
	function isCorrectForm() {
	checkFieldName();
	return checkErrors();
	}
</script>
<%
	String host = "";
	String port = "";
	String version = "";
	String baseDN = "";
	String password = "";
	String searchBase = "";
	String searchScope = "";
	String searchFilter = "";
	String searchAttribute = "";
	String searchTypeOnly = "";
	String maxResultDisplayed = "";
	String valueFieldType = "";

	if (field != null)
	{
		if (parameters.containsKey("host")) {
			host = parameters.get("host");
		}

		if (parameters.containsKey("port")) {
			port = parameters.get("port");
		}

		if (parameters.containsKey("version")) {
			version = parameters.get("version");
		}

		if (parameters.containsKey("baseDN")) {
			baseDN = parameters.get("baseDN");
		}

		if (parameters.containsKey("password")) {
			password = parameters.get("password");
		}

		if (parameters.containsKey("searchBase")) {
			searchBase = parameters.get("searchBase");
		}

		if (parameters.containsKey("searchScope")) {
			searchScope = parameters.get("searchScope");
		}

		if (parameters.containsKey("searchFilter")) {
			searchFilter = parameters.get("searchFilter");
		}

		if (parameters.containsKey("searchAttribute")) {
			searchAttribute = parameters.get("searchAttribute");
		}

		if (parameters.containsKey("searchTypeOnly")) {
			searchTypeOnly = parameters.get("searchTypeOnly");
		}

		if (parameters.containsKey("maxResultDisplayed")) {
			maxResultDisplayed = parameters.get("maxResultDisplayed");
		}

		if (parameters.containsKey("valueFieldType")) {
			valueFieldType = parameters.get("valueFieldType");
		}
	}
%>
<%@ include file="includeTopField.jsp" %>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.host")%> :</td><td><input type="text" name="Param_host" value="<%=host%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.port")%> :</td><td><input type="text" name="Param_port" value="<%=port%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.version")%> :</td><td><input type="text" name="Param_version" value="<%=version%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.baseDN")%> :</td><td><input type="text" name="Param_baseDN" value="<%=baseDN%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.password")%> :</td><td><input type="text" name="Param_password" value="<%=password%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.searchBase")%> :</td><td><input type="text" name="Param_searchBase" value="<%=searchBase%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.searchScope")%> :</td><td><input type="text" name="Param_searchScope" value="<%=searchScope%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.searchFilter")%> :</td><td><input type="text" name="Param_searchFilter" value="<%=searchFilter%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.searchAttribute")%> :</td><td><input type="text" name="Param_searchAttribute" value="<%=searchAttribute%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.searchTypeOnly")%> :</td><td><input type="text" name="Param_searchTypeOnly" value="<%=searchTypeOnly%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.maxResultDisplayed")%> :</td><td><input type="text" name="Param_maxResultDisplayed" value="<%=maxResultDisplayed%>" size="30"/></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.valueFieldType")%> :</td><td><input type="text" name="Param_valueFieldType" value="<%=valueFieldType%>" size="30"/></td>
</tr>
<%@ include file="includeBottomField.jsp" %>
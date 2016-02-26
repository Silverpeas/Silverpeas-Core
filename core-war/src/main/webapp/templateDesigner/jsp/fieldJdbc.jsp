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
	String dataSourceName = "";
	String login = "";
	String password = "";
	String query = "";
	String valueFieldType = "";
	String jdbcDisplayer = "";

	if (field != null)
	{
		if (parameters.containsKey("dataSourceName")) {
      dataSourceName = parameters.get("dataSourceName");
		}

		if (parameters.containsKey("login")) {
			login = parameters.get("login");
		}

		if (parameters.containsKey("password")) {
			password = parameters.get("password");
		}

		if (parameters.containsKey("query")) {
			query = parameters.get("query");
		}

		if (parameters.containsKey("valueFieldType")) {
			valueFieldType = parameters.get("valueFieldType");
		}

		if (parameters.containsKey("displayer")) {
			jdbcDisplayer = parameters.get("displayer");
		}
	}
%>
<%@ include file="includeTopField.jsp" %>
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.dataSourceName")%> :</td><td><input type="text" name="Param_dataSourceName" value="<%=dataSourceName%>" size="30"/></td>
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
<tr>
	<td class="txtlibform"><%=resource.getString("templateDesigner.displayer")%> :</td>
	<td>
		<select name="Param_displayer">
			<option value="autocomplete" <%= "autocomplete".equals(jdbcDisplayer) ? "selected=\"selected\"" : "" %>><%=resource.getString("templateDesigner.displayer.jdbc.autocompletion") %></option>
			<option value="listbox" <%= "listbox".equals(jdbcDisplayer) ? "selected=\"selected\"" : "" %>><%=resource.getString("templateDesigner.displayer.jdbc.listbox") %></option>
		</select>
	</td>
</tr>
<%@ include file="includeBottomField.jsp" %>
<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.kernel.util.StringUtil"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="includeParamsField.jsp" %>

<%
	String scope = "";

	if (field != null) {
		scope = parameters.get("scope");
		if (!StringUtil.isDefined(scope)) {
			scope = "";
		}
	}
%>

<script type="text/javascript">
	function isCorrectForm() {
	checkFieldName();
	if (isWhitespace($('#Param_scope').val())) {
		errorMsg+="  - '<%=resource.getString("templateDesigner.displayer.explorer.scope")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
		errorNb++;
	}
	return checkErrors();
	}
</script>
<%@ include file="includeTopField.jsp" %>
<tr>
<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.explorer.scope")%> :</td><td><input type="text" name="Param_scope" id="Param_scope" value="<%=scope%>" />&nbsp;<img src="<%=resource.getIcon("templateDesigner.mandatory")%>" width="5" height="5"/></td>
</tr>
<%@ include file="includeBottomField.jsp" %>
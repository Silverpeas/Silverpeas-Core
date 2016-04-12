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

<%@page import="org.silverpeas.core.contribution.content.form.field.TextField"%>
<%@page import="org.silverpeas.core.util.StringUtil"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="includeParamsField.jsp" %>

<script language="javascript">
	function isCorrectForm() {
	checkFieldName();
	return checkErrors();
	}
</script>
<%
	String size = "";
	String maxLength = "";
	String suggestionsChecked = "";

	if (field != null) {
		if (parameters.containsKey(TextField.PARAM_MAXLENGTH)) {
			maxLength = parameters.get(TextField.PARAM_MAXLENGTH);
		}

		if (parameters.containsKey("size")) {
			size = parameters.get("size");
		}

		if (parameters.containsKey("suggestions")) {
			String suggestions = parameters.get("suggestions");
			if (StringUtil.getBooleanValue(suggestions)) {
				suggestionsChecked = "checked";
			}
		}
	}
%>
<%@ include file="includeTopField.jsp" %>
<tr>
<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.size")%> :</td><td><input type="text" name="Param_size" value="<%=size%>" size="5" maxLength="3"/></td>
</tr>
<tr>
<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.maxLength")%> :</td><td><input type="text" name="Param_<%=TextField.PARAM_MAXLENGTH %>" value="<%=maxLength%>" size="5" maxLength="3"/></td>
</tr>
<tr>
<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.suggestions")%> :</td><td><input type="checkbox" name="Param_suggestions" value="true" <%=suggestionsChecked%>/></td>
</tr>
<%@ include file="includeBottomField.jsp" %>
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

<%@page import="org.silverpeas.core.contribution.content.form.displayers.EmailFieldDisplayer"%>
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
	String mailto = "";

	if (field != null) {
		if ("true".equalsIgnoreCase(parameters.get(EmailFieldDisplayer.PARAM_MAILTO))) {
			mailto = "checked=\"checked\"";
		}

		if (parameters.containsKey(EmailFieldDisplayer.PARAM_SIZE)) {
			size = parameters.get(EmailFieldDisplayer.PARAM_SIZE);
		}
	}
%>
<%@ include file="includeTopField.jsp" %>
<tr>
<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.size")%> :</td><td><input type="text" name="Param_<%=EmailFieldDisplayer.PARAM_SIZE %>" value="<%=size%>" size="5" maxLength="3"/></td>
</tr>
<tr>
<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.email.mailto")%> :</td><td><input type="checkbox" name="Param_<%=EmailFieldDisplayer.PARAM_MAILTO %>" value="true" <%=mailto%>/></td>
</tr>
<%@ include file="includeBottomField.jsp" %>
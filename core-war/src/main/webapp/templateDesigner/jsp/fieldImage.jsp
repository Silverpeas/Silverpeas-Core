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
	String maxWidth = "";
	String maxHeight = "";
	String galleriesChecked = "";

	if (field != null) {
		if (parameters.containsKey("width")) {
		  maxWidth = parameters.get("width");
		}

		if (parameters.containsKey("height")) {
		  maxHeight = parameters.get("height");
		}

		if (parameters.containsKey("galleries")) {
			String galleries = parameters.get("galleries");
			if ("true".equalsIgnoreCase(galleries)) {
			  galleriesChecked = "checked=\"checked\"";
			}
		}
	}
%>
<%@ include file="includeTopField.jsp" %>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.image.maxWidth")%> :</td><td><input type="text" name="Param_width" value="<%=maxWidth%>" size="5" maxLength="4"/></td>
</tr>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.image.maxHeight")%> :</td><td><input type="text" name="Param_height" value="<%=maxHeight%>" size="5" maxLength="4"/></td>
</tr>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.image.galleries")%> :</td><td><input type="checkbox" name="Param_galleries" value="true" <%=galleriesChecked%>/></td>
</tr>
<%@ include file="includeBottomField.jsp" %>
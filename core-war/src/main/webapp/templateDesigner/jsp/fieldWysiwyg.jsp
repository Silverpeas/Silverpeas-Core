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
<script type="text/javascript">
function isCorrectForm() {
	checkFieldName();

	if($('#galleries').is(':checked')){
		$('#Param_galleries').val("true");
	} else {
		$('#Param_galleries').val("false");
	}

	if($('#fileStorages').is(':checked')){
		$('#Param_fileStorages').val("true");
	} else {
		$('#Param_fileStorages').val("false");
	}
	return checkErrors();
}
</script>
</head>
<body>
<%
	String width = "";
	String height = "";
	String galleriesChecked = "checked=\"checked\"";
	String fileStoragesChecked = "checked=\"checked\"";

	if (field != null) {
		if (parameters.containsKey("width")) {
		  width = parameters.get("width");
		}

		if (parameters.containsKey("height")) {
		  height = parameters.get("height");
		}

		if (parameters.containsKey("galleries")) {
			String galleries = parameters.get("galleries");
			if ("false".equalsIgnoreCase(galleries)) {
			  galleriesChecked = "";
			}
		}

		if (parameters.containsKey("fileStorages")) {
			String fileStorages = parameters.get("fileStorages");
			if ("false".equalsIgnoreCase(fileStorages)) {
			  fileStoragesChecked = "";
			}
		}
	}
%>
<%@ include file="includeTopField.jsp" %>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.wysiwyg.width")%> :</td><td><input type="text" name="Param_width" value="<%=width%>" size="5" maxLength="4"/></td>
</tr>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.wysiwyg.height")%> :</td><td><input type="text" name="Param_height" value="<%=height%>" size="5" maxLength="4"/></td>
</tr>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.wysiwyg.galleries")%> :</td><td><input type="checkbox" id="galleries" <%=galleriesChecked%>/><input type="hidden" name="Param_galleries" id="Param_galleries"/></td>
</tr>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.wysiwyg.fileStorages")%> :</td><td><input type="checkbox" id="fileStorages" <%=fileStoragesChecked%>/><input type="hidden" name="Param_fileStorages" id="Param_fileStorages"/></td>
</tr>
<%@ include file="includeBottomField.jsp" %>
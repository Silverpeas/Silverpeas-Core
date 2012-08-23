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
    "http://www.silverpeas.org/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.util.ArrayList"%>
<%@ page import="com.stratelia.silverpeas.pdc.model.UsedAxis"%>
<%@ page import="com.silverpeas.form.displayers.PdcFieldDisplayer"%>

<%@ include file="includeParamsField.jsp.inc" %>

<%
	String pdcUtilizationContext = m_context + "/RpdcUtilization/jsp/";
	String language = resource.getLanguage();
	ResourcesWrapper pdcResource = new ResourcesWrapper(
		new ResourceLocator("com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle", language),
		new ResourceLocator("com.stratelia.silverpeas.pdcPeas.settings.pdcPeasIcons", language),
		language);
	
	String pdcAxis = "";
	if (parameters != null && parameters.containsKey("pdcAxis")) {
		pdcAxis = (String) parameters.get("pdcAxis");
	}
	
	browseBar.setPath(pdcResource.getString("pdcPeas.paramUsedAxis"));
	
	operationPane.addOperation(pdcResource.getIcon("pdcPeas.icoCreateParamAxis"),
		pdcResource.getString("pdcPeas.paramChooseAxis"), "javascript:chooseAxis()");
	if (pdcAxis.length() > 0) {
		operationPane.addOperation(pdcResource.getIcon("pdcPeas.icoDeleteParamAxis"),
			pdcResource.getString("pdcPeas.deleteAxis"), "javascript:deleteAxis()");
	}
%>

	<script type="text/javascript">
		function isCorrectForm() {
	     	checkFieldName();
	     	if (parseInt(document.getElementById("axisCount").value) == 0) {
		        errorMsg += "  - <%=pdcResource.getString("pdcPeas.selectAxis")%>";
		        errorNb++;
	     	}
	     	return checkErrors();
		}

		function chooseAxis() {
			var form = document.forms["fieldForm"];
			form.action = "<%=pdcUtilizationContext%>UtilizationViewAxis";
			form.submit();
		}

		function editAxis(id) {
			var form = document.forms["fieldForm"];
			form.elements["Id"].value = id;
			form.action = "<%=pdcUtilizationContext%>/UtilizationEditAxis";
			form.submit();
		}

		function deleteAxis() {
			var selectedItems = "";
			var form = document.forms["fieldForm"];
			var boxItems = form.elements["deleteAxis"];
			if (boxItems != null) {
				// At least one box exists.
				var nbBox = boxItems.length;
				if (nbBox == null && boxItems.checked) {
					// Only one checkbox is selected.
					selectedItems += boxItems.value;
				} else {
					// Checked boxes are searched.
					var i; 
					for (i = 0; i < boxItems.length; i++) {
						if (boxItems[i].checked) {
							if (selectedItems.length > 0) {
								selectedItems += ",";
							}
							selectedItems += boxItems[i].value;
						}
					}
				}
				if (selectedItems.length > 0
						&& confirm("<%=pdcResource.getString("pdcPeas.confirmDeleteAxis")%>")) {
					form.elements["Ids"].value = selectedItems;
					form.action = "<%=pdcUtilizationContext%>UtilizationDeleteAxis";
					form.submit();
				}
			}
		}
	</script>
</head>

<body>

<%@ include file="includeTopField.jsp.inc" %>

<tr>
	<td class="txtlibform" width="25%">Axes :</td>
	<td width="75%"><%=(new PdcFieldDisplayer()).getAxisHtml(pdcAxis, language)%>
		<input type="hidden" name="Param_pdcAxis" value="<%=pdcAxis%>"/>
		<input type="hidden" name="pdcFieldMode" value="true"/>
		<input type="hidden" name="actionForm" value="<%=actionForm%>"/>
		<input type="hidden" name="Id" value=""/>
		<input type="hidden" name="Ids" value=""/>
	</td>
</tr>


<%@ include file="includeBottomField.jsp.inc" %>
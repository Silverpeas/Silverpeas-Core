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

<%@page import="org.silverpeas.core.util.StringUtil"%>
<%@ include file="includeParamsField.jsp" %>
	<script type="text/javascript">
		function isCorrectForm()  {
		checkFieldName();
		checkMinLength();
		checkStartValue();
		return checkErrors();
		}

		function checkMinLength() {
			var minLength = document.forms["fieldForm"].elements["Param_minLength"].value;
			if (minLength == "") {
				document.forms["fieldForm"].elements["Param_minLength"].value = "1";
			} else if (minLength != ("" + parseInt(minLength))) {
				errorMsg += "  - '<%=resource.getString("templateDesigner.displayer.sequence.minLength")%>' <%=resource.getString("GML.MustContainsNumber")%>\n";
			errorNb++;
			}
		}

		function checkStartValue() {
			var startValue = document.forms["fieldForm"].elements["Param_startValue"].value;
			if (startValue == "") {
				document.forms["fieldForm"].elements["Param_startValue"].value = "1";
			} else if (startValue != ("" + parseInt(startValue))) {
				errorMsg += "  - '<%=resource.getString("templateDesigner.displayer.sequence.startValue")%>' <%=resource.getString("GML.MustContainsNumber")%>\n";
			errorNb++;
			}
		}
	</script>
<%
	String minLength = "1";
	String startValue = "1";
	boolean reuseAvailableValues = false;
	boolean global = false;
	if (field != null) {
		if (parameters.containsKey("minLength")) {
			minLength = parameters.get("minLength");
		}
		if (parameters.containsKey("startValue")) {
			startValue = parameters.get("startValue");
		}
		if (parameters.containsKey("reuseAvailableValues")) {
			reuseAvailableValues = "true".equals(parameters.get("reuseAvailableValues"));
		}
		global = StringUtil.getBooleanValue(parameters.get("global"));
	}
%>
<%@ include file="includeTopField.jsp" %>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.sequence.minLength")%> :</td>
	<td><input type="text" name="Param_minLength" value="<%=minLength%>" size="5" maxLength="2"/></td>
</tr>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.sequence.startValue")%> :</td>
	<td><input type="text" name="Param_startValue" value="<%=startValue%>" size="5" maxLength="5"/></td>
</tr>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.sequence.valueCreation")%> :</td>
	<td>
		<select name="Param_reuseAvailableValues">
			<option value="false"<%if (!reuseAvailableValues) {%> selected<%}%>><%=resource.getString("templateDesigner.displayer.sequence.alwaysIncrement")%></option>
			<option value="true"<%if (reuseAvailableValues) {%> selected<%}%>><%=resource.getString("templateDesigner.displayer.sequence.reuseAvailableValues")%></option>
		</select>
	</td>
</tr>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.sequence.globalParam")%> :</td>
	<td>
		<select name="Param_global">
			<option value="false"<%if (!global) {%> selected<%}%>><%=resource.getString("templateDesigner.displayer.sequence.notGlobal")%></option>
			<option value="true"<%if (global) {%> selected<%}%>><%=resource.getString("templateDesigner.displayer.sequence.global")%></option>
		</select>
	</td>
</tr>
<%@ include file="includeBottomField.jsp" %>
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

<%@page import="org.silverpeas.core.contribution.content.form.displayers.MapFieldDisplayer"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="includeParamsField.jsp" %>
<style type="text/css">
.map-param .txtlibform {
	padding-left: 20px;
	padding-right: 30px;
}
</style>
<script type="text/javascript">
	function isCorrectForm() {
	checkFieldName();
	return checkErrors();
	}

	function toggleMapOptions() {
		if( $('input[name=Param_<%=MapFieldDisplayer.PARAM_MAP %>]').is(':checked') ){
			$(".map-param").show();
		} else {
			$(".map-param").hide();
		}
	}

	toggleMapOptions();
</script>
<%
	String map = "";
	String width = "";
	String height = "";
	String zoom = "";
	String mapType = "";
	String enlarge = "";

	if (field != null) {
		String paramMap = parameters.get(MapFieldDisplayer.PARAM_MAP);
		if ("true".equals(paramMap)) {
			map = "checked=\"checked\"";
		}

		if (parameters.containsKey(MapFieldDisplayer.PARAM_WIDTH)) {
			width = parameters.get(MapFieldDisplayer.PARAM_WIDTH);
		}

		if (parameters.containsKey(MapFieldDisplayer.PARAM_HEIGHT)) {
			height = parameters.get(MapFieldDisplayer.PARAM_HEIGHT);
		}

		if (parameters.containsKey(MapFieldDisplayer.PARAM_KIND)) {
			mapType = parameters.get(MapFieldDisplayer.PARAM_KIND);
		}

		if (parameters.containsKey(MapFieldDisplayer.PARAM_ZOOM)) {
			zoom = parameters.get(MapFieldDisplayer.PARAM_ZOOM);
		}

		if ("true".equals(parameters.get(MapFieldDisplayer.PARAM_ENLARGE))) {
			enlarge = "checked=\"checked\"";
		}
	}
%>
<%@ include file="includeTopField.jsp" %>
<tr>
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.map.map")%> :</td><td><input type="checkbox" name="Param_<%=MapFieldDisplayer.PARAM_MAP %>" value="true" <%=map %> onclick="toggleMapOptions(this)"/></td>
</tr>
<tr class="map-param">
	<td class="txtlibform"><%=resource.getString("templateDesigner.displayer.map.kind")%> :</td>
	<td>
		<select name="Param_<%=MapFieldDisplayer.PARAM_KIND%>">
			<option value="<%=MapFieldDisplayer.KIND_NORMAL%>" <%= MapFieldDisplayer.KIND_NORMAL.equals(mapType) ? "selected=\"selected\"" : "" %>><%=resource.getString("templateDesigner.displayer.map.kind.normal") %></option>
			<option value="<%=MapFieldDisplayer.KIND_SATELLITE%>" <%= MapFieldDisplayer.KIND_SATELLITE.equals(mapType) ? "selected=\"selected\"" : "" %>><%=resource.getString("templateDesigner.displayer.map.kind.satellite") %></option>
			<option value="<%=MapFieldDisplayer.KIND_HYBRID%>" <%= MapFieldDisplayer.KIND_HYBRID.equals(mapType) ? "selected=\"selected\"" : "" %>><%=resource.getString("templateDesigner.displayer.map.kind.hybrid") %></option>
			<option value="<%=MapFieldDisplayer.KIND_RELIEF%>" <%= MapFieldDisplayer.KIND_RELIEF.equals(mapType) ? "selected=\"selected\"" : "" %>><%=resource.getString("templateDesigner.displayer.map.kind.relief") %></option>
		</select>
	</td>
</tr>
<tr class="map-param">
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.map.width")%> :</td><td><input type="text" name="Param_<%=MapFieldDisplayer.PARAM_WIDTH %>" value="<%=width%>" size="5" maxLength="4"/></td>
</tr>
<tr class="map-param">
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.map.height")%> :</td><td><input type="text" name="Param_<%=MapFieldDisplayer.PARAM_HEIGHT %>" value="<%=height%>" size="5" maxLength="4"/></td>
</tr>
<tr class="map-param">
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.map.zoom")%> :</td><td><input type="text" name="Param_<%=MapFieldDisplayer.PARAM_ZOOM %>" value="<%=zoom%>" size="5" maxLength="4"/> <%=resource.getString("templateDesigner.displayer.map.zoom.help") %></td>
</tr>
<tr class="map-param">
	<td class="txtlibform" width="170px"><%=resource.getString("templateDesigner.displayer.map.link")%> :</td><td><input type="checkbox" name="Param_<%=MapFieldDisplayer.PARAM_ENLARGE %>" value="true" <%=enlarge %>/></td>
</tr>
<%@ include file="includeBottomField.jsp" %>
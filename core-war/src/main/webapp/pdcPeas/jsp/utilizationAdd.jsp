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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkPdc.jsp"%>
<%
	Axis axis = (Axis) request.getAttribute("AxisDetail");
	UsedAxis usedAxis = (UsedAxis) request.getAttribute("UsedAxis");

	Integer isMandatory = (Integer) request.getAttribute("IsMandatory");
	Integer isVariant = (Integer) request.getAttribute("IsVariant");
	String componentId = (String) request.getAttribute("ComponentId");

	String errorMessage = null;
	String mandatoryChecked = "checked";
	String notMandatoryChecked = "";
	String variantChecked = "checked";
	String notVariantChecked = "";
	String baseValueId = "";

	if (usedAxis != null) {
		//L'ajout n'a pas �t� possible
		errorMessage = "<font size=2 color=#FF6600><b>"+resource.getString("pdcPeas.errorMessage")+"</b></font>";

		if (usedAxis.getMandatory() == 1) {
			mandatoryChecked = "checked";
		} else {
			notMandatoryChecked = "checked";
		}

		if (usedAxis.getVariant() == 1) {
			variantChecked = "checked";
		} else {
			notVariantChecked = "checked";
		}

		baseValueId = new Integer(usedAxis.getBaseValue()).toString();
	}

	String axisName = axis.getAxisHeader().getName(language);
	ArrayList axisValues = (ArrayList) axis.getValues();
	Value value = null;
	String valueName = "";
	String valueId = "";
	int valueLevel = -1;
	String increment = "";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script type="text/javascript">
function sendData() {
	document.axisForm.submit();
}

function goBack(){
	document.goBack.submit();
}
</script>
</head>
<body>
<%
    browseBar.setComponentId(componentId);
	browseBar.setPath(resource.getString("pdcPeas.axisUse")+" : "+axisName+"");

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<form action="<%=pdcUtilizationContext%>UtilizationAddAxis" name="axisForm" method="post">
	<input type="hidden" name="Id" value="<%=axis.getAxisHeader().getPK().getId()%>"/>
	<table width="100%" border="0" cellspacing="0" cellpadding="4">
	<% if (errorMessage != null) { %>
		<tr><td colspan="2"><%=errorMessage%></td></tr>
	<% } %>
      <tr>
        <td class="txtlibform" width="50%" nowrap><%=resource.getString("pdcPeas.baseValue")%>&nbsp;:</td>
        <td width="50%" nowrap="nowrap">
			<select name="BaseValue">
		<%
			for (int i = 0; i<axisValues.size(); i++)  {
				value = (Value) axisValues.get(i);
				valueName = value.getName(language);
				valueId = value.getPK().getId();
				valueLevel = value.getLevelNumber();
				increment = "";
				for (int j = 0; j < valueLevel; j++)
					increment += "&nbsp;&nbsp;";

				if (baseValueId.equals(valueId))
					out.println("<option value=\""+valueId+"\" selected>"+increment+valueName+"</option>");
				else
					out.println("<option value=\""+valueId+"\">"+increment+valueName+"</option>");
			}
		%>
			</select>
		</td>
      </tr>
      <tr>
        <td class="txtlibform" nowrap><%=resource.getString("pdcPeas.axisUse2")%>&nbsp;:</td>
        <td nowrap>
		  <% if (isMandatory != null) {
				if (isMandatory.intValue() == 0) { %>
					<input type="hidden" name="Mandatory" value="0"/><span class="textePetitBold">&nbsp;<%=resource.getString("pdcPeas.optional")%></span>
				<% } else {	%>
					<input type="hidden" name="Mandatory" value="1"/><span class="textePetitBold">&nbsp;<%=resource.getString("GML.requiredField")%></span>
				<% } %>
		  <% } else { %>
				<input type="radio" name="Mandatory" value="1" <%=mandatoryChecked%>/><span class="textePetitBold">&nbsp;<%=resource.getString("GML.requiredField")%></span><br>
				<input type="radio" name="Mandatory" value="0" <%=notMandatoryChecked%>/><span class="textePetitBold">&nbsp;<%=resource.getString("pdcPeas.optional")%></span></td>
		  <% } %>
      </tr>
      <% if (isAxisInvarianceUsed) { %>
      <tr>
        <td class="txtlibform" nowrap><%=resource.getString("pdcPeas.axisValue")%>&nbsp;:</td>
        <td nowrap>
		  <% if (isVariant != null) {
				if (isVariant.intValue() == 0) { %>
					<input type="hidden" name="Variant" value="0"/><span class="textePetitBold">&nbsp;<%=resource.getString("pdcPeas.notVariants")%></span>
				<% } else {	%>
					<input type="hidden" name="Variant" value="1"/><span class="textePetitBold">&nbsp;<%=resource.getString("pdcPeas.variants")%></span>
				<% } %>
		  <% } else { %>
				<input type="radio" name="Variant" value="1" <%=variantChecked%>/><span class="textePetitBold">&nbsp;<%=resource.getString("pdcPeas.variants")%></span><br>
				<input type="radio" name="Variant" value="0" <%=notVariantChecked%>/><span class="textePetitBold">&nbsp;<%=resource.getString("pdcPeas.notVariants")%></span></td>
		  <% } %>
      </tr>
    <% } %>
  </table>
  </form>
  <%
	out.println(board.printAfter());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:sendData()", false));
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:goBack()", false));
    out.println("<br/>"+buttonPane.print());

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
<form name="goBack" action="<%=pdcUtilizationContext%>UtilizationViewAxis" method="post">
</form>
</body>
</html>
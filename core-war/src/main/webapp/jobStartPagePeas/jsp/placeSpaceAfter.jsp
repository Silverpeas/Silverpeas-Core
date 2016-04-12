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

<%@ include file="check.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
	SpaceInst[] brothers = (SpaceInst[]) request.getAttribute("Brothers");
	SpaceInst currentSpace = (SpaceInst) request.getAttribute("CurrentSpace");

	window.setPopup(true);
    browseBar.setSpaceId(currentSpace.getId());
    browseBar.setPath(resource.getString("JSPP.SpaceOrder"));
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript">
function B_ANNULER_ONCLICK() {
	window.close();
}
/*****************************************************************************/
function B_VALIDER_ONCLICK() {
    document.spaceOrder.submit();
}
</script>
</head>
<body>
<%
    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<view:board>
<form name="spaceOrder" action="EffectivePlaceSpaceAfter" method="post">
	<table border="0" cellspacing="0" cellpadding="5">
		<tr>
			<td class="txtlibform"><%=resource.getString("JSPP.SpacePlace")%> :</td>
			<td>
	            <select name="SpaceBefore" id="SpaceBefore">
	                <% for (SpaceInst space : brothers) { %>
	                        <option value="<%=space.getId() %>"><%=EncodeHelper.javaStringToHtmlString(space.getName()) %></option>
	                <% } %>
	                <option value="-1" selected="selected"><%=resource.getString("JSPP.PlaceLast")%></option>
	            </select>
			</td>
		</tr>
	</table>
</form>
</view:board>
<br/>
<%
		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
		buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
		out.println(buttonPane.print());
		out.println(frame.printAfter());
        out.println(window.printAfter());
%>
</body>
</html>
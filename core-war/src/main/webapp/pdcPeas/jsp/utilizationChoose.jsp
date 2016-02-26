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

// recuperation des parametres
String 	viewType = (String) request.getAttribute("ViewType"); // type d'axes � visualiser P(rimaire) ou S(econdaire)
List<AxisHeader> 	axisList = (List<AxisHeader>) request.getAttribute("AxisList"); // a list of axis header
String componentId = (String) request.getAttribute("ComponentId");

// initialisation of variables of main loop (show all axes)
String axisId = null;
ArrayLine arrayLine = null;
IconPane iconPane1 = null;
Icon aspiIcon = null;
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script type="text/javascript">
function goBack(){
	document.goBack.submit();
}
</script>
</head>
<body>
<%
	browseBar.setComponentId(componentId);
	browseBar.setExtraInformation(resource.getString("pdcPeas.paramChooseAxis"));

    out.println(window.printBefore());

	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("pdcPeas.primaryAxis"),pdcUtilizationContext+"ChangeViewType?ViewType=P",viewType.equals("P"));
    tabbedPane.addTab(resource.getString("pdcPeas.secondaryAxis"),pdcUtilizationContext+"ChangeViewType?ViewType=S",viewType.equals("S"));
	out.println(tabbedPane.print());

    out.println(frame.printBefore());

    ArrayPane arrayPane = gef.getArrayPane("PdcPeas", pdcUtilizationContext+"ChangeViewType?ViewType="+viewType, request, session);

    ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);

	ArrayColumn arrayColumn = arrayPane.addArrayColumn(resource.getString("pdcPeas.axisName"));
	arrayColumn.setSortable(false);

	// main loop to show all axis
	for (AxisHeader axisHeader : axisList) {
			axisId = axisHeader.getPK().getId();

            arrayLine = arrayPane.addArrayLine();

			arrayLine.addArrayCellText("<div align=right><img src=\""+resource.getIcon("pdcPeas.icoComponent")+"\" border=0 alt=\""+EncodeHelper.javaStringToHtmlString(axisHeader.getName(language))+"\" title=\""+EncodeHelper.javaStringToHtmlString(axisHeader.getName(language))+"\"></div>");

			arrayLine.addArrayCellText("<a href=\""+pdcUtilizationContext+"UtilizationChooseAxis?Id="+axisId+"\" title=\""+resource.getString("pdcPeas.axisUse")+"&nbsp;:&nbsp;"+EncodeHelper.javaStringToHtmlString(axisHeader.getName(language))+"\"><span class=textePetitBold>"+EncodeHelper.javaStringToHtmlString(axisHeader.getName(language))+"</span></a>");
	}

    out.println(arrayPane.print());

    ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.back"), "javascript:goBack()", false));
    out.println("<br/>"+buttonPane.print());

out.println(frame.printAfter());
out.println(window.printAfter());
%>
<form name="goBack" action="<%=pdcUtilizationContext%>Main" method="post">
</form>
</body>
</html>
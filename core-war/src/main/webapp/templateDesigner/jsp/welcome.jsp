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
<%@ include file="check.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
</head>
<body>
<%
List<PublicationTemplate> templates = (List<PublicationTemplate>) request.getAttribute("Templates");

browseBar.setDomainName(resource.getString("templateDesigner.toolName"));
browseBar.setComponentName(resource.getString("templateDesigner.templateList"));

operationPane.addOperationOfCreation(resource.getIcon("templateDesigner.newTemplate"), resource.getString("templateDesigner.newTemplate"), "NewTemplate");

ArrayPane arrayPane = gef.getArrayPane("templateList", "Main", request, session);
arrayPane.setVisibleLineNumber(20);
arrayPane.setTitle(resource.getString("templateDesigner.templateList"));
arrayPane.addArrayColumn(resource.getString("GML.name"));
arrayPane.addArrayColumn(resource.getString("GML.description"));
ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resource.getString("templateDesigner.visibility"));
arrayColumn2.setSortable(false);

for(PublicationTemplate template : templates) {
	ArrayLine ligne = arrayPane.addArrayLine();

	ligne.addArrayCellLink(template.getName(), "ViewTemplate?Template="+template.getFileName());
	ligne.addArrayCellText(template.getDescription());

	IconPane icon = gef.getIconPane();
	Icon viewIcon = icon.addIcon();
	if (template.isVisible()) {
		viewIcon.setProperties(resource.getIcon("templateDesigner.visible"), resource.getString("templateDesigner.visible"));
	} else {
		viewIcon.setProperties(resource.getIcon("templateDesigner.masque"), resource.getString("templateDesigner.hidden"));
	}
	ligne.addArrayCellIconPane(icon);
}

out.println(window.printBefore());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<%
out.println(arrayPane.print());
%>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>
<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<%
Iterator templates = (Iterator) ((List)request.getAttribute("Templates")).iterator();

browseBar.setDomainName(resource.getString("templateDesigner.toolName"));
browseBar.setComponentName(resource.getString("templateDesigner.templateList"));

operationPane.addOperation(resource.getIcon("templateDesigner.newTemplate"), resource.getString("templateDesigner.newTemplate"), "NewTemplate");

ArrayPane arrayPane = gef.getArrayPane("templateList", "Main", request, session);
arrayPane.setVisibleLineNumber(20);
arrayPane.setTitle(resource.getString("templateDesigner.templateList"));
arrayPane.addArrayColumn(resource.getString("GML.name"));
arrayPane.addArrayColumn(resource.getString("GML.description"));
ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resource.getString("templateDesigner.visibility"));
arrayColumn2.setSortable(false);

while(templates.hasNext())
{	
  PublicationTemplate template = (PublicationTemplate) templates.next();
	ArrayLine ligne = arrayPane.addArrayLine();
	
	ligne.addArrayCellLink(template.getName(), "ViewTemplate?Template="+template.getFileName());
	ligne.addArrayCellText(template.getDescription());
	
	IconPane icon = gef.getIconPane();
	Icon viewIcon = icon.addIcon();
	if (template.isVisible())
		viewIcon.setProperties(resource.getIcon("templateDesigner.visible"), "");
	else
		viewIcon.setProperties(resource.getIcon("templateDesigner.hidden"), "");
	ligne.addArrayCellIconPane(icon);
}

out.println(window.printBefore());
out.println(frame.printBefore());

out.println(arrayPane.print());

out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
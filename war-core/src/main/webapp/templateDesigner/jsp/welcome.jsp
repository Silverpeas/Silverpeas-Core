<%@ include file="check.jsp" %>
<%
Iterator templates = ((List) request.getAttribute("Templates")).iterator();
%>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<%
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

while (templates.hasNext())
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
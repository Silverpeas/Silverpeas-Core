<%@ include file="checkThesaurus.jsp"%>
<%
	Vocabulary voca = (Vocabulary) request.getAttribute("Vocabulary");
	Collection jargons = (Collection) request.getAttribute("ListUser");
	String nomVoca = Encode.javaStringToHtmlString(voca.getName());
	Iterator it = jargons.iterator();
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath("<a href=\"Back\">"+resource.getString("thesaurus.thesaurus")+ "</a> > " + resource.getString("thesaurus.BBlistAffectations") + nomVoca);


	operationPane.addOperation(resource.getIcon("thesaurus.callUserPanel"), 
		resource.getString("thesaurus.OPuserPanel"), "UserPanel");


	out.println(window.printBefore());
 
    
	out.println(frame.printBefore());
%>

<% // Ici debute le code de la page %>
<%
	ArrayPane arrayPane = gef.getArrayPane("thesaurus_synonyms", "EditAssignments", request, session);

	ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);

	//arrayPane.addArrayColumn(resource.getString("GML.operation"));
	ArrayColumn arrayColumn = arrayPane.addArrayColumn(resource.getString("GML.nom"));
	arrayColumn.setSortable(false);

	while (it.hasNext())
	{
		Jargon jargon = (Jargon) it.next();
		String user = Encode.javaStringToHtmlString(jargon.readUserName());
		int type = jargon.getType();
		ArrayLine arrayLine = arrayPane.addArrayLine();
		IconPane iconPane1 = gef.getIconPane();
		Icon debIcon = iconPane1.addIcon();
		if (type == 0)
			debIcon.setProperties(resource.getIcon("thesaurus.miniconeUser"),resource.getString("thesaurus.IPUser"), "");
		if (type == 1)
			debIcon.setProperties(resource.getIcon("thesaurus.miniconeGroup"),resource.getString("thesaurus.IPGroup"), "");
		arrayLine.addArrayCellIconPane(iconPane1);	
		arrayLine.addArrayCellText(user);
	}
	/*if (arrayPane.getColumnToSort() == 0)
		arrayPane.setColumnToSort(2); */

	out.println(arrayPane.print());
%>
<br>

<% // Ici se termine le code de la page %>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</BODY>
</HTML>
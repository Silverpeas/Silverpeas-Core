<%@ include file="checkThesaurus.jsp"%>
<%
	Collection jargons = (Collection) request.getAttribute("listJargons");
	Iterator it = jargons.iterator();
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<SCRIPT LANGUAGE="JavaScript">
<!--
function SelectVocabulary()
{
	SP_openWindow('SelectVocabulary', 'select_vocabulary', '500', '160', 'menubar=no,scrollbars=no,statusbar=no');	
}
//-->
</SCRIPT>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath("<a href=\"Back\">"+resource.getString("thesaurus.thesaurus")+ "</a> > " + resource.getString("thesaurus.BBresultSelection"));


	operationPane.addOperation(resource.getIcon("thesaurus.OPaffectVoc"), 
		resource.getString("thesaurus.OPaffectVoc"), "javascript:SelectVocabulary();");


	out.println(window.printBefore());
 
    
	out.println(frame.printBefore());
%>

<% // Ici debute le code de la page %>
<%
	ArrayPane arrayPane = gef.getArrayPane("thesaurus_synonyms", "UserAssignments", request, session);

	ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);
	
	arrayPane.addArrayColumn(resource.getString("GML.nom"));
	arrayPane.addArrayColumn(resource.getString("thesaurus.vocAffecte"));

	while (it.hasNext())
	{
		Jargon jargon = (Jargon) it.next();
		String voca = Encode.javaStringToHtmlString(jargon.readVocaName());
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
		arrayLine.addArrayCellText(voca);
	}
	if (arrayPane.getColumnToSort() == 0)
		arrayPane.setColumnToSort(2);       

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
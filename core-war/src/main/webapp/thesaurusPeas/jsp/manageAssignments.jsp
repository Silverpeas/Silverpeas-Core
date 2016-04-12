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
<%@ include file="checkThesaurus.jsp"%>
<%
	Collection jargons = (Collection) request.getAttribute("listJargons");
	Iterator it = jargons.iterator();
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
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
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
<%@ page import="org.silverpeas.core.pdc.PdcServiceProvider"%>
<%@ page import="org.silverpeas.core.pdc.thesaurus.model.ThesaurusException"%>
<%@ page import="org.silverpeas.core.pdc.thesaurus.service.ThesaurusManager"%>
<%@ page import="org.silverpeas.core.pdc.thesaurus.model.Jargon" %>

<%@ include file="checkPdc.jsp"%>

<%!

String displaySynonymsValue(Boolean activeThesaurus, Jargon jargon, String idTree, String idTerm) throws ThesaurusException {
		String synonyms = "";
		boolean first = true;
		if (jargon != null && activeThesaurus.booleanValue()) {//activ�
			//synonymes du terme
			String idUser = jargon.getIdUser();
			ThesaurusManager thesaurus = PdcServiceProvider.getThesaurusManager();
			Collection listSynonyms = thesaurus.getSynonyms(new Long(idTree).longValue(), new Long(idTerm).longValue(), idUser);
			Iterator it = listSynonyms.iterator();
			synonyms += "<i>";
			while (it.hasNext()) {
				String name = (String) it.next();
				if (first) {
					synonyms += "- "+name;
				}
				else
					synonyms += ", "+name;
				first = false;
			}
			synonyms += "</i>";
		}
		return synonyms;
}

%>
<%
	List positions = (List) request.getAttribute("Positions");
	Jargon jargon = (Jargon) request.getAttribute("Jargon");
	Boolean activeThesaurus = (Boolean) request.getAttribute("ActiveThesaurus");

	ClassifyPosition	position	= null;
	List				values		= null;
	ClassifyValue		value		= null;
	List				pathValues	= null;
	String				path	    = null;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<!-- JAVASCRIPT LANGUAGE -->
<script language="JavaScript">

	// IE / Netscape compliant

	// this method opens a pop-up which warns the user
	function areYouSure(){
		return confirm("<%=resource.getString("pdcPeas.confirmDeleteAxis")%>");
	}

	// this function get all checked boxes by the user and sent
	// data to the router
	function getSelectedItems(){
		var boxItems = document.viewPositions.deletePosition;
		var  selectItems = "";
		if (boxItems != null){
			// au moins une checkbox exist
			var nbBox = boxItems.length;
			if ( (nbBox == null) && (boxItems.checked == true) ){
				// il n'y a qu'une checkbox selectionn�e
				selectItems += boxItems.value;
			} else{
				// search checked boxes
				for (i=0;i<boxItems.length ;i++ ){
					if (boxItems[i].checked == true){
						selectItems += boxItems[i].value+",";
					}
				}
				selectItems = selectItems.substring(0,selectItems.length-1); // erase the last coma
			}
			if ( (selectItems.length > 0) && (areYouSure())  ){
				// an axis has been selected !
				document.viewPositions.Ids.value = selectItems;
				document.viewPositions.action = "<%=pdcClassifyContext%>DeletePosition";
				document.viewPositions.submit();
			}
		}
	}


	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		SP_openWindow(fonction, windowName, '600', '300','scrollbars=yes, resizable, alwaysRaised');
	}

	function activateThesaurus () {
		document.viewPositions.action = "<%=pdcClassifyContext%>ActivateThesaurus";
		document.viewPositions.submit();
	}

	function desactivateThesaurus () {
		document.viewPositions.action = "<%=pdcClassifyContext%>DesactivateThesaurus";
		document.viewPositions.submit();
	}

</script>
<!-- / JAVASCRIPT LANGUAGE -->
<style type="text/css">
<!--

.pos:hover {
	font-size: 10px;
	font-weight: normal;
	color: White;
	background-color : navy;
	text-decoration: none;
	border:1 solid  rgb(255,150,0);
}

.pos {
	font-size: 10px;
	font-weight: bold;
	color: navy;
	background-color : #F5F5F5;
	text-decoration: none;
	border:1 solid  rgb(150,150,150);
}
-->
</style>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<FORM name="viewPositions" Action="<%=pdcClassifyContext%>Main" method="post">
	<input type="hidden" name="Ids">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setExtraInformation(resource.getString("pdcPeas.classifyPublication"));

    operationPane.addOperation(resource.getIcon("pdcPeas.icoAddPosition"),resource.getString("pdcPeas.classifyAdd"), "javascript:openSPWindow('"+pdcClassifyContext+"NewPosition','newposition')");
	operationPane.addOperation(resource.getIcon("pdcPeas.icoDeletePosition"),resource.getString("pdcPeas.classifyDelete"), "javascript:getSelectedItems()");
	//Test si l'utilisateur courant a un jargon
	/*if (jargon != null) {//l'utilisateur utilise un jargon
		if (activeThesaurus.booleanValue()) {//activ�
			//affichage du bouton de d�sactivation du thesaurus
			operationPane.addOperation(resource.getIcon("pdcPeas.icoDesactivateThesaurus"), resource.getString("pdcPeas.desactivateThesaurus"), "javascript:desactivateThesaurus()");
		}
		else {
			//affichage du bouton dactivation du thesaurus
			operationPane.addOperation(resource.getIcon("pdcPeas.icoActivateThesaurus"), resource.getString("pdcPeas.activateThesaurus"), "javascript:activateThesaurus()");
		}
	}*/


    out.println(window.printBefore());
    out.println(frame.printBefore());

%>
<CENTER>
<% if (positions.size() == 0) { %>
	<%=boardStart%>
    <table width="100%" border="0" cellspacing="0" cellpadding="4">
		<tr><td colspan="3"><span class=txtlibform><%=resource.getString("pdcPeas.noPosition")%></span></td></tr>
	</table>
    <%=boardEnd%>
<% } %>

<% for (int i = 0; i<positions.size(); i++)	{
	position	= (ClassifyPosition) positions.get(i);
	values		= position.getValues();
%>
    <%=boardStart%>
    <table width="100%" border="0" cellspacing="0" cellpadding="4">
		<tr><td colspan="2"><span class=txtlibform><%=resource.getString("pdcPeas.position")%> <%=i+1%></span></td></tr>

		<% for (int v = 0; v < values.size(); v++) {
				value		= (ClassifyValue) values.get(v);
				pathValues	= value.getFullPath();
				path = buildCompletPath((ArrayList)pathValues, false, language);
				if (path == null)
					path = separatorPath;
		%>
			<tr>
				<td width="100%">
					<table border="0" cellspacing="0" cellpadding="2">
					<tr>
						<td><LI> <%=path%>
						<%
						//prend le dernier element de la liste
						/*ArrayList term = (ArrayList) pathValues.get(pathValues.size() - 1);
						String termId = (String) term.get(1);
						String treeId = (String) term.get(2);*/
						Value term = (Value) pathValues.get(pathValues.size() - 1);
						String termId = term.getPK().getId();
						String treeId = term.getTreeId();

						out.println(displaySynonymsValue(activeThesaurus, jargon, treeId, termId));
						%>
						</td>
					</tr>
					</table>
				</td>
				<% if (v == 0) { %>
					<td align="right" valign="top" class=intfdcolor width="0%" nowrap>
						<a href="javascript:openSPWindow('<%=pdcClassifyContext%>EditPosition?Id=<%=position.getPositionId()%>')"><img src="<%=resource.getIcon("pdcPeas.update")%>" border=0 align=absmiddle alt="<%=resource.getString("GML.modify")%>" title="<%=resource.getString("GML.modify")%>"></a>&nbsp;&nbsp;
						<input type="checkbox" name="deletePosition" value="<%=position.getPositionId()%>">
					</td>
				<% } else { %>
					 <td>&nbsp;</td>
				<% } %>
			</tr>
		<% } //fin du for termes %>
    </table>
    <%=boardEnd%>
<%=separator%>
  <%
    // Add the return button on the last one
		if(i == positions.size()-1 || positions.size() == 0)
		{
			ButtonPane buttonPane = gef.getButtonPane();
			buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.close"), "javascript:window.close()", false));
			out.println(buttonPane.print());
		}
  %>

<% } //fin du for position%>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</FORM>
<form name="toComponent" action="<%=pdcClassifyContext%>Main" method="post"></form>
</BODY>
</HTML>
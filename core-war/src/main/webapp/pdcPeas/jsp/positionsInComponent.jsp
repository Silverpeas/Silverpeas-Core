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

<%@ page import="org.silverpeas.core.pdc.PdcServiceProvider"%>
<%@ page import="org.silverpeas.core.pdc.thesaurus.model.ThesaurusException"%>
<%@ page import="org.silverpeas.core.pdc.thesaurus.service.ThesaurusManager"%>
<%@ page import="org.silverpeas.core.pdc.thesaurus.model.Jargon"%>
<%@ page import="org.silverpeas.web.pdc.control.PdcClassifySessionController"%>
<%@ page import="org.silverpeas.core.web.mvc.controller.ComponentContext"%>
<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController" %>

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

PdcClassifySessionController setComponentSessionController(HttpSession session, MainSessionController mainSessionCtrl) {
        //ask to MainSessionController to create the ComponentContext
        ComponentContext componentContext = mainSessionCtrl.createComponentContext(null, null);
        //instanciate a new CSC
        PdcClassifySessionController component = createComponentSessionController(mainSessionCtrl, componentContext);
        session.setAttribute("Silverpeas_pdcClassify", component);
        return component;
}

PdcClassifySessionController createComponentSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
{
        return new PdcClassifySessionController(mainSessionCtrl, componentContext, "org.silverpeas.pdcPeas.multilang.pdcBundle", "org.silverpeas.pdcPeas.settings.pdcPeasIcons");
}

%>
<%
	String silverObjectId		= request.getParameter("SilverObjectId");
	String componentId			= request.getParameter("ComponentId");
	String returnURL			= request.getParameter("ReturnURL");
	String sendSubscriptions 	= request.getParameter("SendSubscriptions");

	PdcClassifySessionController pdcSC = (PdcClassifySessionController) session.getAttribute("Silverpeas_pdcClassify");

	if (pdcSC == null) {
		MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
		pdcSC = setComponentSessionController(session, mainSessionCtrl);
	}

	resource = new MultiSilverpeasBundle(pdcSC.getMultilang(), pdcSC.getIcon(), pdcSC.getLanguage());
	language = resource.getLanguage();

	if (silverObjectId != null)
		pdcSC.setCurrentSilverObjectId(silverObjectId);
	if (componentId != null)
		pdcSC.setCurrentComponentId(componentId);

	if ("0".equals(sendSubscriptions))
		pdcSC.setSendSubscriptions(false);
	else
		pdcSC.setSendSubscriptions(true);

	List positions = pdcSC.getPositions();

	pdcSC.initializeJargon();
	Jargon jargon = pdcSC.getJargon();
	Boolean activeThesaurus = new Boolean(pdcSC.getActiveThesaurus());

	ClassifyPosition	position	= null;
	List				values		= null;
	ClassifyValue		value		= null;
	List				pathValues	= null;
	String				path	    = null;
%>
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
				document.viewPositions.ToURL.value = "<%=returnURL%>";
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
		document.viewPositions.action = "<%=pdcClassifyContext%>ActivateThesaurus?ToURL=<%=returnURL%>";
		document.viewPositions.submit();
	}

	function desactivateThesaurus () {
		document.viewPositions.action = "<%=pdcClassifyContext%>DesactivateThesaurus?ToURL=<%=returnURL%>";
		document.viewPositions.submit();
	}

</script>
<!-- / JAVASCRIPT LANGUAGE -->
<FORM name="viewPositions" Action="<%=pdcClassifyContext%>Main" method="post">
	<input type="hidden" name="Ids">
	<input type="hidden" name="ToURL">
<CENTER>
<% if (positions.size() == 0) {
	out.println(board.printBefore());
	%>
    <table width="100%" border="0" cellspacing="0" cellpadding="4">
		<tr><td class="txtlibform"><%=resource.getString("pdcPeas.noPosition")%></td></tr>
	</table>
<%
	out.println(board.printAfter());
} %>

<% for (int i = 0; i<positions.size(); i++)	{
	position	= (ClassifyPosition) positions.get(i);
	values		= position.getValues();

	if (i != 0)
		out.println("<BR>");

	out.println(board.printBefore());
%>
    <table width="100%" border="0" cellspacing="0" cellpadding="4">
		<tr><td class="txtlibform" colspan="2"><%=resource.getString("pdcPeas.position")%> <%=i+1%></td></tr>

		<% for (int v = 0; v < values.size(); v++) {
				value		= (ClassifyValue) values.get(v);
				pathValues	= value.getFullPath();
				path = buildCompletPath(pathValues, false, language);
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
						Value term = (Value) pathValues.get(pathValues.size() - 1);
						//String termId = (String) term.get(1);
						String termId = term.getPK().getId();
						//String treeId = (String) term.get(2);
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
		<% } //fin du for termes
		%>
    </table>
<%
	out.println(board.printAfter());
} //fin du for position
%>
</CENTER>
</FORM>
<form name="refresh" action="<%=pdcClassifyContext%>Main" method="post"></form>
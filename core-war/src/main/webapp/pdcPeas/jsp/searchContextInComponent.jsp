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

int getDescendantsNbObjects(String valueId, List daughters) {
	int nbObjects = 0;
	Value value = null;
	for (int i = 0; i<daughters.size(); i++) {
		value = (Value) daughters.get(i);
		if (value.getFullPath().indexOf("/"+valueId+"/") != -1) {
			nbObjects += value.getNbObjects();
		}
	}
	return nbObjects;
}


String displaySynonymsAxis(Boolean activeThesaurus, Jargon jargon, String axisId) throws ThesaurusException {
		String synonyms = "";
		boolean first = true;
		if (jargon != null && activeThesaurus.booleanValue()) {//active
			//synonymes du terme
			String idUser = jargon.getIdUser();
			ThesaurusManager thesaurus = PdcServiceProvider.getThesaurusManager());
			Collection listSynonyms = thesaurus.getSynonymsAxis(axisId, idUser);
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

String displaySynonymsValue(Boolean activeThesaurus, Jargon jargon, String idTree, String idTerm) throws ThesaurusException {
		String synonyms = "";
		boolean first = true;
		if (jargon != null && activeThesaurus.booleanValue()) {//active
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
// recuperation des parametres
List primaryAxis	= (List) request.getAttribute("ShowPrimaryAxis");
List secondaryAxis	= (List) request.getAttribute("ShowSecondaryAxis");
String showSndSearchAxis = (String) request.getAttribute("ShowSndSearchAxis");

SearchContext searchContext = (SearchContext) request.getAttribute("SearchContext");
List daughters = (List) request.getAttribute("Daughters");
String selectedAxisId = (String) request.getAttribute("SelectedAxis");
String selectedValueId = (String) request.getAttribute("SelectedValue");
List pathCriteria = (List) request.getAttribute("PathCriteria");
Jargon jargon = (Jargon) request.getAttribute("Jargon");
Boolean activeThesaurus = (Boolean) request.getAttribute("ActiveThesaurus");


boolean isEmptySearchContext = true;
int nbPositions = 0; // le nombre de documents pour un axe
String axisName = "";
String axisId = "";
String axisRootId = "";
SearchAxis searchAxis = null;
SearchCriteria searchCriteria = null;
ArrayList criteriaList = new ArrayList();
ArrayList axisCriteriaList = new ArrayList();

if (selectedAxisId == null)
	selectedAxisId = "";
if (selectedValueId == null)
	selectedValueId = "";
if (showSndSearchAxis == null)
	showSndSearchAxis = "NO";


// l'objet SearchContext n'est pas vide
if ( (searchContext != null) && (searchContext.getCriterias().size() > 0) ){
	// on recupere tous les contextes
	criteriaList = searchContext.getCriterias();
	// et on recupere dans une liste tous les axes que ne doivent pas apparaitre
	// dans le primaire ou secondaire
	for (int i=0;i<criteriaList.size();i++ )
	{
		searchCriteria = (SearchCriteria) criteriaList.get(i);
		axisCriteriaList.add(new Integer(searchCriteria.getAxisId()).toString());
	}
	isEmptySearchContext = false;
}

// recuperation des parametres
ContainerWorkspace containerWorkspace = (ContainerWorkspace) request.getAttribute("containerWorkspace");
boolean bUtilization = false;
List alContainerRoles = containerWorkspace.getContainerUserRoles();
for(int nI=0; alContainerRoles != null && nI < alContainerRoles.size(); nI++)
		if(((String)alContainerRoles.get(nI)).equals("containerPDC_admin"))
				bUtilization = true;

// Icones du contenu
List alURLIcone = containerWorkspace.getContentURLIcones();
String sIconePath = null;
String sActionPath = null;
String sAlternateText = null;

// Role
List alUserRoles = containerWorkspace.getContainerUserRoles();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>

<script language="JavaScript">

	// IE / Netscape compliant

	// this method opens a pop-up which warns the user
	function areYouSure(){
		return confirm("<%=resource.getString("pdcPeas.confirmDeleteAxis")%>");
	}

	// this function get all checked boxes by the user and sent
	// data to the router
	function getSelectedItems(){
		var boxItems = document.searchContext.contextResearch;
		var  selectItems = "";
		if (boxItems != null){
			// au moins une checkbox exist
			var nbBox = boxItems.length;
			if ( (nbBox == null) && (boxItems.checked == true) ){
				// il n'y a qu'une checkbox selectionnee
				selectItems += boxItems.value.split('-')[0];
			} else{
				// search checked boxes
				for (i=0;i<boxItems.length ;i++ ){
					if (boxItems[i].checked == true){
						selectItems += boxItems[i].value.split('-')[0]+",";
					}
				}
				selectItems = selectItems.substring(0,selectItems.length-1); // erase the last coma
			}
			if ( (selectItems.length > 0) && (areYouSure())  ){
				// an axis has been selected !
				document.searchContext.Ids.value = selectItems;
				document.searchContext.action = "DeleteCriteria";
				document.searchContext.submit();
			}
		}
	}

	// cette methode appelle le routeur avec le parametre ShowSndSearchAxis
	function viewSecondaryAxis(show){
		if (show){
			document.searchContext.ShowSndSearchAxis.value = "YES";
		} else {
			document.searchContext.ShowSndSearchAxis.value = "NO";
		}
		document.searchContext.submit();
	}

	// Montre les valeurs filles de la valeur choisie
	// et ferme normalement les autres valeur filles visualisees
	function showDaughterValues(axisId,valueId){
		document.searchContext.AxisId.value = axisId;
		document.searchContext.ValueId.value = valueId;

		document.searchContext.action = "ViewArbo";
		document.searchContext.submit();
	}

	// Montre les valeurs filles de la valeur choisie
	// et ferme normalement les autres valeur filles visualisees
	function collapseValue(axisId,valueId){
		document.searchContext.AxisId.value = axisId;
		document.searchContext.ValueId.value = valueId;

		document.searchContext.action = "ViewArbo";
		document.searchContext.submit();
	}

	function addCritere(axisId,valueId){
		document.searchContext.AxisId.value = axisId;
		document.searchContext.ValueId.value = valueId;

		document.searchContext.action = "AddCriteria";
		document.searchContext.submit();
	}

	function modifySearchContext(axisIdAndValueId){
		// on recupere les donnees liees a la selection
		document.searchContext.AxisId.value = axisIdAndValueId.split('-')[0];
		document.searchContext.ValueId.value = axisIdAndValueId.split('-')[1];

		document.searchContext.action = "ModifyCriteria";
		document.searchContext.submit();
	}

	function openSPWindow(fonction,windowName){
		SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
	}

	function activateThesaurus () {
		document.searchContext.action = "ActivateThesaurus";
		document.searchContext.submit();
	}

	function desactivateThesaurus () {
		document.searchContext.action = "DesactivateThesaurus";
		document.searchContext.submit();
	}
</script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");

	// Icone for Utilization
	String sIcone = resource.getIcon("pdcPeas.icoParamPdcPeas");
	if(bUtilization)
		operationPane.addOperation(sIcone, resource.getString("pdcPeas.PDCUtilization"), "javascript:openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+request.getAttribute("ComponentId")+"','utilizationPdc1')");

	for(int nI=0; nI < alURLIcone.size(); nI++) {
		sIconePath = ((URLIcone)alURLIcone.get(nI)).getIconePath();
		sAlternateText = ((URLIcone)alURLIcone.get(nI)).getAlternateText();
		sActionPath = ((URLIcone)alURLIcone.get(nI)).getActionURL();

		operationPane.addOperation(sIconePath, resource.getString(sAlternateText), "ContentForward?contentURL="+sActionPath);
    }


	//Test si l'utilisateur courant a un jargon
	/*if (jargon != null) {//l'utilisateur utilise un jargon
		if (activeThesaurus.booleanValue()) {//active
			//affichage du bouton de desactivation du thesaurus
			operationPane.addOperation(resource.getIcon("pdcPeas.icoDesactivateThesaurus"), resource.getString("pdcPeas.desactivateThesaurus"), "javascript:desactivateThesaurus()");
		}
		else {
			//affichage du bouton dactivation du thesaurus
			operationPane.addOperation(resource.getIcon("pdcPeas.icoActivateThesaurus"), resource.getString("pdcPeas.activateThesaurus"), "javascript:activateThesaurus()");
		}
	}*/


    // affichage de l'icone voir les axes secondaires ou les cacher
    if (secondaryAxis == null){
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDisplaySecondaryAxis"), resource.getString("pdcPeas.secondaryAxis"), "javascript:viewSecondaryAxis(true)");
    } else {
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDisplayPrimaryAxis"), resource.getString("pdcPeas.primaryAxis"), "javascript:viewSecondaryAxis(false)");
	}
	// on permet la suppression de critere
	if (!isEmptySearchContext) {
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDeleteAxisFromContext"), resource.getString("pdcPeas.supprAxisFromContext"), "javascript:getSelectedItems()");

		operationPane.addOperation(resource.getIcon("pdcPeas.icoSearchPubli"), resource.getString("pdcPeas.searchResult"), "SearchView");
	}

    out.println(window.printBefore());

%>

<CENTER>
<form name="searchContext" action="ViewContext" method="post">
  <!-- champs cache pour voir ou non les axes secondaires -->
  <input type="hidden" name="ShowSndSearchAxis" value="<%=showSndSearchAxis%>">
  <input type="hidden" name="AxisId">
  <input type="hidden" name="ValueId">
  <input type="hidden" name="Ids">

  <table width="98%" border="0" cellspacing="0" cellpadding="3" class="intfdcolor">
        <tr>

      <td nowrap> <span class="txtGrandBlanc">&nbsp;<%=resource.getString("pdcPeas.mySearchContext")%></span>
	  <!-- contexte -->
        <table name="context" width="100%" border="0" cellspacing="1" cellpadding="4" bgcolor="#000000">
          <%
			if (!isEmptySearchContext){
				// on recupere tous les axisId et on construit le fullPath
				// comments please
				List list = null;
				String completPath = "";
				String id = "";
				String valueId = "";
				criteriaList = searchContext.getCriterias();
				SearchCriteria sc = null;
				if ( (pathCriteria != null) && (pathCriteria.size()>0) ){
					// comments please
					for (int k=0;k<pathCriteria.size();k++ ){
						list = (List)pathCriteria.get(k);
						completPath = buildCompletPath((ArrayList)list);
						sc = (SearchCriteria) criteriaList.get(k);
						id = new Integer(sc.getAxisId()).toString();
						valueId = (sc.getValue());
						// calcul du nombre de document !
						SearchAxis sa = null;
						boolean searchSecondAxis = true;
						for (int i=0;i<primaryAxis.size(); i++){
							sa = (SearchAxis)primaryAxis.get(i);
							axisId = new Integer(sa.getAxisId()).toString();
							if (axisId.equals(id)){
								nbPositions = sa.getNbObjects();
								searchSecondAxis = false;
								break;
							}
						}
						if ( (secondaryAxis != null) && (searchSecondAxis) ){
							for (int i=0;i<secondaryAxis.size(); i++){
								sa = (SearchAxis)secondaryAxis.get(i);
								axisId = new Integer(sa.getAxisId()).toString();
								if (axisId.equals(id)){
									nbPositions = sa.getNbObjects();
									break;
								}
							}
						}

			%>

					  <tr>
						<td width="100%" class="txtnav" bgcolor="EDEDED"><a href="javascript:modifySearchContext('<%=id%>-<%=valueId%>')"><img src="<%=resource.getIcon("pdcPeas.icoAxisFromContext")%>" align="absmiddle" border="0"></a>
						&nbsp;<%=completPath%>
						<%
						//prend le dernier element de la liste
						/*ArrayList term = (ArrayList) list.get(list.size() - 1);
						String termId = (String) term.get(1);
						String treeId = (String) term.get(2);*/
						Value term = (Value) list.get(list.size() - 1);
						String termId = term.getPK().getId();
						String treeId = term.getTreeId();

						out.println(displaySynonymsValue(activeThesaurus, jargon, treeId, termId));


						%>

						</td>
						<td bgcolor="#CCCCCC" align="center" class="textePetitBold">
						  <input type="checkbox" name="contextResearch" value="<%=id%>-<%=valueId%>">
						</td>
					  </tr>
		   <%
					} // fin du for
			%>
					  <tr>
						<td width="100%" align="right" bgcolor="EDEDED"><font color="#FF6600"><b></b></font>
						  <span class="textePetitBold"><%=resource.getString("pdcPeas.docsNumber")%> : </span></td>
						<td bgcolor="#CCCCCC" align="right" class="textePetitBold"><font color="#FF6600"><b><font face="Verdana, Arial, Helvetica, sans-serif" size="5" color="#666666"><%=nbPositions%></font></b></font>
						</td>
					  </tr>
			<%	} // fin du if
			} else {
			%>
				<tr>
					<td width="100%" class="txtnav" bgcolor="EDEDED">&nbsp;</td>
				</tr>
			<%
			} // fin else
		   %>
        </table>
      </td>
        </tr>
      </table>
      <br>

	<!-- axe primaire -->
      <table width="98%" border="0" cellspacing="0" cellpadding="3" class="intfdcolor51">
        <tr>
          <td nowrap> <span class="domainName">&nbsp;<%=resource.getString("pdcPeas.primaryAxis")%> </span>
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
			    <%
				// il peut y avoir aucun axe primaire dans un 1er temps
				if ( (primaryAxis != null) && (primaryAxis.size()>0) ){
					int nbPrimarySearchCriteria = 0; // le nombre de critere de recherche
					for (int i=0; i<primaryAxis.size(); i++){
						searchAxis = (SearchAxis)primaryAxis.get(i);
						axisId = new Integer(searchAxis.getAxisId()).toString();
						axisRootId = new Integer(searchAxis.getAxisRootId()).toString();
						axisName = EncodeHelper.javaStringToHtmlString(searchAxis.getAxisName());
						nbPositions = searchAxis.getNbObjects();
						// on n'affiche pas les axes que l'on a selectionne pour la recherche
						if (!axisCriteriaList.contains(axisId)){
				%>

							<%
								if (axisId.equals(selectedAxisId)){
									//recherche du detail de la valeur selectionnee
									String selectedValueFullPath = "";
									String selectedValuePath = "";
									for (int x = 0; x < daughters.size(); x++) {
										Value value = (Value) daughters.get(x);
										String valueId = value.getPK().getId();
										String valueFullPath = value.getFullPath();
										String valuePath = value.getPath();
										if (valueId.equals(selectedValueId)) {
											selectedValueFullPath = valueFullPath;
											selectedValuePath = valuePath;
										}
									}

									boolean odd = true;

									for (int j = 0; j<daughters.size(); j++) {
										Value value = (Value) daughters.get(j);
										String valueName = EncodeHelper.javaStringToHtmlString(value.getName());
										String valueId = value.getPK().getId();
										int valueLevel = value.getLevelNumber();
										int valueNbObjects = value.getNbObjects();
										String valueFullPath = value.getFullPath();
										String valueMotherId = value.getMotherId();
										String valuePath = value.getPath();
										String valueTreeId = value.getTreeId();
										//out.println("Daughter ValueID = "+valueId+"  valuePath = "+valuePath+" FullPath "+valueFullPath);

										String increment = "";

										String lineColor = "class=\"intfdcolor51\"";

										if (odd)
											lineColor = "class=\"intfdcolor4\"";;

										if (valueMotherId.equals(selectedValueId) || selectedValueFullPath.indexOf(valueMotherId) != -1 || selectedValueFullPath.indexOf(valueId) != -1 || valuePath.equals(selectedValuePath)) {

											for (int k = 0; k < valueLevel; k++) {
												increment += "<img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\">";
											}
											int descendantsNbObjects = getDescendantsNbObjects(valueId, daughters);
											//int descendantsNbObjects = valueNbObjects;
											out.println("<tr "+lineColor+"><td width=\"95%\">"+increment);
											if (selectedValueFullPath.indexOf(valueId) != -1) {
												if (descendantsNbObjects == 0) {
													out.println("<img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\">");
													out.println("<span class=\"textePetitBold\">"+valueName+"</span>");

													out.println(displaySynonymsValue(activeThesaurus, jargon, valueTreeId, valueId));

													out.println("("+descendantsNbObjects+")</td><td width=\"5%\" align=center>&nbsp;</td></tr>");
												} else {
													if (valueLevel == 0) {
														out.println("<a href=\"Main\"><img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\"></a>");
														out.println("<a href=\"Main\"><span class=\"textePetitBold\">"+valueName+"</span></a>");

														out.println(displaySynonymsValue(activeThesaurus, jargon, valueTreeId, valueId));

														out.println("("+valueNbObjects+")");
													} else {
														//collapse value
														out.println("<a href=\"javascript:collapseValue('"+axisId+"','"+valuePath+"')\"><img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\"></a>");
														out.println("<a href=\"javascript:collapseValue('"+axisId+"','"+valuePath+"')\"><span class=\"textePetitBold\">"+valueName+"</span></a>");

														out.println(displaySynonymsValue(activeThesaurus, jargon, valueTreeId, valueId));


														out.println("("+valueNbObjects+")");
													}
													out.println("</td><td width=\"5%\" align=center><input type=radio name=choix onClick=\"javascript:addCritere("+axisId+",'"+valueFullPath+"')\"></td></tr>");
												}
											} else {
												if (descendantsNbObjects == 0) {
													out.println("<img src=\""+resource.getIcon("pdcPeas.plus")+"\" border=0 align=\"absmiddle\">");
													out.println("<span class=\"textePetitBold\">"+valueName+"</span>");

													out.println(displaySynonymsValue(activeThesaurus, jargon, valueTreeId, valueId));

													out.println("("+descendantsNbObjects+")</td><td width=\"5%\" align=center>&nbsp;</td></tr>");
												} else {
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"','"+valuePath+"')\"><img src=\""+resource.getIcon("pdcPeas.plus")+"\" border=0 align=\"absmiddle\"></a>");
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"','"+valuePath+"')\"><span class=\"textePetitBold\">"+valueName+"</span></a>");

													out.println(displaySynonymsValue(activeThesaurus, jargon, valueTreeId, valueId));

													out.println("("+valueNbObjects+")");
													out.println("</td><td width=\"5%\" align=center><input type=radio name=choix onClick=\"javascript:addCritere("+axisId+",'"+valueFullPath+"')\"></td></tr>");
												}
											}
											odd = !odd;
										}
									}
								} else { // fin du if selectedAxis
									if (nbPositions != 0) {
							%>
									<tr class="intfdcolor4"><td width="95%"><a href="javascript:showDaughterValues('<%=axisId%>','/<%=axisRootId%>/')"><img src="<%=resource.getIcon("pdcPeas.plus")%>" border=0 align="absmiddle"></a>
									<a href="javascript:showDaughterValues('<%=axisId%>','/<%=axisRootId%>/')"><span class="textePetitBold"><%=axisName%></span></a>
									<%

									out.println(displaySynonymsAxis(activeThesaurus, jargon, axisId));

									%>
									  (<%=nbPositions%>)</td>
									<td width="5%" align=center><input type=radio name=choix onClick="javascript:addCritere('<%=axisId%>','/<%=axisRootId%>/')"></td></tr>
							<%
									}
								}
							} else {// fin du test sur l'axisId du contexte de recherche
								nbPrimarySearchCriteria++;
							}
							// maintenant, s'il n'y a plus d'axes car ils ont tous
							// ete selectionnes pour la recherche, on affiche une ligne vide
							if (nbPrimarySearchCriteria == primaryAxis.size()){
								out.println("<tr>");
								out.println("<td width=\"100%\" class=\"txtnav\" bgcolor=\"EDEDED\">&nbsp;</td>");
								out.println("</tr>");
							}
							%>
							<tr><td colspan=2 bgcolor=#666666><table border="0" cellspacing="0" cellpadding="0"><tr><td><img src="<%=resource.getIcon("pdcPeas.noColorPix")%>"></td></tr></table></td></tr>
							<%
						}// fin du for
				} else {
				%>
					<tr>
						<td width="100%" class="txtnav" bgcolor="EDEDED">&nbsp;</td>
					</tr>
				<%
				} // fin du else
				%>

            </table>

          </td>
        </tr>
      </table>
  <br>
  <!-- axe secondaire -->
  <%
	// il peut y avoir aucun axe secondaire
	if (secondaryAxis != null){
  %>
	<table width="98%" border="0" cellspacing="0" cellpadding="3" class="intfdcolor51">
        <tr>
          <td nowrap> <span class="domainName">&nbsp;<%=resource.getString("pdcPeas.secondaryAxis")%> </span>
            <table width="100%" border="0" cellspacing="0" cellpadding="0" bgcolor="#666666">
	  <%
				// il peut y avoir aucun axe secondaire
				if ( (secondaryAxis != null) && (secondaryAxis.size()>0) ){
					int nbSecondarySearchCriteria = 0; // le nombre de criteres de recherche
					for (int i=0; i<secondaryAxis.size(); i++){
						searchAxis = (SearchAxis)secondaryAxis.get(i);
						axisId = new Integer(searchAxis.getAxisId()).toString();
						axisRootId = new Integer(searchAxis.getAxisRootId()).toString();
						axisName = EncodeHelper.javaStringToHtmlString(searchAxis.getAxisName());
						nbPositions = searchAxis.getNbObjects();
						// on n'affiche pas les axes que l'on a selectionne pour la recherche
						if (!axisCriteriaList.contains(axisId)){
					%>
						<tr>
							<td bgcolor="#FFFFFF" width="100%">
							<% if (axisId.equals(selectedAxisId))	{
									//recherche du detail de la valeur selectionnee
									String selectedValueFullPath = "";
									String selectedValuePath = "";
									for (int x = 0; x < daughters.size(); x++) {
										Value value = (Value) daughters.get(x);
										String valueId = value.getPK().getId();
										String valueFullPath = value.getFullPath();
										String valuePath = value.getPath();
										if (valueId.equals(selectedValueId)) {
											selectedValueFullPath = valueFullPath;
											selectedValuePath = valuePath;
										}
									}

									boolean odd = true;

									for (int j = 0; j<daughters.size(); j++) {
										Value value = (Value) daughters.get(j);
										String valueName = value.getName();
										String valueId = value.getPK().getId();
										int valueLevel = value.getLevelNumber();
										int valueNbObjects = value.getNbObjects();
										String valueFullPath = value.getFullPath();
										String valueMotherId = value.getMotherId();
										String valuePath = value.getPath();
										String valueTreeId = value.getTreeId();

										String increment = "";

										String lineColor = "class=\"intfdcolor51\"";

										if (odd)
											lineColor = "class=\"intfdcolor4\"";;

										if (valueMotherId.equals(selectedValueId) || selectedValueFullPath.indexOf(valueMotherId) != -1 || selectedValueFullPath.indexOf(valueId) != -1 || valuePath.equals(selectedValuePath)) {

											for (int k = 0; k < valueLevel; k++) {
												increment += "<img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\">";
											}
											int descendantsNbObjects = getDescendantsNbObjects(valueId, daughters);
											//int descendantsNbObjects = valueNbObjects;
											out.println("<tr "+lineColor+"><td width=\"95%\">"+increment);
											if (selectedValueFullPath.indexOf(valueId) != -1) {
												if (descendantsNbObjects == 0) {
													out.println("<img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\">");
													out.println("<span class=\"textePetitBold\">"+valueName+"</span>");

													out.println(displaySynonymsValue(activeThesaurus, jargon, valueTreeId, valueId));

													out.println("("+descendantsNbObjects+")</td><td width=\"5%\" align=center>&nbsp;</td></tr>");
												} else {
													if (valueLevel == 0) {
														out.println("<a href=\"Main\"><img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\"></a>");
														out.println("<a href=\"Main\"><span class=\"textePetitBold\">"+valueName+"</span></a>");

														out.println(displaySynonymsValue(activeThesaurus, jargon, valueTreeId, valueId));

														out.println("("+valueNbObjects+")");
													} else {
														//collapse value
														out.println("<a href=\"javascript:collapseValue('"+axisId+"','"+valuePath+"')\"><img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\"></a>");
														out.println("<a href=\"javascript:collapseValue('"+axisId+"','"+valuePath+"')\"><span class=\"textePetitBold\">"+valueName+"</span></a>");

														out.println(displaySynonymsValue(activeThesaurus, jargon, valueTreeId, valueId));

														out.println("("+valueNbObjects+")");
													}
													out.println("</td><td width=\"5%\" align=center><input type=radio name=choix onClick=\"javascript:addCritere("+axisId+",'"+valueFullPath+"')\"></td></tr>");
												}
											} else {
												if (descendantsNbObjects == 0) {
													out.println("<img src=\""+resource.getIcon("pdcPeas.plus")+"\" border=0 align=\"absmiddle\">");
													out.println("<span class=\"textePetitBold\">"+valueName+"</span>");

													out.println(displaySynonymsValue(activeThesaurus, jargon, valueTreeId, valueId));

													out.println("("+descendantsNbObjects+")</td><td width=\"5%\" align=center>&nbsp;</td></tr>");
												} else {
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"','"+valuePath+"')\"><img src=\""+resource.getIcon("pdcPeas.plus")+"\" border=0 align=\"absmiddle\"></a>");
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"','"+valuePath+"')\"><span class=\"textePetitBold\">"+valueName+"</span></a>");

													out.println(displaySynonymsValue(activeThesaurus, jargon, valueTreeId, valueId));

													out.println("("+valueNbObjects+")");
													out.println("</td><td width=\"5%\" align=center><input type=radio name=choix onClick=\"javascript:addCritere("+axisId+",'"+valueFullPath+"')\"></td></tr>");
												}
											}
											odd = !odd;
										}
									}
							   } else {
								if (nbPositions != 0) {
								%>
									<tr class="intfdcolor4"><td width="95%"><a href="javascript:showDaughterValues('<%=axisId%>','/<%=axisRootId%>/')"><img src="<%=resource.getIcon("pdcPeas.plus")%>" border=0 align="absmiddle"></a>
									<a href="javascript:showDaughterValues('<%=axisId%>','/<%=axisRootId%>/')"><span class="textePetitBold"><%=axisName%></span></a>
									<%

									out.println(displaySynonymsAxis(activeThesaurus, jargon, axisId));

									%>

									   (<%=nbPositions%>)</td>
									<td width="5%" align=center><input type=radio name=choix onClick="javascript:addCritere('<%=axisId%>','/<%=axisRootId%>/')"></td></tr>
							<%	}
							 } %>
							</td>
						</tr>
						<%
						} else {// fin du test sur l'axisId du contexte de recherche
								nbSecondarySearchCriteria++;
						}
						// maintenant, s'il n'y a plus d'axes car ils ont tous
						// ete selectionnes pour la recherche, on affiche une ligne vide
						if (nbSecondarySearchCriteria == secondaryAxis.size()){
							out.println("<tr>");
							out.println("<td width=\"100%\" class=\"txtnav\" bgcolor=\"EDEDED\">&nbsp;</td>");
							out.println("</tr>");
						}
						%>
						<tr><td colspan=2 bgcolor=#666666><table border="0" cellspacing="0" cellpadding="0"><tr><td><img src="<%=resource.getIcon("pdcPeas.noColorPix")%>"></td></tr></table></td></tr>
						<%
					} // fin du for
				} else {
				%>
					<tr>
						<td width="100%" class="txtnav" bgcolor="EDEDED">&nbsp;</td>
					</tr>
				<%
				}
				%>
				</table>
          </td>
        </tr>
      </table>
<%
} // fin du if
%>

</CENTER>
<%
out.println(window.printAfter());
%>
</form>
</BODY>
</HTML>

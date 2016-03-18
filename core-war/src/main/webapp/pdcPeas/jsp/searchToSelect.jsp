<%@ page import="org.silverpeas.core.pdc.PdcServiceProvider" %><%--

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
<%@ include file="checkAdvancedSearch.jsp"%>

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
		if (jargon != null && activeThesaurus.booleanValue()) {//activ�
			//synonymes du terme
			String idUser = jargon.getIdUser();
			ThesaurusManager thesaurus = PdcServiceProvider.getThesaurusManager();
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
//
//recuperation des parametres pour le PDC
//
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
Boolean activeSelection = (Boolean) request.getAttribute("ActiveSelection");
if (activeSelection == null) {
	activeSelection = new Boolean(false);
}

//
// recuperation des parametres pour la recherche classique
//
UserDetail[] allAuthors = (UserDetail[])request.getAttribute("UserArray");
WAAttributeValuePair[] allComponents = (WAAttributeValuePair[])request.getAttribute("ComponentArray");
List allSpaces = (List)request.getAttribute("SpaceList");
String spaceSelected = (String)request.getAttribute("SpaceSelected");
String componentSelected = (String)request.getAttribute("ComponentSelected");
// recuperation du choix de l'utilisateur
String theSpace = (String) request.getAttribute("theSpace");
String theQuery = (String) request.getAttribute("theQuery");
theQuery = EncodeHelper.javaStringToHtmlString(theQuery);

String theAuthor = (String) request.getAttribute("theAuthor");
String theAfterDate = (String) request.getAttribute("theAfterDate");
String theBeforeDate = (String) request.getAttribute("theBeforeDate");
if (theSpace == null){
	theSpace = "";
}
if (theQuery == null){
	theQuery = "";
}
if (theAuthor == null){
	theAuthor = "";
}
if (theAfterDate == null){
	theAfterDate = "";
}
if (theBeforeDate == null){
	theBeforeDate = "";
}

boolean isEmptySearchContext = true;
int nbPositions = 0; // le nombre de documents pour un axe
String axisName = "";
String axisId = "";
String axisRootId = "";
SearchAxis searchAxis = null;
SearchCriteria searchCriteria = null;
List<String> axisCriteriaList = new ArrayList<>();

if (selectedAxisId == null)
	selectedAxisId = "";
if (selectedValueId == null)
	selectedValueId = "";
if (showSndSearchAxis == null)
	showSndSearchAxis = "NO";




// l'objet SearchContext n'est pas vide
// mais on le fait maintenant, car l'instruction precedente � un impact sur
// la liste de SearchContext
if ( (searchContext != null) && (searchContext.getCriterias().size() > 0) ){
	// on r�cup�re tous les contextes
  List<SearchCriteria> criteriaList = searchContext.getCriterias();
	// et on r�cup�re dans une liste tous les axes que ne doivent pas apparaitre
	// dans le primaire ou secondaire
	for (int i=0;i<criteriaList.size() ;i++ ){
		searchCriteria = criteriaList.get(i); // searchCriteria qui appartient au critere de recherche
		axisCriteriaList.add(Integer.toString(searchCriteria.getAxisId()));

	}
	isEmptySearchContext = false;
}



//
// declarations et precalcul pour la recherche classique
//

String firstName = "";
String lastName = "";
String authorId = "";


String componentName = "";
String label = "";


if (spaceSelected == null){
	spaceSelected = "";
}
if (componentSelected == null){
	componentSelected = "";
}
String separatorData = "";
if (allSpaces != null){
	// recupere le separateur
	separatorData = (String) allSpaces.remove(0);
}
String spaceName = "";
String spaceLab = "";
String selected = "";


%>

<html>
<!-- searchToSelect.jsp -->
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script language="JavaScript1.2">

function positionOfInput(inputName){
	var myForm = document.AdvancedSearch;
	var nbElementInForm = myForm.length;
	// pour chaque element, je recupere son nom et je le compare avec l'inputName
	// qd c'est egal (tjrs egal) je retourne la position du champ inputName
	var ret;
	for (var i=0;i<nbElementInForm ;i++ ){
	// ancien for (var i=nbElementInForm-1;i>0 ;i-- ){
		if (myForm.elements[i].name == inputName){
			ret = i;
			break;
		}
	}

	return ret;
}

	// IE / Netscape compliant

	// this method opens a pop-up which warns the user
	function areYouSure(){
		return confirm("<%=resource.getString("pdcPeas.confirmDeleteAxis")%>");
	}

	// this function get all checked boxes by the user and sent
	// data to the router
	function getSelectedItems(){
		var boxItems = document.AdvancedSearch.contextResearch;
		var  selectItems = "";
		if (boxItems != null){
			// au moins une checkbox exist
			var nbBox = boxItems.length;
			if ( (nbBox == null) && (boxItems.checked == true) ){
				// il n'y a qu'une checkbox selectionn�e
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
				document.AdvancedSearch.Ids.value = selectItems;
				document.AdvancedSearch.action = "GlobalDeleteCriteria";
				document.AdvancedSearch.submit();
			}
		}
	}

	// cette methode appelle le routeur avec le parametre ShowSndSearchAxis
	function viewSecondaryAxis(show){
		if (show){
			document.AdvancedSearch.ShowSndSearchAxis.value = "YES";
		} else {
			document.AdvancedSearch.ShowSndSearchAxis.value = "NO";
		}
		document.AdvancedSearch.submit();
	}

	// Montre les valeurs filles de la valeur choisie
	// et ferme normalement les autres valeur filles visualis�es
	function showDaughterValues(axisId,valueId){
		document.AdvancedSearch.AxisId.value = axisId;
		document.AdvancedSearch.ValueId.value = valueId;

		document.AdvancedSearch.action = "GlobalViewArbo";
		document.AdvancedSearch.submit();
	}

	// Montre les valeurs filles de la valeur choisie
	// et ferme normalement les autres valeur filles visualis�es
	function collapseValue(axisId,valueId){
		document.AdvancedSearch.AxisId.value = axisId;
		document.AdvancedSearch.ValueId.value = valueId;

		document.AdvancedSearch.action = "GlobalViewArbo";
		document.AdvancedSearch.submit();
	}

	function addCritere(axisId,valueId){
		document.AdvancedSearch.AxisId.value = axisId;
		document.AdvancedSearch.ValueId.value = valueId;

		document.AdvancedSearch.action = "GlobalAddCriteria";
		document.AdvancedSearch.submit();
	}

	function modifySearchContext(axisIdAndValueId){
		// on recupere les donn�es li�es � la s�lection
		document.AdvancedSearch.AxisId.value = axisIdAndValueId.split('-')[0];
		document.AdvancedSearch.ValueId.value = axisIdAndValueId.split('-')[1];

		document.AdvancedSearch.action = "GlobalModifyCriteria";
		document.AdvancedSearch.submit();
	}

	function sendQuery() {
		if (!<%=isEmptySearchContext%>){
			document.AdvancedSearch.SearchContext.value = "isNotEmpty";
		}
		document.AdvancedSearch.action = "AdvancedSearch";
		document.AdvancedSearch.submit();
	}

	function activateThesaurus () {
		document.AdvancedSearch.action = "GlobalActivateThesaurus";
		document.AdvancedSearch.submit();
	}

	function desactivateThesaurus () {
		document.AdvancedSearch.action = "GlobalDesactivateThesaurus";
		document.AdvancedSearch.submit();
	}

</script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setComponentName(resource.getString("pdcPeas.SearchPage"));

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

    // affichage de l'icone voir les axes secondaires ou les cacher
    if (secondaryAxis == null){
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDisplaySecondaryAxis"), resource.getString("pdcPeas.secondaryAxis"), "javascript:viewSecondaryAxis(true)");
    } else {
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDisplayPrimaryAxis"), resource.getString("pdcPeas.primaryAxis"), "javascript:viewSecondaryAxis(false)");
	}
	// on permet la suppression de critere
	if (!isEmptySearchContext)
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDeleteAxisFromContext"), resource.getString("pdcPeas.supprAxisFromContext"), "javascript:getSelectedItems()");
	if (activeSelection.booleanValue() && !isEmptySearchContext)
		operationPane.addOperation(resource.getIcon("pdcPeas.icoSearchPubli"), resource.getString("pdcPeas.searchResult"), "javascript:sendQuery()");

    out.println(window.printBefore());
%>

<CENTER>
<form name="AdvancedSearch" action="ViewAdvancedSearch" method="post">
  <!-- champs cach� pour voir ou non les axes secondaires -->
  <input type="hidden" name="ShowSndSearchAxis" value="<%=showSndSearchAxis%>">
  <input type="hidden" name="AxisId">
  <input type="hidden" name="ValueId">
  <input type="hidden" name="Ids">

<br>
  <table width="98%" border="0" cellspacing="0" cellpadding="3" class="intfdcolor">
        <tr>

      <td nowrap> <span class="txtGrandBlanc">&nbsp;<%=resource.getString("pdcPeas.mySearchContext")%></span>
	  <!-- contexte -->
        <table name="context" width="100%" border="0" cellspacing="1" cellpadding="4" bgcolor="#000000">
          <%
			if (!isEmptySearchContext){
		%>
		  <input type="hidden" name="SearchContext">
		<%
				// on recupere tous les axisId et on construit le fullPath
				// comments please
				List list = null;
				String completPath = "";
				String id = "";
				String valueId = "";
        List<SearchCriteria> criteriaList = searchContext.getCriterias();
				SearchCriteria sc = null;
				if ( (pathCriteria != null) && (pathCriteria.size()>0) ){
					// comments please
					for (int k=0;k<pathCriteria.size();k++ ){
						list = (List)pathCriteria.get(k);
						completPath = buildCompletPath((ArrayList)list);
						sc = criteriaList.get(k);
						id = Integer.toString(sc.getAxisId());
						valueId = (sc.getValue());
						// calcul du nombre de document !
						SearchAxis sa = null;
						boolean searchSecondAxis = true;
						for (int i=0;i<primaryAxis.size(); i++){
							sa = (SearchAxis)primaryAxis.get(i);
							axisId = Integer.toString(sa.getAxisId());
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
						  <span class="textePetitBold">Nombre de documents : </span></td>
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
						// on n'affiche pas les axes que l'on a s�lectionn� pour la recherche
						if (!axisCriteriaList.contains(axisId)){
							if (axisId.equals(selectedAxisId)){
									//recherche du detail de la valeur s�lectionn�e
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
														out.println("<a href=\"ViewAdvancedSearch\"><img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\"></a>");
														out.println("<a href=\"ViewAdvancedSearch\"><span class=\"textePetitBold\">"+valueName+"</span></a>");

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
													out.println("<span class=\"textePetitBold\">"+valueName+"</span> ");

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

									(<%=nbPositions%>)


									</td>
									<td width="5%" align=center><input type=radio name=choix onClick="javascript:addCritere('<%=axisId%>','/<%=axisRootId%>/')"></td></tr>
							<%
									}
								}
							} else {// fin du test sur l'axisId du contexte de recherche
								nbPrimarySearchCriteria++;
							}
							// maintenant, s'il n'y a plus d'axes car ils ont tous
							// �t� s�lectionn�s pour la recherche, on affiche une ligne vide
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
				if ( (secondaryAxis != null) && (secondaryAxis.size()>0) && (showSndSearchAxis.equals("YES")) ){
					int nbSecondarySearchCriteria = 0; // le nombre de criteres de recherche
					for (int i=0; i<secondaryAxis.size(); i++){
						searchAxis = (SearchAxis)secondaryAxis.get(i);
						axisId = new Integer(searchAxis.getAxisId()).toString();
						axisRootId = new Integer(searchAxis.getAxisRootId()).toString();
						axisName = EncodeHelper.javaStringToHtmlString(searchAxis.getAxisName());
						nbPositions = searchAxis.getNbObjects();
						// on n'affiche pas les axes que l'on a s�lectionn� pour la recherche
						if (!axisCriteriaList.contains(axisId)){

					%>
						<tr>
							<td bgcolor="#FFFFFF" width="100%">
							<% if (axisId.equals(selectedAxisId))	{
									//recherche du detail de la valeur s�lectionn�e
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
														out.println("<a href=\"ViewAdvancedSearch\"><img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\"></a>");
														out.println("<a href=\"ViewAdvancedSearch\"><span class=\"textePetitBold\">"+valueName+"</span></a>");

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
						// �t� s�lectionn�s pour la recherche, on affiche une ligne vide

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
</form>
<%
	out.println(window.printAfter());
%>
</BODY>
</HTML>
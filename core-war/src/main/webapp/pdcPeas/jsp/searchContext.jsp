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
if ( (searchContext != null) && (searchContext.getCriterias().size() > 0) ){
	// on r�cup�re tous les contextes
	List<SearchCriteria> criteriaList = searchContext.getCriterias();
	// et on r�cup�re dans une liste tous les axes que ne doivent pas apparaitre
	// dans le primaire ou secondaire
	for (int i=0;i<criteriaList.size() ;i++ ){
		searchCriteria = criteriaList.get(i);
		axisCriteriaList.add(Integer.toString(searchCriteria.getAxisId()));
	}
	isEmptySearchContext = false;
}
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
				document.searchContext.Ids.value = selectItems;
				document.searchContext.action = "DeleteCriteria";
				document.searchContext.submit();
			}
		}
	}

	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		//SP_openWindow('NewAxis', 'newaxis', '600', '250','scrollbars=yes, resizable, alwaysRaised');
		SP_openWindow(fonction, windowName, '600', '300','scrollbars=yes, resizable, alwaysRaised');
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
	// et ferme normalement les autres valeur filles visualis�es
	function showDaughterValues(axisId,valueId){
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
		// on recupere les donn�es li�es � la s�lection
		document.searchContext.AxisId.value = axisIdAndValueId.split('-')[0];
		document.searchContext.ValueId.value = axisIdAndValueId.split('-')[1];

		document.searchContext.action = "ModifyCriteria";
		document.searchContext.submit();

	}
</script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setDomainName("Ergonomie");
    browseBar.setComponentName("PdcPeas");
	browseBar.setPath("");

    // affichage de l'icone voir les axes secondaires ou les cacher
    if (secondaryAxis == null){
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDisplaySecondaryAxis"), resource.getString("pdcPeas.secondaryAxis"), "javascript:viewSecondaryAxis(true)");
    } else {
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDisplayPrimaryAxis"), resource.getString("pdcPeas.primaryAxis"), "javascript:viewSecondaryAxis(false)");
	}
	// on permet la suppression de critere
	if (!isEmptySearchContext)
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDeleteAxisFromContext"), resource.getString("pdcPeas.supprAxisFromContext"), "javascript:getSelectedItems()");

	operationPane.addOperation(resource.getIcon("pdcPeas.icoSearchPubli"), resource.getString("pdcPeas.searchResult"), "SearchView");

    out.println(window.printBefore());

%>

<CENTER>
<form name="searchContext" action="ViewContext" method="post">
  <!-- champs cach� pour voir ou non les axes secondaires -->
  <input type="hidden" name="ShowSndSearchAxis" value="<%=showSndSearchAxis%>">
  <input type="hidden" name="AxisId">
  <input type="hidden" name="ValueId">
  <input type="hidden" name="Ids">

  <table width="98%" border="0" cellspacing="0" cellpadding="3" bgcolor="#CC0066">
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
						<td width="100%" class="txtnav" bgcolor="EDEDED">
						<a href="javascript:modifySearchContext('<%=id%>-<%=valueId%>')" class="txtnav" >
							<img src="<%=resource.getIcon("pdcPeas.icoAxisFromContext")%>" align="absmiddle" border="0">
							<%=completPath%>
						</a>
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
      <table width="98%" border="0" cellspacing="0" cellpadding="3" bgcolor="#66CC99">
        <tr>
          <td nowrap> <span class="txtGrandBlanc">&nbsp;<%=resource.getString("pdcPeas.primaryAxis")%> </span>
            <table width="100%" border="0" cellspacing="1" cellpadding="0" bgcolor="#666666">
			    <%
				String whiteBand = "White";		// On affecte des couleurs de fonds pour chaque axe
				String greyBand = "EDEDED"; // Afin de les differencier pour le choix du bouton radio :-(
				String colorBand = whiteBand; // par defaut blanc mais par la suite soit gris, soit blanc
				// il peut y avoir aucun axe primaire dans un 1er temps
				if ( (primaryAxis != null) && (primaryAxis.size()>0) ){
					int nbPrimarySearchCriteria = 0; // le nombre de critere de recherche
					for (int i=0; i<primaryAxis.size(); i++){
						out.println("<tr>");
						out.println("<td>");
						out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\" >");
						searchAxis = (SearchAxis)primaryAxis.get(i);
						axisId = new Integer(searchAxis.getAxisId()).toString();
						axisRootId = new Integer(searchAxis.getAxisRootId()).toString();
						axisName = EncodeHelper.javaStringToHtmlString(searchAxis.getAxisName());
						nbPositions = searchAxis.getNbObjects();
						// on n'affiche pas les axes que l'on a s�lectionn� pour la recherche
						if (!axisCriteriaList.contains(axisId)){
							// S'il s'agit d'un multiple de deux alors on grise
							if ( (i%2)==0 ){
								colorBand = greyBand;
							} else {
								colorBand = whiteBand;
							}
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
									for (int j = 0; j<daughters.size(); j++) {
										Value value = (Value) daughters.get(j);
										String valueName = EncodeHelper.javaStringToHtmlString(value.getName());
										String valueId = value.getPK().getId();
										int valueLevel = value.getLevelNumber();
										int valueNbObjects = value.getNbObjects();
										String valueFullPath = value.getFullPath();
										String valueMotherId = value.getMotherId();
										String valuePath = value.getPath();
										//out.println("Daughter ValueID = "+valueId+"  valuePath = "+valuePath+" FullPath "+valueFullPath);

										String increment = "";

										out.println("<tr>");
										out.println("<td width=\"95%\" bgcolor=\""+colorBand+"\" nowrap>");
										if (valueMotherId.equals(selectedValueId) || selectedValueFullPath.indexOf(valueMotherId) != -1 || selectedValueFullPath.indexOf(valueId) != -1 || valuePath.equals(selectedValuePath)) {

											for (int k = 0; k < valueLevel; k++) {
												increment += "<img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\">";
											}
											out.println(increment);
											int descendantsNbObjects = getDescendantsNbObjects(valueId, daughters);
											if (selectedValueFullPath.indexOf(valueId) != -1) {
												if (descendantsNbObjects == 0) {
													out.println("<img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\">");
													out.println("<span class=\"textePetitBold\">"+valueName+"</span> ("+descendantsNbObjects+")<BR>");
													out.println("</td>");
													out.println("<td width=\"5%\" bgcolor=\""+colorBand+"\" nowrap>");
													out.println("&nbsp;</td>");
												} else {
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"')\"><img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\"></a>");
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"')\"><span class=\"textePetitBold\">"+valueName+"</span></a> ("+descendantsNbObjects+")");
													out.println("</td>");
													out.println("<td width=\"5%\" bgcolor=\""+colorBand+"\" nowrap>");
													out.println("<input type=radio name=choix onClick=\"javascript:addCritere("+axisId+",'"+valueFullPath+"')\"><BR>");
													out.println("</td>");
												}
											} else {
												if (descendantsNbObjects == 0) {
													out.println("<img src=\""+resource.getIcon("pdcPeas.plus")+"\" border=0 align=\"absmiddle\">");
													out.println("<span class=\"textePetitBold\">"+valueName+"</span> ("+descendantsNbObjects+")<BR>");
													out.println("</td>");
													out.println("<td width=\"5%\" bgcolor=\""+colorBand+"\" nowrap>");
													out.println("&nbsp;</td>");
												} else {
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"')\"><img src=\""+resource.getIcon("pdcPeas.plus")+"\" border=0 align=\"absmiddle\"></a>");
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"')\"><span class=\"textePetitBold\">"+valueName+"</span></a> ("+descendantsNbObjects+")");
													out.println("</td>");
													out.println("<td width=\"5%\" bgcolor=\""+colorBand+"\" nowrap>");
													out.println("<input type=radio name=choix onClick=\"javascript:addCritere("+axisId+",'"+valueFullPath+"')\"><BR>");
													out.println("</td>");
												}
											}
										}
																out.println("</tr>");
							// S'il s'agit d'un multiple de deux alors on grise
							if ( (j%2)==0 ){
								colorBand = greyBand;
							} else {
								colorBand = whiteBand;
							}

									} // fin du for des filles
								} else { // fin du if selectedAxis
							%>
									<td width="95%" bgcolor="<%=colorBand%>" nowrap>
									<a href="javascript:showDaughterValues('<%=axisId%>','/<%=axisRootId%>/')"><img src="<%=resource.getIcon("pdcPeas.plus")%>" border=0 align="absmiddle"></a>
									<a href="javascript:showDaughterValues('<%=axisId%>','/<%=axisRootId%>/')"><span class="textePetitBold"><%=axisName%></span></a> (<%=nbPositions%>)
									</td>
									<td  width="5%" bgcolor="<%=colorBand%>" >
									<input type=radio name=choix onClick="javascript:addCritere(<%=axisId%>,'/<%=axisRootId%>/')">
									</td>
					<%
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
						}// fin du for des axes
						out.println("</td>");
						out.println("</tr>");
					out.println("</table>");
				} else {
					//out.println("<tr>");
					//out.println("<td width=\"100%\" class=\"txtnav\" bgcolor=\""+colorBand+"\">&nbsp;</td>");
					out.println("&nbsp;");
					//out.println("</tr>");
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
	<table width="98%" border="0" cellspacing="0" cellpadding="3" bgcolor="#66CC99">
        <tr>
          <td nowrap> <span class="txtGrandBlanc">&nbsp;<%=resource.getString("pdcPeas.secondaryAxis")%> </span>
            <table width="100%" border="0" cellspacing="1" cellpadding="0" bgcolor="#666666">
	  <%
				colorBand = whiteBand; // par defaut blanc mais par la suite soit gris, soit blanc
				// il peut y avoir aucun axe secondaire
				if ( (secondaryAxis != null) && (secondaryAxis.size()>0) ){
					int nbSecondarySearchCriteria = 0; // le nombre de criteres de recherche

					for (int i=0; i<secondaryAxis.size(); i++){
						out.println("<tr>");
						out.println("<td>");
						out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\" >");
						searchAxis = (SearchAxis)secondaryAxis.get(i);
						axisId = new Integer(searchAxis.getAxisId()).toString();
						axisRootId = new Integer(searchAxis.getAxisRootId()).toString();
						axisName = searchAxis.getAxisName();
						nbPositions = searchAxis.getNbObjects();
						// on n'affiche pas les axes que l'on a s�lectionn� pour la recherche
						if (!axisCriteriaList.contains(axisId)){

							// S'il s'agit d'un multiple de deux alors on grise
							if ( (i%2)==0 ){
								colorBand = greyBand;
							} else {
								colorBand = whiteBand;
							}
							if (axisId.equals(selectedAxisId))	{
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
									for (int j = 0; j<daughters.size(); j++) {
										Value value = (Value) daughters.get(j);
										String valueName = value.getName();
										String valueId = value.getPK().getId();
										int valueLevel = value.getLevelNumber();
										int valueNbObjects = value.getNbObjects();
										String valueFullPath = value.getFullPath();
										String valueMotherId = value.getMotherId();
										String valuePath = value.getPath();

										String increment = "";

										out.println("<tr>");
										out.println("<td width=\"95%\" bgcolor=\""+colorBand+"\" nowrap>");
										if (valueMotherId.equals(selectedValueId) || selectedValueFullPath.indexOf(valueMotherId) != -1 || selectedValueFullPath.indexOf(valueId) != -1 || valuePath.equals(selectedValuePath)) {

											for (int k = 0; k < valueLevel; k++) {
												increment += "<img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\">";
											}
											out.println(increment);
											int descendantsNbObjects = getDescendantsNbObjects(valueId, daughters);
											if (selectedValueFullPath.indexOf(valueId) != -1) {
												if (descendantsNbObjects == 0) {
													out.println("<img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\">");
													out.println("<span class=\"textePetitBold\">"+valueName+"</span> ("+descendantsNbObjects+")<BR>");
													out.println("</td>");
													out.println("<td width=\"5%\" bgcolor=\""+colorBand+"\" nowrap>");
													out.println("&nbsp;</td>");
												} else {
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"')\"><img src=\""+resource.getIcon("pdcPeas.minus")+"\" border=0 align=\"absmiddle\"></a>");
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"')\"><span class=\"textePetitBold\">"+valueName+"</span></a> ("+descendantsNbObjects+")");
													out.println("</td>");
													out.println("<td width=\"5%\" bgcolor=\""+colorBand+"\" nowrap>");
													out.println("<input type=radio name=choix onClick=\"javascript:addCritere("+axisId+",'"+valueFullPath+"')\"><BR>");
													out.println("</td>");
												}
											} else {
												if (descendantsNbObjects == 0) {
													out.println("<img src=\""+resource.getIcon("pdcPeas.plus")+"\" border=0 align=\"absmiddle\">");
													out.println("<span class=\"textePetitBold\">"+valueName+"</span> ("+descendantsNbObjects+")<BR>");
													out.println("</td>");
													out.println("<td width=\"5%\" bgcolor=\""+colorBand+"\" nowrap>");
													out.println("&nbsp;</td>");
												} else {
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"')\"><img src=\""+resource.getIcon("pdcPeas.plus")+"\" border=0 align=\"absmiddle\"></a>");
													out.println("<a href=\"javascript:showDaughterValues('"+axisId+"','"+valueFullPath+"')\"><span class=\"textePetitBold\">"+valueName+"</span></a> ("+descendantsNbObjects+")");
													out.println("</td>");
													out.println("<td width=\"5%\" bgcolor=\""+colorBand+"\" nowrap>");
													out.println("<input type=radio name=choix onClick=\"javascript:addCritere("+axisId+",'"+valueFullPath+"')\"><BR>");
													out.println("</td>");
												}
											}
										}
																out.println("</tr>");
							// S'il s'agit d'un multiple de deux alors on grise
							if ( (j%2)==0 ){
								colorBand = greyBand;
							} else {
								colorBand = whiteBand;
							}

									} // fin du for des filles
							   } else { %>
									<td width="95%" bgcolor="<%=colorBand%>" nowrap>
									<a href="javascript:showDaughterValues('<%=axisId%>','/<%=axisRootId%>/')"><img src="<%=resource.getIcon("pdcPeas.plus")%>" border=0 align="absmiddle"></a>
									<a href="javascript:showDaughterValues('<%=axisId%>','/<%=axisRootId%>/')"><span class="textePetitBold"><%=axisName%></span></a> (<%=nbPositions%>)
									</td>
									<td  width="5%" bgcolor="<%=colorBand%>" >
									<input type=radio name=choix onClick="javascript:addCritere('<%=axisId%>','/<%=axisRootId%>/')">
									</td>
							<% } %>
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
							out.println("<td width=\"100%\" class=\"txtnav\" bgcolor=\""+colorBand+"\">&nbsp;</td>");
							out.println("</tr>");
						}

					} // fin du for des axes
						out.println("</td>");
						out.println("</tr>");
					out.println("</table>");
				} else { // fin du il ne peut y avoir d'axe II
					//out.println("<tr>");
					//out.println("<td width=\"100%\" class=\"txtnav\" bgcolor=\""+colorBand+"\">&nbsp;</td>");
					out.println("&nbsp;");
					//out.println("</tr>");
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

<%

	/******************************************************
	****            Affichage des axes                ****
	******************************************************/

%>



<CENTER>
  <table width="98%" border="0" cellspacing="0" cellpadding="3" bgcolor="#CC0066">
    <tr>
      <td nowrap> <span class="txtGrandBlanc">&nbsp;Contexte de recherche :</span>
        <table width="100%" border="0" cellspacing="1" cellpadding="4" bgcolor="#000000">
          <tr>
            <td width="100%" bgcolor="EDEDED"><a href="#" class="txtnav" title="Modifier"><img src="<%=resource.getIcon("pdcPeas.icoAxisFromContext")%>" align="absmiddle" border=0>&nbsp;Serveur
              d'application / IBM</a></td>
            <td bgcolor="#CCCCCC" align="center" class="textePetitBold">
              <input type="checkbox" name="checkbox2" value="checkbox">
            </td>
          </tr>
          <tr>
            <td width="100%" align="right" bgcolor="EDEDED"><font color="#FF6600"><b></b></font>
              <span class="textePetitBold"><%=resource.getString("pdcPeas.docsNumber")%> : </span></td>
            <td bgcolor="#CCCCCC" align="right" class="textePetitBold"><font color="#FF6600"><b><font face="Verdana, Arial, Helvetica, sans-serif" size="5" color="#666666">105</font></b></font>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
  <br>
  <table width="98%" border="0" cellspacing="0" cellpadding="3" bgcolor="#66CC99">
    <tr>
      <td nowrap> <span class="txtGrandBlanc">&nbsp;Axes primaires : </span>
        <table width="100%" border="0" cellspacing="1" cellpadding="0" bgcolor="#666666">
          <tr>
            <td bgcolor="#FFFFFF" width="100%">
              <table width="100%" border="0" cellspacing="0" cellpadding="1">
                <tr>
                  <td width="95%" nowrap><a href="#"><img src="<%=resource.getIcon("pdcPeas.minus")%>" border=0 align="absmiddle"></a>
				  <a href="#"><span class="textePetitBold">Serveurs
                    d'applications (3)</span></a></td>
                  <td width="5%" align="center">
                    <input type="radio" name="radiobutton" value="radiobutton">
                  </td>
                </tr>
                <tr>
                  <td bgcolor="EDEDED" nowrap>&nbsp;&nbsp;&nbsp;<img src="<%=resource.getIcon("pdcPeas.plus")%>" border=0 align="absmiddle">
				  <a href="#">BEA-WebLogic
                    (2)</a></td>
                  <td bgcolor="EDEDED" align="center">
                    <input type="radio" name="radiobutton" value="radiobutton">
                  </td>
                </tr>
                <tr>
                  <td nowrap>&nbsp;&nbsp;&nbsp;<img src="<%=resource.getIcon("pdcPeas.plus")%>" border=0 align="absmiddle"><a href="#">IBM
                    (4)</a></td>
                  <td align="center">
                    <input type="radio" name="radiobutton" value="radiobutton" checked>
                  </td>
                </tr>
                <tr>
                  <td bgcolor="EDEDED" nowrap>&nbsp;&nbsp;&nbsp;<img src="<%=resource.getIcon("pdcPeas.minus")%>" border=0 align="absmiddle">
				  <a href="#">Orion
                    (1)</a></td>
                  <td bgcolor="EDEDED" align="center">
                    <input type="radio" name="radiobutton" value="radiobutton">
                  </td>
                </tr>
                <tr>
                  <td nowrap>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="<%=resource.getIcon("pdcPeas.plus")%>" border=0 align="absmiddle"
				  ><a href="#">Orion
                    1.52 (1)</a></td>
                  <td align="center">
                    <input type="radio" name="radiobutton" value="radiobutton">
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        <table width="100%" border="0" cellspacing="1" cellpadding="0" bgcolor="#666666">
          <tr>
            <td bgcolor="#FFFFFF" width="100%">
              <table width="100%" border="0" cellspacing="0" cellpadding="1">
                <tr>
                  <td width="95%" nowrap><img src="<%=resource.getIcon("pdcPeas.plus")%>" border=0 align="absmiddle"><a href="#">
				  <span class="textePetitBold">Documentation
                    (3)</span></a></td>
                  <td width="5%" align="center">
                    <input type="radio" name="radiobutton" value="radiobutton">
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
  <br>
  <table width="98%" border="0" cellspacing="0" cellpadding="3" bgcolor="#66CC99">
    <tr>
      <td nowrap> <span class="txtGrandBlanc">&nbsp;Axes psecondaires : </span>
        <table width="100%" border="0" cellspacing="1" cellpadding="0" bgcolor="#666666">
          <tr>
            <td bgcolor="#FFFFFF" width="100%">
              <table width="100%" border="0" cellspacing="0" cellpadding="1">
                <tr>
                  <td width="95%" nowrap><img src="<%=resource.getIcon("pdcPeas.plus")%>" border=0 align="absmiddle"><a href="#">
				  <span class="textePetitBold">Publication
                    (3)</span></a></td>
                  <td width="5%" align="center">
                    <input type="radio" name="radiobutton" value="radiobutton">
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</CENTER>

</BODY>
</HTML>
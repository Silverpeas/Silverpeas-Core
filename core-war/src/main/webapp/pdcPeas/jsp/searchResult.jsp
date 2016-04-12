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

ArrayLine arrayLine = null;

SearchContext searchContext = (SearchContext) request.getAttribute("SearchContext");
List pathCriteria = (List) request.getAttribute("PathCriteria");
Jargon jargon = (Jargon) request.getAttribute("Jargon");
Boolean activeThesaurus = (Boolean) request.getAttribute("ActiveThesaurus");

boolean isEmptySearchContext = true;
int nbPositions = 0; // le nombre de documents pour un axe
SearchAxis searchAxis = null;
SearchCriteria searchCriteria = null;
List criteriaList = new ArrayList();
List axisCriteriaList = new ArrayList();

// l'objet SearchContext n'est pas vide
if ( (searchContext != null) && (searchContext.getCriterias().size() > 0) ){
	// on recupere tous les contextes
	criteriaList = searchContext.getCriterias();
	// et on recupere dans une liste tous les axes que ne doivent pas apparaitre
	// dans le primaire ou secondaire
	for (int i=0;i<criteriaList.size() ;i++ ){
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

// Contenu
String sName = null;
String sDescription = null;
String sURL = null;
List alSilverContents = containerWorkspace.getSilverContents();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script language="JavaScript">
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

	function modifySearchContext(axisIdAndValueId){
		// on recupere les donees liees a la selection
		document.searchContext.AxisId.value = axisIdAndValueId.split('-')[0];
		document.searchContext.ValueId.value = axisIdAndValueId.split('-')[1];

		document.searchContext.action = "ModifyCriteria";
		document.searchContext.submit();

	}
</script>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");

    operationPane.addOperation(resource.getIcon("pdcPeas.icoModifyAxisFromContext"), resource.getString("pdcPeas.modifySearchContext"), "Main");

	// on permet la suppression de critere
	if (!isEmptySearchContext)
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDeleteAxisFromContext"), resource.getString("pdcPeas.supprAxisFromContext"), "javascript:getSelectedItems()");

    out.println(window.printBefore());

%>
<CENTER>
<form name="searchContext" action="ViewContext" method="post">
  <!-- champs cache pour voir ou non les axes secondaires -->
  <input type="hidden" name="AxisId">
  <input type="hidden" name="ValueId">
  <input type="hidden" name="Ids">

  <table width="98%" border="0" cellspacing="0" cellpadding="3" class=intfdcolor>
    <tr>
      <td> <span class="txtGrandBlanc">&nbsp;<%=resource.getString("pdcPeas.mySearchContext")%></span> <br/>
        <!--<table width="100%" border="0" cellspacing="1" cellpadding="4" bgcolor="#000000">
          <tr>
            <td width="100%" class="intfdcolor51"><img src="<%=resource.getIcon("pdcPeas.icoAxisFromContext")%>" align="absmiddle"><span class="txtnav">&nbsp;Syst&egrave;me
              d'exploitation</span></td>
          </tr>
          <tr>
            <td width="100%" class="intfdcolor51"><img src="<%=resource.getIcon("pdcPeas.icoAxisFromContext")%>" align="absmiddle"><span class="txtnav">&nbsp;Serveur
              d'application / IBM</span></td>
          </tr>
        </table>-->
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
			%>

					  <tr>
						<td width="100%" class="txtnav" bgcolor="EDEDED"><a href="javascript:modifySearchContext('<%=id%>-<%=valueId%>')"><img src="<%=resource.getIcon("pdcPeas.icoAxisFromContext")%>" align="absmiddle" border="0"></a>
						&nbsp;<%=completPath%>
						<%
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
				} // fin du if
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
  <%
    out.println(separator);
	// Affichage des resultats de la recherche

	// Publication du contenu

    ArrayPane arrayPane = gef.getArrayPane("PdcPeas", "", request, session);

    ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);

    ArrayColumn arrayColumn1 =  arrayPane.addArrayColumn(resource.getString("GML.name"));
	arrayColumn1.setSortable(false);

	ArrayColumn arrayColumn2 =  arrayPane.addArrayColumn(resource.getString("GML.description"));
	arrayColumn2.setSortable(false);

for(int nI=0; nI < alSilverContents.size(); nI++)

{
		sName = ((SilverContentInterface)alSilverContents.get(nI)).getName();
		sDescription = ((SilverContentInterface)alSilverContents.get(nI)).getDescription();
		sURL = ((SilverContentInterface)alSilverContents.get(nI)).getURL();

		// Encode the ? with %3f
		String sBuf = sURL;
		int nIndex = sURL.indexOf("?");
		if(nIndex != -1)
		{
				sBuf = sURL.substring(0, nIndex) + "%3f" + sURL.substring(nIndex+1, sURL.length());
				sURL = sBuf;
		}
		// Encode the = with %3d
		sBuf = sURL;
		nIndex = sURL.indexOf("=");
		while(nIndex != -1)
		{
				sBuf = sURL.substring(0, nIndex) + "%3d" + sURL.substring(nIndex+1, sURL.length());
				sURL = sBuf;
				nIndex = sURL.indexOf("=");
		}

	    arrayLine = arrayPane.addArrayLine();

	    arrayLine.addArrayCellLink("<img src=\""+resource.getIcon("pdcPeas.icoMiniPubli")+"\" border=0 alt=\""+resource.getString("GML.popupTitle")+"\" title=\""+resource.getString("GML.popupTitle")+"\">","ContentForward?contentURL="+sURL);
	    arrayLine.addArrayCellLink("<span class=textePetitBold>"+sName+"</span>","ContentForward?contentURL="+sURL);
	    arrayLine.addArrayCellText(sDescription);
}
	out.println(arrayPane.print());
%>
</CENTER>
<%
out.println(window.printAfter());
%>
<form name="refresh" action="Main" method="post"></form>
</body>
</html>
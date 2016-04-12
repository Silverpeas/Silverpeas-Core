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

boolean isValueAllowed(String valuePath, String baseValuePaths) {
	StringTokenizer st = new StringTokenizer(baseValuePaths,",");
	for (;st.hasMoreTokens();){
		if (valuePath.indexOf(st.nextToken()) != -1)
			return true;
	}
	return false;
}

boolean valueIsBaseValue(String valueId, String baseValueIds) {
	StringTokenizer st = new StringTokenizer(baseValueIds,",");
	for (;st.hasMoreTokens();){
		if (st.nextToken().equals(valueId))
			return true;
	}
	return false;
}

boolean valueIsAscendant(String valueId, String baseValuePaths) {
	StringTokenizer st = new StringTokenizer(baseValuePaths,",");
	for (;st.hasMoreTokens();){
		if (st.nextToken().indexOf("/"+valueId+"/") != -1)
			return true;
	}
	return false;
}

String getValueSearchContext(SearchContext searchContext, int axisId)
{
		if (searchContext != null) { //searchContext peut etre null si on a chang� la selection d'une combo
			List alCriterias = searchContext.getCriterias();
			for(int nI=0; alCriterias != null && nI < alCriterias.size(); nI++)
			{
					SearchCriteria criteria = (SearchCriteria) alCriterias.get(nI);
					if(criteria.getAxisId() == axisId)
							return criteria.getValue();
			}
		}

		return null;
}

//fonction qui sert a afficher les synonymes du terme selectionn� dans la combo
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
	List 			usedAxis 		= (List) request.getAttribute("UsedAxis");
	SearchContext 	searchContext 	= (SearchContext) request.getAttribute("SearchContext"); //est null si a chang� la selection d'une combo
	Jargon 			jargon 			= (Jargon) request.getAttribute("Jargon");
	Boolean 		activeThesaurus = (Boolean) request.getAttribute("ActiveThesaurus");
	Collection 		listValues 		= (Collection) request.getAttribute("ListValues");

	UsedAxis	axis			= null;
	List		axisValues		= null;
	int			axisId			= -1;
	String		axisName		= null;
	int			mandatory		= 0;
	int			variant			= 0;
	String		invariantValue	= null;
	String		baseValueId		= null;
	String		baseValuePath	= null;

	Value		value			= null;
	String		valueName		= null;
	String		valueId			= null;
	String		valuePath		= null;
	String 		valueTreeId     = null;
	int			valueLevel		= -1;
	String		increment		= null;

	String		axisIdsUsed		= null;
	int			nbAxis			= -1;

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<script language="javascript">
function listValues () {
	z = "";
	nbAxis = 0;
	i = 0;
	while (i<document.chooseValues.length-1) {
		if (document.chooseValues.elements[i].value.length != 0) {
            if (nbAxis != 0)
                z += ",";
            nbAxis = 1;
            z += document.chooseValues.elements[i].value;
        }
		i = i+3;
	}
	return z;

}

function checkSelectedValues()
{
	i = 0;
	var axisIdAndValue = "";
	var errorMsg = "";
    var errorNb = 0;
	while (i<document.chooseValues.length-1) {
		if (document.chooseValues.elements[i].value.length != 0) {
			axisIdAndValue = document.chooseValues.elements[i].value.split("|");
			axisId = axisIdAndValue[0];
			value = axisIdAndValue[1];
			if (value == '/0/')
			{
				errorMsg += "- "+document.chooseValues.elements[i+1].value+"\n";
				errorNb++;
			}
        }
		i = i+3;
	}
	switch(errorNb) {
        case 0 :
            result = true;
            break;
        default :
            errorMsg = "<%=resource.getString("pdcPeas.rootValueNotAllowed")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
    return result;
}

function sendData() {

	if (checkSelectedValues())
	{
		document.submitValues.Values.value = listValues();
		document.submitValues.action = "<%=pdcClassifyContext%>CreatePosition";
		document.submitValues.submit();
	}
}

function test(object, reload) {
	if (object.options[object.selectedIndex].value == 'A') {
		//There is a base value, selected value is a parent of this base value
		//This is not allowed. So, base value must be selected by default
		var baseValue = object.name+"|"+document.getElementById("BaseValue_"+object.name).value;
		//alert(object.name + " = "+baseValue);
		for(i=0;i<object.length;++i)
		{
			if(object.options[i].value == baseValue)
				object.selectedIndex = i;
		}
	}

	if (reload == 'true') {
		document.submitValues.Values.value = listValues();
		//recharge la page
		document.submitValues.action = "<%=pdcClassifyContext%>ReloadPosition";
		document.submitValues.submit();
	}
}
</script>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<FORM name="chooseValues" method="post">

  <%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setExtraInformation(resource.getString("pdcPeas.classifyAdd"));

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
  %>
  <CENTER>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
  <% for (int i = 0; i < usedAxis.size(); i++) {
		axis			=	(UsedAxis) usedAxis.get(i);
		axisId			=	axis.getAxisId();
		mandatory		=	axis.getMandatory();
		variant			=	axis.getVariant();
		axisName		=	axis._getAxisName(language);
		axisValues		=	axis._getAxisValues();
		baseValueId		=	new Integer(axis.getBaseValue()).toString();
		invariantValue	=	axis._getInvariantValue();
		nbAxis++;

		baseValuePath	=	"";
		for (int a = 0; a<axisValues.size(); a++) {
			value		= (Value) axisValues.get(a);
			valueId		= value.getPK().getId();
			if (valueId.equals(baseValueId)) {
				//baseValuePath = value.getPath();
				baseValuePath = value.getFullPath();
				break;
			}
		}

		// Find the value selected in the searchContext (if any) for the axis
		String sValueSearchContext = getValueSearchContext(searchContext, axisId);

		String  baseValueIds	= new Integer(baseValueId).toString()+",";
		String	baseValuePaths	= baseValuePath+",";
		int memIndice = i;
		//Recherche si l'axe est utilis� plusieurs fois
		for (int j=i+1; j<usedAxis.size(); j++) {
			UsedAxis	checkAxis		=	(UsedAxis) usedAxis.get(j);
			int			checkAxisId		=	checkAxis.getAxisId();

			if (checkAxisId == axisId) {
				//L'axe est utilis� au moins 2 fois
				//on doit m�moriser baseValueId et baseValuePath
				baseValueIds	+= new Integer(checkAxis.getBaseValue()).toString()+",";
				//baseValuePaths	+= checkAxis._getBaseValuePath()+",";
				baseValuePaths	+= checkAxis._getBaseValuePath()+checkAxis.getBaseValue()+"/,";
			} else {
				break;
			}
			memIndice++;
		}
		i = memIndice;

  %>
      <tr>
        <td class="txtlibform" nowrap width="30%"><%=axisName%>&nbsp;:</td>
        <td width="70%">
          <select name="<%=axisId%>" onChange="test(this, '<%=activeThesaurus.booleanValue()%>')">
		  <%
				String selectedAttr = "";
				boolean bSearchContextValueDone = false;
				String valueIdSelected = null;
				String valueTreeIdSelected = null;
				boolean anElementSelected = false; //true si un des termes != "" a ete selectionn� dans la combo
				boolean isBaseValue = false;
				boolean isValueAllowed = false;
				boolean isAscendentValue = false;

				for (int v = 0; v<axisValues.size(); v++)
				{
					value		= (Value) axisValues.get(v);
					valueName	= value.getName(language);
					valueId		= value.getPK().getId();
					valueLevel	= value.getLevelNumber();
					valuePath	= value.getPath();
					valueTreeId = value.getTreeId();

					increment = "";
					for (int j = 0; j < valueLevel; j++)
						increment += "&nbsp;&nbsp;";

					String sSelStatus = "";
					isBaseValue = valueIsBaseValue(valueId, baseValueIds);
					//isValueAllowed = isValueAllowed(valuePath, baseValueIds);
					isValueAllowed = isValueAllowed(valuePath, baseValuePaths);
					isAscendentValue = valueIsAscendant(valueId, baseValuePaths);

					if (invariantValue == null)	{
						//test si le chemin de la valeur courante contient la valeur de base
						if (isValueAllowed || isBaseValue)
						{
							//premier load de la page
							if (listValues.size() == 0) {
								// The SearchContext is used (if any)
								if (sValueSearchContext != null && sValueSearchContext.equals(valuePath + valueId + "/") && !bSearchContextValueDone) {
									sSelStatus = "selected";
									bSearchContextValueDone = true;
									anElementSelected = true;
								}
							}

							//cas du rechargement de la page si l'on a change de selection dans une combo
							else {
								Iterator k = listValues.iterator();
								while (k.hasNext()) {
									String theValueSelected = (String) k.next();

									//on a selectionne ce terme
									if (theValueSelected.equals(axisId+"|"+valuePath+valueId+"/")) {
										sSelStatus = "selected";
										anElementSelected = true;
									}
								}
							}

							//on a une valeur de base
							if (mandatory == 1 && isBaseValue) {
									sSelStatus = "selected";
									anElementSelected = true;
							}

							out.println("<option value=\""+axisId+"|"+valuePath+valueId+"/\" "+sSelStatus+">"+increment+valueName+"</option>");

							//recupere les valeurs selectionnees
							if (sSelStatus.equals("selected")) {
								valueIdSelected = valueId;
								valueTreeIdSelected = valueTreeId;
							}

						} else {
							if (isAscendentValue) {
								out.println("<option class=intfdcolor51 value=\"A\">"+increment+valueName+"</option>");
							}
						}
					}

					//on a une valeur invariante
					else {
						//Seule la valeur invariante peut �tre s�lectionn�e
						if (isValueAllowed || isBaseValue) {
							if (invariantValue.equals(valuePath+valueId+"/")) {
								out.println("<option value=\""+axisId+"|"+valuePath+valueId+"/\" selected>"+increment+valueName+"</option>");
								valueIdSelected = valueId;
								valueTreeIdSelected = valueTreeId;
								anElementSelected = true;
							} else {
								out.println("<option class=intfdcolor51 value=\"A\">"+increment+valueName+"</option>");
							  }


						} else {
							if (isAscendentValue) {
								out.println("<option class=intfdcolor51 value=\"A\">"+increment+valueName+"</option>");
							}
						}
					}
				}  //fin du for sur les valeurs


				//affichage du dernier element de valeur = "" si une valeur n'est pas obligatoire pour cet axe
				//selectionn� si rien d'autre n'a �t� selectionn� dans la combo

				if (mandatory == 0 && !bSearchContextValueDone) {
					if (! anElementSelected) {
						selectedAttr = "selected";
					}

					out.println("<option value=\"-\" "+selectedAttr+"></option>");
				}
		  %>
		  </select>

		  <%

		  if (valueIdSelected != null) {
				//affiche les synonymes
				out.println(displaySynonymsValue(activeThesaurus, jargon, valueTreeIdSelected, valueIdSelected));
		  }

		  %>
		  <input type="hidden" name="AxisName_<%=axisId%>" value="<%=axisName%>"/>
		  <input type="hidden" id="BaseValue_<%=axisId%>" value="<%=baseValuePath%>"/>
		  <% if (mandatory == 1) { %>
	          <img src="<%=resource.getIcon("pdcPeas.mandatoryField")%>" width=5 align="absmiddle">
		  <% } %>
		  <% if (variant == 0) { %>
	          <img src="<%=resource.getIcon("pdcPeas.buletVert")%>" width=10 align="absmiddle" border="0">
		  <% } %>
        </td>
      </tr>
  <% } //fin du for sur les axes%>
  <% if (usedAxis.size() > 0) { %>
      <tr>
        <td nowrap>(
		<img src="<%=resource.getIcon("pdcPeas.mandatoryField")%>" width=5 align="absmiddle">&nbsp;:<%=resource.getString("GML.requiredField")%>,
		<img src="<%=resource.getIcon("pdcPeas.buletVert")%>" width=10 align="absmiddle" border="0">&nbsp;:<%=resource.getString("pdcPeas.notVariants")%>
		)</td>
        <td>&nbsp;</td>
      </tr>
	<% } else { %>
		<tr><td><%=resource.getString("pdcPeas.classifyImpossible")%></td></tr>
	<% } %>
    </table>
    <%
    out.println(board.printAfter());

    ButtonPane buttonPane = gef.getButtonPane();
	// si aucune position n'est d�finie alors on ne permet pas la validation
	if (axisValues != null && axisValues.size() != 0) {
		buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:sendData()", false));
	}
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false));
    out.println("<br/><center>"+buttonPane.print()+"<br/></center>");
    %>
  </CENTER>
  <%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</FORM>
<FORM name="submitValues" method="post">
	<input type="hidden" name="Values">
</FORM>
<form name="refresh" action="<%=pdcClassifyContext%>Main" method="post">
</form>
</BODY>
</HTML>
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
	List				usedAxis = (List) request.getAttribute("UsedAxis");
	ClassifyPosition	position = (ClassifyPosition) request.getAttribute("Position"); //null si rechargement de la page
	ClassifyPosition	lastPosition = (ClassifyPosition) request.getAttribute("LastPosition"); //utilise en cas de rechargement de la page
	Jargon 				jargon = (Jargon) request.getAttribute("Jargon");
	Boolean 			activeThesaurus = (Boolean) request.getAttribute("ActiveThesaurus");
	Collection 			listValues = (Collection) request.getAttribute("ListValues");

	UsedAxis		axis			= null;
	List			axisValues		= null;
	int				axisId			= -1;
	String			axisName		= null;
	int				mandatory		= 0;
	int				variant			= 0;
	String			baseValueId		= null;
	String			baseValuePath	= null;
	String 			valueTreeId     = null;

	Value			value			= null;
	String			valueName		= null;
	String			valueId			= null;
	String			valuePath		= null;
	int				valueLevel		= -1;
	String			increment		= null;

	int				positionId		= -2;
	List			positionValues	= null;
	ClassifyValue	positionValue	= null;
	String			positionValueId	= null;
	int				positionAxisId	= -1;

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
	//alert("listValues = "+z);
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
		document.submitValues.action = "<%=pdcClassifyContext%>UpdatePosition";
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
		document.submitValues.action = "<%=pdcClassifyContext%>ReloadUpdatePosition";
		document.submitValues.submit();
	}
}
</script>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
  <%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setExtraInformation(resource.getString("pdcPeas.classifyUpdate"));

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
  %>
  <CENTER>
  <FORM name="chooseValues" method="post">
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
  <% for (int i = 0; i < usedAxis.size(); i++) {
		axis			=	(UsedAxis) usedAxis.get(i);
		axisId			=	axis.getAxisId();
		mandatory		=	axis.getMandatory();
		axisName		=	axis._getAxisName(language);
		axisValues		=	axis._getAxisValues();
		nbAxis++;
		variant			=	axis.getVariant();

		baseValueId		=	new Integer(axis.getBaseValue()).toString();
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

		if (position != null) { //premier affichage de la page
			positionValues = position.getValues();
			positionId = position.getPositionId();

			//Recherche si une valeur de cet axe fait partie de la position a modifier
			positionValueId = null;
			for (int pv = 0; pv < positionValues.size(); pv++) {
				positionValue	= (ClassifyValue) positionValues.get(pv);
				positionAxisId	= positionValue.getAxisId();
				if (positionAxisId == axisId) {
					//L'axe est bien utilis� dans la position
					positionValueId = positionValue.getValue();
				}
			}
		}

		else { //reload de la page
			positionValues = lastPosition.getValues();
			positionId = lastPosition.getPositionId();
		}

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
				baseValuePaths	+= checkAxis._getBaseValuePath()+checkAxis.getBaseValue()+"/,";
			} else {
				break;
			}
			memIndice++;
		}
		i = memIndice;

		String valueIdSelected = null;
		String valueTreeIdSelected = null;
		boolean anElementSelected = false; //true si un des termes != "" a ete selectionn� dans la combo

  %>
      <tr>
        <td class="txtlibform" nowrap width="30%"><%=axisName%>&nbsp;:</td>
        <td width="70%">
          <select name="<%=axisId%>" onChange="test(this, '<%=activeThesaurus.booleanValue()%>')">
		  <%
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


					//test si le chemin de la valeur courante contient la valeur de base
					//if (isValueAllowed(valuePath, baseValueIds) || valueIsBaseValue(valueId, baseValueIds)) {
					if (isValueAllowed(valuePath, baseValuePaths) || valueIsBaseValue(valueId, baseValueIds)) {

						//premier affichage de la page : on doit s�lectionner la valeur de la position
						if (positionValueId != null && positionValueId.equals(valuePath+valueId+"/")) {
							sSelStatus = "selected";
						}

						else { //cas du rechargement de la page si l'on a change de selection dans une combo
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

						out.println("<option value=\""+axisId+"|"+valuePath+valueId+"/\" "+sSelStatus+">"+increment+valueName+"</option>");

						//recupere les valeurs selectionnees
						if (sSelStatus.equals("selected")) {
							anElementSelected = true;
							valueIdSelected = valueId;
							valueTreeIdSelected = valueTreeId;
						}

					} else {
						if (valueIsAscendant(valueId, baseValuePaths)) {
							out.println("<option class=intfdcolor51 value=\"A\">"+increment+valueName+"</option>");
						}
					}
				} //fin du for sur les valeurs


				//affichage du dernier element de valeur = "" si une valeur n'est pas obligatoire pour cet axe

				String selectedAttr = "";
				if (mandatory == 0) {
					//premiere fois qu'on affiche cette page ou
					//reload de la page, on a change la selection d'un element dans une combo
					//et auncun autre element n'a ete selectionne
					if (! anElementSelected) {
						selectedAttr = "selected";
					}

					out.println("<option value=\""+axisId+"|-\""+selectedAttr+"></option>");
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
  <% } //fin du for sur les axes
  %>
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
    </FORM>
    <%
    out.println(board.printAfter());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:sendData()", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false));
	out.println("<br/><center>"+buttonPane.print()+"<br/></center>");
    %>
  </CENTER>
  <%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<FORM name="submitValues" method="post">
	<input type="hidden" name="Values">
	<input type="hidden" name="Id" value="<%=positionId%>">
</FORM>
<form name="refresh" action="<%=pdcClassifyContext%>Main" method="post">
</form>
</BODY>
</HTML>
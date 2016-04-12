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
<%@ page import="org.silverpeas.core.util.EncodeHelper" %>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@ page import="org.silverpeas.core.pdc.pdc.model.AxisHeader" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkPdc.jsp"%>
<%
	String type = (String) request.getAttribute("ViewType");
	List primaryAxis = (List) request.getAttribute("PrimaryAxis");
	List secondaryAxis = (List) request.getAttribute("SecondaryAxis");

	AxisHeader axisHeader = (AxisHeader) request.getAttribute("AxisHeader");
	String max = (String) request.getAttribute("MaxAxis");
	String alreadyExist = (String) request.getAttribute("AlreadyExist");
	String modification = (String) request.getAttribute("Modif"); // on modifie l'axe

	String translation = (String) request.getAttribute("Translation");
	if (translation == null || translation.equals("null"))
	{
		translation = I18NHelper.defaultLanguage;
	}

	String id = "";
	String name = "";
	String description = "";
	String formAction = "CreateAxis";
	String order = "";
	String primaryChecked = "checked";
	String secondaryChecked = "";
	String errorMessage = "";
	List selectedAxis = primaryAxis; // pour affichage des options du tag select
	if (type.equals("S")) {
		primaryChecked = "";
		secondaryChecked = "checked";
		selectedAxis = secondaryAxis; // pour affichage des options du tag select
	}
	if (max != null && max.equals("1")) {
		errorMessage = "<font size=2 color=#FF6600><b>"+resource.getString("pdcPeas.maximumAxisReach")+"</b></font>";
	} else {
		if (axisHeader != null) {
			id = axisHeader.getPK().getId();
			if (id.equals("unknown")) {
				//Creation case
				formAction = "CreateAxis";
			} else {
				//Update case
				formAction = "UpdateAxis";
			}
			name = axisHeader.getName(translation);
			description = axisHeader.getDescription(translation);
			order = new Integer(axisHeader.getAxisOrder()).toString();
			if (alreadyExist != null && alreadyExist.equals("1")) {
				errorMessage = "<font size=2 color=#FF6600><b>"+resource.getString("pdcPeas.axisAlreadyExist")+"</b></font>";
			}
		}
	}

	// pour affichage des options du tag select
	Iterator it = null;
	int nbItemShowed = 1; // cas ou ce serait le 1er axe de cr�er
	if (!selectedAxis.isEmpty()){
		it = selectedAxis.iterator();
		if (selectedAxis.size() < 5) {
			nbItemShowed = selectedAxis.size()+2; // par defaut, si la liste n'est pas vide alors nous devons tenir compte des 2 items d'insertions
			if (modification != null) // cas ou nous ne sommes en modification
				nbItemShowed = nbItemShowed - 2;
		} else {
			nbItemShowed = 5;
		}
	}
	AxisHeader tempAxisHeader = null;
	String axisName = "";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>

<script language="Javascript">

	function isCorrectForm(){
		var name = stripInitialWhitespace(document.axisForm.Name.value);
		if (isWhitespace(name)) {
			alert("<%=resource.getString("pdcPeas.emptyName")%>");
			document.axisForm.Name.focus();
			return false;
		} else {
			if (document.axisForm.Description.value.length > 1000) {
               alert("<%=resource.getString("pdcPeas.lenDescription")%>");
               document.axisForm.Description.focus();
               return false;
            } else {
               return true;
            }
		}
	}

	function sendData() {
		if (isCorrectForm()) {
			document.axisForm.submit();
		}
	}

	function changeList(axisType){

		<%!
			AxisHeader h = null; // un axis header pour construire la nouvelle liste
			String nom = ""; // le nom de l'axis header
			String desc = "";
			String ordre = ""; // son ordre
			int o; // son ordre
			int item; // place de l'item dans l'objet html SELECT
		%>
		// effacer toutes les options actuelles
		var longueur_list = document.axisForm.Order.length;
		for (i=0;i<longueur_list ;i++ ){
			document.axisForm.Order.options[0] = null;
		}

		if (axisType == 'P'){
			// d�finition des noms d'axe et de leur ordre de l'axe primaire
			<%
			if (!primaryAxis.isEmpty()){
				// la liste n'est pas vide, on construit les instructions javascript pour reconstuire dynamique la liste
				Iterator IteratorP = primaryAxis.iterator();
				item = 0;
				while (IteratorP.hasNext()){
					h = (AxisHeader)IteratorP.next();
					nom = EncodeHelper.javaStringToHtmlString(h.getName());
					desc = EncodeHelper.javaStringToHtmlString(h.getDescription());
					o = h.getAxisOrder();
					ordre = new Integer(o).toString();
					if (!(h.getPK().getId()).equals(id)){
						out.println("document.axisForm.Order.options["+item+"] = new Option(\""+nom+"\",\""+nom+sepOptionValueTag+ordre+"\");");
						item++;
					}
				}
				o++;
				// ajoute le dernier element
				if (modification == null) // cr�ation
					out.println("document.axisForm.Order.options["+item+"] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\""+o+"\",true,\"selected\");");
				else
					out.println("document.axisForm.Order.options["+item+"] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\""+o+"\");");
			} else {
				out.println("document.axisForm.Order.options[0] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\"0\",true,\"selected\");");
			}
			out.println("document.axisForm.Order.size = 5"); // d�finition en dur de la taille de la liste :-(
			%>
		} else {
			// d�finition des noms d'axe et de leur ordre
			<%
			if (!secondaryAxis.isEmpty()){
				Iterator IteratorS = secondaryAxis.iterator();
				item = 0;
				while (IteratorS.hasNext()){
					h = (AxisHeader)IteratorS.next();
					nom = EncodeHelper.javaStringToHtmlString(h.getName());
					desc = EncodeHelper.javaStringToHtmlString(h.getDescription());
					o = h.getAxisOrder();
					ordre = new Integer(o).toString();
					if (!(h.getPK().getId()).equals(id)){
						out.println("document.axisForm.Order.options["+item+"] = new Option(\""+nom+"\",\""+nom+sepOptionValueTag+ordre+"\");");
						item++;
					}
				}
				o++;
				// ajoute le dernier element
				if (modification == null) // cr�ation
					out.println("document.axisForm.Order.options["+item+"] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\""+o+"\",true,\"selected\");");
				else
					out.println("document.axisForm.Order.options["+item+"] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\""+o+"\");");
			} else {
				out.println("document.axisForm.Order.options[0] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\"0\",true,\"selected\");");
			}
			out.println("document.axisForm.Order.size = 5"); // d�finition en dur de la taille de la liste :-(
			%>
		}
	}

	// gestion des traductions

	<%
	if (axisHeader != null)
	{
		String lang = "";
		Iterator codes = axisHeader.getTranslations().keySet().iterator();

		while (codes.hasNext())
		{
			lang = (String) codes.next();
			out.println("var name_"+lang+" = \""+EncodeHelper.javaStringToJsString(axisHeader.getName(lang))+"\";\n");
			out.println("var desc_"+lang+" = \""+EncodeHelper.javaStringToJsString(axisHeader.getDescription(lang))+"\";\n");
		}
	}
	%>

	function showTranslation(lang)
	{
		showFieldTranslation('AxisName', 'name_'+lang);
		showFieldTranslation('AxisDescription', 'desc_'+lang);
	}

	function removeTranslation()
	{
		document.axisForm.submit();
	}
</script>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF" onload="storeItems(document.axisForm.Order);document.axisForm.Name.focus()">
<%
	browseBar.setDomainName(resource.getString("pdcPeas.pdc"));
    browseBar.setComponentName(resource.getString("pdcPeas.pdcDefinition"));

    TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("pdcPeas.Axe"), "", true);
	// affichage dans la browsebar du bon message : creation ou modification ainsi que de l'onglet Gestionnaire
	if (formAction.equals("CreateAxis"))
	{
		browseBar.setPath(resource.getString("pdcPeas.createAxis"));
		tabbedPane.addTab(resource.getString("pdcPeas.managers"), "ViewManager", false, false);
	}
	else {
		browseBar.setPath(resource.getString("pdcPeas.editAxis"));
		tabbedPane.addTab(resource.getString("pdcPeas.managers"), "ViewManager", false);
	}

	out.println(window.printBefore());
    out.println(tabbedPane.print());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <form action="<%=formAction%>" name="axisForm" method="post">

	<%=I18NHelper.getFormLine(resource, axisHeader, translation)%>

		<input type="hidden" name="Id" value="<%=id%>">
	  <% if (errorMessage != null && errorMessage.length() > 0) { %>
		<tr>
			<td colspan=2 nowrap align=center><%=errorMessage%></td>
		</tr>
	  <% } %>
      <tr>
        <td width="30%" class="txtlibform" nowrap><%=resource.getString("GML.nom")%>&nbsp;:</td>
        <td nowrap><input type="text" style="text-align:left;" name="Name" id="AxisName" maxlength="25" size="30" value="<%=EncodeHelper.javaStringToHtmlString(name)%>" onKeyUP="javascript:highlightItem(document.axisForm.Order,this.value)">&nbsp;<img src="<%=resource.getIcon("pdcPeas.mandatoryField")%>" width="5" align="absmiddle"></td>
      </tr>
      <tr>
	  <td width="30%" class="txtlibform" valign="top" nowrap><%=resource.getString("pdcPeas.definition")%>&nbsp;:</td>
        <td><TEXTAREA style="width:100%" name="Description" id="AxisDescription" rows="3"><%=EncodeHelper.javaStringToHtmlString(description)%></TEXTAREA></td>
     </tr>
      <tr>
        <td class="txtlibform" nowrap><%=resource.getString("GML.type")%>&nbsp;:</td>
        <td nowrap>
          <input type="radio" name="Type" value="P" <%=primaryChecked%> onClick="javascript:changeList('P')"><span class="textePetitBold">&nbsp;<%=resource.getString("pdcPeas.primary")%></span><br>
          <input type="radio" name="Type" value="S" <%=secondaryChecked%> onClick="javascript:changeList('S')"><span class="textePetitBold">&nbsp;<%=resource.getString("pdcPeas.secondary")%></span></td>
      </tr>
      <tr>
        <td class="txtlibform" valign="top"><%=resource.getString("pdcPeas.position")%>&nbsp;:</td>
        <td class="textePetitBold"><%=resource.getString("pdcPeas.brothersAxis")%>&nbsp;:<br>
			<%
				out.println("<select name=\"Order\" size=\""+new Integer(nbItemShowed).toString()+"\">");
				// test s'il ne s'agit pas du premier axe que l'on cr�ait.
				if (!selectedAxis.isEmpty()){
					// affiche les axes fr�res
					while (it.hasNext()){
						tempAxisHeader = (AxisHeader)it.next();
						axisName = EncodeHelper.javaStringToHtmlString(tempAxisHeader.getName());
						order = (new Integer( tempAxisHeader.getAxisOrder() )).toString();
						if (!(tempAxisHeader.getPK().getId()).equals(id)) {
							out.println("<option value=\""+axisName+sepOptionValueTag+order+"\">"+axisName+"</option>");
						}
					}
					// calcul le dernier ordre
					int newOrder_tmp = (new Integer(order)).intValue() + 1;
					String newOrder = (new Integer(newOrder_tmp)).toString();
					if (modification == null) // cr�ation
						out.println("<option value=\""+newOrder+"\" selected>&lt;"+resource.getString("pdcPeas.EndTag")+"&gt;</option>");
					else
						out.println("<option value=\""+newOrder+"\">&lt;"+resource.getString("pdcPeas.EndTag")+"&gt;</option>");
				} else{
					out.println("<option value=\"0\" selected>&lt;"+resource.getString("pdcPeas.EndTag")+"&gt;</option>");
				}
				out.println("</select>");
			%>
		</td>
      </tr>
      <tr>
        <td nowrap>( <img src="<%=resource.getIcon("pdcPeas.mandatoryField")%>" width="5" align="absmiddle">&nbsp;: <%=resource.getString("GML.requiredField")%> )</td>
        <td nowrap>&nbsp;</td>
      </tr>
	</form>
  </table>
  <%
	out.println(board.printAfter());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:sendData()", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false));
    out.println("<center><BR/>"+buttonPane.print()+"</center>");
  %>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>
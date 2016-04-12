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

<%
// recuperation des parametres
Value 	value 			= (Value) request.getAttribute("Value"); // l'objet Value pour afficher ses informations
String 	isRoot 			= (String) request.getAttribute("Root"); // si c'est une valeur racine ou pas.
List 	list 			= (List) request.getAttribute("Path"); // le chemin complet ou l'on peut retrouver la valeur selectionnee
String 	daughterName 	= (String) request.getAttribute("DaughterNameWhichAlreadyExist"); //Dans le cas de la suppression, une des valeurs fille peut avoir
																					  //le m�me nom qu'une des soeurs de la valeur que l'on veut supprimer
																					  //! C'est interdit !
String 	displayLanguage = (String) request.getAttribute("DisplayLanguage");

boolean	isAdmin			= ((Boolean) request.getAttribute("IsAdmin")).booleanValue();

// initialisation des diff�rentes variables pour l'affichage
String valueName 		= value.getName(displayLanguage);
String valueDescription = value.getDescription(displayLanguage);
int valueNbDoc 			= value.getNbObjects();

String completPath = buildCompletPath((ArrayList)list, false, 1, displayLanguage);

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript">
	var currentLanguage = "<%=value.getLanguage()%>";

	// this method opens a pop-up which warns the user
	function areYouSure(choice){
		if (choice == 'all')
		    ConfirmDelete = confirm('<%=resource.getString("pdcPeas.confirmDeleteArbo")%>');
		else
			ConfirmDelete = confirm('<%=resource.getString("pdcPeas.confirmDeleteValue")%>');
		return ConfirmDelete
	}

	// efface une valeur ou/et son arborescence
	function removeSelection(choice){
		// demande confirmation
		if (areYouSure(choice)){
			if (choice == 'all')
				document.deleteForm.action = "DeleteArbo";
			document.deleteForm.submit();
		}
	}

	function editValue()
	{
		location.href = "EditValue?Translation="+currentLanguage;
	}

	function moveValue()
	{
		location.href = "ToMoveValueChooseMother?Translation="+currentLanguage;
	}

	<%
	if (value != null)
	{
		String lang = "";
		Iterator codes = value.getTranslations().keySet().iterator();

		while (codes.hasNext())
		{
			lang = (String) codes.next();
			out.println("var name_"+lang+" = \""+EncodeHelper.javaStringToJsString(value.getName(lang))+"\";\n");
			out.println("var desc_"+lang+" = \""+EncodeHelper.javaStringToHtmlParagraphe(value.getDescription(lang))+"\";\n");
		}
	}
	%>

	function showTranslation(lang)
	{
		<%=I18NHelper.updateHTMLLinks(value)%>
		currentLanguage = lang;

		document.getElementById('ValueName').innerHTML = eval('name_'+lang);
		document.getElementById('ValueDescription').innerHTML = eval('desc_'+lang);
	}
</script>
</head>
<body>
<%
	browseBar.setDomainName(resource.getString("pdcPeas.pdc"));
    browseBar.setComponentName(resource.getString("pdcPeas.pdcDefinition"));
	browseBar.setPath(resource.getString("pdcPeas.editValue"));
	browseBar.setI18N(value, displayLanguage);

	if (isRoot.equals("0")) {
		operationPane.addOperation(resource.getIcon("pdcPeas.icoUpdateValue"),resource.getString("pdcPeas.updateValue"), "javascript:editValue()");
		if (isAdmin)
		{
			operationPane.addOperation(resource.getIcon("pdcPeas.icoMoveValue"),resource.getString("pdcPeas.moveValue"), "javascript:moveValue()");
			operationPane.addOperation(resource.getIcon("pdcPeas.icoDeleteValue"),resource.getString("pdcPeas.deleteValue"), "javascript:removeSelection('one')");
			operationPane.addOperation(resource.getIcon("pdcPeas.icoDeleteArbo"),resource.getString("pdcPeas.deleteArbo"), "javascript:removeSelection('all')");
		}
	}
	if (isAdmin)
	{
	operationPane.addOperation(resource.getIcon("pdcPeas.icoAddMotherValue"),resource.getString("pdcPeas.insertMotherValue"), "NewMotherValue");
	}
	operationPane.addOperationOfCreation(resource.getIcon("pdcPeas.icoAddDaughterValue"),resource.getString("pdcPeas.createDaughterValue"), "NewDaughterValue");

	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab("Valeur", "ViewValue", true);
	tabbedPane.addTab("Gestionnaires", "ViewManager", false);

    out.println(window.printBefore());
%>
<view:areaOfOperationOfCreation/>
<%
    out.println(tabbedPane.print());
%>
<view:frame>
<view:board>
<center>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
  <% if (daughterName != null) { %>
	<tr>
      <td colspan="2" align="center"><font size="2" color="#FF6600"><b><%=resource.getString("pdcPeas.deleteValueImpossibleBegin")%> <i><%=daughterName%></i> <%=resource.getString("pdcPeas.deleteValueImpossibleEnd")%></b></font></td>
    </tr>
  <% } %>
  <% if (completPath != null){ %>
    <tr>
      <td class="txtlibform"><%=resource.getString("pdcPeas.path")%>&nbsp;:</td>
      <td><%=completPath%></td>
    </tr>
  <% }%>
    <tr>
      <td class="txtlibform"><%=resource.getString("pdcPeas.value")%>&nbsp;:</td>
      <td class="textePetitBold" id="ValueName"><%=EncodeHelper.javaStringToHtmlString(valueName)%></td>
    </tr>
	<tr>
		<td valign="top" class="txtlibform"><%=resource.getString("pdcPeas.definition")%>&nbsp;:</td>
		<td id="ValueDescription"><%=EncodeHelper.javaStringToHtmlParagraphe(valueDescription)%></td>
	</tr>
	<tr>
	<td class="txtlibform" nowrap="nowrap"><%=resource.getString("pdcPeas.docsNumber")%>&nbsp;:</td>
	<td width="100%"><%=valueNbDoc%></td>
    </tr>
  </table>
</view:board>
<%
    ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.close"), "javascript:window.close()", false));
    out.println("<br/><center>"+buttonPane.print()+"</center>");
%>
</center>
</view:frame>
<%
out.println(window.printAfter());
%>
<form name="deleteForm" action="DeleteValue" method="post"></form>
</body>
</html>
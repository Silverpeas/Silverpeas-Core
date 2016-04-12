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
List 	sistersValue 	= (List) request.getAttribute("Sisters"); // a list of values
Value 	motherValue 	= (Value) request.getAttribute("Value");
Value 	valueToCreate 	= (Value) request.getAttribute("ValueToCreate"); // the value already exist
String 	valueCreated 	= (String) request.getAttribute("ValueCreated"); // une valeur fille vient d'etre ajouter
String 	translation 	= (String) request.getAttribute("Translation");

// initialisation
String name = ""; // valeur par defaut du champ nom
String order = ""; // valeur par defaut du champ ordre
int nbItemShowed = 1; // nombre d'items montr�s dans l'objet html SELECT
nbItemShowed = sistersValue.size();
boolean isValueCreated = false;
if (valueCreated != null){
	// une valeur fille a �t� pr�c�demment cr��e
	isValueCreated = true;
}

Iterator it = sistersValue.iterator();
String motherValueName = motherValue.getName(translation);
String motherValueDescription = "";
String errorMessage = null;
Value tempValue = null; // pour affichage des options du tag select
String sisterValueName = null; // pour affichage des options du tag select

if (valueToCreate != null){
	// Le nom de la valeur entr�e par l'utilisateur existe deja
	errorMessage = "<font size=2 color=#FF6600><b>"+resource.getString("pdcPeas.valueAlreadyExist")+"</b></font>";
	name = valueToCreate.getName();
}

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script language="javascript">
	function validDescr(){
		if (document.editValue.Description.value.length > 1000) {
			alert("<%=resource.getString("pdcPeas.lenDescription")%>");
			document.editValue.Description.focus();
			return false;
		}
		return true;
	}

	// envoi les donn�es entr�es par l'utilisateur
	function sendData(){
		if (isEmptyField(document.editValue.Name.value)){
			alert("<%=resource.getString("pdcPeas.emptyDaughterName")%>");
		} else if (validDescr()) {
			document.editValue.submit();
		}
	}

	// rafraichi la fenetre principale
	function refresh(){
		document.refresh.submit();
	}
</script>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF" onload="storeItems(document.editValue.Order);document.editValue.Name.focus()">
<FORM name="editValue" action="CreateDaughterValue" method="post">
  <%
	browseBar.setDomainName(resource.getString("pdcPeas.pdc"));
    browseBar.setComponentName(resource.getString("pdcPeas.pdcDefinition"));
	browseBar.setPath(resource.getString("pdcPeas.createDaughterValue"));

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
  <CENTER>
    <table width="100%" border="0" cellspacing="0" cellpadding="4">

    <%=I18NHelper.getFormLine(resource, valueToCreate, translation)%>

	  <% if (errorMessage != null && errorMessage.length() > 0) { %>
		<tr>
			<td colspan=2 nowrap align=center><span class=txtlibform><%=errorMessage%></td>
		</tr>
	  <% } %>
	  <tr>
        <td class="txtlibform" width="30%" valign="top"><%=resource.getString("pdcPeas.motherValue")%>&nbsp;:</td>
        <td class="textePetitBold"><%=motherValueName%></td>
      </tr>
      <tr>
        <td class="txtlibform" valign="top"><%=resource.getString("pdcPeas.daughterValue")%>&nbsp;:</td>
        <td>
          <input type="text" style="text-align:left;" name="Name" maxlength="75" size="75" value="<%=EncodeHelper.javaStringToHtmlString(name)%>"  onKeyUP="javascript:highlightItem(document.editValue.Order,this.value)">&nbsp;<img src="<%=resource.getIcon("pdcPeas.mandatoryField")%>" width=5 align="absmiddle">
        </td>
      </tr>
	 <tr>
		<td class="txtlibform" valign="top" width="30%"><%=resource.getString("pdcPeas.definition")%>&nbsp;:</td>
		<td><TEXTAREA name="Description" rows="4" cols="75"><%=EncodeHelper.javaStringToHtmlString(motherValueDescription)%></TEXTAREA></td>
	</tr>
	 <tr>
	    <td class="txtlibform" width="30%" valign="top"><%=resource.getString("pdcPeas.sistersValue")%>&nbsp;:</td>
		<td>
			<select name="Order" size="5">
			<%
				if (!sistersValue.isEmpty()){
					// affiche les soeurs de la valeur courante
					while (it.hasNext()){
						tempValue = (Value)it.next();
						sisterValueName = EncodeHelper.javaStringToHtmlString(tempValue.getName());
						order = (new Integer( tempValue.getOrderNumber() )).toString();
						out.println("<option value=\""+sisterValueName+sepOptionValueTag+order+"\">"+sisterValueName+"</option>");
					}
					// calcul le dernier ordre
					int newOrder_tmp = (new Integer(order)).intValue() + 1;
					String newOrder = (new Integer(newOrder_tmp)).toString();
					out.println("<option value=\""+sepOptionValueTag+newOrder+"\" selected>&lt;"+resource.getString("pdcPeas.EndTag")+"&gt;</option>");
				} else {
					out.println("<option value=\"0\" selected>&lt;"+resource.getString("pdcPeas.EndTag")+"&gt;</option>");
				}
			%>
			</select>
		</td>
      </tr>
      <tr>
        <td valign="top">( <img src="<%=resource.getIcon("pdcPeas.mandatoryField")%>" width="5" align="absmiddle">&nbsp;:
          <%=resource.getString("GML.requiredField")%> )</td>
        <td>&nbsp;</td>
      </tr>
    </table>
    <%
    out.println(board.printAfter());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:sendData()", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.close"), "javascript:refresh()", false));
    out.println("<BR/><center>"+buttonPane.print()+"</center><BR/>");

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</FORM>
<form name="refresh" action="CloseCreationDaughterValue" method="post">
</form>
<form name="goBack" action="ViewValue" method="post">
	<input type="hidden" name="Id" value="<%=motherValue.getPK().getId()%>">
</form>
</BODY>
</HTML>
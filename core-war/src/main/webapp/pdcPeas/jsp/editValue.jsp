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
Value	value			= (Value) request.getAttribute("Value");			// l'objet Value pour afficher ses informations
List	sisters			= (List) request.getAttribute("Sisters");			// les valeurs soeurs
List	list			= (List) request.getAttribute("Path");				// le chemin complet ou l'on peut retrouver la valeur selectionnee
String	alreadyExist	= (String) request.getAttribute("AlreadyExist");	// La valeur existe deja.
boolean	isAdmin			= ((Boolean) request.getAttribute("IsAdmin")).booleanValue();

String translation = (String) request.getAttribute("Translation");
if (translation == null || translation.equals("null"))
{
	translation = I18NHelper.defaultLanguage;
}

// initialisation des diff�rentes variables pour l'affichage
String valueName		= value.getName(translation);
String valueDescription = value.getDescription(translation);
String valueId			= value.getPK().getId();
int valueNbDoc 			= value.getNbObjects();

String errorMessage = null;

Value		tempValue		= null; // pour affichage des options du tag select
String		sisterValueName = null; // pour affichage des options du tag select
Iterator	itSisters		= sisters.iterator(); // pour affichage des options du tag select
String		order			= ""; // pour affichage des options du tag select

// Pour l'affichage du chemin complet
String completPath = buildCompletPath(list, false, 1, translation);

if ( (alreadyExist != null) && (alreadyExist.equals("1")) ){
	// Le nom de la valeur entr�e par l'utilisateur existe deja
	errorMessage = "<font size=2 color=#FF6600><b>"+resource.getString("pdcPeas.valueAlreadyExist")+"</b></font>";
}

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>

<script language="Javascript">

function validDescr(){
	if (document.editValue.Description.value.length > 1000) {
		alert("<%=resource.getString("pdcPeas.lenDescription")%>");
		document.editValue.Description.focus();
		return false;
	}
	return true;
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var name = stripInitialWhitespace(document.editValue.Name.value);
     if (isWhitespace(name)) {
       errorMsg+="  - <%=resource.getString("pdcPeas.TheField")%> '" + value + "' <%=resource.getString("pdcPeas.MustContainsText")%>\n";
       errorNb++;
     }
     switch(errorNb)
     {
        case 0 :
            result = validDescr();
            break;
        case 1 :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

function sendData() {
	if (isCorrectForm()) {
		document.editValue.submit();
    }
}

//gestion des traductions

<%
if (value != null)
{
	String lang = "";
	Iterator codes = value.getTranslations().keySet().iterator();

	while (codes.hasNext())
	{
		lang = (String) codes.next();
		out.println("var name_"+lang+" = \""+EncodeHelper.javaStringToJsString(value.getName(lang))+"\";\n");
		out.println("var desc_"+lang+" = \""+EncodeHelper.javaStringToJsString(value.getDescription(lang))+"\";\n");
	}
}
%>

function showTranslation(lang)
{
	showFieldTranslation('ValueName', 'name_'+lang);
	showFieldTranslation('ValueDescription', 'desc_'+lang);
}

function removeTranslation()
{
	document.editValue.submit();
}

</script>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF" onload="document.editValue.Name.focus()">
<FORM name="editValue" action="UpdateValue" method="post">
<%
	browseBar.setDomainName(resource.getString("pdcPeas.pdc"));
    browseBar.setComponentName(resource.getString("pdcPeas.pdcDefinition"));
	browseBar.setPath(resource.getString("pdcPeas.updateValue"));

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
    <table width="100%" border="0" cellspacing="0" cellpadding="4">
	  <% if (errorMessage != null && errorMessage.length() > 0) { %>
		<tr>
			<td colspan="2" nowrap align="center"><%=errorMessage%></td>
		</tr>
	  <% } %>
      <tr>
        <td class="txtlibform"><%=resource.getString("pdcPeas.path")%>&nbsp;:&nbsp;</td>
        <td><%=completPath%></td>
      </tr>
      <%=I18NHelper.getFormLine(resource, value, translation)%>
      <tr>
        <td width="30%" class="txtlibform"><%=resource.getString("pdcPeas.value")%>&nbsp;:&nbsp;</td>
        <td><input type="text" style="text-align:left;" name="Name" id="ValueName" maxlength="75" size="75" value="<%=EncodeHelper.javaStringToHtmlString(valueName)%>" onKeyUP="javascript:highlightItem(document.editValue.Order,this.value)">&nbsp;<img src="<%=resource.getIcon("pdcPeas.mandatoryField")%>" width=5 align="absmiddle"/></td>
      </tr>
	  <tr>
		<td valign=top width="30%" class="txtlibform"><%=resource.getString("pdcPeas.definition")%>&nbsp;:&nbsp;</td>
		<td><TEXTAREA name="Description" id="ValueDescription" rows="4" cols="75"><%=EncodeHelper.javaStringToHtmlString(valueDescription)%></TEXTAREA></td>
	  </tr>
	  <tr>
	<td width="30%" class="txtlibform"><%=resource.getString("pdcPeas.docsNumber")%>&nbsp;:&nbsp;</td>
	<td><%=valueNbDoc%></td>
     </tr>
	<% if (isAdmin) { %>
	 <tr>
	 <td width="30%" valign="top" class="txtlibform"><%=resource.getString("pdcPeas.sistersValue")%>&nbsp;:&nbsp;</td>
		<td>
			<select name="Order" size="5">
			<%

				if (!sisters.isEmpty()){
					// affiche les soeurs de la valeur courante
					while (itSisters.hasNext()){
						tempValue = (Value)itSisters.next();
						sisterValueName = EncodeHelper.javaStringToHtmlString(tempValue.getName(translation));
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
	<% } else { %>
		<input type="hidden" name="Order" value="<%=value.getOrderNumber()%>"/>
	<% } %>
		<input type="hidden" name="Id" value="<%=valueId%>"/>
    </table>
    <%
    out.println(board.printAfter());
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript: sendData()", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:history.go(-1)", false));
	out.println("<br/><center>"+buttonPane.print()+"</center>");

out.println(frame.printAfter());
out.println(window.printAfter());
%>
</FORM>
</BODY>
</HTML>
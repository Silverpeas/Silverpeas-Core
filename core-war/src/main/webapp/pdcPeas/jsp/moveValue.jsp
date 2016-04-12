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

<%@ include file="checkPdc.jsp"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
// recuperation des parametres
Value	valueToMove		= (Value) request.getAttribute("Value");			// l'objet Value pour afficher ses informations
Axis	axis 			= (Axis) request.getAttribute("Axis");			// l'arbre
List	list			= (List) request.getAttribute("Path");				// le chemin complet ou l'on peut retrouver la valeur selectionnee
String	alreadyExist	= (String) request.getAttribute("AlreadyExist");	// La valeur existe deja.
String 	translation		= (String) request.getAttribute("Translation");
ArrayList userRights 	= (ArrayList) request.getAttribute("UserRights");
boolean kmAdmin 		= ((Boolean)request.getAttribute("KMAdmin")).booleanValue() ;
List	sisters			= (List) request.getAttribute("Sisters");
String	newMotherId		= (String) request.getAttribute("newFatherId");

if (translation == null || translation.equals("null"))
{
	translation = I18NHelper.defaultLanguage;
}
// initialisation des diff�rentes variables pour l'affichage
String valueName		= valueToMove.getName(translation);
String valueId			= valueToMove.getPK().getId();
int valueNbDoc 			= valueToMove.getNbObjects();
String errorMessage = null;

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
function sendData(action) {
	document.moveValue.action = action;
	if (action == 'MoveValue') {
		if (window.confirm("<%=resource.getString("pdcPeas.confirmMoveValue")%>")) {
			$.progressMessage();
			document.moveValue.submit();
		}
	} else {
		document.moveValue.submit();
	}
}
</script>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">

<%
	browseBar.setDomainName(resource.getString("pdcPeas.pdc"));
    browseBar.setComponentName(resource.getString("pdcPeas.pdcDefinition"));
	browseBar.setPath(resource.getString("pdcPeas.moveValue"));

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<FORM name="moveValue" id="moveValue" action="ToMoveValueGetSisters" method="post">
	  <input type="hidden" name="Id" value="<%=valueId%>"/>
    <table width="100%" border="0" cellspacing="0" cellpadding="4">
	  <% if (errorMessage != null && errorMessage.length() > 0) { %>
		<tr>
			<td colspan="2" nowrap="nowrap" align="center"><%=errorMessage%></td>
		</tr>
	  <% } %>
      <tr>
        <td class="txtlibform"><%=resource.getString("pdcPeas.path")%> :</td>
        <td><%=completPath%></td>
      </tr>
      <tr>
        <td class="txtlibform" width="30%"><%=resource.getString("pdcPeas.value")%> :</td>
        <td><%=EncodeHelper.javaStringToHtmlString(valueName)%></td>
      </tr>
	  <tr>
	<td class="txtlibform" width="30%"><%=resource.getString("pdcPeas.docsNumber")%> :</td>
	<td><%=valueNbDoc%></td>
     </tr>
	 <tr>
	 <td class="txtlibform" width="30%" valign="top"><%=resource.getString("pdcPeas.motherValue")%> :</td>
		<td>
			<select name="newFatherId" onChange="javaScript:sendData('ToMoveValueGetSisters')">
			<%
			// affiche l'arbre courant moins le subtree � d�placer et moins les valeurs pour lesquelles le user n'a pas de droit
			ArrayList axisValues = (ArrayList) axis.getValues();
			int valueLevel = -1;
			String increment = "";
			Value value = null;
			int levelRights = 1000;
			String newMother = "";

			for (int i = 0; i<axisValues.size(); i++)
			  {
				value = (Value) axisValues.get(i);

				newMother = "";
				if (value.getPK().getId().equals(newMotherId))
					newMother = "selected";

				// si la valeur = celle que l'on veut d�placer, on ne l'affiche pas
				// c'est � dire si le chemin commence par celui de la valeur � d�placer + son id
				if ( (!value.getPath().startsWith(valueToMove.getPath()+ valueToMove.getPK().getId() +"/")) && !(value.getPK().getId().equals(valueToMove.getPK().getId()) ) )
				{
						valueName = value.getName(translation);
						valueId = value.getPK().getId();
						valueLevel = value.getLevelNumber();
						increment = "";

						if ( valueLevel <= levelRights ) 		levelRights = 1000 ;
						if ( userRights != null && userRights.contains(valueId) && valueLevel < levelRights )		levelRights = valueLevel ;
						if ( ( levelRights < 1000 ) || ( kmAdmin ) )
						{
							for (int j = 0; j < valueLevel; j++)
							{
								increment += "&nbsp;&nbsp;&nbsp;";
							}
							out.println("<option value=\""+valueId+"\" "+newMother+">"+ increment + valueName +"</option>");
						}
				  }
			  }
			%>
			</select>
		</td>
      </tr>
	  <% if (sisters != null) { %>
	  <tr>
		<td class="txtlibform" width="30%" valign="top"><%=resource.getString("pdcPeas.sistersValue")%> :</span></td>
		<td>
			<select name="Order" size="5">
			<%
				Value		tempValue		= null; // pour affichage des options du tag select
				String		sisterValueName = null; // pour affichage des options du tag select
				Iterator	itSisters		= sisters.iterator(); // pour affichage des options du tag select
				String		order			= ""; // pour affichage des options du tag select

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
	  <% } %>
    </table>
 </FORM>
    <%
out.println(board.printAfter());
ButtonPane buttonPane = gef.getButtonPane();

if (StringUtil.isDefined(newMotherId))
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:sendData('MoveValue')", false));

buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close();", false));
out.println("<br/><center>"+buttonPane.print()+"</center>");
out.println(frame.printAfter());
out.println(window.printAfter());
%>

<view:progressMessage/>
</BODY>
</HTML>
<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkPdc.jsp"%>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view"%>
<%
// recuperation des parametres
Value	valueToMove		= (Value) request.getAttribute("Value");			// l'objet Value pour afficher ses informations
Axis	axis 			= (Axis) request.getAttribute("Axis");			// l'arbre
List<Value>	list			= (List<Value>) request.getAttribute("Path");
// le chemin complet ou l'on peut retrouver la valeur selectionnee
String	alreadyExist	= (String) request.getAttribute("AlreadyExist");	// La valeur existe deja.
String 	translation		= (String) request.getAttribute("Translation");
List<String> userRights 	= (List<String>) request.getAttribute("UserRights");
boolean kmAdmin 		= (Boolean) request.getAttribute("KMAdmin");
List<Value>	sisters			= (List<Value>) request.getAttribute("Sisters");
String	newMotherId		= (String) request.getAttribute("newFatherId");

if (translation == null || translation.equals("null"))
{
	translation = I18NHelper.getDefaultLanguage();
}
// initialisation des diff�rentes variables pour l'affichage
String valueName		= valueToMove.getName(translation);
String valueId			= valueToMove.getPK().getId();
int valueNbDoc 			= valueToMove.getNbObjects();
String errorMessage = null;

// Pour l'affichage du chemin complet
String completPath = buildCompletPath(list, false, 1, translation);

if ( (alreadyExist != null) && (alreadyExist.equals("1")) ){
	// Le nom de la valeur entrée par l'utilisateur existe deja
	errorMessage = "<font size=2 color=#FF6600><b>"+resource.getString("pdcPeas.valueAlreadyExist")+"</b></font>";
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%= language %>">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>

<script type="application/javascript">
function sendData(action) {
	document.moveValue.action = action;
	if (action === 'MoveValue') {
    jQuery.popup.confirm("<%=resource.getString("pdcPeas.confirmMoveValue")%>", function() {
			$.progressMessage();
			document.moveValue.submit();
		});
	} else {
		document.moveValue.submit();
	}
}
</script>
</HEAD>
<BODY>

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
    <table>
      <th></th>
	  <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
		<tr>
			<td style="text-align: center"><%=errorMessage%></td>
		</tr>
	  <% } %>
      <tr>
        <td class="txtlibform"><%=resource.getString("pdcPeas.path")%> :</td>
        <td><%=completPath%></td>
      </tr>
      <tr>
        <td class="txtlibform" style="width: 30%"><%=resource.getString("pdcPeas.value")%> :</td>
        <td><%=WebEncodeHelper.javaStringToHtmlString(valueName)%></td>
      </tr>
	  <tr>
	<td class="txtlibform" style="width: 30%"><%=resource.getString("pdcPeas.docsNumber")%> :</td>
	<td><%=valueNbDoc%></td>
     </tr>
	 <tr>
	 <td class="txtlibform" style="width: 30%">
     <label for="newFatherId"><%=resource.getString("pdcPeas.motherValue")%> :</label></td>
		<td>
			<select id="newFatherId" name="newFatherId" onChange="sendData('ToMoveValueGetSisters')">
			<%
			// affiche l'arbre courant moins le subtree � d�placer et moins les valeurs pour lesquelles le user n'a pas de droit
			List<Value> axisValues = axis.getValues();
			int valueLevel;
			StringBuilder increment;
			Value value;
			int levelRights = 1000;
			String newMother;

        for (Value axisValue : axisValues) {
          value = axisValue;

          newMother = "";
          if (value.getPK().getId().equals(newMotherId))
            newMother = "selected";

          // si la valeur = celle que l'on veut déplacer, on ne l'affiche pas,
          // c'est-à-dire si le chemin commence par celui de la valeur à déplacer + son id
          if ((!value.getPath().startsWith(valueToMove.getPath() + valueToMove.getPK().getId() + "/")) && !(value.getPK().getId().equals(valueToMove.getPK().getId()))) {
            valueName = value.getName(translation);
            valueId = value.getPK().getId();
            valueLevel = value.getLevelNumber();
            increment = new StringBuilder();

            if (valueLevel <= levelRights) levelRights = 1000;
            if (userRights != null && userRights.contains(valueId) && valueLevel < levelRights)
              levelRights = valueLevel;
            if ((levelRights < 1000) || (kmAdmin)) {
              increment.append("&nbsp;&nbsp;&nbsp;".repeat(Math.max(0, valueLevel)));
              out.println("<option value=\"" + valueId + "\" " + newMother + ">" + increment + valueName + "</option>");
            }
          }
        }
			%>
			</select>
		</td>
      </tr>
	  <% if (sisters != null) { %>
	  <tr>
		<td class="txtlibform" style="width: 30%">
      <label for="order"><%=resource.getString("pdcPeas.sistersValue")%> :</label></td>
		<td>
			<select id="order" name="Order" size="5">
			<%
				Value		tempValue; // pour affichage des options du tag select
				String		sisterValueName; // pour affichage des options du tag select
				Iterator<Value>	itSisters		= sisters.iterator();
        // pour affichage des options du tag select
				String		order			= ""; // pour affichage des options du tag select

				if (!sisters.isEmpty()){
					// affiche les soeurs de la valeur courante
					while (itSisters.hasNext()){
						tempValue = itSisters.next();
						sisterValueName = WebEncodeHelper.javaStringToHtmlString(tempValue.getName(translation));
						order = Integer.toString(tempValue.getOrderNumber());
						out.println("<option value=\""+sisterValueName+sepOptionValueTag+order+"\">"+sisterValueName+"</option>");
					}
					// calcul le dernier ordre
					int newOrder_tmp = Integer.parseInt(order) + 1;
					String newOrder = Integer.toString(newOrder_tmp);
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
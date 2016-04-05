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

<%@page import="org.silverpeas.core.util.UnitUtil"%>
<%@ page import="org.silverpeas.core.util.memory.MemoryData" %>
<%@ page import="org.silverpeas.core.util.memory.MemoryUnit" %>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="check.jsp" %>

<%
  SpaceInst	space				= (SpaceInst) request.getAttribute("Space");
String		translation 		= (String) request.getParameter("Translation");
boolean 	isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();
boolean isUserAdmin = ((Boolean)request.getAttribute("isUserAdmin")).booleanValue();

// Component space quota
boolean isComponentSpaceQuotaActivated = isUserAdmin && JobStartPagePeasSettings.componentsInSpaceQuotaActivated;
String componentSpaceQuotaMaxCount = "";
if (isComponentSpaceQuotaActivated) {
  componentSpaceQuotaMaxCount = String.valueOf(space.getComponentSpaceQuota().getMaxCount());
}

// Data storage quota
boolean isDataStorageQuotaActivated = isUserAdmin && JobStartPagePeasSettings.dataStorageInSpaceQuotaActivated;
String dataStorageQuotaMaxCount = "";
if (isDataStorageQuotaActivated) {
  dataStorageQuotaMaxCount = String.valueOf(UnitUtil.convertTo(space.getDataStorageQuota().getMaxCount(),
      MemoryUnit.B, MemoryUnit.MB));
}

browseBar.setSpaceId(space.getId());
browseBar.setPath(resource.getString("JSPP.updateSpace"));
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script type="text/javascript">
function B_VALIDER_ONCLICK() {
	if (isCorrectForm()) {
		document.infoSpace.submit();
	}
}

function isCorrectForm() {
	var errorMsg = "";
	var errorNb = 0;

		var name = stripInitialWhitespace(document.infoSpace.NameObject.value);
		var desc = document.infoSpace.Description;

    if (isWhitespace(name)) {
			errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("MustContainsText")%>\n";
			errorNb++;
		}

	var textAreaLength = 400;
		var s = desc.value;
		if (! (s.length <= textAreaLength)) {
		errorMsg+="  - '<%=resource.getString("GML.description")%>' <%=resource.getString("ContainsTooLargeText")+"400 "+resource.getString("Characters")%>\n";
		errorNb++;
		}

    <% if (isComponentSpaceQuotaActivated) { %>
     var componentSpaceQuota = document.infoSpace.ComponentSpaceQuota.value;
     if (isWhitespace(componentSpaceQuota)) {
       errorMsg += "  - '<%=resource.getString("JSPP.componentSpaceQuotaMaxCount")%>' <%=resource.getString("MustContainsText")%>\n";
       errorNb++;
     }
    <% } %>

    <% if (isDataStorageQuotaActivated) { %>
      var dataStorageQuota = document.infoSpace.DataStorageQuota.value;
      if (isWhitespace(dataStorageQuota)) {
        errorMsg += "  - '<%=resource.getString("JSPP.dataStorageQuota")%>' <%=resource.getString("MustContainsText")%>\n";
        errorNb++;
      }
    <% } %>

     switch(errorNb)
     {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resource.getString("ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resource.getString("ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

<%
String lang = "";
Iterator<String> codes = space.getTranslations().keySet().iterator();
while (codes.hasNext()) {
	lang = codes.next();
	out.println("var name_"+lang+" = \""+EncodeHelper.javaStringToJsString(space.getName(lang))+"\";\n");
	out.println("var desc_"+lang+" = \""+EncodeHelper.javaStringToJsString(space.getDescription(lang))+"\";\n");
}
%>

function showTranslation(lang) {
	showFieldTranslation('spaceName', 'name_'+lang);
	showFieldTranslation('spaceDescription', 'desc_'+lang);
}

function removeTranslation() {
	document.infoSpace.submit();
}
</script>
</head>
<body onload="document.infoSpace.NameObject.focus();">
<form name="infoSpace" action="EffectiveUpdateSpace" method="post">
<%
	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
	<table border="0" cellspacing="0" cellpadding="5" width="100%">
		<%=I18NHelper.getFormLine(resource, space, translation)%>
		<tr>
			<td class="txtlibform"><%=resource.getString("GML.name")%> :</td>
			<td><input type="text" id="spaceName" name="NameObject" size="60" maxlength="60" value="<%=EncodeHelper.javaStringToHtmlString(space.getName(translation))%>"/>&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0"/></td>
		</tr>
		<tr>
			<td class="txtlibform" valign="top"><%=resource.getString("GML.description")%> :</td>
			<td><textarea id="spaceDescription" name="Description" rows="4" cols="49"><%=EncodeHelper.javaStringToHtmlString(space.getDescription(translation))%></textarea></td>
		</tr>
    <% if (isComponentSpaceQuotaActivated) { %>
      <tr>
        <td class="txtlibform"><%=resource.getString("JSPP.componentSpaceQuotaMaxCount")%> :</td>
        <td><input type="text" name="ComponentSpaceQuota" size="5" maxlength="4" value="<%=componentSpaceQuotaMaxCount%>"/>&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0"/> <%=resource.getString("JSPP.componentSpaceQuotaMaxCountHelp")%></td>
      </tr>
    <% } %>
    <% if (isDataStorageQuotaActivated) { %>
      <tr>
        <td class="txtlibform"><%=resource.getString("JSPP.dataStorageQuota")%> :</td>
        <td><input type="text" id="spaceDataStorageQuota" name="DataStorageQuota" size="9" maxlength="10" value="<%=dataStorageQuotaMaxCount%>">&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0"> <%=resource.getString("JSPP.dataStorageQuotaHelp")%></td>
      </tr>
    <% } %>
		<% if (isInHeritanceEnable && !space.isRoot()) { %>
		<tr>
			<td class="textePetitBold" nowrap="nowrap" valign="top"><%=resource.getString("JSPP.inheritanceBlockedComponent") %> :</td>
			<td align="left" valign="top" width="100%">
			<% if (space.isInheritanceBlocked()) { %>
				<input type="radio" name="InheritanceBlocked" value="true" checked="checked" /> <%=resource.getString("JSPP.inheritanceSpaceNotUsed")%><br/>
				<input type="radio" name="InheritanceBlocked" value="false" /> <%=resource.getString("JSPP.inheritanceSpaceUsed")%>
			<% } else { %>
				<input type="radio" name="InheritanceBlocked" value="true"/> <%=resource.getString("JSPP.inheritanceSpaceNotUsed")%><br/>
				<input type="radio" name="InheritanceBlocked" value="false" checked="checked" /> <%=resource.getString("JSPP.inheritanceSpaceUsed")%>
			<% } %>
			</td>
		</tr>
		<% } %>
		<tr>
			<td colspan="2"><img border="0" src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5"/> : <%=resource.getString("GML.requiredField")%></td>
		</tr>
	</table>
<%
	out.println(board.printAfter());

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:onclick=history.back()", false));
	out.println("<br/><center>"+buttonPane.print()+"</center>");

	out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</form>
</body>
</html>
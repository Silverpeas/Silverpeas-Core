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
<%@page import="org.silverpeas.core.util.UnitUtil"%>
<%@ page import="org.silverpeas.core.util.memory.MemoryUnit" %>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="check.jsp" %>

<c:set var="space" value="${requestScope.Space}"/>
<jsp:useBean id="space" type="org.silverpeas.core.admin.space.SpaceInst"/>

<%
String		translation 		= request.getParameter("Translation");
boolean isInheritanceEnable = JobStartPagePeasSettings.IS_INHERITANCE_ENABLED
        && (Boolean) request.getAttribute("inheritanceSupported");
boolean isUserAdmin = (Boolean) request.getAttribute("isUserAdmin");

// Component space quota
boolean isComponentSpaceQuotaActivated = isUserAdmin && JobStartPagePeasSettings.COMPONENTS_IN_SPACE_QUOTA_ENABLED;
String componentSpaceQuotaMaxCount = "";
if (isComponentSpaceQuotaActivated) {
  componentSpaceQuotaMaxCount = String.valueOf(space.getComponentSpaceQuota().getMaxCount());
}

// Data storage quota
boolean isDataStorageQuotaActivated = isUserAdmin && JobStartPagePeasSettings.DATA_STORAGE_IN_SPACE_QUOTA_ENABLED;
String dataStorageQuotaMaxCount = "";
if (isDataStorageQuotaActivated) {
  dataStorageQuotaMaxCount = String.valueOf(UnitUtil.convertTo(space.getDataStorageQuota().getMaxCount(),
      MemoryUnit.B, MemoryUnit.MB));
}

browseBar.setSpaceId(space.getId());
browseBar.setPath(resource.getString("JSPP.updateSpace"));
%>

<view:sp-page>
<view:sp-head-part withCheckFormScript="true">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script type="text/javascript">
function B_VALIDER_ONCLICK() {
	ifCorrectFormExecute(function() {
		document.infoSpace.submit();
  });
}
function B_CANCEL_ONCLICK() {
  if(typeof parent.jumpToSpace === 'function') {
    parent.jumpToSpace('${space.id}');
  } else {
    history.back();
  }
}

function ifCorrectFormExecute(callback) {
  let errorMsg = "";
  let errorNb = 0;

  const name = stripInitialWhitespace(document.infoSpace.NameObject.value);
  const desc = document.infoSpace.Description;

  if (isWhitespace(name)) {
    errorMsg += "  - '<%=resource.getString("GML.name")%>' <%=resource.getString("MustContainsText")%>\n";
    errorNb++;
  }

  const textAreaLength = 400;
  const s = desc.value;
  if (!(s.length <= textAreaLength)) {
    errorMsg += "  - '<%=resource.getString("GML.description")%>' <%=resource.getString("ContainsTooLargeText")+"400 "+resource.getString("Characters")%>\n";
    errorNb++;
  }

  <% if (isComponentSpaceQuotaActivated) { %>
  const componentSpaceQuota = document.infoSpace.ComponentSpaceQuota.value;
  if (isWhitespace(componentSpaceQuota)) {
    errorMsg += "  - '<%=resource.getString("JSPP.componentSpaceQuotaMaxCount")%>' <%=resource.getString("MustContainsText")%>\n";
    errorNb++;
  }
  <% } %>

  <% if (isDataStorageQuotaActivated) { %>
  const dataStorageQuota = document.infoSpace.DataStorageQuota.value;
  if (isWhitespace(dataStorageQuota)) {
    errorMsg += "  - '<%=resource.getString("JSPP.dataStorageQuota")%>' <%=resource.getString("MustContainsText")%>\n";
    errorNb++;
  }
  <% } %>

  switch (errorNb) {
    case 0 :
      callback.call(this);
      break;
    case 1 :
      errorMsg = "<%=resource.getString("ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
      jQuery.popup.error(errorMsg);
      break;
    default :
      errorMsg = "<%=resource.getString("ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
      jQuery.popup.error(errorMsg);
  }
}

<%
String lang = "";
for(String s: space.getTranslations().keySet()){
	lang = s;
	out.println("var name_"+lang+" = \""+WebEncodeHelper.javaStringToJsString(space.getName(lang))+"\";\n");
	out.println("var desc_"+lang+" = \""+WebEncodeHelper.javaStringToJsString(space.getDescription(lang))+"\";\n");
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
<fmt:message key="GML.mandatory" var="mandatory"/>
</view:sp-head-part>
<view:sp-body-part cssClass="page_content_admin" onLoad="document.infoSpace.NameObject.focus();">
<form name="infoSpace" action="EffectiveUpdateSpace" method="post">
<%
	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
	<table>
        <th></th>
		<%=I18NHelper.getFormLine(resource, space, translation)%>
		<tr>
			<td class="txtlibform"><%=resource.getString("GML.name")%> :</td>
			<td><input type="text" id="spaceName" name="NameObject" size="60" maxlength="60"
                       value="<%=WebEncodeHelper.javaStringToHtmlString(space.getName(translation))%>"/>&nbsp;<img alt="${mandatory}" src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5"/></td>
		</tr>
		<tr>
			<td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
			<td><textarea id="spaceDescription" name="Description" rows="4" cols="49"><%=WebEncodeHelper.javaStringToHtmlString(space.getDescription(translation))%></textarea></td>
		</tr>
    <% if (isComponentSpaceQuotaActivated) { %>
      <tr>
        <td class="txtlibform"><%=resource.getString("JSPP.componentSpaceQuotaMaxCount")%> :</td>
        <td><input type="text" name="ComponentSpaceQuota" size="5" maxlength="4" value="<%=componentSpaceQuotaMaxCount%>"/>&nbsp;<img alt="${mandatory}" src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5"/> <%=resource.getString("JSPP.componentSpaceQuotaMaxCountHelp")%></td>
      </tr>
    <% } %>
    <% if (isDataStorageQuotaActivated) { %>
      <tr>
        <td class="txtlibform"><%=resource.getString("JSPP.dataStorageQuota")%> :</td>
        <td><input type="text" id="spaceDataStorageQuota" name="DataStorageQuota" size="9" maxlength="10" value="<%=dataStorageQuotaMaxCount%>">&nbsp;<img alt="${mandatory}" src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5"> <%=resource.getString("JSPP.dataStorageQuotaHelp")%></td>
      </tr>
    <% } %>
		<% if (isInheritanceEnable && !space.isRoot()) { %>
		<tr>
			<td class="textePetitBold"><%=resource.getString("JSPP.inheritanceBlockedComponent") %> :</td>
			<td>
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
			<td colspan="2"><img alt="${mandatory}" src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5"/> : <%=resource.getString("GML.requiredField")%></td>
		</tr>
	</table>
<%
	out.println(board.printAfter());

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:onclick=B_CANCEL_ONCLICK()", false));
	out.println("<br/><center>"+buttonPane.print()+"</center>");

	out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</form>
</view:sp-body-part>
</view:sp-page>
